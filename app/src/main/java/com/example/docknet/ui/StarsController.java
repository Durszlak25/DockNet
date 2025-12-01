package com.example.docknet.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docknet.R;
import com.example.docknet.data.StarRepository;

public class StarsController {
    private final AppCompatActivity activity;

    public StarsController(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setup() {
        RecyclerView starsRecycler = activity.findViewById(R.id.stars_list_view);
        final StarRecyclerAdapter starAdapter = new StarRecyclerAdapter((pos, star) -> {
        });
        starsRecycler.setLayoutManager(new LinearLayoutManager(activity));
        starsRecycler.setAdapter(starAdapter);
        starAdapter.submitList(StarRepository.getStars());
    }
}
