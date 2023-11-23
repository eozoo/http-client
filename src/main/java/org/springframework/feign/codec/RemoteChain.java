package org.springframework.feign.codec;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.LinkedList;

/**
 *
 * @author shanhuiming
 *
 */
@Data
public class RemoteChain {

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
    private int count;

    @JsonIgnore
    @JSONField(serialize = false)
    private int cost;

    private String detail;

    private LinkedList<RemoteChain> children;

    public String getDetail(){
        if(detail != null){
            return detail;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(succs).append("/").append(count);
        builder.append(" ").append(cost).append("ms");
        if(name != null){
            builder.append(" ").append(name);
        }
        builder.append("]").append(url);
        return builder.toString();
    }

    public static void buildeTree(String prefix, LinkedList<RemoteChain> chains, StringBuilder builder) {
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
}
