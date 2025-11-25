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
        StarRecyclerAdapter starAdapter = new StarRecyclerAdapter((pos, star) -> {
            // noop or implement preview later
        });
        starsRecycler.setLayoutManager(new LinearLayoutManager(activity));
        starsRecycler.setAdapter(starAdapter);
        starAdapter.submitList(StarRepository.getStars());
    }
}

