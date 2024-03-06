package com.shuoxd.bluetext.datalogConfig;



//建造者模式构建采集器的配网流程

import com.shuoxd.bluetext.datalogConfig.bean.DatalogConfigBean;

public class ConfigManager {


    private DatalogConfigBean configBean;


    public static ConfigManager instance = null;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }


    public DatalogConfigBean getConfigBean() {
        return configBean;
    }

    public void setConfigBean(DatalogConfigBean configBean) {
        this.configBean = configBean;
    }
}
