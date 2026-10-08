package com.cowave.zoo.http.client.request.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * @author shanhuiming
 */
public class NoopTrustManager implements X509TrustManager {

    /**
     * 接受任意客户端证书
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {

    }

    /**
     * 接受任意服务器证书
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {

    }

    /**
     * 不限定受信任的证书颁发机构
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
