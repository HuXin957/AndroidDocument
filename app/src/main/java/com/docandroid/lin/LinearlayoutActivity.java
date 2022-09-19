package com.docandroid.lin;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import com.baseframework.BaseActivity;
import com.baseframework.interfaces.DarkNavigationBarTheme;
import com.baseframework.interfaces.DarkStatusBarTheme;
import com.baseframework.interfaces.Layout;
import com.baseframework.interfaces.NavigationBarBackgroundColorRes;
import com.baseframework.util.JumpParameter;
import com.docandroid.R;
import com.orhanobut.logger.Logger;


/**
 * @author: majin
 * @createTime: 2022/9/19
 * @desc: 线性布局
 */

@SuppressLint("NonConstantResourceId")
@Layout(R.layout.activity_linearlayout)
//设置底部导航栏背景颜色，此外还可以使用 @NavigationBarBackgroundColor 来指定 argb 颜色
@NavigationBarBackgroundColorRes(R.color.white)
//设置使用底部导航栏暗色图标样式
@DarkNavigationBarTheme(true)
public class LinearlayoutActivity extends BaseActivity {
    @SuppressLint("ResourceType")
    @Override
    public void initViews() {
        setTranslucentStatus(true, Color.parseColor(getResources().getString(R.color.purple_700)) );

    }

    @Override
    public void initDatas(JumpParameter parameter) {


    }

    @Override
    public void setEvents() {

    }

}