package com.example.demo.Entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Ingredient {
    private int id;
    private String name;
    private double unitPrice;
    private Unity unity;
    private List<IngredientPrice> historicalPrices = new ArrayList<>();
    private List<StockMouvement> stockMouvements = new ArrayList<>();

    public Ingredient(int id, String name, LocalDateTime lastModification, double unitPrice, Unity unity) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.unity = unity;
    }

    public Ingredient(String name, LocalDateTime lastModification, double unitPrice, Unity unity) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.unity = unity;
    }

    public Ingredient(String name, LocalDateTime lastModification) {
        this.name = name;
    }

    public Ingredient(int id, String name, LocalDateTime lastModification, double unitPrice, Unity unity, List<IngredientPrice> historicalPrices) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.unity = unity;
        this.historicalPrices = historicalPrices;
    }

    public Ingredient(int id, String name, LocalDateTime lastModification, double unitPrice, Unity unity, List<IngredientPrice> historicalPrices, List<StockMouvement> stockMouvements) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.unity = unity;
        this.historicalPrices = historicalPrices;
        this.stockMouvements = stockMouvements;
    }

    public Ingredient() {}

    public Ingredient(int id) {
        this.id = id;
    }

    public Ingredient(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public double getPriceAtDate(LocalDate date) {
        return historicalPrices.stream()
                .filter(price -> !price.getDate().isAfter(date))
                .max((p1, p2) -> p1.getDate().compareTo(p2.getDate()))
                .map(IngredientPrice::getPrice)
                .orElseThrow(() -> new RuntimeException("No price found for the given date"));
    }

    public Double getActualPrice(){
        return historicalPrices.stream()
                .max(Comparator.comparing(IngredientPrice::getDate))
                .map(IngredientPrice::getPrice)
                .orElse(null);
    }
    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", unitPrice=" + unitPrice +
                ", unity=" + unity +
                '}';
    }

    public void addPrices(List<IngredientPrice> price) {
        historicalPrices.addAll(price);
    }

    public void addStockMouvements(List<StockMouvement> stockMouvement) {
        stockMouvements.addAll(stockMouvement);
    }

    public double getAvailableQuantity(LocalDate date) {
        double availableQuantity = 0;

        for (StockMouvement mouvement : stockMouvements) {
            if (!mouvement.getMouvementDate().toLocalDate().isAfter(date)) {
                if (mouvement.getMouvementType() == MouvementType.IN) {
                    availableQuantity += mouvement.getQuantity();
                } else if (mouvement.getMouvementType() == MouvementType.OUT) {
                    availableQuantity -= mouvement.getQuantity();
                }
            }
        }
        return availableQuantity;
    }

    public double getAvalaibleQuantity() {
        Double in = stockMouvements.stream()
                .filter((m) -> m.getMouvementType().equals(MouvementType.IN))
                .map(StockMouvement::getQuantity)
                .reduce(0.0, Double::sum);
        Double out = stockMouvements.stream()
                .filter((m) -> m.getMouvementType().equals(MouvementType.OUT))
                .map(StockMouvement::getQuantity)
                .reduce(0.0, Double::sum);
        return in-out;
    }
}
