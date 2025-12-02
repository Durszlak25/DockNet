package com.example.docknet.ui;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docknet.R;
import com.example.docknet.network.NetworkClient;
import com.example.docknet.network.RetrofitNetworkClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FactionController {
    private final AppCompatActivity activity;
    private final NetworkClient networkClient;

    public FactionController(AppCompatActivity activity) {
        this.activity = activity;
        this.networkClient = new RetrofitNetworkClient();
    }

    public void setup() {
        EditText input = activity.findViewById(R.id.faction_system_name);
        ProgressBar progress = activity.findViewById(R.id.faction_progress);
        RecyclerView recycler = activity.findViewById(R.id.faction_recycler);
        TextView empty = activity.findViewById(R.id.faction_empty);

        recycler.setLayoutManager(new LinearLayoutManager(activity));
        FactionAdapter adapter = new FactionAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final int debounceMs = 350;
        final Runnable[] last = new Runnable[1];

        input.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String q = s.toString().trim();
                if (last[0] != null) handler.removeCallbacks(last[0]);
                last[0] = () -> {
                    if (q.length() < 3) {
                        adapter.swap(new ArrayList<>());
                        empty.setVisibility(View.VISIBLE);
                        empty.setText(activity.getString(R.string.enter_at_least_3_chars));
                        return;
                    }
                    fetchFactionsFor(q, progress, adapter, empty);
                };
                handler.postDelayed(last[0], debounceMs);
            }
        });
    }

    private void fetchFactionsFor(String name, ProgressBar progress, FactionAdapter adapter, TextView empty) {
        empty.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String encPlus = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8.name());
                String encPct = encPlus.replace("+", "%20");

                String urlPct = "https://www.edsm.net/api-system-v1/factions?systemName=" + encPct;
                String resp = networkClient.performApiRequest(urlPct);
                List<Faction> list = new java.util.ArrayList<>();
                try {
                    list = parseFactions(resp);
                } catch (Exception pe) {
                    String urlPlus = "https://www.edsm.net/api-system-v1/factions?systemName=" + encPlus;
                    try {
                        String resp2 = networkClient.performApiRequest(urlPlus);
                        list = parseFactions(resp2);
                        resp = resp2;
                    } catch (Exception pe2) {
                        activity.runOnUiThread(() -> {
                            progress.setVisibility(View.GONE);
                            empty.setVisibility(View.VISIBLE);
                            empty.setText(activity.getString(R.string.parse_error_short));
                            empty.setOnClickListener(null);
                        });
                        return;
                    }
                }

                final List<Faction> finalList = list;
                final String finalResp = resp;
                activity.runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    adapter.swap(finalList);
                    empty.setVisibility(finalList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (finalList.isEmpty()) empty.setText(activity.getString(R.string.no_factions));
                    else empty.setOnClickListener(null);
                });
            } catch (IOException e) {
                activity.runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                    empty.setText(activity.getString(R.string.network_error, e.getMessage()));
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                    empty.setText(activity.getString(R.string.parse_error, e.getMessage()));
                });
            }
        }).start();

    }

    private List<Faction> parseFactions(String json) throws Exception {
        List<Faction> out = new ArrayList<>();
        if (json == null || json.isEmpty()) return out;
        JSONArray arr = null;
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) {
            JSONObject root = new JSONObject(trimmed);
            if (root.has("factions") && !root.isNull("factions")) {
                Object factionsObj = root.opt("factions");
                if (factionsObj instanceof JSONArray) {
                    arr = (JSONArray) factionsObj;
                } else if (factionsObj instanceof JSONObject) {
                    arr = new JSONArray();
                    arr.put(factionsObj);
                } else if (factionsObj instanceof String) {
                    String s = (String) factionsObj;
                    String st = s.trim();
                    if (st.startsWith("[")) arr = new JSONArray(st);
                    else if (st.startsWith("{")) { JSONArray tmp = new JSONArray(); tmp.put(new JSONObject(st)); arr = tmp; }
                }
            } else {
                java.util.Iterator<String> it = root.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    if (!root.isNull(key)) {
                        Object v = root.opt(key);
                        if (v instanceof JSONArray) { arr = (JSONArray) v; break; }
                    }
                }
            }
        } else if (trimmed.startsWith("[")) {
            arr = new JSONArray(trimmed);
        }
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Faction f = new Faction();
            f.id = o.optLong("id", 0);
            f.name = o.optString("name", "");
            f.allegiance = o.optString("allegiance", "");
            f.government = o.optString("government", "");
            f.influence = o.optDouble("influence", 0);
            f.state = o.optString("state", "");
            f.happiness = o.optString("happiness", "");
            out.add(f);
        }
        return out;
    }

    private static class Faction {
        long id;
        String name;
        String allegiance;
        String government;
        double influence;
        String state;
        String happiness;
    }

    private class FactionAdapter extends RecyclerView.Adapter<FactionAdapter.VH> {
        private List<Faction> items;
        FactionAdapter(List<Faction> items) { this.items = items; }
        void swap(List<Faction> list) { this.items = list; notifyDataSetChanged(); }
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.faction_list_item, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH holder, int position) {
            Faction f = items.get(position);
            holder.name.setText(f.name);
            holder.itemView.setOnClickListener(v -> showDetails(f));
        }
        @Override public int getItemCount() { return items != null ? items.size() : 0; }
        class VH extends RecyclerView.ViewHolder { TextView name; VH(android.view.View v){ super(v); name = v.findViewById(R.id.faction_name);} }
    }

    private void showDetails(Faction f) {
        hideKeyboard();
        if (f == null) return;
        android.view.LayoutInflater inflater = activity.getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.faction_detail, null);
        android.widget.TextView name = view.findViewById(R.id.detail_name);
        android.widget.TextView desc = view.findViewById(R.id.detail_desc);

        name.setText(f.name);
        StringBuilder sb = new StringBuilder();
        if (f.allegiance != null && !f.allegiance.isEmpty()) sb.append("Allegiance: ").append(f.allegiance).append("\n");
        if (f.government != null && !f.government.isEmpty()) sb.append("Government: ").append(f.government).append("\n");
        sb.append(String.format(java.util.Locale.US, "Influence: %.3f\n", f.influence));
        if (f.state != null && !f.state.isEmpty()) sb.append("State: ").append(f.state).append("\n");
        if (f.happiness != null && !f.happiness.isEmpty()) sb.append("Happiness: ").append(f.happiness).append("\n");
        desc.setText(sb.toString());

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

    private void hideKeyboard() {
        View v = activity.getCurrentFocus();
        if (v == null) v = activity.findViewById(android.R.id.content);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && v != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            v.clearFocus();
        }
    }
}
