package com.hx.infusionchairplateproject.network;


import com.hx.infusionchairplateproject.databeen.AndroidVersion;
import com.hx.infusionchairplateproject.databeen.BaseBean;
import com.hx.infusionchairplateproject.databeen.PadApkList;
import com.hx.infusionchairplateproject.databeen.PadApkLists;
import com.hx.infusionchairplateproject.databeen.ScreenInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RequestApi {

    /**  http://shuyeyi.dev.hxyihu.com/server/pad/pad/screen/{sn}
     * 锁屏页信息 rule 轮播图 二维码等信息
     */
    @GET("pad/" + "pad/screen/{sn}")
    Call<ScreenInfo> getScreenInfo(@Path("sn") String sn);

    /**  http://shuyeyi-test.dev.hxyihu.com/server/pad/check/status/{sn}
     * 查询设备状态
     */
    @GET("pad/" + "check/status/{sn}")
    Call<BaseBean<String>> getDeviceStatus(@Path("sn") String sn);

    /**
     *  http://shuyeyi.dev.hxyihu.com/server/pad/android/version
     *  获取软件版本
     */
    @GET("pad/" + "android/version")
    Call<AndroidVersion> getAndroidVersion();

    /**
     * 获取指定类型的所有APP信息（Icon 包名 ....）
     */
    @GET("pad/" + "soft/list/{categoryName}")
    Call<PadApkList> getPadApkList(@Path("categoryName") String categoryName);

    /**
     * 获取所有App信息(URL地址)
     */
    @GET("pad/" + "soft/list")
    Call<PadApkLists> getPadApkLists();


}
