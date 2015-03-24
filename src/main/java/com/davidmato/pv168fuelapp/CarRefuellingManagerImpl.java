package com.davidmato.pv168fuelapp;


import common.ServiceFailureException;
import common.FuelConsumptionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import com.davidmato.pv168fuelapp.entity.FillUp;

import com.davidmato.pv168fuelapp.entity.Car;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class CarRefuellingManagerImpl implements CarRefuellingManager {

    private static final Logger logger = Logger.getLogger(
            CarRefuellingManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public List<FillUp> findFillUpsOfCar(Car car) {
        checkDataSource();
        if (car == null) {
            throw new IllegalArgumentException("car is null");
        }
        if (car.getId() == null) {
            throw new IllegalArgumentException("car id is null");
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement(
                    "SELECT fillup.id, carid, date, litres, distance "
                    + "FROM Car JOIN Fillup ON Car.id = Fillup.carid "
                    + "WHERE Car.id = ?")) {
                st.setLong(1, car.getId());
                FillUpManagerImpl fMan = new FillUpManagerImpl();
                fMan.setDataSource(dataSource);
                return fMan.getMultipleFillUpsFromQuery(st);
            }

        } catch (SQLException ex) {
            String msg = "Error when trying to find fillups of car " + car;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }

    }

    @Override
    public List<FillUp> findFillUpsOfCar(Car car, Date from, Date to) {
        List<FillUp> fillUps = findFillUpsOfCar(car);
        List<FillUp> result = new ArrayList<>();
       
        for (FillUp entry : fillUps) {
            if(entry.getDate().after(from) && entry.getDate().before(to) ){
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public Car findCarWithFillUp(FillUp fillUp) {
        return fillUp.getFilledCar();
    }

    @Override
    public Double getCarAverageFuelConsumption(Car car) throws FuelConsumptionException {
        List<FillUp> fillUps = findFillUpsOfCar(car);
        if(fillUps == null || fillUps.size() < 5){
            throw new FuelConsumptionException("not enough fillups to compute consumption");
        }
        double totalLitres = 0;
        double totalDistance = 0;
        for (FillUp fillUp : fillUps) {
            totalDistance += fillUp.getDistanceFromLastFillUp();
            totalLitres += fillUp.getLitresFilled();
        }
        return totalLitres / (totalDistance/100);
    }

}
