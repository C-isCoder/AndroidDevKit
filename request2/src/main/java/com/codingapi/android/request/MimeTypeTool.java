package com.codingapi.android.request;

import android.text.TextUtils;

/**
 * Created by iCong.
 * Time:2016/12/9-17:07.
 */

public class MimeTypeTool {
    //apk
    private static final String APK_CONTENT_TYPE = "application/vnd.android.package-archive";
    //png
    private static final String PNG_CONTENT_TYPE = "image/png";
    //jpg
    private static final String JPG_CONTENT_TYPE = "image/jpg";
    //gif
    private static final String GIF_CONTENT_TYPE = "image/gif";
    //doc
    private static final String DOC_CONTENT_TYPE = "application/msword";
    //docx
    private static final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    //html
    private static final String HTML_CONTENT_TYPE = "text/html";
    //mp3
    private static final String MP3_CONTENT_TYPE = "audio/mpeg";
    //avi
    private static final String AVI_CONTENT_TYPE1 = "video/msvideo";
    private static final String AVI_CONTENT_TYPE2 = "video/avi";
    private static final String AVI_CONTENT_TYPE3 = "video/x-msvideo";
    //pdf
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    //ppt
    private static final String PPT_CONTENT_TYPE = "application/vnd.ms-powerpoint";
    //txt
    private static final String TXT_CONTENT_TYPE = "text/plain";
    //wav
    private static final String WAV_CONTENT_TYPE1 = "audio/wav";
    private static final String WAV_CONTENT_TYPE2 = "audio/x-wav";
    //xls
    private static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";
    //xlsx
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    //xml
    private static final String XML_CONTENT_TYPE = "application/xml";
    //zip
    private static final String ZIP_CONTENT_TYPE1 = "application/zip";
    private static final String ZIP_CONTENT_TYPE2 = "application/x-compressed-zip";

    public static String getFileSuffix(String type) {
        if (TextUtils.isEmpty(type)) throw new NullPointerException("Type not null");
        if (TextUtils.equals(APK_CONTENT_TYPE, type)) {
            return ".apk";
        } else if (TextUtils.equals(PNG_CONTENT_TYPE, type)) {
            return ".png";
        } else if (TextUtils.equals(JPG_CONTENT_TYPE, type)) {
            return ".jpg";
        } else if (TextUtils.equals(GIF_CONTENT_TYPE, type)) {
            return ".gif";
        } else if (TextUtils.equals(DOC_CONTENT_TYPE, type)) {
            return ".doc";
        } else if (TextUtils.equals(DOCX_CONTENT_TYPE, type)) {
            return ".docx";
        } else if (TextUtils.equals(AVI_CONTENT_TYPE1, type)
                || TextUtils.equals(AVI_CONTENT_TYPE2, type)
                || TextUtils.equals(AVI_CONTENT_TYPE3, type)) {
            return ".avi";
        } else if (TextUtils.equals(HTML_CONTENT_TYPE, type)) {
            return ".html";
        } else if (TextUtils.equals(MP3_CONTENT_TYPE, type)) {
            return ".mp3";
        } else if (TextUtils.equals(PDF_CONTENT_TYPE, type)) {
            return ".pdf";
        } else if (TextUtils.equals(PPT_CONTENT_TYPE, type)) {
            return ".ppt";
        } else if (TextUtils.equals(TXT_CONTENT_TYPE, type)) {
            return ".txt";
        } else if (TextUtils.equals(WAV_CONTENT_TYPE1, type)
                || TextUtils.equals(WAV_CONTENT_TYPE2, type)) {
            return ".wav";
        } else if (TextUtils.equals(XLS_CONTENT_TYPE, type)) {
            return ".xls";
        } else if (TextUtils.equals(XLSX_CONTENT_TYPE, type)) {
            return ".xlsx";
        } else if (TextUtils.equals(XML_CONTENT_TYPE, type)) {
            return ".xml";
        } else if (TextUtils.equals(ZIP_CONTENT_TYPE1, type)
                || TextUtils.equals(ZIP_CONTENT_TYPE2, type)) {
            return ".zip";
        } else {
            throw new IllegalArgumentException("Type not find");
        }
    }
}
