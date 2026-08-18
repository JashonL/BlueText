package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import static com.shuoxd.bluetext.datalogConfig.bluetooth.BleSession.BLE_CONNECTING;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mylhyl.circledialog.BaseCircleDialog;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.res.drawable.CircleDrawable;
import com.mylhyl.circledialog.res.values.CircleColor;
import com.mylhyl.circledialog.res.values.CircleDimen;
import com.shuoxd.ble.BleClient;
import com.shuoxd.ble.callback.BleScanCallback;
import com.shuoxd.ble.model.BleDevice;
import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.BlueToothAdapter;
import com.shuoxd.bluetext.DataLogApDataParseUtil;
import com.shuoxd.bluetext.DatalogAPSetParam;
import com.shuoxd.bluetext.DatalogApUtil;
import com.shuoxd.bluetext.DatalogConfigfinish;
import com.shuoxd.bluetext.DatalogResponBean;
import com.shuoxd.bluetext.GlobalConstant;
import com.shuoxd.bluetext.Mydialog;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.databinding.ActivityBleModuleScanBinding;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.ConfigManager;
import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleScanRecordParser;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleSession;
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

/**
 * BLE scan + connect page powered by {@link BleClient} via {@link BleSession}.
 * Advertising parse rules stay in {@link BleScanRecordParser}.
 */
public class BleModuleScanActivity extends BaseActivity implements View.OnClickListener {

    private ActivityBleModuleScanBinding binding;
    private BlueToothAdapter mAdapter;

    private boolean scaning = false;
    private boolean isAnimShowed = false;
    private boolean isvisible = true;
    private int pos = 0;
    private int step = 0;
    private String version = "";
    private String deviceType = "";
    private BaseCircleDialog dialogUpdate;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver bleStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
                    break;
                case BLE_CONNECTING:
                    setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_OFF) {
                        setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private final BleScanCallback bleScanCallback = new BleScanCallback() {
        @Override
        public void onScanStarted(boolean success) {
            // no-op
        }

        @Override
        public void onScanning(@NonNull BleDevice device) {
            BleBean bleBean = BleScanRecordParser.parse(device.getScanRecord(), device.getMac());
            if (bleBean == null) {
                return;
            }
            for (BleBean existing : mAdapter.getData()) {
                if (existing.getBleName().equals(bleBean.getBleName())) {
                    return;
                }
            }
            mAdapter.addData(bleBean);
            showScanResult();
        }

        @Override
        public void onScanFinished() {
            scanStopUi();
        }

        @Override
        public void onScanFailed(int errorCode, @NonNull String message) {
            Toast.makeText(BleModuleScanActivity.this, message, Toast.LENGTH_SHORT).show();
            scanStopUi();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleModuleScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventBus.getDefault().register(this);

        DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        if (configBean == null) {
            finish();
            return;
        }

        initToobar(binding.headerView.toolbar);
        binding.headerView.tvTitle.setText(R.string.bluetooth_search);

        initRecyclerView();
        initListeners();
        registerBleStatusReceiver();

        String retry = getString(R.string.bluetooth_onoff) + "\n"
                + getString(R.string.two_press) + "\n"
                + getString(R.string.disconnect_retry);
        binding.bluetoothScanFail.tvErrorText.setText(retry);

        binding.tvSearchText.setVisibility(View.GONE);
        binding.clResult.setVisibility(View.GONE);
        binding.tvNote.setVisibility(View.VISIBLE);
        binding.bluetoothScanFail.bluetoothScanFail.setVisibility(View.GONE);

        startSearchAnim();

        if (!BluetoothUtils.isSupportBle()) {
            CircleDialogUtils.showCommentDialog(this, getString(R.string.温馨提示),
                    getString(R.string.not_support_bluetooth),
                    getString(R.string.all_ok), "", Gravity.CENTER,
                    v -> finish(), null, null);
            return;
        }

        if (BluetoothUtils.isBluetoothOpen()) {
            startBleScan();
        } else {
            BluetoothUtils.openBluetooth(this, BluetoothUtils.REQUEST_ENABLE_BT);
        }
    }

    private void initListeners() {
        binding.tvSearchText.setOnClickListener(this);
        binding.bluetoothScanFail.tvRetry.setOnClickListener(this);
    }

    private void registerBleStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BLE_CONNECTING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bleStatusReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(bleStatusReceiver, intentFilter);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initRecyclerView() {
        mAdapter = new BlueToothAdapter(R.layout.item_blue_tooth, new ArrayList<>());
        binding.rlvBluetooth.setLayoutManager(new LinearLayoutManager(this));
        binding.rlvBluetooth.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            pos = position;
            for (BleBean item : mAdapter.getData()) {
                if (BluetoothConstant.BLUETOOTH_CONNET_STATUS_3.equals(item.getStatus())) {
                    return;
                }
            }
            BleBean bleBean = mAdapter.getData().get(position);
            bleBean.setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
            mAdapter.notifyDataSetChanged();

            BleClient.getInstance().stopScan();
            sendBroadcast(new Intent(BLE_CONNECTING));
            BleSession.getInstance().connect(bleBean.getAddress());
        });
    }

    private void startBleScan() {
        BleClient.getInstance().config().setScanTimeoutMs(10_000);
        BleClient.getInstance().startScan(bleScanCallback);
    }

    private void startSearchAnim() {
        scaning = true;
        binding.ripple.startRippleAnimation();
    }

    private void stopSearchAnim() {
        binding.ripple.stopRippleAnimation();
    }

    private void scanStopUi() {
        scaning = false;
        stopSearchAnim();
        scaleZoomAnimator();
        showScanResult();
    }

    private void showScanResult() {
        if (!isAnimShowed) {
            isAnimShowed = true;
            mainHandler.postDelayed(() -> {
                showResult();
                binding.tvNote.setVisibility(View.GONE);
            }, 1500);
        }
        String s = getString(R.string.nearby_bluetooth) + "(" + mAdapter.getData().size() + ")";
        binding.tvNearbyBlue.setText(s);
    }

    private void showResult() {
        int size = mAdapter.getData().size();
        if (size == 0) {
            binding.bluetoothScanFail.bluetoothScanFail.setVisibility(View.VISIBLE);
            binding.ripple.setVisibility(View.GONE);
            binding.tvNote.setVisibility(View.GONE);
            binding.clResult.setVisibility(View.GONE);
            binding.tvSearchText.setVisibility(View.GONE);
        } else {
            TranslateAnimation ctrlAnimation = new TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                    TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0);
            ctrlAnimation.setDuration(500);
            binding.clResult.setVisibility(View.VISIBLE);
            binding.clResult.startAnimation(ctrlAnimation);
            binding.tvSearchText.setVisibility(View.VISIBLE);
            binding.bluetoothScanFail.bluetoothScanFail.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_search_text) {
            reScan();
        } else if (id == R.id.tv_retry) {
            binding.bluetoothScanFail.bluetoothScanFail.setVisibility(View.GONE);
            binding.ripple.setVisibility(View.VISIBLE);
            binding.tvNote.setVisibility(View.GONE);
            binding.clResult.setVisibility(View.VISIBLE);
            binding.tvSearchText.setVisibility(View.VISIBLE);
            reScan();
        }
    }

    private void reScan() {
        if (scaning) {
            return;
        }
        mAdapter.getData().clear();
        mAdapter.notifyDataSetChanged();
        binding.tvNearbyBlue.setText(getString(R.string.nearby_bluetooth) + "(0)");
        isAnimShowed = false;
        startSearchAnim();
        scaleBigAnimator();
        startBleScan();
    }

    private void scaleZoomAnimator() {
        ValueAnimator vValue = ValueAnimator.ofFloat(1.0f, 0.8f);
        vValue.setDuration(300L);
        vValue.addUpdateListener(animation -> {
            float scale = (Float) animation.getAnimatedValue();
            binding.ripple.setScaleX(scale);
            binding.ripple.setScaleY(scale);
        });
        ViewGroup.LayoutParams layoutParams = binding.ripple.getLayoutParams();
        layoutParams.height = (int) (binding.ripple.getHeight() * 0.6);
        binding.ripple.setLayoutParams(layoutParams);
        vValue.setInterpolator(new LinearInterpolator());
        vValue.start();
    }

    private void scaleBigAnimator() {
        ValueAnimator vValue = ValueAnimator.ofFloat(0.8f, 1f);
        vValue.setDuration(300L);
        vValue.addUpdateListener(animation -> {
            float scale = (Float) animation.getAnimatedValue();
            binding.ripple.setScaleX(scale);
            binding.ripple.setScaleY(scale);
        });
        ViewGroup.LayoutParams layoutParams = binding.ripple.getLayoutParams();
        layoutParams.height = (int) (binding.ripple.getHeight() / 0.6);
        binding.ripple.setLayoutParams(layoutParams);
        vValue.setInterpolator(new LinearInterpolator());
        vValue.start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setBleConnStatus(String status) {
        List<BleBean> data = mAdapter.getData();
        if (data.isEmpty() || pos < 0 || pos >= data.size()) {
            return;
        }
        data.get(pos).setStatus(status);
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventConfigFinish(DatalogConfigfinish msg) {
        if (msg != null) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull NotFoundEvent bean) {
        if (bean.isConnet()) {
            if (isvisible) {
                mainHandler.postDelayed(() -> {
                    try {
                        BleBean bleBean = mAdapter.getData().get(pos);
                        deviceType = bleBean.getType();
                        sendCmdConnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 1000);
            }
        } else {
            disconnet();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull BleDisconnectedEvent bean) {
        disconnet();
        if (dialogUpdate != null) {
            dialogUpdate.dialogDismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull ConnBleFailMsg bean) {
        disconnet();
        try {
            Toast.makeText(this, R.string.all_failed, Toast.LENGTH_SHORT).show();
            if (!mAdapter.getData().isEmpty() && pos < mAdapter.getData().size()) {
                mAdapter.getData().get(pos).setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
                mAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void disconnet() {
        List<BleBean> data = mAdapter.getData();
        if (data.isEmpty() || pos < 0 || pos >= data.size()) {
            return;
        }
        data.get(pos).setStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
        mAdapter.notifyDataSetChanged();
    }

    private void sendCmdConnect() throws Exception {
        step = 0;
        String bluetoothCommentKey = BluetoothConstant.BLUETOOTH_OSS_KEY;
        List<DatalogAPSetParam> restartList = new ArrayList<>();
        DatalogAPSetParam bean = new DatalogAPSetParam();
        bean.setParamnum(DataLogApDataParseUtil.BLUETOOTH_KEY);
        bean.setLength(bluetoothCommentKey.length());
        bean.setValue(bluetoothCommentKey);
        restartList.add(bean);
        byte[] bytes = DatalogApUtil.sendMsg_bt18(
                DatalogApUtil.DATALOG_GETDATA_0X18, "0000000000", restartList);
        BleSession.getInstance().writeCharacteristic(bytes);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRespons(@NonNull BleEvent bean) {
        if (!isvisible) {
            return;
        }
        Mydialog.Dismiss();
        byte[] datavalues = bean.getDatavalues();
        try {
            byte type = datavalues[7];
            byte[] removePro = DataLogApDataParseUtil.removePro(datavalues);
            parserData(type, removePro);
        } catch (Exception e) {
            Toast.makeText(this, R.string.返回异常, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void parserData(byte type, byte[] bytes) {
        try {
            DatalogResponBean bean = DataLogApDataParseUtil.paserData(type, bytes);
            if (bean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X18) {
                int statusCode = bean.getStatusCode();
                int paramNum = bean.getParamNum();
                if (paramNum == DataLogApDataParseUtil.BLUETOOTH_KEY || step == 0) {
                    if (statusCode == 0) {
                        toConfig();
                    } else {
                        CircleDialogUtils.showCommentDialog(this, getString(R.string.reminder),
                                getString(R.string.m暂无权限), getString(R.string.all_ok),
                                getString(R.string.all_no), Gravity.CENTER, v -> finish(),
                                view -> finish());
                    }
                }
            } else if (bean.getFuncode() == DatalogApUtil.DATALOG_GETDATA_0X19) {
                List<DatalogResponBean.ParamBean> paramBeanList = bean.getParamBeanList();
                for (int i = 0; i < paramBeanList.size(); i++) {
                    DatalogResponBean.ParamBean paramBean = paramBeanList.get(i);
                    String value = paramBean.getValue();
                    if (TextUtils.isEmpty(value)) {
                        continue;
                    }
                    switch (paramBean.getNum()) {
                        case DataLogApDataParseUtil.DATALOGGER_TYPE:
                            deviceType = value;
                            break;
                        case DataLogApDataParseUtil.FIRMWARE_VERSION:
                            version = value;
                            break;
                        default:
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
                    CircleDrawable bgCircleDrawable = new CircleDrawable(CircleColor.DIALOG_BACKGROUND,
                            CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS,
                            CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS);
                    view.setBackground(bgCircleDrawable);
                })
                .show(getSupportFragmentManager());

        mainHandler.postDelayed(() -> {
            show.dialogDismiss();
            BleBean bleBean = mAdapter.getData().get(pos);
            DatalogConfigBean configBean = new DatalogConfigBean();
            configBean.setSerialNumber(bleBean.getBleName());
            ConfigManager.getInstance().setConfigBean(configBean);

            Intent intent = new Intent(BleModuleScanActivity.this, BleModuleDebugSelectActivity.class);
            intent.putExtra(GlobalConstant.DEVICE_TYPE, "");
            startActivity(intent);
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != Activity.RESULT_OK) {
            finish();
            return;
        }
        if (requestCode == BluetoothUtils.REQUEST_ENABLE_BT) {
            startBleScan();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mainHandler.removeCallbacksAndMessages(null);
        BleClient.getInstance().stopScan();
        try {
            unregisterReceiver(bleStatusReceiver);
        } catch (Exception ignored) {
        }
        if (isFinishing()) {
            BleSession.getInstance().disconnect();
        }
    }
}
