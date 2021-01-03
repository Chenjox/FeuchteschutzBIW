package Util;

public abstract class Druck {

    /**
     * Rechnet den Sättigungsdampfdruck aus der Temperatur aus
     * @param phi_temperatur Temperatur in °C (!Wichtig)
     * @return Sättigungsdampfdruck in Pa
     */
    public static double sattigungsdampfdruck_alt(double phi_temperatur){
        return phi_temperatur < 0 ? 610.5*Math.pow(1.0+(phi_temperatur/148.57), 12.3) : 610.5*Math.pow(1.0+(phi_temperatur/109.8), 8.02);
    }

    public static double sattigungsdampfdruck(double phi_temperatur){
        return phi_temperatur < 0 ? 4.689*Math.pow(1.486+(phi_temperatur/100), 12.3) : 288.68*Math.pow(1.098+(phi_temperatur/100), 8.02);
    }

    public static double sattigungsdampfdruck(double theta_temperatur, int nachkommastellen){
        return Util.functions.RoundToDecimalPlace( sattigungsdampfdruck( theta_temperatur ), nachkommastellen );
    }
    /**
     * Rechnet den Partialdampfdruck f&uuml;r eine spezifische Temperatur und spezifische relative Luftfeuchte aus.
     * @param phi_temperatur Temperatur in &deg;C
     * @param rel_luftfeuchte relative Luftfeuchte in % (0 \< x \< 1)
     * @return Partialdampfdruck in Pa
     */
    public static double partialdampfdruck(double phi_temperatur, double rel_luftfeuchte){
        return rel_luftfeuchte*sattigungsdampfdruck( phi_temperatur );
    }

    public static double partialdampfdruck(double phi_temperatur, double rel_luftfeuchte, int nachkommastellen){
        return Util.functions.RoundToDecimalPlace(  rel_luftfeuchte*sattigungsdampfdruck( phi_temperatur , nachkommastellen), nachkommastellen);
    }
    /**
     * In kg*m^-1*s^-1*Pa^-1
     */
    public static double DIFFUSIONSLEITKOEFFIZIENT_IN_LUFT = 1.85E-10;
    public static double KONDENSATIONSPERIODE_WINTER = 7.776E6;
    public static double VERDUNSTUNGSPERIODE_SOMMER = 7.776E6;
    public static double WASSERDAMPFPARTIALDRUCK_SOMMER = 1200.0;
    public static double WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER = 1700.0;
    public static double WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH = 2000.0;
}
