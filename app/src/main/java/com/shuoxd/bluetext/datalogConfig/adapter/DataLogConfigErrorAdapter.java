package com.shuoxd.bluetext.datalogConfig.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.datalogConfig.bean.DataConfigErrorBean;

import java.util.List;

public class DataLogConfigErrorAdapter extends BaseQuickAdapter<DataConfigErrorBean, BaseViewHolder> {

    public DataLogConfigErrorAdapter(int layoutResId, @Nullable List<DataConfigErrorBean> data) {
        super(layoutResId, data);
    }

    public DataLogConfigErrorAdapter(@Nullable List<DataConfigErrorBean> data) {
        super(data);
    }

    public DataLogConfigErrorAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, DataConfigErrorBean dataConfigErrorBean) {
        if (dataConfigErrorBean.getRes()!=0){
            helper.setImageResource(R.id.iv_pic,dataConfigErrorBean.getRes());
        }

        if (dataConfigErrorBean.getRes2()!=0){
            helper.setImageResource(R.id.iv_pic_2,dataConfigErrorBean.getRes2());

        }

        helper.setText(R.id.tv_title,dataConfigErrorBean.getTitle());
        helper.setText(R.id.tv_content,dataConfigErrorBean.getContent());
    }
}
