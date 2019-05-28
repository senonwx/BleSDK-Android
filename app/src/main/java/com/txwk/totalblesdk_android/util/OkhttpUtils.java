package com.txwk.totalblesdk_android.util;

import com.txwk.totalblesdk_android.BuildConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * OkHttpClient 单例
 */
public class OkhttpUtils {

    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        try {
            if (httpClient == null) {
                synchronized (OkhttpUtils.class) {
                    if (httpClient == null) {
                        httpClient = createService();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpClient;
    }

    private static OkHttpClient createService(){
        OkHttpClient httpClient = new OkHttpClient().newBuilder()
                .sslSocketFactory(getSSLSocketFactory())
                .addInterceptor(getLogInterceptor())
                .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .build();
        return httpClient;
    }

    /**
     * 日志拦截器
     * @return
     */
    private static HttpLoggingInterceptor getLogInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return loggingInterceptor;
    }

    /**
     * 不验证证书
     *
     * @return
     * @throws Exception
     */
    private static SSLSocketFactory getSSLSocketFactory() {
        //创建一个不验证证书链的证书信任管理器。
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType){
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType){
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        }};

        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
