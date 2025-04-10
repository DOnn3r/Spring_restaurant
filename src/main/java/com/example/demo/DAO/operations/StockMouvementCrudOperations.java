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
        String sql = "SELECT * FROM stock_mouvement";
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
        String sql = "INSERT INTO stock_mouvement (id, id_ingredient, quantity, unit, mouvement_type, mouvement_date) " +
                "VALUES (?, ?, ?, ?::unit, ?::mouvement_type, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "id_ingredient = EXCLUDED.id_ingredient, " +
                "quantity = EXCLUDED.quantity, " +
                "unit = EXCLUDED.unit, " +
                "mouvement_type = EXCLUDED.mouvement_type, " +
                "mouvement_date = EXCLUDED.mouvement_date " +
                "RETURNING id, id_ingredient, quantity, unit, mouvement_type, mouvement_date";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            try {
                for (StockMouvement mouvement : entities) {
                    statement.setInt(1, mouvement.getId());
                    statement.setInt(2, mouvement.getIngredientId());
                    statement.setDouble(3, mouvement.getQuantity());
                    statement.setString(4, mouvement.getUnity().name());
                    statement.setString(5, mouvement.getMouvementType().name());
                    statement.setTimestamp(6, Timestamp.valueOf(mouvement.getMouvementDate()));

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            savedMouvements.add(mapToStockMouvement(rs));
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
        String sql = "SELECT id, id_ingredient, quantity, unit, mouvement_type, mouvement_date " +
                "FROM stock_mouvement WHERE id_ingredient = ?";

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
                rs.getInt("id"),
                rs.getInt("id_ingredient"),
                MouvementType.valueOf(rs.getString("mouvement_type")),
                rs.getDouble("quantity"),
                Unity.valueOf(rs.getString("unit")),
                rs.getTimestamp("mouvement_date").toLocalDateTime()
        );
    }

    @Override
    public StockMouvement findByName(String name) throws SQLException {
        throw new UnsupportedOperationException("Method not supported for StockMouvementCrudOperations");
    }
}