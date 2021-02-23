package Feuchtbemessung;

import Texts.Table.Advanced.AdvFuncRow;
import Texts.Table.Advanced.AdvancedTable;
import Texts.Table.Rendering.AsciiDoc.AsciiDocTable;
import Texts.Table.Simple.SimpleTable;

import Util.Arraycheck.Check;
import Util.Druck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bauteil {

    private final double r_si;
    private final double r_se;
    private final double luft_i;
    private final double luft_e;
    private final double theta_i;
    private final double theta_e;
    private final boolean istdach;

    private final ArrayList<Schicht> Schichten;

    public Bauteil(double r_si, double r_se, double luft_i, double luft_e, double theta_i, double theta_e, boolean istdach){
        this.r_si = r_si;
        this.r_se = r_se;
        this.luft_i = luft_i;
        this.luft_e = luft_e;
        this.theta_i = theta_i;
        this.theta_e = theta_e;
        this.istdach = istdach;

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
        /*
        StringTable t = new StringTable()
                .addSpalte( "" )
                .addSpalte( "Fl\u00E4chenbezogene Wassermasse in [kg/m^2]" )
                .addSpalte( "Diffusion Innen in [kg/(m^2*s)]" )
                .addSpalte( "Diffusion au\u00DFen in [kg/(m^2*s)]" );
        StringTable ebenen = new StringTable().addSpalte( "Bauteilebenen" ).addSpalte( "Z in [s]" );

         */
        //Fall 1 Eine Kondensationsebene
        if(kebenenindezes.size()==1){
            return getKondensatmengenKondensationsebene( kebenenindezes.get(0), kebenensatdrucke.get(0), pv_interior, pv_exterior, istdach ).render();
        }else {
            //Fall 3 Ein kondensationsbereich
            if(Check.checkListContainsConsecutiveElements( kebenenindezes )){
                return getKondensatmengeKondensationsbereich( kebenenindezes.get( 0 ), kebenenindezes.get( kebenenindezes.size()-1 ), kebenensatdrucke, pv_interior, pv_exterior, istdach ).render();
            }else{
                return getKondensatmengenKondensationsebenen( kebenenindezes.get( 0 ), kebenenindezes.get( kebenenindezes.size()-1 ), kebenensatdrucke.get( 0 ), kebenensatdrucke.get( kebenensatdrucke.size()-1 ), pv_interior, pv_exterior, istdach).render();
            }
        }
    }

    private AsciiDocTable getKondensatmengenKondensationsebenen(int k_index_i, int k_index_e, double k_satdruck_i, double k_satdruck_e, double pv_interior, double pv_exterior, boolean istdach){

        AdvancedTable<Double> ta = AdvancedTable.getTableBuilder( Double.class ).setColumns( 8 ).setStandardMapper( d -> !d.isNaN() ? String.valueOf( d ): "" ).build();

        double Z_innennachk1 = getDiffusionsleitwiderstand(0, k_index_i);
        double Z_zwischenk1k2 = getDiffusionsleitwiderstand(k_index_i+1, k_index_e);
        double Z_k2nachaussen = getDiffusionsleitwiderstand(k_index_e+1,Schichten.size()-1);

        double einstroemende_Diffusion = (pv_interior-k_satdruck_i)/Z_innennachk1; //gvi
        double zwischen_Diffusion = (k_satdruck_i-k_satdruck_e)/Z_zwischenk1k2; //gvz
        double ausstroemende_Diffusion = (k_satdruck_e-pv_exterior)/Z_k2nachaussen; //gve

        double Kondensatmasse = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;

        ta.addRow( Kondensatmasse, einstroemende_Diffusion, zwischen_Diffusion, ausstroemende_Diffusion, pv_interior, k_satdruck_i, pv_exterior, k_satdruck_e );

        //Trocknung
        if(istdach){
            //Verdunstungszeiten
            double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk1); //gvi
            double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_k2nachaussen); //gve

            double zeit_evaporation_1 = ((einstroemende_Diffusion-zwischen_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER)/ausstroemende_Diffusion_innen;
            double zeit_evaporation_2 = ((zwischen_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER)/ausstroemende_Diffusion_aussen;

            double verdunstungsmasse = 0.0;

            if(zeit_evaporation_1>Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_2>Druck.VERDUNSTUNGSPERIODE_SOMMER){
                verdunstungsmasse=(ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
            }
            if(zeit_evaporation_1<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_2<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_1<zeit_evaporation_2){
                verdunstungsmasse= (ausstroemende_Diffusion_innen*zeit_evaporation_1) + (ausstroemende_Diffusion_aussen*zeit_evaporation_1)+((Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk1+Z_k2nachaussen)+ausstroemende_Diffusion_aussen)*(Druck.VERDUNSTUNGSPERIODE_SOMMER-zeit_evaporation_1);
            }
            if(zeit_evaporation_1<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_2<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_1>zeit_evaporation_2){
                verdunstungsmasse= (ausstroemende_Diffusion_innen*zeit_evaporation_1) + (ausstroemende_Diffusion_aussen*zeit_evaporation_2)+((Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk1+Z_k2nachaussen)+ausstroemende_Diffusion_innen)*(Druck.VERDUNSTUNGSPERIODE_SOMMER-zeit_evaporation_2);
            }
            ta.addRow( verdunstungsmasse, ausstroemende_Diffusion_innen, Double.NaN, ausstroemende_Diffusion_aussen, Druck.WASSERDAMPFPARTIALDRUCK_SOMMER, Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH, Druck.WASSERDAMPFPARTIALDRUCK_SOMMER, Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH );
        }else{
            //Verdunstungszeiten
            double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk1); //gvi
            double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_k2nachaussen); //gve

            double zeit_evaporation_1 = ((einstroemende_Diffusion-zwischen_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER)/ausstroemende_Diffusion_innen;
            double zeit_evaporation_2 = ((zwischen_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER)/ausstroemende_Diffusion_aussen;

            double verdunstungsmasse = 0.0;

            if(zeit_evaporation_1>Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_2>Druck.VERDUNSTUNGSPERIODE_SOMMER){
                verdunstungsmasse=(ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
            }
            if(zeit_evaporation_1<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_2<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_1<zeit_evaporation_2){
                verdunstungsmasse= (ausstroemende_Diffusion_innen*zeit_evaporation_1) + (ausstroemende_Diffusion_aussen*zeit_evaporation_1)+((Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk1+Z_k2nachaussen)+ausstroemende_Diffusion_aussen)*(Druck.VERDUNSTUNGSPERIODE_SOMMER-zeit_evaporation_1);
            }
            if(zeit_evaporation_1<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_2<Druck.VERDUNSTUNGSPERIODE_SOMMER&&zeit_evaporation_1>zeit_evaporation_2){
                verdunstungsmasse= (ausstroemende_Diffusion_innen*zeit_evaporation_1) + (ausstroemende_Diffusion_aussen*zeit_evaporation_2)+((Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk1+Z_k2nachaussen)+ausstroemende_Diffusion_innen)*(Druck.VERDUNSTUNGSPERIODE_SOMMER-zeit_evaporation_2);
            }
            ta.addRow( verdunstungsmasse, ausstroemende_Diffusion_innen, Double.NaN, ausstroemende_Diffusion_aussen, Druck.WASSERDAMPFPARTIALDRUCK_SOMMER, Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER, Druck.WASSERDAMPFPARTIALDRUCK_SOMMER, Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER );
        }

        AsciiDocTable outTable = new AsciiDocTable( ta );
        outTable.setTitle( "Kondensationsbilanz" );
        outTable.addColumn( 0, Arrays.asList("Kondensatmenge", "Verdunstungsmenge") );
        outTable.addHeaderRow(
                Arrays.asList(
                        "-",
                        "Fl\u00E4chenbezogene Wassermasse in [kg/m^2]",
                        "Diffusion Innen in [kg/(m^2*s)]",
                        "Diffusion Zwischen in [kg/(m^2*s)]",
                        "Diffusion au\u00DFen in [kg/(m^2*s)]",
                        "Partialdruck innen in [Pa]",
                        "S\u00E4ttigungsdruck K-Ebene-Innen in [Pa]",
                        "S\u00E4ttigungsdruck K-Ebene-Au\u00DFen in [Pa]",
                        "S\u00E4ttigungsdruck aussen in [Pa]"
                )
        );
        return outTable;
    }

    private AsciiDocTable getKondensatmengenKondensationsebene(int k_index, double k_satdruck, double pv_interior, double pv_exterior, boolean istdach){
        AdvancedTable<Double> ta = AdvancedTable.getTableBuilder( Double.class ).setColumns( 6 ).build();

        //Wichtige einzelheiten.
        double Z_innennachk = getDiffusionsleitwiderstand(0,k_index);
        double Z_knachaussen = getDiffusionsleitwiderstand(k_index+1,Schichten.size()-1);
        double einstroemende_Diffusion = (pv_interior-k_satdruck)/Z_innennachk;
        double ausstroemende_Diffusion = (k_satdruck-pv_exterior)/Z_knachaussen;
        double Kondensatmenge = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;

        ta.addRow( Kondensatmenge, einstroemende_Diffusion, ausstroemende_Diffusion, pv_interior, k_satdruck, pv_exterior );

        if(istdach){
            double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk);
            double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_knachaussen);

            double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
            ta.addRow(
                    Verdunstungsmasse,
                    ausstroemende_Diffusion_innen,
                    ausstroemende_Diffusion_aussen,
                    Druck.WASSERDAMPFPARTIALDRUCK_SOMMER,
                    Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH,
                    Druck.WASSERDAMPFPARTIALDRUCK_SOMMER
            );

        }else {
            double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_innennachk);
            double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_knachaussen);

            double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;
            ta.addRow( Verdunstungsmasse, ausstroemende_Diffusion_innen, ausstroemende_Diffusion_aussen, Druck.WASSERDAMPFPARTIALDRUCK_SOMMER, Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER, Druck.WASSERDAMPFPARTIALDRUCK_SOMMER );

        }
        AsciiDocTable outTable = new AsciiDocTable( ta );
        outTable.setTitle( "Kondensationsbilanz" );
        outTable.addColumn( 0, Arrays.asList("Kondensatmenge", "Verdunstungsmenge") );
        outTable.addHeaderRow(
                Arrays.asList(
                        "-",
                        "Fl\u00E4chenbezogene Wassermasse in [kg/m^2]",
                        "Diffusion Innen in [kg/(m^2*s)]",
                        "Diffusion au\u00DFen in [kg/(m^2*s)]",
                        "Partialdruck innen in [Pa]",
                        "S\u00E4ttigungsdruck K-Ebene in [Pa]",
                        "S\u00E4ttigungsdruck aussen in [Pa]"
                )
        );
        return outTable;
    }


    private AsciiDocTable getKondensatmengeKondensationsbereich(int index_i, int index_e, List<Double> kebenensatdrucke, double pv_interior, double pv_exterior, boolean istdach){
        AdvancedTable<Double> ta = AdvancedTable.getTableBuilder( Double.class ).setColumns( 7 ).build();
        //int index_i = kebenenindezes.get( 0 );
        //int index_e = kebenenindezes.get( kebenenindezes.size()-1 );
        double Z_InnennachBereich = getDiffusionsleitwiderstand(0, index_i+1);
        double Z_Bereich = getDiffusionsleitwiderstand(index_i+1, index_e);
        double Z_BereichnachAussen = getDiffusionsleitwiderstand(index_e+1, Schichten.size()-1 );
        /*
        ebenen.addZeile( index_i==0 ? "0" :"0-"+index_i,
                         String.valueOf( Z_InnennachBereich ) );
        ebenen.addZeile( ((index_i + 1) == index_e) ? String.valueOf( index_i + 1 ) : ((index_i + 1) + "-" + index_e),
                         String.valueOf( Z_Bereich ) );
        ebenen.addZeile( ((index_e + 1) == (Schichten.size() - 1)) ? String.valueOf( index_e + 1 ) : ((index_e + 1) + "-" + (Schichten.size() - 1)),
                         String.valueOf(Z_BereichnachAussen) );

         */

        double einstroemende_Diffusion = (pv_interior-kebenensatdrucke.get( 0 ))/Z_InnennachBereich;
        double ausstroemende_Diffusion = (kebenensatdrucke.get( kebenensatdrucke.size()-1 )-pv_exterior)/Z_BereichnachAussen;

        double Kondensatmasse = (einstroemende_Diffusion-ausstroemende_Diffusion)*Druck.KONDENSATIONSPERIODE_WINTER;
        ta.addRow(
                Kondensatmasse ,
                einstroemende_Diffusion ,
                ausstroemende_Diffusion ,
                pv_interior ,
                kebenensatdrucke.get( 0 ) ,
                pv_exterior ,
                kebenensatdrucke.get( kebenensatdrucke.size()-1 )
        );
        if(istdach){
            double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_InnennachBereich+Z_Bereich/2);
            double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_BereichnachAussen+Z_Bereich/2);

            double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;

            ta.addRow(
                    Verdunstungsmasse,
                    ausstroemende_Diffusion_innen,
                    ausstroemende_Diffusion_aussen,
                    Druck.WASSERDAMPFPARTIALDRUCK_SOMMER,
                    Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH,
                    Druck.WASSERDAMPFPARTIALDRUCK_SOMMER,
                    Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER_DACH
            );
        }else{
            double ausstroemende_Diffusion_innen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_InnennachBereich+Z_Bereich/2);
            double ausstroemende_Diffusion_aussen = (Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER-Druck.WASSERDAMPFPARTIALDRUCK_SOMMER)/(Z_BereichnachAussen+Z_Bereich/2);

            double Verdunstungsmasse = (ausstroemende_Diffusion_innen+ausstroemende_Diffusion_aussen)*Druck.VERDUNSTUNGSPERIODE_SOMMER;

            ta.addRow(
                    Verdunstungsmasse,
                    ausstroemende_Diffusion_innen,
                    ausstroemende_Diffusion_aussen,
                    Druck.WASSERDAMPFPARTIALDRUCK_SOMMER,
                    Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER,
                    Druck.WASSERDAMPFPARTIALDRUCK_SOMMER,
                    Druck.WASSERDAMPFSAETTIGUNGSDRUCK_SOMMER
            );
        }

        AsciiDocTable outTable = new AsciiDocTable( ta );
        outTable.setTitle( "Kondensationsbilanz" );
        outTable.addColumn( 0, Arrays.asList("Kondensatmenge", "Verdunstungsmenge") );
        outTable.addHeaderRow(
                Arrays.asList(
                        "-",
                        "Fl\u00E4chenbezogene Wassermasse in [kg/m^2]",
                        "Diffusion Innen in [kg/(m^2*s)]",
                        "Diffusion au\u00DFen in [kg/(m^2*s)]",
                        "Partialdruck innen in [Pa]",
                        "S\u00E4ttigungsdruck innen in [Pa]",
                        "Partialdruck au\u00DFen in [Pa]",
                        "S\u00E4ttigungsdruck au\u00DFen in [Pa]"
                )
        );
        return outTable;
    }

    public String getDampfdruckprofil(){
        return getRealDampfdruckprofilasString( theta_i, theta_e, r_si, r_se, luft_i, luft_e, istdach );
    }

    public String getRealDampfdruckprofilasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior){
        return getRealDampfdruckprofilasString( phi_interior, phi_exterior, R_interior_surface, R_exterior_surface, rel_luft_interior, rel_luft_exterior, false );
    }
    public String getRealDampfdruckprofilDachasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior){
        return getRealDampfdruckprofilasString( phi_interior, phi_exterior, R_interior_surface, R_exterior_surface, rel_luft_interior, rel_luft_exterior, true );
    }
    public String getRealDampfdruckprofilasString(double phi_interior, double phi_exterior, double R_interior_surface, double R_exterior_surface, double rel_luft_interior, double rel_luft_exterior, boolean istdach){
        AdvancedTable<Double> bauteilschichten = AdvancedTable.getTable( spec-> spec
        .setColumns( 8 )
        .setStandardMapper( d -> !d.isNaN() ? String.valueOf(Math.round( d*100000.0 )/100000.0) : "" )
        .addFunctionalRow( row -> row.setStandardOperator( Double::sum ).setEndRow( AdvFuncRow.ENDROW_FOR_ALL_ROWS ).setStartRow( 0 ) )
        );

        AdvancedTable<Double> ebenen = AdvancedTable.getTable( spec -> spec
            .setColumns( 8 ).setStandardMapper( d -> !d.isNaN() ? String.valueOf( Math.round( d*100000.0 )/100000.0 ) : "" )
        );

        //Transmissionswaermestromdichte
        double R_Bauteil = getWaemedurchgangswiderstand( R_interior_surface, R_exterior_surface );
        double Transmissionswaermestromdichte = (phi_interior-phi_exterior)/R_Bauteil;
        //Dampfdruckdichte
        double Z_Bauteil = getDiffusionsleitwiderstand();
        double pv_interior = Druck.partialdampfdruck( phi_interior, rel_luft_interior );
        double pv_exterior = Druck.partialdampfdruck( phi_exterior, rel_luft_exterior );
        double Dampfdruckdichte = (pv_interior-pv_exterior)/Z_Bauteil;
        //Von Innen zu Bauteil

        ebenen.addRow( Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, phi_interior, Druck.sattigungsdampfdruck( phi_interior ), pv_interior );


        bauteilschichten.addRow( 0.0, 0.0, R_interior_surface, 0.0, 0.0, Double.NaN, Double.NaN, Double.NaN );

        double phi_momentan = phi_interior-Transmissionswaermestromdichte*R_interior_surface;
        double pv_momentan = pv_interior;

        ebenen.addRow( Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, phi_momentan, Druck.sattigungsdampfdruck( phi_momentan ), pv_momentan );

        //Einmal durch das Bauteil
        ArrayList<Integer> kebenen = new ArrayList<>();
        ArrayList<Double> kebenendruck = new ArrayList<>();
        for (int i = 0; i < Schichten.size(); i++) {
            Schicht schicht = Schichten.get( i );
            pv_momentan = schicht.Partialdampfdruckabfall( pv_momentan, Dampfdruckdichte );
            phi_momentan = schicht.Temperaturabfall( phi_momentan, Transmissionswaermestromdichte );

            bauteilschichten.addRow( schicht.getDicke(), schicht.getWaermeleitfaehigkeit(), schicht.getWaermedurchgangswiderstand(), schicht.getDiffusionswiderstand(), schicht.getSd(), Double.NaN, Double.NaN, Double.NaN );

            if (pv_momentan > Druck.sattigungsdampfdruck( phi_momentan )) {
                ebenen.addRow( Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, phi_momentan, Druck.sattigungsdampfdruck( phi_momentan ), pv_momentan );

                kebenen.add( i );
                kebenendruck.add( Druck.sattigungsdampfdruck( phi_momentan ) );
            } else {
                ebenen.addRow( Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, phi_momentan, Druck.sattigungsdampfdruck( phi_momentan ), pv_momentan );
            }
        }
        //Von Bauteil nach außen... jetzt sollte phi_momentan =~ phi_exterior gelten
        bauteilschichten.addRow( 0.0, 0.0, R_exterior_surface, 0.0, 0.0, Double.NaN, Double.NaN, Double.NaN );

        phi_momentan = phi_momentan - Transmissionswaermestromdichte*R_exterior_surface;

        ebenen.addRow(  Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, phi_momentan, Druck.sattigungsdampfdruck( phi_momentan ), pv_momentan );


        //Beide Table joinen.
        SimpleTable<String> out = SimpleTable.<String>getTableBuilder().setColumns( 8 ).build();

        out.addRow( ebenen.getMappedRow( 0 ) );

        for (int i = 0; i < bauteilschichten.getRows()+ebenen.getRows()-1; i++) {
            if( i % 2 == 0 ){
                out.addRow( bauteilschichten.getMappedRow( i/2 ) );
            }else {
                out.addRow( ebenen.getMappedRow( i/2+1 ) );
            }
        }
        //Nettes Formatting machen
        AsciiDocTable outTable = new AsciiDocTable( out );
        outTable.setTitle( "Dampfdruckprofil" );
        outTable.setHasFooter( true );
        outTable.addRow( String.valueOf(Math.round( bauteilschichten.getComputedFunctionalRowEntry( 0, 0 )*1000.0 )/1000.0),
                         "",
                         String.valueOf(Math.round( bauteilschichten.getComputedFunctionalRowEntry( 2, 0 )*1000.0 )/1000.0),
                         "",
                         "",
                         "",
                         "",
                         "" );

        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < outTable.getRows(); i++) {
            if(i>2 && i<outTable.getRows()-3&& i % 2 == 1){
                temp.add( Schichten.get( (i-3)/2 ).getName() );
            }else temp.add( "" );
        }
        temp.set( 1, "Innen" );
        temp.set( temp.size()-3, "Au\u00DFen" );

        outTable.addColumn( 0, temp );

        for (int i = 0; i < outTable.getRows(); i++) {
            double satDruck = !outTable.getCell( 7, i ).isEmpty() ? Double.parseDouble( outTable.getCell( 7, i ) ) : 1.0;
            double partDruck = !outTable.getCell( 8, i ).isEmpty() ? Double.parseDouble( outTable.getCell( 8, i ) ) : 0.0;
            if(partDruck>satDruck) outTable.setCell( 0, i, "K-Ebene" );
        }

        outTable.setHasHeader( true );
        outTable.addHeaderRow( Arrays.asList( "Bauteilname",
                                       "Dicke in [m]",
                                       "\u03BB in [W/(m*K)]",
                                       "R in [m^2*K/W]" ,
                                       "mu in [-]" ,
                                       "s~d~ in [m]" ,
                                       "\u0398 in [\u00B0C]" ,
                                       "Sat. Druck in [Pa]" ,
                                       "Part. Druck in [Pa]" ));

        //Anschließend den Rest ausrechnen
        String k = getKondensatsmengen( kebenen, kebenendruck, pv_interior, pv_exterior, istdach );
        return outTable.render()+"\n\n"+k;
    }
}
