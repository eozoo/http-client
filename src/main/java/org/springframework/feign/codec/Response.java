package org.springframework.feign.codec;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@Data
public class Response<T> {

	/** 请求Id */
	private String requestId;

	/** 响应码 */
	private int code;

	/** 响应数据 */
	private T data;

	/** 响应描述 */
	private String msg;

	/** 错误堆栈信息 */
	private List<String> cause;

	/** 调用链 **/
	private List<RemoteChain> chains;

	public Response(){
		// 收到响应时会自动进行json反序列化，直接获取chains
	}

	private Response(int code, String msg, T data){
		this.code = code;
		this.msg = msg;
		this.data = data;

		RemoteChainHolder holder = RemoteChain.CHAIN.get(); // 当前请求的同步远程调用记录
		RemoteChain.CHAIN.remove();
		if(holder != null){
			this.chains = holder.getChains();
		}

		String threadName = Thread.currentThread().getName(); // 请求对应的异步远程调用记录
		Map<String, RemoteChain> chainMap = RemoteChain.ASYNC_CHAIN.remove(threadName);
		if(chainMap != null){
			if(this.chains == null){
				this.chains = chainMap.values().stream().toList();
			}else{
				this.chains.addAll(chainMap.values());
			}
		}
	}

	@Override
	public String toString() {
		return "{code=" + code + ", msg=" + msg + ", data=" + data + "}";
	}

	public static <V> Response<V> code(ResponseCode responseCode){
		Response<V> response = new Response<>(responseCode.code(), responseCode.msg(), null);
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(attributes != null){
			HttpServletResponse httpResponse = attributes.getResponse();
			if(httpResponse != null){
				httpResponse.setStatus(responseCode.status());
			}
		}
		return response;
	}

	public static <V> Response<V> data(ResponseCode responseCode, V data){
		Response<V> response = new Response<>(responseCode.code(), responseCode.msg(), data);
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(attributes != null){
			HttpServletResponse httpResponse = attributes.getResponse();
			if(httpResponse != null){
				httpResponse.setStatus(responseCode.status());
			}
		}
		return response;
	}

	public static <V> Response<V> msg(ResponseCode responseCode, String msg){
		Response<V> response = new Response<>(responseCode.code(), msg, null);
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(attributes != null){
			HttpServletResponse httpResponse = attributes.getResponse();
			if(httpResponse != null){
				httpResponse.setStatus(responseCode.status());
			}
		}
		return response;
	}

	public static <V> Response<V> success(){
        return new Response<>(HttpStatus.OK.value(), "success", null);
    }

	public static <V> Response<V> success(V data){
        return new Response<>(HttpStatus.OK.value(), "success", data);
    }

	public static <V> Response<V> success(V data, String msg){
		return new Response<>(HttpStatus.OK.value(), msg, data);
	}

	public static <V> Response<V> error(){
		return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null);
	}

	public static <V> Response<V> error(String msg){
		return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, null);
	}

	public static <E> Response<Page<E>> page(List<E> list){
    	Response<Page<E>> response = new Response<>(HttpStatus.OK.value(), "success", null);
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

	public static <E> Response<Page<E>> page(com.baomidou.mybatisplus.extension.plugins.pagination.Page<E> page){
		Response<Page<E>> response = new Response<>(HttpStatus.OK.value(), "success", null);
		response.setData(new Page<>(page.getRecords(), page.getTotal()));
		return response;
	}

	public static <T, E> Response<Page<E>> page(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page, Class<E> clazz){
		Response<Page<E>> response = new Response<>(HttpStatus.OK.value(), "success", null);
		response.setData(new Page<>(copyList(page.getRecords(), clazz), page.getTotal()));
		return response;
	}

	public static <T, E> Response<Page<E>> page(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page, Function<T, E> mapper) {
		Response<Page<E>> response = new Response<>(HttpStatus.OK.value(), "success", null);
		response.setData(new Page<>(page.getRecords().stream().map(mapper).toList(), page.getTotal()));
		return response;
	}

	public static <T, E> Response<Page<E>> page(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page, Function<T, E> mapper, Predicate<T> filter) {
		Response<Page<E>> response = new Response<>(HttpStatus.OK.value(), "success", null);
		response.setData(new Page<>(page.getRecords().stream().filter(filter).map(mapper).toList(), page.getTotal()));
		return response;
	}

	static <E, T> E copyBean(T src, Class<E> clazz) {
		E target = null;
		try {
			target = clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		BeanUtils.copyProperties(src, target);
		return target;
	}

	static <E, T> List<E> copyList(List<T> srcList, Class<E> clazz) {
		if (CollectionUtils.isEmpty(srcList)) {
			return Collections.emptyList();
		}
		return srcList.stream().map(src -> copyBean(src, clazz)).toList();
	}

	public static class Page<E> {

		/** 总数 */
		private int total;

		/** 列表数据 */
		private Collection<E> list;

		/** 页码 */
		private int page;

		/** 每页行数 */
		private int pageSize;

		/** 总页数 */
		private int totalPage;

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

		@Deprecated
		public void setTotalRows(int total) {
			this.total = total;
		}

		@Deprecated
		public void setTotalRows(long total) {
			this.total = (int)total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public void setTotal(long total) {
			this.total = (int)total;
		}

		public int getTotalPage() {
			return totalPage;
		}

		public void setTotalPage(int totalPage) {
			this.totalPage = totalPage;
		}

		public void setTotalPage(long totalPage) {
			this.totalPage = (int)totalPage;
		}

		public Collection<E> getList() {
			return list;
		}

		public void setList(Collection<E> list) {
			this.list = list;
		}

		public int getPage() {
			return page;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public void setPage(long page) {
			this.page = (int)page;
		}

		public void setPageSize(long pageSize) {
			this.pageSize = (int)pageSize;
		}

		@Override
		public String toString() {
			return "{total=" + total + ", list=" + list + "}";
		}
	}
}
