package org.springframework.feign.codec;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;

/**
 *
 * @author shanhuiming
 *
 */
@Data
public class RemoteChain {

    public static final ThreadLocal<ArrayList<RemoteChain>> CHAIN = new ThreadLocal<>();

    @JsonIgnore
    @JSONField(serialize = false)
    private String name;

    @JsonIgnore
    @JSONField(serialize = false)
    private String url;

    @JsonIgnore
    @JSONField(serialize = false)
    private int succs;

    @JsonIgnore
    @JSONField(serialize = false)
    private int count = 1;

    @JsonIgnore
    @JSONField(serialize = false)
    private long cost;

    @JsonIgnore
    @JSONField(serialize = false)
    private boolean async;

    @JsonIgnore
    @JSONField(serialize = false)
    private String code;

    @JsonIgnore
    @JSONField(serialize = false)
    private int httpCode;

    private String detail;

    private ArrayList<RemoteChain> children;

    public void increaseCost(long cost2){
        this.cost = cost + cost2;
    }

    public void increaseCount(){
        this.count++;
    }

    public void increaseSuccs(){
        this.succs++;
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

    public static void buildeTree(String prefix, ArrayList<RemoteChain> chains, StringBuilder builder) {
        if (chains != null) {
            for (int i = 0; i < chains.size(); i++) {
                RemoteChain chain = chains.get(i);
                String newPrefix = prefix + (i == chains.size() - 1 ? "└─ " : "├─ ");
                builder.append(newPrefix).append(chain.getDetail()).append("\n");
                if (chain.getChildren() != null) {
                    buildeTree(prefix + (i == chains.size() - 1 ? "    " : "│   "), chain.getChildren(), builder);
                }
            }
        }
    }

    public static void appendChain(boolean success, String name, String url, long cost, int httpCode, String code, ArrayList<RemoteChain> next){
        int index = url.indexOf("?");
        if(index != -1){
            url = url.substring(0, index);
        }

        ArrayList<RemoteChain> chainList = CHAIN.get();
        if(chainList == null){
            chainList = new ArrayList<>();
            CHAIN.set(chainList);
        }else{
            RemoteChain preChain = chainList.get(chainList.size() - 1);
            if(url.equals(preChain.getUrl())){
                preChain.increaseCount();
                preChain.increaseCost(cost);
                if(success){
                    preChain.increaseSuccs();
                }else{
                    // 只在失败时更新code
                    preChain.setHttpCode(httpCode);
                    preChain.setCode(code);
                }
                return;
            }
        }

        RemoteChain chain = new RemoteChain();
        if(success){
            chain.setSuccs(1);
        }
        chain.setName(name);
        chain.setUrl(url);
        chain.setCost(cost);
        chain.setHttpCode(httpCode);
        chain.setCode(code);
        chain.setChildren(next);
        chainList.add(chain);
    }
}
