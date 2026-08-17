package com.shuoxd.bluetext;

import static com.shuoxd.bluetext.datalogConfig.PermissionConstant.RC_LOCATION;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.shuoxd.bluetext.databinding.ActivityBleToolHomeBinding;
import com.shuoxd.bluetext.datalogConfig.BlueToothMode.BleModuleScanActivity;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.Constant;
import com.shuoxd.bluetext.datalogConfig.PermissionConstant;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * App launcher home: jump into the new BLE scan flow.
 */
public class BleToolHomeActivity extends BaseActivity implements View.OnClickListener {

    private ActivityBleToolHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleToolHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Home is not a swipe-back page
        getSwipeBackLayout().setEnableGesture(false);

        binding.tvTitle.setText("采集器调试工具");
        binding.btnScanBluetooth.setText("扫描蓝牙");
        binding.btnScanBluetooth.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_scan_bluetooth) {
            checkBlePermissions();
        }
    }

    @AfterPermissionGranted(RC_LOCATION)
    private void checkBlePermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms = PermissionConstant.BLE_SCAN;
        } else {
            perms = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        if (EasyPermissions.hasPermissions(this, perms)) {
            toBleScan();
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.scan_bluetooth_permission),
                    RC_LOCATION,
                    perms);
        }
    }

    private void toBleScan() {
        DatalogConfigBean bean = ConfigManager.getInstance().getConfigBean();
        if (bean == null) {
            bean = new DatalogConfigBean();
        }
        bean.setConfigMode(String.valueOf(Constant.CONFIG_BLUETOOTH));
        ConfigManager.getInstance().setConfigBean(bean);

        startActivity(new Intent(this, BleModuleScanActivity.class));
    }
}
