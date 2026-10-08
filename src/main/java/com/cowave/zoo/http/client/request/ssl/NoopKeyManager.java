package com.cowave.zoo.http.client.request.ssl;

import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * @author shanhuiming
 */
public class NoopKeyManager implements X509KeyManager {

    /**
     * 不提供客户端证书别名
     */
    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return null;
    }

    /**
     * 不选择客户端证书
     */
    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return null;
    }

    /**
     * 不提供服务器证书别名
     */
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return null;
    }

    /**
     * 不选择服务器证书
     */
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return null;
    }

    /**
     * 不提供证书链
     */
    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return null;
    }

    /**
     * 不提供私钥
     */
    @Override
    public PrivateKey getPrivateKey(String alias) {
        return null;
    }
}
