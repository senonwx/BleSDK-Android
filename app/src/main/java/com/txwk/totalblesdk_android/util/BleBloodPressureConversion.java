package com.txwk.totalblesdk_android.util;

import com.clj.fastble.utils.HexUtil;
import java.util.List;

/**
 * 血压jiance
 */
public class BleBloodPressureConversion {

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * 功能解析：
     * 位置0.起始碼：下行包头，固定为常量：5AH。
     * 位置1.包长：整个包的长度，包括 CheckSum 部分.
     * 位置2.包类别：下行指令类型
     * <p>
     * 00H	读取血糠仪信息.
     * 01H  血压计启动测量
     * 03H	读取血糖仪测量数据.
     * 09H	握手包.
     * ：	保留.
     * <p>
     * 位置3-8.系统时间：智能系统的当前时间，用于更新血糠时间之用.
     * 位置9.Check_sum：校验码，从起始码开始，到Check_sum之间所有字节之和+2，取低字节，高字节丢弃。
     *
     * @param sendPack
     * @return
     */
    public static String getSendHex(int sendPack) {
        String time = TimeTool.getCurrentDateTime("yy-MM-dd-HH-mm-ss");
        String[] arrTime = time.split("-");
        if (arrTime.length == 6) {
            int index0 = 90;
            int index1 = 10;
            int index2 = sendPack;//00 包 03包
            int index3 = Integer.parseInt(arrTime[0]);//年
            int index4 = Integer.parseInt(arrTime[1]);//月
            int index5 = Integer.parseInt(arrTime[2]);//日
            int index6 = Integer.parseInt(arrTime[3]);//时
            int index7 = Integer.parseInt(arrTime[4]);//分
            int index8 = Integer.parseInt(arrTime[5]);//秒
            int index9 = index0 + index1 + index2 + index3 + index4 + index5 + index6 + index7 + index8 + 2;

            byte[] data = new byte[]{(byte) index0, (byte) index1, (byte) index2, (byte) index3,
                    (byte) index4, (byte) index5, (byte) index6, (byte) index7, (byte) index8};

            String hexSend = HexUtil.encodeHexStr(data);
            String hexIndex9 = HexUtil.encodeHexStr(new byte[]{(byte) index9});
            int len = hexIndex9.length();
            String i9 = hexIndex9.substring(len - 2, len);
            return String.format("%s%s", hexSend, i9).toUpperCase();
        }
        return "";
    }

    //验证 数据包 第0-12位+2 相加等于 第13位Check_sum
    public static boolean isBloodPreTrue(List<String> tempArrays){
        if(tempArrays.size() != 14)return false;
        long totalLong = 0;
        for (int i = 0; i < tempArrays.size() - 1; i++) {
            totalLong += Long.parseLong(tempArrays.get(i), 16);
        }
        return (Long.parseLong(Long.toHexString(totalLong),16) & 0xFF)
                == Long.parseLong(tempArrays.get(13), 16) - 2;
    }


    /**
     * 	格式：	起始碼	包长	包类别	年	月	日	时	分	秒	Check_sum
     偏移量	0	1	2	3	4	5	6	7	8	9
     数值范围	5AH	00H-FFH	00H-FFH	00H~63H	01H-0CH	01H-1FH	00H-17H	00H-3BH	00H-3BH	00~FF

     */
    public static String startPackage() {
        int index0 = 90;
        int index1 = 6;
        int index2 = 1;// 01包
        int index3 = 0;
        int index4 = 0;
        int index5 = 92;

        byte[] data = new byte[]{(byte) index0, (byte) index1, (byte) index2, (byte) index3,
                (byte) index4, (byte) index5};

        String hexSend = HexUtil.encodeHexStr(data);
        return hexSend.toUpperCase();
    }

}
