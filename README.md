# 🐢 hitacal AI — Personalized Calorie Tracker

A 100% Java 17 + JavaFX desktop application for daily calorie, protein, and fat tracking.
Local-first (no cloud), SQLite storage, Apache POI Excel export, coffee-colored turtle UI.

---

## ✅ Prerequisites

| Tool | Required Version | Download |
|------|-----------------|---------|
| **Java JDK** | 17 or 21 (LTS) | https://adoptium.net |
| **Gradle** | 8.7 (wrapper auto-downloads) | bundled via wrapper |

No database server. No API keys. No cloud account required.

---

## 🚀 Running the App

### First-time setup (Windows / macOS / Linux)

**Step 1 — Bootstrap the Gradle wrapper** (one-time, needs internet):
```bash
# macOS / Linux
cd hitacal-ai
gradle wrapper --gradle-version 8.7
chmod +x gradlew

# Windows
cd hitacal-ai
gradle wrapper --gradle-version 8.7
```
> If you don't have Gradle installed globally, download it from https://gradle.org/install
> OR use IntelliJ / Eclipse which includes Gradle support.

**Step 2 — Run**:
```bash
# macOS / Linux
./gradlew :hitacal-app:run

# Windows
gradlew.bat :hitacal-app:run
```

On first launch:
- SQLite database is created at: `~/.hitacal/hitacal.db`
- 40 common foods are pre-loaded (no download needed)
- Default admin account: **username=admin, password=changeme**
- ⚠ Change the admin password on first login!

---

## 🖥 Opening in IntelliJ IDEA (recommended)

1. File → Open → select the `hitacal-ai` folder
2. IntelliJ detects the Gradle project automatically
3. Wait for indexing to finish
4. Run → Edit Configurations → Application
   - Main class: `com.hitacal.MainApp`
   - Module: `hitacal-app.main`
5. Press ▶ Run

---

## 🖥 Opening in Eclipse

1. File → Import → Gradle → Existing Gradle Project
2. Select the `hitacal-ai` folder
3. Right-click `MainApp.java` → Run As → Java Application

---

## 📁 Project Structure

```
hitacal-ai/
├── hitacal-core/          ← Models, Repositories, Services, DB layer
├── hitacal-ui/            ← JavaFX Controllers, FXML, CSS, TurtlePane
├── hitacal-app/           ← MainApp entry point
├── build.gradle.kts       ← Root build with all dependency versions
└── settings.gradle.kts    ← Multi-module project settings
```

---

## 🔐 Default Accounts

| Username | Password   | Role  |
|----------|-----------|-------|
| admin    | changeme  | Admin |

Create regular user accounts from the Login screen → "New User",
or from the Admin Dashboard → "Create User".

---

## 📊 Excel Export

Excel workbooks are saved to: `~/hitacal_exports/{username}_nutrition.xlsx`

Each file contains:
- Monthly sheets (one per calendar month)
- Summary sheet with averages and goal tracking
- Coffee-brown color theme matching the app

---

## ⚠ Disclaimers

- **Calorie accuracy**: Values from USDA SR Legacy dataset; real-world values may differ ±10–30%
- **Security**: bcrypt password hashing for local protection; not equivalent to server-side auth
- **Medical**: This app is not a medical device. Consult a healthcare professional before any weight-loss program.
- **Goal projections**: Based on evidence-based formulas (Mifflin-St Jeor + 7700 kcal/kg fat rule); individual results vary.

---

## 📚 Research Citations (stored in DB)

1. Sacks FM et al. "Comparison of weight-loss diets..." NEJM 2009. DOI: 10.1056/NEJMoa0804748
2. Hall KD et al. "Quantification of the effect of energy imbalance..." The Lancet 2011. DOI: 10.1016/S0140-6736(11)60812-X

---

## 🐢 Turtle Expressions

| Expression | Trigger |
|-----------|---------|
| IDLE      | Default, no action for 30s |
| HAPPY     | Food logged, login success |
| EXCITED   | Streak milestone (7/14/21/30/60/90 days) |
| CHEERING  | Daily calorie goal reached (100%) |
| THINKING  | Typing in food search box |
| SLEEPING  | App idle > 5 min |
| WORRIED   | 20%+ over calorie goal |

This is the UI 

<img width="716" height="829" alt="image" src="https://github.com/user-attachments/assets/796d618a-2e68-40ad-8139-5263317f54a8" />
<img width="1919" height="1079" alt="Screenshot 2026-03-07 220926" src="https://github.com/user-attachments/assets/45c3968c-765f-4371-88f6-ab56f3d1f8f3" />
<img width="1919" height="1079" alt="Screenshot 2026-03-07 220946" src="https://github.com/user-attachments/assets/aac4b4dd-2900-40e4-8292-b01d74b15d2a" />

