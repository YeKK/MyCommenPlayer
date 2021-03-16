package com.d.commenplayer;

import android.app.Application;
import android.os.Handler;

import com.d.commenplayer.netstate.NetCompat;
import com.d.commenplayer.util.PrefHelper;

/**
 * App
 * Created by D on 2018/9/22.
 */
public class App extends Application {

    private Handler mUiHandler;
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        NetCompat.init(getApplicationContext());
        sInstance = this;
        mUiHandler = new Handler();
        initUtils();
    }

    private void initUtils() {
        PrefHelper.initDefault(this);
    }

    public static App instance() {
        return sInstance;
    }

    public static Handler getUiHandler() {
        return instance().mUiHandler;
    }
}
