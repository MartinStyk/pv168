/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.davidmato.pv168fuelapp.gui;

import com.davidmato.pv168fuelapp.CarManagerImpl;
import com.davidmato.pv168fuelapp.entity.Car;
import com.davidmato.pv168fuelapp.entity.CarType;
import com.davidmato.pv168fuelapp.entity.FuelType;
import common.DBHelper;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author david
 */
class CarTableModel extends AbstractTableModel {

    private final CarManagerImpl carManager = new CarManagerImpl();
    Locale defaultLocale = Locale.getDefault();
    ResourceBundle text = ResourceBundle.getBundle("Text", defaultLocale);

    public CarTableModel() {
        carManager.setDataSource(DBHelper.getDataSource());
    }

    @Override
    public int getRowCount() {
        return carManager.findAllCars().size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<Car> cars = carManager.findAllCars();
        Car car = cars.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return car.getManufacturerName();
            case 1:
                return car.getTypeName();
            case 2:
                return car.getCarType();
            case 3:
                return car.getFuelType();
            case 4:
                return car.getId();
            default:
                throw new IllegalArgumentException("columnIndex");
        }

    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return text.getString("manufacturer_name");
            case 1:
                return text.getString("type_name");
            case 2:
                return text.getString("car_type");
            case 3:
                return text.getString("fuel_type");
            case 4:
                return text.getString("id_check");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                return String.class;
            case 2:
                return CarType.class;
            case 3:
                return FuelType.class;
            case 4:
                return Long.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        List<Car> cars = carManager.findAllCars();
        Car car = cars.get(rowIndex);

        switch (columnIndex) {
            case 0:
                car.setManufacturerName((String) aValue);
                break;
            case 1:
                car.setTypeName((String) aValue);
                break;
            case 2:
                car.setCarType((CarType) aValue);
                break;
            case 3:
                car.setFuelType((FuelType) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        carManager.updateCar(car);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
            case 3:
                return true;
            case 4:
                return false;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void addCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("car");
        }
        carManager.createCar(car);

        int lastRow = carManager.findAllCars().size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }

    public void removeCar(Car car) {
        carManager.deleteCar(car);
        int lastRow = carManager.findAllCars().size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }

}
