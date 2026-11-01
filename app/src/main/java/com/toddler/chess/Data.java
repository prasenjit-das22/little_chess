package com.toddler.chess;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads the word lists in assets/: one card per line, columns split by '|', '#' for comments. */
final class Data {
    private static Context app;
    private static final Map<String, List<String[]>> CACHE = new HashMap<>();

    private Data() {}

    static void init(Context context) {
        app = context.getApplicationContext();
    }

    static synchronized List<String[]> rows(String file) {
        List<String[]> rows = CACHE.get(file);
        if (rows != null) return rows;
        rows = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(app.getAssets().open(file), "UTF-8"));
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    rows.add(line.split("\\|", -1));
                }
            } finally {
                in.close();
            }
        } catch (IOException ignored) {
            // A missing list just gives an empty deck.
        }
        CACHE.put(file, rows);
        return rows;
    }
}
