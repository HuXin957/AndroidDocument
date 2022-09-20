package com.docandroid.util;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.docandroid.layout.LinearlayoutActivity;


public class MyJumpUtil extends JumpUtil {
    private static MyJumpUtil nUtil;

    private MyJumpUtil() {
    }


    /** rqc请求码  rec 结果码 */

    /** 主页面 */
    private final int rqc_lin          = 1000;


    /**
     * 单一实例
     */
    public static MyJumpUtil getInstance() {
        if (nUtil == null) {
            synchronized (MyJumpUtil.class) {
                if (nUtil == null) {
                    nUtil = new MyJumpUtil();
                }
            }
        }
        return nUtil;
    }


    /** 线性布局 */
    public void startLinActivity(Activity activity, Bundle bundle) {
        startBaseActivityForResult(activity, LinearlayoutActivity.class, bundle, rqc_lin);
    }


    /** VideoActivity */
    public void startVideoActivity(Activity activity, Fragment fragment, Bundle bundle) {
//        if (fragment != null) {
//            fragmentStartBaseActivityForResult(activity, VideoActivity.class, fragment, bundle, rqc_activtiy_video);
//        } else {
//            startBaseActivityForResult(activity, VideoActivity.class, bundle, rqc_activtiy_video);
//        }
    }


}