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
        // Requête séparée pour les ingrédients de base
        String ingredientSql = "SELECT id, name, last_modification, unit_price, unity FROM ingredient";
        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(ingredientSql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                // ... autres champs de base ...

                // Chargez les prix et stocks séparément
                ingredient.setHistoricalPrices(loadHistoricalPrices(connection, ingredient.getId()));
                ingredient.setStockMouvements(loadStockMovements(connection, ingredient.getId()));

                ingredients.add(ingredient);
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ingredients", e);
        }
    }

    private List<IngredientPrice> loadHistoricalPrices(Connection connection, int ingredientId) throws SQLException {
        String sql = "SELECT * FROM ingredient_price WHERE ingredient_id = ? ORDER BY date DESC";
        List<IngredientPrice> prices = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(new IngredientPrice(
                            rs.getInt("id"),
                            rs.getDouble("price"),
                            rs.getDate("date").toLocalDate(),
                            new Ingredient(ingredientId) // Juste une référence avec l'ID
                    ));
                }
            }
        }
        return prices;
    }

    private List<StockMouvement> loadStockMovements(Connection connection, int ingredientId) throws SQLException {
        String sql = "SELECT * FROM stock_mouvement WHERE ingredient_id = ? ORDER BY mouvement_date DESC";
        List<StockMouvement> movements = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movements.add(new StockMouvement(
                            rs.getInt("id"),
                            rs.getInt("ingredient_id"),
                            MouvementType.valueOf(rs.getString("mouvement_type")),
                            rs.getDouble("quantity"),
                            Unity.valueOf(rs.getString("unity")),
                            rs.getTimestamp("mouvement_date").toLocalDateTime()
                    ));
                }
            }
        }
        return movements;
    }

    @Override
    public List<Ingredient> saveAll(List<Ingredient> entities) throws SQLException {
        List<Ingredient> savedIngredients = new ArrayList<>();
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            for (Ingredient ingredient : entities) {
                // Validation des données avant insertion
                if (ingredient.getName() == null) {
                    throw new IllegalArgumentException("Ingredient name cannot be null for id: " + ingredient.getId());
                }

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
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback(); // Rollback en cas d'erreur
            }
            throw new SQLException("Erreur lors de l'enregistrement en lot des ingrédients", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
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
        String sql = "SELECT i.id, i.name, i.last_modification, i.unit_price, i.unity, "
                + "(SELECT SUM(sm.quantity) FROM stock_mouvement sm WHERE sm.ingredient_id = i.id) AS current_stock "
                + "FROM ingredient i WHERE i.id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("id"));
                    ingredient.setName(resultSet.getString("name"));
                    ingredient.setUnitPrice(resultSet.getDouble("unit_price"));
                    ingredient.setUnity(Unity.valueOf(resultSet.getString("unity")));
                    ingredient.setHistoricalPrices(loadHistoricalPrices(connection, ingredient.getId()));
                    ingredient.setStockMouvements(loadStockMovements(connection, ingredient.getId()));
                    ingredient.getAvalaibleQuantity();

                    return ingredient;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'ingrédient par ID: " + id, e);
        }
        throw new RuntimeException("Ingrédient non trouvé avec l'ID: " + id);
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
                ingredient.setUnitPrice(rs.getDouble("unit_price"));
                ingredient.setUnity(Unity.valueOf(rs.getString("unity")));
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch basic ingredients", e);
        }
        return ingredients;
    }

    public double getCurrentStock(int ingredientId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) AS current_stock " +
                "FROM stock_mouvement " +
                "WHERE ingredient_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, ingredientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("current_stock");
                }
            }

            return 0; // Retourne 0 si aucun mouvement de stock trouvé

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du stock actuel pour l'ingrédient " + ingredientId, e);
        }
    }
}
