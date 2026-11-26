package com.ytfumi.app;

public class RecentItem {
    private final String url;
    private final String format;
    private final String quality;
    private final String outputPath;
    private final long timestamp;

    public RecentItem(String url, String format, String quality, String outputPath) {
        this.url = url;
        this.format = format;
        this.quality = quality;
        this.outputPath = outputPath;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUrl() { return url; }
    public String getFormat() { return format; }
    public String getQuality() { return quality; }
    public String getOutputPath() { return outputPath; }
    public long getTimestamp() { return timestamp; }
}
