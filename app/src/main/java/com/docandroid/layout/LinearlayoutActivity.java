package com.docandroid.layout;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.widget.TextView;

import com.baseframework.BaseActivity;
import com.baseframework.interfaces.DarkNavigationBarTheme;
import com.baseframework.interfaces.Layout;
import com.baseframework.interfaces.NavigationBarBackgroundColorRes;
import com.baseframework.util.JumpParameter;
import com.docandroid.ConstantParams;
import com.docandroid.R;


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
public class LinearlayoutActivity extends LayoutActivity {



    @Override
    public void initViews() {
        super.initViews();
    }


    @Override
    public void initDatas(JumpParameter parameter) {
        super.initDatas(parameter);

    }

    @Override
    public void setEvents() {

    }


}