package com.txwk.totalblesdk_android.util;

import com.clj.fastble.utils.HexUtil;
import java.util.List;

/**
 * 体重验证
 */
public class BleWeightConversion {

    /**
     * 锁定数据
     * service UUID: 0x181B
     * charateristic UUID: 0x2A9C read,indicate
     *
     * byte 定义   value  description
     * 0-1  flags  16bit
     * 2-3  时间   uint16  年
     * 4  uint8   月 1~12
     * 5  uint8   日 1~31
     * 6  uint8   时 0~23
     * 7  uint8   分 0~59
     * 8  uint8   秒 0~59
     * 9-10   unt16  第一电极电阻，精度 0.1
     * 11-12  unt16  体重
     * 13-14  unt16  第二电极电阻(如果有)，精度 0.1
     * 15     uint8  消息属性，参见表一
     * 16-19  --     预留
     *
     * eg: 体重秤: 电阻为 0   flags 0x0002 byte0:02 byte1:00
     * eg: 体脂秤(一个电阻)： flags 0x0306 byte0:06 byte1:03
     * eg: 体脂秤(两个电阻)： flags 0x2306 byte0:06 byte1:23
     * 注：uint16 的数据发送时，低位在前，高位在后。
     */

    /**
     *   表一
     * 消息体
     * 属性
     * Bit7  Bit6  Bit5  保留
     * Bit4  Bit3        单位选择
     * Bit2  Bit1        小数点
     * Bit0              保留
     *
     * Bit4-3 单位选择   Bit2-1 小数位数选择
     * 00 = KG(默认)     00 = 2 位小数(默认)
     * 01 = 斤           01 = 0 位小数
     * 10 = LB           10 = 1 位小数
     * 11 = ST:LB
     *
     * 保留 1 位小数，则上传的体重值为实际值的 10 倍，保留 2 位小数，则上传的体重值为实际
     * 值的 100 倍，以此类推。
     * 注意：1.ST:LB 单位默认约定：ST 字节固定为整数，LB 字节固定为 1 位小数。此时的小数点
     * 选择位表示的是转换成其它单位后的小数位。
     */
    //验证是否是正确的 体脂秤1
    public static boolean isWeightTrue(List<String> tempArrays) {
        if(tempArrays.size() != 20) return false;
        //验证 包头 0x06  0x03
        if(!tempArrays.get(0).equals("06") || !tempArrays.get(1).equals("03")){
            return false;
        }
        return true;
    }

    //读取体重秤数据
    public static String readData(List<String> tempArrays) {
        //第一电极电阻，精度 0.1   双脚光脚站上去返回5000
        int resistance1 = Integer.parseInt(tempArrays.get(10)+tempArrays.get(9), 16);
        //第二电极电阻(如果有)，精度 0.1
        int resistance2 = Integer.parseInt(tempArrays.get(14)+tempArrays.get(13), 16);
        //体重
        int weight = Integer.parseInt(tempArrays.get(12)+tempArrays.get(11), 16);

        LogUtils.e("resistance1= " + resistance1+"   resistance2= "+resistance2);
        //消息体解析
        byte byte15 = HexUtil.hexStringToBytes(tempArrays.get(15))[0];
        int bit4 = HexUtils.getBit(byte15,4);
        int bit3 = HexUtils.getBit(byte15,3);
        int bit2 = HexUtils.getBit(byte15,2);
        int bit1 = HexUtils.getBit(byte15,1);
        //判断体重单位
        double tempInt = 0d;
        if((""+bit4 + bit3).equals("00")){//KG(默认)
            tempInt = weight;
        }else if((""+bit4 + bit3).equals("01")){//斤
            tempInt = weight * 0.5;
        }else if((""+bit4 + bit3).equals("10")){//LB
            tempInt = weight * 0.4536;
        }else if((""+bit4 + bit3).equals("11")){//ST:LB
            return "";
        }
        //判断体重小数位
        if((""+bit2 + bit1).equals("00")){//2 位小数(默认)
            tempInt = tempInt*1.0/100;
        }else if((""+bit2 + bit1).equals("01")){//0 位小数

        }else if((""+bit2 + bit1).equals("10")){//1 位小数
            tempInt = tempInt*1.0/10;
        }
        String tempStr = tempInt+"";
        if(tempStr.contains(".")){
            return tempStr.substring(0,tempStr.indexOf(".")+2);
        }else{
            return tempStr;
        }
    }


}
