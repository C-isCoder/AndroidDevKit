package com.codingapi.android.request;

import com.baichang.android.config.ConfigurationImpl;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by iCong.
 * Time:2016/12/6-10:14.
 * 要求在具体项目的assets文件夹中放入mySSL.cer证书文件
 */

class HttpsHelper {
    private static SSLContext sslContext = null;
    private static TrustManagerFactory trustManagerFactory = null;
    private static final String SSL_ERROR = "检查项目Assets文件夹是否放入了,mySSL.cer证书";

    public static SSLSocketFactory getSSLContext() {
        try {
            InputStream certificates =
                ConfigurationImpl.get().getAppContext().getAssets().open("mySSL.cer");
            if (certificates == null) throw new NullPointerException(SSL_ERROR);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("0", certificateFactory.generateCertificate(certificates));
            try {
                certificates.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sslContext = SSLContext.getInstance("TLS");
            trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext.getSocketFactory();
    }

    public static X509TrustManager getTrustManager() {
        return (X509TrustManager) trustManagerFactory;
    }
}
