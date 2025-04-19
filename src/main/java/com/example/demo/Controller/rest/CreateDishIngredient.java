package com.example.demo.Controller.rest;

import com.example.demo.Entity.Unity;
import lombok.Data;

@Data
public class CreateDishIngredient {
    private Integer ingredientId;
    private Double requiredQuantity;
    private Unity unity;
}