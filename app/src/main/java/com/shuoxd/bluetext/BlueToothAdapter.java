package com.shuoxd.bluetext;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shuoxd.bluetext.datalogConfig.bluetooth.bean.BleBean;
import com.shuoxd.bluetext.datalogConfig.bluetooth.constant.BluetoothConstant;

import java.util.List;

public class BlueToothAdapter extends BaseQuickAdapter<BleBean, BaseViewHolder> {
    public BlueToothAdapter(int layoutResId, @Nullable List<BleBean> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder helper, BleBean item) {
        helper.setText(R.id.tv_bluetooth_name, item.getBleName());
        helper.setImageResource(R.id.iv_dataloger_bluetooth, R.drawable.dataloger_bluetooth);
        TextView tvstatus = helper.getView(R.id.tv_bluetooth_status);
        String status = item.getStatus();
        if (TextUtils.isEmpty(status)) {
            setStatus(tvstatus, BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
        } else {
            setStatus(tvstatus, item.getStatus());
        }
    }


    private void setStatus(TextView tvStatus, String status) {
        switch (status) {
            case BluetoothConstant.BLUETOOTH_CONNET_STATUS_2:
                tvStatus.setText(R.string.connected);
                tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.note_bg_white));
                break;
            case BluetoothConstant.BLUETOOTH_CONNET_STATUS_3:
                tvStatus.setText(R.string.connecting);
                tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.headerView));
                break;

            default:
                tvStatus.setText(R.string.未连接);
                tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.content_bg_white));
                break;
        }


    }


}
