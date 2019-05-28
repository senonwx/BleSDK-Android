package com.txwk.totalblesdk_android.bean;

/**
 * 尿大夫
 * 绑定用户获取usertoken
 */
public class UrineIsBind {

    /**
     * error : 0
     * data : {"usertoken":"ce09e8a93aceaa18a714b7a219ea5227","uid":631669}
     * error_msg : success
     */

    private int error;
    private DataBean data;
    private String error_msg;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public static class DataBean {
        /**
         * usertoken : ce09e8a93aceaa18a714b7a219ea5227
         * uid : 631669
         */

        private String usertoken;
        private int uid;

        public String getUsertoken() {
            return usertoken;
        }

        public void setUsertoken(String usertoken) {
            this.usertoken = usertoken;
        }

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }
    }
}