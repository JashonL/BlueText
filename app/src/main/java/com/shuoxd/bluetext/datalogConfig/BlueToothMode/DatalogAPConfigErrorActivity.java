package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;


import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.DatalogStep2ModActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.datalogConfig.Constant;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogErrorLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DatalogAPConfigErrorActivity extends BaseActivity {


    @BindView(R.id.status_bar_view)
    View statusBarView;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.view_selected_background)
    View viewSelectedBackground;
    @BindView(R.id.tv_selected)
    TextView tvSelected;
    @BindView(R.id.tv_step_title4)
    TextView tvStepTitle4;
    @BindView(R.id.tv_tips)
    TextView tvTips;
    @BindView(R.id.btn_next)
    Button btnNext;


    private String plantId;
    private String userId;
    private String id;//采集器序列号
    private String action;
    //根据配网类型判断是否需要添加
    private String configType;


    private String wifiTypeString = "";
    private String intType = "";
    private String isHave;
    private String isNewDatalog;
    private String serverId;
    private String errorCode;
    private String errorNameCn;
    private String errorNameEn;


    private int configMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_error);
        ButterKnife.bind(this);
        initIntent();


        tvTitle.setText(R.string.config_datalog);
        tvStepTitle4.setText(R.string.config_finish);
        initToobar(toolbar);


        //配网类型
        String tips;
        if (configMode == Constant.CONFIG_STANDART) {
            tips = "1." + getString(R.string.check_router_name) + "\n"
                    + "2." + getString(R.string.check_router_frequency) + "\n"
                    + "3." + getString(R.string.restart_datalog);
            btnNext.setVisibility(View.VISIBLE);
        } else if (configMode == Constant.CONFIG_BLUETOOTH) {
            tips = "1." + getString(R.string.请检查wifi密码是否正确) + "\n"
                    + "2." + getString(R.string.ap_config_error_1) ;

        } else {
            tips = "1." + getString(R.string.ap_config_error_1) + "\n"
                    + "2." + getString(R.string.ap_config_error_2) + "\n"
                    + "3." + getString(R.string.ap_config_error_3);

            btnNext.setVisibility(View.GONE);
        }

        tvTips.setText(tips);

    }





    private void initIntent() {
 /*       DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        wifiTypeString = configBean.getWifiTypeName();
        intType = configBean.getTypeNumber();
        id = configBean.getSerialNumber();
        plantId = configBean.getPlantId();
        userId = configBean.getUserId();
        action = configBean.getAction();
        configType = configBean.getConfigType();
        isHave = configBean.getIsHave();
        isNewDatalog = configBean.getIsNewDatalog();
        serverId = configBean.getServerId();
        configMode = Integer.parseInt(configBean.getConfigMode());


        errorCode = getIntent().getStringExtra("errorCode");
        errorNameCn = getIntent().getStringExtra("errorNameCn");
        errorNameEn = getIntent().getStringExtra("errorNameEn");*/
    }


    @OnClick({R.id.tv_error_analysis, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_error_analysis:
                modeErrorAnaly();
                break;
            case R.id.btn_next:
                retryConfig();
                break;
        }
    }


    private void modeErrorAnaly() {
        Intent intent = new Intent(this, DatalogConfigErrorActivity.class);
        intent.putExtra("wifiType", intType);
        intent.putExtra(Constant.DATALOGER_SRIAL_NUM, id);
        intent.putExtra("plantId", plantId);
        intent.putExtra("userId", userId);
        intent.putExtra("isHave", isHave);
        intent.putExtra(Constant.DATALOG_ISNEW_DATALOG, isNewDatalog);
        intent.putExtra(Constant.DATALOGER_CONFIG_ACTION, action);
        intent.putExtra(Constant.DATALOG_CONFIG_TYPE, configType);
        intent.putExtra("serverId", serverId);
        startActivity(intent);
    }


    private void retryConfig() {
        Intent intent = new Intent(this, DatalogStep2ModActivity.class);
        startActivity(intent);
        finish();
    }

}
