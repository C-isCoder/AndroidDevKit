package com.codingapi.android.request;

import com.codingapi.android.config.Configuration;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by iCong.
 * Time:2017年7月6日
 */
public class HttpClient {

    private static HttpClient INSTANCE;
    private Retrofit retrofit;
    private Retrofit newUrlRetrofit;
    private static String BaseUrl = Configuration.get().getApiDefaultHost();

    private HttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //cache目录
        File cacheFile =
            new File(Configuration.get().getAppContext().getCacheDir(), "netWorkCache");
        builder.cache(new Cache(cacheFile, 1024 * 1024 * 50));//50MB
        //自定义请求拦截器
        builder.addInterceptor(new LoggerInterceptor());
        //设置超时
        builder.connectTimeout(8, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(false);
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder().baseUrl(BaseUrl)
            .addConverterFactory(ConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(client)
            .build();
    }

    private HttpClient(String baseUrl) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //cache目录
        File cacheFile =
            new File(Configuration.get().getAppContext().getCacheDir(), "netWorkCache");
        builder.cache(new Cache(cacheFile, 1024 * 1024 * 50));//50MB
        //自定义请求拦截器
        builder.addInterceptor(new LoggerInterceptor());
        //设置超时
        builder.connectTimeout(8, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(false);
        OkHttpClient client = builder.build();
        newUrlRetrofit = new Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(ConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(client)
            .build();
    }

    /**
     * 默认的BaseUrl = APIConstant.BASE_URL
     */
    public static HttpClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HttpClient();
        }
        return INSTANCE;
    }

    /**
     * @param baseUrl
     * @return
     */
    public static HttpClient getInstance(String baseUrl) {
        return new HttpClient(baseUrl);
    }

    /**
     * 自定义Service
     *
     * @param service 传入自定义的Service
     */
    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    /**
     * 自定义Service
     *
     * @param service 传入自定义的Service
     */
    public <T> T createNewUrl(Class<T> service) {
        return newUrlRetrofit.create(service);
    }
}
