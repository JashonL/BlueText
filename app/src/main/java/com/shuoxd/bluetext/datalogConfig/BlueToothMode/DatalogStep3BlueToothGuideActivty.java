package com.shuoxd.bluetext.datalogConfig.BlueToothMode;


import static com.shuoxd.bluetext.datalogConfig.PermissionConstant.RC_LOCATION;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;


import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.LocalUtil;
import com.youth.banner.Banner;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class DatalogStep3BlueToothGuideActivty extends BaseActivity implements Toolbar.OnMenuItemClickListener {


    @BindView(R.id.status_bar_view)
    View statusBarView;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.tv_step_title3)
    TextView tvStepTitle3;
    @BindView(R.id.banner)
    Banner banner;



    private String[] title;
    private int[] images;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalog_step3_guide);
        ButterKnife.bind(this);
        initToobar(toolbar);


        tvTitle.setText(R.string.config_datalog);
        tvStepTitle3.setText(R.string.config_network);


        title = new String[]{
                getString(R.string.datalog_bluetooth_step)
               };




    }



    @OnClick({R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.btn_next:
                boolean b = LocalUtil.checkGPSIsOpen(this);
                if (b) {
                    try {
                        checkCameraPermissions();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    CircleDialogUtils.showCommentDialog(this, getString(R.string.温馨提示),
                            getString(R.string.utf_open_gprs), v -> {
                                LocalUtil.goToOpenGPS(this);
                    }, v -> finish(), false);
                }


                break;
        }
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
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            toScanSerial();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.scan_bluetooth_permission),
                    RC_LOCATION, perms);
        }
    }


    private void toScanSerial(){
        Intent intent = new Intent(this, BlueToothScanActivity.class);
        startActivity(intent);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==LocalUtil.OPEN_GPS_CODE){
            checkCameraPermissions();
        }
    }
}
