package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.GlobalConstant;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.databinding.ActivityBleModuleDebugSelectBinding;
import com.shuoxd.bluetext.datalogConfig.DatalogStringChooseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * After BLE connect: choose debug mode.
 * 配网 → {@link BleModuleConfigActivity}
 * 0x17透传 → {@link BleModulePassthroughActivity}
 */
public class BleModuleDebugSelectActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    private static final int OPTION_CONFIG = 0;
    private static final int OPTION_PASSTHROUGH_0X17 = 1;

    private ActivityBleModuleDebugSelectBinding binding;
    private DatalogStringChooseAdapter mAdapter;
    private String deviceType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleModuleDebugSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToobar(binding.headerView.toolbar);
        binding.headerView.tvTitle.setText("调试选择");

        if (getIntent() != null) {
            deviceType = getIntent().getStringExtra(GlobalConstant.DEVICE_TYPE);
            if (deviceType == null) {
                deviceType = "";
            }
        }

        List<String> options = new ArrayList<>(Arrays.asList("配网", "0x17透传"));
        mAdapter = new DatalogStringChooseAdapter(R.layout.item_string_choose, options);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (position == OPTION_CONFIG) {
            toConfig();
        } else if (position == OPTION_PASSTHROUGH_0X17) {
            toPassthrough0x17();
        }
    }

    private void toConfig() {
        Intent intent = new Intent(this, BleModuleConfigActivity.class);
        intent.putExtra(GlobalConstant.DEVICE_TYPE, deviceType);
        startActivity(intent);
    }

    private void toPassthrough0x17() {
        startActivity(new Intent(this, BleModulePassthroughActivity.class));
    }
}
