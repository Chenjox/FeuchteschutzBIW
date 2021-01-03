package Util;

public abstract class functions {
    public static double RoundToDecimalPlace(double zahl, int nachkommastellen){
        double potenz=Math.pow(10.0,nachkommastellen);
        return ((double) Math.round( zahl*potenz ) )/potenz;
    }
    public static double RoundToLeadingDigits(double zahl, int nachkommastellen){
        return 0.0;
    }
}
