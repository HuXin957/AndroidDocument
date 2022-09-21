package com.docandroid.layout;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.widget.TextView;

import com.baseframework.BaseActivity;
import com.baseframework.interfaces.Layout;
import com.baseframework.util.JumpParameter;
import com.docandroid.ConstantParams;
import com.docandroid.R;


/**
 * @author: majin
 * @createTime: 2022/9/21
 * @desc: 相对布局
 */
@SuppressLint("NonConstantResourceId")
@Layout(R.layout.activity_relative_layout)
public class RelativeLayoutActivity extends BaseActivity {
    private TextView mTitle;


    @SuppressLint("ResourceType")
    @Override
    protected void init() {
        super.init();
        setTranslucentStatus(true, Color.parseColor(getResources().getString(R.color.purple_700)));
    }
    @Override
    public void initViews() {
        mTitle = findViewById(com.baseframework.R.id.txt_title);
    }

    @Override
    public void initDatas(JumpParameter parameter) {
        mTitle.setText(parameter.getString(ConstantParams.LAYOUTNAME));
    }

    @Override
    public void setEvents() {

    }
}