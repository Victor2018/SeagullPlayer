package com.victor.player.library.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.victor.player.library.R;

public class SeagullPlayCtrl extends FrameLayout {
    protected View mMediaCtrlView;//控制器视图
    private SeagullPlayeView mSeagullPlayeView;

    public SeagullPlayCtrl(@NonNull Context context) {
        this(context,null);
    }

    public SeagullPlayCtrl(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SeagullPlayCtrl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView () {

    }

    public void setPlayerView (SeagullPlayeView playView) {
        mSeagullPlayeView = playView;
    }
}
