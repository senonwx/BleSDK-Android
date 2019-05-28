package com.txwk.totalblesdk_android.util;

import com.clj.fastble.utils.HexUtil;
import java.util.List;

/**
 * 体温验证
 */
public class BleTemperatureConversion {

    //验证 16进制string byte中的第5、6位是否为 00：体温
    public static boolean isBodyTemp(String data1){
        int indexSix = HexUtils.getBit(HexUtil.hexStringToBytes(data1)[0],6);
        int indexFive = HexUtils.getBit(HexUtil.hexStringToBytes(data1)[0],5);
        if(indexSix == 0 && indexFive ==0){
            return true;
        }
        return false;
    }

    //高位byte 与 低位byte 合成温度值
    public static String getTemp(String hi,String lo){
        double tempInt = Integer.parseInt((hi+lo), 16)*1.0/100;
        String tempStr = tempInt+"";
        if(tempStr.contains(".")){
            return tempStr.substring(0,tempStr.indexOf(".")+2);
        }else{
            return tempStr;
        }
    }

    //验证 数据包 第1-6位相加等于第7位
    public static boolean isPressureTrue(List<String> tempArrays){
        if(!(tempArrays.get(0).equalsIgnoreCase("af") &&
                tempArrays.get(1).equalsIgnoreCase("6a") &&
                tempArrays.get(2).equalsIgnoreCase("72"))){
            return false;
        }

        long sixLong = 0;
        for (int i = 1; i < tempArrays.size() - 1; i++) {
            sixLong += Long.parseLong(tempArrays.get(i), 16);
        }

        return (Long.parseLong(Long.toHexString(sixLong),16) & 0xFF)
                == Long.parseLong(tempArrays.get(7), 16);
    }

}




