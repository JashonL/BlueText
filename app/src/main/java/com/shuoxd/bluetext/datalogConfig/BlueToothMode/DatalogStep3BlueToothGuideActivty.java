package com.shuoxd.bluetext.datalogConfig.BlueToothMode;


import static com.shuoxd.bluetext.datalogConfig.PermissionConstant.RC_LOCATION;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;


import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.databinding.ActivityDatalogStep3GuideBinding;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.LocalUtil;
import com.shuoxd.bluetext.datalogConfig.PermissionConstant;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class DatalogStep3BlueToothGuideActivty extends BaseActivity implements Toolbar.OnMenuItemClickListener {





    private String[] title;
    private int[] images;


    private ActivityDatalogStep3GuideBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDatalogStep3GuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToobar(binding.headerView.toolbar);


        binding.headerView.tvTitle.setText(R.string.config_datalog);
        binding.titleStep3.tvStepTitle3.setText(R.string.config_network);


        title = new String[]{
                getString(R.string.datalog_bluetooth_step)
        };
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = LocalUtil.checkGPSIsOpen(DatalogStep3BlueToothGuideActivty.this);
                if (b) {
                    try {
                        checkCameraPermissions();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    CircleDialogUtils.showCommentDialog(DatalogStep3BlueToothGuideActivty.this, getString(R.string.温馨提示),
                            getString(R.string.utf_open_gprs), v -> {
                                LocalUtil.goToOpenGPS(DatalogStep3BlueToothGuideActivty.this);
                            }, v -> finish(), false);
                }
            }
        });

    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return true;
    }


    /**
     * 检测拍摄权限
     */
    @AfterPermissionGranted(RC_LOCATION)
    private void checkCameraPermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms = PermissionConstant.BLE_SCAN;
        } else {
            perms = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        }


        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            toScanSerial();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.scan_bluetooth_permission),
                    RC_LOCATION, perms);
        }

    }


    private void toScanSerial() {
        Intent intent = new Intent(this, BleModuleScanActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocalUtil.OPEN_GPS_CODE) {
            checkCameraPermissions();
        }
    }
}
