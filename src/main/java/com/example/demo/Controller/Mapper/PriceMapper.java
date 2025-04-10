package com.example.demo.Controller.Mapper;

import com.example.demo.Controller.rest.CreateIngredientPrice;
import com.example.demo.Controller.rest.PriceRest;
import com.example.demo.Entity.IngredientPrice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PriceMapper implements Function<IngredientPrice, PriceRest> {

    @Override
    public PriceRest apply(IngredientPrice ingredientPrice) {
        return new PriceRest(ingredientPrice.getId(), ingredientPrice.getPrice(), ingredientPrice.getDate());
    }

    public PriceRest toRest(IngredientPrice price) {
        return new PriceRest(price.getId(), price.getPrice(), price.getDate());
    }

    public List<PriceRest> toRestList(List<IngredientPrice> prices) {
        return prices.stream().map(this::toRest).toList();
    }

    public IngredientPrice toModel(CreateIngredientPrice dto) {
        IngredientPrice price = new IngredientPrice();
        // Ne pas setter l'ID ici
        price.setPrice(dto.getPrice());
        price.setDate(dto.getDateValue());
        return price;
    }
}
