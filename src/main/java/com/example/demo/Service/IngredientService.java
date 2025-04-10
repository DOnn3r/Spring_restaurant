package com.example.demo.Service;

import com.example.demo.DAO.operations.IngredientDAO;
import com.example.demo.DAO.operations.PriceCrudOperations;
import com.example.demo.DAO.operations.StockMouvementCrudOperations;
import com.example.demo.Entity.Ingredient;
import com.example.demo.Entity.IngredientPrice;
import com.example.demo.Entity.MouvementType;
import com.example.demo.Entity.StockMouvement;
import com.example.demo.Service.exception.NotFoundException;
import lombok.AllArgsConstructor;
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
    private final IngredientDAO ingredientDAO;
    private final PriceCrudOperations priceCrudOperations;
    private final StockMouvementCrudOperations stockMouvementCrudOperations;
    private PriceCrudOperations ingredientPrice;

    @Autowired
    public IngredientService(
            IngredientDAO ingredientDAO,
            PriceCrudOperations priceCrudOperations,
            StockMouvementCrudOperations stockMouvementCrudOperations
    ) {
        this.ingredientDAO = ingredientDAO;
        this.priceCrudOperations = priceCrudOperations;
        this.stockMouvementCrudOperations = stockMouvementCrudOperations;
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
        Ingredient ingredient = ingredientDAO.findById(ingredientId);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient not found");
        }

        List<IngredientPrice> savedPrices = priceCrudOperations.saveAll(
                prices.stream()
                        .peek(price -> price.setIngredient(ingredient))
                        .collect(Collectors.toList())
        );

        ingredient.setHistoricalPrices(savedPrices);
        ingredientDAO.saveOne(ingredient);

        return savedPrices;
    }

    public List<StockMouvement> updateStockMovements(int ingredientId, List<StockMouvement> movements) throws SQLException {
        Ingredient ingredient = ingredientDAO.findById(ingredientId);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient not found");
        }

        // Associe l'ingrédient et sauvegarde
        List<StockMouvement> savedMovements = stockMouvementCrudOperations.saveAll(
                movements.stream()
                        .peek(m -> m.setIngredientId(ingredientId))
                        .collect(Collectors.toList())
        );

        // Mise à jour de la référence
        ingredient.setStockMouvements(savedMovements);
        ingredientDAO.saveOne(ingredient);

        return savedMovements;
    }

    public List<IngredientPrice> getPricesForIngredient(int ingredientId) {
        try {
            return ingredientPrice.findByIdIngredient(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get prices for ingredient: " + ingredientId, e);
        }
    }
}
