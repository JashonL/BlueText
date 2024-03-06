package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.mylhyl.circledialog.view.listener.OnCreateBodyViewListener;
import com.shuoxd.bluetext.AccessPoint;
import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.ComenStringAdapter;
import com.shuoxd.bluetext.CommonPopupWindow;
import com.shuoxd.bluetext.ConfigConstant;
import com.shuoxd.bluetext.DataLogApDataParseUtil;
import com.shuoxd.bluetext.DatalogAPSetParam;
import com.shuoxd.bluetext.DatalogApUtil;
import com.shuoxd.bluetext.DatalogConfigfinish;
import com.shuoxd.bluetext.DatalogResponBean;
import com.shuoxd.bluetext.GlobalConstant;
import com.shuoxd.bluetext.MyApplication;
import com.shuoxd.bluetext.Mydialog;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.SmartHomeUtil;
import com.shuoxd.bluetext.WifiUtils;
import com.shuoxd.bluetext.datalogConfig.BlueToothMode.BlueToothAdvanceActivity;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.MyUtils;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleService;
import com.shuoxd.bluetext.datalogConfig.bluetooth.constant.BluetoothConstant;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleNoRespon;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlueToothConfigActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

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
    @BindView(R.id.tv_step_title3)
    TextView tvStepTitle3;
    @BindView(R.id.tv_unselected)
    TextView tvUnselected;
    @BindView(R.id.et_ssid)
    EditText etSsid;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.iv_switch_password)
    ImageView ivSwitchPassword;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.tv_current_wifi)
    TextView tvCurrentWifi;
    @BindView(R.id.view_ssid_background)
    View view_ssid_background;


    //获取wifi列表
    private List<AccessPoint> lastAccessPoints = new ArrayList<>();
    private CommonPopupWindow wifiWindow;


    //蓝牙通信的服务
    private BleService mService;
    //当前设备是否已绑定
    private String deviceType;
    //设备id
    private String devId;
    //wifi密码可见FLAG
    private boolean passwordOn = false;


    private String errorCode;
    private String errorNameCn;
    private String errorNameEn;


    //
    private DialogFragment dialogFragment;


    //配网倒计时
    private Timer mTimer;
    private TimerTask timerTask;

    private int progress = 0;
    private int max = 100;
    private boolean isView;
    private boolean isvisible = true;
//    private String action;



    private int step=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalog_blue);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initToobar(toolbar);
        deviceType = getIntent().getStringExtra(GlobalConstant.DEVICE_TYPE);


        //获取设备Id
        DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        if (configBean==null){
            finish();
            return;
        }
        devId = "0000000000";
//        action = configBean.getAction();

//        toolbar.inflateMenu(R.menu.menu_quetion_right);
//        toolbar.setOnMenuItemClickListener(this);

        tvTitle.setText(R.string.config_datalog);
        tvStepTitle3.setText(R.string.config_network);

        connectSendMsg();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isView = true;
    }

    @OnClick({R.id.tv_setwifi_guide, R.id.iv_switch_wifi, R.id.iv_switch_password, R.id.btn_next, R.id.btn_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_setwifi_guide:
                Intent intent = new Intent(this, BlueToothAdvanceActivity.class);
                intent.putExtra("devId", devId);
                jumpTo(intent, false);
                break;
            case R.id.iv_switch_wifi:
                setSsid(view_ssid_background);
                break;
            case R.id.iv_switch_password:
                clickPasswordSwitch();
                break;
            case R.id.btn_next:
                try {
                    requestSetting();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_add:

                break;
        }
    }


    public void clickPasswordSwitch() {
        passwordOn = !passwordOn;
        if (passwordOn) {
            ivSwitchPassword.setImageResource(R.drawable.icon_signin_see);
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            ivSwitchPassword.setImageResource(R.drawable.icon_signin_see);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (etPassword.getText().length() > 0) {
            etPassword.setSelection(etPassword.getText().length());
        }
    }


    /*制冷弹框*/
    private void setSsid(View dropView) {
        lastAccessPoints = WifiUtils.getInstance(this).updateAccessPoints();
        if (wifiWindow == null) {
            wifiWindow = new CommonPopupWindow(this, R.layout.popuwindow_comment_list_layout, dropView.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT) {
                @Override
                protected void initView() {
                    List<String> ssids = new ArrayList<>();
                    for (AccessPoint point : lastAccessPoints) {
                        ssids.add(point.ssid);
                    }
                    View view = getContentView();
                    RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
                    recyclerView.setLayoutManager(new LinearLayoutManager(BlueToothConfigActivity.this, LinearLayoutManager.VERTICAL, false));
                    ComenStringAdapter adapter = new ComenStringAdapter(R.layout.item_text, ssids);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener((adapter1, view1, position) -> {
                        String item = adapter.getItem(position);
                        etSsid.setText(item);
                        etPassword.setText("");
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull BleEvent bean) {

        if (isView) {
            Mydialog.Dismiss();
            //00 01 00 07 00 0F 01 18 103B293E39462709425B775774F87C
            byte[] datavalues = bean.getDatavalues();
            try {

                    //接收正确，开始解析
                    byte type = datavalues[7];

                    //1.去除头部包头
                    byte[] removePro = DataLogApDataParseUtil.removePro(datavalues);
             /*       //2.解密
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventNores(@NonNull BleNoRespon bean) {
        //00 01 00 07 00 0F 01 18 103B293E39462709425B775774F87C
        byte[] datavalues = bean.getDatavalues();
        //弹框提示无响应
        if (isView) {
            CircleDialogUtils.showCommentDialog(this, getString(R.string.温馨提示),
                    getString(R.string.m477等待设备响应超时), getString(R.string.retry),
                    getString(R.string.mCancel_ios), Gravity.CENTER, v -> {
                        mService.writeCharacteristic(datavalues);
                    }, null, null);
        }

    }


/*********************发送命令设置数据***************************/


    /**
     * 获取service读取数据
     */
    private void connectSendMsg() {
        mService = MyApplication.getInstance().getgBleServer();
        if (TextUtils.isEmpty(deviceType)) {
            finish();
        }


        try {
//            sendCmdConnect();
            sendCmdGetWifi();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /*发送密钥连接蓝牙*/
    private void sendRestart() throws Exception {
        step=1;
        String value = "1";
        List<DatalogAPSetParam> restartList = new ArrayList<>();
        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.DATALOGGER_RESTART);
        bean.setLength(value.length());
        bean.setValue(value);
        restartList.add(bean);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, restartList);
        mService.writeCharacteristic(bytes);
    }


    /*发送密钥连接蓝牙*/
    private void sendCmdConnect() throws Exception {
        step=0;
        String bluetoothCommentKey = BluetoothConstant.BLUETOOTH_OSS_KEY;
   /*     //蓝牙公共密钥
        if (action.equals(Constant.DATALOGER_CONFIG_FROM_OSS)) {
            bluetoothCommentKey=BluetoothConstant.BLUETOOTH_OSS_KEY;
        }else {
            if (deviceType.contains("G")&&!TextUtils.isEmpty(bluetoothCommentKey)) {//已绑定
                bluetoothCommentKey = BluetoothConstant.BLUETOOTH_SHARE_KEY;
            } else {//已经绑定  发送指定密钥
                bluetoothCommentKey= Cons.userBean.getCpowerToken();
            }
        }*/

        List<DatalogAPSetParam> restartList = new ArrayList<>();
        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.BLUETOOTH_KEY);
        bean.setLength(bluetoothCommentKey.length());
        bean.setValue(bluetoothCommentKey);
        restartList.add(bean);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, restartList);
        mService.writeCharacteristic(bytes);
    }








    /*发送密钥连接蓝牙*/
    private void setKey() throws Exception {
        step=0;
        String bluetoothCommentKey = BluetoothConstant.BLUETOOTH_OSS_KEY;
        List<DatalogAPSetParam> restartList = new ArrayList<>();
        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.BLUETOOTH_KEY);
        bean.setLength(bluetoothCommentKey.length());
        bean.setValue(bluetoothCommentKey);
        restartList.add(bean);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, restartList);
        mService.writeCharacteristic(bytes);
    }






    /*发送获取命令，获取当前设置的wifi*/
    private void sendCmdGetWifi() {
        byte[] bytes = new byte[0];
        try {
            bytes = DatalogApUtil.sendMsg_blue19(DatalogApUtil.DATALOG_GETDATA_0X19, devId,
                    new int[]{DataLogApDataParseUtil.WIFI_SSID, DataLogApDataParseUtil.WIFI_PASSWORD});
        } catch (Exception e) {
            e.printStackTrace();
        }
        Mydialog.Show(this);
        mService.writeCharacteristic(bytes);
    }


    private void requestSetting() throws Exception {
        step=3;
        //请求重置采集器
        List<DatalogAPSetParam> routerList = new ArrayList<>();
        String ssid = etSsid.getText().toString();
        if (TextUtils.isEmpty(ssid)) {
            Toast.makeText(this,R.string.不能为空,Toast.LENGTH_SHORT).show();
            return;
        }

        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,R.string.不能为空,Toast.LENGTH_SHORT).show();
            return;
        }

        boolean letterDigit_ssid = SmartHomeUtil.specificSymbol(ssid);
        boolean letterDigit_sspassord = SmartHomeUtil.specificSymbol(password);
        if (letterDigit_ssid || letterDigit_sspassord) {
            CircleDialogUtils.showCommentDialog(this, getString(R.string.温馨提示),
                    getString(R.string.m连接的路由器名称和密码不能包含特殊字符), getString(R.string.all_ok),
                    "", Gravity.CENTER, v -> {

                    }, null, null);
            return;
        }


        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.WIFI_SSID);
        bean.setLength(ssid.length());
        bean.setValue(ssid);
        routerList.add(bean);

        DatalogAPSetParam bean1 = new DatalogAPSetParam();
        bean1.setParamnum(DataLogApDataParseUtil.WIFI_PASSWORD);
        bean1.setLength(password.length());
        bean1.setValue(password);
        routerList.add(bean1);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, devId, routerList);
        Mydialog.Show(this);
        mService.writeCharacteristic(bytes);
    }


    /*发送命令查询状态*/
    private void checkStatus() throws Exception {
        new Handler().postDelayed(() -> {
            byte[] bytes = new byte[0];
            try {
                bytes = DatalogApUtil.sendMsg_blue19(DatalogApUtil.DATALOG_GETDATA_0X19, devId,
                        new int[]{DataLogApDataParseUtil.CHECKCONNET_STATUS});
            } catch (Exception e) {
                e.printStackTrace();
            }
            mService.writeCharacteristic(bytes);
        }, 2000);


    }


    /*发送命令查询状态*/
    private void checkServerStatus() throws Exception {
        new Handler().postDelayed(() -> {
            byte[] bytes = new byte[0];
            try {
                bytes = DatalogApUtil.sendMsg_blue19(DatalogApUtil.DATALOG_GETDATA_0X19, devId,
                        new int[]{DataLogApDataParseUtil.LINK_STATUS});
            } catch (Exception e) {
                e.printStackTrace();
            }
            mService.writeCharacteristic(bytes);
        }, 2000);

    }


    /********************************************************/
    /**
     * 解析数据
     *
     * @param bytes
     */
    private void parserData(byte type, byte[] bytes) {
        try {
            //1.字节数组成bean
            DatalogResponBean bean = DataLogApDataParseUtil.paserData(type, bytes);
            //2.通过回应的功能码解析数据
            if (bean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X18) {
                int statusCode = bean.getStatusCode();
                int paramNum = bean.getParamNum();


                if (paramNum == DataLogApDataParseUtil.BLUETOOTH_KEY||step==0) {//连接成功
                    if (statusCode == 0) {
                        //发送重启指令
                        sendRestart();
                        configSuccess();
                    } else {//提示失败
                        CircleDialogUtils.showCommentDialog(this, getString(R.string.reminder),
                                getString(R.string.连接失败), getString(R.string.retry),
                                getString(R.string.all_no), Gravity.CENTER, v -> {
                                    try {
                                        sendCmdConnect();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }, null);
                    }
                }


                if (paramNum == DataLogApDataParseUtil.WIFI_PASSWORD||step==3) {//设置WiFi名称密码回应
                    if (statusCode == 0) {
                        checkStatus();//查询配网状态
                        configStart();
                    } else {
                        errorCode = "301";
                        errorNameCn = ConfigConstant.bluetooth_config_error_zn_301;
                        errorNameEn = ConfigConstant.bluetooth_config_error_en_301;
                        configError();
                    }

                }


            } else if (bean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X19) {
                List<DatalogResponBean.ParamBean> paramBeanList = bean.getParamBeanList();
                for (int i = 0; i < paramBeanList.size(); i++) {
                    DatalogResponBean.ParamBean paramBean = paramBeanList.get(i);
                    int num = paramBean.getNum();
                    String value = paramBean.getValue();
                    if (num == DataLogApDataParseUtil.WIFI_SSID) {
                        if (!TextUtils.isEmpty(value)) {
                            etSsid.setText(value);
                        }
                    }
                    if (num == DataLogApDataParseUtil.WIFI_PASSWORD) {
                        if (!TextUtils.isEmpty(value)) {
                            etPassword.setText(value);
                        }
                    }
                    //查询配网状态  一直到连接服务器才会返回60，如果要判断是否采集器是否连接上路由器请求55

                    if (num == DataLogApDataParseUtil.CHECKCONNET_STATUS) {
                        //连接错误
                        if ("0".equals(value)) {//连接成功
                            Toast.makeText(this,R.string.m107wifi连接路由器,Toast.LENGTH_SHORT).show();
                            checkServerStatus();
                        } else if ("255".equalsIgnoreCase(value)) {
                            checkStatus();//查询配网状态
                        } else {
                            errorCode = "302";

                            errorNameCn = ConfigConstant.bluetooth_config_error_zn_302;
                            errorNameEn = ConfigConstant.bluetooth_config_error_en_302;
                            configError();//连接失败
                        }
                    }


                    if (num == DataLogApDataParseUtil.LINK_STATUS) {
                        //连接错误
                        int status = Integer.parseInt(value);
                        if (status==4||status==3||status==16) {//连接成功
                            Toast.makeText(this,R.string.m107wifi连接路由器,Toast.LENGTH_SHORT).show();
                            if (deviceType.contains("g")) {
                                setKey();
                            } else {
                                configSuccess();
                            }

                        } else {
                            checkServerStatus();//查询配网状态
                        /*    errorCode = "303";
                            errorNameCn = ConfigConstant.bluetooth_config_error_zn_303;
                            errorNameEn = ConfigConstant.bluetooth_config_error_en_303;
                            configError();//连接失败*/
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private TextView tvProgress;
    private TextView tvTips;

    private void configStart() {
        //1.弹框
        if (dialogFragment == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.config_dialog_layout, null);
            dialogFragment = CircleDialogUtils.showCommentBodyView(this, view, "", this.getSupportFragmentManager(), new OnCreateBodyViewListener() {
                @Override
                public void onCreateBodyView(View view) {
                    view.findViewById(R.id.loading_tips);
                    tvProgress = view.findViewById(R.id.tv_progress);
                    tvTips = view.findViewById(R.id.loading_tips);
                    tvTips.setText(R.string.config_wait);
                }
            }, Gravity.CENTER, 0.8f, 0.5f);
        }
        //设置倒计时
        startTimer();
    }


    /**
     * 定时刷新开始
     */
    private void startTimer() {
        if (mTimer == null) mTimer = new Timer();
        if (timerTask != null) timerTask.cancel();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                progress++;
                if (progress <= max) {
                    runOnUiThread(() -> tvProgress.setText(progress + "%"));
                } else {
                    errorCode = "303";
                    errorNameCn = ConfigConstant.bluetooth_config_error_en_303;
                    errorNameEn = ConfigConstant.bluetooth_config_error_zn_303;
                    configError();
                }
            }
        };
        mTimer.schedule(timerTask, 0, 1000);
    }


    //失败界面
    private void configError() {
        //1.停止倒计时
        configStop();
        //1.通知其他页面关闭
        EventBus.getDefault().post(new DatalogConfigfinish());
        //2.跳转失败界面
        Intent intent = new Intent(this, DatalogAPConfigErrorActivity.class);
        intent.putExtra("errorCode", errorCode);
        intent.putExtra("errorNameCn", errorNameCn);
        intent.putExtra("errorNameEn", errorNameEn);
        jumpTo(intent, true);
    }


    private void configSuccess() {
//        //1.延迟五秒钟
        new Handler().postDelayed(() -> {
            //关闭配网倒计时
            configStop();
            //直接跳转到配网成功
            showConfigSuccess();
        }, 5000);

    }


    //成功界面
    private void showConfigSuccess() {
        //1.通知其他页面关闭
        EventBus.getDefault().post(new DatalogConfigfinish());
        //2.跳转成功界面
        Intent intent = new Intent(this, DatalogConfigSuccessActivity.class);
        jumpTo(intent, true);
    }


    public void configStop() {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
            dialogFragment = null;
        }
        stopTimer();
    }


    /**
     * 停止定时器
     */
    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            timerTask.cancel();
            mTimer.purge();
            mTimer = null;
        }
        progress = 0;
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        isView = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mService!=null){
            mService.disconnect();
        }
    }
}
