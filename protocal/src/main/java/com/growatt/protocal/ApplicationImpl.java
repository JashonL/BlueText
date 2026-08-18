package com.growatt.protocal;

import android.content.Context;

public class ApplicationImpl {

    private Context applicationContext;

    private static class Holder {
        private static final ApplicationImpl INSTANCE = new ApplicationImpl();
    }

    public static ApplicationImpl INSTANCE() {
        return Holder.INSTANCE;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
    }
}
