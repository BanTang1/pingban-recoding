package com.hx.infusionchairplateproject.network;

import com.hx.infusionchairplateproject.databeen.ScreenInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RequestApi {

    /**  http://192.168.101.118:8080/pad/pad/screen/a0:37:b0:ea:6c:7d   锁屏页信息 rule 轮播图 二维码等信息
     */
    @GET("pad/" + "pad/screen/{sn}")
    Call<ScreenInfo> getScreenInfo(@Path("sn") String sn);

}
