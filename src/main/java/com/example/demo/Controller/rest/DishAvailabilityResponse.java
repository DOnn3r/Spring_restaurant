package com.example.demo.Controller.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
public class DishAvailabilityResponse {
    private int dishId;
    private String dishName;
    private int maxAvailable;
    private Map<String, Double> ingredientDetails;
}
