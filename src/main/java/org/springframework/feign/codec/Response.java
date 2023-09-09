package org.springframework.feign.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
public class Response<T> {

	/** 请求Id */
	private String requestId;

	/** 响应描述 */
	private String msg;

	/** 响应码 */
	private int code;

	/** 响应数据 */
	private T data;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Response(){

	}

	public Response(ResponseCode responseCode){
		this.code = responseCode.getCode();
		this.msg = responseCode.getDesc();
	}

	public Response(ResponseCode responseCode, T data){
		this.code = responseCode.getCode();
		this.msg = responseCode.getDesc();
		this.data = data;
	}

	private Response(int code, T data){
		this.code = code;
		this.data = data;
	}

	private Response(int code, T data, String msg){
		this.code = code;
		this.data = data;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "{requestId=" + requestId + ", code=" + code + ", msg=" + msg + ", data=" + data + "}";
	}

	public static <V> Response<V> response(int code, V data){
		return new Response<>(code, data);
	}

	public static <V> Response<V> response(int code, V data, String msg){
		return new Response<>(code, data, msg);
	}

	public static <V> Response<V> success(){
        return new Response<>(ResponseCode.OK);
    }

	public static <V> Response<V> success(V data){
        return new Response<>(ResponseCode.OK, data);
    }

	public static <V> Response<V> success(String msg, V data){
		Response<V> response = new Response<>(ResponseCode.OK, data);
		response.msg = msg;
		return response;
	}

	public static <V> Response<V> error(){
		return new Response<>(ResponseCode.INTERNAL_SERVER_ERROR);
	}

	public static <V> Response<V> error(ResponseCode responseCode){
		return new Response<>(responseCode);
	}

	public static <V> Response<V> error(String msg){
		Response<V> response = new Response<>(ResponseCode.INTERNAL_SERVER_ERROR);
		response.msg = msg;
		return response;
	}

	public static <V> Response<V> error(ResponseCode responseCode, String msg){
		Response<V> response = new Response<>(responseCode);
		response.msg = msg;
		return response;
	}

	public static <E> Response<Page<E>> page(List<E> list){
    	Response<Page<E>> response = new Response<>(ResponseCode.OK);
    	if(list == null){
        	list = new ArrayList<>();
        }

    	if(list instanceof com.github.pagehelper.Page<E> page){
			response.setData(new Page<>(page, page.getTotal()));
    	}else {
    		response.setData(new Page<>(list, list.size()));
    	}
    	return response;
    }

	public static class Page<E> {

		/** 总数 */
		private int total;

		/** 列表数据 */
		private Collection<E> list;

		public Page(){

		}

		public Page(Collection<E> list, int total){
			this.list = list;
			this.total = total;
		}

		public Page(Collection<E> list, long total){
			this.list = list;
			this.total = (int)total;
		}

		public int getTotal() {
			return total;
		}

		public void setTotalRows(int total) {
			this.total = total;
		}

		public Collection<E> getList() {
			return list;
		}

		public void setList(Collection<E> list) {
			this.list = list;
		}

		@Override
		public String toString() {
			return "{total=" + total + ", list=" + list + "}";
		}
	}
}
