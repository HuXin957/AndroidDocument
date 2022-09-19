package com.docandroid.main;

/**
 * @author: majin
 * @createTime: 2022/9/19
 * @desc: 主页
 */
import android.annotation.SuppressLint;
import android.view.View;

import com.baseframework.BaseActivity;
import com.baseframework.BaseFragment;
import com.baseframework.interfaces.BindView;
import com.baseframework.interfaces.DarkNavigationBarTheme;
import com.baseframework.interfaces.DarkStatusBarTheme;
import com.baseframework.interfaces.FragmentLayout;
import com.baseframework.interfaces.Layout;
import com.baseframework.interfaces.NavigationBarBackgroundColorRes;
import com.baseframework.interfaces.OnFragmentChangeListener;
import com.baseframework.util.FragmentChangeUtil;
import com.baseframework.util.JumpParameter;
import com.docandroid.R;
import com.docandroid.main.fr.FrLayout;
import com.docandroid.main.fr.FrWidget;
import com.kongzue.tabbar.Tab;
import com.kongzue.tabbar.TabBarView;
import com.kongzue.tabbar.interfaces.OnTabChangeListener;

import java.util.ArrayList;
import java.util.List;

//使用 @Layout 注解直接绑定要显示的布局
@SuppressLint("NonConstantResourceId")
@Layout(R.layout.activity_main)
//设置不使用状态栏暗色文字图标样式
@DarkStatusBarTheme(true)
//设置底部导航栏背景颜色，此外还可以使用 @NavigationBarBackgroundColor 来指定 argb 颜色
@NavigationBarBackgroundColorRes(R.color.white)
//设置使用底部导航栏暗色图标样式
@DarkNavigationBarTheme(true)
//绑定子 Fragment 要显示的容器布局
@FragmentLayout(R.id.viewPager)

public class MainActivity extends BaseActivity {
    //子 Fragment （布局 、组件）
    private FrLayout frLayout = new FrLayout();
    private FrWidget frWidget = new FrWidget();


    //使用 @BindView(resId) 来初始化组件
    @BindView(R.id.tabbar)
    private TabBarView tabbar;

    @Override
    public void initViews() {
        tabbar = findViewById(R.id.tabbar);
    }

    @Override
    public void initDatas(JumpParameter parameter) {
        List<Tab> tabs = new ArrayList<>();
        tabs.add(new Tab(this, getString(R.string.tab_layout), R.mipmap.img_tab_introduction));
        tabs.add(new Tab(this, getString(R.string.tab_widget), R.mipmap.img_maintab_function));
        tabbar.setTab(tabs);
    }

    @Override
    //此处为添加子布局逻辑
    public void initFragment(FragmentChangeUtil fragmentChangeUtil) {
        fragmentChangeUtil.addFragment(frLayout);
        fragmentChangeUtil.addFragment(frWidget);
        //默认切换至第一个界面
        changeFragment(0);
    }

    @Override
    //此处为组件绑定功能事件、回调等方法
    public void setEvents() {
        tabbar.setOnTabChangeListener(new OnTabChangeListener() {
            @Override
            public boolean onTabChanged(View v, int index) {
                changeFragment(index);
                return false;
            }
        });

        getFragmentChangeUtil().setOnFragmentChangeListener(new OnFragmentChangeListener() {
            @Override
            public void onChange(int index, BaseFragment fragment) {
                tabbar.setNormalFocusIndex(index);
            }
        });
    }
}