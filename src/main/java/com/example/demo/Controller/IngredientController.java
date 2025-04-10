package com.example.demo.Controller;

import com.example.demo.Controller.Mapper.IngredientMapper;
import com.example.demo.Controller.Mapper.PriceMapper;
import com.example.demo.Controller.Mapper.StockMapper;
import com.example.demo.Controller.rest.*;
import com.example.demo.Entity.*;
import com.example.demo.Service.IngredientService;
import com.example.demo.Service.exception.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IngredientController {

    private static final Logger log = LogManager.getLogger(IngredientController.class);
    private final IngredientService ingredientService;
    private final IngredientMapper ingredientMapper;
    private final PriceMapper priceMapper;
    private final StockMapper stockMapper;

    public IngredientController(IngredientService ingredientService,
                                IngredientMapper ingredientMapper, PriceMapper priceMapper, StockMapper stockMapper) {
        this.ingredientService = ingredientService;
        this.ingredientMapper = ingredientMapper;
        this.priceMapper = priceMapper;
        this.stockMapper = stockMapper;
    }

    @PostMapping("/ingredients")
    public ResponseEntity<?> createIngredients(@RequestBody List<CreateOrUpdateIngredient> createDtos) {
        try {
            if (createDtos == null || createDtos.isEmpty()) {
                return ResponseEntity.badRequest().body("La liste ne peut pas être vide");
            }

            List<Ingredient> newIngredients = createDtos.stream()
                    .map(dto -> {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setId(dto.getId());
                        ingredient.setName(dto.getName());
                        ingredient.setUnity(dto.getUnity());
                        ingredient.setLastModification(LocalDateTime.now());
                        ingredient.setUnitPrice(dto.getUniPrice());
                        return ingredient;
                    })
                    .collect(Collectors.toList());

            List<Ingredient> savedIngredients = ingredientService.saveAll(newIngredients);
            log.info("Saved ingredients count: {}", savedIngredients.size());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedIngredients.stream()
                            .map(ingredientMapper::toRest)
                            .collect(Collectors.toList()));

        } catch (DataAccessException e) {
            log.error("Database error", e); // Log l'exception réelle
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Erreur de base de données");
        } catch (Exception e) {
            log.error("Server error", e); // Log l'exception réelle
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<List<IngredientRest>> updateIngredients(
            @RequestBody List<CreateOrUpdateIngredient> updateDtos) {
        try {
            List<Ingredient> ingredients = updateDtos.stream()
                    .map(dto -> {
                        Ingredient existing = ingredientService.findById(dto.getId());
                        existing.setName(dto.getName());
                        existing.setUnity(dto.getUnity());
                        return existing;
                    })
                    .toList();

            List<Ingredient> updated = ingredientService.saveAll(ingredients);
            return ResponseEntity.ok(updated.stream()
                    .map(ingredientMapper::toRest)
                    .toList());
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable int id) {
        try {
            Ingredient ingredient = ingredientService.findById(id);
            if (ingredient == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ingredientMapper.toRest(ingredient));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ingrédient ID {}", id, e);
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientRest>> getIngredients(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        try {
            List<Ingredient> ingredients;
            if (minPrice != null && maxPrice != null) {
                ingredients = ingredientService.getFilteredIngredientByPrice(minPrice, maxPrice);
            } else if (minPrice != null) {
                ingredients = ingredientService.getFilteredIngredient(minPrice);
            } else {
                ingredients = ingredientService.getAllIngredients();
            }

            List<IngredientRest> response = ingredients.stream()
                    .map(ingredientMapper::toRest)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ingredients/{ingredientId}/prices")
    public ResponseEntity<List<PriceRest>> getIngredientPrices(@PathVariable int ingredientId) {
        try {
            List<IngredientPrice> prices = ingredientService.getPricesForIngredient(ingredientId);
            List<PriceRest> response = prices.stream()
                    .map(price -> new PriceRest(
                            price.getId(),
                            price.getPrice(),
                            price.getDate()))
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ingredients/{ingredientId}/stockMovements")
    public ResponseEntity<List<StockMouvementRest>> getIngredientStock(@PathVariable int ingredientId) {
        try {
            List<StockMouvement> stockMouvements = ingredientService.getStockMouvementForIngredient(ingredientId);
            List<StockMouvementRest> response = stockMouvements.stream()
                    .map(m -> new StockMouvementRest(
                            m.getId(),          // ID manuel bien transmis
                            m.getQuantity(),
                            m.getMouvementType(),
                            m.getMouvementDate(),
                            m.getUnity()       // Ajout de l'unité
                    ))
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting stock movements", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/ingredients/{ingredientId}/prices")
    public ResponseEntity<?> updateIngredientPrices(
            @PathVariable int ingredientId,
            @RequestBody List<CreateIngredientPrice> priceDtos) {
        try {
            Ingredient ingredient = ingredientService.findById(ingredientId);
            if (ingredient == null) {
                return ResponseEntity.notFound().build();
            }

            List<IngredientPrice> prices = priceDtos.stream()
                    .map(dto -> {
                        IngredientPrice price = new IngredientPrice();
                        price.setId(dto.getId());
                        price.setPrice(dto.getPrice());
                        price.setDate(dto.getDateValue());
                        price.setIngredient(ingredientService.findById(ingredientId)); // Associez l'ingrédient
                        return price;
                    })
                    .collect(Collectors.toList());
            List<IngredientPrice> savedPrices = ingredientService.updatePrices(ingredientId, prices);
            return ResponseEntity.ok(priceMapper.toRestList(savedPrices));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/ingredients/{ingredientId}/stockMovements")
    public ResponseEntity<?> updateIngredientStockMovements(
            @PathVariable int ingredientId,
            @RequestBody List<CreateStockouvement> movementDtos) {
        try {
            List<StockMouvement> movements = new ArrayList<>();
            int tempId = 1; // Compteur temporaire

            for (CreateStockouvement dto : movementDtos) {
                StockMouvement mouvement = new StockMouvement(
                        tempId++, // ID manuel temporaire
                        ingredientId,
                        dto.getMouvementType(),
                        dto.getQuantity(),
                        dto.getUnity(),
                        dto.getMouvementDate()
                );
                movements.add(mouvement);
            }

            List<StockMouvement> savedMovements = ingredientService.updateStockMovements(ingredientId, movements);
            return ResponseEntity.ok(stockMapper.toRestList(savedMovements));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}