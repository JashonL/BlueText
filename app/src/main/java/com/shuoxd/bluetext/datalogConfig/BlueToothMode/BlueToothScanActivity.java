package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.mylhyl.circledialog.BaseCircleDialog;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.res.drawable.CircleDrawable;
import com.mylhyl.circledialog.res.values.CircleColor;
import com.mylhyl.circledialog.res.values.CircleDimen;
import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.BlueToothAdapter;
import com.shuoxd.bluetext.DataLogApDataParseUtil;
import com.shuoxd.bluetext.DatalogAPSetParam;
import com.shuoxd.bluetext.DatalogApUtil;
import com.shuoxd.bluetext.DatalogConfigfinish;
import com.shuoxd.bluetext.DatalogResponBean;
import com.shuoxd.bluetext.GlobalConstant;
import com.shuoxd.bluetext.MyApplication;
import com.shuoxd.bluetext.Mydialog;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.RippleBackground;
import com.shuoxd.bluetext.SmartHomeUtil;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleScanManager;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleService;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BluetoothUtils;
import com.shuoxd.bluetext.datalogConfig.bluetooth.bean.BleBean;
import com.shuoxd.bluetext.datalogConfig.bluetooth.constant.BluetoothConstant;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleDisconnectedEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.ConnBleFailMsg;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.NotFoundEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 开发步骤
 * 1、权限申请 (Android6.0搜索周围的蓝牙设备，需要位置权限 ACCESS_COARSE_LOCATION 和ACCESS_FINE_LOCATION
 * 其中的一个，并且将手机的位置服务（定位 GPS）打开。)
 * 2、开启蓝牙
 * 3、扫描蓝牙
 * 4、连接蓝牙
 * 5、通信（实现双向通信）
 * 6、关闭各种通信
 */

public class BlueToothScanActivity extends BaseActivity
        implements Toolbar.OnMenuItemClickListener, BleScanManager.Scanlisteners,
        BleScanManager.ConnetedListeners {

    @BindView(R.id.status_bar_view)
    View statusBarView;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.ripple)
    RippleBackground ripple;
    @BindView(R.id.rlv_bluetooth)
    RecyclerView rlvBlueTooth;
    @BindView(R.id.tvNote)
    TextView tvNote;
    @BindView(R.id.tv_nearby_blue)
    TextView tvNearbyBlue;
    @BindView(R.id.cl_result)
    ConstraintLayout clResult;
    @BindView(R.id.tv_search_text)
    TextView tvSearchText;
    @BindView(R.id.tv_error_text)
    TextView tvError;

    @BindView(R.id.bluetooth_scan_fail)
    LinearLayout blueScanFail;


    private BlueToothAdapter mAdapter;
    private BleScanManager manager;
    private boolean scaning = false;
    private BleService mBleService;
    private int pos = 0;//当前要连接的蓝牙
    private boolean isAnimShowed = false;

    //    private String datalogSn;
    //版本
    private String version = "";
    //设备类型
    private String deviceType = "";


    private BleBean bleBean;

    private boolean isvisible = true;
    private BaseCircleDialog dialogUpdate;

//    private String action;

    private int step=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blue_tooth_scan);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        if (configBean == null) {
            finish();
            return;
        }



        //初始化头部
        initToobar(toolbar);


        tvTitle.setText(R.string.bluetooth_search);
//        toolbar.inflateMenu(R.menu.menu_scan);
//        toolbar.setOnMenuItemClickListener(this);

        //初始化列表
        initRecyclerView();

        //初始化错误提示
        String retry = getString(R.string.bluetooth_onoff) + "\n" + getString(R.string.two_press) + "\n" + getString(R.string.disconnect_retry);

        tvError.setText(retry);


        //隐藏搜索结果控件，开始搜索
        tvSearchText.setVisibility(View.GONE);
        clResult.setVisibility(View.GONE);
        tvNote.setVisibility(View.VISIBLE);

        blueScanFail.setVisibility(View.GONE);

        startSearchAnim();


        manager = new BleScanManager(this, this, this);
        //注册蓝牙监听
        manager.registerBluetoothReceiver();
        //初始化蓝牙扫描结果回调
        manager.initCallBack();
        //判断是否支持蓝牙
        boolean b = manager.scanBluetooth();
        if (!b) return;

        //判断蓝牙是否打开
        boolean bluetoothOpen = BluetoothUtils.isBluetoothOpen();
        //如果已经打开了，初始化对应控件并开始搜索
        if (bluetoothOpen) {
            // 搜索蓝牙设备
            manager.startBleScan();
        } else {
            BluetoothUtils.openBluetooth(this, BluetoothUtils.REQUEST_ENABLE_BT);
        }


        // 绑定服务开启服务
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }


    /*修改设备名称*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventConfigFinish(DatalogConfigfinish msg) {
        if (msg != null) {
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isvisible = true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        isvisible = false;
        reConnect = 0;

    }

    //初始化列表
    private void initRecyclerView() {
        mAdapter = new BlueToothAdapter(R.layout.item_blue_tooth, new ArrayList<>());
        rlvBlueTooth.setLayoutManager(new LinearLayoutManager(this));
        rlvBlueTooth.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            manager.stopBleScan();
            pos = position;
            BleBean bleBean = mAdapter.getData().get(position);
            bleBean.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
            //mBleService.connect(mAdapter.getData().get(position).getAddress());
    /*        if (!bleName.equals(datalogSn)){
                toast(R.string.m采集器序列号错误_ios);
                return;
            }*/
            mBleService.connect(mAdapter.getData().get(position).getAddress());
        });
    }


    //开始动画
    private void startSearchAnim() {
        scaning = true;
        ripple.startRippleAnimation();
    }


    //结束搜索 动画
    private void stopSearchAnim() {
        ripple.stopRippleAnimation();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return true;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != Activity.RESULT_OK) {
            finish();
            return;
        }
        switch (requestCode) {
            case BluetoothUtils.REQUEST_ENABLE_BT:
                // 搜索蓝牙设备
                manager.startBleScan();
                break;
            case 100:
                //去连接
                String sn = intent.getStringExtra("sn");
                List<BleBean> data = mAdapter.getData();
                int index = -1;
                for (int i = 0; i < data.size(); i++) {
                    String bleName = data.get(i).getBleName();
                    if (bleName.equals(sn)) {
                        index = i;
                        break;
                    }
                }
                pos = index;

                if (index == -1) {
                    blueScanFail.setVisibility(View.VISIBLE);
                    ripple.setVisibility(View.GONE);
                    tvNote.setVisibility(View.GONE);
                    clResult.setVisibility(View.GONE);
                    tvSearchText.setVisibility(View.GONE);
                } else {
                    BleBean bleBean = data.get(pos);
                    bleBean.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
                    mBleService.connect(bleBean.getAddress());
                }

                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        manager.unRegisterBleReceiver();
        unbindService(mServiceConnection);
        if (mBleService != null) {
            mBleService.disconnect();
//        BluetoothUtils.closeBluetooth();
            mBleService = null;
        }

    }

    @Override
    public List<BleBean> getBleData() {
        return mAdapter.getData();
    }

    @Override
    public void addBleData(BleBean bleBean) {
        mAdapter.addData(bleBean);
        showScanResult();
        pos = mAdapter.getData().size() - 1;


        String bleName = bleBean.getBleName();
/*        if (bleName.equals(datalogSn)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //停止扫描  然后去连接
                        manager.stopBleScan();
                        BleBean bleBean1 = mAdapter.getData().get(pos);
                        bleBean1.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
                        mBleService.connect(mAdapter.getData().get(pos).getAddress());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);


        }*/


    }

    private void showScanResult() {
        //判断是否执行过动画
        if (!isAnimShowed) {
            isAnimShowed = true;
            //延迟1.5秒再执行
            new Handler().postDelayed(() -> {
                showResult();
                tvNote.setVisibility(View.GONE);
            }, 1500);

        }
        String s = getString(R.string.nearby_bluetooth) + "(" + mAdapter.getData().size() + ")";
        tvNearbyBlue.setText(s);
    }

    @Override
    public void scanStop() {
        scaning = false;
        stopSearchAnim();
        scaleZoomAnimator();
        showScanResult();

    }


    //设置出现动画
    private void showResult() {
        int size = mAdapter.getData().size();
        if (size == 0) {
            blueScanFail.setVisibility(View.VISIBLE);
            ripple.setVisibility(View.GONE);
            tvNote.setVisibility(View.GONE);
            clResult.setVisibility(View.GONE);
            tvSearchText.setVisibility(View.GONE);
        } else {
            //设置动画，从自身位置的最下端向上滑动了自身的高度，持续时间为500ms
            final TranslateAnimation ctrlAnimation = new TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                    TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0);
            ctrlAnimation.setDuration(500);     //设置动画的过渡时间
            clResult.setVisibility(View.VISIBLE);
            clResult.startAnimation(ctrlAnimation);
            tvSearchText.setVisibility(View.VISIBLE);
            blueScanFail.setVisibility(View.GONE);
        }

    }


    @OnClick({R.id.tv_search_text, R.id.tv_retry})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_search_text:
                reScan();
                break;
            case R.id.tv_retry:
                blueScanFail.setVisibility(View.GONE);
                ripple.setVisibility(View.VISIBLE);
                tvNote.setVisibility(View.GONE);
                clResult.setVisibility(View.VISIBLE);
                tvSearchText.setVisibility(View.VISIBLE);

                reScan();
                break;

        }
    }


    private void reScan() {
        if (!scaning) {

            mAdapter.getData().clear();
            String s = getString(R.string.nearby_bluetooth) + "(" + 0 + ")";
            tvNearbyBlue.setText(s);

            startSearchAnim();
            scaleBigAnimator();
            manager.startBleScan();
        }
    }


    //缩小效果
    private void scaleZoomAnimator() {

/*        //这里故意用两个是想让大家体会下组合动画怎么用而已~
        final float scale = 0.5f;
        AnimatorSet scaleSet = new AnimatorSet();
        ValueAnimator valueAnimatorSmall = ValueAnimator.ofFloat(1.0f, scale);
        valueAnimatorSmall.setDuration(500);

        ValueAnimator valueAnimatorLarge = ValueAnimator.ofFloat(scale, 1.0f);
        valueAnimatorLarge.setDuration(500);

        valueAnimatorSmall.addUpdateListener(animation -> {
            float scale1 = (Float) animation.getAnimatedValue();
            img_babi.setScaleX(scale1);
            img_babi.setScaleY(scale1);
        });
        valueAnimatorLarge.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (Float) animation.getAnimatedValue();
                img_babi.setScaleX(scale);
                img_babi.setScaleY(scale);
            }
        });

        scaleSet.play(valueAnimatorLarge).after(valueAnimatorSmall);
        scaleSet.start();*/

        //其实可以一个就搞定的
        ValueAnimator vValue = ValueAnimator.ofFloat(1.0f, 0.8f);
        vValue.setDuration(300L);
        vValue.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (Float) animation.getAnimatedValue();
                ripple.setScaleX(scale);
                ripple.setScaleY(scale);

            }
        });

        ViewGroup.LayoutParams layoutParams = ripple.getLayoutParams();
        layoutParams.height = (int) (ripple.getHeight() * 0.6);
        ripple.setLayoutParams(layoutParams);


        vValue.setInterpolator(new LinearInterpolator());
        vValue.start();
    }


    //放大效果
    private void scaleBigAnimator() {
/*        //这里故意用两个是想让大家体会下组合动画怎么用而已~
        final float scale = 0.5f;
        AnimatorSet scaleSet = new AnimatorSet();
        ValueAnimator valueAnimatorSmall = ValueAnimator.ofFloat(1.0f, scale);
        valueAnimatorSmall.setDuration(500);

        ValueAnimator valueAnimatorLarge = ValueAnimator.ofFloat(scale, 1.0f);
        valueAnimatorLarge.setDuration(500);

        valueAnimatorSmall.addUpdateListener(animation -> {
            float scale1 = (Float) animation.getAnimatedValue();
            img_babi.setScaleX(scale1);
            img_babi.setScaleY(scale1);
        });
        valueAnimatorLarge.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (Float) animation.getAnimatedValue();
                img_babi.setScaleX(scale);
                img_babi.setScaleY(scale);
            }
        });

        scaleSet.play(valueAnimatorLarge).after(valueAnimatorSmall);
        scaleSet.start();*/
        //其实可以一个就搞定的
        ValueAnimator vValue = ValueAnimator.ofFloat(0.8f, 1f);
        vValue.setDuration(300L);
        vValue.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (Float) animation.getAnimatedValue();
                ripple.setScaleX(scale);
                ripple.setScaleY(scale);

            }
        });

        ViewGroup.LayoutParams layoutParams = ripple.getLayoutParams();
        layoutParams.height = (int) (ripple.getHeight() / 0.6);
        ripple.setLayoutParams(layoutParams);


        vValue.setInterpolator(new LinearInterpolator());
        vValue.start();
    }


    /**
     * 服务
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mBleService = ((BleService.LocalBinder) rawBinder).getService();
            MyApplication.getInstance().setgBleServer(mBleService);
        }


        public void onServiceDisconnected(ComponentName classname) {
            mBleService = null;
        }
    };


    @Override
    public void setBleConnStatus(String status) {
        List<BleBean> data = mAdapter.getData();
        if (data.size() == 0) return;
        //更新列表
        bleBean = data.get(pos);
        bleBean.setStatus(status);
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull NotFoundEvent bean) {
        boolean connet = bean.isConnet();
        if (connet) {//连接成功
            if (isvisible) {
             /*   dialogUpdate = new CircleDialog.Builder()
                        .setWidth(0.75f)
                        .setBodyView(R.layout.dialog_config_datalog, view -> {
                            CircleDrawable bgCircleDrawable = new CircleDrawable(CircleColor.DIALOG_BACKGROUND
                                    , CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS,
                                    CircleDimen.DIALOG_RADIUS);
                            view.setBackground(bgCircleDrawable);
                            TextView tvTips = view.findViewById(R.id.loading_tips);
                            View viewById = view.findViewById(R.id.tv_time);
                            viewById.setVisibility(View.GONE);
                            tvTips.setText(R.string.check_newversion);

                        })
                        .show(getSupportFragmentManager());

                new Handler().postDelayed(() -> {
                    //判断升级
                    try {
                        BleBean bleBean = mAdapter.getData().get(pos);
                        deviceType = bleBean.getType();
                        sendCmdConnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 1000);*/


                new Handler().postDelayed(() -> {
                    //判断升级
                    try {
                        BleBean bleBean = mAdapter.getData().get(pos);
                        deviceType = bleBean.getType();
                        sendCmdConnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 1000);

            }
        } else {//连接失败
            disconnet();
        }


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull BleDisconnectedEvent bean) {
        //已经连接上  但是又断开了
        disconnet();

        if (dialogUpdate != null) {
            dialogUpdate.dialogDismiss();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull ConnBleFailMsg bean) {
        //点击连接的时候  直接断开了
        disconnet();
    }


    private void disconnet() {
        //更新列表
        List<BleBean> data = mAdapter.getData();
        if (data.size() == 0) return;
        //更新列表
        bleBean = data.get(pos);
        bleBean.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
        mAdapter.notifyDataSetChanged();

        //重新连接


    }


    private void getVersion() {
   /*     //1.设备类型 2.软件版本号 3.
        int[] paramByte = new int[]{DataLogApDataParseUtil.DATALOGGER_TYPE, DataLogApDataParseUtil.FIRMWARE_VERSION, DataLogApDataParseUtil.FOTA_FILE_TYPE};
        byte[] bytes = new byte[0];
        try {
            bytes = DatalogApUtil.sendMsg_blue19(DatalogApUtil.DATALOG_GETDATA_0X19, datalogSn, paramByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBleService.writeCharacteristic(bytes);*/
    }


    /*发送密钥连接蓝牙*/
    private void sendCmdConnect() throws Exception {
//        String bluetoothCommentKey = "";
        //蓝牙公共密钥
    /*    if (deviceType.contains("G")) {//已绑定 发送账户密钥
            bluetoothCommentKey = TextUtils.isEmpty(BluetoothConstant.BLUETOOTH_SHARE_KEY) ?
                    Cons.userBean.getCpowerToken() : BluetoothConstant.BLUETOOTH_SHARE_KEY;

        } else {//未绑定  发送公共密钥 等到配网成功之后再发送账户密钥
            bluetoothCommentKey = BluetoothConstant.BLUETOOTH_OSS_KEY;
        }*/


        step=0;

        String bluetoothCommentKey = BluetoothConstant.BLUETOOTH_OSS_KEY;


        List<DatalogAPSetParam> restartList = new ArrayList<>();
        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.BLUETOOTH_KEY);
        bean.setLength(bluetoothCommentKey.length());
        bean.setValue(bluetoothCommentKey);
        restartList.add(bean);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(DatalogApUtil.DATALOG_GETDATA_0X18, "0000000000", restartList);
        mBleService.writeCharacteristic(bytes);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull BleEvent bean) {
        if (isvisible) {
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


    /**
     * 解析数据
     *
     * @param bytes
     */
    private void parserData(byte type, byte[] bytes) {
        try {
            //1.字节数组成bean
            DatalogResponBean bean = DataLogApDataParseUtil.paserData(type, bytes);
            if (bean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X18) {
                int statusCode = bean.getStatusCode();
                int paramNum = bean.getParamNum();
                if (paramNum == DataLogApDataParseUtil.BLUETOOTH_KEY||step==0) {//连接成功
                    if (statusCode == 0) {
                        toConfig();
                    } else {//提示失败
                        CircleDialogUtils.showCommentDialog(this, getString(R.string.reminder),
                                getString(R.string.m暂无权限), getString(R.string.all_ok),
                                getString(R.string.all_no), Gravity.CENTER, v -> {
                                    finish();//设置密钥失败直接退出
                                }, view -> finish());
                    }
                }


            } else if (bean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X19) {
                int statusCode = bean.getStatusCode();
                if (statusCode == 1) {
                    Toast.makeText(this,R.string.all_failed,Toast.LENGTH_SHORT).show();
                }
                boolean isUpdate = false;
                List<DatalogResponBean.ParamBean> paramBeanList = bean.getParamBeanList();
                for (int i = 0; i < paramBeanList.size(); i++) {
                    DatalogResponBean.ParamBean paramBean = paramBeanList.get(i);
                    int num = paramBean.getNum();
                    String value = paramBean.getValue();
                    if (TextUtils.isEmpty(value)) continue;
                    switch (num) {
                        case DataLogApDataParseUtil.DATALOGGER_TYPE:
                            deviceType = value;
                            isUpdate = true;
                            break;
                        case DataLogApDataParseUtil.FIRMWARE_VERSION:
                            version = value;
                            isUpdate = true;
                            break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    private void toConfig() {

        BaseCircleDialog show = new CircleDialog.Builder()
                .setWidth(0.75f)
                .setBodyView(R.layout.dialog_bluetooth_conneted, view -> {
                    CircleDrawable bgCircleDrawable = new CircleDrawable(CircleColor.DIALOG_BACKGROUND
                            , CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS,
                            CircleDimen.DIALOG_RADIUS);
                    view.setBackground(bgCircleDrawable);
                })
                .show(getSupportFragmentManager());

        //跳转到配置页面
        new Handler().postDelayed(() -> {
            show.dialogDismiss();
            String type = bleBean.getType();
            DatalogConfigBean configBean = new DatalogConfigBean();
            configBean.setSerialNumber(bleBean.getBleName());
            ConfigManager.getInstance().setConfigBean(configBean);


            Intent intent = new Intent(BlueToothScanActivity.this, BlueToothConfigActivity.class);
            intent.putExtra(GlobalConstant.DEVICE_TYPE, type);
            startActivity(intent);
        }, 2000);
    }




    private int reConnect = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventTask(ConnBleFailMsg bean) {
        try {
            //主动连接失败
            BleBean bleBean = mAdapter.getData().get(pos);
            bleBean.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
            mAdapter.notifyDataSetChanged();


            reConnect++;

            //重新连接
            if (reConnect < 3) {
                BleBean bleBean1 = mAdapter.getData().get(pos);
                bleBean1.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
                mBleService.connect(mAdapter.getData().get(pos).getAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
