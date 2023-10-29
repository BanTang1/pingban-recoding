package com.hx.infusionchairplateproject.databeen;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 锁屏页面信息
 */
public class ScreenInfo extends BaseBean<ScreenInfo.DataDataBeen>{

    public static class DataDataBeen {
        /**
         * id
         */
        public String id;
        /**
         * number
         */
        public String number;
        /**
         * url
         */
        public String url;
        /**
         * feePackages
         */
        public List<FeePackagesDataBeen> feePackages;
        /**
         * images
         */
        public List<String> images;

        public static class FeePackagesDataBeen {
            /**
             * id
             */
            public String id;
            /**
             * name
             */
            public String name;
            /**
             * icon
             */
            public String icon;
            /**
             * type
             */
            public String type;
            /**
             * trial
             */
            public int trial;
            /**
             * feeDetail
             */
            public Object feeDetail;
            /**
             * createTime
             */
            public String createTime;
            /**
             * rules
             */
            public List<RulesDataBeen> rules;

            public static class RulesDataBeen {
                /**
                 * id
                 */
                public String id;
                /**
                 * name
                 */
                public Object name;
                /**
                 * sort
                 */
                public int sort;
                /**
                 * fee
                 */
                public float fee;
                /**
                 * duration
                 */
                public String duration;
                /**
                 * timeUnit
                 */
                public String timeUnit;
                /**
                 * feeStr
                 */
                public String feeStr;
                /**
                 * recommendStatus
                 */
                public String recommendStatus;


                @Override
                public String toString() {
                    return "RulesDataBeen{" +
                            "id='" + id + '\'' +
                            ", name=" + name +
                            ", sort=" + sort +
                            ", fee=" + fee +
                            ", duration='" + duration + '\'' +
                            ", timeUnit='" + timeUnit + '\'' +
                            ", feeStr='" + feeStr + '\'' +
                            ", recommendStatus='" + recommendStatus + '\'' +
                            '}';
                }
            }
        }

        @Override
        public String toString() {
            return "DataDataBeen{" +
                    "id='" + id + '\'' +
                    ", number='" + number + '\'' +
                    ", url='" + url + '\'' +
                    ", feePackages=" + feePackages +
                    ", images=" + images +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ScreenInfo{" +
                "data=" + data +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}


/**
 * {
 *     "status": 200,
 *     "message": null,
 *     "data": {
 *         "id": "87qY5dkszhsjk8bS",
 *         "number": "PB2023102700005",
 *         "url": "https://llm-huxing.oss-cn-beijing.aliyuncs.com/code/code_2023-10-27/PB2023102700005.png",
 *         "feePackages": [
 *             {
 *                 "id": 1056,
 *                 "name": "吉林省长春中医药大学附属第三临床医院\t",
 *                 "icon": null,
 *                 "type": "TABLET",
 *                 "trial": 5,
 *                 "feeDetail": null,
 *                 "createTime": "2023-10-24 08:41:54",
 *                 "recommendStatus": null,
 *                 "white": false,
 *                 "whiteCount": 0,
 *                 "status": "NORMAL",
 *                 "rules": [
 *                     {
 *                         "id": 1259,
 *                         "name": null,
 *                         "sort": 0,
 *                         "fee": 6.99,
 *                         "duration": "30",
 *                         "timeUnit": "MINUTE",
 *                         "feeStr": "30分钟 6.99元",
 *                         "trial": 0,
 *                         "recommendStatus": "NO"
 *                     },
 *                     {
 *                         "id": 1260,
 *                         "name": null,
 *                         "sort": 0,
 *                         "fee": 9.99,
 *                         "duration": "60",
 *                         "timeUnit": "MINUTE",
 *                         "feeStr": "60分钟 9.99元",
 *                         "trial": 0,
 *                         "recommendStatus": "NO"
 *                     },
 *                     {
 *                         "id": 1261,
 *                         "name": null,
 *                         "sort": 0,
 *                         "fee": 12.99,
 *                         "duration": "90",
 *                         "timeUnit": "MINUTE",
 *                         "feeStr": "90分钟 12.99元",
 *                         "trial": 0,
 *                         "recommendStatus": "NO"
 *                     },
 *                     {
 *                         "id": 1262,
 *                         "name": null,
 *                         "sort": 0,
 *                         "fee": 15.99,
 *                         "duration": "120",
 *                         "timeUnit": "MINUTE",
 *                         "feeStr": "120分钟 15.99元",
 *                         "trial": 0,
 *                         "recommendStatus": "NO"
 *                     }
 *                 ]
 *             }
 *         ],
 *         "images": [
 *             "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16510441689001041921.jpg",
 *             "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16510442816891330562.png",
 *             "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16510443615444869123.jpg",
 *             "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16510444391936368644.jpg",
 *             "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16510444987779194885.jpg",
 *             "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16510445588697128966.jpg"
 *         ]
 *     }
 * }
 */
