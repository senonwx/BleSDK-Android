package com.txwk.totalblesdk_android.util;

import java.util.List;

/**
 * 血糖尿酸检测
 */
public class BleBloodSugarConversion {
    /**
     * 功能解析：
     * 位置0 1.起始碼：下行包头，固定为常量：2个字节，固定为0x53 0x4E
     * 位置2.  包长：1个字节，为Length 字节之后的所有字节的长度，不包括Length 本字节，包括Checksum字节
     * 位置3 4.机器代码  产品代码：2个字节，为0x00 0x0A
     * 位置5.  命令字：1个字节，
     *              测试连接命令 0x01     清空存储数据命令0x05
     *              读历史数据   0x02     仪器关机 0x06
     *              同步时间命令 0x03     仪器开机 0x07
     *              读仪器 ID    0x04     数据上传 0x08
     *
     * 6/7/8/9.  0x53 0x49 0x4e 0x4f
     *
     * 位置10.Check_sum：校验码，1个字节，为Length 和Checksum 字节间所有数据的累加和，包括Length 字节,
     * unsigned char 类型（取累加和的低字节）
     *
     * @return
     */
    public static String getSendHex01() {
        int index0 = 83;
        int index1 = 78;
        int index2 = 8;
        int index3 = 0;
        int index4 = 10;
        int index5 = 1;
        int index6 = 83;
        int index7 = 73;
        int index8 = 78;
        int index9 = 79;
        int index10 = index2 + index3 + index4 + index5 + index6 + index7 + index8 + index9;

        byte[] data0_9 = new byte[]{(byte) index0, (byte) index1,(byte) index2,(byte) index3, (byte) index4,
                (byte) index5, (byte) index6, (byte) index7, (byte) index8,(byte) index9};
        String hexSend0_9 = HexUtils.encodeHexStr(data0_9);
        String hexIndex10 = HexUtils.encodeHexStr(new byte[]{(byte) index10});
        int len = hexIndex10.length();
        String i10 = hexIndex10.substring(len - 2, len);
        return String.format("%s%s",hexSend0_9,i10).toUpperCase();
    }

    /**
     * 0X53 0X4E+长度+机器代码+命令字+数据类型+校验；
     * 数据类型以两字节数据表示：
     *      血糖血液值：  0x00 0x01
     *      血糖质控液值：0x00 0x02
     *      尿酸血液值：  0x00 0x05
     *      尿酸质控液值：0x00 0x06
     * @return
     */
    public static String getSendHexHistory(int sendPack) {
        int index0 = 83;
        int index1 = 78;
        int index2 = 6;
        int index3 = 0;
        int index4 = 10;
        int index5 = 2;
        int index6 = 0;
        int index7 = sendPack;
        int index8 = index2 + index3 + index4 + index5 + index6 + index7;

        byte[] data0_7 = new byte[]{(byte) index0, (byte) index1,(byte) index2,(byte) index3, (byte) index4,
                (byte) index5, (byte) index6, (byte) index7};
        String hexSend0_7 = HexUtils.encodeHexStr(data0_7);
        String hexIndex8 = HexUtils.encodeHexStr(new byte[]{(byte) index8});
        int len = hexIndex8.length();
        String i8 = hexIndex8.substring(len - 2, len);
        return String.format("%s%s",hexSend0_7,i8).toUpperCase();
    }

    /**
     * 验证是否是正确的 数据响应
     * @param tempArrays
     * @return 0：不是正确格式   1：是血糖值    2：是尿酸值  3:开关机值  4：写入数据成功
     */
    public static int isBloodSugerTrue(List<String> tempArrays){
        if(tempArrays.size() == 7){
            return 3;
        }
        if(tempArrays.size() == 11){
            return 4;
        }
        if(tempArrays.size() < 21) return 0;
        //验证length长度
        int index2 = Integer.parseInt(tempArrays.get(2),16);
        if(index2 != tempArrays.size() - 3){
            return 0;
        }
        //验证checksum
        int totalLong = 0;
        for (int i = 2; i < tempArrays.size() -1; i++) {
            totalLong += Long.parseLong(tempArrays.get(i), 16);
        }
        if((Integer.parseInt(Long.toHexString(totalLong),16) & 0xFF)
                != Integer.parseInt(tempArrays.get(tempArrays.size()-1), 16)){
            return 0;
        }
        //血糖校正码c55   EA-12型：c42    尿酸校正码：655
        int checkSu = Integer.parseInt(tempArrays.get(8)+tempArrays.get(9), 16);
        if( checkSu != 55 && checkSu != 42 && checkSu != 655 && checkSu != 606){
            return 0;
        }else{
            if(checkSu == 55 || checkSu == 42){
                return 1;
            } else if(checkSu == 655 ||checkSu == 606){
                return 2;
            }
        }
        return 0;
    }

    /**
     * index = 15 16为测试结果
     *         19为测试单位   0x00:mmol/L 、umol/L   0x55: mg/dL
     *         6 7为数据类型
     *                  血糖血液值：0x00 0x01
     *                  血糖质控液值：0x00 0x02
     *                  尿酸血液值：0x00 0x05
     *                  尿酸质控液值：0x00 0x06
     * @param tempArrays
     * @return 返回数据
     */
    public static String readData(List<String> tempArrays){
        if(tempArrays.size() < 21)return "";
        //测试结果
        int result = Integer.parseInt(tempArrays.get(15)+tempArrays.get(16), 16);
        int type = Integer.parseInt(tempArrays.get(7), 16);
        //测试单位
        int unit = Integer.parseInt(tempArrays.get(19), 16);
        if(unit == 0){
            if(type == 1 || type == 2){
                return result*1.0/10+"";
            }else if(type == 5 || type == 6){
                return result+"";
            }
        }else if(unit == 85){
            if(type == 1 || type == 2){
                return result+"";
            }else if(type == 5 || type == 6){
                return result*1.0/10+"";
            }
        }
        return "";
    }


    //验证是否是一个完整的包
    public static boolean isACompleteData(List<String> tempArrays){
        if(tempArrays.size() == 0){
            return true;
        }
        //验证 包头 0x53  0x4e
        if(!tempArrays.get(0).equals("53") || !tempArrays.get(1).equals("4e")){
            return false;
        }
        //length 不匹配
        if(Integer.parseInt(tempArrays.get(2), 16)+3 != tempArrays.size()){
            return false;
        }
        //验证checksum
        int totalLong = 0;
        for (int i = 2; i < tempArrays.size() -1; i++) {
            totalLong += Long.parseLong(tempArrays.get(i), 16);
        }
        if((Integer.parseInt(Long.toHexString(totalLong),16) & 0xFF)
                != Integer.parseInt(tempArrays.get(tempArrays.size()-1), 16)){
            return false;
        }

        return true;
    }
}
