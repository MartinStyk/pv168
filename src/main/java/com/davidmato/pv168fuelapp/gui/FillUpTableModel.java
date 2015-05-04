/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.davidmato.pv168fuelapp.gui;

import com.davidmato.pv168fuelapp.FillUpManagerImpl;
import com.davidmato.pv168fuelapp.entity.Car;
import com.davidmato.pv168fuelapp.entity.FillUp;
import common.DBHelper;
import java.sql.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author david
 */
class FillUpTableModel extends AbstractTableModel {

    private final FillUpManagerImpl fillupManager = new FillUpManagerImpl();
    Locale defaultLocale = Locale.getDefault();
    ResourceBundle text = ResourceBundle.getBundle("Text",defaultLocale);
    
    public FillUpTableModel() {
        fillupManager.setDataSource(DBHelper.getDataSource());
    }

    @Override
    public int getRowCount() {
        return fillupManager.findAllFillUps().size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<FillUp> fillups = fillupManager.findAllFillUps();
        FillUp fillup = fillups.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return fillup.getDate();
            case 1:
                return fillup.getFilledCar();
            case 2:
                return fillup.getLitresFilled();
            case 3:
                return fillup.getDistanceFromLastFillUp();
            case 4:
                return fillup.getId();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return   text.getString("date");
            case 1:
                return text.getString("filled_car");
            case 2:
                return text.getString("litres_filled");
            case 3:
                return text.getString("distance_from_last_fillup");
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
                return Date.class;
            case 1:
                return Car.class;
            case 2:
                return Double.class;
            case 3:
                return Double.class;
            case 4:
                return Long.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        List<FillUp> fillups = fillupManager.findAllFillUps();
        FillUp fillup = fillups.get(rowIndex);

        switch (columnIndex) {
            case 0:
                fillup.setDate((Date) aValue);
                break;
            case 1:
                fillup.setFilledCar((Car) aValue);
                break;
            case 2:
                fillup.setLitresFilled((Double) aValue);
                break;
            case 3:
                fillup.setDistanceFromLastFillUp((Double) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        fillupManager.updateFillUp(fillup);
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

    public void addFillUp(FillUp fillUp) {
        if (fillUp == null) {
            throw new IllegalArgumentException("fillUp");
        }
        fillupManager.createFillUp(fillUp);

        //fireTableRowsInserted(0, cars.size());
        int lastRow = fillupManager.findAllFillUps().size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }

    public void removeRow(int rowIndex) {
        List<FillUp> fillups = fillupManager.findAllFillUps();
        FillUp fillup = fillups.get(rowIndex);
        
        fillupManager.deleteFillUp(fillup);
        int lastRow = fillupManager.findAllFillUps().size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }
}
