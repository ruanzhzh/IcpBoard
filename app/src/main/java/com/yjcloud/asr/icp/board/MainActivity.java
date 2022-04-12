
package com.yjcloud.asr.icp.board;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.sdk.WebView;
import com.yjcloud.asr.icp.board.update.AppAutoUpdateActivity;
import com.yjcloud.asr.icp.board.update.GetUpdateInfo;

import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final String TAG = "Main";

    private WebView webview;

    // 服务端基础地址
    private String baseUrl;

    private Integer newVerCode;

    // 更新内容
    private String updateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            webview.loadUrl("file:///android_asset/index.html"); //显示本地网页
        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

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