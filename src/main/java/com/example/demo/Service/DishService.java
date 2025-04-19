package com.example.demo.Service;

import com.example.demo.Controller.rest.CreateDishIngredient;
import com.example.demo.DAO.operations.DishDAO;
import com.example.demo.Entity.Dish;
import com.example.demo.Entity.DishIngredient;
import com.example.demo.Entity.Ingredient;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishService {
    private final DishDAO dishDAO;

    public DishService(DishDAO dishDAO) {
        this.dishDAO = dishDAO;
    }

    public Dish addDishes(int dishId, List<CreateDishIngredient> ingredients) throws SQLException {
        Dish dish = dishDAO.findById(dishId);

        List<DishIngredient> dishIngredients = ingredients.stream()
                .map(ci -> {
                    Ingredient fullIngredient = dishDAO.findIngredientById(ci.getIngredientId());
                    return new DishIngredient(
                            fullIngredient,
                            ci.getRequiredQuantity(),
                            ci.getUnity()
                    );
                })
                .collect(Collectors.toList());

        if (dish.getDishIngredients() == null) {
            dish.setDishIngredients(new ArrayList<>());
        }
        dish.getDishIngredients().addAll(dishIngredients);

        return dish;
    }

    public List<Dish> getAll() throws SQLException {
        return dishDAO.getAll();
    }

    public Dish findById(int id) throws SQLException {
        return dishDAO.findById(id);
    }
}