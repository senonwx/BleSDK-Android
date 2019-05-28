package com.txwk.totalblesdk_android.util;

import android.util.Log;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * 尿常规util
 */
public class BleUrineRoutinUtils {

    public static String appKey = "UtzXaYldad";
    public static String appSercet = "tU9n-2h_aRhxmypXA2JQ";//tU9n-2h_aRhxmypXA2JQ

    public static String web_url = "https://open.niaodaifu.cn/wap/login?";
    public static String BASE_URL = "https://open.niaodaifu.cn/sdk";

    private static String getMD5(String inputStr) {
        try {
            String md5Str = inputStr;
            if (inputStr != null) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(inputStr.getBytes());
                BigInteger hash = new BigInteger(1, md.digest());
                md5Str = hash.toString(16);//转为16位 首位会丢失0
                if ((md5Str.length() % 2) != 0) {
                    md5Str = "0" + md5Str;
                }
            }

            /**首位补0*/
            for (int i = 0; i < md5Str.length(); i++) {
                int val = md5Str.length();
                if (val < 32)
                    md5Str = "0" + md5Str;
            }

            return md5Str;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";
    }


    public static String getSign(long time) {
        String code = getMD5(appKey + ":" + appSercet).substring(8, 24);

        Log.e("TAG", "appKey:" + appKey + " appSercet: " + appSercet);

        code = getMD5(code + ":" + time);
        Log.e("TAG", "code:" + code);
        return code;
    }

    public static String getCheck(long time, String userToken) {

        String code = (userToken.substring(10, 30));
        String codeB = appKey + code;
        String codeC = getMD5(codeB + ":" + time);
        return codeC;
    }


}
