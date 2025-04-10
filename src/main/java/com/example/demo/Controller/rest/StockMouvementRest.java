package com.example.demo.Controller.rest;

import com.example.demo.Entity.MouvementType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class StockMouvementRest {
    private int id;
    private Double quantity;
    private MouvementType type;
    private LocalDateTime creationDatetime;
}
