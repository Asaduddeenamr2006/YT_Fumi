package com.ytfumi.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST = 1;

    private EditText urlEditText;
    private RadioButton mp4Radio, mp3Radio;
    private RadioGroup formatRadioGroup;
    private Spinner qualitySpinner;
    private Button pasteButton, downloadButton;
    private ProgressBar progressBar;
    private TextView statusTextView, fileNameTextView;
    private TextView engineStatusTextView, engineLogTextView;
    private Button initEngineButton, updateEngineButton;
    private RecyclerView recentRecyclerView;
    private View downloadRootView, enginesRootView;
    private LinearLayoutManager linearLayoutManager;

    private DownloadAdapter downloadAdapter;

    private String currentFormat = "mp4";
    private String currentQuality = "Best";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasStoragePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
        }

        initViews();
        setupTabs();
        setupFormatToggle();
        setupPasteButton();
        setupDownloadButton();
        setupEngineButtons();
        setupRecentDownloads();
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void initViews() {
        urlEditText = findViewById(R.id.urlEditText);
        mp4Radio = findViewById(R.id.mp4Radio);
        mp3Radio = findViewById(R.id.mp3Radio);
        formatRadioGroup = findViewById(R.id.formatRadioGroup);
        qualitySpinner = findViewById(R.id.qualitySpinner);
        pasteButton = findViewById(R.id.pasteButton);
        downloadButton = findViewById(R.id.downloadButton);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        engineStatusTextView = findViewById(R.id.engineStatusText);
        initEngineButton = findViewById(R.id.initEngineButton);
        updateEngineButton = findViewById(R.id.updateEngineButton);
        engineLogTextView = findViewById(R.id.engineLogText);
        recentRecyclerView = findViewById(R.id.recentRecyclerView);
        downloadRootView = findViewById(R.id.downloadScroll);
        enginesRootView = findViewById(R.id.enginesRoot);

        linearLayoutManager = new LinearLayoutManager(this);
        recentRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_download));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_engines));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIndex = tab.getPosition();
                if (tabIndex == 0) {
                    downloadRootView.setVisibility(View.VISIBLE);
                    enginesRootView.setVisibility(View.GONE);
                } else {
                    downloadRootView.setVisibility(View.GONE);
                    enginesRootView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupFormatToggle() {
        formatRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.mp4Radio) {
                currentFormat = "mp4";
                updateQualitySpinnerMp4();
                ((TextView) findViewById(R.id.qualityLabel)).setText(R.string.quality_best);
            } else if (checkedId == R.id.mp3Radio) {
                currentFormat = "mp3";
                updateQualitySpinnerMp3();
                ((TextView) findViewById(R.id.qualityLabel)).setText(R.string.quality_320k);
            }
        });
        if (mp4Radio.isChecked()) {
            currentFormat = "mp4";
            updateQualitySpinnerMp4();
        } else {
            currentFormat = "mp3";
            updateQualitySpinnerMp3();
        }
    }

    private void updateQualitySpinnerMp4() {
        List<String> qualities = Arrays.asList("Best", "1080p", "720p", "480p");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, qualities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(adapter);
        qualitySpinner.setSelection(0);
        currentQuality = "Best";
    }

    private void updateQualitySpinnerMp3() {
        List<String> qualities = Arrays.asList("320 kbps", "192 kbps", "128 kbps");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, qualities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(adapter);
        qualitySpinner.setSelection(0);
        currentQuality = "320 kbps";
    }

    private void setupPasteButton() {
        pasteButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String text = item.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    urlEditText.setText(text);
                    Toast.makeText(MainActivity.this, R.string.url_pasted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void setupDownloadButton() {
        downloadButton.setOnClickListener(v -> downloadSelectedMedia());
    }

    private void downloadSelectedMedia() {
        String url = urlEditText.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.error_empty_url, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText(getString(R.string.status_downloading));

        DownloadHelper.DownloadCallback callback = new DownloadHelper.DownloadCallback() {
            @Override
            public void onProgress(float progress, long etaInSeconds, String line) {
                runOnUiThread(() -> {
                    progressBar.setProgress(Math.round(progress));
                    statusTextView.setText(line);
                });
            }

            @Override
            public void onComplete(String outputPath) {
                runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    statusTextView.setText(R.string.status_complete);
                    fileNameTextView.setText("Saved to: " + outputPath);
                    addRecentDownload(url, currentFormat, currentQuality);
                    Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusTextView.setText(R.string.status_error);
                    fileNameTextView.setText("");
                    Toast.makeText(MainActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        };
        DownloadHelper.startDownload(MainActivity.this, url, currentFormat, currentQuality, callback);
    }

    private void addRecentDownload(String url, String format, String quality) {
        RecentItem item = new RecentItem(url, format, quality, DownloadHelper.getOutputDirectoryPath(this));
        List<RecentItem> items = downloadAdapter.getItems();
        items.add(0, item);
        if (items.size() > 10) {
            items.remove(items.size() - 1);
        }
        downloadAdapter.notifyDataSetChanged();
    }

    private void setupEngineButtons() {
        initEngineButton.setOnClickListener(v -> {
            EngineUpdater.initializeEngine(this, new EngineUpdater.EngineCallback() {
                @Override
                public void onProgress(String message) {
                    engineStatusTextView.setText(message);
                    engineLogTextView.append(message + "\n");
                }

                @Override
                public void onComplete(String message) {
                    engineStatusTextView.setText(message);
                    engineLogTextView.append(message + "\n");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String error) {
                    engineStatusTextView.setText("Error: " + error);
                    engineLogTextView.append("ERROR: " + error + "\n");
                    Toast.makeText(MainActivity.this, "Engine init failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        updateEngineButton.setOnClickListener(v -> {
            EngineUpdater.updateEngine(this, new EngineUpdater.EngineCallback() {
                @Override
                public void onProgress(String message) {
                    engineStatusTextView.setText(message);
                    engineLogTextView.append(message + "\n");
                }

                @Override
                public void onComplete(String message) {
                    engineStatusTextView.setText(message);
                    engineLogTextView.append(message + "\n");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String error) {
                    engineStatusTextView.setText("Error: " + error);
                    engineLogTextView.append("ERROR: " + error + "\n");
                    Toast.makeText(MainActivity.this, "Engine update failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setupRecentDownloads() {
        List<RecentItem> items = new ArrayList<>();
        downloadAdapter = new DownloadAdapter(this, items);
        recentRecyclerView.setAdapter(downloadAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}