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
    private WebSocketClient webSocketClient;

    private final String Retrofit_URL =  "http://shuyeyi.dev.hxyihu.com/server/";       // 正式服
//    private final String Retrofit_URL =  "http://shuyeyi-test.dev.hxyihu.com/server/";  // 测试服

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

//        if (webSocketClient == null) {
//            webSocketClient = new WebSocketClient() {
//                @Override
//                public void onOpen(ServerHandshake handshakedata) {
//
//                }
//
//                @Override
//                public void onMessage(String message) {
//
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//
//                }
//
//                @Override
//                public void onError(Exception ex) {
//
//                }
//            }
//        }
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
