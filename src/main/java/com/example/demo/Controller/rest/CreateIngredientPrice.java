package com.example.demo.Controller.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class CreateIngredientPrice {
    private int id;
    private Double price;
    private LocalDate dateValue;
}
