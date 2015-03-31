package com.davidmato.pv168fuelapp;

import common.ServiceFailureException;
import java.sql.SQLException;
import com.davidmato.pv168fuelapp.entity.CarType;
import com.davidmato.pv168fuelapp.entity.FuelType;
import com.davidmato.pv168fuelapp.entity.Car;
import common.DBHelper;
import java.net.URL;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.After;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CarManagerImplTest {

    private CarManagerImpl manager;

    @Before
    public void setUp() throws SQLException {

        URL urlToScript = CarManagerImpl.class.getResource("/createTables.sql");
        DBHelper.executeSqlScript(DBHelper.getDataSource(), urlToScript);
        manager = new CarManagerImpl();
        manager.setDataSource(DBHelper.getDataSource());
    }

    @After
    public void tearDown() throws SQLException {
        URL urlToScript = CarManagerImpl.class.getResource("/dropTables.sql");
        DBHelper.executeSqlScript(DBHelper.getDataSource(), urlToScript);
    }

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void createCar() {

        Car car = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.DIESEL);
        manager.createCar(car);

        Long carId = car.getId();
        assertNotNull(carId);

        Car resultCar = manager.findCarById(carId);
        assertEquals(car, resultCar);
        assertNotSame(car, resultCar);
        assertDeepEquals(car, resultCar);
    }

    @Test
    public void createNullCar() {
        expException.expect(IllegalArgumentException.class);
        manager.createCar(null);
    }

    @Test
    public void createCarWithModifiedId() {

        Car car = newCar("BMW", "M3", CarType.SPORT, FuelType.PETROL);
        car.setId(1l);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void createCarWithNullManufacturerName() {

        Car car = newCar(null, "M3", CarType.SPORT, FuelType.PETROL);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void createCarWithNullTypeName() {

        Car car = newCar("BMW", null, CarType.SPORT, FuelType.PETROL);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void createCarWithWrongManufacturerName() {

        Car car = newCar("", "M3", CarType.SPORT, FuelType.PETROL);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void createCarWithWrongTypeName() {

        Car car = newCar("BMW", "", CarType.SPORT, FuelType.PETROL);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void createCarWithNullCarType() {

        Car car = newCar("BMW", "M3", null, FuelType.PETROL);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void createCarWithNullFuelType() {

        Car car = newCar("BMW", "M3", CarType.SPORT, null);

        expException.expect(IllegalArgumentException.class);
        manager.createCar(car);
    }

    @Test
    public void findCarById() {

        assertNull(manager.findCarById(1098l));

        Car car = newCar("BMW", "M3", CarType.SPORT, FuelType.PETROL);
        manager.createCar(car);
        Long carId = car.getId();

        Car resultCar = manager.findCarById(carId);
        assertEquals(car, resultCar);
        assertDeepEquals(car, resultCar);
    }

    @Test
    public void getAllCars() {

        assertTrue(manager.findAllCars().isEmpty());

        Car car1 = newCar("BMW", "M3", CarType.SPORT, FuelType.PETROL);
        Car car2 = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.DIESEL);
        Car car3 = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);

        manager.createCar(car1);
        manager.createCar(car2);
        manager.createCar(car3);

        List<Car> expected = Arrays.asList(car1, car2, car3);
        List<Car> actual = manager.findAllCars();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertCollectionDeepEquals(expected, actual);
    }

    @Test
    public void updateCar() {
        
        Car car1 = newCar("Skoda", "Favorit", CarType.HATCHBACK, FuelType.PETROL);
        Car car2 = newCar("Tatra", "V3S", CarType.VAN, FuelType.DIESEL);
        manager.createCar(car1);
        manager.createCar(car2);

        Long car1Id = car1.getId();

        car1 = manager.findCarById(car1Id);
        car1.setManufacturerName("Skodovka");
        manager.updateCar(car1);
        assertEquals("Skodovka", car1.getManufacturerName());
        assertEquals("Favorit", car1.getTypeName());
        assertEquals(CarType.HATCHBACK, car1.getCarType());
        assertEquals(FuelType.PETROL, car1.getFuelType());

        car1 = manager.findCarById(car1Id);
        car1.setTypeName("x");
        manager.updateCar(car1);
        assertEquals("Skodovka", car1.getManufacturerName());
        assertEquals("x", car1.getTypeName());
        assertEquals(CarType.HATCHBACK, car1.getCarType());
        assertEquals(FuelType.PETROL, car1.getFuelType());

        car1 = manager.findCarById(car1Id);
        car1.setFuelType(FuelType.CNG);
        manager.updateCar(car1);
        assertEquals("Skodovka", car1.getManufacturerName());
        assertEquals("x", car1.getTypeName());
        assertEquals(CarType.HATCHBACK, car1.getCarType());
        assertEquals(FuelType.CNG, car1.getFuelType());

        car1 = manager.findCarById(car1Id);
        car1.setCarType(CarType.CONVERTIBLE);
        manager.updateCar(car1);
        assertEquals("Skodovka", car1.getManufacturerName());
        assertEquals("x", car1.getTypeName());
        assertEquals(CarType.CONVERTIBLE, car1.getCarType());
        assertEquals(FuelType.CNG, car1.getFuelType());

        // Check if updates didn't affected other records
        assertDeepEquals(car2, manager.findCarById(car2.getId()));
    }

    @Test
    public void updateCarWithNullArgument() {

        expException.expect(IllegalArgumentException.class);
        manager.updateCar(null);
    }

    @Test
    public void updateCarWithNullId() {

        Car car = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);
        manager.createCar(car);
        Long carId = car.getId();

        car = manager.findCarById(carId);
        car.setId(null);

        expException.expect(IllegalArgumentException.class);
        manager.updateCar(car);
    }

    @Test
    public void updateCarWithWrongModifiedId() {

        Car car = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);
        manager.createCar(car);
        Long carId = car.getId();

        car = manager.findCarById(carId);
        car.setId(carId - 1);

        expException.expect(ServiceFailureException.class);
        manager.updateCar(car);
    }

    @Test
    public void updateCarWithNullManufacturerName() {

        Car car = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);
        manager.createCar(car);
        Long carId = car.getId();

        car = manager.findCarById(carId);
        car.setManufacturerName(null);

        expException.expect(IllegalArgumentException.class);
        manager.updateCar(car);
    }

    @Test
    public void updateCarWithNullCarType() {

        Car car = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);
        manager.createCar(car);
        Long carId = car.getId();

        car = manager.findCarById(carId);
        car.setCarType(null);

        expException.expect(IllegalArgumentException.class);
        manager.updateCar(car);
    }

    @Test
    public void updateCarWithNullFuelType() {

        Car car = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);
        manager.createCar(car);
        Long carId = car.getId();

        car = manager.findCarById(carId);
        car.setFuelType(null);

        expException.expect(IllegalArgumentException.class);
        manager.updateCar(car);
    }

    @Test
    public void deleteCar() {

        Car car1 = newCar("Skoda", "Octavia", CarType.SEDAN, FuelType.LPG);
        Car car2 = newCar("Skoda", "Superb", CarType.ESTATE, FuelType.CNG);
        manager.createCar(car1);
        manager.createCar(car2);

        assertNotNull(manager.findCarById(car1.getId()));
        assertNotNull(manager.findCarById(car2.getId()));

        manager.deleteCar(car1);

        assertNull(manager.findCarById(car1.getId()));
        assertNotNull(manager.findCarById(car2.getId()));

    }

    @Test
    public void deleteNullCar() {
        expException.expect(IllegalArgumentException.class);
        manager.deleteCar(null);
    }

    @Test
    public void deleteWrongModifiedIdCar() {

        Car car = newCar("Skoda", "Superb", CarType.ESTATE, FuelType.CNG);
        manager.createCar(car);
        car.setId(car.getId() - 1);

        expException.expect(ServiceFailureException.class);
        manager.deleteCar(car);
    }

    @Test
    public void deleteNullIdCar() {
        
        Car car = newCar("Skoda", "Superb", CarType.ESTATE, FuelType.CNG);
        manager.createCar(car);
        car.setId(null);

        expException.expect(IllegalArgumentException.class);
        manager.deleteCar(car);
    }

    protected static Car newCar(String manufacturer, String typeName, CarType carType, FuelType fuelType) {
        Car car = new Car();
        car.setManufacturerName(manufacturer);
        car.setTypeName(typeName);
        car.setCarType(carType);
        car.setFuelType(fuelType);

        return car;
    }

    protected static void assertCollectionDeepEquals(List<Car> expectedList, List<Car> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Car expected = expectedList.get(i);
            Car actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    protected static void assertDeepEquals(Car expected, Car actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getManufacturerName(), actual.getManufacturerName());
        assertEquals(expected.getTypeName(), actual.getTypeName());
        assertEquals(expected.getCarType(), actual.getCarType());
        assertEquals(expected.getFuelType(), actual.getFuelType());
    }
    
    protected static Comparator<Car> idComparator = new Comparator<Car>() {
        @Override
        public int compare(Car o1, Car o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

}
