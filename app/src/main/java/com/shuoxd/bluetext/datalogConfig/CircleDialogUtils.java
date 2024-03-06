package com.shuoxd.bluetext.datalogConfig;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;


import com.mylhyl.circledialog.BaseCircleDialog;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.TextParams;
import com.mylhyl.circledialog.res.drawable.CircleDrawable;
import com.mylhyl.circledialog.res.values.CircleColor;
import com.mylhyl.circledialog.res.values.CircleDimen;
import com.mylhyl.circledialog.view.listener.OnCreateBodyViewListener;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;
import com.mylhyl.circledialog.view.listener.OnLvItemClickListener;
import com.shuoxd.bluetext.R;

import java.util.List;

public class CircleDialogUtils {
    /**
     * 公共自定义弹框
     *
     * @return
     */
    public static DialogFragment showCommentBodyDialog(View bodyView, FragmentManager fragmentManager, OnCreateBodyViewListener listener) {
        DialogFragment bulbBodyDialog = new CircleDialog.Builder()
                .setBodyView(bodyView, listener)
                .setGravity(Gravity.BOTTOM)
                .setYoff(20)
                .show(fragmentManager);
        ;
        return bulbBodyDialog;
    }


    /**
     * 公共自定义弹框
     *
     * @return
     */
    public static DialogFragment showCommentBodyDialog(int width, float height, View bodyView, FragmentManager fragmentManager, OnCreateBodyViewListener listener) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        builder.setWidth(width);
        builder.setMaxHeight(height);
        builder.setBodyView(bodyView, listener);
        builder.setGravity(Gravity.BOTTOM);

        return builder.show(fragmentManager);
    }


    /**
     * 跳转到连接wifi界面
     */
    public static DialogFragment showSetWifiDialog(Context context, FragmentManager fragmentManager) {
        DialogFragment setWifiDialog = new CircleDialog.Builder()
                .setTitle(context.getString(R.string.温馨提示))
                .setText(context.getString(R.string.手机暂未连接WiFi))
                .setWidth(0.7f)
                .setPositive(context.getString(R.string.去连接), v -> {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(intent);
                })
                .show(fragmentManager);
        return setWifiDialog;
    }


    /**
     * 跳转到连接5G提示
     */
    public static DialogFragment show5GHzDialog(Context context, FragmentManager fragmentManager, View.OnClickListener onNegativeListener, View.OnClickListener onPositiveListner) {
        DialogFragment _5GhzDialog = new CircleDialog.Builder()
                .setTitle(context.getString(R.string.温馨提示))
                .setText(context.getString(R.string.您当前连接的是5GWiFi))
                .setWidth(0.7f)
                .setNegative(context.getString(R.string.继续), onNegativeListener)
                .setPositive(context.getString(R.string.切换WiFi), onPositiveListner)
                .show(fragmentManager);
        return _5GhzDialog;
    }


    /**
     * 获取涂鸦token失败提示
     */
    public static DialogFragment showGetTuyaTokenFail(Context context, FragmentManager fragmentManager, String errorMsg) {
        DialogFragment getTokenFailDialog = new CircleDialog.Builder()
                .setTitle(context.getString(R.string.温馨提示))
                .setText(errorMsg)
                .setWidth(0.7f)
                .setPositive(context.getString(R.string.all_ok), v -> {
                    ((FragmentActivity) context).finish();
                })
                .show(fragmentManager);
        return getTokenFailDialog;
    }


    /**
     * 配网失败
     *
     * @return
     */
    public static DialogFragment showFailConfig(View bodyView, FragmentManager fragmentManager, OnCreateBodyViewListener listener) {
        DialogFragment failConfigDialog = new CircleDialog.Builder()
                .setBodyView(bodyView, listener)
                .setGravity(Gravity.CENTER)
                .show(fragmentManager);
        ;
        return failConfigDialog;
    }


    /**
     * 公共输入框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentInputDialog(FragmentActivity activity, String title, String text,
                                                        String hint, boolean showKeyboard,
                                                        OnInputClickListener listener) {
        DialogFragment inputDialog = new CircleDialog.Builder()
                .setCanceledOnTouchOutside(true)
                .setCancelable(true)
                .setTitle(title)
                .setInputText(text)
                .setInputHint(hint)
                .setWidth(0.75f)
                .setGravity(Gravity.CENTER)
                .setInputShowKeyboard(showKeyboard)
                .setInputCounter(1000, (maxLen, currentLen) -> "")
                .setPositiveInput(activity.getString(R.string.all_ok), listener)
                .configInput(params -> {
//                            params.isCounterAllEn = true;
                    params.padding = new int[]{5, 5, 5, 5};
                    params.strokeColor = ContextCompat.getColor(activity, R.color.title_bg_white);
//                                params.inputBackgroundResourceId = R.drawable.bg_input;
//                                params.gravity = Gravity.CENTER;
                    //密码
//                                params.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
//                                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
                    //文字加粗
//            params.styleText = Typeface.BOLD;
                })
                .setNegative(activity.getString(R.string.all_no), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show(activity.getSupportFragmentManager());
        return inputDialog;


    }


    /**
     * 公共输入框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCustomInputDialog(FragmentActivity activity, String title, String subTitle, String text, String hint,
                                                       boolean showKeyboard, int gravity, String positiveText, OnInputClickListener inputClickListener,
                                                       String negativeText, ConfigInput configInput, View.OnClickListener negativeListner) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.setTitle(title);
        if (!TextUtils.isEmpty(subTitle)) {
            builder.setSubTitle(subTitle);
        }

        if (!TextUtils.isEmpty(text)) {
            builder.setInputText(text);
        }

        if (!TextUtils.isEmpty(hint)) {
            builder.setInputHint(hint);
        }
        builder.setWidth(0.8f);
        builder.setGravity(gravity);
        builder.setInputShowKeyboard(showKeyboard);
        builder.setInputCounter(1000, (maxLen, currentLen) -> "");
        if (configInput != null) {
            builder.configInput(configInput);
        }
   /*     builder.configInput(params -> {
//                            params.isCounterAllEn = true;
            params.padding = new int[]{5, 5, 5, 5};
            params.strokeColor = ContextCompat.getColor(activity, R.color.title_bg_white);
//                                params.inputBackgroundResourceId = R.drawable.bg_input;
//                                params.gravity = Gravity.CENTER;
            //密码
//                                params.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
//                                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            //文字加粗
//            params.styleText = Typeface.BOLD;
        });*/

        if (negativeListner != null) {
            builder.setNegative(negativeText, negativeListner);
        }

        if (inputClickListener != null) {
            builder.setPositiveInput(positiveText, inputClickListener);
        }


        return builder.show(activity.getSupportFragmentManager());

    }


    /**
     * 公共提示框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentDialog(FragmentActivity activity, String title, String text, View.OnClickListener listener) {
        return showCommentDialog(activity, title, text, listener, null);
    }


    /**
     * 公共提示框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentDialog(FragmentActivity activity, String title, String text, View.OnClickListener posiListener, View.OnClickListener negativeListener) {
        DialogFragment inputDialog = new CircleDialog.Builder()
                .setTitle(title)
                .setText(text)
                .setWidth(0.75f)
                .setGravity(Gravity.CENTER)
                .setPositive(activity.getString(R.string.all_ok), posiListener)
                .setNegative(activity.getString(R.string.all_no), negativeListener)
                .show(activity.getSupportFragmentManager());
        return inputDialog;
    }


    /**
     * 公共提示框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentDialog(FragmentActivity activity, String title, String text, String positive, String negative, int TextGravity,
                                                   View.OnClickListener posiListener, View.OnClickListener negativeListener) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(text)) {
            builder.setText(text);
        }
        builder.configText(new ConfigText() {
            @Override
            public void onConfig(TextParams params) {
                params.gravity = TextGravity;
            }
        });

        if (negativeListener != null) {
            builder.setNegative(negative, negativeListener);
        }

        if (posiListener != null) {
            builder.setPositive(positive, posiListener);
        }

        builder.setCancelable(false);
        builder.setWidth(0.75f);
        return builder.show(activity.getSupportFragmentManager());
    }


    /**
     * 公共提示框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentDialog(FragmentActivity activity, String title, String text, String positive, String negative, int TextGravity,
                                                   View.OnClickListener posiListener, View.OnClickListener negativeListener, DialogInterface.OnDismissListener listener) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        if (!TextUtils.isEmpty(text)) {
            builder.setText(text);
        }

        builder.configText(new ConfigText() {
            @Override
            public void onConfig(TextParams params) {
                params.gravity = TextGravity;
            }
        });

        if (negativeListener != null) {
            builder.setNegative(negative, negativeListener);
        }

        if (posiListener != null) {
            builder.setPositive(positive, posiListener);
        }

        if (listener != null) {
            builder.setOnDismissListener(listener);
        }
        builder.setWidth(0.8f);
        return builder.show(activity.getSupportFragmentManager());
    }


    /**
     * 公共提示框没有取消
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentNoCancelDialog(FragmentActivity activity, String title, String text, View.OnClickListener posiListener) {
        DialogFragment inputDialog = new CircleDialog.Builder()
                .setTitle(title)
                .setText(text)
                .setGravity(Gravity.CENTER)
                .setPositive(activity.getString(R.string.all_ok), posiListener)
                .show(activity.getSupportFragmentManager());
        return inputDialog;
    }


    /**
     * 公共提示框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentDialog(FragmentActivity activity, String title, String text, View.OnClickListener posiListener, View.OnClickListener negativeListener, boolean isCancelTouchOutsize) {
        DialogFragment inputDialog = new CircleDialog.Builder()
                .setTitle(title)
                .setText(text)
                .setGravity(Gravity.CENTER)
                .setPositive(activity.getString(R.string.all_ok), posiListener)
                .setNegative(activity.getString(R.string.all_no), negativeListener)
                .setCanceledOnTouchOutside(isCancelTouchOutsize)
                .show(activity.getSupportFragmentManager());
        return inputDialog;
    }


    /**
     * 公共复选框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentItemDialog(FragmentActivity activity, String title, List<String> items, int gravity, OnLvItemClickListener listener) {
        DialogFragment itemsSelectDialog = new CircleDialog.Builder()
                .setTitle(title)
                .configTitle(params -> {
                    params.styleText = Typeface.BOLD;
                })
                .setItems(items, listener)
                .configItems(params -> {
                    params.dividerHeight = 0;
                    params.textColor = ContextCompat.getColor(activity, R.color.title_bg_white);
                })
                .setGravity(gravity)
                .show(activity.getSupportFragmentManager());

        return itemsSelectDialog;
    }


    /**
     * 公共复选框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentItemDialog(FragmentActivity activity, String title, List<String> items, int gravity, OnLvItemClickListener listener, View.OnClickListener negativeListener) {
        DialogFragment itemsSelectDialog = new CircleDialog.Builder()
                .setTitle(title)
                .configTitle(params -> {
                    params.styleText = Typeface.BOLD;
                })
                .setItems(items, listener)
                .configItems(params -> {
                    params.dividerHeight = 0;
                    params.textColor = ContextCompat.getColor(activity, R.color.title_bg_white);
                })
                .setGravity(gravity)
                .setNegative(activity.getString(R.string.all_no), negativeListener)
                .show(activity.getSupportFragmentManager());

        return itemsSelectDialog;
    }


    /**
     * 公共复选框,没有标题
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentItemDialog(FragmentActivity activity, List<String> items, int gravity, OnLvItemClickListener listener) {
        DialogFragment itemsSelectDialog = new CircleDialog.Builder()
                .setItems(items, listener)
                .configItems(params -> {
                    params.dividerHeight = 0;
                    params.textColor = ContextCompat.getColor(activity, R.color.title_bg_white);
                })
                .setGravity(gravity)
                .show(activity.getSupportFragmentManager());

        return itemsSelectDialog;
    }


    /**
     * 公共复选框
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentItemDialog(FragmentActivity activity, String title,
                                                       List<String> items, int gravity, String negative,
                                                       OnLvItemClickListener listener, View.OnClickListener negativeListener) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        if (listener != null) {
            builder.setItems(items, listener);
        }

        if (negativeListener != null) {
            builder.setNegative(negative, negativeListener);
        }

        builder.setGravity(gravity);
        builder.setWidth(0.75f);
        return builder.show(activity.getSupportFragmentManager());
    }


    /**
     * 公共复选框2
     *
     * @param activity
     * @return
     */
    public static DialogFragment showCommentItemDialog2(FragmentActivity activity, String title, String subtitle,
                                                        List<String> items, int gravity, String negative,
                                                        OnLvItemClickListener listener, View.OnClickListener negativeListener) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        if (!TextUtils.isEmpty(subtitle)) {
            builder.setSubTitle(subtitle);
        }

        if (listener != null) {
            builder.setItems(items, listener);
        }

        if (negativeListener != null) {
            builder.setNegative(negative, negativeListener);
        }

        builder.setGravity(gravity);
        builder.setWidth(0.75f);
        return builder.show(activity.getSupportFragmentManager());
    }


    /**
     * 公共自定义框
     *
     * @return
     */
    public static DialogFragment showCommentBodyView(Context context, View bodyView, String title, FragmentManager fragmentManager, OnCreateBodyViewListener listener, int gravity, float width, float maxHeight) {
        CircleDialog.Builder builder = new CircleDialog.Builder();
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setBodyView(bodyView, listener);
        builder.setGravity(gravity);
        builder.setWidth(width);
        builder.setMaxHeight(maxHeight);
        builder.setCancelable(false);
        return builder.show(fragmentManager);
    }


    /**
     * 公共自定义框
     *
     * @return
     */
    public static DialogFragment showCommentBodyViewNoCancel(Context context, View bodyView, String title, FragmentManager fragmentManager, OnCreateBodyViewListener listener, View.OnClickListener positiveListner) {
        DialogFragment commentBodyDialog = new CircleDialog.Builder()
                .setTitle(title)
                .setBodyView(bodyView, listener)
                .setGravity(Gravity.CENTER)
                .setPositive(context.getString(R.string.all_ok), positiveListner)
                .show(fragmentManager);
        ;
        return commentBodyDialog;
    }


    /**
     * 公共复选择框
     *
     * @return
     */
    public static DialogFragment showSelectItemDialog(FragmentActivity context, String title, RecyclerView.Adapter adapter, RecyclerView.LayoutManager layoutManager, View.OnClickListener positiveListner) {
        DialogFragment selectItemDialog = new CircleDialog.Builder()
                .setTitle(title)
                .setItems(adapter, layoutManager)
                .setPositive(context.getString(R.string.all_ok), positiveListner)
                .setNegative(context.getString(R.string.all_no), null)
                .show(context.getSupportFragmentManager());
        ;
        return selectItemDialog;
    }


    /**
     * 设置图片
     *
     * @param activity
     * @param modes
     * @param listener
     * @return
     */

    public static DialogFragment showTakePictureDialog(FragmentActivity activity, List<String> modes, OnLvItemClickListener listener) {

        DialogFragment takePictureDialog = new CircleDialog.Builder()
                .setTitle(activity.getString(R.string.m225请选择))
                .setItems(modes, listener)
                .setGravity(Gravity.BOTTOM)
                .setNegative(activity.getString(R.string.all_no), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show(activity.getSupportFragmentManager());
        return takePictureDialog;
    }




    /**
     * 公共自定义弹框
     *
     * @return
     */
    public static DialogFragment showCommentBodyDialog(View bodyView, FragmentManager fragmentManager, int gravity, OnCreateBodyViewListener listener) {
        DialogFragment bulbBodyDialog = new CircleDialog.Builder()
                .setBodyView(bodyView, listener)
                .setGravity(gravity)
                .setYoff(20)
                .show(fragmentManager);
        ;
        return bulbBodyDialog;
    }


    /**
     * 场景颜色闪烁模式弹框
     */
    public static DialogFragment showSceneFlashMode(FragmentActivity activity, List<String> modes, OnLvItemClickListener listener) {
        DialogFragment flashModeDialog = new CircleDialog.Builder()
                .setTitle(activity.getString(R.string.m161_colour_flash_mode))
                .configTitle(params -> {
                    params.styleText = Typeface.BOLD;
                })
                .setItems(modes, listener)
                .setGravity(Gravity.BOTTOM)
                .show(activity.getSupportFragmentManager());
        return flashModeDialog;
    }




    public interface OndialogComfirListener {
        void comfir(String value);
    }


    public interface timeSelectedListener {
        void cancle();

        void ok(boolean status, int hour, int min);
    }


}
