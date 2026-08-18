package com.shuoxd.bluetext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.shuoxd.ble.BleClient;
import com.shuoxd.ble.callback.BlePermissionCallback;
import com.shuoxd.bluetext.databinding.ActivityBleToolHomeBinding;
import com.shuoxd.bluetext.datalogConfig.BlueToothMode.BleModuleScanActivity;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.Constant;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;

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
        getSwipeBackLayout().setEnableGesture(false);

        binding.tvTitle.setText("采集器调试工具");
        binding.btnScanBluetooth.setText("扫描蓝牙");
        binding.btnScanBluetooth.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_scan_bluetooth) {
            BleClient.getInstance().requestPermissions(this, new BlePermissionCallback() {
                @Override
                public void onGranted() {
                    toBleScan();
                }

                @Override
                public void onDenied(boolean neverAskAgain) {
                    Toast.makeText(BleToolHomeActivity.this,
                            R.string.scan_bluetooth_permission,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (BleClient.getInstance().handlePermissionsResult(this, requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
