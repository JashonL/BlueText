
package com.shuoxd.bluetext;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * Baseclass of all Activities of the Demo Application.
 *
 * @author Philipp Jahoda
 */
public abstract class BaseActivity extends SwipeBackActivity implements
        EasyPermissions.PermissionCallbacks{



    Toast toast;
    protected Context mContext;
    /**
     * 是否继续询问权限
     */
    protected boolean isContinue = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;


    }


    /**
     * 隐藏软键盘-点击空白处隐藏键盘
     */
    protected void hideSoftInputClickOut() {
        try {
            //点击空白处隐藏键盘
            ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).setOnClickListener(view -> {
                if (view.getWindowToken() != null) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 通过不同类型activity请求
     */
    public void requestWindowTitleByActivity() {
        if (this instanceof AppCompatActivity) {
            ((AppCompatActivity) this).supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }


    /**
     * 初始化侧滑返回
     */
    private SwipeBackLayout mSwipeBackLayout;






    public void initToobar(Toolbar toolbar){
        if (toolbar!=null){
            toolbar.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // 必须调用该方法，防止内存泄漏
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ��ȡ��ǰ���
     *
     * @return
     */
    public static int getCurrentYear() {

        return Calendar.getInstance().get(Calendar.YEAR);
    }



    /**
     * @return
     */
    public int smarthomeGetLanguage() {
        int lan;
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.toLowerCase().contains("zh")) {
            lan = 0;
        } else if (language.toLowerCase().contains("en")) {
            lan = 1;
        } else {
            lan = 2;
        }
        return lan;
    }



    public String getLanguageStr() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.toLowerCase().contains("zh")) {
            language = "zh_cn";
        } else if (language.toLowerCase().contains("en")) {
            language = "en";
        } else if (language.toLowerCase().contains("fr")) {
            language = "fr";
        } else if (language.toLowerCase().contains("ja")) {
            language = "ja";
        } else if (language.toLowerCase().contains("it")) {
            language = "it";
        } else if (language.toLowerCase().contains("ho")) {
            language = "ho";
        } else if (language.toLowerCase().contains("tk")) {
            language = "tk";
        } else if (language.toLowerCase().contains("pl")) {
            language = "pl";
        } else if (language.toLowerCase().contains("gk")) {
            language = "gk";
        } else if (language.toLowerCase().contains("gm")) {
            language = "gm";
        } else if (language.toLowerCase().contains("hu")) {
            language = "hu";
        } else if (language.toLowerCase().contains("hk")) {
            language = "hk";
        } else {
            language = "en";
        }
        return language;
    }









    public void log(String log) {
        Log.d("TAG", this.getClass().getSimpleName() + ": " + log);
    }



    public void jumpTo(Class<?> clazz, boolean isFinish) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
        if (isFinish) {
            finish();
        }
    }

    public void jumpTo(Intent intent, boolean isFinish) {
        startActivity(intent);
        if (isFinish) {
            finish();
        }
    }


    /**
     * ������λС��
     *
     * @param str ԭʼ����
     * @param num ����С��λ��
     * @return
     */
    public String getNumberFormat(String str, int num) {
        BigDecimal bd = new BigDecimal(str);
        return bd.setScale(num, BigDecimal.ROUND_HALF_UP) + "";
    }

    //获取屏幕密度
    public float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    public void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();

    }

    protected void onPause() {
        super.onPause();

    }

    public void onStop() {
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onPermissionsDenied(int requestCode, List<String> perms, String permission) {
    }

    public void onPermissionsDenied(int requestCode, List<String> perms, int permissionResId) {
        onPermissionsDenied(requestCode, perms, getString(permissionResId));
    }
    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {

    }
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.onPermissionsGranted(requestCode, null);
    }

}
