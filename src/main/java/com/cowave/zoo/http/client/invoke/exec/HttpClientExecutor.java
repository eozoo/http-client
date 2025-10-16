package com.cowave.zoo.http.client.invoke.exec;

import com.cowave.zoo.http.client.request.HttpRequestTemplate;
import com.cowave.zoo.http.client.response.HttpResponseTemplate;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.cowave.zoo.http.client.constants.HttpHeader.*;
import static com.cowave.zoo.http.client.constants.HttpMethod.*;

/**
 *
 * @author shanhuiming
 *
 */
public class HttpClientExecutor implements HttpExecutor {
    private final CloseableHttpClient httpClient;

    public HttpClientExecutor(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
        SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslSocketFactory, hostnameVerifier);
        httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(100).setSSLSocketFactory(sslFactory).build()).build();
    }

    @Override
    public HttpResponseTemplate execute(HttpRequestTemplate request) throws IOException {
        HttpUriRequestBase httpRequest = buildRequest(request);
        httpRequest.setConfig(RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(request.getConnectTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(request.getReadTimeout())).build());
        IOException failure = null;
        for (int attempt = 0; attempt <= request.getRetryTimes(); attempt++) {
            try {
                ClassicHttpResponse response = httpClient.executeOpen(null, httpRequest, null);
                Map<String, List<String>> headers = new HashMap<>();
                for (Header header : response.getHeaders()) {
                    headers.computeIfAbsent(header.getName(), key -> new ArrayList<>()).add(header.getValue());
                }
                HttpEntity entity = response.getEntity();
                InputStream stream = entity == null ? null : entity.getContent();
                int length = entity == null ? 0 : (int) entity.getContentLength();
                return new HttpResponseTemplate(response, response.getCode(), headers,
                        response.getReasonPhrase(), stream, length);
            } catch (IOException exception) {
                failure = exception;
                if (!(exception instanceof SocketException) || attempt >= request.getRetryTimes()) {
                    throw exception;
                }
                try {
                    Thread.sleep(request.getRetryInterval());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw exception;
                }
            }
        }
        throw failure;
    }

    private HttpUriRequestBase buildRequest(HttpRequestTemplate request) throws IOException {
        String method = request.getMethod().toUpperCase();
        HttpUriRequestBase result;
        switch (method) {
            case GET: result = new HttpGet(request.getUrl()); break;
            case DELETE: result = new HttpDelete(request.getUrl()); break;
            case POST: result = new HttpPost(request.getUrl()); break;
            case PATCH: result = new HttpPatch(request.getUrl()); break;
            case PUT: result = new HttpPut(request.getUrl()); break;
            case HEAD: result = new HttpHead(request.getUrl()); break;
            case OPTIONS: result = new HttpOptions(request.getUrl()); break;
            case TRACE: result = new HttpTrace(request.getUrl()); break;
            default: throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }
        for (Map.Entry<String, Collection<String>> entry : request.getHeaders().entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(Content_Length)) {
                entry.getValue().forEach(value -> result.addHeader(entry.getKey(), value));
            }
        }
        if (request.getHeaders().keySet().stream().noneMatch(Accept::equalsIgnoreCase)) {
            result.addHeader(Accept, "*/*");
        }
        setEntity(result, request);
        return result;
    }

    private void setEntity(HttpEntityContainer request, HttpRequestTemplate template) throws IOException {
        if (template.getBody() != null) {
            byte[] body = template.getBody();
            Collection<String> encodings = template.getHeaders().get(Content_Encoding);
            if (encodings != null && encodings.contains("gzip")) {
                body = compress(body, true);
            } else if (encodings != null && encodings.contains("deflate")) {
                body = compress(body, false);
            }
            request.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM));
        } else if (template.getMultiFile() != null || template.getMultiForm() != null) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (template.getMultiFile() != null) {
                builder.addBinaryBody("file", template.getMultiFile(),
                        ContentType.APPLICATION_OCTET_STREAM, template.getMultiFileName());
            }
            if (template.getMultiForm() != null) {
                template.getMultiForm().forEach((key, value) -> builder.addTextBody(key, value.toString()));
            }
            request.setEntity(builder.build());
        }
    }

    private byte[] compress(byte[] body, boolean gzip) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStream stream = gzip ? new GZIPOutputStream(output) : new DeflaterOutputStream(output);
        try (OutputStream compressed = stream) {
            compressed.write(body);
        }
        return output.toByteArray();
    }
}
