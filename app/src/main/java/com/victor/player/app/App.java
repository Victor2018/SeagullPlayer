package com.victor.player.app;

import android.app.Application;

/**
 * Created by victor on 2017/9/12 0012.
 */

public class App extends Application {
    private static App instance;

    public App() {
        instance = this;
    }

    public static App get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
//        VolleyRequest.buildRequestQueue(this);
    }

}
