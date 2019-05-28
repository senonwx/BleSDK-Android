package com.txwk.totalblesdk_android.bean;

import java.util.List;

/**
 * 检测结果详情
 */
public class UrineDetail {

    /**
     * error : 0
     * data : {"detail_data":[{"record_id":"404730","record_detail_value":"≥120","record_detail_status":"1","record_detail_created":"1555396851","detail_value_desc":"表示尿液标本白蛋白含量在≥120mg/L范围。可见于：①生理性包括剧烈运动、发热、低温刺激、紧张等；②病理性包括肾小球损伤。","cname":"微量白蛋白","category_desc":"白蛋白又称清蛋白，是血浆蛋白的主要成分。健康人在正常正常生理条件下，排泄到尿液中的量极少，一般24小时少于30mg（或＜20mg/L），用常规的检测方法难以检出，所以称微量白蛋白；如果排泄到尿液中的白蛋白增加，即提示可能肾脏受损害，是检测肾损害的早期敏感指标。","unit":"mg/L","standard_value":"0","detail_status":"1"}],"clinical_desc":"可见于：糖尿病、糖尿病肾病、糖尿病合并肾病","expert_advice":"①普通人群健康检查且不出现身体不适，请检查血糖（随时血糖、空腹血糖、糖耐量、糖化血红蛋白）、肝肾功能、血脂等，并咨询医生；②糖尿病患者、或（和）伴随高血压、或（和）伴随晨起眼睑及（或）双下肢浮肿等，请咨询肾内科或内分泌科医生；③孕妇请咨询专科医生；④抗坏血酸可能会影响本次检测结果的准确性，请综合评估本次检测结果。"}
     * error_msg : 操作成功
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
         * detail_data : [{"record_id":"404730","record_detail_value":"≥120","record_detail_status":"1","record_detail_created":"1555396851","detail_value_desc":"表示尿液标本白蛋白含量在≥120mg/L范围。可见于：①生理性包括剧烈运动、发热、低温刺激、紧张等；②病理性包括肾小球损伤。","cname":"微量白蛋白","category_desc":"白蛋白又称清蛋白，是血浆蛋白的主要成分。健康人在正常正常生理条件下，排泄到尿液中的量极少，一般24小时少于30mg（或＜20mg/L），用常规的检测方法难以检出，所以称微量白蛋白；如果排泄到尿液中的白蛋白增加，即提示可能肾脏受损害，是检测肾损害的早期敏感指标。","unit":"mg/L","standard_value":"0","detail_status":"1"}]
         * clinical_desc : 可见于：糖尿病、糖尿病肾病、糖尿病合并肾病
         * expert_advice : ①普通人群健康检查且不出现身体不适，请检查血糖（随时血糖、空腹血糖、糖耐量、糖化血红蛋白）、肝肾功能、血脂等，并咨询医生；②糖尿病患者、或（和）伴随高血压、或（和）伴随晨起眼睑及（或）双下肢浮肿等，请咨询肾内科或内分泌科医生；③孕妇请咨询专科医生；④抗坏血酸可能会影响本次检测结果的准确性，请综合评估本次检测结果。
         */

        private String clinical_desc;
        private String expert_advice;
        private List<DetailDataBean> detail_data;

        public String getClinical_desc() {
            return clinical_desc;
        }

        public void setClinical_desc(String clinical_desc) {
            this.clinical_desc = clinical_desc;
        }

        public String getExpert_advice() {
            return expert_advice;
        }

        public void setExpert_advice(String expert_advice) {
            this.expert_advice = expert_advice;
        }

        public List<DetailDataBean> getDetail_data() {
            return detail_data;
        }

        public void setDetail_data(List<DetailDataBean> detail_data) {
            this.detail_data = detail_data;
        }

        public static class DetailDataBean {
            /**
             * record_id : 404730
             * record_detail_value : ≥120
             * record_detail_status : 1
             * record_detail_created : 1555396851
             * detail_value_desc : 表示尿液标本白蛋白含量在≥120mg/L范围。可见于：①生理性包括剧烈运动、发热、低温刺激、紧张等；②病理性包括肾小球损伤。
             * cname : 微量白蛋白
             * category_desc : 白蛋白又称清蛋白，是血浆蛋白的主要成分。健康人在正常正常生理条件下，排泄到尿液中的量极少，一般24小时少于30mg（或＜20mg/L），用常规的检测方法难以检出，所以称微量白蛋白；如果排泄到尿液中的白蛋白增加，即提示可能肾脏受损害，是检测肾损害的早期敏感指标。
             * unit : mg/L
             * standard_value : 0
             * detail_status : 1
             */

            private String record_id;
            private String record_detail_value;
            private String record_detail_status;
            private String record_detail_created;
            private String detail_value_desc;
            private String cname;
            private String category_desc;
            private String unit;
            private String standard_value;
            private String detail_status;

            public String getRecord_id() {
                return record_id;
            }

            public void setRecord_id(String record_id) {
                this.record_id = record_id;
            }

            public String getRecord_detail_value() {
                return record_detail_value;
            }

            public void setRecord_detail_value(String record_detail_value) {
                this.record_detail_value = record_detail_value;
            }

            public String getRecord_detail_status() {
                return record_detail_status;
            }

            public void setRecord_detail_status(String record_detail_status) {
                this.record_detail_status = record_detail_status;
            }

            public String getRecord_detail_created() {
                return record_detail_created;
            }

            public void setRecord_detail_created(String record_detail_created) {
                this.record_detail_created = record_detail_created;
            }

            public String getDetail_value_desc() {
                return detail_value_desc;
            }

            public void setDetail_value_desc(String detail_value_desc) {
                this.detail_value_desc = detail_value_desc;
            }

            public String getCname() {
                return cname;
            }

            public void setCname(String cname) {
                this.cname = cname;
            }

            public String getCategory_desc() {
                return category_desc;
            }

            public void setCategory_desc(String category_desc) {
                this.category_desc = category_desc;
            }

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public String getStandard_value() {
                return standard_value;
            }

            public void setStandard_value(String standard_value) {
                this.standard_value = standard_value;
            }

            public String getDetail_status() {
                return detail_status;
            }

            public void setDetail_status(String detail_status) {
                this.detail_status = detail_status;
            }
        }
    }
}

