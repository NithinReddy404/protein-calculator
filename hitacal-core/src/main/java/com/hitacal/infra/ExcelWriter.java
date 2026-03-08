package com.hitacal.infra;

import com.hitacal.model.DailyLog;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ExcelWriter {
    private static final Logger LOG = LoggerFactory.getLogger(ExcelWriter.class);

    // Coffee colors (ARGB)
    private static final byte[] ESPRESSO    = hex("3B1F0E");
    private static final byte[] COFFEE_BROWN= hex("6B3A2A");
    private static final byte[] CREAM       = hex("F5ECD7");
    private static final byte[] PARCHMENT   = hex("FDF6EC");
    private static final byte[] WARM_SAND   = hex("F0E0C8");
    private static final byte[] WHITE       = hex("FFFFFF");
    private static final byte[] TURTLE_GRN  = hex("4A7C59");

    public static Path resolveWorkbookPath(String username) {
        Path dir = Paths.get(System.getProperty("user.home"), "hitacal_exports");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir.resolve(username + "_nutrition.xlsx");
    }

    public static void exportAllLogs(String username, List<DailyLog> logs, int calorieTarget) throws IOException {
        Path path = resolveWorkbookPath(username);
        XSSFWorkbook wb = loadOrCreate(path);

        for (DailyLog log : logs) {
            String sheetName = log.getLogDate().toString().substring(0, 7); // YYYY-MM
            XSSFSheet sheet = wb.getSheet(sheetName);
            if (sheet == null) sheet = createMonthSheet(wb, sheetName, calorieTarget);
            appendOrUpdateRow(wb, sheet, log, calorieTarget);
        }

        ensureSummarySheet(wb, username, logs);
        ensureAboutSheet(wb);

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            wb.write(fos);
        }
        wb.close();
        LOG.info("Excel exported to {}", path);
    }

    // ── Sheet creation ──────────────────────────────────────────────────────

    private static XSSFSheet createMonthSheet(XSSFWorkbook wb, String yearMonth, int calorieTarget) {
        XSSFSheet sheet = wb.createSheet(yearMonth);
        sheet.setColumnWidth(0, 14 * 256);  // Date
        sheet.setColumnWidth(1, 6  * 256);  // Day
        sheet.setColumnWidth(2, 16 * 256);  // Calories
        sheet.setColumnWidth(3, 12 * 256);  // Protein
        sheet.setColumnWidth(4, 10 * 256);  // Fat
        sheet.setColumnWidth(5, 10 * 256);  // Carbs
        sheet.setColumnWidth(6, 12 * 256);  // Goal Cal
        sheet.setColumnWidth(7, 14 * 256);  // vs Goal
        sheet.setColumnWidth(8, 10 * 256);  // Streak
        sheet.setColumnWidth(9, 36 * 256);  // Notes

        // Title row (row 0)
        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeight((short) 600);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🐢 hitacal AI — Monthly Nutrition Log: " + yearMonth);
        titleCell.setCellStyle(titleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        // Disclaimer row (row 1)
        XSSFRow disclaimerRow = sheet.createRow(1);
        XSSFCell dCell = disclaimerRow.createCell(0);
        dCell.setCellValue("⚠ Values are estimates (±10–30%). Not medical advice. Source: USDA FoodData Central (public domain).");
        dCell.setCellStyle(disclaimerStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

        // Header row (row 2)
        XSSFRow headerRow = sheet.createRow(2);
        headerRow.setHeight((short) 450);
        String[] headers = {"Date","Day","Calories (kcal)","Protein (g)","Fat (g)","Carbs (g)","Goal Cal","vs. Goal","Streak","Notes"};
        XSSFCellStyle hs = headerStyle(wb);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(hs);
        }

        return sheet;
    }

    private static void appendOrUpdateRow(XSSFWorkbook wb, XSSFSheet sheet, DailyLog log, int calorieTarget) {
        // Check if row for this date already exists (rows start at index 3)
        String dateStr = log.getLogDate().toString();
        for (int i = 3; i <= sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            if (row != null) {
                XSSFCell cell = row.getCell(0);
                if (cell != null && dateStr.equals(cell.getStringCellValue())) {
                    fillDataRow(wb, row, log, calorieTarget, i - 3);
                    return;
                }
            }
        }
        // Append new row
        int rowIdx = Math.max(sheet.getLastRowNum() + 1, 3);
        XSSFRow row = sheet.createRow(rowIdx);
        fillDataRow(wb, row, log, calorieTarget, rowIdx - 3);
    }

    private static void fillDataRow(XSSFWorkbook wb, XSSFRow row, DailyLog log, int calorieTarget, int rowOffset) {
        boolean isEven = rowOffset % 2 == 0;
        XSSFCellStyle dataStyle  = dataStyle(wb, isEven, false);
        XSSFCellStyle numStyle   = numStyle(wb, isEven, false);
        XSSFCellStyle deltaStyle = deltaStyle(wb, log.getTotalCalories() - calorieTarget);

        LocalDate date = log.getLogDate();
        double delta = log.getTotalCalories() - calorieTarget;

        row.createCell(0).setCellValue(date.toString());
        row.getCell(0).setCellStyle(dataStyle);

        row.createCell(1).setCellValue(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        row.getCell(1).setCellStyle(dataStyle);

        row.createCell(2).setCellValue(log.getTotalCalories());
        row.getCell(2).setCellStyle(numStyle);

        row.createCell(3).setCellValue(log.getTotalProteinG());
        row.getCell(3).setCellStyle(numStyle);

        row.createCell(4).setCellValue(log.getTotalFatG());
        row.getCell(4).setCellStyle(numStyle);

        row.createCell(5).setCellValue(log.getTotalCarbG());
        row.getCell(5).setCellStyle(numStyle);

        row.createCell(6).setCellValue(calorieTarget);
        row.getCell(6).setCellStyle(numStyle);

        row.createCell(7).setCellValue(delta);
        row.getCell(7).setCellStyle(deltaStyle);

        row.createCell(9).setCellValue(log.getNotes() != null ? log.getNotes() : "");
        row.getCell(9).setCellStyle(dataStyle);
    }

    private static void ensureSummarySheet(XSSFWorkbook wb, String username, List<DailyLog> logs) {
        XSSFSheet sheet = wb.getSheet("Summary");
        if (sheet != null) wb.removeSheetAt(wb.getSheetIndex(sheet));
        sheet = wb.createSheet("Summary");

        XSSFRow title = sheet.createRow(0);
        XSSFCell tc = title.createCell(0);
        tc.setCellValue("hitacal AI — Summary for " + username);
        tc.setCellStyle(titleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,3));

        double totalCal = logs.stream().mapToDouble(DailyLog::getTotalCalories).sum();
        double avgCal   = logs.isEmpty() ? 0 : totalCal / logs.size();
        double totalPro = logs.stream().mapToDouble(DailyLog::getTotalProteinG).sum();
        double totalFat = logs.stream().mapToDouble(DailyLog::getTotalFatG).sum();

        String[][] rows = {
            {"Total days logged",     String.valueOf(logs.size())},
            {"Total calories",        String.format("%.0f kcal", totalCal)},
            {"Average daily calories",String.format("%.1f kcal", avgCal)},
            {"Total protein",         String.format("%.1f g", totalPro)},
            {"Total fat",             String.format("%.1f g", totalFat)},
        };
        for (int i = 0; i < rows.length; i++) {
            XSSFRow row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(rows[i][0]);
            row.createCell(1).setCellValue(rows[i][1]);
        }
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 20 * 256);
    }

    private static void ensureAboutSheet(XSSFWorkbook wb) {
        if (wb.getSheet("About") != null) return;
        XSSFSheet sheet = wb.createSheet("About");
        sheet.createRow(0).createCell(0).setCellValue("hitacal AI — Data Source & Disclaimer");
        sheet.createRow(2).createCell(0).setCellValue("Nutrition Data Source:");
        sheet.createRow(3).createCell(0).setCellValue("USDA FoodData Central SR Legacy Dataset");
        sheet.createRow(4).createCell(0).setCellValue("https://fdc.nal.usda.gov/download-foods");
        sheet.createRow(5).createCell(0).setCellValue("Public domain. No copyright restrictions.");
        sheet.createRow(7).createCell(0).setCellValue("Accuracy Disclaimer:");
        sheet.createRow(8).createCell(0).setCellValue(
            "Calorie and nutrient values are estimates and may differ from actual food by ±10–30%.");
        sheet.createRow(9).createCell(0).setCellValue(
            "This file is not medical advice. Consult a healthcare professional for dietary guidance.");
        sheet.setColumnWidth(0, 80 * 256);
    }

    // ── Styles ──────────────────────────────────────────────────────────────

    private static XSSFCellStyle titleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(ESPRESSO, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short)14);
        f.setColor(new XSSFColor(WHITE, null));
        s.setFont(f);
        return s;
    }

    private static XSSFCellStyle disclaimerStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(hex("5C3317"), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setItalic(true); f.setFontHeightInPoints((short)9);
        f.setColor(new XSSFColor(hex("F5ECD7"), null));
        s.setFont(f);
        return s;
    }

    private static XSSFCellStyle headerStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(COFFEE_BROWN, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        setBorder(s, BorderStyle.THIN, COFFEE_BROWN);
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short)10);
        f.setColor(new XSSFColor(WHITE, null));
        s.setFont(f);
        return s;
    }

    private static XSSFCellStyle dataStyle(XSSFWorkbook wb, boolean even, boolean bold) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(even ? PARCHMENT : WARM_SAND, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s, BorderStyle.THIN, hex("C49A6C"));
        if (bold) { XSSFFont f = wb.createFont(); f.setBold(true); s.setFont(f); }
        return s;
    }

    private static XSSFCellStyle numStyle(XSSFWorkbook wb, boolean even, boolean bold) {
        XSSFCellStyle s = dataStyle(wb, even, bold);
        s.setDataFormat(wb.createDataFormat().getFormat("0.0"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private static XSSFCellStyle deltaStyle(XSSFWorkbook wb, double delta) {
        XSSFCellStyle s = wb.createCellStyle();
        boolean over = delta > 100;
        s.setFillForegroundColor(new XSSFColor(over ? hex("FFD0D0") : hex("D0FFD0"), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setDataFormat(wb.createDataFormat().getFormat("+0.0;-0.0"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(s, BorderStyle.THIN, hex("C49A6C"));
        return s;
    }

    private static void setBorder(XSSFCellStyle s, BorderStyle style, byte[] color) {
        XSSFColor c = new XSSFColor(color, null);
        s.setBorderTop(style); s.setTopBorderColor(c);
        s.setBorderBottom(style); s.setBottomBorderColor(c);
        s.setBorderLeft(style); s.setLeftBorderColor(c);
        s.setBorderRight(style); s.setRightBorderColor(c);
    }

    private static XSSFWorkbook loadOrCreate(Path path) throws IOException {
        if (Files.exists(path)) {
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                return new XSSFWorkbook(fis);
            }
        }
        return new XSSFWorkbook();
    }

    private static byte[] hex(String h) {
        return new byte[]{
            (byte) Integer.parseInt(h.substring(0,2),16),
            (byte) Integer.parseInt(h.substring(2,4),16),
            (byte) Integer.parseInt(h.substring(4,6),16)
        };
    }
}
