package Feuchtbemessung;

import java.io.File;

public class Haupt {
    public static void main(String[] args) {
        File f = new File( "Input" );

        File[] inputs = f.isDirectory() ? f.listFiles( fi -> fi.isFile()&&fi.getName().endsWith( ".json" )) : null;

        if(inputs!=null)
        for (File pot:inputs) {
            try {
                BauteilFactory potentialBauteil = new BauteilFactory( pot.getAbsolutePath() );
                Bauteil b = potentialBauteil.build();
                IO.Writer.WritetoFile( b.getDampfdruckprofil(), pot.getName().replace( ".json", ".adoc" ), "Output/" );
            }catch (Exception e){
                e.printStackTrace();
                IO.Writer.WritetoFile( "Fehler beim Einlesen der Datei: "+e.toString(), pot.getName().replace( ".json", ".txt" ), "Output/" );
            }
        }

        //Beleg();
    }
}
