package com.docandroid.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;


/**
 * BaseActivity
 */
public abstract class BaseActivity extends AppCompatActivity {

    public Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initView());
        initData();
    }


    public abstract int initView();

    public abstract void initData();


}