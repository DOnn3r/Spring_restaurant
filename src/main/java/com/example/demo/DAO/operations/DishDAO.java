package com.example.demo.DAO.operations;

import com.example.demo.DAO.DataSource;
import com.example.demo.Entity.Dish;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishDAO implements CrudOperation<Dish>{
    private DataSource dataSource;

    public DishDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DishDAO(){
        this.dataSource = new DataSource();
    };

    @Override
    public List<Dish> getAll(){
        String sql = "select d.id, d.name, d.price from dish d";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Dish> dishes = new ArrayList<>();
                while (resultSet.next() == true) {
                    Dish dish = new Dish();
                    dish.setId(resultSet.getInt("id"));
                    dish.setName(resultSet.getString("name"));
                    dish.setUnitPrice(resultSet.getDouble("price"));
                    dishes.add(dish);
                }
                return dishes;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Dish> saveAll(List<Dish> entities) throws SQLException {
        return List.of();
    }

    @Override
    public Dish findByName(String name) throws SQLException {
        return null;
    }


}