package org.springframework.feign.codec;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author shanhuiming
 *
 */
@Data
public class RemoteChain {

    public static final ThreadLocal<RemoteChainHolder> CHAIN = new ThreadLocal<>();

    // <threadName, <url, RemoteChain>>
    public static final ConcurrentMap<String, ConcurrentMap<String, RemoteChain>> ASYNC_CHAIN = new ConcurrentHashMap<>();

    @JsonIgnore
    @JSONField(serialize = false)
    private String name;

    @JsonIgnore
    @JSONField(serialize = false)
    private String url;

    @JsonIgnore
    @JSONField(serialize = false)
    private AtomicInteger succs = new AtomicInteger(0);

    @JsonIgnore
    @JSONField(serialize = false)
    private AtomicInteger count = new AtomicInteger(1);

    @JsonIgnore
    @JSONField(serialize = false)
    private AtomicLong cost = new AtomicLong(0);

    @JsonIgnore
    @JSONField(serialize = false)
    private boolean async;

    @JsonIgnore
    @JSONField(serialize = false)
    private int httpCode;

    @JsonIgnore
    @JSONField(serialize = false)
    private String code;

    private String detail;

    private List<RemoteChain> children;

    public void sumCost(long cost2){
        this.cost.getAndAdd(cost2);
    }

    public void increaseCount(){
        this.count.incrementAndGet();
    }

    public void increaseSuccs(){
        this.succs.incrementAndGet();
    }

    public String getDetail(){
        if(detail != null){
            return detail;
        }
        StringBuilder builder = new StringBuilder();
        if(async){
            builder.append("*");
        }
        builder.append("[");
        builder.append(succs).append("/").append(count);
        builder.append(" ").append(httpCode).append("|").append(code);
        builder.append(" ").append(cost).append("ms");
        if(name != null){
            builder.append(" ").append(name);
        }
        builder.append("]").append(" ").append(url);
        return builder.toString();
    }

    public static void buildeTree(String prefix, List<RemoteChain> chains, StringBuilder builder) {
        if (chains != null) {
            for (int i = 0; i < chains.size(); i++) {
                RemoteChain chain = chains.get(i);
                String newPrefix = prefix + (i == chains.size() - 1 ? "└─ " : "├─ ");
                builder.append(newPrefix).append(chain.getDetail());
                if (chain.getChildren() != null) {
                    builder.append("\n");
                    buildeTree(prefix + (i == chains.size() - 1 ? "    " : "│   "), chain.getChildren(), builder);
                }
                if(i < chains.size() - 1){
                    builder.append("\n");
                }
            }
        }
    }

    public static void appendChain(boolean success, String name, String url, long cost, int httpCode, String code, List<RemoteChain> next){
        RequestAttributes attributes =  RequestContextHolder.getRequestAttributes();
        String threadName = Thread.currentThread().getName();
        if(attributes == null && (threadName == null || !threadName.endsWith("-async"))){
            // 如果不是servlet或其子线程，就忽略，'-async'是一个异步线程的后缀约定
            return;
        }

        // 去掉url中的参数
        int index = url.indexOf("?");
        if(index != -1){
            url = url.substring(0, index);
        }

        if(attributes != null){
            // 如果时servlet线程，直接记到当前ThreadLocal中
            RemoteChainHolder remoteChainHolder = CHAIN.get();
            if(remoteChainHolder == null){
                remoteChainHolder = new RemoteChainHolder(threadName);
                CHAIN.set(remoteChainHolder);
                // 这里不能清除ASYNC_CHAIN，不然第一个同步远程调用肯定会清掉所有已经远程调用过的记录
                // 但不删的话，如果远程调用全是异步，那么又会漏删ASYNC_CHAIN，所以在response中也要兜底删除
            }else if(!remoteChainHolder.getHolderName().equals(threadName)){
                // 说明线程被复用了，前面的请求已经结束了
                remoteChainHolder = new RemoteChainHolder(threadName);
                CHAIN.set(remoteChainHolder);
                ASYNC_CHAIN.remove(threadName);
            }

            ArrayList<RemoteChain> chainList = remoteChainHolder.getChains();
            if(chainList == null){
                chainList = new ArrayList<>();
                remoteChainHolder.setChains(chainList);
            }

            if(!chainList.isEmpty()){
                RemoteChain lastChain = chainList.get(chainList.size() - 1);
                // 去掉参数，如果url一样则累计次数（这里忽略了一种场景：参数不同调用链不一样）
                if(url.equals(lastChain.getUrl())){
                    lastChain.increaseCount();
                    lastChain.sumCost(cost);
                    if(success){
                        lastChain.increaseSuccs();
                    }else{
                        // 累计次数时，如果失败则更新code（所以只能看的最后一次失败的code）
                        lastChain.setHttpCode(httpCode);
                        lastChain.setCode(code);
                    }
                    return;
                }
            }
            chainList.add(newChain(success, name, url, cost, httpCode, code, next));
        }else if(threadName.endsWith("-async")){ // 约定对于请求处理的异步线程，以-async作为线程名后缀
            // 如果是servlet子线程，则尝试记到全局Map中（如果不存在，则可能是servlet请求处理已经结束而被清除了，所以忽略）
            threadName = threadName.substring(0, threadName.length() - 6);
            Map<String, RemoteChain> chainMap = ASYNC_CHAIN.get(threadName);
            if(chainMap == null){
                return;
            }

            RemoteChain chain = chainMap.get(url);
            if(chain == null){
                return;
            }

            // 异步调用在开始时已经记录了，这里直接累计就行
            if(success){
                chain.increaseSuccs();
            }
            chain.sumCost(cost);
            chain.setHttpCode(httpCode);
            chain.setCode(code);
            chain.setChildren(next);
        }
    }

    public static RemoteChain newChain(boolean success, String name, String url, long cost, int httpCode, String code, List<RemoteChain> next){
        RemoteChain chain = new RemoteChain();
        if(success){
            chain.setSuccs(new AtomicInteger(1));
        }
        chain.setName(name);
        chain.setUrl(url);
        chain.setCost(new AtomicLong(cost));
        chain.setHttpCode(httpCode);
        chain.setCode(code);
        chain.setChildren(next);
        return chain;
    }
}
