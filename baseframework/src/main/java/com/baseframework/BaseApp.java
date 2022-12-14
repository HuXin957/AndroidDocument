package com.baseframework;

import static com.baseframework.BaseFrameworkSettings.DEBUGMODE;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import com.baseframework.interfaces.OnBugReportListener;
import com.baseframework.interfaces.OnSDKInitializedCallBack;
import com.baseframework.util.AppManager;
import com.baseframework.util.DebugLogG;
import com.baseframework.util.SettingsUtil;
import com.baseframework.util.toast.Toaster;

import java.lang.reflect.Field;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2019/9/23 12:43
 */
public abstract class BaseApp<YourApp extends BaseApp> extends Application {
    
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private OnSDKInitializedCallBack onSDKInitializedCallBack;
    
    private boolean isInitializedSDKs;
    
    public abstract void init();
    
    public void initSDKs() {
    }
    
    public void initSDKInitialized() {
    }
    
    public YourApp me;
    private static Application instance;
    
    public static <App extends BaseApp> App getInstance(Class<App> appClass) {
        return (App) instance;
    }
    
    @Override
    @Deprecated
    public void onCreate() {
        super.onCreate();
        
        me = (YourApp) this;
        instance = me;
        
        init();
        new Thread() {
            @Override
            public void run() {
                synchronized (me) {
                    isInitializedSDKs = false;
                    initSDKs();
                    
                    if (onSDKInitializedCallBack != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                initSDKInitialized();
                                onSDKInitializedCallBack.onInitialized();
                                isInitializedSDKs = true;
                            }
                        });
                    }
                }
            }
        }.start();
    }
    
    public boolean isInitializedSDKs() {
        return isInitializedSDKs;
    }
    
    public BaseApp<YourApp> setOnSDKInitializedCallBack(OnSDKInitializedCallBack onSDKInitializedCallBack) {
        this.onSDKInitializedCallBack = onSDKInitializedCallBack;
        return this;
    }
    
    public void log(final Object obj) {
        DebugLogG.LogI(obj);
    }
    
    public void error(final Object obj) {
        DebugLogG.LogE(obj);
    }
    
    public void bigLog(String msg) {
        DebugLogG.bigLog(msg, false);
    }
    
    private void logG(String tag, Object o) {
        DebugLogG.LogG(tag + ">>>" + o.toString());
    }
    
    protected final static String NULL = "";
    private Toast toast;
    
    public void toast(final Object obj) {
        try {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    logG("toast", obj.toString());
                    if (toast == null) {
                        toast = Toast.makeText(BaseApp.this, NULL, Toast.LENGTH_SHORT);
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
    
    public void runOnMain(Runnable runnable) {
        mainHandler.post(new Runnable() {
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, time);
    }
    
    //????????????dip???px??????
    public static int dip2px(float dpValue) {
        if (getPrivateInstance() == null) {
            return (int) (dpValue * (Resources.getSystem().getDisplayMetrics().density) + 0.5f);
        }
        final float scale = getPrivateInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    //????????????px???dip??????
    public static int px2dip(float pxValue) {
        if (getPrivateInstance() == null) {
            return (int) (pxValue / (Resources.getSystem().getDisplayMetrics().density) + 0.5f);
        }
        final float scale = getPrivateInstance().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    
    //??????????????????????????????
    public void startAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
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
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display disp = windowManager.getDefaultDisplay();
        Point outP = new Point();
        disp.getSize(outP);
        return outP.x;
    }
    
    //?????????????????????????????????????????????-???????????????-?????????????????????
    public int getDisplayHeight() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display disp = windowManager.getDefaultDisplay();
        Point outP = new Point();
        disp.getSize(outP);
        return outP.y;
    }
    
    //??????????????????
    public int getNavbarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                WindowInsets windowInsets = null;
                windowInsets = AppManager.getInstance().currentActivity().getWindow().getDecorView().getRootView().getRootWindowInsets();
                if (windowInsets != null) {
                    return windowInsets.getStableInsetBottom();
                }
            } catch (Exception e) {
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
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int diaplayHeight = 0;
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
            diaplayHeight = point.y;
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dm);
            diaplayHeight = dm.heightPixels; //????????????```
        }
        return diaplayHeight;
    }
    
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
    
    public static boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s) || "(null)".equals(s)) {
            return true;
        }
        return false;
    }
    
    //????????????App
    public static boolean openApp(String packageName) {
        if (getPrivateInstance() == null) {
            return false;
        }
        PackageManager packageManager = getPrivateInstance().getPackageManager();
        if (isInstallApp(packageName)) {
            try {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                getPrivateInstance().startActivity(intent);
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
    public static boolean isInstallApp(String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPrivateInstance().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }
    
    public static Settings Settings(String path) {
        return new Settings(path);
    }
    
    public static class Settings extends SettingsUtil {
        
        public Settings(String path) {
            super(path);
        }
    }
    
    /**
     * ???????????????????????????????????? application ??????????????????????????? context ????????????????????????????????????????????????
     * ??????????????? App extends BaseApp ???????????????????????????
     * ???????????????????????????????????????????????? BaseActivity ????????????????????????
     *
     * @param context Application ?????????
     * @hide
     */
    public static void setPrivateInstance(Application context) {
        BaseApp.instance = context;
    }
    
    /**
     * ??????????????????????????????????????? application ??????????????????????????? context ????????????????????????????????????????????????
     * ??????????????? App ?????????????????????????????????????????????
     * ??????????????? getInstance() ?????????????????? {@link #getInstance(Class)} ????????????
     *
     * @return ????????? application ??????
     */
    public static Application getPrivateInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }
    
    public void setOnCrashListener(OnBugReportListener onBugReportListener) {
        BaseFrameworkSettings.turnOnReadErrorInfoPermissions(this, onBugReportListener);
    }
    
    public void exit() {
        BaseFrameworkSettings.exitApp();
    }
    
    public static String getAndroidId() {
        return BaseFrameworkSettings.getAndroidId();
    }
}