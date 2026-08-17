package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;



import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.DatalogStep2ModActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.databinding.ActivityApErrorBinding;
import com.shuoxd.bluetext.datalogConfig.Constant;


public class DatalogAPConfigErrorActivity extends BaseActivity implements View.OnClickListener {

    private String intType = "";

    private ActivityApErrorBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityApErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initIntent();
        initLiseners();

        binding.headerView.tvTitle.setText(R.string.config_datalog);
        binding.titleStep4.tvStepTitle4.setText(R.string.config_finish);
        initToobar( binding.headerView.toolbar);


        String  tips = "1." + getString(R.string.请检查wifi密码是否正确) + "\n"
                + "2." + getString(R.string.ap_config_error_1) ;

        binding. btnNext.setVisibility(View.GONE);

        binding.tvTips.setText(tips);

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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_error_analysis:
                modeErrorAnaly();
                break;
            case R.id.btn_next:
                retryConfig();
                break;
        }
    }

    private void initLiseners() {
        binding.tvErrorAnalysis.setOnClickListener(this);
        binding.btnNext.setOnClickListener(this);
    }



    private void modeErrorAnaly() {
        Intent intent = new Intent(this, DatalogConfigErrorActivity.class);
        intent.putExtra("wifiType", intType);

        startActivity(intent);
    }


    private void retryConfig() {
        Intent intent = new Intent(this, DatalogStep2ModActivity.class);
        startActivity(intent);
        finish();
    }


}
