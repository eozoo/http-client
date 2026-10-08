package com.cowave.zoo.http.client;

import com.cowave.zoo.http.client.response.HttpResponse;

/**
 * 按本次失败响应和异常创建HTTP接口的降级实现。
 * <p>
 * 接口声明指定工厂：
 * <pre>{@code
 * @HttpClient(url = "${user-service.url}", fallback = UserFallbackFactory.class)
 * public interface UserClient {
 *
 *     @GetMapping("/users/{id}")
 *     UserVo getUser(@PathVariable("id") Long id);
 * }
 * }</pre>
 * 工厂注册为Spring Bean，每次失败创建对应接口的实现：
 * <pre>{@code
 * @Component
 * public class UserFallbackFactory implements HttpFallbackFactory<UserClient> {
 *
 *     @Override
 *     public UserClient create(HttpResponse<?> response, Throwable cause) {
 *         return new UserClientFallback(response, cause);
 *     }
 * }
 * }</pre>
 * 降级实现不需要注册Bean，通过构造参数持有本次失败信息：
 * <pre>{@code
 * @RequiredArgsConstructor
 * public class UserClientFallback implements UserClient {
 *
 *     // 本次失败响应，网络异常时为null
 *     private final HttpResponse<?> response;
 *
 *     // 本次失败原因
 *     private final Throwable cause;
 *
 *     @Override
 *     public UserVo getUser(Long id) {
 *         // 同时获取原始参数id、失败响应response和异常cause
 *         // 可以查询缓存、返回默认数据，或者抛出业务异常
 *         return null;
 *     }
 * }
 * }</pre>
 * 示例中的null仅为占位，实际应替换为业务兜底结果。
 * <p>
 * 调用顺序：HTTP调用及重试失败 → create(response, cause) → 调用降级实现的原接口方法。
 * 远端响应头通过response.getRemoteHeaders()获取；失败响应流在降级结束后关闭，不应保留供后续使用。
 *
 * @param <T> 原HTTP接口类型
 * @author shanhuiming
 */
public interface HttpFallbackFactory<T> {

    /**
     * 创建HTTP接口的降级实现
     *
     * @param response 失败响应，未获得响应时为null；流式响应仅在本次降级调用期间可读
     * @param cause 失败原因
     */
    T create(HttpResponse<?> response, Throwable cause);
}
