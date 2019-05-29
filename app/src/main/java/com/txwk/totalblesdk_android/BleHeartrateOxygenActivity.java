package com.txwk.totalblesdk_android;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.view.View;
import android.widget.TextView;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.txwk.totalblesdk_android.util.BleHeartOxygenConversion;
import com.txwk.totalblesdk_android.util.HexUtils;
import com.txwk.totalblesdk_android.util.LogUtils;
import com.txwk.totalblesdk_android.util.PreferenceTool;
import com.txwk.totalblesdk_android.util.ToastUtils;
import com.zhouwei.mzbanner.MZBannerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 心率血氧仪
 */
public class BleHeartrateOxygenActivity extends BaseActivity {
    private static final String TAG = "BleHeartrateOxygenActiv";
    private MZBannerView banner;
    private TextView connetstatus_tv, descripte_tv, bind_tv;
    private BleDevice bleDevice;
    private String uuid_service = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";//服务uuid
    private String uuid_characteristic_write = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";//写
    private String uuid_characteristic_notify = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";//读
    private boolean isScan = false;//是否正在扫描
    private String mBloodOxygen;//血氧值
    private String mHeartRate;//心率值
    private boolean isConect = false;//是否连接ble

    @Override
    public int getLayoutId() {
        return R.layout.activity_ble_heartrate_oxygen;
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
                Arrays.asList(new Integer[]{R.drawable.ble_heartrate_1,R.drawable.ble_heartrate_2,R.drawable.ble_connet}),
                Arrays.asList(new String[]{"第一步:\n          将手指放入血氧仪里面。",
                        "第二步:\n          等待血氧仪的显示屏里面显示测量的数值。",
                        "第三步:\n          点击下面的圆形按钮，进行连接测量数据。"}));
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
//                .setDeviceName(true, "Chipsea-BLE")         // 只扫描指定广播名的设备，可选
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
                isScan = false;
                bleDevice = bledevic;

                //连接成功 将mac地址存放到本地
                PreferenceTool.putString(TAG, bledevic.getMac());
                PreferenceTool.commit();
                bind_tv.setText("设备已绑定");

                //可以延迟500ms左右打开通知和发送通知
                //定时主动发送心跳包
                Observable.interval(200, 2, TimeUnit.SECONDS)
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                if(isConect){
                                    bleWrite(HexUtils.hexStringToBytes("80"), bleDevice, uuid_service, uuid_characteristic_write);
                                }
                            }
                        });
                //打开通知
                Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                bleNotify(bleDevice, uuid_service, uuid_characteristic_notify);
                            }
                        });

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

    /**
     * 向蓝牙发送写入数据
     * @param data 写入的byte数据
     */
    @SuppressLint("CheckResult")
    private void bleWrite(final byte[] data, final BleDevice bleDevice, final String uuid_service,
                          String uuid_characteristic_write) {
        BleManager.getInstance().write(
                bleDevice,
                uuid_service,
                uuid_characteristic_write,
                data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功
                        LogUtils.e("写入数据成功:" + Arrays.toString(data));
                    }
                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                        LogUtils.e("写入数据失败:" + Arrays.toString(data));
                    }
                });
    }

    /**
     * 订阅接受广播通知
     */
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
                        LogUtils.e("收到data==" + Arrays.toString(data));
                        if(connetstatus_tv != null){
                            connetstatus_tv.setText("心率血氧仪已连接");
                        }
                        analysisData(HexUtils.formatHexString(data, true));
                    }
                });
    }

    //中间数据
    private List<String> tempArrays = new ArrayList<>();

    /**
     * 解析数据
     * @param formatHexString
     */
    private void analysisData(String formatHexString) {
        String[] strings = formatHexString.split(" ");
        tempArrays.clear();
        if(strings.length < 8){
            return;
        }else if(strings.length > 12){
            for(int i = 0; i< 12 ; i++){
                tempArrays.add(strings[i]);
            }
        }else{
            tempArrays.addAll(Arrays.asList(strings));
        }

        if(BleHeartOxygenConversion.isHeartRateTrue(tempArrays) && BleHeartOxygenConversion.isDataTrue(tempArrays)){
            //检测数据长度是否符合 不符合略过
            mBloodOxygen = Integer.parseInt(tempArrays.get(5), 16)+"";//血氧浓度
            mHeartRate = Integer.parseInt(tempArrays.get(7)+tempArrays.get(6), 16)+"";//脉搏（心率）

            descripte_tv.setText("心率:"+mHeartRate+"bpm\t"+"血氧:"+mBloodOxygen+"%");
        }

    }

    private void setStatusPre() {
        if (connetstatus_tv == null || descripte_tv == null) {
            return;
        }
        isConect = false;
        connetstatus_tv.setText("心率血氧仪未连接");
        descripte_tv.setText("点击左侧按钮，连接设备!");
    }

    private void setStatusStop() {
        if (connetstatus_tv == null || descripte_tv == null) {
            return;
        }
        isConect = true;
        connetstatus_tv.setText("心率血氧仪已连接");
        descripte_tv.setText("请使用设备开始测量心率血氧!");
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

