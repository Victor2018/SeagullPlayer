package com.victor.player.library.youtube;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.victor.player.library.util.HttpUtil;

public class NetworkReceiver extends BroadcastReceiver {

    public interface NetworkListener {
        void onNetworkAvailable();
        void onNetworkUnavailable();
    }

    private NetworkListener networkListener;

    public NetworkReceiver(NetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(HttpUtil.isNetEnable(context))
            networkListener.onNetworkAvailable();
        else
            networkListener.onNetworkUnavailable();
    }
}
