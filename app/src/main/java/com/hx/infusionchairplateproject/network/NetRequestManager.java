package com.hx.infusionchairplateproject.network;

import android.database.Observable;

import com.hx.infusionchairplateproject.databeen.ScreenInfo;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 管理网络请求
 */
public class NetRequestManager {
    private static NetRequestManager instance;
    private Retrofit retrofit;
    private RequestApi requestApi;

    private NetRequestManager() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
//                    .baseUrl("http://shuyeyi-test.dev.hxyihu.com/server/")   // 测试服
                    .baseUrl("http://shuyeyi.dev.hxyihu.com/server/") // 正式服
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (requestApi == null) {
            requestApi = retrofit.create(RequestApi.class);
        }
    }

    public static NetRequestManager getInstance() {
        if (instance == null) {
            synchronized (NetRequestManager.class) {
                if (instance == null) {
                    instance = new NetRequestManager();
                }
            }
        }
        return instance;
    }

    public RequestApi getRequestApi(){
        return requestApi;
    }


}
