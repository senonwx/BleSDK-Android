package com.txwk.totalblesdk_android.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class ToastUtils {

    private static volatile Toast mToast;
    private static Context context;

    public static void init(Context con){
        context = con;
    }

    public static void initToast(String msg) {
        initShortToast(context,msg);
    }

    public static void initShortToast(String msg){
        initToast( msg,context,Toast.LENGTH_SHORT);
    }

    public static void initLongToast(String msg){
        initToast( msg,context,Toast.LENGTH_LONG);
    }

    public static void initShortToast(Context context,String msg) {
        initToast(msg,context,Toast.LENGTH_SHORT);
    }

    public static void initLongToast(Context context,String msg) {
        initToast(msg,context,Toast.LENGTH_LONG);
    }

    //显示引用的字符串
    public static void initToast(int resId, Context context,int duration) {
        if (context instanceof Activity || context instanceof FragmentActivity)
            context = context.getApplicationContext();
        initToast(context.getString(resId), context,duration);
    }

    //显示
    public static void initToast(String msg, Context context,int duration) {
        if (context instanceof Activity || context instanceof FragmentActivity){
            context = context.getApplicationContext();
        }
        mToast = Toast.makeText(context, null,duration);
        mToast.setText(msg);
//        mToast.setGravity(Gravity.BOTTOM, 0, 0);//默认显示位置
        mToast.show();
    }
}

