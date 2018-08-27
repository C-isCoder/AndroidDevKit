package com.codingapi.android.request;

/**
 * Created by iCong.
 * Time:2016/12/9-10:23.
 */

public class HttpFactory {

    private HttpFactory() {
    }

    public static <T> T creatHttps(Class<T> clazz) {
        return HttpsClient.getInstance().create(clazz);
    }

    public static <T> T creatHttps(Class<T> clazz, String url) {
        return HttpsClient.getInstance(url).createNewUrl(clazz);
    }

    public static <T> T creatHttp(Class<T> clazz) {
        return HttpClient.getInstance().create(clazz);
    }

    public static <T> T creatHttp(Class<T> clazz, String url) {
        return HttpClient.getInstance(url).createNewUrl(clazz);
    }

    public static <T> T creatDownload(Class<T> clazz) {
        return DownloadClient.getInstance().create(clazz);
    }

    public static <T> T creatDownload(Class<T> clazz, String url) {
        return DownloadClient.getInstance(url).create(clazz);
    }

    public static <T> T creatUpload(Class<T> clazz) {
        return UploadClient.getInstance().create(clazz);
    }

    public static <T> T creatUpload(Class<T> clazz, String url) {
        return UploadClient.getInstance(url).create(clazz);
    }
}
