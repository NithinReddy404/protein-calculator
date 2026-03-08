package com.hitacal.infra;

import com.hitacal.model.FoodItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Loads USDA FoodData Central SR Legacy CSV data into the local food_items table.
 *
 * Data source: https://fdc.nal.usda.gov/download-foods (public domain)
 * We use the bundled sample CSV in resources; replace with full dataset for production.
 */
public class UsdaDataLoader {
    private static final Logger LOG = LoggerFactory.getLogger(UsdaDataLoader.class);
    private static final String SAMPLE_RESOURCE = "/com/hitacal/data/usda_sample.csv";

    public static boolean needsLoad(Connection conn) throws SQLException {
        try (var st = conn.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM food_items")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    /**
     * Load food data from bundled sample CSV.
     * Format: fdc_id, description, category, calories_100g, protein_100g, fat_100g, carb_100g, fiber_100g
     */
    public static int loadFromResources(Connection conn) throws Exception {
        LOG.info("Loading USDA food data from bundled sample...");
        InputStream is = UsdaDataLoader.class.getResourceAsStream(SAMPLE_RESOURCE);
        if (is == null) {
            LOG.warn("Bundled USDA sample not found. Loading built-in fallback data.");
            return loadFallbackData(conn);
        }
        return parseCsvAndInsert(conn, new InputStreamReader(is));
    }

    private static int parseCsvAndInsert(Connection conn, Reader reader) throws Exception {
        int count = 0;
        String insertSql = """
            INSERT OR IGNORE INTO food_items
            (fdc_id, description, category, calories_100g, protein_100g, fat_100g, carb_100g, fiber_100g, data_type)
            VALUES (?,?,?,?,?,?,?,?,'sr_legacy')
        """;
        conn.setAutoCommit(false);
        try (var ps = conn.prepareStatement(insertSql);
             var br = new BufferedReader(reader)) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                String[] parts = parseCsvLine(line);
                if (parts.length < 8) continue;
                try {
                    ps.setInt(1, Integer.parseInt(parts[0].trim()));
                    ps.setString(2, parts[1].trim());
                    ps.setString(3, parts[2].trim());
                    ps.setDouble(4, parseDouble(parts[3]));
                    ps.setDouble(5, parseDouble(parts[4]));
                    ps.setDouble(6, parseDouble(parts[5]));
                    ps.setDouble(7, parseDouble(parts[6]));
                    ps.setDouble(8, parseDouble(parts[7]));
                    ps.addBatch();
                    count++;
                    if (count % 500 == 0) { ps.executeBatch(); conn.commit(); }
                } catch (NumberFormatException e) {
                    // skip malformed rows
                }
            }
            ps.executeBatch();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
        LOG.info("Loaded {} food items.", count);
        return count;
    }

    private static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return 0.0; }
    }

    /** Minimal CSV parser handling quoted fields */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { fields.add(sb.toString()); sb.setLength(0); }
            else { sb.append(c); }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    /** Fallback: inserts ~50 common foods so the app is usable right away */
    private static int loadFallbackData(Connection conn) throws SQLException {
        Object[][] foods = {
            {1001, "Butter, salted", "Dairy", 717.0, 0.9, 81.1, 0.1, 0.0},
            {1002, "Butter, whipped", "Dairy", 719.0, 0.9, 81.1, 0.1, 0.0},
            {1004, "Cheese, blue", "Dairy", 353.0, 21.4, 28.7, 2.3, 0.0},
            {1005, "Cheese, brick", "Dairy", 371.0, 23.2, 29.7, 2.8, 0.0},
            {1009, "Cheese, cheddar", "Dairy", 403.0, 22.9, 33.1, 1.3, 0.0},
            {1011, "Cheese, cottage, 1% fat", "Dairy", 72.0, 12.4, 1.0, 2.7, 0.0},
            {1016, "Cheese, mozzarella, part skim", "Dairy", 254.0, 24.3, 15.9, 2.8, 0.0},
            {1022, "Cheese, parmesan", "Dairy", 392.0, 35.8, 25.8, 3.2, 0.0},
            {1077, "Milk, whole", "Dairy", 61.0, 3.2, 3.3, 4.8, 0.0},
            {1079, "Milk, 2% fat", "Dairy", 50.0, 3.3, 2.0, 4.9, 0.0},
            {1082, "Milk, skim", "Dairy", 34.0, 3.4, 0.2, 5.0, 0.0},
            {1123, "Egg, whole, raw", "Eggs", 143.0, 12.6, 9.5, 0.7, 0.0},
            {1124, "Egg, white, raw", "Eggs", 52.0, 10.9, 0.2, 0.7, 0.0},
            {5006, "Chicken, breast, raw", "Poultry", 120.0, 22.5, 2.6, 0.0, 0.0},
            {5013, "Chicken, thigh, raw", "Poultry", 177.0, 18.7, 10.9, 0.0, 0.0},
            {5064, "Turkey, breast, raw", "Poultry", 135.0, 19.1, 6.3, 0.0, 0.0},
            {9003, "Apple, raw", "Fruits", 52.0, 0.3, 0.2, 14.0, 2.4},
            {9010, "Apricots, raw", "Fruits", 48.0, 1.4, 0.4, 11.1, 2.0},
            {9021, "Avocado, raw", "Fruits", 160.0, 2.0, 14.7, 8.5, 6.7},
            {9040, "Banana, raw", "Fruits", 89.0, 1.1, 0.3, 22.8, 2.6},
            {9050, "Blueberries, raw", "Fruits", 57.0, 0.7, 0.3, 14.5, 2.4},
            {9070, "Cherries, sweet, raw", "Fruits", 63.0, 1.1, 0.2, 16.0, 2.1},
            {9087, "Dates, medjool", "Fruits", 277.0, 1.8, 0.2, 74.9, 6.7},
            {9094, "Grapes, red", "Fruits", 67.0, 0.6, 0.4, 17.2, 0.9},
            {9111, "Kiwi, raw", "Fruits", 61.0, 1.1, 0.5, 14.7, 3.0},
            {9150, "Mango, raw", "Fruits", 60.0, 0.8, 0.4, 15.0, 1.6},
            {9200, "Orange, raw", "Fruits", 47.0, 0.9, 0.1, 11.8, 2.4},
            {9236, "Peach, raw", "Fruits", 39.0, 0.9, 0.3, 9.5, 1.5},
            {9252, "Pear, raw", "Fruits", 57.0, 0.4, 0.1, 15.2, 3.1},
            {9279, "Pineapple, raw", "Fruits", 50.0, 0.5, 0.1, 13.1, 1.4},
            {9291, "Plum, raw", "Fruits", 46.0, 0.7, 0.3, 11.4, 1.4},
            {9316, "Strawberries, raw", "Fruits", 32.0, 0.7, 0.3, 7.7, 2.0},
            {9326, "Watermelon, raw", "Fruits", 30.0, 0.6, 0.2, 7.6, 0.4},
            {11090, "Broccoli, raw", "Vegetables", 34.0, 2.8, 0.4, 6.6, 2.6},
            {11109, "Carrot, raw", "Vegetables", 41.0, 0.9, 0.2, 9.6, 2.8},
            {11116, "Celery, raw", "Vegetables", 16.0, 0.7, 0.2, 3.0, 1.6},
            {11124, "Cucumber, raw", "Vegetables", 15.0, 0.7, 0.1, 3.6, 0.5},
            {11165, "Kale, raw", "Vegetables", 35.0, 2.9, 1.5, 4.4, 4.1},
            {11200, "Onion, raw", "Vegetables", 40.0, 1.1, 0.1, 9.3, 1.7},
            {11210, "Peas, green, raw", "Vegetables", 81.0, 5.4, 0.4, 14.5, 5.1},
            {11251, "Potato, raw", "Vegetables", 77.0, 2.0, 0.1, 17.5, 2.2},
            {11282, "Spinach, raw", "Vegetables", 23.0, 2.9, 0.4, 3.6, 2.2},
            {11304, "Tomato, red, raw", "Vegetables", 18.0, 0.9, 0.2, 3.9, 1.2},
            {20081, "Rice, white, cooked", "Grains", 130.0, 2.7, 0.3, 28.2, 0.4},
            {20137, "Rice, brown, cooked", "Grains", 123.0, 2.7, 1.0, 25.6, 1.8},
            {20080, "Bread, white", "Grains", 265.0, 9.0, 3.2, 49.0, 2.7},
            {20082, "Bread, whole wheat", "Grains", 247.0, 13.0, 3.5, 41.0, 7.0},
            {20040, "Oats, dry", "Grains", 379.0, 13.2, 6.9, 67.7, 10.1},
            {16057, "Lentils, cooked", "Legumes", 116.0, 9.0, 0.4, 20.1, 7.9},
            {16043, "Chickpeas, cooked", "Legumes", 164.0, 8.9, 2.6, 27.4, 7.6},
            {16006, "Almonds", "Nuts", 579.0, 21.2, 49.9, 21.6, 12.5},
            {16098, "Peanut butter", "Nuts", 588.0, 25.1, 50.4, 20.1, 6.0},
            {16097, "Peanuts, dry roasted", "Nuts", 585.0, 23.7, 49.7, 21.5, 8.0},
            {13004, "Beef, ground, 80% lean, raw", "Meats", 254.0, 17.2, 20.0, 0.0, 0.0},
            {13193, "Beef, sirloin, raw", "Meats", 207.0, 21.3, 13.0, 0.0, 0.0},
            {10006, "Pork, tenderloin, raw", "Meats", 143.0, 21.7, 5.7, 0.0, 0.0},
            {15076, "Salmon, Atlantic, raw", "Seafood", 208.0, 20.4, 13.4, 0.0, 0.0},
            {15086, "Tuna, yellowfin, raw", "Seafood", 109.0, 24.4, 1.0, 0.0, 0.0},
            {4053, "Olive oil", "Fats & Oils", 884.0, 0.0, 100.0, 0.0, 0.0},
            {4582, "Coconut oil", "Fats & Oils", 892.0, 0.0, 99.1, 0.0, 0.0},
            {19041, "Honey", "Sweeteners", 304.0, 0.3, 0.0, 82.4, 0.2},
            {19335, "Sugar, white", "Sweeteners", 387.0, 0.0, 0.0, 99.9, 0.0},
            {14209, "Coffee, brewed", "Beverages", 1.0, 0.3, 0.0, 0.0, 0.0},
            {14355, "Tea, green, brewed", "Beverages", 1.0, 0.2, 0.0, 0.2, 0.0},
        };

        String sql = """
            INSERT OR IGNORE INTO food_items
            (fdc_id, description, category, calories_100g, protein_100g, fat_100g, carb_100g, fiber_100g, data_type)
            VALUES (?,?,?,?,?,?,?,?,'sr_legacy')
        """;
        conn.setAutoCommit(false);
        try (var ps = conn.prepareStatement(sql)) {
            for (Object[] f : foods) {
                ps.setInt(1, (int) f[0]);
                ps.setString(2, (String) f[1]);
                ps.setString(3, (String) f[2]);
                ps.setDouble(4, (double) f[3]);
                ps.setDouble(5, (double) f[4]);
                ps.setDouble(6, (double) f[5]);
                ps.setDouble(7, (double) f[6]);
                ps.setDouble(8, (double) f[7]);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
        LOG.info("Loaded {} fallback food items.", foods.length);
        return foods.length;
    }
}
