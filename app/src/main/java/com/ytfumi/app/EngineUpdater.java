package com.ytfumi.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel;
import com.yausername.youtubedl_android.YoutubeDL.UpdateStatus;
import com.yausername.youtubedl_android.YoutubeDLException;

public class EngineUpdater {

    private static final String TAG = "EngineUpdater";

    public interface EngineCallback {
        void onProgress(String message);
        void onComplete(String message);
        void onError(String error);
    }

    public static void initializeEngine(Context context, final EngineCallback callback) {
        new Thread(() -> {
            try {
                YoutubeDL.getInstance().init(context);
                android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onProgress("Engine initializing..."));
                android.os.Handler mainHandler2 = new android.os.Handler(Looper.getMainLooper());
                mainHandler2.post(() -> callback.onComplete("Engine initialized"));
            } catch (YoutubeDLException e) {
                Log.e(TAG, "Engine initialization failed", e);
                android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Initialization failed: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during initialization", e);
                android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Unexpected error: " + e.getMessage()));
            }
        }).start();
    }

    public static void updateEngine(Context context, final EngineCallback callback) {
        new Thread(() -> {
            try {
                UpdateStatus status = YoutubeDL.getInstance().updateYoutubeDL(context, UpdateChannel._STABLE);
                android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());
                if (status == UpdateStatus.DONE) {
                    mainHandler.post(() -> callback.onComplete("Engine updated successfully"));
                } else if (status == UpdateStatus.ALREADY_UP_TO_DATE) {
                    mainHandler.post(() -> callback.onComplete("Engine already up to date"));
                } else {
                    mainHandler.post(() -> callback.onComplete("Update check completed"));
                }
            } catch (YoutubeDLException e) {
                Log.e(TAG, "Engine update failed", e);
                android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Update failed: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during update", e);
                android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Unexpected error: " + e.getMessage()));
            }
        }).start();
    }
}