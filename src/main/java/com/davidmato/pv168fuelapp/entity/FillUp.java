package com.davidmato.pv168fuelapp.entity;

import java.sql.Date;
import java.util.Objects;

public class FillUp {

    private Long id;
    private Date date;
    private Car filledCar;
    private double litresFilled;
    private double distanceFromLastFillUp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Car getFilledCar() {
        return filledCar;
    }

    public void setFilledCar(Car filledCar) {
        this.filledCar = filledCar;
    }

    public double getLitresFilled() {
        return litresFilled;
    }

    public void setLitresFilled(double litresFilled) {
        this.litresFilled = litresFilled;
    }

    public double getDistanceFromLastFillUp() {
        return distanceFromLastFillUp;
    }

    public void setDistanceFromLastFillUp(double distanceFromLastFillUp) {
        this.distanceFromLastFillUp = distanceFromLastFillUp;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FillUp other = (FillUp) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("FillUp id = %d ; date = %s, litres = %.2f, distance = %.2f; of carID = %d", id, date, litresFilled, distanceFromLastFillUp, filledCar.getId());
    }

}
