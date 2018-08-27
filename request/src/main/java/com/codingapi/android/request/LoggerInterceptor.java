package com.codingapi.android.request;

import android.text.TextUtils;
import com.baichang.android.config.ConfigurationImpl;
import com.codingapi.android.library.logger.Logger;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 自定义请求拦截器，处理请求的token，加密，打印日志等
 * Created by iCong on 2016/9/17.
 */
public class LoggerInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String TAG = "HttpLogger";

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = doRequest(chain.request());
        long startNs = System.nanoTime();
        return doResponse(startNs, chain.proceed(request));
    }

    // 处理请求体
    private Request doRequest(Request request) {
        RequestBody requestBody = request.body();
        HttpUrl url = request.url();
        HttpUrl.Builder newUrl = url.newBuilder();
        Buffer buffer = new Buffer();
        try {
            if (requestBody != null) {
                requestBody.writeTo(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String parameter = buffer.readString(UTF8);
        buffer.flush();
        buffer.close();
        String token = ConfigurationImpl.get().getToken();
        if (!TextUtils.isEmpty(token)) {
            newUrl.addQueryParameter("token", token);
        }
        Logger.i(TAG, "REQUEST\n" +
            "param ->[T_T]     ：" + (TextUtils.isEmpty(parameter) ? "空空如也" : parameter) + "\n" +
            "url   ->[Q_Q]     ：" + url + "\n" +
            "host  ->[@_@]     ：" + url.host() + "\n" +
            "token:->[*_*]     ：" + token + "\n" +
            "method->[^_^]     ：" + request.method()
        );
        Request.Builder requestBuilder =
            request.newBuilder().method(request.method(), request.body()).url(newUrl.build());
        return requestBuilder.build();
    }

    // 处理响应体
    private Response doResponse(long startNs, Response response) {
        if (!ConfigurationImpl.get().isDebug()) {
            return response;
        } else {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            ResponseBody responseBody = response.body();
            assert responseBody != null;
            BufferedSource source = responseBody.source();
            try {
                source.request(Long.MAX_VALUE); // Buffer the entire body.
            } catch (IOException e) {
                e.printStackTrace();
            }
            Buffer responseBuffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    return response;
                }
            }
            if (!isPlaintext(responseBuffer)) {
                return response;
            }
            if (charset != null) {
                Logger.d(TAG,
                    "RESPONSE\n"
                        + "url:" + response.request().url() + "\n"
                        + "timer:" + tookMs + "ms\n"
                        + responseBuffer.clone().readString(charset)
                );
            }
            return response;
        }
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}

