package com.codingapi.android.request;

import com.codingapi.android.config.Configuration;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by iCong.
 * Time:2017年7月6日
 */
public class UploadClient {
    private static UploadClient INSTANCE;
    private Retrofit retrofit;
    private static String BaseUrl = Configuration.get().getApiUpload();

    private UploadClient() {

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
    public static UploadClient getInstance() {
        if (INSTANCE == null) {
            synchronized (UploadClient.class) {
                INSTANCE = new UploadClient();
            }
        }
        return INSTANCE;
    }

    /**
     * 默认的BaseUrl = APIConstant.BASE_URL
     */
    public static UploadClient getInstance(String url) {
        BaseUrl = url;
        if (INSTANCE == null) {
            synchronized (UploadClient.class) {
                INSTANCE = new UploadClient();
            }
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
