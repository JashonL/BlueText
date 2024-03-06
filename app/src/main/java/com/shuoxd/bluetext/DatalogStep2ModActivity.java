package com.shuoxd.bluetext;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.shuoxd.bluetext.datalogConfig.BlueToothMode.DatalogStep3BlueToothGuideActivty;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.Constant;
import com.shuoxd.bluetext.datalogConfig.DatalogStringChooseAdapter;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DatalogStep2ModActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener, Toolbar.OnMenuItemClickListener {

    @BindView(R.id.status_bar_view)
    View statusBarView;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.tv_step_one)
    TextView tvStepOne;
    @BindView(R.id.view_step_one)
    View viewStepOne;
    @BindView(R.id.view_selected_background)
    View viewSelectedBackground;
    @BindView(R.id.tv_selected)
    TextView tvSelected;
    @BindView(R.id.tv_step_title)
    TextView tvStepTitle;
    @BindView(R.id.view_dash_1)
    View viewDash1;
    @BindView(R.id.tv_unselected)
    TextView tvUnselected;
    @BindView(R.id.view_dash_2)
    View viewDash2;
    @BindView(R.id.ll_guide)
    ConstraintLayout llGuide;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    private DatalogStringChooseAdapter mAdapter;
    private String[] titles;


    private String isNewDatalog;
    private String plantId;
    private String userId;
    private String isHave;
    private String configType;
    private String action;
    private String serverId;
    private String datalogSn;

    private String wifiName;
    private String wifiType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalog_step2_mode);
        ButterKnife.bind(this);
        initToobar(toolbar);



        tvStepTitle.setText(R.string.choose_config_mode);
        tvTitle.setText(R.string.config_datalog);

        isNewDatalog = getIntent().getStringExtra(Constant.DATALOG_ISNEW_DATALOG);
        plantId = getIntent().getStringExtra("plantId");
        userId = getIntent().getStringExtra("userId");
        isHave = getIntent().getStringExtra("isHave");
        configType = getIntent().getStringExtra(Constant.DATALOG_CONFIG_TYPE);
        action = getIntent().getStringExtra(Constant.DATALOGER_CONFIG_ACTION);
        serverId = getIntent().getStringExtra("serverId");
        datalogSn = getIntent().getStringExtra(Constant.DATALOGER_SRIAL_NUM);
        wifiName = getIntent().getStringExtra("wifiName");
        wifiType = getIntent().getStringExtra("wifiType");

        DatalogConfigBean bean=new DatalogConfigBean();
        bean.setSerialNumber(datalogSn);
        bean.setWifiTypeName(wifiName);
        bean.setTypeNumber(wifiType);
        bean.setPlantId(plantId);
        bean.setUserId(userId);
        bean.setIsHave(isHave);
        bean.setServerId(serverId);
        bean.setConfigType(configType);
        bean.setAction(action);
        bean.setIsNewDatalog(isNewDatalog);
        ConfigManager.getInstance().setConfigBean(bean);



        titles = new String[]{
                getString(R.string.bluetooth_mode)
        };


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> mList = new ArrayList<>();
        mAdapter = new DatalogStringChooseAdapter(R.layout.item_string_choose, mList);
        recyclerView.setAdapter(mAdapter);
        initData();
        mAdapter.setOnItemClickListener(this);

    }



    private void initData() {
        List<String> newList = new ArrayList<>(Arrays.asList(titles));
        mAdapter.replaceData(newList);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        toConfigGuide();
    }



    private void toConfigGuide() {
        Intent intent = new Intent(DatalogStep2ModActivity.this, DatalogStep3BlueToothGuideActivty.class);
        DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        configBean.setConfigMode(String.valueOf(Constant.CONFIG_BLUETOOTH));
        jumpTo(intent, false);


    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return true;
    }
}
