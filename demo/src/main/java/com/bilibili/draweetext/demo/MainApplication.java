package com.bilibili.draweetext.demo;

import android.app.Application;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

/**
 * @author yrom.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // initialize fresco with enabled webp
        Fresco.initialize(this, ImagePipelineConfig.newBuilder(this)
                .experiment()
                .setWebpSupportEnabled(true)
                .build());
        // for debug
        if (BuildConfig.DEBUG) {
            FLog.setMinimumLoggingLevel(Log.VERBOSE);
        }
    }
}
