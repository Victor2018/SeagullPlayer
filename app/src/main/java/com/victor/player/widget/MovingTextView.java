package com.victor.player.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/** 
 * 单行文本跑马灯控件 
 * Created by victor on 2016/1/9.
 * 
 */
@SuppressLint("AppCompatCustomView")
public class MovingTextView extends TextView {
  
    public MovingTextView(Context context) {  
        super(context);  
        // TODO Auto-generated constructor stub  
    }  
  
    public MovingTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public MovingTextView(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);  
    }  
  
    @Override  
    public boolean isFocused() {  
        return true;
    }  
  
}  