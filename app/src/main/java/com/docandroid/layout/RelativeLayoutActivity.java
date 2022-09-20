package com.docandroid.layout;

import android.annotation.SuppressLint;
import android.graphics.Color;

import com.baseframework.BaseActivity;
import com.baseframework.interfaces.Layout;
import com.baseframework.util.JumpParameter;
import com.docandroid.R;


@SuppressLint("NonConstantResourceId")
@Layout(R.layout.activity_relative_layout)
public class RelativeLayoutActivity extends BaseActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void init() {
        super.init();
        setTranslucentStatus(true, Color.parseColor(getResources().getString(R.color.purple_700)));
    }
    @Override
    public void initViews() {

    }

    @Override
    public void initDatas(JumpParameter parameter) {

    }

    @Override
    public void setEvents() {

    }
}