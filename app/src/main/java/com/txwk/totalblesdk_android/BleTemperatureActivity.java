package com.txwk.totalblesdk_android;

import android.bluetooth.BluetoothGatt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.txwk.totalblesdk_android.util.BleTemperatureConversion;
import com.txwk.totalblesdk_android.util.BleWeightConversion;
import com.txwk.totalblesdk_android.util.HexUtils;
import com.txwk.totalblesdk_android.util.LogUtils;
import com.txwk.totalblesdk_android.util.PreferenceTool;
import com.txwk.totalblesdk_android.util.ToastUtils;
import com.zhouwei.mzbanner.MZBannerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 体温计
 */
public class BleTemperatureActivity extends BaseActivity {
    private static final String TAG = "BleTemperatureActivity";
    private MZBannerView banner;
    private TextView connetstatus_tv, descripte_tv, bind_tv;
    private BleDevice bleDevice;
    private String uuid_service = "0000fff0-0000-1000-8000-00805f9b34fb";
    private String uuid_characteristic_notify = "0000fff2-0000-1000-8000-00805f9b34fb";
    private boolean isScan = false;//是否正在扫描
    private String temperature;//体温
    private boolean isConect = false;//是否连接ble

    @Override
    public int getLayoutId() {
        return R.layout.activity_ble_temperature;
    }

    @Override
    public void init() {
        initView();
        initBanner();
        initBle();
    }

    /**
     * 初始化View
     */
    private void initView() {
        banner = findViewById(R.id.banner);
        connetstatus_tv = findViewById(R.id.connetstatus_tv);
        descripte_tv = findViewById(R.id.descripte_tv);
        bind_tv = findViewById(R.id.bind_tv);
    }

    /**
     * 初始化Banner
     */
    private void initBanner() {
        // 父类设置数据
        initBanner(banner,
                Arrays.asList(new Integer[]{R.drawable.ble_connet,R.drawable.ble_temperature}),
                Arrays.asList(new String[]{"第一步:\n          让体温枪开机，并点击下面的圆形按钮，连接体温枪。",
                        "第二步:\n          体温枪连接成功后，将体温枪对准额头5~10厘米处，扣动扳机。等待体温检测结果。"}));
    }

    /**
     * 初始化Ble扫描规则
     */
    private void initBle() {
        String mac = PreferenceTool.getString(TAG, "");
        if (mac.isEmpty()) {
            bind_tv.setText("设备未绑定");
        } else {
            bind_tv.setText("设备已绑定");
        }
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(new UUID[]{UUID.fromString(uuid_service)})// 只扫描指定的服务的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
//                .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
//                .setDeviceName(true, "")         // 只扫描指定广播名的设备，可选
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void searchBle() {
        isScan = true;
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanStarted(boolean success) {
                // 开始扫描（主线程）
                isScan = success;
                if (!success) {
                    ToastUtils.initToast("扫描设备失败");
                } else {
                    addSweetDialog("正在扫描设备。。。");
                }
                LogUtils.e("onScanStarted = " + success);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                // 扫描到一个符合扫描规则的BLE设备（主线程）
                LogUtils.e("onScanning");
            }

            @Override
            public void onScanFinished(BleDevice scanResult) {
                // 扫描结束，结果即为扫描到的第一个符合扫描规则的BLE设备，如果为空表示未搜索到（主线程）
                LogUtils.e("onScanFinished");
                dissmissDialog();
                isScan = false;
                if (scanResult == null && !isDestory) {
                    ToastUtils.initToast("未扫描到设备，请重试");
                }
            }

            @Override
            public void onStartConnect() {
                // 开始连接（主线程）
                LogUtils.e("onStartConnect");
                addSweetDialog("正在连接设备。。。");
                isScan = false;
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                // 连接失败（主线程）
                LogUtils.e("onConnectFail");
                dissmissDialog();
                isScan = false;
                ToastUtils.initToast("连接设备失败，请重试");
                stopBle();
                setStatusStop();
            }

            @Override
            public void onConnectSuccess(BleDevice bledevic, BluetoothGatt gatt, int status) {
                LogUtils.e("onConnectSuccess");

                // 连接成功，BleDevice即为所连接的BLE设备（主线程）
                setStatusStop();
                bleDevice = bledevic;
                isScan = false;
                //连接成功 将mac地址存放到本地
                PreferenceTool.putString(TAG, bledevic.getMac());
                PreferenceTool.commit();
                bind_tv.setText("设备已绑定");

                bleNotify(bleDevice, uuid_service, uuid_characteristic_notify);
                dissmissDialog();
                ToastUtils.initToast("连接设备成功");
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                // 连接断开，isActiveDisConnected是主动断开还是被动断开（主线程）
                LogUtils.e("onDisConnected 是否是主动断开 " + isActiveDisConnected);
                isScan = false;

                setStatusPre();
                BleManager.getInstance().stopNotify(bleDevice, uuid_service, uuid_characteristic_notify);
                BleManager.getInstance().disconnect(bleDevice);
            }
        });
    }

    //开始接收蓝牙通知
    private void bleNotify(BleDevice bleDevice, String uuid_service, String uuid_characteristic_notify) {
        BleManager.getInstance().notify(
                bleDevice,
                uuid_service,
                uuid_characteristic_notify,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        if (connetstatus_tv != null) {
                            connetstatus_tv.setText("体温枪已连接");
                        }
                        analysisData(HexUtils.formatHexString(data, true));

                    }
                });
    }

    private List<String> tempArrays = new ArrayList<>();
    //解析数据
    private void analysisData(String content) {
        String[] strings = content.split(" ");
        LogUtils.e(content);

        if (strings != null && strings.length > 0) {
            if (strings.length < 8) {
                if (tempArrays.size() % 8 == 0) {
                    //验证头包是否是正确的 初步验证
                    if(strings[0].equals("af")){
                        tempArrays.clear();
                        tempArrays.addAll(Arrays.asList(strings));
                    }
                } else if (tempArrays.size() + strings.length == 8) {
                    tempArrays.addAll(Arrays.asList(strings));
                } else {
                    tempArrays.addAll(Arrays.asList(strings));
                    if(tempArrays.size() > 8){
                        //超过8个字节 认为数据产生了极小概率的异常情况  比如收到了一个af开头的包
                        //此时直接忽略本地测量  就是这么吊 Orz  - -!
                        tempArrays.clear();
                    }
                }
            } else if (strings.length == 8) {
                tempArrays.clear();
                tempArrays.addAll(Arrays.asList(strings));
            }
        }
        if (tempArrays.size() == 8) {
            checkTemp(tempArrays);
        }

    }

    private void checkTemp(List<String> tempArrays) {
        if (!BleTemperatureConversion.isPressureTrue(tempArrays)) {
            descripte_tv.setText("传输数据有误，请重新测量传输");
        } else {
            if (BleTemperatureConversion.isBodyTemp(tempArrays.get(6))) {
                temperature = BleTemperatureConversion.getTemp(tempArrays.get(4), tempArrays.get(5));
                descripte_tv.setText("检测到的体温数据：" + temperature + "℃");
            } else {
                descripte_tv.setText("请使用体温检测方式测量体温");
            }
        }
    }

    private void setStatusPre() {
        if (connetstatus_tv == null || descripte_tv == null) {
            return;
        }
        isConect = false;
        connetstatus_tv.setText("体温枪未连接");
        descripte_tv.setText("点击左侧按钮，连接设备!");
    }

    private void setStatusStop() {
        if (connetstatus_tv == null || descripte_tv == null) {
            return;
        }
        isConect = true;
        connetstatus_tv.setText("体温枪已连接");
        descripte_tv.setText("请使用设备开始测量体温!");
    }

    private void stopBle() {
        //调用该方法后，如果当前还处在扫描状态，会立即结束，并回调`onScanFinished`方法。
        if (isScan) {
            BleManager.getInstance().cancelScan();
        }
        if (bleDevice != null) {
            BleManager.getInstance().stopNotify(bleDevice, uuid_service, uuid_characteristic_notify);
            //断开设备
            BleManager.getInstance().disconnect(bleDevice);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBle();
    }

    public void 绑定设备(View view) {
        if (!PreferenceTool.getString(TAG, "").equals("")) {
            PreferenceTool.putString(TAG, "");
            PreferenceTool.commit();
            bind_tv.setText("设备未绑定");
            ToastUtils.initToast("解绑成功");
        }
    }

    public void 连接蓝牙(View view) {
        if (isConect) {//已连接时
            setStatusPre();
            stopBle();
        } else {//未连接时
            //如果设备已解绑  那么需要重新配置扫描规则
            if (PreferenceTool.getString(TAG, "").equals("")) {
                //重新配置搜索ble规则
                initBle();
            }
            //搜索ble
            searchBle();
        }
    }
}

