package com.davidmato.pv168fuelapp;

import com.davidmato.pv168fuelapp.entity.CarType;
import com.davidmato.pv168fuelapp.entity.FuelType;
import com.davidmato.pv168fuelapp.entity.Car;
import com.davidmato.pv168fuelapp.entity.FillUp;
import common.DBHelper;
import common.ServiceFailureException;
import java.net.URL;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.sql.Date;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FillUpManagerImplTest {

    public static final double EPSILON = 0.001;
    private FillUpManagerImpl manager;
    private Car mCar;

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {

        URL urlToScript = CarManagerImpl.class.getResource("/createTables.sql");
        DBHelper.executeSqlScript(DBHelper.getDataSource(), urlToScript);
        mCar = CarManagerImplTest.newCar("Skoda", "Octavia", CarType.ESTATE, FuelType.PETROL);
        manager = new FillUpManagerImpl();
        manager.setDataSource(DBHelper.getDataSource());

        CarManagerImpl carManager = new CarManagerImpl();
        carManager.setDataSource(DBHelper.getDataSource());
        carManager.createCar(mCar);
    }

    @After
    public void tearDown() throws SQLException {
        URL urlToScript = CarManagerImpl.class.getResource("/dropTables.sql");
        DBHelper.executeSqlScript(DBHelper.getDataSource(), urlToScript);
    }

    @Test
    public void createFillUp() {

        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);

        manager.createFillUp(fillUp);

        Long fillUpId = fillUp.getId();

        assertNotNull(fillUpId);
        FillUp resultFillUp = manager.findFillUpById(fillUpId);
        assertEquals(fillUp, resultFillUp);
        assertNotSame(fillUp, resultFillUp);
        assertDeepEquals(fillUp, resultFillUp);
    }

    @Test
    public void findFillUpById() {

        assertNull(manager.findFillUpById(1098l));

        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);

        manager.createFillUp(fillUp);
        Long fillUpId = fillUp.getId();

        FillUp resultFillUp = manager.findFillUpById(fillUpId);
        assertEquals(fillUp, resultFillUp);
        assertDeepEquals(fillUp, resultFillUp);
    }

    @Test
    public void getAllFillUps() {

        assertTrue(manager.findAllFillUps().isEmpty());

        FillUp fillUp1 = newFillUp(new Date(100), 100, 1000, mCar);
        FillUp fillUp2 = newFillUp(new Date(500), 10, 9.5, mCar);
        FillUp fillUp3 = newFillUp(new Date(15520), 14, 9.5, mCar);

        manager.createFillUp(fillUp1);
        manager.createFillUp(fillUp2);
        manager.createFillUp(fillUp3);

        List<FillUp> expected = Arrays.asList(fillUp1, fillUp2, fillUp3);
        List<FillUp> actual = manager.findAllFillUps();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertCollectionDeepEquals(expected, actual);
    }

    public void createNullFillUp() {
        expException.expect(IllegalArgumentException.class);
        manager.createFillUp(null);
    }

    public void createFillUpWithWrongId() {
        
        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        fillUp.setId(1l);
        expException.expect(IllegalArgumentException.class);
        manager.createFillUp(fillUp);
    }

    @Test
    public void updateFillUp() {
        
        FillUp fillUp1 = newFillUp(new Date(100), 100, 1000, mCar);
        FillUp fillUp2 = newFillUp(new Date(900), 1009, 91000, mCar);
        manager.createFillUp(fillUp1);
        manager.createFillUp(fillUp2);

        Long fillUp1Id = fillUp1.getId();

        Date mDate = new Date(5000);

        fillUp1 = manager.findFillUpById(fillUp1Id);
        fillUp1.setDate(mDate);
        manager.updateFillUp(fillUp1);
        assertEquals(mDate, fillUp1.getDate());
        assertEquals(100, fillUp1.getLitresFilled(), EPSILON);
        assertEquals(1000, fillUp1.getDistanceFromLastFillUp(), EPSILON);

        fillUp1 = manager.findFillUpById(fillUp1Id);
        fillUp1.setDistanceFromLastFillUp(1);
        manager.updateFillUp(fillUp1);
        assertEquals(mDate.toString(), fillUp1.getDate().toString());
        assertEquals(100, fillUp1.getLitresFilled(), EPSILON);
        assertEquals(1, fillUp1.getDistanceFromLastFillUp(), EPSILON);

        fillUp1 = manager.findFillUpById(fillUp1Id);
        fillUp1.setLitresFilled(2);
        manager.updateFillUp(fillUp1);
        assertEquals(mDate.toString(), fillUp1.getDate().toString());
        assertEquals(2, fillUp1.getLitresFilled(), EPSILON);
        assertEquals(1, fillUp1.getDistanceFromLastFillUp(), EPSILON);

    }

    @Test
    public void updateFillUpWithNullArgument() {
        expException.expect(IllegalArgumentException.class);
        manager.updateFillUp(null);
    }

    @Test
    public void updateFillUpWithNullId() {

        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        manager.createFillUp(fillUp);
        Long fillUpId = fillUp.getId();

        fillUp = manager.findFillUpById(fillUpId);
        fillUp.setId(null);
        expException.expect(IllegalArgumentException.class);
        manager.updateFillUp(fillUp);
    }

    @Test
    public void updateFillUpWithWrongModifiedId() {

        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        manager.createFillUp(fillUp);
        Long fillUpId = fillUp.getId();
        fillUp = manager.findFillUpById(fillUpId);
        fillUp.setId(fillUpId - 1);
        expException.expect(ServiceFailureException.class);
        manager.updateFillUp(fillUp);
    }

    @Test
    public void updateFillUpWithWrongDistance() {

        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        manager.createFillUp(fillUp);
        Long fillUpId = fillUp.getId();
        fillUp = manager.findFillUpById(fillUpId);
        fillUp.setDistanceFromLastFillUp(-1);
        expException.expect(IllegalArgumentException.class);
        manager.updateFillUp(fillUp);
    }

    @Test
    public void updateFillUpWithWrongLitres() {

        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        manager.createFillUp(fillUp);
        Long fillUpId = fillUp.getId();
        fillUp = manager.findFillUpById(fillUpId);
        fillUp.setLitresFilled(-1);
        expException.expect(IllegalArgumentException.class);
        manager.updateFillUp(fillUp);
    }

    @Test
    public void deleteFillUp() {

        FillUp fillUp1 = newFillUp(new Date(100), 100, 1000, mCar);
        FillUp fillUp2 = newFillUp(new Date(550), 100, 1000, mCar);
        manager.createFillUp(fillUp1);
        manager.createFillUp(fillUp2);

        assertNotNull(manager.findFillUpById(fillUp1.getId()));
        assertNotNull(manager.findFillUpById(fillUp2.getId()));

        manager.deleteFillUp(fillUp1);

        assertNull(manager.findFillUpById(fillUp1.getId()));
        assertNotNull(manager.findFillUpById(fillUp2.getId()));

    }

    @Test
    public void deleteNullFillUp() {

        expException.expect(IllegalArgumentException.class);
        manager.deleteFillUp(null);
    }

    @Test
    public void deleteWrongModifiedIdFillUp() {
        
        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        manager.createFillUp(fillUp);
        fillUp.setId(fillUp.getId() - 1);

        expException.expect(ServiceFailureException.class);
        manager.deleteFillUp(fillUp);
    }

    @Test
    public void deleteNullIdFillUp() {
        
        FillUp fillUp = newFillUp(new Date(100), 100, 1000, mCar);
        manager.createFillUp(fillUp);
        fillUp.setId(null);

        expException.expect(IllegalArgumentException.class);
        manager.deleteFillUp(fillUp);
    }

    protected static FillUp newFillUp(Date date, double litresFilled, double distanceFromLastFillUp, Car car) {
        FillUp fillUp = new FillUp();
        fillUp.setFilledCar(car);
        fillUp.setDate(date);
        fillUp.setLitresFilled(litresFilled);
        fillUp.setDistanceFromLastFillUp(distanceFromLastFillUp);

        return fillUp;
    }

    private void assertCollectionDeepEquals(List<FillUp> expectedList, List<FillUp> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            FillUp expected = expectedList.get(i);
            FillUp actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(FillUp expected, FillUp actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDate().toString(), actual.getDate().toString());
        assertEquals(expected.getDistanceFromLastFillUp(), actual.getDistanceFromLastFillUp(), EPSILON);
        assertEquals(expected.getFilledCar(), actual.getFilledCar());
        assertEquals(expected.getLitresFilled(), actual.getLitresFilled(), EPSILON);
    }
    
    protected static Comparator<FillUp> idComparator = new Comparator<FillUp>() {
        @Override
        public int compare(FillUp o1, FillUp o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

}
