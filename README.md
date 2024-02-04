spring-feign
======================

Java Http调用

#### 使用方式

##### 1. 指定服务url

```java
@FeignClient(url = "${feign.service-user.url}")
public interface UserService {

    @RequestLine("GET /{context-path}/api/v1/user/info/{id}")
    Response<SysUser> userInfo(@Param("id") int id);

    @RequestLine("POST /{context-path}/api/v1/user/list")
    @Headers({"Content-Type: application/json"})
    Response<List<SysUser>> list(SysUser user);
}
```

##### 2. 指定服务name

如果使用服务注册中心，那么可以实现一个`NameServiceChooser`，那么便可以通过name来调用，这里假设服务使用了Eureka进行注册管理

```java
@RequiredArgsConstructor
@Component
public class EurekaServiceChooser implements NameServiceChooser {

    private final LoadBalancerClient balancerClient;

    @Override
    public String choose(String name) {
        ServiceInstance instance = balancerClient.choose(name.toUpperCase());
        if (instance == null) {
            throw new IllegalArgumentException("service[" + name + "] not exist");
        }
        return instance.getUri().toString();
    }
}
```

另外，也可以指定decoder和logger。指定ResponseDecoder可以在接收时直接获取数据，而不用自己判断code，指定logger可以方便对日志按模块进行配置

```java :UserService
@FeignClient(name = "service-user", decoder = ResponseDecoder.class, logger = AccessLogger.class)
public interface UserService {

    @RequestLine("GET /{context-path}/api/v1/user/info/{id}")
    SysUser userInfo(@Param("id") int id);

    @RequestLine("POST /{context-path}/api/v1/user/list")
    @Headers({"Content-Type: application/json"})
    List<SysUser> list(SysUser user);
}
```

##### 3. 请求设置（RequestInterceptor）

如果需要对调用请求进行一些设置，可以实现一个`RequestInterceptor`并注入到spring容器中。比如在请求头中设置认证Token和请求requestId信息

```java 
public class FeignInterceptor implements RequestInterceptor {

    // ...

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String requestId = Access.id();
        String authorization = Access.token();
        if(StringUtils.isBlank(requestId)) {
            requestId = requestId();
        }
        if(StringUtils.isBlank(authorization) && tokenService != null) {
            authorization = newToken();
        }
        requestTemplate.header("requestId", requestId);
        requestTemplate.header("Authorization", authorization);
    }
}
```

##### 4. 不确定url（FeignManager）

可能存在这样的场景，无法提前确定url信息（调用时才指定），但是对于请求和响应的结构是确定的。对此我们提供了静态工厂：FeignManager

```java
public static <T> T get(Class<T> clazz, String url);
```

比如下面这样：我们先定义好响应结构

```java
@FeignClient(connectTimeoutMillis = 5000, readTimeoutMillis = 5000)
public interface TemporaryClient {

    @RequestLine("POST")
    @Headers({"Content-Type: application/json"})
    Response post(Object data);

    @RequestLine("GET")
    Response get();
}
```

然后在真正调用时再设置url，创建client实例进行调用

```java
Response response = FeignManager.get(TemporaryClient.class, "http://10.x.x.1:80/api/v1/xxx").get();
```
