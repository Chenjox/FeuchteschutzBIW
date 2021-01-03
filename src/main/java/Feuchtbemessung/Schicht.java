package Feuchtbemessung;

import Util.Druck;

public class Schicht {
    //lambda
    private final double Waermeleitfaehigkeit;
    //Dicke d
    private final double Dicke;
    //Wasserdampfdiffusionswiderstand mü
    private final double Diffusionswiderstand;
    private final String name;

    public Schicht(String pname, double pDicke,double pWaermeleitfaehigkeit, double pDiffusionswiderstand){
        this.name = pname;
        this.Dicke = pDicke;
        this.Waermeleitfaehigkeit = pWaermeleitfaehigkeit;
        this.Diffusionswiderstand = pDiffusionswiderstand;
    }

    public double getWaermeleitfaehigkeit() {
        return Waermeleitfaehigkeit;
    }

    public double getDicke() {
        return Dicke;
    }

    public double getDiffusionswiderstand() {
        return Diffusionswiderstand;
    }

    public String getName() {
        return name;
    }

    public double getWaermedurchgangswiderstand(){
        return Dicke/Waermeleitfaehigkeit;
    }

    public double getWaermedurchgangswiderstand(int nachkommastellen){
        return Util.functions.RoundToDecimalPlace( getWaermedurchgangswiderstand(), nachkommastellen );
    }

    public double getDiffusionsleitwiderstand(){
        return (Diffusionswiderstand*Dicke)/Druck.DIFFUSIONSLEITKOEFFIZIENT_IN_LUFT;
    }
    public double getDiffusionsleitwiderstand(int nachkommastellen){
        return Util.functions.RoundToDecimalPlace( (Diffusionswiderstand*Dicke), nachkommastellen )/Druck.DIFFUSIONSLEITKOEFFIZIENT_IN_LUFT;
    }
    /**
     *
     * @param phi_schichtanfang In °C
     * @param Transmissionswaermestromdichte In W/m^2
     * @return phi_schichtende
     */
    public double Temperaturabfall(double phi_schichtanfang, double Transmissionswaermestromdichte){
        return phi_schichtanfang-(Transmissionswaermestromdichte*getWaermedurchgangswiderstand());
    }
    public double Partialdampfdruckabfall(double p_schichtanfang, double Wasserdampfdiffusionsstromdichte){
        return p_schichtanfang-(Wasserdampfdiffusionsstromdichte* getDiffusionsleitwiderstand());
    }

}
