package com.cowave.zoo.http.client.invoke.exec;

import com.cowave.zoo.http.client.request.HttpRequestTemplate;
import com.cowave.zoo.http.client.response.HttpResponseTemplate;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.cowave.zoo.http.client.constants.HttpHeader.*;
import static com.cowave.zoo.http.client.constants.HttpMethod.*;

/**
 *
 * @author shanhuiming
 *
 */
public class OkHttpExecutor implements HttpExecutor {

    private final OkHttpClient httpClient;

    public OkHttpExecutor(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponseTemplate execute(HttpRequestTemplate template) throws IOException {
        Request request = buildRequest(template);
        OkHttpClient client = httpClient.newBuilder()
                .connectTimeout(template.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(template.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(template.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .callTimeout(template.remainingCallTimeout(), TimeUnit.MILLISECONDS)
                .build();
        Response response = client.newCall(request).execute();
        try {
            ResponseBody body = response.body();
            // 流式返回交给调用方，关闭流时同时关闭底层响应
            InputStream stream = new FilterInputStream(body.byteStream()) {
                @Override
                public void close() {
                    response.close();
                }
            };
            return new HttpResponseTemplate(response, response.code(),
                    response.headers().toMultimap(), response.message(), stream, body.contentLength());
        } catch (RuntimeException exception) {
            response.close();
            throw exception;
        }
    }

    private Request buildRequest(HttpRequestTemplate template) throws IOException {
        // 请求method
        String method = template.getMethod().toUpperCase(Locale.ROOT);
        switch (method) {
            case GET:
            case HEAD:
            case DELETE:
            case POST:
            case PATCH:
            case PUT:
            case OPTIONS:
            case TRACE:
                break;
            default:
                throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }
        // 请求url
        Request.Builder builder = new Request.Builder().url(template.getUrl());
        // 请求header
        for (Map.Entry<String, Collection<String>> entry : template.getHeaders().entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(Content_Length)) {
                entry.getValue().forEach(value -> builder.addHeader(entry.getKey(), value));
            }
        }
        // 默认Accept
        if (template.getHeaders().keySet().stream().noneMatch(Accept::equalsIgnoreCase)) {
            builder.addHeader(Accept, "*/*");
        }

        RequestBody body = buildBody(template);
        // 补充请求体body
        if (body == null && (POST.equals(method) || PUT.equals(method) || PATCH.equals(method))) {
            body = RequestBody.create(new byte[0], null);
        }
        // Multipart请求
        if (body instanceof MultipartBody) {
            // 补充boundary请求头
            builder.header(Content_Type, body.contentType().toString());
        } else if (template.getBody() != null
                && template.getHeaders().keySet().stream().noneMatch(Content_Type::equalsIgnoreCase)) {
            // 补充Content-Type请求头，默认按二进制内容发送
            builder.header(Content_Type, "application/octet-stream");
        }
        return builder.method(method, body).build();
    }

    private RequestBody buildBody(HttpRequestTemplate template) throws IOException {
        // 请求体字节数据
        if (template.getBody() != null) {
            byte[] body = template.getBody();
            Collection<String> encodings = template.getHeaders().get(Content_Encoding);
            if (encodings != null && encodings.contains("gzip")) {
                body = compress(body, true);
            } else if (encodings != null && encodings.contains("deflate")) {
                body = compress(body, false);
            }
            return RequestBody.create(body, null);
        }
        // 文件表单
        if (template.getMultiFile() == null && (template.getMultiForm() == null
                || template.getMultiForm().values().stream().allMatch(Objects::isNull))) {
            return null;
        }
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // 文件流
        if (template.getMultiFile() != null) {
            RequestBody file = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.get("application/octet-stream");
                }

                @Override
                public boolean isOneShot() {
                    return true;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    // 文件流由调用层统一关闭，连接失败时也能释放
                    Source source = Okio.source(template.getMultiFile());
                    sink.writeAll(source);
                }
            };
            builder.addFormDataPart("file", template.getMultiFileName(), file);
        }
        // 表单
        if (template.getMultiForm() != null) {
            template.getMultiForm().forEach((key, value) -> {
                if (value != null) {
                    builder.addFormDataPart(key, value.toString());
                }
            });
        }
        return builder.build();
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
