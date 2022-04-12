package com.yjcloud.asr.icp.board.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * App自动更新
 */
public class AppAutoUpdateActivity extends Activity {

    private static final String TAG = "AppAutoUpdate";

    private ProgressDialog pBar;

    private Handler handler;

    // 服务端基础地址
    private String baseUrl;

    // 更新内容
    private String updateInfo;

    /**
     * 检查是否需要更新
     */
    @Override
    protected void onStart()  {
        super.onStart();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        baseUrl = bundle.getString("baseUrl");
        updateInfo = bundle.getString("updateInfo");
        handler = new Handler();
        showUpdateDialog();
    }


    /**
     * 弹出是否更新对话框
     */
    private void showUpdateDialog(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = new AlertDialog.Builder(AppAutoUpdateActivity.this)
                        .setTitle("软件更新")
                        .setMessage(updateInfo)
                        .setCancelable(false)
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showProgressBar();
                            }
                        })
                        .setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    /**
     * 显示下载进度
     */
    private void showProgressBar(){
        pBar = new ProgressDialog(this);
        pBar.setTitle("正在下载");
        pBar.setMessage("请稍后...");
        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downAppFile();
    }

    /**
     * 下载APK
     */
    private void downAppFile(){
        pBar.show();
        new Thread(){
            public void run(){
                String downApkUrl = baseUrl + "/board/"+ CurrentVersion.APK_NAME;
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(downApkUrl);
                HttpResponse response;
                try{
                    response = client.execute(get);
                    if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                        throw new RuntimeException("更新包不存在");
                    }
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if(is == null){
                        throw new RuntimeException("isStream is null");
                    }
                    File file = new File(Environment.getExternalStorageDirectory(), CurrentVersion.APK_NAME);
                    file = new File(Environment.getExternalStorageDirectory(), CurrentVersion.APK_NAME);
                    fileOutputStream = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int ch = -1;
                    do{
                        ch = is.read(buf);
                        if(ch <= 0) break;
                        fileOutputStream.write(buf, 0, ch);
                    }while(true);
                    is.close();
                    fileOutputStream.close();
                    haveDownLoad();
                }catch (Exception e){
                    Log.e(TAG, "下载APK失败", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(AppAutoUpdateActivity.this, "下载失败:"+ e.getMessage(), Toast.LENGTH_LONG);
                            toast.show();
                            finish();
                        }
                    });
                }

            }
        }.start();
    }

    /**
     * 结束下载
     */
    private void haveDownLoad(){
        File file = new File(Environment.getExternalStorageDirectory(), CurrentVersion.APK_NAME);
        if(!file.exists()){
            Toast toast = Toast.makeText(AppAutoUpdateActivity.this, "文件不存在，更新失败!", Toast.LENGTH_LONG);
            toast.show();
            finish();
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                pBar.cancel();
                Dialog installDialog = new AlertDialog.Builder(AppAutoUpdateActivity.this)
                        .setTitle("下载完成")
                        .setMessage("是否安装新的应用")
                        .setCancelable(false)
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                installNewApk();
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).create();
                installDialog.show();
            }
        });
    }

    /**
     * 安装Apk
     */
    private void installNewApk(){
        /*try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), CurrentVersion.APK_NAME)),
                    "application/vnd.android.package-archive");
            startActivity(intent);
        }catch (Exception e){
            Log.e(TAG, "安装APK失败", e);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(AppAutoUpdateActivity.this, "安装APK失败:"+ e.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            });
        }*/

        File apkFile = new File(Environment.getExternalStorageDirectory(), CurrentVersion.APK_NAME);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //记得修改com.xxx.fileprovider与androidmanifest相同
                uri = FileProvider.getUriForFile(getApplicationContext(),"com.yjcloud.asr.icp.board.fileprovider", apkFile);
                intent.setDataAndType(uri,"application/vnd.android.package-archive");
            }else{
                uri = Uri.parse("file://" + apkFile.toString());
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
        }catch (Exception e){
            Log.e(TAG, "安装APK失败", e);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(AppAutoUpdateActivity.this, "安装APK失败:"+ e.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            });
        }

    }

}
