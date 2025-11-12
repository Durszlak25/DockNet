package com.example.docknet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText bar; // single-system search input
    TextView result;
    Button btn;

    EditText searchList;
    Button submitList;

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    // original master list (could be loaded from network or local DB)
    private final List<String> masterItems = new ArrayList<>();
    // currently displayed list (adapter data)
    private final List<String> items = new ArrayList<>();

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bar = findViewById(R.id.search_bar);
        result = findViewById(R.id.result);
        btn = findViewById(R.id.search_button);

        searchList = findViewById(R.id.searchList);
        submitList = findViewById(R.id.submitList);
        recyclerView = findViewById(R.id.recycler_view);

        // prepare RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(items, new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String text) {
                // when clicking an item, fill the single search bar and perform system lookup
                bar.setText(text);
                getSystemInfo(text);
            }
        });
        recyclerView.setAdapter(adapter);

        // populate masterItems with sample data (replace with real data source if you have one)
        initSampleItems();

        // initially show all
        items.clear();
        items.addAll(masterItems);
        adapter.notifyDataSetChanged();

        // filter/submit list button — simple case-insensitive substring filter
        submitList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String q = String.valueOf(searchList.getText()).trim();
                filterList(q);
            }
        });

        // existing single search button: exact system lookup
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String systemName = String.valueOf(bar.getText()).trim();
                if (!systemName.isEmpty()) {
                    getSystemInfo(systemName);
                } else {
                    Toast.makeText(MainActivity.this, "Enter a system name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initSampleItems() {
        // Replace or populate from a network source if you prefer.
        // These are example names; replace with your own data when available.
        String[] sample = new String[] {
                "Write down at least 3 chars"
        };
        masterItems.clear();
        for (String s : sample) masterItems.add(s);
    }

    private void filterList(String name) {
        items.clear();
//        if (query.isEmpty()) {
//            items.addAll(masterItems);
//        } else {
//            String ql = query.toLowerCase();
//            for (String s : masterItems) {
//                if (s.toLowerCase().contains(ql)) {
//                    items.add(s);
//                }
//            }
//        }

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection http = null;
                String display = "Unknown error";

                try {
                    String encoded = URLEncoder.encode(name.trim(), "UTF-8");
                    URL url = new URL(String.format(
                            "https://www.edsm.net/api-v1/systems?systemName=%s",
                            encoded
                    ));
                    http = (HttpURLConnection) url.openConnection();
                    http.setConnectTimeout(10000);
                    http.setReadTimeout(10000);

                    int code = http.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        StringBuilder jsonResponse = new StringBuilder();
                        try (java.io.InputStream inputStream = http.getInputStream();
                             java.io.BufferedReader reader = new java.io.BufferedReader(
                                     new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8)
                             )) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                jsonResponse.append(line);
                            }
                        }

                        String responseText = jsonResponse.toString();
                        display = buildDisplayFromJson(responseText);
                    } else {
                        display = code + " " + http.getResponseMessage();
                    }
                } catch (IOException e) {
                    display = "Network error: " + e.getMessage();
                } finally {
                    if (http != null) http.disconnect();
                }

                final String finalDisplay = display;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        masterItems.clear();
//                        JSONObject obj = new JSONObject(finalDisplay);
//                        for (String s : obj) masterItems.add(s);
                            Toast.makeText(this, "No matches", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            Toast.makeText(this, "No matches", Toast.LENGTH_SHORT).show();
        }
    }

    private void getSystemInfo(String systemName) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection http = null;
                String display = "Unknown error";

                try {
                    String encoded = URLEncoder.encode(systemName.trim(), "UTF-8");
                    URL url = new URL(String.format(
                            "https://www.edsm.net/api-v1/system?systemName=%s&showInformation=1&showCoordinates=1&showPrimaryStar=1",
                            encoded
                    ));
                    http = (HttpURLConnection) url.openConnection();
                    http.setConnectTimeout(10000);
                    http.setReadTimeout(10000);

                    int code = http.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        StringBuilder jsonResponse = new StringBuilder();
                        try (java.io.InputStream inputStream = http.getInputStream();
                             java.io.BufferedReader reader = new java.io.BufferedReader(
                                     new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8)
                             )) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                jsonResponse.append(line);
                            }
                        }

                        String responseText = jsonResponse.toString();
                        display = buildDisplayFromJson(responseText);
                    } else {
                        display = code + " " + http.getResponseMessage();
                    }
                } catch (IOException e) {
                    display = "Network error: " + e.getMessage();
                } finally {
                    if (http != null) http.disconnect();
                }

                final String finalDisplay = display;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(finalDisplay);
                    }
                });
            }
        });
    }

    private String buildDisplayFromJson(String responseText) {
        try {
            JSONObject obj = new JSONObject(responseText);

            // Name
            String sysName = obj.optString("name", "Unknown");

            // Coords
            JSONObject coords = obj.optJSONObject("coords");
            double x = coords != null ? coords.optDouble("x", 0) : 0;
            double y = coords != null ? coords.optDouble("y", 0) : 0;
            double z = coords != null ? coords.optDouble("z", 0) : 0;
            boolean coordsLocked = obj.optBoolean("coordsLocked", false);

            // Information
            JSONObject info = obj.optJSONObject("information");
            String allegiance = info != null ? info.optString("allegiance", "") : "";
            String government = info != null ? info.optString("government", "") : "";
            String faction = info != null ? info.optString("faction", "") : "";
            String factionState = info != null ? info.optString("factionState", "") : "";
            long population = info != null ? info.optLong("population", -1) : -1;
            String security = info != null ? info.optString("security", "") : "";
            String economy = info != null ? info.optString("economy", "") : "";
            String secondEconomy = info != null ? info.optString("secondEconomy", "") : "";
            String reserve = info != null ? info.optString("reserve", "") : "";

            // Primary star (as object per your sample)
            JSONObject pso = obj.optJSONObject("primaryStar");
            String starType = "";
            String starName = "";
            boolean scoopable = false;
            if (pso != null) {
                starType = pso.optString("type", "");
                starName = pso.optString("name", "");
                scoopable = pso.optBoolean("isScoopable", false);
            }

            StringBuilder sb = new StringBuilder();

            // Title line
            sb.append(sysName);
            if (!starType.isEmpty()) {
                sb.append(" — ").append(starType);
            }
            if (scoopable) {
                sb.append(" (scoopable)");
            }
            sb.append('\n');

            // Star name if present and different
            if (!starName.isEmpty() && !starName.equals(sysName)) {
                sb.append("Primary Star: ").append(starName).append('\n');
            }

            // Coords
            sb.append(String.format("Coords: [%.2f, %.2f, %.2f]%s\n",
                    x, y, z, coordsLocked ? " (locked)" : ""));

            // Compact info block (only show if present)
            if (!allegiance.isEmpty() || !government.isEmpty() || population >= 0) {
                if (!allegiance.isEmpty()) sb.append("Allegiance: ").append(allegiance).append('\n');
                if (!government.isEmpty()) sb.append("Government: ").append(government).append('\n');
                if (!faction.isEmpty()) sb.append("Faction: ").append(faction).append('\n');
                if (!factionState.isEmpty()) sb.append("Faction State: ").append(factionState).append('\n');
                if (population >= 0) sb.append("Population: ").append(String.format("%,d", population)).append('\n');
                if (!security.isEmpty()) sb.append("Security: ").append(security).append('\n');
                if (!economy.isEmpty()) {
                    sb.append("Economy: ").append(economy);
                    if (!secondEconomy.isEmpty()) sb.append(" / ").append(secondEconomy);
                    sb.append('\n');
                }
                if (!reserve.isEmpty()) sb.append("Reserve: ").append(reserve).append('\n');
            }

            return sb.toString().trim();
        } catch (JSONException e) {
            return "Invalid JSON format";
        }
    }

    // Adapter & ViewHolder as inner classes to keep everything in this file
    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.VH> {

        public interface OnItemClickListener {
            void onItemClick(int position, String text);
        }

        private final List<String> data;
        private final OnItemClickListener listener;

        public MyAdapter(List<String> data, OnItemClickListener listener) {
            this.data = data;
            this.listener = listener;
        }

        public static class VH extends RecyclerView.ViewHolder {
            TextView label;
            public VH(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.item_text);
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, final int position) {
            final String text = data.get(position);
            holder.label.setText(text);

            holder.itemView.setClickable(true);
            holder.itemView.setFocusable(true);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onItemClick(position, text);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}