package com.yjcloud.asr.icp.board.update;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 获取服务端版本更新信息
 */
public class GetUpdateInfo {

    public static String getUpateVerJSON(String url) throws Exception{
        StringBuffer verJSON = new StringBuffer();
        HttpClient client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        HttpResponse response = client.execute(new HttpGet(url));
        HttpEntity entity = response.getEntity();
        if(entity != null){
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"),8192);
            String line = null;
            while((line = reader.readLine()) != null){
                verJSON.append(line + "\n");
            }
            reader.close();
        }
        return verJSON.toString();
    }
}
