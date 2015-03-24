package com.davidmato.pv168fuelapp;


import common.FuelConsumptionException;
import  com.davidmato.pv168fuelapp.entity.Car;

import  com.davidmato.pv168fuelapp.entity.FillUp;

import java.sql.Date;
import java.util.List;

public interface CarRefuellingManager {
	
	 List<FillUp> findFillUpsOfCar(Car mCar);
	 List<FillUp> findFillUpsOfCar(Car mCar, Date from, Date to);
	 Car findCarWithFillUp(FillUp mFillUp);
	 Double getCarAverageFuelConsumption(Car mCar) throws FuelConsumptionException;
}
