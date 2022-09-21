package com.docandroid.main.fr;


import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;

import com.baseframework.BaseFragment;
import com.baseframework.interfaces.Layout;
import com.baseframework.util.JumpParameter;
import com.docandroid.ConstantParams;
import com.docandroid.Control;
import com.docandroid.R;
import com.docandroid.layout.FrameLayoutActivity;
import com.docandroid.layout.LinearlayoutActivity;
import com.docandroid.layout.RelativeLayoutActivity;
import com.docandroid.main.MainActivity;
import com.docandroid.main.bean.BeAllWidget;
import com.docandroid.util.UReadAssets;
import com.google.gson.reflect.TypeToken;
import com.rq.rvlibrary.BaseAdapter;
import com.rq.rvlibrary.BaseViewHolder;
import com.rq.rvlibrary.RecyclerUtil;

import java.util.List;

/**
 * @author: majin
 * @createTime: 2022/9/19
 * @desc: 布局碎片
 */
//使用 @Layout 注解直接绑定要显示的布局
@SuppressLint("NonConstantResourceId")
@Layout(R.layout.fr_layout)
public class FrLayout extends BaseFragment<MainActivity> {

    private RecyclerView mRecyclerView;
    private List<BeAllWidget> listData;
    private BaseAdapter mAdapter;


    //此处用于绑定布局组件，你也可以使用 @BindView(resId) 来初始化组件
    @Override
    public void initViews() {
        mRecyclerView = findViewById(R.id.mRecyclerView);
    }

    @Override
    public void initDatas() {
        String json = new UReadAssets().readStringFromAssets(me, Control.MIAN_JSON);
        listData = gson.fromJson(json, new TypeToken<List<BeAllWidget>>() {
        }.getType());

        bigLog(json);

        mAdapter = new BaseAdapter(me, R.layout.item_main) {
            @Override
            protected void onBindEasyHolder(BaseAdapter adapter, BaseViewHolder holder, int position, Object o) {
                holder.setItemText(R.id.name, ((BeAllWidget) o).name + "      " + ((BeAllWidget) o).cnName);
            }
        };
        new RecyclerUtil(mAdapter).set2View(mRecyclerView);
        mAdapter.setData(listData);
    }

    //点击事件
    @Override
    public void setEvents() {
        mAdapter.setOnItemClickListener((adapter, holder, o, view, position) -> {
            JumpParameter jumpParameter = new JumpParameter();
            jumpParameter.put(ConstantParams.LAYOUTNAME, ((BeAllWidget) o).cnName);

            switch (((BeAllWidget) o).id) {
                case 1:
                    jump(LinearlayoutActivity.class, jumpParameter);
                    break;
                case 2:
                    jump(RelativeLayoutActivity.class, jumpParameter);
                    break;
                case 3:
                    jump(FrameLayoutActivity.class, jumpParameter);
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                default:
                    break;

            }

        });
    }

}
