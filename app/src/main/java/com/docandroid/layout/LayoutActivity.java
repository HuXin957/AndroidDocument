package com.docandroid.layout;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.widget.TextView;

import com.baseframework.BaseActivity;
import com.baseframework.util.JumpParameter;
import com.docandroid.ConstantParams;
import com.docandroid.R;

/**
 * @author majin
 * @date 2022/9/21 11:12
 * @Description:
 */
public class LayoutActivity extends BaseActivity {
    @SuppressLint("NonConstantResourceId")
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
