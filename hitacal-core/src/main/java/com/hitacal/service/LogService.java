package com.hitacal.service;

import com.hitacal.model.*;
import com.hitacal.repository.DailyLogRepository;
import com.hitacal.repository.FoodRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LogService {
    private final DailyLogRepository logRepo  = new DailyLogRepository();
    private final FoodRepository     foodRepo = new FoodRepository();

    public DailyLog getOrCreateToday(int userId) throws SQLException {
        return logRepo.getOrCreate(userId, LocalDate.now());
    }

    public DailyLog addFood(int userId, int fdcId, double servingGrams) throws SQLException {
        FoodItem food = foodRepo.findByFdcId(fdcId)
            .orElseThrow(() -> new IllegalArgumentException("Food not found: " + fdcId));
        DailyLog dl = logRepo.getOrCreate(userId, LocalDate.now());
        LogEntry e  = new LogEntry();
        e.setLogId(dl.getId());
        e.setFdcId(fdcId);
        e.setFoodName(food.getDescription());
        e.setServingGrams(servingGrams);
        e.setCalories(food.getCalories100g() * servingGrams / 100.0);
        e.setProteinG(food.getProtein100g() * servingGrams / 100.0);
        e.setFatG(food.getFat100g() * servingGrams / 100.0);
        e.setCarbG(food.getCarb100g() * servingGrams / 100.0);
        logRepo.addEntry(e);
        return logRepo.findByUserAndDate(userId, LocalDate.now()).orElseThrow();
    }

    public void deleteEntry(int entryId, int logId) throws SQLException {
        logRepo.deleteEntry(entryId, logId);
    }

    public Optional<DailyLog> getDay(int userId, LocalDate date) throws SQLException {
        return logRepo.findByUserAndDate(userId, date);
    }

    public List<DailyLog> getAllLogs(int userId) throws SQLException {
        return logRepo.findAll(userId);
    }

    public int getStreak(int userId) throws SQLException {
        return logRepo.getStreak(userId);
    }

    public List<FoodItem> searchFood(String query) throws SQLException {
        return foodRepo.search(query, 30);
    }
}
