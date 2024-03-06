package com.shuoxd.bluetext.datalogConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shuoxd.bluetext.R;

import java.util.List;

public class DatalogStringChooseAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public DatalogStringChooseAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_content,item);
    }
}
