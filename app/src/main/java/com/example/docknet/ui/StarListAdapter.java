package com.example.docknet.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.docknet.R;

import java.util.List;
import java.util.Map;

public class StarListAdapter extends ArrayAdapter<Map.Entry<String, Integer>> {

    private static class ViewHolder {
        ImageView starImageView;
        TextView starNameView;
    }

    public StarListAdapter(Context context, List<Map.Entry<String, Integer>> stars) {
        super(context, 0, stars);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.star_list_item, parent, false);
            holder = new ViewHolder();
            holder.starImageView = convertView.findViewById(R.id.star_list_image);
            holder.starNameView = convertView.findViewById(R.id.star_list_name);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map.Entry<String, Integer> entry = getItem(position);

        if (entry != null) {
            holder.starNameView.setText(entry.getKey());
            holder.starImageView.setImageResource(entry.getValue());
            AnimationHelper.setupImageAnimation(holder.starImageView);
        }

        return convertView;
    }
}

