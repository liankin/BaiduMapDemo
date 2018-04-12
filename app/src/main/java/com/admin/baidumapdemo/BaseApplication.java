package com.admin.baidumapdemo;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.support.think.util.CacheUtil;
import android.support.think.util.CrashHandler;

import com.admin.baidumapdemo.officiallocationdemo.LocationService;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by admin on 2018/3/29.
 */

public class BaseApplication extends Application{

    private static BaseApplication mInstance;
    public  static String appName ;

    //官方定位demo
    public LocationService locationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        appName = getApplicationInfo().name;

        //异常捕获
        CacheUtil.build(this);
        CrashHandler.build(this);

        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(BaseApplication.this);
    }

    public static BaseApplication getAppContext() {
        return mInstance;
    }

    public static String getAppName(){
        return appName;
    }
}
