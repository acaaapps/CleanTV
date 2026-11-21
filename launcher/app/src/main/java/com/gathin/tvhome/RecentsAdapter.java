package com.gathin.tvhome;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {
    public interface Listener {
        void onRecentsClick(AppAdapter.Entry entry);
        void onRecentsLongClick(AppAdapter.Entry entry, View card);
        void onRecentsFocus(AppAdapter.Entry entry, View card);
        boolean onRecentsUp();
        boolean onRecentsDown(int position);
    }

    private final List<AppAdapter.Entry> items = new ArrayList<>();
    private final Listener listener;

    public RecentsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AppAdapter.Entry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public AppAdapter.Entry getItem(int position) {
        return position >= 0 && position < items.size() ? items.get(position) : null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recents_app, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AppAdapter.Entry e = items.get(position);
        if (e == null) return;

        h.name.setText(e.label);
        h.card.setOnClickListener(v -> {
            if (listener != null) listener.onRecentsClick(e);
        });
        h.card.setOnLongClickListener(v -> {
            if (listener != null) listener.onRecentsLongClick(e, v);
            return true;
        });

        h.card.setOnFocusChangeListener((v, hasFocus) -> {
            h.name.setAlpha(hasFocus ? 1f : 0f);
            h.name.setTranslationY(hasFocus ? 0f : -4f);
            v.setScaleX(hasFocus ? 1.08f : 1f);
            v.setScaleY(hasFocus ? 1.08f : 1f);
            if (hasFocus && listener != null) {
                listener.onRecentsFocus(e, v);
            }
        });

        h.card.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) pos = position;

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (listener != null) return listener.onRecentsUp();
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (listener != null) return listener.onRecentsDown(pos);
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (pos == 0) return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (pos >= getItemCount() - 1) return true;
            }
            return false;
        });

        if (e.banner != null) {
            h.banner.setImageDrawable(e.banner);
            h.banner.setVisibility(View.VISIBLE);
            h.icon.setVisibility(View.GONE);
            h.label.setVisibility(View.GONE);
        } else {
            h.banner.setVisibility(View.GONE);
            h.icon.setImageDrawable(e.icon);
            h.icon.setVisibility(View.VISIBLE);
            h.label.setText(e.label);
            h.label.setVisibility(View.VISIBLE);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View card;
        final ImageView banner;
        final ImageView icon;
        final TextView label;
        final TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            card.setClipToOutline(true);
            banner = itemView.findViewById(R.id.banner);
            icon = itemView.findViewById(R.id.icon);
            label = itemView.findViewById(R.id.label);
            name = itemView.findViewById(R.id.name);
        }
    }
}
