package com.example.demo.Controller.rest;

import com.example.demo.Entity.Unity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class IngredientRest {
    private int id;
    private String name;
    private List<PriceRest> prices;
    private Unity unity;
    private List<StockMouvementRest> stocks;
    private Double availableQuantity;
}