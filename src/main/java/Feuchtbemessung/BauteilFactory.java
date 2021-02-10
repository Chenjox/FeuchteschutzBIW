package Feuchtbemessung;

import IO.Reader;
import org.json.JSONArray;
import org.json.JSONObject;


public class BauteilFactory {
    private final JSONObject serialisiertesBauteil;

    public BauteilFactory(String pfilepath){
        this.serialisiertesBauteil = Reader.readJSONFromFile( pfilepath );
    }
    public Bauteil build(){
        JSONArray a = serialisiertesBauteil.getJSONArray( "Schichten" );
        Bauteil result = new Bauteil(
                serialisiertesBauteil.optDouble( "r_si", 0.25 ),
                serialisiertesBauteil.optDouble( "r_se", 0.04 ),
                serialisiertesBauteil.optDouble( "relative_luftfeuchte_i", 0.5 ),
                serialisiertesBauteil.optDouble( "relative_luftfeuchte_e", 0.8 ),
                serialisiertesBauteil.optDouble( "temperatur_i", 20.0 ),
                serialisiertesBauteil.optDouble( "temperatur_e", -5.0 ),
                serialisiertesBauteil.optBoolean( "IstDach", false )
        );
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.getJSONObject( i );
            result.addSchicht(
                    new Schicht(
                            o.getString( "Name"),
                            o.getDouble( "Dicke" ),
                            o.getDouble( "Waermeleitfaehigkeit" ),
                            o.getDouble( "Diffusionswiderstand" )
                    )
            );
        }
        return result;
    }
}
