package com.docandroid.layout;


import android.annotation.SuppressLint;

import com.baseframework.interfaces.Layout;
import com.baseframework.util.JumpParameter;
import com.docandroid.R;
import com.orhanobut.logger.Logger;


/**
 * @author: majin
 * @createTime: 2022/9/21
 * @desc: 相对布局
 */
@SuppressLint("NonConstantResourceId")
@Layout(R.layout.activity_frame_layout)
public class FrameLayoutActivity extends LayoutActivity {


    @Override
    public void initViews() {
        super.initViews();
        Logger.d("FrameLayoutActivity initViews");
    }

    @Override
    public void initDatas(JumpParameter parameter) {
        super.initDatas(parameter);

    }

    @Override
    public void setEvents() {

    }
}