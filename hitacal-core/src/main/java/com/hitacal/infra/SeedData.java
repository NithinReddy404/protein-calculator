package com.hitacal.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class SeedData {
    private static final Logger log = LoggerFactory.getLogger(SeedData.class);

    public static void seed() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        seedAdmin(conn);
        seedMotivations(conn);
        seedCitations(conn);
        seedSampleFoods(conn);
    }

    private static void seedAdmin(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE is_admin=1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String hash = BcryptUtil.hash("changeme");
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users(username,display_name,password_hash,is_admin,created_at) VALUES(?,?,?,1,?)")) {
            ps.setString(1, "admin"); ps.setString(2, "Admin");
            ps.setString(3, hash); ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
            log.info("Admin user created (username=admin, password=changeme)");
        }
    }

    private static void seedMotivations(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM motivations")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String[][] t = {
            {"Every bite logged is a step closer, {name}. Keep it up!","0","0","DAILY"},
            {"You showed up today, {name}. That''s already a win.","0","0","DAILY"},
            {"Tracking takes courage, {name}. You''ve got this.","0","0","DAILY"},
            {"Small steps, big shell. One log at a time, {name}!","0","0","DAILY"},
            {"Almost there, {name}! Your goal can smell you coming.","0","75","GOAL"},
            {"You''re in the home stretch, {name}. The turtle is rooting for you!","0","75","GOAL"},
            {"{name}, 7 days in a row! Your shell is getting shinier!","7","0","STREAK"},
            {"14-day streak, {name}! You''re unstoppable.","14","0","STREAK"},
            {"30 days, {name}! You''ve officially out-turtled the turtle.","30","0","STREAK"},
            {"You hit your goal today, {name}! Happy dance time.","0","100","CHEERING"},
            {"Goal crushed! Every day like this gets you there faster, {name}.","0","100","CHEERING"},
        };
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO motivations(template,min_streak,min_progress_pct,tag) VALUES(?,?,?,?)")) {
            for (String[] r : t) {
                ps.setString(1, r[0]); ps.setInt(2, Integer.parseInt(r[1]));
                ps.setDouble(3, Double.parseDouble(r[2])); ps.setString(4, r[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("Motivations seeded.");
    }

    private static void seedCitations(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM citations")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String[][] c = {
            {"Comparison of weight-loss diets with different compositions of fat, protein, and carbohydrates",
             "Sacks FM, Bray GA, Carey VJ, et al.","New England Journal of Medicine","2009",
             "10.1056/NEJMoa0804748","https://doi.org/10.1056/NEJMoa0804748"},
            {"Quantification of the effect of energy imbalance on bodyweight",
             "Hall KD et al.","The Lancet","2011",
             "10.1016/S0140-6736(11)60812-X","https://doi.org/10.1016/S0140-6736(11)60812-X"},
        };
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO citations(title,authors,journal,year,doi,url) VALUES(?,?,?,?,?,?)")) {
            for (String[] r : c) {
                ps.setString(1,r[0]); ps.setString(2,r[1]); ps.setString(3,r[2]);
                ps.setInt(4,Integer.parseInt(r[3])); ps.setString(5,r[4]); ps.setString(6,r[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("Citations seeded.");
    }

    private static void seedSampleFoods(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM food_items")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        Object[][] foods = {
            {1001,"Butter, salted","Dairy",717.0,0.9,81.1,0.1,0.0},
            {1077,"Milk, whole, 3.25%","Dairy",61.0,3.2,3.3,4.8,0.0},
            {1082,"Yogurt, plain, whole milk","Dairy",61.0,3.5,3.3,4.7,0.0},
            {1123,"Egg, whole, raw","Eggs",143.0,12.6,9.5,0.7,0.0},
            {1004,"Cheese, cheddar","Dairy",403.0,24.9,33.1,1.3,0.0},
            {1009,"Cheese, mozzarella","Dairy",280.0,28.1,17.1,2.2,0.0},
            {9003,"Apples, raw, with skin","Fruits",52.0,0.3,0.2,13.8,2.4},
            {9040,"Bananas, raw","Fruits",89.0,1.1,0.3,22.8,2.6},
            {9050,"Blueberries, raw","Fruits",57.0,0.7,0.3,14.5,2.4},
            {9200,"Oranges, raw","Fruits",47.0,0.9,0.1,11.8,2.4},
            {11111,"Avocado, raw","Fruits",160.0,2.0,14.7,8.5,6.7},
            {11090,"Broccoli, raw","Vegetables",34.0,2.8,0.4,6.6,2.6},
            {11124,"Carrots, raw","Vegetables",41.0,0.9,0.2,9.6,2.8},
            {11209,"Cucumber, with peel, raw","Vegetables",15.0,0.7,0.1,3.6,0.5},
            {11529,"Tomatoes, red, raw","Vegetables",18.0,0.9,0.2,3.9,1.2},
            {11457,"Spinach, raw","Vegetables",23.0,2.9,0.4,3.6,2.2},
            {20081,"Bread, whole-wheat","Grains",247.0,13.0,3.4,41.0,6.0},
            {20082,"Bread, white","Grains",267.0,9.0,3.6,49.0,2.7},
            {20137,"Rice, white, cooked","Grains",130.0,2.7,0.3,28.6,0.4},
            {20040,"Oatmeal, cooked","Grains",71.0,2.5,1.5,12.0,1.7},
            {20100,"Pasta, cooked","Grains",158.0,5.8,0.9,30.9,1.8},
            {5064,"Chicken breast, roasted","Poultry",165.0,31.0,3.6,0.0,0.0},
            {5095,"Chicken thigh, roasted","Poultry",209.0,26.0,10.9,0.0,0.0},
            {13397,"Beef, ground, 85% lean, cooked","Beef",215.0,26.1,11.8,0.0,0.0},
            {15236,"Salmon, Atlantic, cooked","Fish",206.0,20.4,12.4,0.0,0.0},
            {15264,"Tuna, light, canned in water","Fish",116.0,25.5,0.8,0.0,0.0},
            {16057,"Lentils, cooked","Legumes",116.0,9.0,0.4,20.1,7.9},
            {16043,"Chickpeas, cooked","Legumes",164.0,8.9,2.6,27.4,7.6},
            {16084,"Kidney beans, cooked","Legumes",127.0,8.7,0.5,22.8,6.4},
            {12061,"Almonds","Nuts",579.0,21.2,49.9,21.6,12.5},
            {12155,"Walnuts","Nuts",654.0,15.2,65.2,13.7,6.7},
            {19297,"Peanut butter, smooth","Nuts",588.0,25.1,50.4,20.1,5.9},
            {19057,"Chocolate, dark (70-85% cacao)","Sweets",598.0,7.8,42.6,45.9,10.9},
            {14209,"Coffee, brewed","Beverages",1.0,0.3,0.0,0.0,0.0},
            {14429,"Water, plain","Beverages",0.0,0.0,0.0,0.0,0.0},
            {14400,"Orange juice, fresh","Beverages",45.0,0.7,0.2,10.4,0.2},
            {14355,"Tea, brewed","Beverages",1.0,0.0,0.0,0.3,0.0},
            {2047,"Salt, table","Spices",0.0,0.0,0.0,0.0,0.0},
            {2009,"Cinnamon, ground","Spices",247.0,4.0,1.2,80.6,53.1},
            {11282,"Onions, raw","Vegetables",40.0,1.1,0.1,9.3,1.7},
        };
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO food_items(fdc_id,description,category,calories_100g,protein_100g,fat_100g,carb_100g,fiber_100g,data_type) VALUES(?,?,?,?,?,?,?,?,?)")) {
            for (Object[] f : foods) {
                ps.setInt(1,(Integer)f[0]); ps.setString(2,(String)f[1]); ps.setString(3,(String)f[2]);
                ps.setDouble(4,(Double)f[3]); ps.setDouble(5,(Double)f[4]);
                ps.setDouble(6,(Double)f[5]); ps.setDouble(7,(Double)f[6]);
                ps.setDouble(8,(Double)f[7]); ps.setString(9,"builtin");
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("Sample foods seeded (40 items).");
    }
}
