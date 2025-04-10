package com.example.demo.Controller.rest;

import com.example.demo.Entity.MouvementType;
import com.example.demo.Entity.Unity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CreateStockouvement {
    private MouvementType mouvementType;
    private double quantity;
    private Unity unity;
    private LocalDateTime mouvementDate;
}
