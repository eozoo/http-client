## spring-feign Http调用声明

### 功能说明

- 提供了一个在Spring中使用Feign调用的声明注解，简单方便定制；

#### 依赖

```xml
<dependency>
    <groupId>com.cowave.commons</groupId>
    <artifactId>spring-feign</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 使用示例

#### 1. 指定调用服务的url

> 对于Post的Body请求，默认会设置：Content-Type=application/json，如果手动设置了则以设置的为准

```java
@FeignClient(url = "${feign.demo.url}")
public interface DemoService {

    @RequestLine("GET /demo/api/v1/user/info/{id}")
    Response<SysUser> userInfo(@Param("id") int id);

    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /demo/api/v1/user/list")
    Response<List<SysUser>> list(SysUser user);
}
```

#### 2. 指定调用服务的注册name

> 如果使用服务注册中心，可以注入一个FeignServiceChooser实例，在Chooser中将name转换成对应服务的url，比如：

```java
@RequiredArgsConstructor
@Component
public class CloudServiceChooser implements FeignServiceChooser {

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

> 然后在调用声明时，就可以使用对应服务注册的name

```java
@FeignClient(name = "service-demo")
public interface DemoService {

    @RequestLine("GET /demo/api/v1/user/info/{id}")
    Response<SysUser> userInfo(@Param("id") int id);

    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /demo/api/v1/user/list")
    Response<List<SysUser>> list(SysUser user);
}
```

#### 3. 指定调用接口的url

> 如果调用的服务url不能在初始化时指定，需要在具体接口调用时才确定，那么可以使用@Host来标记，其优先级高于@FeignClient中的name和url；

> @Options可以在方法上指定调用的超时属性，其优先级也高于@FeignClient中的设置，这样方便针对不同的方法设置超时；

> 这里声明的返回类型是HttpResponse，主要是针对外部服务的调用（通用Http规范，但是没有遵从我们的约定）；

```java
@FeignClient
public interface NmsService {
    
    // 比如 url = http://ip:port
    @Options(connectTimeoutMillis = 1000, readTimeoutMillis = 1000)
    @RequestLine("GET /api/nms/svn_rcst/rcst_authentication?ids={ids}")
    HttpResponse<NmsNetworkInfoDTO> list(@Host String url, @Param("ids") List<Integer> ids);
}
```

#### 4. 请求设置 RequestInterceptor

> 如果需要对请求做一些设置，可以实现一个RequestInterceptor，比如常见的在请求头中设置认证Token或requestId信息

```java 
public class FeignInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String accessId = Access.id();
        String authorization = Access.token();
        if(StringUtils.isBlank(accessId)) {
            accessId = newAccessId();
        }
        if(StringUtils.isBlank(authorization)) {
            authorization = newAuthorization();
        }
        requestTemplate.header("accessId", accessId);
        requestTemplate.header("Authorization", authorization);
    }
}
```

#### 5. 响应Decoder设置

> 可以指定decoder，比如对于我们约定的Response响应结构，可以使用ResponseDecoder，这样就可以直接获取结果类型，不用再自己判断响应码（如果Http.status或Response.code不是200，则抛异常）

```java :UserService
@FeignClient(name = "service-demo", decoder = ResponseDecoder.class)
public interface DemoService {

    @RequestLine("GET /demo/api/v1/user/info/{id}")
    SysUser userInfo(@Param("id") int id);

    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /demo/api/v1/user/list")
    List<SysUser> list(SysUser user);
}
```

#### 6. 下载上传支持

> 下载支持比较简单，只需使用`HttpResponse<InputStream>`接收就行，不过注意要自己关闭Stream；

> 对于上传只做了有限的支持，固定设置：Content-Type=multipart/form-data; 
> 使用@MultipartForm来标记表单数据，仅支持类型：`Map<String, ?>`； 
> 使用@MultipartFile来标记文件，仅支持类型：InputStream、File、MultipartFile、byte[]； 

```java
@FeignClient(url = "http://localhost:8080")
public interface DemoService {
    
    @RequestLine("GET /demo/api/download")
    HttpResponse<InputStream> download();

    @RequestLine("POST /demo/api/upload")
    HttpResponse<Void> upload(@MultipartForm Map<String, String> map, @MultipartFile(fileName = "xx.sql") InputStream inputStream);
}
```

#### 7. Https支持

> Feign.Client原本就支持Https连接的设置，可以指定SSLSocketFactory和HostnameVerifier，这里做的也只是在@FeignClient声明中暴露了这两个设置，比如:

```java
@FeignClient(url = "https://localhost:8080",
        sslSocketFactory = DemoSslSocketFactory.class, hostnameVerifier = DemoHostnameVerifier.class)
public interface FeignService {

    @RequestLine("GET /demo/api/v1/user/info/{id}")
    Response<SysUser> userInfo(@Param("id") int id);
}
```
