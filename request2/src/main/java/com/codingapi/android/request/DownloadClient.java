package com.codingapi.android.request;

import com.codingapi.android.config.Configuration;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by iCong.
 * Time:2016/9/25-13:16.
 */

public class DownloadClient {
    private static DownloadClient INSTANCE;
    private Retrofit retrofit;
    private static String BaseUrl = Configuration.get().getApiDownload();

    private DownloadClient() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //官方请求拦截器
        builder.addInterceptor(new LoggerInterceptor());
        //设置超时
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(1, TimeUnit.MINUTES);
        builder.writeTimeout(1, TimeUnit.MINUTES);
        //错误重连
        builder.retryOnConnectionFailure(false);
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder().baseUrl(BaseUrl)
            .addConverterFactory(ConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build();
    }

    /**
     * 默认的BaseUrl = APIConstant.BASE_URL
     */
    public static DownloadClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DownloadClient();
        }
        return INSTANCE;
    }

    public static DownloadClient getInstance(String baseUrl) {
        BaseUrl = baseUrl;
        if (INSTANCE == null) {
            INSTANCE = new DownloadClient();
        }
        return INSTANCE;
    }

    /**
     * 自定义Service
     *
     * @param service 传入自定义的Service
     */
    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }
}