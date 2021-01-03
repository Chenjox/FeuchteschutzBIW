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
        Bauteil result = new Bauteil();
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
