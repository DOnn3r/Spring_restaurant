package com.example.demo.DAO.operations;

import com.example.demo.Entity.*;
import com.example.demo.DAO.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class IngredientDAO implements CrudOperation<Ingredient> {
    private DataSource dataSource;
    private PriceCrudOperations priceCrudOperations;
    private StockMouvementCrudOperations stockMouvementCrudOperations;



    public IngredientDAO(){
        this.dataSource = new DataSource();
    };

    @Override
    public List<Ingredient> getAll() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, last_modification, unit_price, unity FROM ingredient";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));

                Timestamp timestamp = rs.getTimestamp("last_modification");
                if (timestamp != null) {
                    ingredient.setLastModification(timestamp.toLocalDateTime());
                }

                ingredient.setUnitPrice(rs.getDouble("unit_price"));

                String unityStr = rs.getString("unity");
                if (unityStr != null) {
                    ingredient.setUnity(Unity.valueOf(unityStr));
                }

                ingredients.add(ingredient);
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ingredients", e);
        }
    }

    @Override
    public List<Ingredient> saveAll(List<Ingredient> entities) throws SQLException {
        List<Ingredient> savedIngredients = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            for (Ingredient ingredient : entities) {
                String sql = "INSERT INTO ingredient (id, name, last_modification, unit_price, unity) " +
                        "VALUES (?, ?, ?, ?, ?::unit) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "name = EXCLUDED.name, " +
                        "last_modification = EXCLUDED.last_modification, " +
                        "unit_price = EXCLUDED.unit_price, " +
                        "unity = EXCLUDED.unity " +
                        "RETURNING id";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, ingredient.getId());
                    statement.setString(2, ingredient.getName());
                    statement.setTimestamp(3, Timestamp.valueOf(ingredient.getLastModification()));
                    statement.setDouble(4, ingredient.getUnitPrice());
                    statement.setObject(5, ingredient.getUnity() != null ?
                            ingredient.getUnity().name() : null, Types.OTHER);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            ingredient.setId(rs.getInt(1));
                            savedIngredients.add(ingredient);
                        }
                    }
                }
            }

            connection.commit(); // Valide la transaction après tous les inserts
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'enregistrement en lot des ingrédients", e);
        }

        return savedIngredients;
    }

    public Ingredient saveOne(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredient (id, name, last_modification, unit_price, unity) " +
                "VALUES (?, ?, ?, ?, ?::unit) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, " +
                "last_modification = EXCLUDED.last_modification, " +
                "unit_price = EXCLUDED.unit_price, " +
                "unity = EXCLUDED.unity " +
                "RETURNING id";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            statement.setInt(1, ingredient.getId());
            statement.setString(2, ingredient.getName());
            statement.setTimestamp(3, Timestamp.valueOf(ingredient.getLastModification()));
            statement.setDouble(4, ingredient.getUnitPrice());
            statement.setObject(5, ingredient.getUnity() != null ?
                    ingredient.getUnity().name() : null, Types.OTHER);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    ingredient.setId(rs.getInt(1));
                }
            }

            connection.commit();
            return ingredient;
        }
    }

    @Override
    public Ingredient findByName(String name) {
        throw new Error("Not implemented");
    }

    public Ingredient findById(int id) {
        String sql = "SELECT id, name, last_modification, unit_price, unity FROM ingredient WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Ingredient ingredient = new Ingredient(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getTimestamp("last_modification").toLocalDateTime(),
                            resultSet.getDouble("unit_price"),
                            Unity.valueOf(resultSet.getString("unity"))
                    );

                    return ingredient;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'ingrédient par nom", e);
        }
        return null;
    }

    private List<IngredientPrice> loadHistoricalPrices(Connection connection, int ingredientId) throws SQLException {
        String sql = "SELECT * FROM ingredient_price WHERE ingredient_id = ? ORDER BY date ASC";
        List<IngredientPrice> historicalPrices = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historicalPrices.add(new IngredientPrice(
                            rs.getInt("id"),
                            rs.getDouble("price"),
                            rs.getDate("date").toLocalDate()
                    ));
                }
            }
        }

        return historicalPrices;
    }


    public List<Ingredient> getFilteredIngredientByPrice(double minPrice, double maxPrice) {
        String sql = "select i.id, i.name, i.last_modification, i.unit_price, i.unity from ingredient i where i.unit_price between ? and ?";
        if (minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice > maxPrice");
        }
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, minPrice);
            statement.setDouble(2, maxPrice);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();
                while (resultSet.next() == true) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("id"));
                    ingredient.setName(resultSet.getString("name"));
                    ingredient.setUnitPrice(resultSet.getDouble("unit_price"));
                    ingredient.setLastModification(resultSet.getTimestamp("last_modification").toLocalDateTime());
                    ingredient.setUnity(Unity.valueOf(resultSet.getString("unity")));
                    ingredients.add(ingredient);
                }
                return ingredients;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Ingredient> getFilteredIngredient(double minPrice) {
        String sql = "select i.id, i.name, i.last_modification, i.unit_price, i.unity from ingredient i where i.unit_price = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, minPrice);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();
                while (resultSet.next() == true) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("id"));
                    ingredient.setName(resultSet.getString("name"));
                    ingredient.setUnitPrice(resultSet.getDouble("unit_price"));
                    ingredient.setLastModification(resultSet.getTimestamp("last_modification").toLocalDateTime());
                    ingredient.setUnity(Unity.valueOf(resultSet.getString("unity")));
                    ingredients.add(ingredient);
                }
                return ingredients;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> getBasicIngredients() {
        String sql = "SELECT id, name, last_modification, unit_price, unity FROM ingredient";
        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setLastModification(rs.getTimestamp("last_modification").toLocalDateTime());
                ingredient.setUnitPrice(rs.getDouble("unit_price"));
                ingredient.setUnity(Unity.valueOf(rs.getString("unity")));
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch basic ingredients", e);
        }
        return ingredients;
    }
}
