package com.shuoxd.bluetext;

public class GlobalConstant {
    public static final String COMPANY_NAME="growatt";

    //*********************Activity跳转传值**************************
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_TYPE = "device_type";
    public static final String ROOM_ID = "room_id";
    public static final String ROOM_NAME = "room_name";
    public static final String DEVICE_ROAD = "road";
    public static final String DEVICE_CONFIG_TYPE = "config_type";
    public static final String MESSAGE_DETAIL="message_detail";
    //**************************场景*****************************
    public static final String DEVICE_BEAN = "device_bean";
    public static final String SCENE_DEVICE_OPEN = "open";
    public static final String SCENE_DEVICE_SET = "set";
    public static final String SCENE_DEVICE_SHUT = "shut";
    public static final int SCENE_TYPE_LUANCH = 0;



    //*****************************时间********************************
    public static final String TIME_VALUE = "start_value";
    public static final int REQUEST_CODE_SELECT_REPEAT = 101;
    public static final String TIME_LOOPTYPE = "start_looptype";
    public static final String TIME_LOOPVALUE = "start_loopvalue";



    //**************************开关********************************
    public static final int STATUS_ON = 1;
    public static final int STATUS_OFF = 0;

    public static final String STRING_STATUS_ON = "1";
    public static final String STRING_STATUS_OFF = "0";

    //***************************彩灯场景*******************************
    public static final String BULB_ISWHITE="iswhite";
    public static final String BULB_SCENE_BEAN = "bulb_scene_bean";
    public static final String BULB_SCENE_BEAN_LIST = "bulb_scene_list";


    //**************************item类型添加或是其他********************************
    public static final int STATUS_ITEM_DATA= 1;
    public static final int STATUS_ITEM_OTHER = 0;



    //******************************温度符号******************************
    public static final String TEMP_UNIT = "temp_unit";
    public static final String TEMP_UNIT_CELSIUS = "℃";
    public static final String TEMP_UNIT_FAHRENHEIT = "℉";

    //**************************温控器场景*********************************
    public static final String TEMP_MODE="temp_mode";
    public static final String MODE_HOLIDAY="holiday";
    public static final String MODE_SMART="smart";


    //*************************添加还是编辑******************************
    public static final String EDIT="edit";
    public static final String ADD="add";
    public static final String ACTION="action";
    //**************************定时***********************************
    public static final String TIMING_BEAN="timing_bean";
    public static final String TIMING_CKEY="timing_ckey";

    //***********************名字集合*********************
    public static final String NAME_LIST="name_list";

    //**************请求修改邮箱********************************
    public static final String IMAGE_FILE_LOCATION = "grohome/avatar.png";

    //**************充电桩********************************
    public static final String CHARGE_PILE_BEAN = "chargepile";
    public static final String CHARGE_PILE_GUN_BEAN= "chargepile_gun";
    public static final String CHARGE_PILE_CHARGE_BEAN="chargepile_charge_bean";
    public static final String CHARGE_PILE_SETTING_TYPE="chargepile_setting_type";
    public static final String SERVER_IP="server_ip";
    public static final String SERVER_PORT="server_port";
    public static final String CHARGE_PILE_RESERVATION="chargepile_reservation";


    //**************电站********************
    public static final String PLANT_ID = "PLANT_ID";
    public static final String PLANT_NAME = "PLANT_NAME";

    //****************Groboost****************

    public static final String GRO_BOOST_MODE_LINKAGE = "0";
    public static final String GRO_BOOST_MODE_SMART = "1";

    public static final String WEB_URL="web_url";


    public static final String KEY_APP_FIRST_INSTALL="key_app_first_install";
    public static final String KEY_LAN="Languge";
    public static final String KEY_APP_NEW_VERSION="key_new_version";
}
