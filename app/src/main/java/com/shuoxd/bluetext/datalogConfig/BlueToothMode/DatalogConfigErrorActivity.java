package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.DatalogStep2ModActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.Constant;
import com.shuoxd.bluetext.datalogConfig.adapter.DataLogConfigErrorAdapter;
import com.shuoxd.bluetext.datalogConfig.bean.DataConfigErrorBean;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DatalogConfigErrorActivity extends BaseActivity {


    @BindView(R.id.status_bar_view)
    View statusBarView;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.rv_error)
    RecyclerView rvError;
    @BindView(R.id.btn_next)
    Button btnNext;


    private List<DataConfigErrorBean> list;
    private DataLogConfigErrorAdapter mAdapter;
    private String[] titles;
    private String[] content;
    private int[] picRes;
    private int[] picRes2;


    private String plantId;
    private String userId;
    private String id;//采集器序列号
    private String action;
    private String configType;
    private String wifiTypeString = "";
    private String intType = "";
    private String isHave;
    private String isNewDatalog;
    private String serverId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalog_config_error);
        ButterKnife.bind(this);
        initIntent();
        initToobar(toolbar);

        tvTitle.setText(R.string.m311故障分析);

        //配网类型
        titles = new String[]{getString(R.string.light_red_flash), getString(R.string.light_green_flash), getString(R.string.light_green_steady), getString(R.string.light_blue_flash), getString(R.string.light_blue_steady), getString(R.string.check_frequency), getString(R.string.check_router)};
        content = new String[]{getString(R.string.m11), getString(R.string.checked_server_connet), getString(R.string.checked_router_connet), getString(R.string.datalog_connet_normal), getString(R.string.switch_to_apmode), getString(R.string.请检查路由器当前频段), getString(R.string.请检查配置页面中输入的路由器用户名和密码是否正确)};
        picRes = new int[]{R.drawable.datalog_red, R.drawable.datalog_green, R.drawable.datalog_green, R.drawable.datalog_blue, R.drawable.datalog_blue, R.drawable.datalog_hz, R.drawable.datalog_router};
        picRes2=new int[]{R.drawable.wifi_singal};

        list=new ArrayList<>();

        for (int i = 0; i < titles.length; i++) {
            DataConfigErrorBean bean = new DataConfigErrorBean();
            bean.setTitle(titles[i]);
            bean.setContent(content[i]);
            bean.setRes(picRes[i]);
            if (i<picRes2.length){
                bean.setRes2(picRes2[i]);
            }else {
                bean.setRes2(0);
            }
            list.add(bean);
        }

        rvError.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new DataLogConfigErrorAdapter(R.layout.item_error_analysis, list);
        rvError.setAdapter(mAdapter);

        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.xa20);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayout.VERTICAL);
        rvError.addItemDecoration(dividerItemDecoration);
    }

    private void initIntent() {
        DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        wifiTypeString=configBean.getWifiTypeName();
        intType=configBean.getTypeNumber();
        id = configBean.getSerialNumber();
        plantId = configBean.getPlantId();
        userId = configBean.getUserId();
        action = configBean.getAction();
        configType = configBean.getConfigType();
        isHave = configBean.getIsHave();
        isNewDatalog=configBean.getIsNewDatalog();
        serverId=configBean.getServerId();
    }


    @OnClick({R.id.btn_next,R.id.tv_comment_quetion})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.btn_next:
                retryConfig();
                break;
            case R.id.tv_comment_quetion:

                break;
        }
    }



    private void retryConfig() {
        Intent intent = new Intent(this, DatalogStep2ModActivity.class);
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
        finish();
    }
}
