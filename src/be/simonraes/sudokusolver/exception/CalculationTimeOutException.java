package be.simonraes.sudokusolver.exception;

/**
 * Created by Simon Raes on 27/07/2014.
 */
public class CalculationTimeOutException extends Exception {
    public CalculationTimeOutException(){
        super("Couldn't find a solution in time.");
    }
}
