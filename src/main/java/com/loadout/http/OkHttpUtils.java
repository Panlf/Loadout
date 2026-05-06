package com.loadout.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 工具类
 * 封装了 GET、POST（JSON/表单）、文件上传/下载等常用操作
 * 支持连接池、超时配置、日志拦截器、HTTPS 忽略证书（测试用）等特性
 * @author panlf
 * @date 2026/5/6
 */
@Slf4j
public class OkHttpUtils {
    private static volatile OkHttpUtils instance;
    private final OkHttpClient client;

    private OkHttpUtils() {
        this(DefaultConfig.build());
    }

    private OkHttpUtils(Config config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(config.readTimeout, TimeUnit.SECONDS)
                .writeTimeout(config.writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(config.retryOnConnectionFailure)
                .connectionPool(config.connectionPool)
                .addInterceptor(new LoggingInterceptor());

        if (config.ignoreHttps) {
            ignoreHttps(builder);
        }

        this.client = builder.build();
        log.info("OkHttpUtils initialized with config: {}", config);
    }

    public static OkHttpUtils getInstance() {
        if (instance == null) {
            synchronized (OkHttpUtils.class) {
                if (instance == null) {
                    instance = new OkHttpUtils();
                }
            }
        }
        return instance;
    }

    public static OkHttpUtils getInstance(Config config) {
        if (instance == null) {
            synchronized (OkHttpUtils.class) {
                if (instance == null) {
                    instance = new OkHttpUtils(config);
                }
            }
        }
        return instance;
    }

    // ==================== 同步 GET ====================
    public String get(String url) {
        return get(url, null);
    }

    public String get(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).get();
        addHeaders(builder, headers);
        return executeSync(builder.build());
    }

    // ==================== 同步 POST JSON ====================
    public String postJson(String url, String json) {
        return postJson(url, json, null);
    }

    public String postJson(String url, String json, Map<String, String> headers) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, mediaType);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        return executeSync(builder.build());
    }

    // ==================== 同步 POST 表单 ====================
    public String postForm(String url, Map<String, String> params) {
        return postForm(url, params, null);
    }

    public String postForm(String url, Map<String, String> params, Map<String, String> headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody body = formBuilder.build();
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        return executeSync(builder.build());
    }

    // ==================== 文件上传 ====================
    public String uploadFile(String url, String fileKey, File file, Map<String, String> formFields) {
        return uploadFile(url, fileKey, file, formFields, null);
    }

    public String uploadFile(String url, String fileKey, File file, Map<String, String> formFields,
                             Map<String, String> headers) {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (formFields != null) {
            for (Map.Entry<String, String> entry : formFields.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        multipartBuilder.addFormDataPart(fileKey, file.getName(), fileBody);
        RequestBody body = multipartBuilder.build();
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        return executeSync(builder.build());
    }

    // ==================== 文件下载 ====================
    public boolean downloadFile(String url, File targetFile) {
        return downloadFile(url, targetFile, null);
    }

    public boolean downloadFile(String url, File targetFile, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        addHeaders(builder, headers);
        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                log.error("Download failed, response code: {}", response.code());
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.error("Download failed, response body is null");
                return false;
            }
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (InputStream is = body.byteStream();
                 FileOutputStream fos = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                log.info("File downloaded successfully: {}", targetFile.getAbsolutePath());
                return true;
            }
        } catch (IOException e) {
            log.error("Download file error: {}", e.getMessage(), e);
            return false;
        }
    }

    // ==================== 异步 GET ====================
    public void getAsync(String url, Callback callback) {
        getAsync(url, null, callback);
    }

    public void getAsync(String url, Map<String, String> headers, Callback callback) {
        Request.Builder builder = new Request.Builder().url(url).get();
        addHeaders(builder, headers);
        executeAsync(builder.build(), callback);
    }

    // ==================== 异步 POST JSON ====================
    public void postJsonAsync(String url, String json, Callback callback) {
        postJsonAsync(url, json, null, callback);
    }

    public void postJsonAsync(String url, String json, Map<String, String> headers, Callback callback) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, mediaType);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        executeAsync(builder.build(), callback);
    }

    // ==================== 异步 POST 表单 ====================
    public void postFormAsync(String url, Map<String, String> params, Callback callback) {
        postFormAsync(url, params, null, callback);
    }

    public void postFormAsync(String url, Map<String, String> params, Map<String, String> headers, Callback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody body = formBuilder.build();
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        executeAsync(builder.build(), callback);
    }

    // ==================== 私有辅助方法 ====================
    private void addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private String executeSync(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Request failed, url: {}, response code: {}", request.url(), response.code());
                return null;
            }
            ResponseBody body = response.body();
            String result = body != null ? body.string() : null;
            log.debug("Sync request success, url: {}", request.url());
            return result;
        } catch (IOException e) {
            log.error("Sync request error, url: {}", request.url(), e);
            return null;
        }
    }

    private void executeAsync(Request request, Callback callback) {
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                log.error("Async request failed, url: {}", request.url(), e);
                callback.onFailure(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        log.error("Async request unsuccessful, url: {}, code: {}", request.url(), response.code());
                        callback.onFailure(new IOException("Unexpected code " + response.code()));
                        return;
                    }
                    ResponseBody body = response.body();
                    String result = body != null ? body.string() : null;
                    log.debug("Async request success, url: {}", request.url());
                    callback.onSuccess(result);
                } catch (Exception e) {
                    log.error("Async request process error, url: {}", request.url(), e);
                    callback.onFailure(e);
                } finally {
                    response.close(); // 手动关闭响应
                }
            }
        });
    }

    private void ignoreHttps(OkHttpClient.Builder builder) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            log.warn("Failed to ignore HTTPS certificates", e);
        }
    }

    // ==================== 日志拦截器 ====================
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();
            log.info("Request: {} {}", request.method(), request.url());
            if (request.body() != null && log.isDebugEnabled()) {
                log.debug("Request body: {}", request.body().toString());
            }
            Response response = chain.proceed(request);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Response: {} in {} ms", response.code(), duration);
            return response;
        }
    }

    // ==================== 配置类 ====================
    public static final class Config {
        final long connectTimeout;
        final long readTimeout;
        final long writeTimeout;
        final boolean retryOnConnectionFailure;
        final ConnectionPool connectionPool;
        final boolean ignoreHttps;

        private Config(Builder builder) {
            this.connectTimeout = builder.connectTimeout;
            this.readTimeout = builder.readTimeout;
            this.writeTimeout = builder.writeTimeout;
            this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
            this.connectionPool = builder.connectionPool;
            this.ignoreHttps = builder.ignoreHttps;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "connectTimeout=" + connectTimeout +
                    ", readTimeout=" + readTimeout +
                    ", writeTimeout=" + writeTimeout +
                    ", retryOnConnectionFailure=" + retryOnConnectionFailure +
                    ", connectionPool=" + connectionPool +
                    ", ignoreHttps=" + ignoreHttps +
                    '}';
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private long connectTimeout = 30;
            private long readTimeout = 30;
            private long writeTimeout = 30;
            private boolean retryOnConnectionFailure = true;
            private ConnectionPool connectionPool = new ConnectionPool(64, 5, TimeUnit.MINUTES);
            private boolean ignoreHttps = false;

            public Builder connectTimeout(long seconds) {
                this.connectTimeout = seconds;
                return this;
            }

            public Builder readTimeout(long seconds) {
                this.readTimeout = seconds;
                return this;
            }

            public Builder writeTimeout(long seconds) {
                this.writeTimeout = seconds;
                return this;
            }

            public Builder retryOnConnectionFailure(boolean retry) {
                this.retryOnConnectionFailure = retry;
                return this;
            }

            public Builder connectionPool(ConnectionPool pool) {
                this.connectionPool = pool;
                return this;
            }

            public Builder ignoreHttps(boolean ignore) {
                this.ignoreHttps = ignore;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

    private static final class DefaultConfig {
        private static Config build() {
            return Config.builder().build();
        }
    }

    // ==================== 回调接口 ====================
    public interface Callback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }
}