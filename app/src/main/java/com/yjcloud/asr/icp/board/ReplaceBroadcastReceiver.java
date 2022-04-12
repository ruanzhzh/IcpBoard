package com.yjcloud.asr.icp.board;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.yjcloud.asr.icp.board.update.CurrentVersion;

import java.io.File;

public class ReplaceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ApkDelete";

    @Override
    public void onReceive(Context context, Intent intent) {
        File file = new File(Environment.getExternalStorageDirectory(), CurrentVersion.APK_NAME);
        if(file.exists()){
            file.delete();
        }
    }
}
