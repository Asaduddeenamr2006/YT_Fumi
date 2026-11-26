package com.ytfumi.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import java.io.File;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class DownloadHelper {

    private static final String TAG = "DownloadHelper";
    private static final String PROCESS_ID = "ytfumi_download";

    public interface DownloadCallback {
        void onProgress(float progress, long etaInSeconds, String line);
        void onComplete(String outputPath);
        void onError(String error);
    }

    public static void startDownload(Context context, String url, String format, String quality, DownloadCallback callback) {
        final YoutubeDLRequest request = buildRequest(context, url, format, quality);

        Function3<Float, Long, String, Unit> progressCallback = (progress, etaInSeconds, line) -> {
            callback.onProgress(progress, etaInSeconds, line);
            return Unit.INSTANCE;
        };

        new Thread(() -> {
            try {
                YoutubeDLResponse response = YoutubeDL.getInstance().execute(request, PROCESS_ID, progressCallback);

                if (response.exitCode == 0) {
                    String outputPath = getOutputDirectory(context).getAbsolutePath();
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onComplete(outputPath));
                } else {
                    String errorMsg = response.err.isEmpty() ? response.out : response.err;
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (YoutubeDLException e) {
                Log.e(TAG, "Download failed", e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error", e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    public static void cancelDownload() {
        try {
            YoutubeDL.getInstance().destroyProcessById(PROCESS_ID);
        } catch (Exception e) {
            Log.w(TAG, "Cancel failed", e);
        }
    }

    private static YoutubeDLRequest buildRequest(Context context, String url, String format, String quality) {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File outputDir = getOutputDirectory(context);
        String outputPattern = outputDir.getAbsolutePath() + "/%(title)s.%(ext)s";
        request.addOption("--no-mtime");
        request.addOption("-o", outputPattern);

        if ("mp4".equals(format)) {
            switch (quality) {
                case "Best":
                    request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
                    break;
                case "1080p":
                    request.addOption("-f", "bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[height<=1080]/best");
                    break;
                case "720p":
                    request.addOption("-f", "bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720]/best");
                    break;
                case "480p":
                    request.addOption("-f", "bestvideo[height<=480][ext=mp4]+bestaudio[ext=m4a]/best[height<=480]/best");
                    break;
                default:
                    request.addOption("-f", "best[ext=mp4]/best");
                    break;
            }
        } else if ("mp3".equals(format)) {
            request.addOption("-x");
            request.addOption("--audio-format");
            request.addOption("mp3");
            switch (quality) {
                case "320 kbps":
                    request.addOption("--audio-quality");
                    request.addOption("320K");
                    break;
                case "192 kbps":
                    request.addOption("--audio-quality");
                    request.addOption("192K");
                    break;
                case "128 kbps":
                    request.addOption("--audio-quality");
                    request.addOption("128K");
                    break;
                default:
                    request.addOption("--audio-quality");
                    request.addOption("128K");
                    break;
            }
        } else {
            request.addOption("-f", "best");
        }

        return request;
    }

    private static File getOutputDirectory(Context context) {
        File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File ytDir = new File(downloadsDir, "ytfumi");
        if (!ytDir.exists()) {
            ytDir.mkdirs();
        }
        return ytDir;
    }

    public static String getOutputDirectoryPath(Context context) {
        return getOutputDirectory(context).getAbsolutePath();
    }
}