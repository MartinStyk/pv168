/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

/**
 *
 * @author Martin Styk
 */
public class FuelConsumptionException extends Exception {

    public FuelConsumptionException() {
        super();
    }

    public FuelConsumptionException(String msg) {
        super(msg);
    }

    public FuelConsumptionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
