package com.davidmato.pv168fuelapp;

import common.DBHelper;
import common.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.davidmato.pv168fuelapp.entity.FillUp;

import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FillUpManagerImpl implements FillUpManager {

    private final static Logger logger = LoggerFactory.getLogger(FillUpManagerImpl.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createFillUp(FillUp fillUp) {
        checkDataSource();
        checkFillUp(fillUp);
        if (fillUp.getId() != null) {
            throw new IllegalArgumentException("id of fillUp is already set");
        }

        logger.info("adding " + fillUp + "in db");

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement st = connection.prepareStatement(
                    "INSERT INTO FillUp (carid, date, litres, distance) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                connection.setAutoCommit(false);

                st.setLong(1, fillUp.getFilledCar().getId());
                st.setDate(2, fillUp.getDate());
                st.setDouble(3, fillUp.getLitresFilled());
                st.setDouble(4, fillUp.getDistanceFromLastFillUp());

                int count = st.executeUpdate();
                DBHelper.checkUpdatesCount(count, fillUp, true);
                fillUp.setId(DBHelper.getId(st.getGeneratedKeys()));

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                String msg = "Error when inserting " + fillUp + " into db";
                logger.error(msg, ex);
                throw new ServiceFailureException(msg, ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String msg = "Error in getting connection while inserting " + fillUp + " into db";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }

    }

    @Override
    public void updateFillUp(FillUp fillUp) {
        checkDataSource();
        checkFillUp(fillUp);
        if (fillUp.getId() == null) {
            throw new IllegalArgumentException("id of fill up is not set");
        }

        logger.info("updating " + fillUp + "in db");

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement st = connection.prepareStatement(
                    "UPDATE FillUp SET carid = ?, date = ?, litres = ?, distance = ? WHERE id = ?")) {

                connection.setAutoCommit(false);

                st.setLong(1, fillUp.getFilledCar().getId());
                st.setDate(2, fillUp.getDate());
                st.setDouble(3, fillUp.getLitresFilled());
                st.setDouble(4, fillUp.getDistanceFromLastFillUp());
                st.setLong(5, fillUp.getId());

                int count = st.executeUpdate();
                DBHelper.checkUpdatesCount(count, fillUp, false);

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                String msg = "Error when updating fillup in db";
                logger.error(msg, ex);
                throw new ServiceFailureException(msg, ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String msg = "Error in getting connection while updating " + fillUp + " into db";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }

    @Override
    public void deleteFillUp(FillUp fillUp) {
        checkDataSource();
        checkFillUp(fillUp);
        if (fillUp.getId() == null) {
            throw new IllegalArgumentException("id is null");
        }

        logger.info("deleting " + fillUp + "in db");

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement st = connection.prepareStatement("DELETE FROM FillUp WHERE id = ?")) {

                connection.setAutoCommit(false);

                st.setLong(1, fillUp.getId());

                int count = st.executeUpdate();
                DBHelper.checkUpdatesCount(count, fillUp, false);

                connection.commit();

            } catch (SQLException ex) {
                connection.rollback();
                String msg = "Error when deleting fillup from the db";
                logger.error(msg, ex);
                throw new ServiceFailureException(msg, ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String msg = "Error in getting connection";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }

    @Override
    public FillUp findFillUpById(Long id) {
        checkDataSource();

        if (id == null) {
            throw new IllegalArgumentException("id of fill up is null");
        }

        logger.info("finding fillup id " + id + " in db");

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement st = connection.prepareStatement(
                    "SELECT id, carid, date, litres, distance FROM fillUp WHERE id = ?")) {
                st.setLong(1, id);
                return getSingleFillUpFromQuery(st);
            }

        } catch (SQLException ex) {
            String msg = "Error when getting fill up id" + id + " from DB";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }

    @Override
    public List<FillUp> findAllFillUps() {
        checkDataSource();

        logger.info("finding all fill ups in db");

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement st = connection.prepareStatement(
                    "SELECT id, carid, date, litres, distance FROM FillUp")) {
                return getMultipleFillUpsFromQuery(st);
            }
        } catch (SQLException ex) {
            String msg = "Error when getting all fillups from DB";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }

    private void checkFillUp(FillUp fillUp) {
        if (fillUp == null) {
            throw new IllegalArgumentException("fillUp is null");
        }
        if (fillUp.getFilledCar() == null) {
            throw new IllegalArgumentException("filedcar is null");
        }
        if (fillUp.getFilledCar().getId() == null) {
            throw new IllegalArgumentException("filedcar is not in db or doesnt have id");
        }
        if (fillUp.getDate() == null) {
            throw new IllegalArgumentException("date is null");
        }
        if (fillUp.getLitresFilled() <= 0) {
            throw new IllegalArgumentException("litres filled is <= 0");
        }
        if (fillUp.getDistanceFromLastFillUp() <= 0) {
            throw new IllegalArgumentException("distance from last fillup is <= 0");
        }
    }

    protected FillUp getSingleFillUpFromQuery(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            FillUp result = rowToFillUp(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more fillups with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }

    protected List<FillUp> getMultipleFillUpsFromQuery(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<FillUp> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToFillUp(rs));
        }
        return result;
    }

    private FillUp rowToFillUp(ResultSet rs) throws SQLException {
        FillUp result = new FillUp();
        result.setId(rs.getLong("id"));
        result.setDate(rs.getDate("date"));
        result.setLitresFilled(rs.getDouble("litres"));
        result.setDistanceFromLastFillUp(rs.getDouble("distance"));

        CarManagerImpl carMng = new CarManagerImpl();
        carMng.setDataSource(dataSource);

        result.setFilledCar(carMng.findCarById(rs.getLong("carid")));

        return result;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            logger.debug("data source check failed");
            throw new IllegalStateException("DataSource not set");
        }
    }
}
