package org.springframework.feign.invoke.method;

import feign.Param;
import feign.RequestTemplate;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignMethodMetadata implements Serializable {
    private String configKey;
    private transient Type returnType;
    private Integer urlIndex;
    private Integer bodyIndex;
    private Integer headerMapIndex;
    private Integer queryMapIndex;
    private boolean queryMapEncoded;
    private transient Type bodyType;
    private final RequestTemplate template = new RequestTemplate();
    private final List<String> formParams = new ArrayList<>();
    private final Map<Integer, Collection<String>> indexToName = new LinkedHashMap<>();
    private final Map<Integer, Class<? extends Param.Expander>> indexToExpanderClass = new LinkedHashMap<>();
    private transient Map<Integer, Param.Expander> indexToExpander;
    private int connectTimeoutMillis = -1;
    private int readTimeoutMillis = -1;
    private Integer hostIndex;
    private Integer multipartFileIndex;
    private String multipartFileName;
    private String multipartFileBoundary;
    private Integer multipartFormIndex;

    FeignMethodMetadata() {
    }

    public String configKey() {
        return configKey;
    }

    public FeignMethodMetadata configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public Type returnType() {
        return returnType;
    }

    public FeignMethodMetadata returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public int connectTimeoutMillis(){
        return connectTimeoutMillis;
    }

    public FeignMethodMetadata connectTimeoutMillis(int connectTimeoutMillis){
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    public int readTimeoutMillis(){
        return readTimeoutMillis;
    }

    public FeignMethodMetadata readTimeoutMillis(int readTimeoutMillis){
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    public Integer hostIndex(){
        return hostIndex;
    }

    public FeignMethodMetadata hostIndex(Integer hostIndex){
        this.hostIndex = hostIndex;
        return this;
    }

    public Integer multipartFileIndex(){
        return multipartFileIndex;
    }

    public FeignMethodMetadata multipartFileIndex(Integer multipartFileIndex){
        this.multipartFileIndex = multipartFileIndex;
        return this;
    }

    public String multipartFileName(){
        return multipartFileName;
    }

    public FeignMethodMetadata multipartFileName(String multipartFileName){
        this.multipartFileName = multipartFileName;
        return this;
    }

    public String multipartFileBoundary(){
        return multipartFileBoundary;
    }

    public FeignMethodMetadata multipartFileBoundary(String multipartFileBoundary){
        this.multipartFileBoundary = multipartFileBoundary;
        return this;
    }

    public Integer multipartFormIndex(){
        return multipartFormIndex;
    }

    public FeignMethodMetadata multipartFormIndex(Integer multipartFormIndex){
        this.multipartFormIndex = multipartFormIndex;
        return this;
    }

    public Integer urlIndex() {
        return urlIndex;
    }

    public FeignMethodMetadata urlIndex(Integer urlIndex) {
        this.urlIndex = urlIndex;
        return this;
    }

    public Integer bodyIndex() {
        return bodyIndex;
    }

    public FeignMethodMetadata bodyIndex(Integer bodyIndex) {
        this.bodyIndex = bodyIndex;
        return this;
    }

    public Integer headerMapIndex() {
        return headerMapIndex;
    }

    public FeignMethodMetadata headerMapIndex(Integer headerMapIndex) {
        this.headerMapIndex = headerMapIndex;
        return this;
    }

    public Integer queryMapIndex() {
        return queryMapIndex;
    }

    public FeignMethodMetadata queryMapIndex(Integer queryMapIndex) {
        this.queryMapIndex = queryMapIndex;
        return this;
    }

    public boolean queryMapEncoded() {
        return queryMapEncoded;
    }

    public FeignMethodMetadata queryMapEncoded(boolean queryMapEncoded) {
        this.queryMapEncoded = queryMapEncoded;
        return this;
    }

    /**
     * Type corresponding to {@link #bodyIndex()}.
     */
    public Type bodyType() {
        return bodyType;
    }

    public FeignMethodMetadata bodyType(Type bodyType) {
        this.bodyType = bodyType;
        return this;
    }

    public RequestTemplate template() {
        return template;
    }

    public List<String> formParams() {
        return formParams;
    }

    public Map<Integer, Collection<String>> indexToName() {
        return indexToName;
    }

    /**
     * If {@link #indexToExpander} is null, classes here will be instantiated by newInstance.
     */
    public Map<Integer, Class<? extends Param.Expander>> indexToExpanderClass() {
        return indexToExpanderClass;
    }

    /**
     * After {@link #indexToExpanderClass} is populated, this is set by contracts that support
     * runtime injection.
     */
    public FeignMethodMetadata indexToExpander(Map<Integer, Param.Expander> indexToExpander) {
        this.indexToExpander = indexToExpander;
        return this;
    }

    /**
     * When not null, this value will be used instead of {@link #indexToExpander()}.
     */
    public Map<Integer, Param.Expander> indexToExpander() {
        return indexToExpander;
    }
}
