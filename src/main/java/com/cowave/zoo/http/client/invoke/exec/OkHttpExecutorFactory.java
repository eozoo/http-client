package com.cowave.zoo.http.client.invoke.exec;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.HttpClientProperties;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.DisposableBean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shanhuiming
 *
 */
public class OkHttpExecutorFactory implements DisposableBean {

    // 基础客户端，持有共享连接池和调度资源
    private final OkHttpClient baseClient;

    // 派生客户端（相同TLS配置复用），共享基础连接池
    private final Map<List<Object>, OkHttpClient> clients = new HashMap<>();

    public OkHttpExecutorFactory(HttpClientProperties properties) {
        ConnectionPool pool = new ConnectionPool(properties.getPoolConnections(),
                properties.getKeepAliveDuration().toNanos(), TimeUnit.NANOSECONDS);
        baseClient = new OkHttpClient.Builder().connectionPool(pool)
                .retryOnConnectionFailure(false).build();
    }

    public synchronized OkHttpClient getClient(HttpClient annotation)
            throws ReflectiveOperationException, GeneralSecurityException {
        List<Object> key = Arrays.asList(annotation.keyManager(), annotation.trustManager(), annotation.hostnameVerifier());
        OkHttpClient client = clients.get(key);
        if (client != null) {
            return client;
        }

        // 服务端证书
        X509TrustManager trustManager = annotation.trustManager().getDeclaredConstructor().newInstance();
        // 客户端证书
        X509KeyManager keyManager = annotation.keyManager().getDeclaredConstructor().newInstance();
        // 域名检查
        HostnameVerifier verifier = annotation.hostnameVerifier().getDeclaredConstructor().newInstance();
        // sslSocket
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{trustManager}, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        // 派生client
        client = baseClient.newBuilder()
                .sslSocketFactory(sslSocketFactory, trustManager).hostnameVerifier(verifier).build();
        clients.put(key, client);
        return client;
    }

    @Override
    public synchronized void destroy() {
        baseClient.dispatcher().cancelAll();
        baseClient.dispatcher().executorService().shutdown();
        baseClient.connectionPool().evictAll();
        clients.clear();
    }
}
