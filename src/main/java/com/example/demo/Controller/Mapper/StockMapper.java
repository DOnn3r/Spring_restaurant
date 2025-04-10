package com.example.demo.Controller.Mapper;

import com.example.demo.Controller.rest.StockMouvementRest;
import com.example.demo.Entity.StockMouvement;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class StockMapper implements Function<StockMouvement, StockMouvementRest> {

    @Override
    public StockMouvementRest apply(StockMouvement stockMouvement) {
        return new StockMouvementRest(stockMouvement.getId(),
                stockMouvement.getQuantity(),
                stockMouvement.getMouvementType(),
                stockMouvement.getMouvementDate());
    }
}
