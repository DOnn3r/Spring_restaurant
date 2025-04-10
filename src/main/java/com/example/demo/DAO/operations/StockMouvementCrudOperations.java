package com.example.demo.DAO.operations;

import com.example.demo.DAO.DataSource;
import com.example.demo.Entity.MouvementType;
import com.example.demo.Entity.StockMouvement;
import com.example.demo.Entity.Unity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockMouvementCrudOperations implements CrudOperation<StockMouvement> {
    private DataSource dataSource;

    public StockMouvementCrudOperations(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<StockMouvement> getAll() throws SQLException {
        String sql = "SELECT id, ingredient_id, mouvement_type, quantity, unity, mouvement_date FROM stock_mouvement";
        List<StockMouvement> mouvements = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                mouvements.add(mapToStockMouvement(rs));
            }
        }
        return mouvements;
    }

    @Override
    public List<StockMouvement> saveAll(List<StockMouvement> entities) throws SQLException {
        List<StockMouvement> savedMouvements = new ArrayList<>();
        String sql = "INSERT INTO stock_mouvement (id, ingredient_id, quantity, unity, mouvement_type, mouvement_date) " +
                "VALUES (?, ?, ?, ?::unit, ?::mouvement_type, ?) " +
                "RETURNING id"; // Important: bien retourner l'ID

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            try {
                for (StockMouvement mouvement : entities) {
                    // Vérification que l'ID est bien défini
                    if (mouvement.getId() <= 0) {
                        throw new SQLException("ID must be manually set and positive");
                    }

                    statement.setInt(1, mouvement.getId()); // ID manuel
                    statement.setInt(2, mouvement.getIngredientId());
                    statement.setDouble(3, mouvement.getQuantity());
                    statement.setString(4, mouvement.getUnity().name());
                    statement.setString(5, mouvement.getMouvementType().name());
                    statement.setTimestamp(6, Timestamp.valueOf(mouvement.getMouvementDate()));

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            mouvement.setId(rs.getInt("id")); // Confirmation de l'ID
                            savedMouvements.add(mouvement);
                        }
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
        return savedMouvements;
    }

    public List<StockMouvement> findByIdIngredient(int idIngredient) throws SQLException {
        List<StockMouvement> mouvements = new ArrayList<>();
        String sql = "SELECT id, ingredient_id, mouvement_type, quantity, unity, mouvement_date " +
                "FROM stock_mouvement WHERE ingredient_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idIngredient);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    mouvements.add(mapToStockMouvement(rs));
                }
            }
        }
        return mouvements;
    }

    private StockMouvement mapToStockMouvement(ResultSet rs) throws SQLException {
        return new StockMouvement(
                rs.getInt("id"),               // ID manuel bien récupéré
                rs.getInt("ingredient_id"),
                MouvementType.valueOf(rs.getString("mouvement_type")),
                rs.getDouble("quantity"),
                Unity.valueOf(rs.getString("unity")), // Vérifiez bien "unity" et non "unit"
                rs.getTimestamp("mouvement_date").toLocalDateTime()
        );
    }

    @Override
    public StockMouvement findByName(String name) throws SQLException {
        throw new UnsupportedOperationException("Method not supported for StockMouvementCrudOperations");
    }
}