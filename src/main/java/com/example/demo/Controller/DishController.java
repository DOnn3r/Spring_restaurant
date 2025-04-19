package com.example.demo.Controller;

import com.example.demo.Controller.rest.CreateDishIngredient;
import com.example.demo.Controller.rest.DishAvailabilityResponse;
import com.example.demo.Entity.Dish;
import com.example.demo.Entity.DishIngredient;
import com.example.demo.Service.DishService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dishes")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public List<Dish> getDishes() throws SQLException {
        return dishService.getAll();
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> addIngredientsInDish(
            @PathVariable int id,
            @RequestBody List<CreateDishIngredient> ingredients) {

        try {
            Dish updatedDish = dishService.addDishes(id, ingredients);
            return ResponseEntity.ok(updatedDish);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Erreur base de données",
                            "message", e.getMessage()
                    ));
        }
    }
}