package com.codingapi.android.library.image;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.codingapi.android.config.Configuration;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class Image {
    private static final String TAG = Image.class.getCanonicalName();
    private static String IMAGE_LOAD_URL = Configuration.get().getApiLoadImage();

    private Image() {
    }

    /**
     * 加载图片
     *
     * @param idOrUrl urlOrId 图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @param targetView View 自动判断 ImageView 和 普通 View（默认设置到 View 的 background）。
     */
    public static void load(String idOrUrl, View targetView) {
        load(idOrUrl, -1, false, -1, targetView);
    }

    /**
     * 加载图片，失败时显示错误图片
     *
     * @param idOrUrl urlOrId 图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @param errorRes 加载错误显示的图片资源
     * @param targetView View 自动判断 ImageView 和 普通 View（默认设置到 View 的 background）。
     */
    public static void load(String idOrUrl, int errorRes, View targetView) {
        load(idOrUrl, errorRes, false, -1, targetView);
    }

    /**
     * 加载圆形图片
     *
     * @param idOrUrl urlOrId 图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @param targetView View 自动判断 ImageView 和 普通 View（默认设置到 View 的 background）。
     */
    public static void loadCircle(String idOrUrl, View targetView) {
        load(idOrUrl, -1, true, -1, targetView);
    }

    /**
     * 加载圆形图片，失败显示错误图片
     *
     * @param idOrUrl urlOrId 图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @param errorRes 错误图片资源
     * @param targetView View 自动判断 ImageView 和 普通 View（默认设置到 View 的 background）。
     */
    public static void loadCircle(String idOrUrl, int errorRes, View targetView) {
        load(idOrUrl, errorRes, true, -1, targetView);
    }

    /**
     * 加载圆角图片
     *
     * @param idOrUrl urlOrId 图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @param roundRadius 圆角图片的角度
     * @param targetView View 自动判断 ImageView 和 普通 View（默认设置到 View 的 background）。
     */
    public static void loadRound(String idOrUrl, int roundRadius, View targetView) {
        load(idOrUrl, -1, false, roundRadius, targetView);
    }

    /**
     * 加载圆角图片，失败显示错误图片
     *
     * @param idOrUrl urlOrId 图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @param roundRadius 圆角角度 px
     * @param errorRes 错误图片资源
     * @param targetView View 自动判断 ImageView 和 普通 View（默认设置到 View 的 background）。
     */
    public static void loadRound(String idOrUrl, int roundRadius, int errorRes, View targetView) {
        load(idOrUrl, errorRes, false, roundRadius, targetView);
    }

    @SuppressLint("CheckResult")
    private static void load(
      String idOrUrl,
      int error,
      boolean isCircle,
      int roundRadius,
      final View view
    ) {
        if (view == null) {
            throw new NullPointerException("View not null.");
        }
        if (!idOrUrl.contains("://")) {
            Log.w(TAG, "maybe url is image data id.");
            if (TextUtils.isEmpty(IMAGE_LOAD_URL)) {
                throw new NullPointerException("IMAGE_LOAD_URL not null , please initial "
                  + "IMAGE_LOAD_URL in config.");
            }
            idOrUrl = IMAGE_LOAD_URL + idOrUrl;
        }
        final RequestBuilder request =
          Glide.with(Configuration.get().getAppContext()).load(idOrUrl).thumbnail(0.25f);
        if (isCircle) {
            request.apply(RequestOptions.circleCropTransform());
        }
        if (!isCircle && roundRadius != -1) {
            request.apply(
              RequestOptions.bitmapTransform(new RoundedCorners(roundRadius))
            );
        }
        if (error != -1) {
            request.apply(RequestOptions.errorOf(error));
        }
        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                int w = view.getWidth();
                int h = view.getHeight();
                request.submit(w, h);
                if (view instanceof ImageView) {
                    request.into((ImageView) view);
                } else {
                    Target target = request.into(new SimpleTarget<Drawable>() {
                        @Override public void onResourceReady(@NonNull Drawable resource,
                          @Nullable Transition transition) {
                            view.setBackground(resource);
                        }
                    });
                    Glide.with(Configuration.get().getAppContext()).clear(target);
                }
                return true;
            }
        });
    }

    /**
     * 同步下载图片文件
     * note: don't use in UI main thread.
     *
     * @param urlOrId 下载图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @return File 文件
     */
    public static File downloadFile(String urlOrId) {
        if (TextUtils.isEmpty(urlOrId)) {
            throw new NullPointerException("url not null.");
        }
        if (!urlOrId.contains("://")) {
            Log.w(TAG, "maybe url is image data id.");
            if (TextUtils.isEmpty(IMAGE_LOAD_URL)) {
                throw new NullPointerException("IMAGE_LOAD_URL not null , please initial "
                  + "IMAGE_LOAD_URL in config.");
            }
            urlOrId = IMAGE_LOAD_URL + urlOrId;
        }
        File file = null;
        try {
            file = Glide.with(Configuration.get().getAppContext())
              .asFile()
              .load(urlOrId)
              .submit()
              .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 同步下载图片
     * note: don't use in UI main thread.
     *
     * @param urlOrId 下载图片的连接，可以传图片的全路经，在默认情况下也可以只 id，此时会拼接默认的 IMAGE HOST。
     * @return Bitmap 位图
     */
    public static Bitmap downloadBitmap(String urlOrId) {
        if (TextUtils.isEmpty(urlOrId)) {
            throw new NullPointerException("url not null.");
        }
        if (!urlOrId.contains("://")) {
            Log.w(TAG, "maybe url is image data id.");
            if (TextUtils.isEmpty(IMAGE_LOAD_URL)) {
                throw new NullPointerException("IMAGE_LOAD_URL not null , please initial "
                  + "IMAGE_LOAD_URL in config.");
            }
            urlOrId = IMAGE_LOAD_URL + urlOrId;
        }
        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(Configuration.get().getAppContext())
              .asBitmap()
              .load(urlOrId)
              .submit()
              .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 初始化
     *
     * @param imageApiUrl 图片地址 HOST
     */
    public static void setImageLoadUrl(String imageApiUrl) {
        IMAGE_LOAD_URL = imageApiUrl;
    }
}


