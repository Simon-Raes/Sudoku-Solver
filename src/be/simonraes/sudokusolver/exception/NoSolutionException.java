package be.simonraes.sudokusolver.exception;

/**
 * Created by Simon Raes on 26/07/2014.
 */
public class NoSolutionException extends Exception{
    public NoSolutionException(){
        super("No solution possible for this Sudoku.");
    }
}

