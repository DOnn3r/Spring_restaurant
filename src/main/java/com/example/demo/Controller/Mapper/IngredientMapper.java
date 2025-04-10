package com.example.demo.Controller.Mapper;

import com.example.demo.Controller.rest.*;
import com.example.demo.Entity.*;
import com.example.demo.Service.IngredientService;
import com.example.demo.Service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class IngredientMapper {
    @Autowired private PriceMapper priceMapper;
    @Autowired private StockMapper stockMapper;
    @Autowired private IngredientService ingredientService;

    public IngredientRest toRest(Ingredient ingredient) {
        if (ingredient == null) return null;

        return new IngredientRest(
                ingredient.getId(),
                ingredient.getName(),
                priceMapper.toRestList(ingredient.getHistoricalPrices()),
                ingredient.getUnity(),
                stockMapper.toRestList(ingredient.getStockMouvements())
        );
    }

    public Ingredient toModel(CreateOrUpdateIngredient dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(dto.getId());
        ingredient.setName(dto.getName());
        ingredient.setUnity(dto.getUnity());

        try {
            Ingredient existing = ingredientService.findById(dto.getId());
            ingredient.setHistoricalPrices(existing.getHistoricalPrices());
            ingredient.setStockMouvements(existing.getStockMouvements());
        } catch (NotFoundException e) {
            ingredient.setHistoricalPrices(new ArrayList<>());
            ingredient.setStockMouvements(new ArrayList<>());
        }

        return ingredient;
    }

    private List<PriceRest> toPriceRestList(List<IngredientPrice> prices) {
        return prices.stream()
                .map(priceMapper::apply)
                .toList();
    }

    private List<StockMouvementRest> toStockMovementRestList(List<StockMouvement> movements) {
        return movements.stream()
                .map(stockMapper::apply)
                .toList();
    }

    public PriceRest toPriceRest(IngredientPrice price) {
        return price != null ?
                new PriceRest(price.getId(), price.getPrice(), price.getDate()) :
                null;
    }
}