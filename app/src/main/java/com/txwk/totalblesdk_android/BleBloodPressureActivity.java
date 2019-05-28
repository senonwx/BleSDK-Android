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
import com.clj.fastble.utils.HexUtil;
import com.txwk.totalblesdk_android.util.BleBloodPressureConversion;
import com.txwk.totalblesdk_android.util.LogUtils;
import com.txwk.totalblesdk_android.util.PreferenceTool;
import com.txwk.totalblesdk_android.util.ToastUtils;
import com.zhouwei.mzbanner.MZBannerView;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 血压
 */
public class BleBloodPressureActivity extends BaseActivity {
    private static final String TAG = "BleBloodPressureActivit";
    private MZBannerView banner;
    private TextView connetstatus_tv, descripte_tv, bind_tv;
    private BleDevice bleDevice;
    private String uuid_service = "00001000-0000-1000-8000-00805f9b34fb";//服务uuid
    private String uuid_characteristic_write = "00001001-0000-1000-8000-00805f9b34fb";//写
    private String uuid_characteristic_notify = "00001002-0000-1000-8000-00805f9b34fb";//读
    private boolean isScan = false;//是否正在扫描
    private String mHighValue ;//检测到的收缩压  高压
    private String mmLowValue ;//舒张压  低压
    private String mHeratValue;// 脉搏
    private boolean isConect = false;//是否连接ble

    @Override
    public int getLayoutId() {
        return R.layout.activity_ble_blood_pressure;
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
                Arrays.asList(new Integer[]{R.drawable.ble_pressure_1,R.drawable.ble_pressure_2,R.drawable.ble_connet}),
                Arrays.asList(new String[]{"第一步:\n          将血压计绑在左臂或右臂上，与心脏齐平。注意，不能有毛衣等较厚的衣服。",
                        "第二步:\n          请点击血压计上面的开关键打开血压计。",
                        "第三步:\n          点击下面的圆形按钮进行血压测量。期间保持心情放松，不要说话。"}));
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

                //可以延迟500ms左右打开通知或写入的操作
                bleWrite(HexUtil.hexStringToBytes(BleBloodPressureConversion.getSendHex(0)),
                        bledevic, uuid_service, uuid_characteristic_write);
                Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                bleNotify(bleDevice, uuid_service, uuid_characteristic_notify);
                            }
                        });
                Observable.timer(1000, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                bleWrite(HexUtil.hexStringToBytes(BleBloodPressureConversion.getSendHex(1)),
                                        bleDevice, uuid_service, uuid_characteristic_write);
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
                            connetstatus_tv.setText("血压计已连接");
                        }
                        
                        //v2版本
                        getData(data);
                        //v3版本
//                        if (data.length != 15 && data.length != 18){
//                            getValueData2(data);
//                        }
                    }
                });
    }

    //    血压的正常参考值为：
//    收缩压90～140mmhg(毫米汞柱)
//    舒张压60～90mmhg
    private void showContent(byte[] data) {
        if (data.length != 14) return;
        byte index2 = data[2];
        if (index2 != 3) return;//只获取结果包
        //验证数据包
        if(!BleBloodPressureConversion.isBloodPreTrue(Arrays.asList(HexUtil.formatHexString(data,true).split(" ")))){
            LogUtils.e("不正确的格式"+data.toString());
            return;
        }

        byte index9 = data[9];//9-10是收缩压
        byte index10 = data[10];//9-10是收缩压
        byte index11 = data[11];//舒张压
        byte index12 = data[12];//心率

        mHighValue = Integer.parseInt(HexUtil.encodeHexStr(new byte[]{index9}), 16)+
                Integer.parseInt(HexUtil.encodeHexStr(new byte[]{index10}), 16) + "";
        mmLowValue = Integer.parseInt(HexUtil.encodeHexStr(new byte[]{index11}), 16) + "";
        mHeratValue = Integer.parseInt(HexUtil.encodeHexStr(new byte[]{index12}), 16) + "";
        descripte_tv.setText("收缩压:"+mHighValue+"\t舒张压:"+mmLowValue+"\t心率"+mHeratValue);


        ToastUtils.initToast("测量完成");
    }

    /**
     *  解析数据
     */
    //中间数据
    private byte[] data_temp = new byte[]{};
    private int length = 8;
    private void getData(byte[] data) {
        if (null == data) return;
        if ((data.length > 2 && data[2] == 3) || (data_temp.length > 2 && data_temp[2] == 3)) {
            if (data.length == 14) {
                showContent(data);
            } else {
                if (data_temp.length < 14) {
                    //如果加起来长度还是不够，就直接拼接
                    if (data_temp.length + data.length < 14) {
                        data_temp = byteMerger(data_temp, data);
                    }
                    //如果加起来长度刚好够了，
                    else if (data_temp.length + data.length == 14) {
                        data_temp = byteMerger(data_temp, data);
                        LogUtils.e("length：" + length + "data_temp：" + Arrays.toString(data_temp));
                        showContent(data_temp);
                        data_temp = null;
                        data_temp = new byte[]{};
                    }
                }
            }
        }
    }

    //java 合并两个byte数组
    private byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    private void setStatusPre() {
        if (connetstatus_tv == null || descripte_tv == null) {
            return;
        }
        isConect = false;
        connetstatus_tv.setText("血压计未连接");
        descripte_tv.setText("点击左侧按钮，连接设备!");
    }

    private void setStatusStop() {
        if (connetstatus_tv == null || descripte_tv == null) {
            return;
        }
        isConect = true;
        connetstatus_tv.setText("血压计已连接");
        descripte_tv.setText("请使用设备开始测量血压!");
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

    private void getValueData2(byte[] data) {
        if (null == data) return;
        //先判断是过程包还是结果包
        if (data_temp.length < 3) {
            data_temp = byteMerger(data_temp, data);
            if (data_temp.length > 2) {
                byte index2 = data_temp[2];
                //[85, 5, -18, 1, 75]
                switch (index2) {
                    case 2:
                        length = 8;
                        break;
                    case 3:
                        length = 14;
                        break;
                    case -18:
                        length = 5;
                        break;
                }
                //如果加起来长度刚好够了，
                if (data_temp.length == length) {
                    switch (length) {
                        case 8:
                            setPreValue(data_temp);
                            break;
                        case 14:
                            try {
                                //验证数据包
                                if(!BleBloodPressureConversion.isBloodPreTrue(Arrays.asList(HexUtil.formatHexString(data_temp,true).split(" ")))){
                                    LogUtils.e("不正确的格式"+data.toString());
                                    return;
                                }
                                mHighValue = Integer.parseInt(HexUtil.encodeHexStr(new byte[]{data_temp[10]}), 16)+
                                        Integer.parseInt(HexUtil.encodeHexStr(new byte[]{data_temp[9]}), 16) + "";
                                mmLowValue = String.valueOf(Integer.valueOf(HexUtil.encodeHexStr(new byte[]{data_temp[11]}), 16));//低压
                                mHeratValue = String.valueOf(Integer.valueOf(HexUtil.encodeHexStr(new byte[]{data_temp[12]}), 16));//心率
                                descripte_tv.setText("收缩压:"+mHighValue+"\t舒张压:"+mmLowValue+"\t心率"+mHeratValue);

                            } catch (Exception e) {
                                LogUtils.e(e.toString());
                            }
                            break;
                        case 5:
//                            showHeadType(CONNECTSUCCESSTYPE);
//                            new CommonHintDialog(getContext()).title(getString(R.string.tip)).message(getString(R.string.bp_measure_error))
//                                    .builder().show();
                            break;
                    }
                    data_temp = null;
                    data_temp = new byte[]{};
                }
            }
        } else {
            if (data_temp.length < length) {
                //如果加起来长度还是不够，就直接拼接
                if (data_temp.length + data.length < length) {
                    data_temp = byteMerger(data_temp, data);
                }
                //如果加起来长度刚好够了，
                else if (data_temp.length + data.length == length) {
                    data_temp = byteMerger(data_temp, data);
                    LogUtils.e("length：" + length + "data_temp：" + Arrays.toString(data_temp));
                    switch (length) {
                        case 8:
                            setPreValue(data_temp);
                            break;
                        case 14:
                            try {
                                //验证数据包
                                if(!BleBloodPressureConversion.isBloodPreTrue(Arrays.asList(HexUtil.formatHexString(data_temp,true).split(" ")))){
                                    LogUtils.e("不正确的格式"+data.toString());
                                    return;
                                }

                                mHighValue = Integer.parseInt(HexUtil.encodeHexStr(new byte[]{data_temp[10]}), 16)+
                                        Integer.parseInt(HexUtil.encodeHexStr(new byte[]{data_temp[9]}), 16) + "";
                                mmLowValue = String.valueOf(Integer.valueOf(HexUtil.encodeHexStr(new byte[]{data_temp[11]}), 16));//低压
                                mHeratValue = String.valueOf(Integer.valueOf(HexUtil.encodeHexStr(new byte[]{data_temp[12]}), 16));//心率
                                descripte_tv.setText("收缩压:"+mHighValue+"\t舒张压:"+mmLowValue+"\t心率"+mHeratValue);

                            } catch (Exception e) {
                                LogUtils.e(e.toString());
                            }
                            break;
                        case 5:
                            ToastUtils.initToast("血压测量出错，测量中请保持坐姿、手势平稳，勿动");
                            break;

                    }
                    data_temp = null;
                    data_temp = new byte[]{};
                }
                //如果加起来长度超了，截取下来
                else {
                    int excess_length = data_temp.length + data.length - length;
                    data_temp = byteMerger(data_temp, subBytes(data, 0, data.length - excess_length));
                    setPreValue(data_temp);
                    data_temp = subBytes(data, data.length - excess_length, excess_length);
                }
            }
        }
    }

    /**
     * 在字节数组中截取指定长度数组
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    /**
     * 得到过程包显示出来
     */
    private void setPreValue(byte[] data) {
        byte[] valueArr = {data[6], data[5]};
        try {
            String strValue = String.valueOf(Integer.valueOf(HexUtil.encodeHexStr(valueArr), 16));//血压 6+5 在转换成10进制
            descripte_tv.setText(String.format("过程值:%s", strValue));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}