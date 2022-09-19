package com.baseframework.util;

public interface OnPermissionResponseListener {
    void onSuccess(String[] permissions);
    void onFail();
}
