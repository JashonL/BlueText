package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.growatt.protocal.modbus.Modbus;
import com.growatt.protocal.modbus.ModbusSet;
import com.growatt.protocal.modbus.ModbusSet06;
import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.version6.Protocol0X17;
import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.databinding.ActivityBleModulePassthroughBinding;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleSession;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 0x17 透传调试：06 单寄存器设置 / 10 连续寄存器设置。
 */
public class BleModulePassthroughActivity extends BaseActivity implements View.OnClickListener {

    private ActivityBleModulePassthroughBinding binding;
    private String lastPlainSendHex = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleModulePassthroughBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventBus.getDefault().register(this);

        initToobar(binding.headerView.toolbar);
        binding.headerView.tvTitle.setText("0x17透传调试");

        binding.rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            boolean is06 = checkedId == R.id.rb_06;
            binding.llMode06.setVisibility(is06 ? View.VISIBLE : View.GONE);
            binding.llMode10.setVisibility(is06 ? View.GONE : View.VISIBLE);
        });

        binding.etStartAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    binding.etEndAddr.setText("");
                    return;
                }
                try {
                    int startAddr = Integer.parseInt(s.toString());
                    binding.etEndAddr.setText(String.valueOf(startAddr + 2));
                } catch (NumberFormatException e) {
                    binding.etEndAddr.setText("");
                }
            }
        });

        binding.btnOk.setOnClickListener(this);
        showMode06();
    }

    private void showMode06() {
        binding.rb06.setChecked(true);
        binding.llMode06.setVisibility(View.VISIBLE);
        binding.llMode10.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.btn_ok) {
            return;
        }
        if (!BleSession.getInstance().isConnected()) {
            Toast.makeText(this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (binding.rb06.isChecked()) {
                send06();
            } else {
                send10();
            }
        } catch (Exception e) {
            Toast.makeText(this, "组包失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void send06() throws Exception {
        Integer addr = parseIntOrDecimal(binding.etRegAddr.getText().toString());
        Integer setValue = parseIntOrDecimal(binding.etSetValue.getText().toString());
        if (addr == null || setValue == null) {
            Toast.makeText(this, "请输入寄存器地址和设置值", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] setValues = ByteUtils.intTo2Byte(setValue);
        ModbusSet06 modbus = ModbusSet06.newInstance(addr, setValues);
        sendModbus(modbus);
    }

    private void send10() throws Exception {
        Integer start = parseIntOnly(binding.etStartAddr.getText().toString());
        Integer v1 = parseIntOnly(binding.etValue1.getText().toString());
        Integer v2 = parseIntOnly(binding.etValue2.getText().toString());
        Integer v3 = parseIntOnly(binding.etValue3.getText().toString());
        if (start == null || v1 == null || v2 == null || v3 == null) {
            Toast.makeText(this, "请输入开始寄存器地址和三个设置值", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] setValues = ByteUtils.join(
                ByteUtils.intTo2Byte(v1),
                ByteUtils.intTo2Byte(v2),
                ByteUtils.intTo2Byte(v3)
        );
        ModbusSet modbus = ModbusSet.newInstance(start, setValues);
        sendModbus(modbus);
    }

    private void sendModbus(@NonNull Modbus modbus) throws Exception {
        byte[] modbusBytes = modbus.getBytes();
        Protocol0X17 protocol = Protocol0X17.newInstanceForModbus(modbusBytes);
        lastPlainSendHex = safeHex(protocol.getDecodeBytes());
        appendLog("发送(未加密):\n" + lastPlainSendHex + "\n返回:\n");
        BleSession.getInstance().writeCharacteristic(protocol.getBytes());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleEvent(@NonNull BleEvent event) {
        byte[] data = event.getDatavalues();
        if (data == null || data.length < 8 || (data[7] & 0xFF) != 0x17) {
            return;
        }
        String receiveHex = safeHex(data);
        appendLog("发送(未加密):\n" + lastPlainSendHex + "\n返回:\n" + receiveHex);
    }

    private void appendLog(@NonNull String text) {
        binding.tvLog.setText(text);
    }

    @Nullable
    private Integer parseIntOrDecimal(@Nullable String raw) {
        if (TextUtils.isEmpty(raw) || ".".equals(raw) || "-".equals(raw)) {
            return null;
        }
        try {
            return (int) Math.round(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private Integer parseIntOnly(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @NonNull
    private String safeHex(@Nullable byte[] data) {
        String hex = ByteUtils.bytesToHexString(data);
        return hex == null ? "" : hex.trim();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
