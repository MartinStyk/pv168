package com.davidmato.pv168fuelapp;

import com.davidmato.pv168fuelapp.entity.CarType;
import com.davidmato.pv168fuelapp.entity.FuelType;
import com.davidmato.pv168fuelapp.entity.Car;
import com.davidmato.pv168fuelapp.entity.FillUp;
import common.DBHelper;
import common.FuelConsumptionException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import common.ValidationException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CarRefuellingManagerImplTest {

    private FillUpManagerImpl fillManager;
    private CarManagerImpl carManager;
    private CarRefuellingManagerImpl refuelManager;

    @Rule
    public ExpectedException expExcpetion = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {

        refuelManager = new CarRefuellingManagerImpl();
        refuelManager.setDataSource(DBHelper.getDataSource());

        fillManager = new FillUpManagerImpl();
        fillManager.setDataSource(DBHelper.getDataSource());
        carManager = new CarManagerImpl();
        carManager.setDataSource(DBHelper.getDataSource());

        URL urlToScript = CarManagerImpl.class.getResource("/createTables.sql");
        DBHelper.executeSqlScript(DBHelper.getDataSource(), urlToScript);

    }

    @After
    public void tearDown() throws SQLException {
        URL urlToScript = CarManagerImpl.class.getResource("/dropTables.sql");
        DBHelper.executeSqlScript(DBHelper.getDataSource(), urlToScript);
    }

    @Test
    public void findFillUpsOfCarTest() {
        Car car = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car);
        FillUp fUp = FillUpManagerImplTest.newFillUp(new Date(1), 10, 100, car);
        fillManager.createFillUp(fUp);
        FillUp fUp2 = FillUpManagerImplTest.newFillUp(new Date(81), 108, 1008, car);
        fillManager.createFillUp(fUp2);

        Car evilCar = CarManagerImplTest.newCar("Skoda", "Fabia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(evilCar);
        FillUp evilFUip = FillUpManagerImplTest.newFillUp(new Date(1), 500, 666, evilCar);
        fillManager.createFillUp(evilFUip);

        List<FillUp> expected = Arrays.asList(fUp, fUp2);
        List<FillUp> actual = refuelManager.findFillUpsOfCar(car);

        Collections.sort(actual, FillUpManagerImplTest.idComparator);
        Collections.sort(expected, FillUpManagerImplTest.idComparator);

        assertEquals(expected, actual);
    }

    @Test
    public void findFillUpsOfCarInTimeRangeTest() {
        Car car = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car);
        FillUp fUp = FillUpManagerImplTest.newFillUp(new Date(1000), 10, 100, car);
        fillManager.createFillUp(fUp);
        FillUp fUp2 = FillUpManagerImplTest.newFillUp(new Date(86400000), 108, 1008, car);
        fillManager.createFillUp(fUp2);
        FillUp fUp3 = FillUpManagerImplTest.newFillUp(new Date(88800000), 1009, 8, car);
        fillManager.createFillUp(fUp3);
        FillUp fUp4 = FillUpManagerImplTest.newFillUp(new Date(999999999), 1009, 8, car);
        fillManager.createFillUp(fUp4);

        Car evilCar = CarManagerImplTest.newCar("Skoda", "Fabia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(evilCar);
        FillUp evilFUip = FillUpManagerImplTest.newFillUp(new Date(1), 500, 666, evilCar);
        fillManager.createFillUp(evilFUip);

        List<FillUp> expected = Arrays.asList(fUp3, fUp2);
        List<FillUp> actual = refuelManager.findFillUpsOfCar(car, new Date(1), new Date(300000000));

        Collections.sort(actual, FillUpManagerImplTest.idComparator);
        Collections.sort(expected, FillUpManagerImplTest.idComparator);

        assertEquals(expected, actual);
    }

    @Test
    public void findFillUpsOfCarInTimeRangeTest_wrongDates() {
        Car car = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car);

        expExcpetion.expect(ValidationException.class);
        refuelManager.findFillUpsOfCar(car, new Date(300000000), new Date(1));

    }

    @Test
    public void findCarWithFillUp() {
        Car car = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car);
        Car car2 = CarManagerImplTest.newCar("Skoda", "Rapid", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car2);
        FillUp fUp = FillUpManagerImplTest.newFillUp(new Date(1), 10, 100, car);
        fillManager.createFillUp(fUp);

        Car resultCar = refuelManager.findCarWithFillUp(fUp);

        assertEquals(car, resultCar);
        assertNotSame(car, resultCar);
        CarManagerImplTest.assertDeepEquals(car, resultCar);
    }

    @Test
    public void getCarAverageFuelConsumption() throws FuelConsumptionException {
        Car car = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car);
        FillUp fUp = FillUpManagerImplTest.newFillUp(new Date(1), 10, 100, car);
        fillManager.createFillUp(fUp);
        fUp = FillUpManagerImplTest.newFillUp(new Date(1), 10, 200, car);
        fillManager.createFillUp(fUp);
        fUp = FillUpManagerImplTest.newFillUp(new Date(1), 20, 100, car);
        fillManager.createFillUp(fUp);
        fUp = FillUpManagerImplTest.newFillUp(new Date(1), 20, 1000, car);
        fillManager.createFillUp(fUp);
        fUp = FillUpManagerImplTest.newFillUp(new Date(1), 80, 1000, car);
        fillManager.createFillUp(fUp);

        double consumption = refuelManager.getCarAverageFuelConsumption(car);

        assertEquals(5.83, consumption, 0.05);
    }

    @Test
    public void getCarAverageFuelConsumption_notEnaughData() throws FuelConsumptionException {
        Car car = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        carManager.createCar(car);
        FillUp fUp = FillUpManagerImplTest.newFillUp(new Date(1), 10, 100, car);
        fillManager.createFillUp(fUp);

        expExcpetion.expect(common.FuelConsumptionException.class);

        double consumption = refuelManager.getCarAverageFuelConsumption(car);

    }
}
