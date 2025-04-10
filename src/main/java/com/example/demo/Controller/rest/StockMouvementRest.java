package com.example.demo.Controller.rest;

import com.example.demo.Entity.MouvementType;
import com.example.demo.Entity.Unity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class StockMouvementRest {
    private int id;
    private Double quantity;
    private MouvementType mouvementType;
    private LocalDateTime mouvementDate;
    private Unity unity;
}
