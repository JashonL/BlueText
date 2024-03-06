package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.params.InputParams;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;
import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.ComenStringAdapter;
import com.shuoxd.bluetext.CommonPopupWindow;
import com.shuoxd.bluetext.DataLogApDataParseUtil;
import com.shuoxd.bluetext.DatalogAPSetParam;
import com.shuoxd.bluetext.DatalogApUtil;
import com.shuoxd.bluetext.DatalogResponBean;
import com.shuoxd.bluetext.DatalogStep2ModActivity;
import com.shuoxd.bluetext.MyApplication;
import com.shuoxd.bluetext.Mydialog;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.SmartHomeUtil;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.MyUtils;
import com.shuoxd.bluetext.datalogConfig.WifiTypeEnum;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleService;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlueToothAdvanceActivity extends BaseActivity implements
        Toolbar.OnMenuItemClickListener,
        CompoundButton.OnCheckedChangeListener,TabLayout.OnTabSelectedListener{


    @BindView(R.id.status_bar_view)
    View statusBarView;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.tab_title)
    TabLayout tabTitle;
    @BindView(R.id.tv_dhcp)
    AppCompatTextView tvDhcp;
    @BindView(R.id.cb_dhcp)
    CheckBox cbDhcp;
    @BindView(R.id.et_ip)
    AppCompatTextView etIp;
    @BindView(R.id.iv_ip_next)
    ImageView ivIpNext;
    @BindView(R.id.et_gateway)
    AppCompatTextView etGateway;
    @BindView(R.id.iv_gateway_next)
    ImageView ivGatewayNext;
    @BindView(R.id.et_mask)
    AppCompatTextView etMask;
    @BindView(R.id.iv_mask_next)
    ImageView ivMaskNext;
    @BindView(R.id.ll_router_setting)
    ConstraintLayout llRouterSetting;
    @BindView(R.id.et_intervals)
    AppCompatTextView etIntervals;
    @BindView(R.id.tv_datalog_time)
    AppCompatTextView etDatalogTime;
    @BindView(R.id.cb_synchronize_time)
    CheckBox cbSynchronizeTime;
    @BindView(R.id.ll_time_setting)
    ConstraintLayout llTimeSetting;

    @BindView(R.id.et_server_domain)
    AppCompatTextView etServerDomain;
    @BindView(R.id.iv_server_domain_next)
    ImageView ivServerDomainNext;


    @BindView(R.id.et_server_port)
    AppCompatTextView etServerPort;
    @BindView(R.id.iv_server_port_next)
    ImageView ivServerPortNext;
    @BindView(R.id.ll_url_setting)
    ConstraintLayout llUrlSetting;
    @BindView(R.id.et_serialnum)
    AppCompatTextView etSerialnum;
    @BindView(R.id.iv_serialnum_next)
    ImageView ivSerialnumNext;
    @BindView(R.id.et_mac)
    AppCompatTextView etMac;
    @BindView(R.id.et_device_type)
    AppCompatTextView etDeviceType;
    @BindView(R.id.et_version)
    AppCompatTextView etVersion;
    @BindView(R.id.ll_base_setting)
    ConstraintLayout llBaseSetting;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.cl_unlock)
    ConstraintLayout clUnlock;
    @BindView(R.id.srl_pull)
    SwipeRefreshLayout srlPull;
    @BindView(R.id.et_datalog_time)
    TextView etDatalogSystemTime;
    @BindView(R.id.tv_server_domain)
    TextView tvServerDomain;



    //连接对象
    private String devId;


    private int index = 0;

    //获取到当前设置内容
    private DatalogResponBean mCurrentSettingBean;
    //路由器设置
    private String netDhcp = "";
    private String localIp = "";
    private String gateway = "";
    private String mask = "";
    //时间设置
    private String dataInterval = "";
    private String systemTime = "";
    //服务器设置
    private String remoteUrl = "";
    private String remotePort = "";
    //基本信息
    private String dataloggerSn = "";
    private String localMac = "";
    private String dataloggerType = "";
    private String version = "";

    //是否开启了设置域名
    private boolean isSetDomain = false;

    //是否开启static
    private boolean dhcpStatus = false;


    //是否已经解锁
    public boolean ISUNLOCK = true;//采集器设置是否已解锁

    private String[] serverUrls;
    private CommonPopupWindow wifiWindow;


    //蓝牙通信的服务
    private BleService mService;
    private boolean isvisible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_dataloger_ap_setting);
        ButterKnife.bind(this);
        initIntent();
        initResource();
        initViews();
        connectSendMsg();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isvisible=true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        isvisible=false;
    }

    @OnClick({R.id.et_ip, R.id.iv_ip_next, R.id.et_gateway, R.id.iv_gateway_next, R.id.et_mask, R.id.iv_mask_next,
            R.id.et_intervals, R.id.iv_intervals_next, R.id.et_datalog_time, R.id.iv_datalog_time_next,
            R.id.et_server_domain, R.id.iv_server_domain_next,     R.id.et_server_port, R.id.iv_server_port_next,
            R.id.et_serialnum, R.id.iv_serialnum_next, R.id.et_mac, R.id.iv_mac_next, R.id.et_device_type, R.id.iv_type_next,
            R.id.cl_unlock, R.id.btn_next, R.id.tv_quite_ap})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cb_synchronize_time:

            case R.id.cb_dhcp:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                break;

            case R.id.et_ip:
            case R.id.iv_ip_next:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!dhcpStatus) return;
                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.LOCAL_IP, etIp, getString(R.string.ip_address));
                break;

            case R.id.iv_gateway_next:
            case R.id.et_gateway:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!dhcpStatus) return;
                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.DEFAULT_GATEWAY, etGateway, getString(R.string.m157网关));
                break;

            case R.id.et_mask:
            case R.id.iv_mask_next:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!dhcpStatus) return;
                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.SUBNET_MASK, etMask, getString(R.string.m158子网掩码));

                break;

            case R.id.iv_intervals_next:
            case R.id.et_intervals:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();

                    return;
                }
                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.DATA_INTERVAL, etIntervals, getString(R.string.intervals));
                break;

            case R.id.iv_datalog_time_next:
            case R.id.et_datalog_time:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                MyUtils.showTotalTime(this, etDatalogTime);
                break;


            case R.id.et_server_domain:
            case R.id.iv_server_domain_next:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }

                //弹框选择
                setServer(etServerDomain);


                break;



            case R.id.iv_serialnum_next:
            case R.id.et_serialnum:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.DATALOGGER_SN, etSerialnum, getString(R.string.序列号));
                break;

            case R.id.iv_mac_next:
            case R.id.et_mac:
//                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.LOCAL_MAC, etMac,getString(R.string.m159网络MAC地址));
                break;

            case R.id.iv_type_next:
            case R.id.et_device_type:
//                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.DATALOGGER_TYPE, etDeviceType,getString(R.string.mdevice_type_ios));
                break;

            case R.id.iv_server_port_next:
            case R.id.et_server_port:
                if (!ISUNLOCK) {
                    Toast.makeText(this,R.string.unlock_setting,Toast.LENGTH_SHORT).show();
                    return;
                }
                DatalogApUtil.showDialog(this, DataLogApDataParseUtil.REMOTE_PORT, etServerPort,getString(R.string.inverterdevicedata_port));
                break;

            case R.id.tv_quite_ap:
                CircleDialogUtils.showCommentDialog(this, getString(R.string.quit_ap_config), getString(R.string.reset_datalog_quit), getString(R.string.all_ok), getString(R.string.all_no), Gravity.CENTER, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //1.发送重置指令
                        try {
                            sendCmdRestart();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //2.断开TCP连接
                        mService.disconnect();
                        Intent intent=new Intent(BlueToothAdvanceActivity.this, DatalogStep2ModActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        jumpTo(intent, true);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }, null);
                break;

            case R.id.btn_next:
                try {
                    requestSetting();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.cl_unlock:
                CircleDialogUtils.showCustomInputDialog(this, getString(R.string.请输入密码), "",
                        "", getString(R.string.input_password_tips), false, Gravity.CENTER, getString(R.string.all_ok), new OnInputClickListener() {

                            @Override
                            public boolean onClick(String text, View v) {
                                if (TextUtils.isEmpty(text)) return true;
                                String datalogPassword = "123456";
                                if (text.equals(datalogPassword)) {
                                    ISUNLOCK = true;
                                    btnNext.setVisibility(View.VISIBLE);
                                    clUnlock.setVisibility(View.GONE);
                                    serverSetVisible();
                                    if (cbDhcp.isChecked()) {
                                        ivIpNext.setVisibility(View.VISIBLE);
                                        ivGatewayNext.setVisibility(View.VISIBLE);
                                        ivMaskNext.setVisibility(View.VISIBLE);
                                    }
                                    cbDhcp.setEnabled(true);
                                    cbSynchronizeTime.setEnabled(true);

                                    ivServerPortNext.setVisibility(View.VISIBLE);
                                    ivSerialnumNext.setVisibility(View.VISIBLE);

                                } else {
                                    Toast.makeText(BlueToothAdvanceActivity.this,R.string.m密码错误,Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        }, getString(R.string.all_no), new ConfigInput() {
                            @Override
                            public void onConfig(InputParams params) {
                                params.padding = new int[]{5, 5, 5, 5};
                                params.strokeColor = ContextCompat.getColor(BlueToothAdvanceActivity.this, R.color.title_bg_white);
                        /*        params.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE;*/
                            }
                        }, null);
                break;
        }
    }


    private void requestSetting() throws Exception {
        switch (index) {
            case 0:
                List<DatalogAPSetParam> routerList = new ArrayList<>();
                String dhcp = cbDhcp.isChecked() ? "1" : "0";
                if (!dhcp.equals(netDhcp)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.NET_DHCP);
                    bean.setLength(dhcp.length());
                    bean.setValue(dhcp);
                    routerList.add(bean);
                }

                String ip = etIp.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    ip = "";
                }
                if (!localIp.equals(ip)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.LOCAL_IP);
                    bean.setLength(ip.length());
                    bean.setValue(ip);
                    routerList.add(bean);
                }


                String gateWay = etGateway.getText().toString();
                if (TextUtils.isEmpty(gateWay)) {
                    gateWay = "";
                }
                if (!gateway.equals(gateWay)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.DEFAULT_GATEWAY);
                    bean.setLength(gateWay.length());
                    bean.setValue(gateWay);
                    routerList.add(bean);
                }


                String cMask = etMask.getText().toString();
                if (TextUtils.isEmpty(cMask)) {
                    cMask = "";
                }

                if (!mask.equals(cMask)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.SUBNET_MASK);
                    bean.setLength(cMask.length());
                    bean.setValue(cMask);
                    routerList.add(bean);
                }

                if (routerList.size() == 0) {
                    Toast.makeText(BlueToothAdvanceActivity.this,R.string.设置未做任何更改,Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, routerList);
                Mydialog.Show(this);
                mService.writeCharacteristic(bytes);
                break;

            case 1:
                List<DatalogAPSetParam> serverList = new ArrayList<>();

                String serverDomain = etServerDomain.getText().toString();
                if (TextUtils.isEmpty(serverDomain)) {
                    serverDomain = "";
                }
                if (!remoteUrl.equals(serverDomain)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.REMOTE_IP);
                    bean.setLength(serverDomain.length());
                    bean.setValue(serverDomain);
                    serverList.add(bean);
                }




                String serverPort = etServerPort.getText().toString();

                if (TextUtils.isEmpty(serverPort)) {
                    serverPort = "";
                }
                if (!remotePort.equals(serverPort)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.REMOTE_PORT);
                    bean.setLength(serverPort.length());
                    bean.setValue(serverPort);
                    serverList.add(bean);
                }


                if (serverList.size() == 0) {
                    Toast.makeText(BlueToothAdvanceActivity.this,R.string.设置未做任何更改,Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] bytes2 = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, serverList);
                Mydialog.Show(this);
                mService.writeCharacteristic(bytes2);
                break;
            case 2:
                List<DatalogAPSetParam> baseList = new ArrayList<>();
                String serialnum = etSerialnum.getText().toString();
                if (TextUtils.isEmpty(serialnum)) {
                    serialnum = "";
                }
                if (!dataloggerSn.equals(serialnum)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.DATALOGGER_SN);
                    bean.setLength(serialnum.length());
                    bean.setValue(serialnum);
                    baseList.add(bean);
                }


                String mac = etMac.getText().toString();

                if (TextUtils.isEmpty(mac)) {
                    mac = "";
                }
                if (!localMac.equals(mac)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.LOCAL_MAC);
                    bean.setLength(mac.length());
                    bean.setValue(mac);
                    baseList.add(bean);
                }


                String type = etDeviceType.getText().toString();
                if (TextUtils.isEmpty(type)) {
                    type = "";
                }
                if (!dataloggerType.equals(type)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.DATALOGGER_TYPE);
                    bean.setLength(type.length());
                    bean.setValue(type);
                    baseList.add(bean);
                }


                if (baseList.size() == 0) {
                    Toast.makeText(BlueToothAdvanceActivity.this,R.string.设置未做任何更改,Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] bytes3 = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, baseList);
                Mydialog.Show(this);
                mService.writeCharacteristic(bytes3);
                break;


            case 3:

                List<DatalogAPSetParam> timeList = new ArrayList<>();


                String intervals = etIntervals.getText().toString();
                if (TextUtils.isEmpty(intervals)) {
                    intervals = "";
                }
                if (!dataInterval.equals(intervals)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.DATA_INTERVAL);
                    bean.setLength(intervals.length());
                    bean.setValue(intervals);
                    timeList.add(bean);
                }

                String dataTime = etDatalogTime.getText().toString();

                if (TextUtils.isEmpty(dataTime)) {
                    dataTime = "";
                }
                if (!systemTime.equals(dataTime)) {
                    DatalogAPSetParam bean = new DatalogAPSetParam();
                    bean.setParamnum(DataLogApDataParseUtil.SYSTEM_TIME);
                    bean.setLength(dataTime.length());
                    bean.setValue(dataTime);
                    timeList.add(bean);
                }


                if (timeList.size() == 0) {
                    Toast.makeText(BlueToothAdvanceActivity.this,R.string.设置未做任何更改,Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] bytes1 = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, timeList);
                Mydialog.Show(this);
                mService.writeCharacteristic(bytes1);
                break;
        }
    }



    /*制冷弹框*/
    private void setServer(View dropView) {
        if (wifiWindow == null) {
            wifiWindow = new CommonPopupWindow(this, R.layout.popuwindow_comment_list_layout, dropView.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT) {
                @Override
                protected void initView() {
                    List<String> ssids = Arrays.asList(serverUrls);
                    View view = getContentView();
                    RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
                    recyclerView.setLayoutManager(new LinearLayoutManager(BlueToothAdvanceActivity.this, LinearLayoutManager.VERTICAL, false));
                    ComenStringAdapter adapter = new ComenStringAdapter(R.layout.item_text, ssids);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener((adapter1, view1, position) -> {
                        if (position == 0) {
                            DatalogApUtil.showDialog(BlueToothAdvanceActivity.this, DataLogApDataParseUtil.REMOTE_IP, etServerDomain, getString(R.string.server_url));
                        } else {
                            etServerDomain.setText(ssids.get(position));
                        }
                        wifiWindow.getPopupWindow().dismiss();
                    });
                }

                @Override
                protected void initEvent() {
                }
            };
        }
        int[] location = new int[2];
        dropView.getLocationOnScreen(location);
        wifiWindow.showAsDropDown(dropView, 0, 0);
    }



    private void initIntent() {
        devId = getIntent().getStringExtra("devId");
    }


    private void initResource() {
        serverUrls = new String[]{getString(R.string.m499手动输入), "redx.shuoxd.com"};
    }



    private void initViews() {
        //头部初始化
        tvTitle.setText(R.string.高级设置);
        toolbar.setNavigationIcon(R.drawable.icon_return);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cbDhcp.setOnCheckedChangeListener(this);

        cbSynchronizeTime.setOnCheckedChangeListener(this);

        if (!ISUNLOCK) {
            cbDhcp.setEnabled(false);
            cbSynchronizeTime.setEnabled(false);
        }

        //选项卡
        String[] tabTitles = new String[]{getString(R.string.router_settings), getString(R.string.server_setting), getString(R.string.基本信息), getString(R.string.time_settings)};
        //tablayout初始化
        for (String title : tabTitles) {
            TabLayout.Tab tab = tabTitle.newTab();
            tab.setText(title);
            tabTitle.addTab(tab);
        }

        tabTitle.addOnTabSelectedListener(this);

        //下拉刷新
        srlPull.setColorSchemeColors(ContextCompat.getColor(this, R.color.charging_text_green));
        srlPull.setOnRefreshListener(this::connectServer);


        btnNext.setVisibility(View.VISIBLE);
        clUnlock.setVisibility(View.GONE);
        String ip = getString(R.string.服务器地址);
        tvServerDomain.setText(ip);

    }




    /*建立TCP连接*/
    private void connectSendMsg() {
        Mydialog.Show(this);
        connectServer();
    }


    /**
     * 连接
     */

    private void connectServer() {
        mService = MyApplication.getInstance().getgBleServer();
        //直接读取数据
        sendCmdConnect();

    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == cbSynchronizeTime) {
            if (isChecked) {
                String date = MyUtils.getFormatDate(null, null);
                etDatalogTime.setText(date);
            } else {
                if (!TextUtils.isEmpty(systemTime)) {
                    etDatalogTime.setText(systemTime);
                }
            }
        }   else if (buttonView == cbDhcp) {
            dhcpStatus = isChecked;
            routerVisible();
        }
    }



    private void serverSetVisible() {
        ivServerDomainNext.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(remoteUrl)){
            etServerDomain.setText(remoteUrl);
        }
    }



    private void routerVisible() {
        if (dhcpStatus) {
            ivIpNext.setVisibility(View.VISIBLE);
            ivGatewayNext.setVisibility(View.VISIBLE);
            ivMaskNext.setVisibility(View.VISIBLE);

        } else {
            ivIpNext.setVisibility(View.GONE);
            ivGatewayNext.setVisibility(View.GONE);
            ivMaskNext.setVisibility(View.GONE);

        }
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        index = tab.getPosition();
        switch (index) {
            case 0:
                llRouterSetting.setVisibility(View.VISIBLE);
                llTimeSetting.setVisibility(View.GONE);
                llUrlSetting.setVisibility(View.GONE);
                llBaseSetting.setVisibility(View.GONE);
                if (ISUNLOCK) {//已解锁
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                }
                sendCmdConnect();
                break;

            case 1:
                llRouterSetting.setVisibility(View.GONE);
                llTimeSetting.setVisibility(View.GONE);
                llUrlSetting.setVisibility(View.VISIBLE);
                llBaseSetting.setVisibility(View.GONE);
                if (ISUNLOCK) {//已解锁
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                    ivServerPortNext.setVisibility(View.GONE);
                    serverSetVisible();
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                    ivServerDomainNext.setVisibility(View.GONE);
                    ivServerPortNext.setVisibility(View.GONE);
                }
                sendCmdConnect();
                break;
            case 2:
                llRouterSetting.setVisibility(View.GONE);
                llTimeSetting.setVisibility(View.GONE);
                llUrlSetting.setVisibility(View.GONE);
                llBaseSetting.setVisibility(View.VISIBLE);
                if (ISUNLOCK) {//已解锁
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                    ivSerialnumNext.setVisibility(View.VISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                    ivSerialnumNext.setVisibility(View.GONE);
                }
                sendCmdConnect();
                break;

            case 3:
                llRouterSetting.setVisibility(View.GONE);
                llTimeSetting.setVisibility(View.VISIBLE);
                llUrlSetting.setVisibility(View.GONE);
                llBaseSetting.setVisibility(View.GONE);
                if (ISUNLOCK) {//已解锁
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    clUnlock.setVisibility(View.GONE);
                }
                sendCmdConnect();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }




    /*发送重启命令*/
    private void sendCmdRestart() throws Exception {
        List<DatalogAPSetParam> restartList = new ArrayList<>();
        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.DATALOGGER_RESTART);
        bean.setLength("1".length());
        bean.setValue("1");
        restartList.add(bean);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, restartList);
        mService.writeCharacteristic(bytes);
    }





    private void sendCmdConnect() {
        int[] paramByte = new int[0];
        switch (index) {
            case 0:
                //1.dhcp 2.ip地址3.网关设置4.子网掩码
                paramByte = new int[]{DataLogApDataParseUtil.NET_DHCP, DataLogApDataParseUtil.LOCAL_IP, DataLogApDataParseUtil.DEFAULT_GATEWAY, DataLogApDataParseUtil.SUBNET_MASK};
                break;

            case 1:
                //1.服务器域名设置2.服务器IP3.服务器端口
                paramByte = new int[]{DataLogApDataParseUtil.REMOTE_IP, DataLogApDataParseUtil.REMOTE_PORT};
                break;
            case 2:
                //1.采集器序列号 2.MAC 3.设备类型 4.软件版本号
                paramByte = new int[]{DataLogApDataParseUtil.DATALOGGER_SN, DataLogApDataParseUtil.LOCAL_MAC, DataLogApDataParseUtil.DATALOGGER_TYPE, DataLogApDataParseUtil.FIRMWARE_VERSION};
                break;

            case 3:
                //1.间隔时间 2.采集器时间
                paramByte = new int[]{DataLogApDataParseUtil.DATA_INTERVAL, DataLogApDataParseUtil.SYSTEM_TIME};
                break;

        }
        byte[] bytes = new byte[0];
        try {
            bytes = DatalogApUtil.sendMsg_blue19(DatalogApUtil.DATALOG_GETDATA_0X19, devId, paramByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Mydialog.Show(this);
        mService.writeCharacteristic(bytes);
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull BleEvent bean) {
        if (isvisible){
            Mydialog.Dismiss();
            //00 01 00 07 00 0F 01 18 103B293E39462709425B775774F87C
            byte[] datavalues = bean.getDatavalues();
            try {

                    //接收正确，开始解析
                    byte type = datavalues[7];

                    //1.去除头部包头
                    byte[] removePro = DataLogApDataParseUtil.removePro(datavalues);
         /*           //2.解密
                    byte[] bytes = DatalogApUtil.desCode(removePro);
                    LogUtil.d("解密" + SmartHomeUtil.bytesToHexString(bytes));*/
                    //3.解析数据
                    parserData(type, removePro);
            } catch (Exception e) {
                Toast.makeText(this,R.string.返回异常,Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


    }



    /**
     * 解析数据
     *
     * @param bytes
     */
    private void parserData(byte type, byte[] bytes) {
        try {
            //1.字节数组解析成bean
            mCurrentSettingBean = DataLogApDataParseUtil.paserData(type, bytes);
            //2.通过回应的功能码解析数据
            if (mCurrentSettingBean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X18) {
                int statusCode = mCurrentSettingBean.getStatusCode();
                if (statusCode == 0) {
                    Toast.makeText(this,R.string.m设置成功,Toast.LENGTH_SHORT).show();
                    //设置成功后重新获取数据
                    sendCmdConnect();
                } else {
                    Toast.makeText(this,R.string.m设置成功,Toast.LENGTH_SHORT).show();
                }
            } else if (mCurrentSettingBean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X19) {
                int statusCode = mCurrentSettingBean.getStatusCode();
                if (statusCode == 1) {
                    Toast.makeText(this,R.string.all_failed,Toast.LENGTH_SHORT).show();
                }
                String curUrl="";
                List<DatalogResponBean.ParamBean> paramBeanList = mCurrentSettingBean.getParamBeanList();
                for (int i = 0; i < paramBeanList.size(); i++) {
                    DatalogResponBean.ParamBean paramBean = paramBeanList.get(i);
                    int num = paramBean.getNum();
                    String value = paramBean.getValue();
                    if (TextUtils.isEmpty(value)) continue;
                    switch (num) {
                        case DataLogApDataParseUtil.NET_DHCP:
                            netDhcp = value;
                            cbDhcp.setChecked("1".equals(value));


                            if (ISUNLOCK && cbDhcp.isChecked()) {
                                ivIpNext.setVisibility(View.VISIBLE);
                                ivGatewayNext.setVisibility(View.VISIBLE);
                                ivMaskNext.setVisibility(View.VISIBLE);
                            } else {
                                ivIpNext.setVisibility(View.GONE);
                                ivGatewayNext.setVisibility(View.GONE);
                                ivMaskNext.setVisibility(View.GONE);
                            }

                            break;
                        case DataLogApDataParseUtil.LOCAL_IP:
                            etIp.setText(value);
                            localIp = value;
                            break;
                        case DataLogApDataParseUtil.DEFAULT_GATEWAY:
                            etGateway.setText(value);
                            gateway = value;
                            break;
                        case DataLogApDataParseUtil.SUBNET_MASK:
                            etMask.setText(value);
                            mask = value;
                            break;
                        case DataLogApDataParseUtil.DATA_INTERVAL:
                            etIntervals.setText(value);
                            dataInterval = value;
                            break;
                        case DataLogApDataParseUtil.SYSTEM_TIME:
                            etDatalogSystemTime.setText(value);
                            systemTime = value;
                            break;

                        case DataLogApDataParseUtil.REMOTE_IP:
                            etServerDomain.setText(value);
                            remoteUrl = value;
                            curUrl=value;
                            break;
                        case DataLogApDataParseUtil.REMOTE_PORT:
                            etServerPort.setText(value);
                            remotePort = value;
                            break;
                        case DataLogApDataParseUtil.DATALOGGER_SN:
                            etSerialnum.setText(value);
                            dataloggerSn = value;
                            break;
                        case DataLogApDataParseUtil.LOCAL_MAC:
                            etMac.setText(value);
                            localMac = value;
                            break;
                        case DataLogApDataParseUtil.DATALOGGER_TYPE:
                            String shineWifiX = WifiTypeEnum.SHINE_WIFI_X + "";
                            String shineWifiS = WifiTypeEnum.SHINE_WIFI_S + "";

                            if (value.equals(shineWifiX)) {
                                etDeviceType.setText(WifiTypeEnum.WIFI_X_NAME);
                            } else if (value.equals(shineWifiS)) {
                                etDeviceType.setText(WifiTypeEnum.WIFI_S_NAME);
                            }
                            dataloggerType = value;
                            break;
                        case DataLogApDataParseUtil.FIRMWARE_VERSION:
                            etVersion.setText(value);
                            version = value;
                            break;
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        mService.disconnect();
    }
}
