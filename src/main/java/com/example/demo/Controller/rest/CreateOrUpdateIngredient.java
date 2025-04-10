package com.example.demo.Controller.rest;

import com.example.demo.Entity.Unity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;

@AllArgsConstructor
@Getter
public class CreateOrUpdateIngredient {
    private int id;
    private String name;
    private Unity unity;
}

