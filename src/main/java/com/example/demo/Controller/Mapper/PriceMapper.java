package com.example.demo.Controller.Mapper;

import com.example.demo.Controller.rest.PriceRest;
import com.example.demo.Entity.IngredientPrice;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PriceMapper implements Function<IngredientPrice, PriceRest> {

    @Override
    public PriceRest apply(IngredientPrice ingredientPrice) {
        return new PriceRest(ingredientPrice.getId(), ingredientPrice.getPrice(), ingredientPrice.getDate());
    }
}
