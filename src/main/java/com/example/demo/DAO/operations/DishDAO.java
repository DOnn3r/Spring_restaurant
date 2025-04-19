package com.example.demo.DAO.operations;

import com.example.demo.Controller.Mapper.DishMapper;
import com.example.demo.DAO.DataSource;
import com.example.demo.Entity.Dish;
import com.example.demo.Entity.DishIngredient;
import com.example.demo.Entity.Ingredient;
import com.example.demo.Entity.Unity;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishDAO implements CrudOperation<Dish>{
    private DataSource dataSource;
    private final DishMapper dishMapper;

    public DishDAO(DataSource dataSource, DishMapper dishMapper) {
        this.dataSource = dataSource;
        this.dishMapper = dishMapper;
    }

    public DishDAO() {
        this.dataSource = new DataSource();
        this.dishMapper = new DishMapper();
    }

    @Override
    public List<Dish> getAll() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String dishSql = "SELECT d.id, d.name, d.price FROM dish d";
        String ingredientSql = "SELECT i.id, i.name, i.unit_price, i.unity, " +
                "(SELECT SUM(sm.quantity) FROM stock_mouvement sm WHERE sm.ingredient_id = i.id) as current_stock, " +
                "di.required_quantity " +
                "FROM dish_ingredient di " +
                "JOIN ingredient i ON di.ingredient_id = i.id " +
                "WHERE di.dish_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement dishStatement = connection.prepareStatement(dishSql);
             ResultSet dishResultSet = dishStatement.executeQuery()) {

            while (dishResultSet.next()) {
                Dish dish = dishMapper.apply(dishResultSet);

                try (PreparedStatement ingredientStatement = connection.prepareStatement(ingredientSql)) {
                    ingredientStatement.setInt(1, dish.getId());
                    try (ResultSet ingredientResultSet = ingredientStatement.executeQuery()) {
                        List<DishIngredient> ingredients = new ArrayList<>();

                        while (ingredientResultSet.next()) {
                            Ingredient ingredient = new Ingredient();
                            ingredient.setId(ingredientResultSet.getInt("id"));
                            ingredient.setName(ingredientResultSet.getString("name"));
                            ingredient.setUnitPrice(ingredientResultSet.getDouble("unit_price"));
                            ingredient.setUnity(Unity.valueOf(ingredientResultSet.getString("unity")));
                            double requiredQuantity = ingredientResultSet.getDouble("required_quantity");

                            ingredients.add(new DishIngredient(ingredient, requiredQuantity, ingredient.getUnity()));
                        }

                        dish.setDishIngredients(ingredients);
                        dish.getAvailableQuantity();
                    }
                }
                dishes.add(dish);
            }
        }
        return dishes;
    }

    @Override
    public List<Dish> saveAll(List<Dish> entities) throws SQLException {
        return List.of();
    }

    @Override
    public Dish findByName(String name) throws SQLException {
        return null;
    }

    public Dish findById(int id) throws SQLException {
        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("select id,name,price from dish where id=?"))
        {
            ps.setInt(1,id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) {
                    return dishMapper.apply(rs);
                }
                throw new RuntimeException("Dish.id=" + id + " not found");
            }
        }
    }

    public Ingredient findIngredientById(Integer ingredientId) {
        String sql = "SELECT i.id, i.name, i.unit_price, i.unity FROM ingredient i WHERE i.id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setUnitPrice(rs.getDouble("unit_price"));
                    ingredient.setUnity(Unity.valueOf(rs.getString("unity")));
                    return ingredient;
                }
                throw new RuntimeException("Ingredient not found with id: " + ingredientId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}