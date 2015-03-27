package com.davidmato.pv168fuelapp.entity;

public class Car {

	private Long id;
	private String manufacturerName;
	private String typeName;
	private CarType carType;
	private FuelType fuelType;
	
	@Override
	public boolean equals(Object mObject){
		
		if( ! (mObject instanceof Car ) ) return false;
		Car mCar = (Car) mObject;
		return id.equals(mCar.id);
	}
	@Override
	public int hashCode(){
		return id.hashCode();
	}
    	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getManufacturerName() {
		return manufacturerName;
	}
	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public CarType getCarType() {
		return carType;
	}
	public void setCarType(CarType carType) {
		this.carType = carType;
	}
	public FuelType getFuelType() {
		return fuelType;
	}
	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}
        @Override
        public String toString(){
            return String.format("Car id= %d ; manufacturer = %s , cartype = %s", id, manufacturerName,typeName);
        }
	
	
}
