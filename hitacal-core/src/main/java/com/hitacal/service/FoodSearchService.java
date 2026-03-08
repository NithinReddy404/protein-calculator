package com.hitacal.service;

import com.hitacal.model.FoodItem;
import com.hitacal.repository.FoodRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FoodSearchService {
    private final FoodRepository foodRepo = new FoodRepository();

    public List<FoodItem> search(String query) throws SQLException {
        if (query == null || query.trim().length() < 2) return List.of();
        return foodRepo.search(query.trim(), 30);
    }

    public Optional<FoodItem> getByFdcId(int fdcId) throws SQLException {
        return foodRepo.findByFdcId(fdcId);
    }
}
