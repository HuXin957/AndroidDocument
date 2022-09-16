package com.docandroid;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;


public class MyApp extends Application {

    private static MyApp instance;

    private String cookie;


    private int isJoin = -1;//0 未加 1 已加
    private boolean isChange;//true party 加入或者删除 false 无变化


    public static MyApp getInstance() {
        if (instance == null) {
            synchronized (MyApp.class) {
                if (instance == null) {
                    instance = new MyApp();
                }
            }
        }
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) initLogger();

    }


    /**
     * Logger初始化
     */
    private void initLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // 是否显示线程信息，默认为ture
                .methodCount(3)         // 显示的方法行数，默认为2
                .methodOffset(5)        // 隐藏内部方法调用到偏移量，默认为5
               // .logStrategy() // 更改要打印的日志策略。
                .tag("majin")   // 每个日志的全局标记。默认PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    static {
        //启用矢量图兼容
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//        //设置全局默认配置（优先级最低，会被其他设置覆盖）
//        SmartRefreshLayout.setDefaultRefreshInitializer(new DefaultRefreshInitializer() {
//            @Override
//            public void initialize(@NonNull Context context, @NonNull RefreshLayout layout) {
//                //全局设置（优先级最低）
//                layout.setEnableAutoLoadMore(true);
//                layout.setEnableOverScrollDrag(false);
//                layout.setEnableOverScrollBounce(true);
//                layout.setEnableLoadMoreWhenContentNotFull(true);
//                layout.setEnableScrollContentWhenRefreshed(true);
//            }
//        });
//        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
//            @NonNull
//            @Override
//            public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
//                //全局设置主题颜色（优先级第二低，可以覆盖 DefaultRefreshInitializer 的配置，与下面的ClassicsHeader绑定）
//                //                layout.setPrimaryColorsId(R.color.color_page_bg, android.R.color.black);
//                // return new ClassicsHeader(context).setTimeFormat(new DynamicTimeFormat("更新于 %s"));
//                return new WmpsHeaderView(context, R.layout.refresh_view);
//            }
//        });

//        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
//            @NonNull
//            @Override
//            public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
//                //  return new WmpsFooterView(context, R.layout.refresh_view);
//                return new WmpsFooterView(context, R.layout.refresh_view);
//                //   return new ClassicsFooter(context);
//            }
//        });
//


    }
}
