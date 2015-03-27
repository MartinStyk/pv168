package com.davidmato.pv168fuelapp;

import common.DBHelper;
import common.ServiceFailureException;
import com.davidmato.pv168fuelapp.entity.Car;
import com.davidmato.pv168fuelapp.entity.CarType;
import com.davidmato.pv168fuelapp.entity.FillUp;
import com.davidmato.pv168fuelapp.entity.FuelType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CarManagerImpl implements CarManager {
    
    private final static Logger logger = LoggerFactory.getLogger(CarManagerImpl.class);
  
    private DataSource dataSource;
    
    public CarManagerImpl() {
        
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createCar(Car car) {
        checkDataSource();
        checkCar(car);
        if (car.getId() != null) {
            throw new IllegalArgumentException("id of car is set");
        }
        logger.info("Inserting "+car+"in db");
        try (Connection connection = dataSource.getConnection()) {
            
            try (PreparedStatement st = connection.prepareStatement(
                    "INSERT INTO Car (manufacturer, typename, cartype, fueltype) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                
                connection.setAutoCommit(false);
                
                st.setString(1, car.getManufacturerName());
                st.setString(2, car.getTypeName());
                st.setString(3, car.getCarType().name());
                st.setString(4, car.getFuelType().name());
                
                int count = st.executeUpdate();
                DBHelper.checkUpdatesCount(count, car, true);
                car.setId(DBHelper.getId(st.getGeneratedKeys()));
                
                connection.commit();
                
            } catch (SQLException ex) {
                connection.rollback();
                String msg = "Error when inserting "+ car +" into db";
                logger.error(msg, ex);
                throw new ServiceFailureException(msg, ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String msg = "Error in getting connection when inserting "+ car +" into db";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
        
    }
    
    @Override
    public void updateCar(Car car) {
        
        checkDataSource();
        checkCar(car);
        if (car.getId() == null) {
            throw new IllegalArgumentException("id of car is not set");
        }
        logger.info("Updating "+car+"in db");
        try (Connection connection = dataSource.getConnection()) {
            
            try (PreparedStatement st = connection.prepareStatement(
                    "UPDATE Car SET manufacturer = ?, typename = ?, cartype = ?, fueltype = ? WHERE id = ?")) {
                
                connection.setAutoCommit(false);
                
                st.setString(1, car.getManufacturerName());
                st.setString(2, car.getTypeName());
                st.setString(3, car.getCarType().name());
                st.setString(4, car.getFuelType().name());
                st.setLong(5, car.getId());
                
                int count = st.executeUpdate();
                DBHelper.checkUpdatesCount(count, car, false);
                
                connection.commit();
                
            } catch (SQLException ex) {
                connection.rollback();
                String msg = "Error when updating "+ car +" in db";
                logger.error(msg, ex);
                throw new ServiceFailureException(msg, ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String msg = "Error in getting connection when updating "+ car +" in db";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    @Override
    public void deleteCar(Car car) {
        
        checkDataSource();
        checkCar(car);
        if (car.getId() == null) {
            throw new IllegalArgumentException("car not in db or doesnt have id");
        }
        
        CarRefuellingManagerImpl refuelMan = new CarRefuellingManagerImpl();
        refuelMan.setDataSource(dataSource);
        
        FillUpManagerImpl fMan = new FillUpManagerImpl();
        fMan.setDataSource(dataSource);
        
        logger.info("Deleting fillups of "+car+" in db");
        
        List<FillUp> fillUps = refuelMan.findFillUpsOfCar(car);
        for(FillUp fUp : fillUps){
            fMan.deleteFillUp(fUp);
        }
        
        
        logger.info("Deleting "+car+"in db");
        try (Connection connection = dataSource.getConnection()) {
            
            try (PreparedStatement st = connection.prepareStatement("DELETE FROM Car WHERE id = ?")) {
                
                connection.setAutoCommit(false);
                
                st.setLong(1, car.getId());
                
                int count = st.executeUpdate();
                DBHelper.checkUpdatesCount(count, car, false);
                
                connection.commit();
                
            } catch (SQLException ex) {
                connection.rollback();
                String msg = "Error when deleting "+ car +" from the db";
                logger.error(msg, ex);
                throw new ServiceFailureException(msg, ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String msg = "Error in getting connection when deleting "+ car +" from the db";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    @Override
    public Car findCarById(Long carId) {
        
        checkDataSource();
        
        if (carId == null) {
            throw new IllegalArgumentException("id of car is null");
        }
        logger.info("Finding car id "+carId+" in db");
        try (Connection connection = dataSource.getConnection()) {
            
            try (PreparedStatement st = connection.prepareStatement(
                    "SELECT id, manufacturer, typename, cartype, fueltype FROM Car WHERE id = ?")) {
                st.setLong(1, carId);
                return getSingleCarFromQuery(st);
            }
            
        } catch (SQLException ex) {
            String msg = "Error when getting car with id = " + carId + " from DB";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    @Override
    public List<Car> findAllCars() {
        
        checkDataSource();
        
        logger.info("Finding all cars in db");
        
        try (Connection connection = dataSource.getConnection()) {
            
            try (PreparedStatement st = connection.prepareStatement(
                    "SELECT id, manufacturer, typename, cartype, fueltype FROM Car")) {
                return getMultipleCarsFromQuery(st);
            }
        } catch (SQLException ex) {
            String msg = "Error when getting all car from DB";
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    private void checkCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("car is null");
        }
        if (car.getManufacturerName() == null) {
            throw new IllegalArgumentException("manufacturer name is null");
        }
        if (car.getTypeName() == null) {
            throw new IllegalArgumentException("type name is null");
        }
        if (car.getFuelType() == null) {
            throw new IllegalArgumentException("fuel type is null");
        }
        if (car.getCarType() == null) {
            throw new IllegalArgumentException("car type is null");
        }
        if (car.getManufacturerName().length() == 0) {
            throw new IllegalArgumentException("manufacturer name is empty");
        }
        if (car.getTypeName().length() == 0) {
            throw new IllegalArgumentException("type name is empty");
        }
        
    }
    
    private static Car getSingleCarFromQuery(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Car result = rowToCar(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more cars with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    private static List<Car> getMultipleCarsFromQuery(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Car> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToCar(rs));
        }
        return result;
    }
    
    private static Car rowToCar(ResultSet rs) throws SQLException {
       
        logger.info("Getting car from result set");
        
        Car result = new Car();
        result.setId(rs.getLong("id"));
        result.setManufacturerName(rs.getString("manufacturer"));
        result.setTypeName(rs.getString("typename"));
        result.setCarType(CarType.valueOf(rs.getString("cartype")));
        result.setFuelType(FuelType.valueOf(rs.getString("fueltype")));
        return result;
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            logger.debug("data source check failed");
            throw new IllegalStateException("DataSource not set");
        }
    }
    
}
