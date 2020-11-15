package com.kookminuniv.team17.hotplace;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;

public class CacheClear {
    public void clearCache(Activity activity) {
        final File cacheDirFile = activity.getCacheDir();
            if (null != cacheDirFile && cacheDirFile.isDirectory()) {
            clearSubCacheFiles(cacheDirFile);
        }
    }
    private void clearSubCacheFiles(File cacheDirFile) {
        if (null == cacheDirFile || cacheDirFile.isFile()) {
            return;
        }
        for (File cacheFile : cacheDirFile.listFiles()) {
            if (cacheFile.isFile()) {
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
            } else {
                clearSubCacheFiles(cacheFile);
            }
        }
    }
}
