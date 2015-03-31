package com.davidmato.pv168fuelapp;

import common.ServiceFailureException;
import common.FuelConsumptionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import com.davidmato.pv168fuelapp.entity.FillUp;

import com.davidmato.pv168fuelapp.entity.Car;
import common.ValidationException;
import java.sql.Date;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarRefuellingManagerImpl implements CarRefuellingManager {

    private final static Logger logger = LoggerFactory.getLogger(FillUpManagerImpl.class);

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

        logger.info("finding all fillups of " + car);

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
            logger.error(msg, ex);
            throw new ServiceFailureException(msg, ex);
        }

    }

    @Override
    public List<FillUp> findFillUpsOfCar(Car car, Date from, Date to) {
        
        List<FillUp> fillUps = findFillUpsOfCar(car);
        List<FillUp> result = new ArrayList<>();

        if (from.after(to)) {
            logger.warn(from + " is after " + to);
            throw new ValidationException(from + " is after " + to);
        }

        logger.info("filter only fillups in time range");

        for (FillUp entry : fillUps) {
            if (entry.getDate().after(from) && entry.getDate().before(to)) {
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public Car findCarWithFillUp(FillUp fillUp) {
        checkDataSource();
        
        Car carFromFillUp = fillUp.getFilledCar();

        try {
            CarManagerImpl cMan = new CarManagerImpl();
            cMan.setDataSource(dataSource);
            return cMan.findCarById(carFromFillUp.getId());
        } catch (ServiceFailureException e) {
            logger.error("error getting " + carFromFillUp + " from fillup " + fillUp + "from db");
            throw new ServiceFailureException("error getting " + carFromFillUp + " from fillup " + fillUp + "from db", e);
        } catch (IllegalArgumentException e) {
            logger.error("error " + fillUp.getFilledCar() + " not in db");
            throw new IllegalArgumentException("error getting " + carFromFillUp + " from fillup " + fillUp + "from db", e);
        }
    }

    @Override
    public Double getCarAverageFuelConsumption(Car car) throws FuelConsumptionException {
        
        List<FillUp> fillUps = findFillUpsOfCar(car);
        if (fillUps == null || fillUps.size() < 5) {
            throw new FuelConsumptionException("not enough fillups to compute consumption");
        }
        double totalLitres = 0;
        double totalDistance = 0;
        for (FillUp fillUp : fillUps) {
            totalDistance += fillUp.getDistanceFromLastFillUp();
            totalLitres += fillUp.getLitresFilled();
        }
        return (100 * totalLitres) / (totalDistance);
    }

}
