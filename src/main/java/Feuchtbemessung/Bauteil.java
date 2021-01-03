package Feuchtbemessung;

import Util.Arraycheck.Check;
import Util.Druck;
import Util.Table.StringTable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Bauteil {
    private final ArrayList<Schicht> Schichten;

    public Bauteil(ArrayList<Schicht> pSchichten){
        this.Schichten = pSchichten;
    }
    public Bauteil(){
        this.Schichten = new ArrayList<>();
    }
    public void addSchicht(Schicht pSchicht){
        this.Schichten.add( pSchicht );
    }
    public double getWaemedurchgangswiderstand(double R_interior_surface, double R_exterior_surface){
        double R_Bauteil = R_interior_surface+R_exterior_surface;
        for (Schicht s:Schichten) {
            R_Bauteil += s.getWaermedurchgangswiderstand();
        }
        return R_Bauteil;
    }
    public double getWaemedurchgangswiderstand(double R_interior_surface, double R_exterior_surface, int nachkommastellen){
        double R_Bauteil = R_interior_surface+R_exterior_surface;
        for (Schicht s:Schichten) {
            R_Bauteil += s.getWaermedurchgangswiderstand(nachkommastellen);
        }
        return R_Bauteil;
    }
    public double getDiffusionsleitwiderstand(){
        double Z_Bauteil = 0.0;
        for (Schicht s:Schichten) {
            Z_Bauteil += s.getDiffusionsleitwiderstand();
        }
        return Z_Bauteil;
    }
    public double getDiffusionsleitwiderstand(int schicht1, int schicht2){
        double Z_teil = 0.0;
        if(schicht1==schicht2){
            return Schichten.get( schicht1 ).getDiffusionsleitwiderstand();
        }else for (int i = schicht1; i < schicht2; i++) {
            Z_teil += Schichten.get( i ).getDiffusionsleitwiderstand();
        }
        return Z_teil;
    }
    private double Temperaturabfall(double phi_momentan, double Transmissionswaermestromdichte){
        for (Schicht schicht : Schichten) {
            phi_momentan = schicht.Temperaturabfall( phi_momentan, Transmissionswaermestromdichte );
        }
        return phi_momentan;
    }
    private String getKondensatsmengen(List<Integer> kebenenindezes, List<Double> kebenensatdrucke, double pv_interior, double pv_exterior, boolean istdach){
        StringTable t = new StringTable()
                .addSpalte( "" )
                .addSpalte( "Fl\u00E4chenbezogene Wassermasse in [kg/m^2]" )
                .addSpalte( "Diffusion Innen in [kg/(m^2*s)]" )
                .addSpalte( "Diffusion au\u00DFen in [kg/(m^2*s)]" );
        StringTable ebenen = new StringTable().addSpalte( "Bauteilebenen" ).addSpalte( "Z in [s]" );
        //Fall 1 Eine Kondensationsebene
        if(kebenenindezes.size()==1){
            t.addSpalte( "Partialdruck innen in [Pa]" )
                    .addSpalte( "S\u00E4ttigungsdruck K-Ebene in [Pa]" )
                    .addSpalte( "S\u00E4ttigungsdruck aussen in [Pa]" );

            int k_index = kebenenindezes.get( 0 );
            double k_satdruck = kebenensatdrucke.get(0);
            double Z_innennachk = getDiffusionsleitwiderstand(0,k_index);
            double Z_knachaussen = getDiffusionsleitwiderstand(k_index+1,Schichten.size()-1);
            ebenen.addZeile( k_index==0 ? "0" :"0-"+k_index,String.valueOf( Z_innennachk ) );
            ebenen.addZeile( ((k_index+1)==(Schichten.size()-1)) ? String.valueOf(k_index+1) : (k_index+1)+"-"+(Schichten.size()-1), String.valueOf( Z_knachaussen) );

            double einstroemende_Diffusion = (pv_interior-k_satdruck)/Z_innennachk;
            double ausstroemende_Diffusion = (k_satdruck-pv_exterior)/Z_knachaussen;
            double Kondensatmenge = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;

            t.addZeile( "Kondensatmenge",
                        String.valueOf( Kondensatmenge ),
                        String.valueOf( einstroemende_Diffusion ),
                        String.valueOf( ausstroemende_Diffusion ),
                        String.valueOf( pv_interior ),
                        String.valueOf( k_satdruck ),
                        String.valueOf( pv_exterior ));
            if(istdach){
                double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk);
                double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_knachaussen);

                double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                t.addZeile( "Verdunstungsmasse",
                            String.valueOf( Verdunstungsmasse ),
                            String.valueOf( ausstroemende_Diffusion_innen ),
                            String.valueOf( ausstroemende_Diffusion_aussen ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                            String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ) );
            }else {
                double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk);
                double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_knachaussen);

                double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                t.addZeile( "Verdunstungsmasse",
                            String.valueOf( Verdunstungsmasse ),
                            String.valueOf( ausstroemende_Diffusion_innen ),
                            String.valueOf( ausstroemende_Diffusion_aussen ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                            String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ) );
            }
        }else {
            //Fall 3 Ein kondensationsbereich
            if(Check.checkListContainsConsecutiveElements( kebenenindezes )){
                t.addSpalte( "Partialdruck innen in [Pa]" )
                        .addSpalte( "Saettigungsdruck innen in [Pa]" )
                        .addSpalte( "Partialdruck au\u00DFen in [Pa]" )
                        .addSpalte( "Saettigungsdruck aussen in [Pa]" );

                int index_i = kebenenindezes.get( 0 );
                int index_e = kebenenindezes.get( kebenenindezes.size()-1 );
                double Z_InnennachBereich = getDiffusionsleitwiderstand(0, index_i+1);
                double Z_Bereich = getDiffusionsleitwiderstand(index_i+1, index_e);
                double Z_BereichnachAussen = getDiffusionsleitwiderstand(index_e+1, Schichten.size()-1 );
                ebenen.addZeile( index_i==0 ? "0" :"0-"+index_i,
                                 String.valueOf( Z_InnennachBereich ) );
                ebenen.addZeile( ((index_i + 1) == index_e) ? String.valueOf( index_i + 1 ) : ((index_i + 1) + "-" + index_e),
                                 String.valueOf( Z_Bereich ) );
                ebenen.addZeile( ((index_e + 1) == (Schichten.size() - 1)) ? String.valueOf( index_e + 1 ) : ((index_e + 1) + "-" + (Schichten.size() - 1)),
                                 String.valueOf(Z_BereichnachAussen) );

                double einstroemende_Diffusion = (pv_interior-kebenensatdrucke.get( 0 ))/Z_InnennachBereich;
                double ausstroemende_Diffusion = (kebenensatdrucke.get( kebenensatdrucke.size()-1 )-pv_exterior)/Z_BereichnachAussen;

                double Kondensatmasse = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;
                t.addZeile( "Kondensatmasse",
                            String.valueOf( Kondensatmasse ),
                            String.valueOf( einstroemende_Diffusion ),
                            String.valueOf( ausstroemende_Diffusion ),
                            String.valueOf( pv_interior ),
                            String.valueOf( kebenensatdrucke.get( 0 ) ),
                            String.valueOf( pv_exterior ),
                            String.valueOf( kebenensatdrucke.get( kebenensatdrucke.size()-1 ) ));
                if(istdach){
                    double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_InnennachBereich+Z_Bereich/2);
                    double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_BereichnachAussen+Z_Bereich/2);

                    double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                    t.addZeile( "Verdunstungsmasse",
                                String.valueOf( Verdunstungsmasse ),
                                String.valueOf( ausstroemende_Diffusion_innen ),
                                String.valueOf( ausstroemende_Diffusion_aussen ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH ) );
                }else{
                    double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_InnennachBereich+Z_Bereich/2);
                    double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_BereichnachAussen+Z_Bereich/2);

                    double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                    t.addZeile( "Verdunstungsmasse",
                                String.valueOf( Verdunstungsmasse ),
                                String.valueOf( ausstroemende_Diffusion_innen ),
                                String.valueOf( ausstroemende_Diffusion_aussen ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER ) );
                }
            }else{
                //TODO
            }
        }
        return ebenen.TableToString()+"\n"+t.TableToString();
    }
    private String getKondensatsmengenMarkdown(List<Integer> kebenenindezes, List<Double> kebenensatdrucke, double pv_interior, double pv_exterior, boolean istdach){
        StringTable t = new StringTable()
                .addSpalte( "" )
                .addSpalte( "Fl\u00E4chenbezogene Wassermasse in [kg/m^2]" )
                .addSpalte( "Diffusion Innen in [kg/(m^2*s)]" )
                .addSpalte( "Diffusion au\u00DFen in [kg/(m^2*s)]" );
        StringTable ebenen = new StringTable().addSpalte( "Bauteilebenen" ).addSpalte( "Z in [s]" );
        //Fall 1 Eine Kondensationsebene
        if(kebenenindezes.size()==1){
            t.addSpalte( "Partialdruck innen in [Pa]" )
                    .addSpalte( "S\u00E4ttigungsdruck K-Ebene in [Pa]" )
                    .addSpalte( "S\u00E4ttigungsdruck aussen in [Pa]" );

            int k_index = kebenenindezes.get( 0 );
            double k_satdruck = kebenensatdrucke.get(0);
            double Z_innennachk = getDiffusionsleitwiderstand(0,k_index);
            double Z_knachaussen = getDiffusionsleitwiderstand(k_index+1,Schichten.size()-1);
            ebenen.addZeile( k_index==0 ? "0" :"0-"+k_index,String.valueOf( Z_innennachk ) );
            ebenen.addZeile( ((k_index+1)==(Schichten.size()-1)) ? String.valueOf(k_index+1) : (k_index+1)+"-"+(Schichten.size()-1), String.valueOf( Z_knachaussen) );

            double einstroemende_Diffusion = (pv_interior-k_satdruck)/Z_innennachk;
            double ausstroemende_Diffusion = (k_satdruck-pv_exterior)/Z_knachaussen;
            double Kondensatmenge = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;

            t.addZeile( "Kondensatmenge",
                        String.valueOf( Kondensatmenge ),
                        String.valueOf( einstroemende_Diffusion ),
                        String.valueOf( ausstroemende_Diffusion ),
                        String.valueOf( pv_interior ),
                        String.valueOf( k_satdruck ),
                        String.valueOf( pv_exterior ));
            if(istdach){
                double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk);
                double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_knachaussen);

                double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                t.addZeile( "Verdunstungsmasse",
                            String.valueOf( Verdunstungsmasse ),
                            String.valueOf( ausstroemende_Diffusion_innen ),
                            String.valueOf( ausstroemende_Diffusion_aussen ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                            String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ) );
            }else {
                double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk);
                double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_knachaussen);

                double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                t.addZeile( "Verdunstungsmasse",
                            String.valueOf( Verdunstungsmasse ),
                            String.valueOf( ausstroemende_Diffusion_innen ),
                            String.valueOf( ausstroemende_Diffusion_aussen ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                            String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER ),
                            String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ) );
            }
        }else {
            //Fall 3 Ein kondensationsbereich
            if(Check.checkListContainsConsecutiveElements( kebenenindezes )){
                t.addSpalte( "Partialdruck innen in [Pa]" )
                        .addSpalte( "Saettigungsdruck innen in [Pa]" )
                        .addSpalte( "Partialdruck au\u00DFen in [Pa]" )
                        .addSpalte( "Saettigungsdruck aussen in [Pa]" );

                int index_i = kebenenindezes.get( 0 );
                int index_e = kebenenindezes.get( kebenenindezes.size()-1 );
                double Z_InnennachBereich = getDiffusionsleitwiderstand(0, index_i+1);
                double Z_Bereich = getDiffusionsleitwiderstand(index_i+1, index_e);
                double Z_BereichnachAussen = getDiffusionsleitwiderstand(index_e+1, Schichten.size()-1 );
                ebenen.addZeile( index_i==0 ? "0" :"0-"+index_i,
                                 String.valueOf( Z_InnennachBereich ) );
                ebenen.addZeile( ((index_i + 1) == index_e) ? String.valueOf( index_i + 1 ) : ((index_i + 1) + "-" + index_e),
                                 String.valueOf( Z_Bereich ) );
                ebenen.addZeile( ((index_e + 1) == (Schichten.size() - 1)) ? String.valueOf( index_e + 1 ) : ((index_e + 1) + "-" + (Schichten.size() - 1)),
                                 String.valueOf(Z_BereichnachAussen) );

                double einstroemende_Diffusion = (pv_interior-kebenensatdrucke.get( 0 ))/Z_InnennachBereich;
                double ausstroemende_Diffusion = (kebenensatdrucke.get( kebenensatdrucke.size()-1 )-pv_exterior)/Z_BereichnachAussen;

                double Kondensatmasse = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;
                t.addZeile( "Kondensatmasse",
                            String.valueOf( Kondensatmasse ),
                            String.valueOf( einstroemende_Diffusion ),
                            String.valueOf( ausstroemende_Diffusion ),
                            String.valueOf( pv_interior ),
                            String.valueOf( kebenensatdrucke.get( 0 ) ),
                            String.valueOf( pv_exterior ),
                            String.valueOf( kebenensatdrucke.get( kebenensatdrucke.size()-1 ) ));
                if(istdach){
                    double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_InnennachBereich+Z_Bereich/2);
                    double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_BereichnachAussen+Z_Bereich/2);

                    double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                    t.addZeile( "Verdunstungsmasse",
                                String.valueOf( Verdunstungsmasse ),
                                String.valueOf( ausstroemende_Diffusion_innen ),
                                String.valueOf( ausstroemende_Diffusion_aussen ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH ) );
                }else{
                    double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_InnennachBereich+Z_Bereich/2);
                    double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_BereichnachAussen+Z_Bereich/2);

                    double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
                    t.addZeile( "Verdunstungsmasse",
                                String.valueOf( Verdunstungsmasse ),
                                String.valueOf( ausstroemende_Diffusion_innen ),
                                String.valueOf( ausstroemende_Diffusion_aussen ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFPARTIALDRUCK_SOMMER ),
                                String.valueOf( Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER ) );
                }
            }else{
                //TODO
            }
        }
        return ebenen.TabletoMarkdown()+"\n"+t.TabletoMarkdown();
    }
    public String getTemperaturprofilasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface){
        StringTable t = new StringTable()
                .addSpalte( "Bauteilname" )
                .addSpalte( "R" )
                .addSpalte( "Phi in \u00B0C" );
        //Transmissionswaermestromdichte
        double R_Bauteil = getWaemedurchgangswiderstand( R_interior_surface, R_exterior_surface );
        double Transmissionswaermestromdichte = (phi_interior-phi_exterior)/R_Bauteil;
        //Von Innen zu Bauteil
        t.addZeile( " "," ",String.valueOf( phi_interior ) );
        t.addZeile( "Innen",String.valueOf( R_interior_surface )," " );
        double phi_momentan = phi_interior-Transmissionswaermestromdichte*R_interior_surface;
        t.addZeile( " "," ",String.valueOf( phi_momentan ));
        //Einmal durch das Bauteil
        for (Schicht schicht : Schichten) {
            phi_momentan = schicht.Temperaturabfall( phi_momentan, Transmissionswaermestromdichte );
            t.addZeile( schicht.getName(),
                        String.valueOf( schicht.getWaermedurchgangswiderstand() ),
                        " " );
            t.addZeile( " ", " ", String.valueOf( phi_momentan ) );
        }
        //Von Bauteil nach außen... jetzt sollte phi_momentan == phi_exterior gelten
        t.addZeile( "Aussen", String.valueOf( R_exterior_surface ), " " );
        phi_momentan = phi_momentan - Transmissionswaermestromdichte*R_exterior_surface;
        t.addZeile( " "," ",String.valueOf( phi_momentan ) );
        return t.TableToString();
    }
    public String getRealDampfdruckprofilasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior){
        return getRealDampfdruckprofilasString( phi_interior, phi_exterior, R_interior_surface, R_exterior_surface, rel_luft_interior, rel_luft_exterior, false );
    }
    public String getRealDampfdruckprofilDachasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior){
        return getRealDampfdruckprofilasString( phi_interior, phi_exterior, R_interior_surface, R_exterior_surface, rel_luft_interior, rel_luft_exterior, true );
    }
    public String getRealDampfdruckprofilasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior, boolean istdach){
        StringTable t = new StringTable()
                .addSpalte( "Bauteilname" )
                .addSpalte( "Dicke in [m]" )
                .addSpalte( "\u03BB in [W/(m*K)]" )
                .addSpalte( "R in [m^2*K/W]" )
                .addSpalte( "mu in [-]" )
                .addSpalte( "Z in [s]" )
                .addSpalte( "\u0398 in [\u00B0C]" )
                .addSpalte( "Sat. Druck in [Pa]" )
                .addSpalte( "Part. Druck in [Pa]" );
        //Transmissionswaermestromdichte
        double R_Bauteil = getWaemedurchgangswiderstand( R_interior_surface, R_exterior_surface );
        double Transmissionswaermestromdichte = (phi_interior-phi_exterior)/R_Bauteil;
        //Dampfdruckdichte
        double Z_Bauteil = getDiffusionsleitwiderstand();
        double pv_interior = Druck.partialdampfdruck( phi_interior, rel_luft_interior );
        double pv_exterior = Druck.partialdampfdruck( phi_exterior, rel_luft_exterior );
        double Dampfdruckdichte = (pv_interior-pv_exterior)/Z_Bauteil;
        //Von Innen zu Bauteil
        t.addZeile(
                " ",
                " ",
                " ",
                " ",
                " ",
                " ",
                String.valueOf( phi_interior ),
                String.valueOf( Druck.sattigungsdampfdruck( phi_interior ) ),
                String.valueOf( pv_interior )
        );
        t.addZeile( "Innen"," "," ",String.valueOf( R_interior_surface )," "," "," "," "," " );

        double phi_momentan = phi_interior-Transmissionswaermestromdichte*R_interior_surface;
        double pv_momentan = pv_interior;
        t.addZeile( " "," "," "," "," "," ", String.valueOf( phi_momentan ), String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ),String.valueOf( pv_momentan ));

        //Einmal durch das Bauteil
        ArrayList<Integer> kebenen = new ArrayList<>();
        ArrayList<Double> kebenendruck = new ArrayList<>();
        for (int i = 0; i < Schichten.size(); i++) {
            Schicht schicht = Schichten.get( i );
            pv_momentan = schicht.Partialdampfdruckabfall( pv_momentan, Dampfdruckdichte );
            phi_momentan = schicht.Temperaturabfall( phi_momentan, Transmissionswaermestromdichte );

            t.addZeile( i +": "+ schicht.getName(),
                        String.valueOf( schicht.getDicke() ),
                        String.valueOf( schicht.getWaermeleitfaehigkeit() ),
                        String.valueOf( schicht.getWaermedurchgangswiderstand() ),
                        String.valueOf( schicht.getDiffusionswiderstand() ),
                        String.valueOf( schicht.getDiffusionsleitwiderstand() ),
                        " ", " ", " " );
            if (pv_momentan > Druck.sattigungsdampfdruck( phi_momentan )) {
                t.addZeile( "  K-Ebene", " ", " ", " "," "," ",
                            String.valueOf( phi_momentan ),
                            String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ),
                            String.valueOf( pv_momentan ) );
                kebenen.add( i );
                kebenendruck.add( Druck.sattigungsdampfdruck( phi_momentan ) );
            } else t.addZeile( " ", " ", " "," "," "," ", String.valueOf( phi_momentan ),
                               String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ),
                               String.valueOf( pv_momentan ) );
        }
        //Von Bauteil nach außen... jetzt sollte phi_momentan =~ phi_exterior gelten
        t.addZeile( "Aussen", " "," ", String.valueOf( R_exterior_surface )," "," "," "," "," " );
        phi_momentan = phi_momentan - Transmissionswaermestromdichte*R_exterior_surface;
        t.addZeile( " "," "," "," "," "," ",String.valueOf( phi_momentan ), String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ), String.valueOf( pv_momentan ) );

        //Anschließend den Rest ausrechnen
        String k = getKondensatsmengen( kebenen, kebenendruck, pv_interior, pv_exterior, istdach );
        return t.TableToString()+"\n"+k;
    }
    public String getRealDampfdruckprofilasMarkdown(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior, boolean istdach){
        StringTable t = new StringTable()
                .addSpalte( "Bauteilname" )
                .addSpalte( "Dicke in [m]" )
                .addSpalte( "\u03BB in [W/(m*K)]" )
                .addSpalte( "R in [m^2*K/W]" )
                .addSpalte( "mu in [-]" )
                .addSpalte( "Z in [s]" )
                .addSpalte( "\u0398 in [\u00B0C]" )
                .addSpalte( "Sat. Druck in [Pa]" )
                .addSpalte( "Part. Druck in [Pa]" );
        //Transmissionswaermestromdichte
        double R_Bauteil = getWaemedurchgangswiderstand( R_interior_surface, R_exterior_surface );
        double Transmissionswaermestromdichte = (phi_interior-phi_exterior)/R_Bauteil;
        //Dampfdruckdichte
        double Z_Bauteil = getDiffusionsleitwiderstand();
        double pv_interior = Druck.partialdampfdruck( phi_interior, rel_luft_interior );
        double pv_exterior = Druck.partialdampfdruck( phi_exterior, rel_luft_exterior );
        double Dampfdruckdichte = (pv_interior-pv_exterior)/Z_Bauteil;
        //Von Innen zu Bauteil
        t.addZeile(
                " ",
                " ",
                " ",
                " ",
                " ",
                " ",
                String.valueOf( phi_interior ),
                String.valueOf( Druck.sattigungsdampfdruck( phi_interior ) ),
                String.valueOf( pv_interior )
        );
        t.addZeile( "Innen"," "," ",String.valueOf( R_interior_surface )," "," "," "," "," " );

        double phi_momentan = phi_interior-Transmissionswaermestromdichte*R_interior_surface;
        double pv_momentan = pv_interior;
        t.addZeile( " "," "," "," "," "," ", String.valueOf( phi_momentan ), String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ),String.valueOf( pv_momentan ));

        //Einmal durch das Bauteil
        ArrayList<Integer> kebenen = new ArrayList<>();
        ArrayList<Double> kebenendruck = new ArrayList<>();
        for (int i = 0; i < Schichten.size(); i++) {
            Schicht schicht = Schichten.get( i );
            pv_momentan = schicht.Partialdampfdruckabfall( pv_momentan, Dampfdruckdichte );
            phi_momentan = schicht.Temperaturabfall( phi_momentan, Transmissionswaermestromdichte );

            t.addZeile( i +": "+ schicht.getName(),
                        String.valueOf( schicht.getDicke() ),
                        String.valueOf( schicht.getWaermeleitfaehigkeit() ),
                        String.valueOf( schicht.getWaermedurchgangswiderstand() ),
                        String.valueOf( schicht.getDiffusionswiderstand() ),
                        String.valueOf( schicht.getDiffusionsleitwiderstand() ),
                        " ", " ", " " );
            if (pv_momentan > Druck.sattigungsdampfdruck( phi_momentan )) {
                t.addZeile( "  K-Ebene", " ", " ", " "," "," ",
                            String.valueOf( phi_momentan ),
                            String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ),
                            String.valueOf( pv_momentan ) );
                kebenen.add( i );
                kebenendruck.add( Druck.sattigungsdampfdruck( phi_momentan ) );
            } else t.addZeile( " ", " ", " "," "," "," ", String.valueOf( phi_momentan ),
                               String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ),
                               String.valueOf( pv_momentan ) );
        }
        //Von Bauteil nach außen... jetzt sollte phi_momentan =~ phi_exterior gelten
        t.addZeile( "Aussen", " "," ", String.valueOf( R_exterior_surface )," "," "," "," "," " );
        phi_momentan = phi_momentan - Transmissionswaermestromdichte*R_exterior_surface;
        t.addZeile( " "," "," "," "," "," ",String.valueOf( phi_momentan ), String.valueOf( Druck.sattigungsdampfdruck( phi_momentan ) ), String.valueOf( pv_momentan ) );

        //Anschließend den Rest ausrechnen
        String k = getKondensatsmengenMarkdown( kebenen, kebenendruck, pv_interior, pv_exterior, istdach );
        return t.TabletoMarkdown()+"\n"+k;
    }
}
