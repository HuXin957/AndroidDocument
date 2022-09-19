package com.baseframework.util;

import com.baseframework.BaseFragment;

import java.util.Objects;
import java.util.Stack;

/**
 * @author: majin
 * @createTime: 2022/9/19
 * @desc: BaseFragmentManager
 */
public class BaseFragmentManager {
    
    private static BaseFragmentManager instance;
    
    private BaseFragmentManager() {
    }
    
    public static BaseFragmentManager getInstance() {
        synchronized (BaseFragmentManager.class) {
            if (instance == null) {
                instance = new BaseFragmentManager();
            }
            return instance;
        }
    }
    
    private static OnFragmentStatusChangeListener onFragmentStatusChangeListener;
    private static Stack<BaseFragment> fragmentStack;
    
    public void onFragmentCreate(BaseFragment fragment) {
        if (fragmentStack == null) {
            fragmentStack = new Stack<BaseFragment>();
        }
        fragmentStack.add(fragment);
        if (onFragmentStatusChangeListener != null) {
            onFragmentStatusChangeListener.onFragmentCreate(fragment);
        }
    }
    
    public void onFragmentDestroy(BaseFragment fragment) {
        if (fragment != null) {
            if (fragmentStack != null) {
                fragmentStack.remove(fragment);
            }
            if (onFragmentStatusChangeListener != null) {
                onFragmentStatusChangeListener.onFragmentDestroy(fragment);
            }
        }
    }
    
    public void onFragmentShow(BaseFragment fragment) {
        if (fragment != null) {
            if (onFragmentStatusChangeListener != null) {
                onFragmentStatusChangeListener.onFragmentShow(fragment);
            }
        }
    }
    
    public void onFragmentHide(BaseFragment fragment) {
        if (fragment != null) {
            if (onFragmentStatusChangeListener != null) {
                onFragmentStatusChangeListener.onFragmentHide(fragment);
            }
        }
    }
    
    public static <B extends BaseFragment> B getFragment(String instanceKey) {
        for (BaseFragment fragment : fragmentStack) {
            if (Objects.equals(fragment.getInstanceKey(), instanceKey)) {
                return (B) fragment;
            }
        }
        return null;
    }
    
    public static <B extends BaseFragment> B getFragment(Class<?> clazz) {
        for (BaseFragment fragment : fragmentStack) {
            if (fragment.getClass().equals(clazz)) {
                return (B) fragment;
            }
        }
        return null;
    }
    
    public static Stack<BaseFragment> getFragmentStack() {
        return fragmentStack;
    }
    
    public static abstract class OnFragmentStatusChangeListener {
        
        public void onFragmentCreate(BaseFragment activity) {
        }
        
        public void onFragmentDestroy(BaseFragment activity) {
        }
        
        public void onFragmentShow(BaseFragment activity) {
        }
        
        public void onFragmentHide(BaseFragment activity) {
        }
    }
    
    public static OnFragmentStatusChangeListener getOnFragmentStatusChangeListener() {
        return onFragmentStatusChangeListener;
    }
    
    public static void setOnFragmentStatusChangeListener(OnFragmentStatusChangeListener onFragmentStatusChangeListener) {
        BaseFragmentManager.onFragmentStatusChangeListener = onFragmentStatusChangeListener;
    }
}
