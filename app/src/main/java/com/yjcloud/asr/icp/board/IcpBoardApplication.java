package com.yjcloud.asr.icp.board;

import android.app.Application;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.yjcloud.asr.icp.board.util.FileUtils;

import java.util.HashMap;

public class IcpBoardApplication extends Application {

    private static final String TAG = "IcpBoardApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        //initTBS();
    }

    public void initTBS() {
        boolean isInitTbs = QbSdk.canLoadX5(getApplicationContext());
        if (!isInitTbs || QbSdk.getTbsVersion(getApplicationContext()) < 46011) {
            FileUtils.copyAssets(getApplicationContext(), "046011_x5.tbs.apk", FileUtils.getTBSFileDir(getApplicationContext()).getPath() + "/046011_x5.tbs.apk");
        }

        HashMap<String, Object> map = new HashMap<>(2);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
    }

}
