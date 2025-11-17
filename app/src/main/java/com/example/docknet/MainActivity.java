package com.example.docknet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView result;
    private EditText searchList;
    private ImageView starImage;

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private final List<String> masterItems = new ArrayList<>();
    private final List<String> items = new ArrayList<>();

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private static final Map<String, Integer> starImageMap = new HashMap<>();

    static {
        // Zmapuj nazwy typów gwiazd na identyfikatory zasobów drawable.
        // Upewnij się, że masz pliki o tych nazwach (np. star_o.png) w res/drawable.
        starImageMap.put("o (blue-white) star", R.drawable.star_1);
        starImageMap.put("b (blue-white) star", R.drawable.star_2);
        starImageMap.put("b (blue-white super giant) star", R.drawable.star_2);
        starImageMap.put("a (blue-white) star", R.drawable.star_3);
        starImageMap.put("a (blue-white super giant) star", R.drawable.star_3);
        starImageMap.put("f (white) star", R.drawable.star_4);
        starImageMap.put("f (white super giant) star", R.drawable.star_4);
        starImageMap.put("g (white-yellow) star", R.drawable.star_5);
        starImageMap.put("g (white-yellow super giant) star", R.drawable.star_5);
        starImageMap.put("k (yellow-orange) star", R.drawable.star_6);
        starImageMap.put("k (yellow-orange giant) star", R.drawable.star_6);
        starImageMap.put("m (red dwarf) star", R.drawable.star_7);
        starImageMap.put("m (red giant) star", R.drawable.star_7);
        starImageMap.put("m (red super giant) star", R.drawable.star_7);
        starImageMap.put("l (brown dwarf) star", R.drawable.star_8);
        starImageMap.put("t (brown dwarf) star", R.drawable.star_9);
        starImageMap.put("y (brown dwarf) star", R.drawable.star_10);
        starImageMap.put("t tauri star", R.drawable.star_11);
        starImageMap.put("herbig ae/be star", R.drawable.star_12);
        starImageMap.put("wolf-rayet n star", R.drawable.star_22);
        starImageMap.put("wolf-rayet nc star", R.drawable.star_23);
        starImageMap.put("wolf-rayet c star", R.drawable.star_24);
        starImageMap.put("wolf-rayet o star", R.drawable.star_25);
        starImageMap.put("c star", R.drawable.star_32);
        starImageMap.put("cn star", R.drawable.star_33);
        starImageMap.put("cj star", R.drawable.star_42);
        starImageMap.put("ms-type star", R.drawable.star_42);
        starImageMap.put("s-type star", R.drawable.star_42);
        starImageMap.put("white dwarf (d) star", R.drawable.star_51);
        starImageMap.put("white dwarf (da) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dab) star", R.drawable.star_51);
        starImageMap.put("white dwarf (daz) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dav) star", R.drawable.star_51);
        starImageMap.put("white dwarf (db) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dbz) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dbv) star", R.drawable.star_51);
        starImageMap.put("white dwarf (do) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dov) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dq) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dc) star", R.drawable.star_51);
        starImageMap.put("white dwarf (dcv) star", R.drawable.star_51);
        starImageMap.put("neutron star", R.drawable.star_91);
        starImageMap.put("black hole", R.drawable.star_92);
        starImageMap.put("supermassive black hole", R.drawable.star_92);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        searchList = findViewById(R.id.searchList);
        recyclerView = findViewById(R.id.recycler_view);
        starImage = findViewById(R.id.star_image);

        setupRecyclerView();
        setupListeners();

        initSampleItems();
        resetAndShowMasterItems();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(items, (position, text) -> {
            getSystemInfo(text);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        searchList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nie jest potrzebne
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nie jest potrzebne
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterList(s.toString());
            }
        });
    }

    private void initSampleItems() {
        masterItems.clear();
        masterItems.add("Write down at least 3 chars");
    }

    private void resetAndShowMasterItems() {
        items.clear();
        items.addAll(masterItems);
        adapter.notifyDataSetChanged();
        starImage.setVisibility(View.GONE);
        result.setText("");
    }

    private void filterList(String name) {
        if (name.length() < 3) {
            resetAndShowMasterItems();
            return;
        }

        backgroundExecutor.execute(() -> {
            try {
                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
                String urlString = "https://www.edsm.net/api-v1/systems?systemName=" + encodedName + "&showInformation=1&showCoordinates=1&showPrimaryStar=1";
                String jsonResponse = performApiRequest(urlString);
                List<String> systemNames = parseSystemListFromJson(jsonResponse);

                runOnUiThread(() -> {
                    if (systemNames.isEmpty()) {
                        items.clear();
                        items.add("No systems found");
                    } else {
                        items.clear();
                        items.addAll(systemNames);
                    }
                    adapter.notifyDataSetChanged();
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    items.clear();
                    items.add("Error: " + e.getMessage());
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void getSystemInfo(String systemName) {
        backgroundExecutor.execute(() -> {
            try {
                String encodedName = URLEncoder.encode(systemName, StandardCharsets.UTF_8.toString());
                String urlString = "https://www.edsm.net/api-v1/system?systemName=" + encodedName + "&showInformation=1&showCoordinates=1&showPrimaryStar=1";
                String jsonResponse = performApiRequest(urlString);
                JSONObject systemObject = new JSONObject(jsonResponse);
                String displayText = buildDisplayFromJson(systemObject);

                JSONObject pso = systemObject.optJSONObject("primaryStar");
                String starType = pso != null ? pso.optString("type", "") : "";
                Integer imageResId = getStarImageResId(starType);

                runOnUiThread(() -> {
                    result.setText(displayText);
                    if (imageResId != null) {
                        starImage.setVisibility(View.VISIBLE);
                        Glide.with(MainActivity.this)
                                .load(imageResId)
                                .into(starImage);
                    } else {
                        starImage.setVisibility(View.GONE);
                    }
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> result.setText("Error fetching system details: " + e.getMessage()));
            }
        });
    }

    private String performApiRequest(String urlString) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return readStream(connection.getInputStream());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private List<String> parseSystemListFromJson(String jsonResponse) throws JSONException {
        List<String> systemNames = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonResponse);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject systemObject = jsonArray.getJSONObject(i);
            systemNames.add(systemObject.optString("name", ""));
        }
        return systemNames;
    }

    private String buildDisplayFromJson(JSONObject obj) {
        try {
            StringBuilder sb = new StringBuilder();
            appendTitle(sb, obj);
            appendStarInfo(sb, obj);
            appendCoordinates(sb, obj);
            appendSystemDetails(sb, obj);
            return sb.toString().trim();
        } catch (Exception e) {
            return "Invalid JSON format";
        }
    }

    private Integer getStarImageResId(String starType) {
        if (starType == null || starType.isEmpty()) {
            return null;
        }
        String lowerCaseType = starType.toLowerCase();
        return starImageMap.get(lowerCaseType);
    }

    // --- Helper methods for buildDisplayFromJson ---

    private void appendTitle(StringBuilder sb, JSONObject obj) {
        String sysName = obj.optString("name", "Unknown");
        JSONObject pso = obj.optJSONObject("primaryStar");
        String starType = pso != null ? pso.optString("type", "") : "";
        boolean scoopable = pso != null && pso.optBoolean("isScoopable", false);

        sb.append(sysName);
        if (!starType.isEmpty()) {
            sb.append(" — ").append(starType);
        }
        if (scoopable) {
            sb.append(" (scoopable)");
        }
        sb.append('\n');
    }

    private void appendStarInfo(StringBuilder sb, JSONObject obj) {
        String sysName = obj.optString("name", "Unknown");
        JSONObject pso = obj.optJSONObject("primaryStar");
        if (pso != null) {
            String starName = pso.optString("name", "");
            if (!starName.isEmpty() && !starName.equals(sysName)) {
                sb.append("Star: ").append(starName).append('\n');
            }
        }
    }

    private void appendCoordinates(StringBuilder sb, JSONObject obj) {
        JSONObject coords = obj.optJSONObject("coords");
        double x = coords != null ? coords.optDouble("x", 0) : 0;
        double y = coords != null ? coords.optDouble("y", 0) : 0;
        double z = coords != null ? coords.optDouble("z", 0) : 0;
        boolean coordsLocked = obj.optBoolean("coordsLocked", false);

        sb.append(String.format(Locale.US, "Coords: [%.2f, %.2f, %.2f]%s\n",
                x, y, z, coordsLocked ? " (locked)" : ""));
    }

    private void appendSystemDetails(StringBuilder sb, JSONObject obj) {
        JSONObject info = obj.optJSONObject("information");
        if (info == null) return;

        appendDetail(sb, "Allegiance", info.optString("allegiance"));
        appendDetail(sb, "Government", info.optString("government"));
        appendDetail(sb, "Faction", info.optString("faction"));
        appendDetail(sb, "Faction State", info.optString("factionState"));
        long population = info.optLong("population", -1);
        if (population >= 0) {
            sb.append("Population: ").append(String.format(Locale.US, "%,d", population)).append('\n');
        }
        appendDetail(sb, "Security", info.optString("security"));
        String economy = info.optString("economy");
        String secondEconomy = info.optString("secondEconomy");
        if (!economy.isEmpty()) {
            sb.append("Economy: ").append(economy);
            if (!secondEconomy.isEmpty()) {
                sb.append(" / ").append(secondEconomy);
            }
            sb.append('\n');
        }
        appendDetail(sb, "Reserve", info.optString("reserve"));
    }

    private void appendDetail(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append('\n');
        }
    }


    // --- Adapter & ViewHolder ---

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

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            final String text = data.get(position);
            holder.label.setText(text);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(holder.getAdapterPosition(), text);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class VH extends RecyclerView.ViewHolder {
            TextView label;
            public VH(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.item_text);
            }
        }
    }
}
