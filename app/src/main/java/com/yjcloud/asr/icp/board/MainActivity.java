
package com.yjcloud.asr.icp.board;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.yjcloud.asr.icp.board.update.AppAutoUpdateActivity;
import com.yjcloud.asr.icp.board.update.GetUpdateInfo;
import com.yjcloud.asr.icp.board.util.FileUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainActivity extends Activity {

    private static final String TAG = "Main";

    private WebView webview;

    // 服务端基础地址
    private String baseUrl;

    private Integer newVerCode;

    // 更新内容
    private String updateInfo;

    private ProgressDialog pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        installLocalTbsCoreAndLoadWebView();
    }

    /**
     * 安装X5内核，并加截WebView
     */
    private void installLocalTbsCoreAndLoadWebView(){
        boolean canLoadX5 = QbSdk.canLoadX5(getApplicationContext());
        Log.i(TAG, "canLoadX5: " + canLoadX5 +"|TbsVersion:"+ QbSdk.getTbsVersion(getApplicationContext()));
        if (canLoadX5) {
            Log.i(TAG, "已经安装X5内核");
            loadWebView();
            return;
        }
        showProgressBar();

        FileUtils.copyAssets(getApplicationContext(), "046011_x5.tbs.apk", FileUtils.getTBSFileDir(getApplicationContext()).getPath() + "/046011_x5.tbs.apk");

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {

            }

            @Override
            public void onInstallFinish(int i) {
                Log.i(TAG, "onInstallFinish: " + i);
                pBar.cancel();
                // i == 200表示安装成功，其余安装失败
                if(i == 200){
                    showInstallSuccessDialog();
                }else{
                    showInstallFailDialog(String.valueOf(i));
                }
            }

            @Override
            public void onDownloadProgress(int i) {

            }
        });
        QbSdk.reset(getApplicationContext());
        QbSdk.installLocalTbsCore(getApplicationContext(), 46011, FileUtils.getTBSFileDir(getApplicationContext()).getPath() + "/046011_x5.tbs.apk");
    }

    private void killAppProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 显示正在安装X5内核
     */
    private void showProgressBar(){
        pBar = new ProgressDialog(this);
        pBar.setTitle("正在安装X5内核");
        pBar.setMessage("请稍后...");
        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pBar.show();
    }

    /**
     * 显示成功安装对话框
     */
    private void showInstallSuccessDialog(){
        Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("成功")
                .setMessage("安装X5内核成功, 请重启App")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        killAppProcess();
                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 显示成功安装对话框
     */
    private void showInstallFailDialog(String errorMsg){
        Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("失败")
                .setMessage("离线安装X5内核失败，请联系系统管理员! 错误原因："+ errorMsg)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).create();
        alertDialog.show();
    }


    /**
     * 加截WebView
     */
    private void loadWebView(){
        Map<String, Object> map = new HashMap<>(2);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);

        webview = findViewById(R.id.llq);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);

        if (webview.getX5WebViewExtension()==null){
            webview.loadUrl("http://debugtbs.qq.com/");
            Toast.makeText(this,"X5内核未启动，请先安装线上内核", Toast.LENGTH_LONG).show();
        }else{
            //显示本地网页
            if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                webview.loadUrl("file:///android_asset/ptindex.html"); // 竖屏
            }else{
                webview.loadUrl("file:///android_asset/index.html"); // 横屏
            }
        }

        webview.setWebViewClient(new MyWebViewClient());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack() ){
            webview.goBack();
            return  true;
        }
        return  false;
    }


    private class MyWebViewClient extends WebViewClient {
        // 在WebView中而不是默认浏览器中显示页面
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        /**
         * 获取localStorage存储的服务端地址
         * @param view
         * @param url
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String js = "window.localStorage.getItem('baseUrl');";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript(js, new ValueCallback(){
                    @Override
                    public void onReceiveValue(Object value) {
                        String url = value.toString();
                        if(url != null && url.trim().length() > 0){
                            url = url.replaceAll("\"","");
                            if(!"null".equalsIgnoreCase(url)){
                                baseUrl = url;
                                new Thread(){
                                    public void run(){
                                        try{
                                            if(getServerVersion()){
                                                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                                                if(newVerCode > info.versionCode){
                                                    Intent intent = new Intent();
                                                    intent.setClass(MainActivity.this, AppAutoUpdateActivity.class);
                                                    intent.putExtra("baseUrl", baseUrl);
                                                    intent.putExtra("updateInfo", updateInfo);
                                                    startActivity(intent);
                                                }
                                            }
                                        }catch (PackageManager.NameNotFoundException e){
                                            Log.e(TAG, "检查是否需要更新失败", e);
                                        }
                                    }
                                }.start();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取服务端版本信息
     * @return
     */
    private boolean getServerVersion(){
        try{
            String versionUrl = baseUrl + "/board/version.json";
            String newVerJSON = GetUpdateInfo.getUpateVerJSON(versionUrl);
            JSONObject jsonObject = new JSONObject(newVerJSON);
            newVerCode = Integer.parseInt(jsonObject.getString("verCode"));
            updateInfo = "修复Bug";
            if(jsonObject.has("updateInfo")){
                updateInfo = jsonObject.getString("updateInfo");
            }
        }catch (Exception e){
            Log.e(TAG, "获取服务端版本信息失败", e);
            return false;
        }
        return true;
    }


}