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
import com.example.docknet.model.Ship;

public class ShipRecyclerAdapter extends ListAdapter<Ship, ShipRecyclerAdapter.VH> {
    public interface OnShipClickListener { void onShipClick(int pos, Ship ship); }
    private final OnShipClickListener listener;

    public ShipRecyclerAdapter(OnShipClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Ship> DIFF = new DiffUtil.ItemCallback<Ship>() {
        @Override public boolean areItemsTheSame(@NonNull Ship oldItem, @NonNull Ship newItem) { return oldItem.name.equals(newItem.name); }
        @Override public boolean areContentsTheSame(@NonNull Ship oldItem, @NonNull Ship newItem) { return oldItem.equals(newItem); }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ship_list_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Ship ship = getItem(position);
        holder.name.setText(ship.name);
        String desc = ship.description != null ? ship.description : "";
        if (ship.category != null && !ship.category.isEmpty()) desc = desc + "\n(" + ship.category + ")";
        holder.desc.setText(desc);
        if (ship.resId != null) {
            Glide.with(holder.image.getContext()).load(ship.resId).placeholder(R.drawable.star_1).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.star_1);
        }
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) listener.onShipClick(pos, ship);
        });
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, desc;
        public VH(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ship_image);
            name = itemView.findViewById(R.id.ship_name);
            desc = itemView.findViewById(R.id.ship_desc);
        }
    }
}
