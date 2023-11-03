package com.hx.infusionchairplateproject.databeen;


import java.util.List;

/**
 * 一群app的页面
 */
public class PadApkList extends BaseBean<List<PadApkList.DataDataBeen>>{
    /**
     * {
     *     "status": 200,
     *     "message": null,
     *     "data": [
     *         {
     *             "id": "1v8xCOC8oXkFawEc",
     *             "name": "少儿五子棋",
     *             "icon": "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_1646460061330640896dc528bec91dbec47d285dad422ffad1.png",
     *             "sort": 0,
     *             "userId": null,
     *             "createTime": "2023-04-13 18:29:22",
     *             "updateTime": "2023-04-13 18:29:22",
     *             "categoryId": "BEr24CiOGroKKeIu",
     *             "categoryName": "游戏",
     *             "status": "UPPER_SHELVES",
     *             "statusName": "上架",
     *             "apkUrl": "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_1646460534741733376110_139beaa72496b972b9b47aeb5608da1a.apk",
     *             "apkName": "aircommiraclegobang",
     *             "packageName": "air.com.miracle.gobang",
     *             "attachmentId": "NXwanOpSHyOizRIh"
     *         },
     *         {
     *             "id": "HjsmaVFIVXjlNJkS",
     *             "name": "宝宝神奇厨房",
     *             "icon": "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_16459762716593479688.png",
     *             "sort": 0,
     *             "userId": null,
     *             "createTime": "2023-04-12 10:25:06",
     *             "updateTime": "2023-04-12 10:25:06",
     *             "categoryId": "BEr24CiOGroKKeIu",
     *             "categoryName": "游戏",
     *             "status": "UPPER_SHELVES",
     *             "statusName": "上架",
     *             "apkUrl": "https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/shuyeyi_1645976190784778240110_5f9134cf9aff870db31fd3a021bacb45.apk",
     *             "apkName": "comsinyeebabybuskitchen",
     *             "packageName": "com.sinyee.babybus.kitchen",
     *             "attachmentId": "gIKpxx3SYcsQDfKa"
     *         },
     *         ..........
     *         .........
     *         .........
     */
    public static class DataDataBeen {
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
         * sort
         */
        public int sort;
        /**
         * userId
         */
        public Object userId;
        /**
         * createTime
         */
        public String createTime;
        /**
         * updateTime
         */
        public String updateTime;
        /**
         * categoryId
         */
        public String categoryId;
        /**
         * categoryName
         */
        public String categoryName;
        /**
         * status
         */
        public String status;
        /**
         * statusName
         */
        public String statusName;
        /**
         * apkUrl
         */
        public String apkUrl;
        /**
         * packageName
         */
        public String packageName;
    }
}
