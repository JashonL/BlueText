package com.shuoxd.bluetext.datalogConfig;

import android.content.Context;


public class Constant {
    //oss动态服务器地址
//    public static  String ossUrl = "http://192.168.3.32:8081/ShineOSS";


//     	public static  String ossUrl = "http://20.6.1.114:8080/ShineOSS";


//    public static  String ossUrl = "http://47.91.176.158";
//    public static  String ossUrl = "http://oss.growatt.com";
//	public static  String ossUrl = "http://oss1.growatt.com:8080/ShineOSS";
    //热更新版本号
    public static int TinkerCode = 0;

//            public static  String ossUrl = "http://192.168.3.214/ShineOSS";
//            public static  String ossUrl = "http://192.168.3.35:8081/ShineOSS";
//    public static  String ossUrl = "http://192.168.3.117:8080/ShineOSS";
//    public static  String ossUrl = "http://test.growatt.com";
//    public static  String ossUrl = "http://odm.growatt.com";
//    public static  String ossUrl = "http://oss1.growatt.com";
//	public static  String ossUrl = "http://server.growatt.com";
//	public static  String ossUrl = "http://120.76.153.241:8080/ShineServer_2016";
  public static String https_Heaed="https://";
  public static String http_Heaed="http://";


    //自动登录常量
    public static String AUTO_LOGIN = "com.growatt.auto_login";//0:代表不自动登录；1：代表自动登录
    public static String AUTO_LOGIN_TYPE = "com.growatt.auto_login_type";//0:oss用户；1：server用户
    public static String Frag_Receiver = "com.action.fragment1.refresh";
    public static String MyQuestionfragment_Receiver = "com.action.MyQuestionfragment.refresh";
    public static int AboutActivity_Updata = 1;
    public static int LoginActivity_Updata = 2;

    //activity request code
    public static int VideoCenterActivity_search = 201;

    public static int CacheVideoActToPlayerAct = 100;//�ӻ����б���ת�����Ž���
    public static int OtherToPlayerAct = 101;
    //刷新EnergyFragment广播
    public static String Energy_Receiver = "com.action.energy.refresh";
    //设备页Demo设备类型
    public static String Device_Type = "type";//intent跳转类型
    public static int Device_Inv = 1;//逆变器
    public static int Device_Power = 2;//功率调节器
    public static int Device_Charge = 3;//充电桩
    public static int Device_storage = 4;//储能机
    //oss常量
    public static String OssUser_Phone;//oss用户电话
    public static String OssUser_Email = "ossuser_email";//oss用户电话
    public static String OssNewsTime = "0";//oss最新消息时间
    public final static String OssServer_cn = "cnName";//oss服务器中文字段
    public final static String OssServer_en = "enName";//oss服务器英文字段
    public final static String OssServer_url = "url";//oss服务器地址
    //集成商常量
    public final static String OssJK_State = "OssJK_State";//集成商设备状态
    //逆变器
    public final static int OssJK_State_all = 0;//在线
    public final static int OssJK_State_offline = 1;//离线
    public final static int OssJK_State_online = 2;//在线
    public final static int OssJK_State_err = 3;//故障
    public final static int OssJK_State_wait_inv = 4;//等待
    public final static int OssJK_State_charge_storage = 4;//充电
    public final static int OssJK_State_discharge_storage = 5;//放电

    public final static String OssJK_Dev_type = "OssJK_Dev_type";//集成商设备类型：0：逆变器；1：储能机
    public final static int OssJK_DeviceType_inv = 0;//逆变器类型
    public final static int OssJK_DeviceType_stor = 1;//储能机类型
    public final static int OssJK_DeviceType_mix = 2;//储能机类型

    public final static String OssJK_Dev_status = "OssJK_Dev_status";//集成商接入状态：2：已接入设备；3：未接入设备
    public final static int OssJK_DeviceStatus_all = 0;//已接入设备
    public final static int OssJK_DeviceStatus_yes = 1;//已接入设备
    public final static int OssJK_DeviceStatus_no = 2;//未接入设备


    //代理商
    public final static String Agent_Company = "agentCompany";//代理商公司名称
    public final static String Agent_Code = "code";//代理商编码
    public final static String Agent_Name = "company";//代理商公司名

    //所有涉及到分页的页码以及list
    public final static String currentPage = "currentPage";//当前页码
    public final static String totalPage = "totalPage";//总页码
    public final static String jumpList = "list";//要跳转的list集合

    //界面跳转常量
    public final static String ossJumpToDeviceList = "OssDeviceListActivity";//跳转oss设备列表常量
    public final static int ossPlantToDeviceList = 100;//从电站列表跳转到设备列表；根据电站id获取数据
    public final static int ossSnToDeviceList = 101;//从sn或别名跳转到设备列表；根据sn或者别名获取数据
    //包名:用来识别 是否提示更新或是否显示更新
//    public final static String google_package_name = "com.growatt.shinephones";
    public final static String google_package_name = "com.growatt.shinephones";
    public final static String shinephone_package_name = "com.growatt.shinephone";
    public  static String onlineServerAddress = "";//在线客服地址，通过服务器获取
    public final static String onlineServerAPPType = "0";//在线客服标识

    //数据库版本
    public static String getSqliteName(Context context) {
        if (google_package_name.equals(context.getPackageName())) {
            return "sqldata4.db";
        } else if ("com.smten.shinephone".equals(context.getPackageName())) {
            return "sqldata3.db";
        } else {
            return "sqldata2.db";
        }
    }

    //wifi配置识别
    public final static String WiFi_Type_ShineWIFI = "shinewifi";
    public final static String WiFi_Type_ShineWIFI_S = "shinewifi-s";
    public final static String WiFi_Type_ShineWIFI_X = "shinewifi-x";
    //Max控制密码常量：
    public final static String MAX_PWD = "max_password";
    /**
     * max本地iv曲线最后更新时间常量：MaxCheckIVActivity
     */
    public final static String MAX_IV_LAST_TIME = "max_iv_last_time";
    public final static String MAX_ONEKEY_LAST_TIME = "max_onekey_last_time";
    public final static String MAX_REAL_LAST_TIME = "max_real_last_time";
    public final static String MAX_ERR_LAST_TIME = "max_err_last_time";


    /**
     * max本地工具数据表
     */
    public final static String MAX_TOOLS_SQLITE = "max_tools_sqlite";
    /**
     * max本地曲线最后更新数据常量：MaxCheckIVActivity
     */
    public final static String MAX_IV_LAST_DATA = "max_iv_last_data";
    public final static String MAX_REAL_LAST_DATA = "max_real_last_data";
    public final static String MAX_REAL_ID_LAST_DATA = "max_real_id_last_data";
    public final static String MAX_ERR_LAST_DATA = "max_err_last_data";
    public final static String MAX_ERR_ID_LAST_DATA = "max_err_id_last_data";
    public final static String MAX_ERR_OTHER_LAST_DATA = "max_err_other_last_data";

    public final static String MAX_ONEKEY_LAST_DATA1 = "max_onekey_last_data1";
    public final static String MAX_ONEKEY_LAST_DATA2 = "max_onekey_last_data2";
    public final static String MAX_ONEKEY_LIST_LAST_DATA2 = "max_onekey_list_last_data2";
    public final static String MAX_ONEKEY_LAST_DATA3 = "max_onekey_last_data3";
    public final static String MAX_ONEKEY_LIST_LAST_DATA3 = "max_onekey_list_last_data3";

    public final static String MAX_ONEKEY_LAST_DATA4 = "max_onekey_last_data4";
    public final static String MAX_ONEKEY_LAST_DATA5 = "max_onekey_last_data5";
    /**
     * max需要密码常量：oss不需要0；server需要为1
     */
    public final static String MAX_NEED_PWD = "max_need_pwd";
    public final static String MAX_NEED_PWD_TRUE = "max_need_pwd_true";
    public final static String MAX_NEED_PWD_FALSE = "max_need_pwd_false";
    //Max控制密码常量：
    public static boolean isOss2Server = false;

    /**
     * 服务器id常量
     */
    public final static String SERVER_ID = "serverId";
/**
 * server 电站管理保存列常量
 */
    public final static String SERVER_PLANT_MANAGER_PARAM = "server_plant_manager_param";
    /**
     * 是否重新调用登录接口获取电站列表
     */
    public static boolean isRefreshServerPlantList = false;

    /**
     * 显示登录密码标示:1:标示显示；0：不显示
     */
    public static final String LOGING_SHOW_PWD = "1";

    /**
     * 优先显示电站列表：0:电站列表；1：设备列表
     */
    public static  final  String PRIORITY_DEVICE_OR_PLANT = "priority_device_or_plant";

    /**
     * 电站列表优先
     */
    public static final String PRIORITY_DEVICE="priority_device";

    /**
     * 设备列表优先
     */
    public static final String PRIORITY_PLANT="priority_plant";

    /**
     * 首页获取电站详情接口次数：每5次获取一次
     */
    public static  final  String PLANT_MARQUEE = "plant_marquee";
    /**
     * 是否弹跑马灯
     */
    public static boolean isFlagMarquee = false;

    public static final String ISFLAGMARQUEE = "marquee_flag";

    /**
     * 是否发送电站和功率异常广播：不同app控制
     */
    public static boolean isEventBusSticky_Loc = true;
    /**
     * 定位是否正常:plantId + 0:错误；1.正常  "123_0"
     */
    public final static String LOCATION_LNGLAT_TURE = "location_lnglat";


    public final static String MAINACTIVITY_ALIVE = "main_live";



    /**
     * 默认快递公司
     */
    public static String defaultCourier = "顺丰，圆通两家快递公司随机配送";

    /**
     * App版本标示
     * a			ShinePhone（server/-cn）
     b			ShinePhone瑞光宝盒（server/-cn）
     c			ShinePhone google版本（server/-cn）
     d			SMTEN斯迈特 (server.smten.com)
     e			尚科 PVguarder(http://pvguarder.sacolar.com/)
     f			尚科中性app PVbutler ()
     g			太阳雨电站 (tyy.smten.com)
     h			阿特斯CSI Phone (monitoring.csisolar.com)
     i			威阳科技 (server.smten.com)
     j			爱康 (ak.smten.com)
     k			海尔HRI4 (server.smten.com)
     l			UjaasHome (server.smten.com)
     m			中车光伏
     n			中光能电站
     */
    public static final String APP_MARK_TYPE = "a";

    /**
     * 保存默认电站
     */
    public final static String SAVE_DEFAULT_PLANT = "save_default_plant";
    /**
     * 是否有分享功能
     */
    public final static boolean IS_SHARE = true;

    public final static String FLOW_APP_CODE="flow_app_code";
    //tigo用户密码
    public static String TIGO_USER_PWD = "tigo_user_pwd";
    //mian里面提示 GPRS
    public static boolean MAIN_GPRS = false;
    public static String LOCATION_COORTYPE = "gcj02";//定位坐标系
    //全局密码控制:设置项是否需要密码
    public static boolean isNeedPwd = true;
    //本地调试记忆安规对应使能 s1:int2
    public static String TOOL_TLX_MODEL_COUNTRY = "tool_tlx_model_country";//定位坐标系
    //是否显示Growatt优化器
    public static int SHOW_GROWATT_OPTIMER = 0;//0.没有Growatt优化器  1.有Growatt优化器


    //本地调试，模式选择
    public static final String LOCAL_MODE_SELECT="local_mode_select";
    public static final int USB_WIFI=0;
    public static final int AP_MODE=1;

    //跳转扫面页面相关常量
    public static final String INTENT_ZXING_CONFIG="zxingConfig";
    public static final String SCAN_MODE="scan_mode";
    public static final String DATALOGER_LOCAL_DEBUG_STANDARD="datalog_debug_standard";
    public static final String DATALOGER_LOCAL_DEBUG_AP="datalog_debug_ap";
    public static final String DATALOGER_CONFIG_SCAN="datalog_config_scan";

    public static final String DATALOGER_SRIAL_NUM="serial_num";
    public static final String DATALOGER_SCAN_RESULT="result";

    public static final String DATALOGER_CONFIG_ACTION="config_action";
    public static final String DATALOGER_CONFIG_SSID="ssid";
    public static final String DATALOGER_CONFIG_TIMER="time_long";


    public static final String DATALOGER_CONFIG_FROM_TOOL ="100";
    public static final String DATALOGER_CONFIG_FROM_OSS ="101";
    public static final String DATALOGER_CONFIG_TO_LOGIN ="1";
    public static final String DATALOGER_CONFIG_TO_MAIN="3";


    public static final String LOGIN_SPECIAL_JUM ="login_special_jum";



    /**
     * 采集器配网类型
     */
    public final static String DATALOG_CONFIG_TYPE = "datalog_config_type";
    //是否是新的采集器
    public final static String DATALOG_ISNEW_DATALOG = "datalog_is_new";

    //配网、添加、登录
    public final static String CONFIG_ADD_LOGIN = "config_add_login";
    //配网、添加
    public final static String CONFIG_ADD = "config_add";
    //只配网
    public final static String CONFIG_ONLIY = "config_onliy";



    //采集器配网模式
    public static final String DATALOG_MODE_SELECT="datalog_mode_select";
    public static final int CONFIG_STANDART=0;
    public static final int CONFIG_AP=1;
    public static final int CONFIG_BLUETOOTH=2;
    public static final int CONFIG_5G=3;


    //常见问题
    public static final String FAQDATALOGGER_CN="file:///android_asset/FAQdatalogger_cn.html";
    public static final String FAQDATALOGGER_EN="file:///android_asset/FAQdatalogger_en.html";


    public static final String AGREEMENT_CN="file:///android_asset/UserAgreementShinePhone_cn.html";
    public static final String PRIVACY_CN="file:///android_asset/PrivacyPolicyShinePhone_cn.html";
    public static final String DESCRIPTION_CN="file:///android_asset/RemoteServiceAgreement_cn.html";


    public static final String AGREEMENT_EN="file:///android_asset/NewUserAgreement_en.html";
    public static final String PRIVACY_EN="file:///android_asset/NewPrivacyAgreement_en.html";
    public static final String DESCRIPTION_EN="";


    //错误日志
    public static final String DATALOG_CONFIG_ERROR_LOG="error_log";
    //超时
    public static final String DATALOG_ERROR_TIMEOUT="timeout";
    //获取wifi错误
    public static final String DATALOG_ERROR_GETWIFIERROR="getwifierror";
    //设置wifi错误
    public static final String DATALOG_ERROR_SETWIFI_ERROR="setwifierror";
    //
    public static final String DATALOG_ERROR_RESETERROR="reseterror";
    //
    public static final String DATALOG_ERROR_WIFISTATUS_ERROR="wifistatuserror";

    public static final String DATALOG_ERROR_DATALOG_OFFLINE="datalog_offline";
    public static final String DATALOG_ERROR_DATALOG_SOCKET_EXCEPTION="socket exception";


    /**
     * 获取隐私政策版本
     */
    public static  final  String PRIVACY_CODE = "privacy_code";
}
