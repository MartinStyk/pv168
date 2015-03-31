package com.davidmato.pv168fuelapp;

import com.davidmato.pv168fuelapp.entity.Car;
import java.util.List;

public interface CarManager {

    /**
     * Saves car into database
     *
     * @param mCar car to be saved in database
     */
    void createCar(Car mCar);

    /**
     * Updates car in database
     *
     * @param mCar car to be updated
     */
    void updateCar(Car mCar);

    /**
     * Deletes car from database
     *
     * @param mCar car to be deleted
     */
    void deleteCar(Car mCar);

    /**
     * Finds car according to id
     *
     * @param carId id of car to retrieve from database
     * @return car according to given id
     */
    Car findCarById(Long carId);

    /**
     * Finds all cars in database
     *
     * @return cars in database
     */
    List<Car> findAllCars();
}
