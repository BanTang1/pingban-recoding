package com.hx.infusionchairplateproject.network;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.hjq.toast.Toaster;
import com.hx.infusionchairplateproject.BaseActivity;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.Interceptor;

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
    private final String RETROFIT_URL =  "http://shuyeyi.dev.hxyihu.com/server/";       // 正式服
//    private final String RETROFIT_URL =  "http://shuyeyi-test.dev.hxyihu.com/server/";  // 测试服

    /**
     * WebSocket 服务器
     */
    public static final String WEBSOCKET_URL = "ws://shuyeyi.dev.hxyihu.com/message";    // 正式服
//    public static final String WEBSOCKET_URL = "ws://shuyeyi-test.dev.hxyihu.com/message";    // 测试服

    private NetworkManager() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new LoadingInterceptor())
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(RETROFIT_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
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


    /**
     * 网络拦截器
     */
    public static class LoadingInterceptor implements Interceptor {
        private Handler handler = BaseActivity.Companion.getHandler();
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            showLoading();
            try {
                return chain.proceed(chain.request());
            } catch (IOException e) {
                Toaster.showShort("网络异常，请稍后再试");
                throw e;
            } finally {
                hideLoading();
            }
        }

        private void showLoading() {
            handler.sendEmptyMessage(1);
        }

        private void hideLoading() {
            handler.sendEmptyMessage(0);
        }
    }



}
