package com.example.demo.Entity;

import java.time.LocalDate;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private double unitPrice;
    private List<DishIngredient> dishIngredients;
    private double availableQuantity;

    public Dish(int id, String name, double unitPrice) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
    }

    public Dish(int id, String name, double unitPrice, List<DishIngredient> dishIngredients, double availableQuantity) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.dishIngredients = dishIngredients;
        this.availableQuantity = availableQuantity;
    }

    public Dish() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean addDishIngredient(List<DishIngredient> dishIngredient) {
        return dishIngredients.addAll(dishIngredient);
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public double getAvailableQuantity() {
        return dishIngredients.stream()
                .mapToInt(dishIngredient -> {
                    LocalDate date = LocalDate.now();
                    double availableQuantity = dishIngredient.getIngredient().getAvalaibleQuantity();
                    return (int) (availableQuantity / dishIngredient.getRequiredQuantity());
                })
                .min()
                .orElse(0);
    }
}
