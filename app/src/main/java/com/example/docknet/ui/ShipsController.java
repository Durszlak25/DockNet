package com.example.docknet.ui;

import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docknet.R;
import com.example.docknet.model.Ship;

import java.util.ArrayList;
import java.util.List;

public class ShipsController {
    private final AppCompatActivity activity;
    private com.example.docknet.ui.ShipRecyclerAdapter adapter;
    private List<Ship> allShips = new ArrayList<>();

    public ShipsController(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setup() {
        RecyclerView rv = activity.findViewById(R.id.ships_list_view);
        EditText search = activity.findViewById(R.id.ship_search);

        adapter = new com.example.docknet.ui.ShipRecyclerAdapter((pos, ship) -> {
            showDetails(ship);
        });
        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setAdapter(adapter);

        allShips = com.example.docknet.data.ShipRepository.getShips(activity);
        adapter.submitList(new ArrayList<>(allShips));

        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { filter(s.toString()); }
        });
    }

    private void filter(String q) {
        if (q == null || q.isEmpty()) { adapter.submitList(new ArrayList<>(allShips)); return; }
        String low = q.toLowerCase();
        List<Ship> filtered = new ArrayList<>();
        for (Ship s : allShips) {
            if (s.name != null && s.name.toLowerCase().contains(low)) filtered.add(s);
        }
        adapter.submitList(filtered);
    }

    private void showDetails(com.example.docknet.model.Ship ship) {
        android.view.LayoutInflater inflater = activity.getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.ship_detail, null);
        android.widget.ImageView img = view.findViewById(R.id.detail_image);
        android.widget.TextView name = view.findViewById(R.id.detail_name);
        android.widget.TextView desc = view.findViewById(R.id.detail_desc);
        name.setText(ship.name);
        desc.setText(ship.description);
        if (ship.resId != null) com.bumptech.glide.Glide.with(activity).load(ship.resId).into(img);
        else img.setImageResource(R.drawable.star_1);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setView(view)
                .create();
        dialog.show();
        android.view.View maybe = view.findViewWithTag("detail_ok");
        if (maybe instanceof android.widget.Button) {
            android.widget.Button ok = (android.widget.Button) maybe;
            ok.setOnClickListener(v -> dialog.dismiss());
        }
    }
}
