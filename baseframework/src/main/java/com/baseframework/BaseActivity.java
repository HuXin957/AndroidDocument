package com.baseframework;

import static com.baseframework.BaseFrameworkSettings.DEBUGMODE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;

import com.baseframework.interfaces.ActivityResultCallback;
import com.baseframework.interfaces.BindView;
import com.baseframework.interfaces.BindViews;
import com.baseframework.interfaces.DarkNavigationBarTheme;
import com.baseframework.interfaces.DarkStatusBarTheme;
import com.baseframework.interfaces.FragmentLayout;
import com.baseframework.interfaces.FullScreen;
import com.baseframework.interfaces.GlobalLifeCircleListener;
import com.baseframework.interfaces.Layout;
import com.baseframework.interfaces.LifeCircleListener;
import com.baseframework.interfaces.NavigationBarBackgroundColor;
import com.baseframework.interfaces.NavigationBarBackgroundColorHex;
import com.baseframework.interfaces.NavigationBarBackgroundColorInt;
import com.baseframework.interfaces.NavigationBarBackgroundColorRes;
import com.baseframework.interfaces.OnClick;
import com.baseframework.interfaces.OnClicks;
import com.baseframework.interfaces.SwipeBack;
import com.baseframework.util.AppManager;
import com.baseframework.util.BuildProperties;
import com.baseframework.util.CycleRunner;
import com.baseframework.util.DebugLogG;
import com.baseframework.util.FragmentChangeUtil;
import com.baseframework.util.JumpParameter;
import com.baseframework.util.LanguageUtil;
import com.baseframework.util.OnJumpResponseListener;
import com.baseframework.util.OnPermissionResponseListener;
import com.baseframework.util.ParameterCache;
import com.baseframework.util.swipeback.util.SwipeBackActivityBase;
import com.baseframework.util.swipeback.util.SwipeBackActivityHelper;
import com.baseframework.util.swipeback.util.SwipeBackLayout;
import com.baseframework.util.swipeback.util.SwipeBackUtil;
import com.baseframework.util.toast.Toaster;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class BaseActivity extends AppCompatActivity implements SwipeBackActivityBase {

    private LifeCircleListener lifeCircleListener;                          //????????????????????????
    private static GlobalLifeCircleListener globalLifeCircleListener;       //??????????????????

    public boolean isActive = false;                                        //??????Activity??????????????????
    public boolean isAlive = false;                                         //??????Activity????????????????????????

    public OnJumpResponseListener onResponseListener;                       //jump????????????
    private OnPermissionResponseListener onPermissionResponseListener;      //??????????????????

    private FragmentChangeUtil fragmentChangeUtil;

    public BaseActivity me = this;

    private boolean isFullScreen = false;
    private boolean darkStatusBarThemeValue = false;
    private boolean darkNavigationBarThemeValue = false;
    private int navigationBarBackgroundColorValue = Color.BLACK;
    private int layoutResId = -1;
    private int fragmentLayoutId = -1;

    private Bundle savedInstanceState;
    private SwipeBackActivityHelper mHelper;

    @Override
    @Deprecated
    protected void onCreate(Bundle savedInstanceState) {
        AppManager.getInstance().preCreate(this);
        super.onCreate(savedInstanceState);
        if (BaseApp.getPrivateInstance() == null) {
            BaseApp.setPrivateInstance(getApplication());
        }
        SwipeBack swipeBack = getClass().getAnnotation(SwipeBack.class);
        if (swipeBack != null) {
            enableSwipeBack = swipeBack.value();
        }
        if (enableSwipeBack) mHelper = new SwipeBackActivityHelper(this);
        this.savedInstanceState = savedInstanceState;

        logG("\n" + me.getClass().getSimpleName(), "onCreate");
        info(2, me.getClass().getSimpleName() + ":onCreate");

        isAlive = true;

        initAttributes();

        if (!interceptSetContentView()) {
            layoutResId = resetLayoutResId();
            if (layoutResId == -1) {
                View contentView = resetContentView();
                if (contentView == null) {
                    errorLog("????????????Activity???Class????????????@Layout(??????layout??????id)?????????resetLayoutResId()?????????????????????");
                    return;
                } else {
                    setContentView(resetContentView());
                }
            } else {
                setContentView(layoutResId);
            }
        }

        if (isFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            setTranslucentStatus(true, Color.TRANSPARENT);
        }
        AppManager.getInstance().pushActivity(me);
        init();
        initBindViewAndFunctions();
        initViews();
        initFragments();
        bindAutoEvent();
        initDatas(getParameter());
        setEvents();
        getRootView().post(new Runnable() {
            @Override
            public void run() {
                lazyInit(getParameter());
            }
        });

        if (lifeCircleListener != null) {
            lifeCircleListener.onCreate();
        }
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.onCreate(me, me.getClass().getName());
        }
    }
    protected void init() {
    }
    public View resetContentView() {
        return null;
    }

    //?????? #resetContentView
    @Deprecated
    public boolean interceptSetContentView() {
        return false;
    }

    protected int resetLayoutResId() {
        return layoutResId;
    }

    private void initFragments() {
        if (fragmentLayoutId != -1 && fragmentChangeUtil == null) {
            fragmentChangeUtil = new FragmentChangeUtil(this, fragmentLayoutId);
            initFragment(fragmentChangeUtil);
        }
    }

    protected void initBindViewAndFunctions() {
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(BindView.class)) {
                    BindView bindView = field.getAnnotation(BindView.class);
                    if (bindView != null && bindView.value() != 0) {
                        field.setAccessible(true);
                        field.set(me, me.findViewById(bindView.value()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(BindViews.class)) {
                    BindViews bindView = field.getAnnotation(BindViews.class);
                    if (bindView != null && bindView.value().length != 0) {
                        List<View> viewList = new ArrayList<>();
                        for (int id : bindView.value()) {
                            viewList.add(findViewById(id));
                        }
                        field.setAccessible(true);
                        field.set(me, viewList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Method[] methods = getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(OnClick.class)) {
                    OnClick onClick = method.getAnnotation(OnClick.class);
                    if (onClick != null && onClick.value() != 0) {
                        View v = findViewById(onClick.value());
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    method.invoke(me, v);
                                } catch (Exception e) {
                                    try {
                                        method.invoke(me);
                                    } catch (Exception e1) {
                                    }
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Method[] methods = getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(OnClicks.class)) {
                    OnClicks onClicks = method.getAnnotation(OnClicks.class);
                    if (onClicks != null && onClicks.value().length != 0) {
                        for (int id : onClicks.value()) {
                            View v = findViewById(id);
                            v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        method.invoke(me, v);
                                    } catch (Exception e) {
                                        try {
                                            method.invoke(me);
                                        } catch (Exception e1) {
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindAutoEvent() {
        View backView = findViewById(R.id.back);
        if (backView != null) {
            backView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    /**
     * ??????????????????????????????????????? onBack()???????????? return ??????????????????????????????????????????
     */
    @Override
    @Deprecated
    public void onBackPressed() {
        if (!onBack()) {
            super.onBackPressed();
        }
    }

    /**
     * ?????? return ??????????????????????????????????????????
     * ????????? Activity ???????????? Fragment????????????????????????????????? Fragment ???????????????????????????
     *
     * @return true????????????????????????false????????????????????????
     */
    public boolean onBack() {
        if (fragmentChangeUtil != null && fragmentChangeUtil.getFocusFragment() != null) {
            return fragmentChangeUtil.getFocusFragment().onBack();
        }
        return false;
    }

    public void setLifeCircleListener(LifeCircleListener lifeCircleListener) {
        this.lifeCircleListener = lifeCircleListener;
    }

    protected boolean enableSwipeBack;

    //??????????????????
    private void initAttributes() {
        try {
            FullScreen fullScreen = getClass().getAnnotation(FullScreen.class);
            Layout layout = getClass().getAnnotation(Layout.class);
            FragmentLayout fragmentLayout = getClass().getAnnotation(FragmentLayout.class);
            DarkNavigationBarTheme darkNavigationBarTheme = getClass().getAnnotation(DarkNavigationBarTheme.class);
            DarkStatusBarTheme darkStatusBarTheme = getClass().getAnnotation(DarkStatusBarTheme.class);
            NavigationBarBackgroundColor navigationBarBackgroundColor = getClass().getAnnotation(NavigationBarBackgroundColor.class);
            NavigationBarBackgroundColorRes navigationBarBackgroundColorRes = getClass().getAnnotation(NavigationBarBackgroundColorRes.class);
            NavigationBarBackgroundColorInt navigationBarBackgroundColorInt = getClass().getAnnotation(NavigationBarBackgroundColorInt.class);
            NavigationBarBackgroundColorHex navigationBarBackgroundColorHex = getClass().getAnnotation(NavigationBarBackgroundColorHex.class);
            if (fullScreen != null) {
                isFullScreen = fullScreen.value();
                if (isFullScreen) {
                    requestFeature(Window.FEATURE_NO_TITLE);
                }
            }

            if (enableSwipeBack) {
                mHelper.onActivityCreate();
                setSwipeBackEnable(enableSwipeBack);
            }
            if (layout != null) {
                if (layout.value() != -1) {
                    layoutResId = layout.value();
                }
            }
            if (fragmentLayout != null) {
                fragmentLayoutId = fragmentLayout.value();
            }
            if (darkStatusBarTheme != null) {
                darkStatusBarThemeValue = darkStatusBarTheme.value();
            }
            if (darkNavigationBarTheme != null) {
                darkNavigationBarThemeValue = darkNavigationBarTheme.value();
            }
            if (navigationBarBackgroundColor != null) {
                if (navigationBarBackgroundColor.a() != -1 && navigationBarBackgroundColor.r() != -1 && navigationBarBackgroundColor.g() != -1 && navigationBarBackgroundColor.b() != -1) {
                    navigationBarBackgroundColorValue = Color.argb(navigationBarBackgroundColor.a(), navigationBarBackgroundColor.r(), navigationBarBackgroundColor.g(), navigationBarBackgroundColor.b());
                }
            }
            if (navigationBarBackgroundColorRes != null) {
                if (navigationBarBackgroundColorRes.value() != -1) {
                    navigationBarBackgroundColorValue = getColorS(navigationBarBackgroundColorRes.value());
                }
            }
            if (navigationBarBackgroundColorInt != null) {
                navigationBarBackgroundColorValue = navigationBarBackgroundColorInt.value();
            }
            if (navigationBarBackgroundColorHex != null) {
                navigationBarBackgroundColorValue = Color.parseColor(navigationBarBackgroundColorHex.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean requestFeature(int featureId) {
        try {
            return getWindow().requestFeature(featureId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void finish() {
        AppManager.getInstance().killActivity(me);
    }

    public void finishActivity() {
        super.finish();
    }

    //?????????????????????

    /**
     * initViews????????????????????????????????????????????????????????????????????????View??????????????????
     */
    public abstract void initViews();

    /**
     * initDatas??????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param parameter ???????????????????????????????????????GET???SET????????????????????????
     */
    public abstract void initDatas(JumpParameter parameter);

    /**
     * setEvents????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public abstract void setEvents();

    /**
     * ?????? BaseFragment ??????????????????????????? @FragmentLayout(R.layout.frameLayout) ?????????????????? FragmentChangeUtil????????????????????? addFragment
     *
     * @param fragmentChangeUtil BaseFragment??????????????????
     */
    public void initFragment(FragmentChangeUtil fragmentChangeUtil) {

    }

    /**
     * ??????????????? BaseFragment
     *
     * @param index ???????????? Fragment ??????????????????
     */
    public void changeFragment(int index) {
        if (fragmentChangeUtil != null) {
            fragmentChangeUtil.show(index);
        } else {
            initFragments();
        }
    }

    public void changeFragment(int index, int enterAnimResId, int exitAnimResId) {
        if (fragmentChangeUtil != null) {
            fragmentChangeUtil.anim(enterAnimResId, exitAnimResId).show(index);
        } else {
            initFragments();
        }
    }

    /**
     * ??????????????? BaseFragment
     *
     * @param fragment ???????????? Fragment ??????
     */
    public void changeFragment(BaseFragment fragment) {
        if (fragmentChangeUtil != null) {
            fragmentChangeUtil.show(fragment);
        }
    }

    public void changeFragment(BaseFragment fragment, int enterAnimResId, int exitAnimResId) {
        if (fragmentChangeUtil != null) {
            fragmentChangeUtil.anim(enterAnimResId, exitAnimResId).show(fragment);
        }
    }

    /**
     * ?????? FragmentChangeUtil ??????
     */
    public FragmentChangeUtil getFragmentChangeUtil() {
        return fragmentChangeUtil;
    }

    //????????????????????????
    public void setDarkStatusBarTheme(boolean value, int color) {
        darkStatusBarThemeValue = value;
        setTranslucentStatus(true, color);
    }

    public void setDarkNavigationBarTheme(boolean value) {
        darkNavigationBarThemeValue = value;
        setTranslucentStatus(true, Color.TRANSPARENT);
    }

    public void setNavigationBarBackgroundColor(@ColorInt int color) {
        navigationBarBackgroundColorValue = color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(navigationBarBackgroundColorValue);
        }
    }

    public void setNavigationBarBackgroundColor(int a, int r, int g, int b) {
        navigationBarBackgroundColorValue = Color.argb(a, r, g, b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(navigationBarBackgroundColorValue);
        }
    }

    //???????????????
    protected void setTranslucentStatus(boolean on, int color) {
        if (isMIUI()) {
            setStatusBarDarkModeInMIUI(darkStatusBarThemeValue, this);
        }
        if (isFlyme()) {
            setStatusBarDarkIconInFlyme(getWindow(), darkStatusBarThemeValue);
        }
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            if (darkStatusBarThemeValue) {
                if (darkNavigationBarThemeValue) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    );
                } else {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    );
                }
            } else {
                if (darkNavigationBarThemeValue) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    );
                } else {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    );
                }
            }

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams winParams = window.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            window.setAttributes(winParams);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(navigationBarBackgroundColorValue);
        }
    }

    private void setStatusBarDarkModeInMIUI(boolean darkmode, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean setStatusBarDarkIconInFlyme(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                Log.e("MeiZu", "setStatusBarDarkIcon: failed");
            }
        }
        return result;
    }

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    //MIUI??????
    public static boolean isMIUI() {
        try {
            BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    //Flyme??????
    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");

            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    protected final static String NULL = "";
    private Toast toast;

    public void runOnMain(Runnable runnable) {
        if (!isAlive) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    public void runOnMainDelayed(Runnable runnable, long time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnMain(runnable);
            }
        }, time);
    }

    public void runDelayed(Runnable runnable, long time) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, time);
    }

    private List<CycleRunner> cycleTimerList = new ArrayList<>();

    public CycleRunner runCycle(Runnable runnable, long firstDelay, long interval) {
        CycleRunner runner = new CycleRunner();
        runner.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, firstDelay, interval);
        cycleTimerList.add(runner);
        return runner;
    }

    public CycleRunner runOnMainCycle(Runnable runnable, long firstDelay, long interval) {
        CycleRunner runner = new CycleRunner();
        runner.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnMain(runnable);
            }
        }, firstDelay, interval);
        cycleTimerList.add(runner);
        return runner;
    }

    //????????????
    public void toast(final Object obj) {
        try {
            runOnMain(new Runnable() {
                @Override
                public void run() {
                    logG("toast", obj.toString());
                    if (toast == null) {
                        toast = Toast.makeText(BaseActivity.this, NULL, Toast.LENGTH_SHORT);
                    }
                    toast.setText(obj.toString());
                    toast.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toastS(final Object obj) {
        Toaster.build(me).show(obj.toString());
    }

    //??????Log
    public void log(final Object obj) {
        DebugLogG.LogI(obj);
    }

    public void log(final Object obj, boolean showStack) {
        DebugLogG.LogI(obj, showStack);
    }

    public void errorLog(final Object obj) {
        DebugLogG.LogE(obj);
    }

    private void info(int level, String msg) {
        if (!DEBUGMODE) {
            return;
        }
        switch (level) {
            case 0:
                Log.v(">>>", msg);
                break;
            case 1:
                Log.i(">>>", msg);
                break;
            case 2:
                Log.d(">>>", msg);
                break;
            case 3:
                Log.e(">>>", msg);
                break;
        }
    }

    //????????????????????????
    public void showIME(boolean show, EditText editText) {
        if (editText == null) {
            return;
        }
        if (show) {
            editText.requestFocus();
            editText.setFocusableInTouchMode(true);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showIME(@NonNull EditText editText) {
        if (editText == null) {
            return;
        }
        editText.requestFocus();
        editText.setFocusableInTouchMode(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void hideIME(@Nullable EditText editText) {
        if (editText != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    //?????????
    @Deprecated
    public void setIMMStatus(boolean show, EditText editText) {
        showIME(show, editText);
    }

    public static String StartFindWords = "";

    //????????????dip???px??????
    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //????????????px???dip??????
    public int px2dip(float pxValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    //????????????
    private final String TAG = "PermissionsUtil";
    private int REQUEST_CODE_PERMISSION = 0x00099;

    /**
     * ????????????
     * <p>
     * ???????????????????????????????????????????????????????????????????????????????????????????????????AndroidManifest.xml????????????????????????
     * Android6.0+?????????????????????????????????????????????????????????????????????AndroidManifest.xml??????????????????
     *
     * @param permissions                  ???????????????
     * @param onPermissionResponseListener ???????????????
     */
    public void requestPermission(String[] permissions, OnPermissionResponseListener onPermissionResponseListener) {
        this.onPermissionResponseListener = onPermissionResponseListener;
        if (checkPermissions(permissions)) {
            if (onPermissionResponseListener != null) {
                onPermissionResponseListener.onSuccess(permissions);
            }
        } else {
            List<String> needPermissions = getDeniedPermissions(permissions);
            ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //???????????????
    public boolean checkPermissions(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param permissions
     * @return
     */
    private List<String> getDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                needRequestPermissionList.add(permission);
            }
        }
        return needRequestPermissionList;
    }


    /**
     * ????????????????????????
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (verifyPermissions(grantResults)) {
                if (onPermissionResponseListener != null) {
                    onPermissionResponseListener.onSuccess(permissions);
                }
            } else {
                if (onPermissionResponseListener != null) {
                    onPermissionResponseListener.onFail();
                }
                showTipsDialog();
            }
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param grantResults
     * @return
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * ?????????????????????
     */
    private void showTipsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("??????")
                .setMessage("????????????????????????????????????????????????????????????????????????????????????\n??????????????????????????????????????????????????????????????????????????????????????????")
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                }).show();
    }

    //??????????????????????????????
    public void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    //????????????????????????
    public int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //??????????????????
    public int getDisplayWidth() {
        Display disp = getWindowManager().getDefaultDisplay();
        Point outP = new Point();
        disp.getSize(outP);
        return outP.x;
    }

    //?????????????????????????????????????????????-???????????????-?????????????????????
    public int getDisplayHeight() {
        Display disp = getWindowManager().getDefaultDisplay();
        Point outP = new Point();
        disp.getSize(outP);
        return outP.y;
    }

    //??????????????????
    public int getNavbarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowInsets windowInsets = null;
            windowInsets = getWindow().getDecorView().getRootView().getRootWindowInsets();
            if (windowInsets != null) {
                return windowInsets.getStableInsetBottom();
            }
        }
        int resourceId = 0;
        int rid = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    //?????????????????????????????????????????????0
    public int getRootHeight() {
        int diaplayHeight = 0;
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
            diaplayHeight = point.y;
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            diaplayHeight = dm.heightPixels; //????????????```
        }
        return diaplayHeight;
    }

    //????????????
    public ObjectAnimator moveAnimation(Object obj, String perference, float aimValue, long time, long delay) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(obj, perference, aimValue);
        objectAnimator.setDuration(time);
        objectAnimator.setStartDelay(delay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            objectAnimator.setAutoCancel(true);
        }
        objectAnimator.start();
        return objectAnimator;
    }

    public ObjectAnimator moveAnimation(Object obj, String perference, float aimValue, long time) {
        return moveAnimation(obj, perference, aimValue, time, 0);
    }

    public ObjectAnimator moveAnimation(Object obj, String perference, float aimValue) {
        return moveAnimation(obj, perference, aimValue, 300, 0);
    }

    //????????????????????????
    public boolean copy(String s) {
        if (isNull(s)) {
            log("????????????????????????");
            return false;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", s);
        cm.setPrimaryClip(mClipData);
        return true;
    }

    //??????????????????????????????
    public static boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s) || "(null)".equals(s)) {
            return true;
        }
        return false;
    }

    //????????????????????????
    public boolean jump(Class<?> cls) {
        try {
            startActivity(new Intent(me, cls));
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    //??????????????????????????????????????????
    public boolean jump(Class<?> cls, JumpParameter jumpParameter) {
        try {
            if (jumpParameter != null) {
                ParameterCache.getInstance().set(cls.getName(), jumpParameter);
            }
            startActivity(new Intent(me, cls));
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    //?????????????????????
    public boolean jump(Class<?> cls, OnJumpResponseListener onResponseListener) {
        return jump(cls, null, onResponseListener);
    }

    //???????????????????????????
    public boolean jump(Class<?> cls, JumpParameter jumpParameter, OnJumpResponseListener onResponseListener) {
        try {
            startActivity(new Intent(me, cls));
            ParameterCache.getInstance().cleanResponse(me.getClass().getName());
            if (jumpParameter == null) {
                jumpParameter = new JumpParameter();
            }
            ParameterCache.getInstance().set(cls.getName(), jumpParameter
                    .put("needResponse", true)
                    .put("responseClassName", getInstanceKey())
            );
            this.onResponseListener = onResponseListener;
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    //????????????????????????????????????
    public boolean jump(Class<?> cls, View transitionView) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                me.setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                        for (View view : sharedElements) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                });
                startActivity(new Intent(me, cls), ActivityOptions.makeSceneTransitionAnimation(me, transitionView, transitionView.getTransitionName()).toBundle());
            } else {
                startActivity(new Intent(me, cls));
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public boolean jump(Class<?> cls, View... transitionViews) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                me.setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                        for (View view : sharedElements) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                });

                Pair<View, String>[] pairs = new Pair[transitionViews.length];
                int i = 0;
                for (View tv : transitionViews) {
                    Pair<View, String> pair = new Pair<>(tv, tv.getTransitionName());
                    pairs[i] = pair;
                    i++;
                }
                startActivity(new Intent(me, cls), ActivityOptions.makeSceneTransitionAnimation(me, pairs).toBundle());
            } else {
                startActivity(new Intent(me, cls));
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    //?????????????????????????????????????????????
    public boolean jump(Class<?> cls, JumpParameter jumpParameter, View transitionView) {
        try {
            if (jumpParameter != null) {
                ParameterCache.getInstance().set(cls.getName(), jumpParameter);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                me.setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                        for (View view : sharedElements) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                });
                startActivity(new Intent(me, cls), ActivityOptions.makeSceneTransitionAnimation(me, transitionView, transitionView.getTransitionName()).toBundle());
            } else {
                startActivity(new Intent(me, cls));
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public boolean jump(Class<?> cls, JumpParameter jumpParameter, View... transitionViews) {
        try {
            if (jumpParameter != null) {
                ParameterCache.getInstance().set(cls.getName(), jumpParameter);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                me.setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                        for (View view : sharedElements) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                });

                Pair<View, String>[] pairs = new Pair[transitionViews.length];
                int i = 0;
                for (View tv : transitionViews) {
                    Pair<View, String> pair = new Pair<>(tv, tv.getTransitionName());
                    pairs[i] = pair;
                    i++;
                }

                startActivity(new Intent(me, cls), ActivityOptions.makeSceneTransitionAnimation(me, pairs).toBundle());
            } else {
                startActivity(new Intent(me, cls));
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    //?????????????????????????????????????????????
    public boolean jump(Class<?> cls, OnJumpResponseListener onResponseListener, View transitionView) {
        return jump(cls, null, onResponseListener, transitionView);
    }

    //???????????????????????????????????????????????????
    public boolean jump(Class<?> cls, JumpParameter jumpParameter, OnJumpResponseListener onResponseListener, View transitionView) {
        try {
            ParameterCache.getInstance().cleanResponse(me.getClass().getName());
            if (jumpParameter == null) {
                jumpParameter = new JumpParameter();
            }
            ParameterCache.getInstance().set(cls.getName(), jumpParameter
                    .put("needResponse", true)
                    .put("responseClassName", getInstanceKey())
            );
            this.onResponseListener = onResponseListener;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                me.setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                        for (View view : sharedElements) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                });
                startActivity(new Intent(me, cls), ActivityOptions.makeSceneTransitionAnimation(me, transitionView, transitionView.getTransitionName()).toBundle());
            } else {
                startActivity(new Intent(me, cls));

            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public void jumpAnim(int enterAnim, int exitAnim) {
        int version = Integer.valueOf(Build.VERSION.SDK_INT);
        if (version > 5) {
            overridePendingTransition(enterAnim, exitAnim);
        }
    }

    //??????Activity???????????????????????????
    public void setResponse(JumpParameter jumpParameter) {
        BaseActivity backResponseActivity = AppManager.getInstance().getActivityInstance(getParameter().getString("responseClassName"));
        if (backResponseActivity != null) {
            backResponseActivity.setResponseMessage(jumpParameter);
        }
    }

    private Runnable waitResponseRunnable;

    protected void setResponseMessage(JumpParameter jumpParameter) {
        log(getClass().getName() + ".setResponseMessage: " + jumpParameter);
        waitResponseRunnable = new Runnable() {
            @Override
            public void run() {
                if (onResponseListener != null) {
                    JumpParameter responseData = jumpParameter;
                    if (responseData == null) {
                        responseData = new JumpParameter();
                    }
                    onResponseListener.OnResponse(responseData);
                    onResponseListener = null;
                }
            }
        };
        if (isActive) {
            runOnMain(waitResponseRunnable);
            waitResponseRunnable = null;
        }
    }

    //??????Activity????????????????????????????????????2
    public void returnParameter(JumpParameter parameter) {
        setResponse(parameter);
    }

    //??????????????????
    public JumpParameter getParameter() {
        JumpParameter jumpParameter = ParameterCache.getInstance().get(me.getClass().getName());
        if (jumpParameter == null) {
            jumpParameter = new JumpParameter();
        }
        return jumpParameter;
    }

    protected void lazyInit(JumpParameter parameter) {
    }



    @Override
    protected void onResume() {
        isActive = true;
        logG("\n" + me.getClass().getSimpleName(), "onResume");
        super.onResume();
        if (waitResponseRunnable != null) {
            runOnMain(waitResponseRunnable);
            waitResponseRunnable = null;
        }
        if (lifeCircleListener != null) {
            lifeCircleListener.onResume();
        }
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.onResume(me, me.getClass().getName());
        }
        AppManager.setActiveActivity(this);

        if (resumeRunnableList != null) {
            CopyOnWriteArrayList<Runnable> copyOnWriteArrayList = new CopyOnWriteArrayList<>(resumeRunnableList);
            for (Runnable runnable : copyOnWriteArrayList) {
                runOnMain(runnable);
            }
            resumeRunnableList.removeAll(copyOnWriteArrayList);
        }
    }

    private List<Runnable> resumeRunnableList;

    public void runOnResume(Runnable resumeRunnable) {
        if (resumeRunnableList == null) {
            resumeRunnableList = new ArrayList<>();
        }
        resumeRunnableList.add(resumeRunnable);
    }

    public void cleanResumeRunnable() {
        resumeRunnableList = new ArrayList<>();
    }

    public void deleteResumeRunnable(Runnable resumeRunnable) {
        if (resumeRunnableList != null) resumeRunnableList.remove(resumeRunnable);
    }

    @Override
    protected void onPause() {
        if (Toaster.isSupportToast) {
            Toaster.cancel();
        }
        isActive = false;
        logG("\n" + me.getClass().getSimpleName(), "onPause");
        if (lifeCircleListener != null) {
            lifeCircleListener.onPause();
        }
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.onPause(me, me.getClass().getName());
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        isAlive = false;
        logG("\n" + me.getClass().getSimpleName(), "onDestroy");
        info(2, me.getClass().getSimpleName() + ":onDestroy");
        if (getParameter() != null) {
            getParameter().cleanAll();
        }
        AppManager.getInstance().deleteActivity(me);
        if (lifeCircleListener != null) {
            lifeCircleListener.onDestroy();
        }
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.onDestroy(me, me.getClass().getName());
        }
        for (CycleRunner runnable : cycleTimerList) {
            if (!runnable.isCanceled()) {
                runnable.cancel();
            }
        }
        super.onDestroy();
    }

    //?????????????????????Log????????????????????????????????????????????????????????????????????????????????????
    public void bigLog(String msg) {
        DebugLogG.bigLog(msg, false);
    }

    public static GlobalLifeCircleListener getGlobalLifeCircleListener() {
        return globalLifeCircleListener;
    }

    public static void setGlobalLifeCircleListener(GlobalLifeCircleListener globalLifeCircleListener) {
        BaseActivity.globalLifeCircleListener = globalLifeCircleListener;
    }

    public static boolean DEBUGMODE() {
        return DEBUGMODE;
    }

    private void logG(String tag, Object o) {
        DebugLogG.LogG(tag + ">>>" + o.toString());
    }

    //?????????????????????????????????
    public boolean openUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
            return false;
        }
    }

    //????????????App
    public boolean openApp(String packageName) {
        PackageManager packageManager = getPackageManager();
        if (isInstallApp(packageName)) {
            try {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                startActivity(intent);
                return true;
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            }
        } else {
            return false;
        }
    }

    //??????App???????????????
    public boolean isInstallApp(String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }

    //??????IMEI (???????????? AndroidManifest.xml ????????????<uses-permission android:name="android.permission.READ_PHONE_STATE"/>)
    @SuppressLint({"WrongConstant", "MissingPermission"})
    public String getIMEI() {
        String result = null;
        try {
            if (checkPermissions(new String[]{"android.permission.READ_PHONE_STATE"})) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService("phone");
                if (telephonyManager != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        try {
                            Method method = telephonyManager.getClass().getMethod("getImei", new Class[0]);
                            method.setAccessible(true);
                            result = (String) method.invoke(telephonyManager, new Object[0]);
                        } catch (Exception e) {
                        }
                        if (isNull(result)) {
                            result = telephonyManager.getDeviceId();
                        }
                    } else {
                        result = telephonyManager.getDeviceId();
                    }
                }
            } else {
                requestPermission(new String[]{"android.permission.READ_PHONE_STATE"}, new OnPermissionResponseListener() {
                    @Override
                    public void onSuccess(String[] permissions) {
                        getIMEI();
                    }

                    @Override
                    public void onFail() {
                        if (BaseFrameworkSettings.DEBUGMODE) {
                            Log.e(">>>", "getIMEI(): ???????????????????????????READ_PHONE_STATE");
                        }
                    }
                });
            }
        } catch (Exception e) {
            if (BaseFrameworkSettings.DEBUGMODE) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getAndroidId() {
        return BaseFrameworkSettings.getAndroidId();
    }

    //??????Mac?????? (???????????? AndroidManifest.xml ????????????<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>)
    public String getMacAddress() {
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:02";
        }
        return macAddress;
    }

    public void restartMe() {
        finish();
        jump(me.getClass());
        jumpAnim(R.anim.fade, R.anim.hold);
    }

    //?????????????????????????????????
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (enableSwipeBack) mHelper.onPostCreate();
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return enableSwipeBack ? mHelper.getSwipeBackLayout() : null;
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        SwipeBackUtil.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    @Override
    protected void attachBaseContext(Context c) {
        super.attachBaseContext(LanguageUtil.wrap(c));
    }

    //????????????SDK???getColor??????
    public int getColorS(@ColorRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(id, getTheme());
        } else {
            return getResources().getColor(id);
        }
    }

    public View getRootView() {
        return getWindow().getDecorView().findViewById(android.R.id.content);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.windowFocus(me, me.getClass().getName(), hasFocus);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.remove("android:support:fragments");
    }

    public void click(View v, View.OnClickListener onClickListener) {
        v.setFocusableInTouchMode(true);
        v.setOnTouchListener(new View.OnTouchListener() {
            int touchFlag = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (touchFlag == 0) {
                            touchFlag = 1;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (touchFlag == 1) {
                            touchFlag = -1;
                            onClickListener.onClick(v);
                            runDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    touchFlag = 0;
                                }
                            }, 500);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (touchFlag == 1) {
                            touchFlag = 0;
                        }
                        break;
                }
                return true;
            }
        });
    }

    public String getInstanceKey() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    private List<ActivityResultCallback> activityResultCallbackList;

    public void startActivityForResult(Intent intent, ActivityResultCallback activityResultCallback) {
        if (activityResultCallbackList == null) activityResultCallbackList = new ArrayList<>();
        if (activityResultCallback.getResultId() == 0) {
            activityResultCallback.setResultId(100000 + activityResultCallbackList.size());
        }
        activityResultCallbackList.add(activityResultCallback);
        super.startActivityForResult(intent, activityResultCallback.getResultId());
    }

    public void startActivityForResult(Intent intent, ActivityResultCallback activityResultCallback, @Nullable Bundle options) {
        if (activityResultCallbackList == null) activityResultCallbackList = new ArrayList<>();
        if (activityResultCallback.getResultId() == 0) {
            activityResultCallback.setResultId(100 + activityResultCallbackList.size());
        }
        activityResultCallbackList.add(activityResultCallback);
        super.startActivityForResult(intent, activityResultCallback.getResultId(), options);
    }

    public void setActivityResultCallbackList(ActivityResultCallback activityResultCallback) {
        if (activityResultCallbackList == null) activityResultCallbackList = new ArrayList<>();
        if (activityResultCallback.getResultId() == 0) {
            activityResultCallback.setResultId(100 + activityResultCallbackList.size());
        }
        activityResultCallbackList.add(activityResultCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (activityResultCallbackList != null) {
            List<ActivityResultCallback> runActivityResultCallback = new ArrayList<>();
            for (ActivityResultCallback callback : activityResultCallbackList) {
                if (callback.getResultId() == requestCode || callback.getResultId() < 0) {
                    callback.onActivityResult(requestCode, resultCode, data);
                    if (callback.getResultId() != -2) runActivityResultCallback.add(callback);
                }
            }
            activityResultCallbackList.removeAll(runActivityResultCallback);
        }
    }

    public static <B extends BaseActivity> B getActivity(String instanceKey) {
        return (B) AppManager.getInstance().getActivityInstance(instanceKey);
    }

    public static <B extends BaseActivity> B getActivity(Class c) {
        return (B) AppManager.getInstance().getActivityInstance(c);
    }

    @Override
    protected void onStart() {
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.onStart(me, me.getClass().getName());
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (globalLifeCircleListener != null) {
            globalLifeCircleListener.onStop(me, me.getClass().getName());
        }
        super.onStop();
    }
}