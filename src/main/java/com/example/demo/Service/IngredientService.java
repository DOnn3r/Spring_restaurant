package com.example.demo.Service;

import com.example.demo.Controller.IngredientController;
import com.example.demo.Controller.Mapper.PriceMapper;
import com.example.demo.Controller.rest.CreateIngredientPrice;
import com.example.demo.DAO.operations.IngredientDAO;
import com.example.demo.DAO.operations.PriceCrudOperations;
import com.example.demo.DAO.operations.StockMouvementCrudOperations;
import com.example.demo.Entity.Ingredient;
import com.example.demo.Entity.IngredientPrice;
import com.example.demo.Entity.MouvementType;
import com.example.demo.Entity.StockMouvement;
import com.example.demo.Service.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngredientService {
    private static final Logger log = LogManager.getLogger(IngredientController.class);
    private final IngredientDAO ingredientDAO;
    private final PriceCrudOperations priceCrudOperations;
    private final StockMouvementCrudOperations stockMouvementCrudOperations;
    private final PriceMapper priceMapper;

    @Autowired
    public IngredientService(
            IngredientDAO ingredientDAO,
            PriceCrudOperations priceCrudOperations,
            StockMouvementCrudOperations stockMouvementCrudOperations,
            PriceMapper priceMapper) {
        this.ingredientDAO = ingredientDAO;
        this.priceCrudOperations = priceCrudOperations;
        this.stockMouvementCrudOperations = stockMouvementCrudOperations;
        this.priceMapper = priceMapper;
    }

    public List<Ingredient> getAllIngredients() {
        return ingredientDAO.getAll();
    }

    public Ingredient findById(int id) {
        return  ingredientDAO.findById(id);
    }

    public List<Ingredient> getFilteredIngredient(Double minPrice) {
        return ingredientDAO.getFilteredIngredient(minPrice);
    }

    public List<Ingredient> getFilteredIngredientByPrice(Double minPrice, Double maxPrice) {
        return ingredientDAO.getFilteredIngredientByPrice(minPrice, maxPrice);
    }

    public List<Ingredient> saveAll(List<Ingredient> ingredients) throws SQLException {
        return ingredientDAO.saveAll(ingredients);
    }

    public List<IngredientPrice> updatePrices(int ingredientId, List<IngredientPrice> prices) throws SQLException {
        // Validation des IDs
        if (prices.stream().anyMatch(price -> price.getId() <= 0)) {
            throw new IllegalArgumentException("Tous les prix doivent avoir un ID valide (entier positif)");
        }
        return priceCrudOperations.saveAll(prices);
    }

    public List<StockMouvement> updateStockMovements(int ingredientId, List<StockMouvement> movements) throws SQLException {
        log.info("Updating {} stock movements for ingredient {}", movements.size(), ingredientId);

        Ingredient ingredient = ingredientDAO.findById(ingredientId);
        if (ingredient == null) {
            log.error("Ingredient {} not found", ingredientId);
            throw new NotFoundException("Ingredient not found");
        }

        log.debug("Original movements: {}", movements);

        List<StockMouvement> savedMovements = stockMouvementCrudOperations.saveAll(
                movements.stream()
                        .peek(m -> {
                            log.debug("Processing movement: {}", m);
                            m.setIngredientId(ingredientId);
                        })
                        .collect(Collectors.toList())
        );

        log.info("Saved movements: {}", savedMovements);

        ingredient.setStockMouvements(savedMovements);
        ingredientDAO.saveOne(ingredient);

        return savedMovements;
    }

    public List<IngredientPrice> getPricesForIngredient(int ingredientId) {
        try {
            return priceCrudOperations.findByIdIngredient(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get prices for ingredient: " + ingredientId, e);
        }
    }

    public List<StockMouvement> getStockMouvementForIngredient(int ingredientId) {
        try {
            List<StockMouvement> mouvements = stockMouvementCrudOperations.findByIdIngredient(ingredientId);
            log.debug("Retrieved movements: {}", mouvements);
            return mouvements;
        } catch (SQLException e) {
            log.error("Error fetching stock movements for ingredient {}", ingredientId, e);
            throw new RuntimeException("Failed to get stock movements: " + e.getMessage(), e);
        }
    }
}
