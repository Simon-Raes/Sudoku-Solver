package be.simonraes.sudokusolver.util;

import be.simonraes.sudokusolver.model.GridValue;

/**
 * Help class to output the content of a two-dimensional array.
 * Created by Simon Raes on 14/09/2014.
 */
public class Printer {

    public static void printArray(int[][] values){
        if(values!=null) {
            for (int i = 0; i < values.length; i++) {
                for (int j = 0; j < values[i].length; j++) {
                    System.out.print(values[i][j] + " ");
                }
                System.out.println("");
            }
        } else {
            System.out.println("Array is null");
        }
    }

    public static void printArray(GridValue[][] values){
        if(values!=null) {
            for (int i = 0; i < values.length; i++) {
                for (int j = 0; j < values[i].length; j++) {
                    if(values[i][j]!=null){
                        System.out.print(values[i][j].getValue() + " ");
                    } else {
                        System.out.print("n ");
                    }
                }
                System.out.println("");
            }
        } else {
            System.out.println("Array is null");
        }
    }
}
