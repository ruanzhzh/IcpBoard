package com.yjcloud.asr.icp.board.update;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.yjcloud.asr.icp.board.R;

/**
 * 获取APP当前版本信息
 */
public class CurrentVersion {

    private static final String TAG = "Config";

    public static final String APP_PACK_NAME = "com.yjcloud";

    public static final String APK_NAME = "icpboard.apk";

    public static int getVerCode(Context context) throws PackageManager.NameNotFoundException {
        int verCode = -1;
        try{
            verCode = context.getPackageManager().getPackageInfo(APP_PACK_NAME, 0).versionCode;
        }catch (Exception e){
            Log.e(TAG, "获取vercode失败", e);
        }
        return verCode;
    }

    public static String getVerName(Context context) throws PackageManager.NameNotFoundException {
        String verName = "";
        try{
            verName = context.getPackageManager().getPackageInfo(APP_PACK_NAME, 0).versionName;
        }catch (Exception e){
            Log.e(TAG, "获取verName失败", e);
        }
        return verName;
    }

    public static String getAppName(Context context) {
        String appName = context.getResources().getText(R.string.app_name).toString();
        return appName;
    }


}
