package be.simonraes.sudokusolver.exception;

/**
 * Created by Simon Raes on 26/07/2014.
 */
public class SolutionFoundException extends Exception {
    public SolutionFoundException(){
        super("Solution found.");
    }
}

