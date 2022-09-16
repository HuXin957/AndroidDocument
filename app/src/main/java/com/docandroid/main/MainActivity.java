package com.docandroid.main;


import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.docandroid.Control;
import com.docandroid.R;
import com.docandroid.base.BaseActivity;
import com.docandroid.main.bean.BeAllWidget;
import com.docandroid.util.UReadAssets;
import com.google.gson.reflect.TypeToken;
import com.rq.rvlibrary.BaseAdapter;
import com.rq.rvlibrary.BaseViewHolder;
import com.rq.rvlibrary.RecyclerUtil;

import java.util.List;


/**
 * 主页列表
 */
public class MainActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private List<BeAllWidget> listData;

    @Override
    public int initView() {
        return R.layout.recylerview;
    }

    @Override
    public void initData() {
        String json = new UReadAssets().readStringFromAssets(this, Control.MIAN_JSON);
        listData = gson.fromJson(json, new TypeToken<List<BeAllWidget>>() {
        }.getType());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initWidget();
        this.initRecyclerView();
    }


    private void initWidget() {
        mRecyclerView = findViewById(R.id.mRecyclerView);
    }


    private void initRecyclerView() {

        BaseAdapter mAdapter = new BaseAdapter(this, R.layout.item_main) {
            @Override
            protected void onBindEasyHolder(BaseAdapter adapter, BaseViewHolder holder, int position, Object o) {
                holder.setItemText(R.id.name, ((BeAllWidget) o).name + "      " + ((BeAllWidget) o).cnName);
            }
        };
        new RecyclerUtil(mAdapter).set2View(mRecyclerView);
        mAdapter.setData(listData);
        //点击事件
        mAdapter.setOnItemClickListener((adapter, holder, o, view, position) -> {

        });
    }

}