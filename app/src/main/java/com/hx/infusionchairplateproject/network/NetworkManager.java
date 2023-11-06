package com.hx.infusionchairplateproject.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import okhttp3.WebSocket;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 管理网络请求
 */
public class NetworkManager {
    private static NetworkManager instance;
    private Retrofit retrofit;
    private RequestApi requestApi;

    /**
     * 数据请求服务器
     */
    private final String Retrofit_URL =  "http://shuyeyi.dev.hxyihu.com/server/";       // 正式服
//    private final String Retrofit_URL =  "http://shuyeyi-test.dev.hxyihu.com/server/";  // 测试服

    /**
     * WebSocket 服务器
     */
    public static final String WEBSOCKET_URL = "ws://shuyeyi.dev.hxyihu.com/message";    // 正式服
//    public static final String WEBSOCKET_URL = "ws://shuyeyi-test.dev.hxyihu.com/message";    // 测试服

    private NetworkManager() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Retrofit_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (requestApi == null) {
            requestApi = retrofit.create(RequestApi.class);
        }

    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) {
                    instance = new NetworkManager();
                }
            }
        }
        return instance;
    }

    public RequestApi getRequestApi(){
        return requestApi;
    }


}
