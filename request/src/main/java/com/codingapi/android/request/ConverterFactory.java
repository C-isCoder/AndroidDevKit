package com.codingapi.android.request;

import android.text.TextUtils;
import com.codingapi.android.config.Configuration;
import com.google.gson.Gson;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by iCong
 *
 * Date:2017年7月6日
 */
public class ConverterFactory extends Converter.Factory {
    private final Gson mGson;

    public static ConverterFactory create() {
        return new ConverterFactory();
    }

    private ConverterFactory() {
        mGson = new Gson();
    }

    /**
     * 服务器相应处理
     * 根据具体Result API 自定义处理逻辑
     *
     * @return 返回Data相应的实体
     */
    @Override public Converter<ResponseBody, ?> responseBodyConverter(Type mType,
        Annotation[] annotations, Retrofit retrofit) {
        return new ResponseBodyConverter<>(mType);//响应
    }

    /**
     * 请求处理
     * request body 我们无需特殊处理，直接返回 GsonConverterFactory 创建的 converter。
     *
     * @return 返回 GsonConverterFactory 创建的 converter
     */
    @Override public Converter<?, RequestBody> requestBodyConverter(Type mType,
        Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return GsonConverterFactory.create()
            .requestBodyConverter(mType, parameterAnnotations, methodAnnotations, retrofit);
    }

    /**
     * 自定义的result Api处理逻辑
     *
     * @param <T> 泛型
     */
    private class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private Type mType;//泛型，当服务器返回的数据为数组的时候回用到
        // state
        private static final int SERVICE_STATE_SUCCESS = 1;
        private static final int REQUEST_SUCCESS = 4_0000;
        private static final int TOKEN_ERROR = 3_0000;

        private ResponseBodyConverter(Type mType) {
            this.mType = mType;
        }

        /**
         * 自定义转换器-处理服务器返回数据
         *
         * @return 返回data的实体or列表
         */
        @Override public T convert(ResponseBody response) {
            try {
                String strResponse = response.string();
                if (TextUtils.isEmpty(strResponse)) {
                    throw new HttpException(HttpException.SERVICE_ERROR);
                }
                JSONObject jb = new JSONObject(strResponse);
                // 服务器状态
                int service_state = jb.getInt("state");
                if (service_state != SERVICE_STATE_SUCCESS) {
                    String error = HttpException.SERVICE_ERROR + ": " + jb.getString("msg");
                    throw new HttpException(error);
                }
                // 接口状态
                int ret_state = jb.getJSONObject("data").getInt("code");
                if (ret_state == REQUEST_SUCCESS) {
                    if (jb.getJSONObject("data").isNull("data")) {
                        throw new HttpException(HttpException.SERVICE_ERROR);
                    }
                    String parameters = jb.getJSONObject("data").get("data").toString();
                    if (TextUtils.isEmpty(parameters)) {
                        throw new HttpException(HttpException.DATA_ERROR);
                    }
                    return mGson.fromJson(parameters, mType);
                } else if (ret_state == TOKEN_ERROR) {
                    Configuration.get().refreshToken();
                    throw new HttpException(jb.getJSONObject("data").getString("msg"));
                } else {
                    // 接口异常
                    throw new HttpException(jb.getJSONObject("data").getString("msg"));
                }
            } catch (Exception e) {
                throw new HttpException(e.getMessage());
            } finally {
                response.close();
            }
        }
    }
}


