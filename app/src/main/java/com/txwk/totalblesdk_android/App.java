package com.txwk.totalblesdk_android;

import android.app.Application;

import com.clj.fastble.BleManager;
import com.txwk.totalblesdk_android.util.PreferenceTool;
import com.txwk.totalblesdk_android.util.ToastUtils;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化Toast
        ToastUtils.init(this);
        //初始化PreferenceTool
        PreferenceTool.init(this);
        //初始化ble
        BleManager.getInstance().init(this);
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
    }
}
