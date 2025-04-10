package com.example.demo.DAO.operations;

import com.example.demo.DAO.DataSource;
import com.example.demo.Entity.Ingredient;
import com.example.demo.Entity.IngredientPrice;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PriceCrudOperations implements CrudOperation<IngredientPrice> {
    private DataSource dataSource;

    public PriceCrudOperations(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<IngredientPrice> getAll() throws SQLException {
        String sql = "SELECT ip.*, i.id as ingredient_id, i.name as ingredient_name " +
                "FROM ingredient_price ip " +
                "JOIN ingredient i ON ip.ingredient_id = i.id";

        List<IngredientPrice> prices = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("ingredient_id"));
                ingredient.setName(rs.getString("ingredient_name"));

                IngredientPrice price = new IngredientPrice(
                        rs.getInt("id"),
                        rs.getDouble("price"),
                        rs.getDate("date").toLocalDate()
                );
                price.setIngredient(ingredient);

                prices.add(price);
            }
        }
        return prices;
    }

    @Override
    public List<IngredientPrice> saveAll(List<IngredientPrice> entities) throws SQLException {
        List<IngredientPrice> savedPrices = new ArrayList<>();
        String updateSql = "UPDATE ingredient SET unit_price = ? WHERE id = ?";
        String insertSql = "INSERT INTO ingredient_price (ingredient_id, price, date) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                for (IngredientPrice entity : entities) {
                    // Update current price in ingredient table
                    updateStmt.setDouble(1, entity.getPrice());
                    updateStmt.setInt(2, entity.getIngredient().getId());
                    updateStmt.addBatch();

                    // Insert historical price
                    insertStmt.setInt(1, entity.getIngredient().getId());
                    insertStmt.setDouble(2, entity.getPrice());
                    insertStmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    insertStmt.addBatch();

                    savedPrices.add(entity);
                }

                updateStmt.executeBatch();
                insertStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return savedPrices;
    }

    public List<IngredientPrice> findByIdIngredient(long id) throws SQLException {
        List<IngredientPrice> prices = new ArrayList<>();
        String sql = "SELECT id, ingredient_id, price, date FROM ingredient_price WHERE ingredient_id = ? ORDER BY date DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    prices.add(new IngredientPrice(
                            rs.getInt("id"),
                            rs.getDouble("price"),
                            rs.getDate("date").toLocalDate()
                    ));
                }
            }
        }
        return prices;
    }

    @Override
    public IngredientPrice findByName(String name) throws SQLException {
        throw new UnsupportedOperationException("Method not supported for PriceCrudOperations");
    }

    public IngredientPrice createPrice(IngredientPrice price) throws SQLException {
        String sql = "INSERT INTO ingredient_price (ingredient_id, price, date) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, price.getIngredient().getId());
            stmt.setDouble(2, price.getPrice());
            stmt.setDate(3, java.sql.Date.valueOf(price.getDate()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    price.setId(rs.getInt("id"));
                    return price;
                }
            }
            throw new SQLException("Failed to create price, no ID returned");
        }
    }
}