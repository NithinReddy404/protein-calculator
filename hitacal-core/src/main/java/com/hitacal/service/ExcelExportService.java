package com.hitacal.service;

import com.hitacal.model.DailyLog;
import com.hitacal.model.Goal;
import com.hitacal.model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class ExcelExportService {
    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    private static final String EXPORT_DIR =
        System.getProperty("user.home") + File.separator + "hitacal_exports";

    // Coffee color constants (ARGB hex)
    private static final String COLOR_ESPRESSO  = "FF3B1F0E";
    private static final String COLOR_COFFEE    = "FF6B3A2A";
    private static final String COLOR_LATTE     = "FFC49A6C";
    private static final String COLOR_CREAM     = "FFFFF8EC";
    private static final String COLOR_PARCHMENT = "FFFDF6EC";
    private static final String COLOR_SAND      = "FFF0E0C8";
    private static final String COLOR_WHITE     = "FFFFFFFF";

    public File exportForUser(User user, List<DailyLog> logs, Optional<Goal> goal) throws IOException {
        new File(EXPORT_DIR).mkdirs();
        String path = EXPORT_DIR + File.separator + user.getUsername() + "_nutrition.xlsx";
        File file = new File(path);

        XSSFWorkbook wb = file.exists()
            ? new XSSFWorkbook(new FileInputStream(file))
            : new XSSFWorkbook();

        // Group logs by YYYY-MM
        Map<String, List<DailyLog>> byMonth = new LinkedHashMap<>();
        for (DailyLog dl : logs) {
            String key = dl.getLogDate().toString().substring(0, 7);
            byMonth.computeIfAbsent(key, k -> new ArrayList<>()).add(dl);
        }

        for (Map.Entry<String, List<DailyLog>> entry : byMonth.entrySet()) {
            buildMonthSheet(wb, entry.getKey(), entry.getValue(), goal);
        }
        buildSummarySheet(wb, user, logs, goal);

        try (FileOutputStream fos = new FileOutputStream(path)) { wb.write(fos); }
        wb.close();
        log.info("Excel exported: {}", path);
        return file;
    }

    private void buildMonthSheet(XSSFWorkbook wb, String yearMonth,
                                  List<DailyLog> logs, Optional<Goal> goal) {
        XSSFSheet sheet = wb.getSheet(yearMonth);
        if (sheet == null) sheet = wb.createSheet(yearMonth);
        else { // clear existing rows
            for (int i = sheet.getLastRowNum(); i >= 0; i--) {
                Row r = sheet.getRow(i);
                if (r != null) sheet.removeRow(r);
            }
        }

        // -- Styles --
        CellStyle titleStyle  = coffeeStyle(wb, COLOR_ESPRESSO, COLOR_WHITE,  true,  14);
        CellStyle headerStyle = coffeeStyle(wb, COLOR_COFFEE,   COLOR_WHITE,  true,  11);
        CellStyle row1Style   = coffeeStyle(wb, COLOR_PARCHMENT,"FF2C1A0E",  false, 10);
        CellStyle row2Style   = coffeeStyle(wb, COLOR_SAND,     "FF2C1A0E",  false, 10);

        // Row 0: Title
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(22);
        Cell tc = titleRow.createCell(0);
        tc.setCellValue("hitacal AI — " + yearMonth + " Nutrition Log  •  ⚠ Values are estimates (±10–30%)");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        // Row 1: Headers
        String[] headers = {"Date","Day","Calories (kcal)","Protein (g)","Fat (g)",
                             "Carbs (g)","Goal Cal.","vs. Goal","Streak (days)","Notes"};
        Row hRow = sheet.createRow(1);
        hRow.setHeightInPoints(16);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        // Data rows
        int rowIdx = 2;
        int goalCal = goal.map(Goal::getCalorieTarget).orElse(0);
        for (DailyLog dl : logs) {
            Row row = sheet.createRow(rowIdx);
            CellStyle style = (rowIdx % 2 == 0) ? row1Style : row2Style;
            LocalDate d = dl.getLogDate();
            setCell(row, 0, d.toString(), style);
            setCell(row, 1, d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), style);
            setNumCell(row, 2, dl.getTotalCalories(),  style);
            setNumCell(row, 3, dl.getTotalProteinG(),  style);
            setNumCell(row, 4, dl.getTotalFatG(),      style);
            setNumCell(row, 5, dl.getTotalCarbG(),     style);
            setNumCell(row, 6, goalCal,                style);
            setNumCell(row, 7, dl.getTotalCalories() - goalCal, style);
            setCell(row, 8, "", style); // streak computed live
            setCell(row, 9, dl.getNotes() != null ? dl.getNotes() : "", style);
            rowIdx++;
        }

        // Column widths
        int[] widths = {12,5,16,12,10,10,12,12,12,28};
        for (int i = 0; i < widths.length; i++)
            sheet.setColumnWidth(i, widths[i] * 256);
    }

    private void buildSummarySheet(XSSFWorkbook wb, User user,
                                   List<DailyLog> logs, Optional<Goal> goal) {
        String sheetName = "Summary";
        XSSFSheet sheet = wb.getSheet(sheetName);
        if (sheet == null) sheet = wb.createSheet(sheetName);
        else { for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            Row r = sheet.getRow(i); if (r != null) sheet.removeRow(r); } }

        CellStyle titleStyle  = coffeeStyle(wb, COLOR_ESPRESSO, COLOR_WHITE,  true,  14);
        CellStyle labelStyle  = coffeeStyle(wb, COLOR_COFFEE,   COLOR_WHITE,  true,  11);
        CellStyle valueStyle  = coffeeStyle(wb, COLOR_CREAM,    "FF2C1A0E",  false, 11);

        Row r0 = sheet.createRow(0); r0.setHeightInPoints(22);
        Cell t = r0.createCell(0);
        t.setCellValue("hitacal AI — Summary for " + user.getDisplayName());
        t.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,3));

        int row = 1;
        double totalCal = logs.stream().mapToDouble(DailyLog::getTotalCalories).sum();
        double avgCal   = logs.isEmpty() ? 0 : totalCal / logs.size();
        double totalPro = logs.stream().mapToDouble(DailyLog::getTotalProteinG).sum();

        String[][] data = {
            {"Total days logged",  String.valueOf(logs.size())},
            {"Total calories",     String.format("%.0f kcal", totalCal)},
            {"Average calories/day", String.format("%.0f kcal", avgCal)},
            {"Total protein",      String.format("%.0f g", totalPro)},
            {"Goal calorie target", goal.map(g -> g.getCalorieTarget() + " kcal/day").orElse("Not set")},
            {"Goal start weight",   goal.map(g -> g.getStartWeightKg() + " kg").orElse("—")},
            {"Goal target weight",  goal.map(g -> g.getTargetWeightKg() + " kg").orElse("—")},
            {"Projected end date",  goal.map(g -> g.getProjectedEndDate().toString()).orElse("—")},
        };

        for (String[] kv : data) {
            Row r = sheet.createRow(row++);
            Cell lc = r.createCell(0); lc.setCellValue(kv[0]); lc.setCellStyle(labelStyle);
            Cell vc = r.createCell(1); vc.setCellValue(kv[1]); vc.setCellStyle(valueStyle);
        }
        sheet.setColumnWidth(0, 28 * 256);
        sheet.setColumnWidth(1, 20 * 256);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private CellStyle coffeeStyle(XSSFWorkbook wb, String bgArgb, String fgArgb,
                                   boolean bold, int fontSize) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(bgArgb), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        font.setColor(new XSSFColor(hexToBytes(fgArgb), null));
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(hexToBytes(COLOR_LATTE), null));
        style.setRightBorderColor(new XSSFColor(hexToBytes(COLOR_LATTE), null));
        return style;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col); c.setCellValue(value); c.setCellStyle(style);
    }
    private void setNumCell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col); c.setCellValue(value); c.setCellStyle(style);
    }

    private byte[] hexToBytes(String argb) {
        // ARGB hex like "FF3B1F0E" → byte[3] RGB
        int r = Integer.parseInt(argb.substring(2, 4), 16);
        int g = Integer.parseInt(argb.substring(4, 6), 16);
        int b = Integer.parseInt(argb.substring(6, 8), 16);
        return new byte[]{ (byte)r, (byte)g, (byte)b };
    }
}
