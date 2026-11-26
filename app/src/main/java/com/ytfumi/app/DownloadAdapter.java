package com.ytfumi.app;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private final Context context;
    private final List<RecentItem> items;

    public DownloadAdapter(Context context, List<RecentItem> items) {
        this.context = context;
        this.items = items;
    }

    public List<RecentItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentItem item = items.get(position);
        String url = item.getUrl();
        holder.title.setText(TextUtils.isEmpty(url) ? "Unknown" : url);
        holder.subtitle.setText(String.format("%s - %s", item.getFormat().toUpperCase(), item.getQuality()));
        holder.status.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(item.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            subtitle = itemView.findViewById(R.id.itemSubtitle);
            status = itemView.findViewById(R.id.itemStatus);
        }
    }
}