package com.example.demo.Controller.Mapper;

import com.example.demo.Entity.Dish;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Function;

@Component
public class DishMapper implements Function<ResultSet, Dish> {
    @Override
    public Dish apply(ResultSet resultSet) {
        try {
            Dish dish = new Dish();
            dish.setId(resultSet.getInt("id"));
            dish.setName(resultSet.getString("name"));
            dish.setUnitPrice(resultSet.getDouble("price"));
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet to Dish", e);
        }
    }
}