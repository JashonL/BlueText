package com.shuoxd.bluetext.datalogConfig.BlueToothMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;


import com.shuoxd.bluetext.BaseActivity;
import com.shuoxd.bluetext.DatalogStep2ModActivity;
import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DatalogConfigSuccessActivity extends BaseActivity {

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
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.tv_step_title4)
    TextView tvStepTile4;

/*
    private String action;
    //根据配网类型判断是否需要添加
    private String configType;
    private String datalogSn;
    private String userId;
    private String plantId;
    private String serverId;
    private String isHave;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalog_config_success);
        ButterKnife.bind(this);

   /*     DatalogConfigBean configBean = ConfigManager.getInstance().getConfigBean();
        action = configBean.getAction();
        configType = configBean.getConfigType();
        datalogSn = configBean.getSerialNumber();
        userId = configBean.getUserId();
        plantId = configBean.getPlantId();
        serverId = configBean.getServerId();
        isHave = configBean.getIsHave();*/


        tvTitle.setText(R.string.dataloggers_add_success);
        tvStepTile4.setText(R.string.config_finish);

        toolbar.setNavigationIcon(R.drawable.icon_return);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });


    /*    if (Constant.DATALOGER_CONFIG_FROM_OSS.equals(this.action)) {//从OSS进来
            btnNext.setText(R.string.返回);
        }*/
    }


    @OnClick({R.id.btn_next})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_next) {
            back();
        }
    }


    public void back() {
    /*    if (configType.equals(Constant.CONFIG_ADD) || configType.equals(Constant.CONFIG_ADD_LOGIN)) {//判断是否需要添加
            if (action.equals(Constant.DATALOGER_CONFIG_FROM_OSS)) {//从OSS进来
                ossAddDatalog();
            } else {
                addDatalog();
            }
        } else {
            configFinish();
        }*/

        configFinish();

    }


    private void addDatalog() {/*
        String code = AppUtils.validateWebbox(datalogSn);
        if (!TextUtils.isEmpty(userId)) {
            DatalogApUtil.addDatalog(this, datalogSn, code, userId, plantId, new IDatalogAddCallback() {
                @Override
                public void addDatalogSuccess(Context context) {
                    configFinish();
                }

                @Override
                public void addDatalogFail(Context context, String msg) {
                    addFailDialog(msg);
                }

                @Override
                public void netWorkError() {
                    addException();
                }

            });
        } else {
            DatalogApUtil.addDatalogLogined(this, datalogSn, code, plantId, new IDatalogAddCallback() {
                @Override
                public void addDatalogSuccess(Context context) {
                    configFinish();
                }

                @Override
                public void addDatalogFail(Context context, String msg) {
                    addFailDialog(msg);
                }

                @Override
                public void netWorkError() {
                    addException();
                }

            });
        }*/
    }


    private void ossAddDatalog() {/*
        String code = AppUtils.validateWebbox(datalogSn);
        DatalogApUtil.ossAddDatalog(this, datalogSn, code, serverId, plantId, new IDatalogAddCallback() {
            @Override
            public void addDatalogSuccess(Context context) {
                configFinish();
            }

            @Override
            public void addDatalogFail(Context context, String msg) {
                addFailDialog(msg);
            }

            @Override
            public void netWorkError() {
                addException();
            }

        });*/
    }


    private void addException() {
        CircleDialogUtils.showCommentDialog(this, getString(R.string.温馨提示), getString(R.string.switch_network), getString(R.string.all_ok), getString(R.string.all_no), Gravity.CENTER, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }, null);
    }


    private void addFailDialog(String msg) {/*
        String text = getString(R.string.添加失败);
        switch (msg) {
            case "501":
                text += "," + getString(R.string.serviceerror);
                break;
            case "502":
                text += "," + getString(R.string.dataloggers_add_no_jurisdiction);
                break;
            case "503":
                text += "," + getString(R.string.dataloggers_add_no_number);
                break;
            case "504":
                text += "," + getString(R.string.dataloggers_add_no_v);
                break;
            case "505":
                text += "," + getString(R.string.dataloggers_add_no_full);
                break;
            case "506":
                text += "," + getString(R.string.dataloggers_add_no_exist);
                break;
            case "507":
                text += "," + getString(R.string.dataloggers_add_no_matching);
                break;
            case "701":
                text += "," + getString(R.string.m7);
                break;
        }
        new CircleDialog.Builder()
                .setTitle(getString(R.string.温馨提示))
                .setText(text)
                .setWidth(0.7f)
                .setNegative(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (action.equals(Constant.DATALOGER_CONFIG_FROM_OSS)) {
                            ossAddDatalog();
                        } else {
                            addDatalog();
                        }
                    }
                })
                .setPositive(getString(R.string.m504跳过), v -> {
                    configFinish();
                })
                .show(getSupportFragmentManager());*/
    }


    private void configFinish() {/*
        if (Constant.DATALOGER_CONFIG_TO_MAIN.equals(action)) {
            jumpTo(MainActivity.class, true);
        } else if (Constant.DATALOGER_CONFIG_TO_LOGIN.equals(action)) {//去登录
            //直接调用server登录接口登录
            ServerLoginUtils.serverLogin(0, mContext, Urlsutil.getUrl_cons(), Cons.regMap.getRegUserName(),
                    Cons.regMap.getRegPassword(), new LoginListener() {
                        @Override
                        public void ossloginSuccess() {

                        }

                        @Override
                        public void ossLoginFail(String code, String msg) {

                        }

                        @Override
                        public void serverLoginSuccess() {
                            SharedPreferencesUnit.getInstance(mContext).put("country", Cons.regMap.getRegCountry());
                        }

                        @Override
                        public void serverLoginFail(String code, String msg) {

                        }
                    });


        } else if (Constant.DATALOGER_CONFIG_FROM_OSS.equals(action)) {
            finish();
        } else {
            finish();
        }*/

        jumpTo(DatalogStep2ModActivity.class,true);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
