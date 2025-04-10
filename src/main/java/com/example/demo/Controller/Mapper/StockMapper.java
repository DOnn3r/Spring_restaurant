package com.example.demo.Controller.Mapper;

import com.example.demo.Controller.rest.CreateStockouvement;
import com.example.demo.Controller.rest.StockMouvementRest;
import com.example.demo.Entity.StockMouvement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class StockMapper implements Function<StockMouvement, StockMouvementRest> {

    @Override
    public StockMouvementRest apply(StockMouvement stockMouvement) {
        return new StockMouvementRest(
                stockMouvement.getId(),
                stockMouvement.getQuantity(),
                stockMouvement.getMouvementType(),
                stockMouvement.getMouvementDate(),
                stockMouvement.getUnity());
    }

    public StockMouvementRest toRest(StockMouvement mouvement) {
        return new StockMouvementRest(
                mouvement.getId(),
                mouvement.getQuantity(),
                mouvement.getMouvementType(),
                mouvement.getMouvementDate(),
                mouvement.getUnity()
        );
    }

    public List<StockMouvementRest> toRestList(List<StockMouvement> mouvements) {
        return mouvements.stream().map(this::toRest).toList();
    }

    public StockMouvement toModel(CreateStockouvement dto, int ingredientId) {
        StockMouvement mouvement = new StockMouvement();
        mouvement.setIngredientId(ingredientId);
        mouvement.setMouvementType(dto.getMouvementType());
        mouvement.setQuantity(dto.getQuantity());
        mouvement.setUnity(dto.getUnity());
        mouvement.setMouvementDate(dto.getMouvementDate());
        return mouvement;
    }
}
