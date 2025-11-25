package com.example.docknet.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docknet.R;
import com.example.docknet.model.Star;

public class StarRecyclerAdapter extends ListAdapter<Star, StarRecyclerAdapter.VH> {
    public interface OnStarClickListener { void onStarClick(int pos, Star star); }
    private final OnStarClickListener listener;

    public StarRecyclerAdapter(OnStarClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Star> DIFF = new DiffUtil.ItemCallback<Star>() {
        @Override public boolean areItemsTheSame(@NonNull Star oldItem, @NonNull Star newItem) { return oldItem.name.equals(newItem.name); }
        @Override public boolean areContentsTheSame(@NonNull Star oldItem, @NonNull Star newItem) { return oldItem.equals(newItem); }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.star_list_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Star star = getItem(position);
        holder.name.setText(star.name);
        if (star.resId != null) {
            Glide.with(holder.image.getContext()).load(star.resId).placeholder(R.drawable.star_1).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.star_1);
        }
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) listener.onStarClick(pos, star);
        });
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        public VH(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.star_list_image);
            name = itemView.findViewById(R.id.star_list_name);
        }
    }
}

