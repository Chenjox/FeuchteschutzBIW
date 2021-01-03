package Feuchtbemessung;


public class Haupt {
    public static void main(String[] args) {
        Beleg();
    }
    public static void Beleg(){
        BauteilFactory DachF = new BauteilFactory( "Input/Betonflachdach.json" );
        BauteilFactory MauerF = new BauteilFactory( "Input/Mauerwerk.json" );
        Bauteil Dach = DachF.build();
        Bauteil Mauer = MauerF.build();
        IO.Writer.WritetoFile( Dach.getRealDampfdruckprofilDachasString( 20.0, -5.0, 0.25, 0.04, 0.5, 0.8 ),
                               "Dach25", "Output/");
        IO.Writer.WritetoFile( Mauer.getRealDampfdruckprofilasString( 20.0, -5.0, 0.25, 0.04, 0.5, 0.8 ),
                               "Mauer25", "Output/");
        IO.Writer.WritetoFile( Dach.getRealDampfdruckprofilDachasString( 20.0, -5.0, 0.13, 0.04, 0.5, 0.8 ),
                               "Dach13", "Output/");
        IO.Writer.WritetoFile( Mauer.getRealDampfdruckprofilasString( 20.0, -5.0, 0.13, 0.04, 0.5, 0.8 ),
                               "Mauer13", "Output/");
        IO.Writer.WritetoFile( Dach.getRealDampfdruckprofilasMarkdown( 20.0, -5.0, 0.25, 0.04, 0.5, 0.8 , true),
                               "Dach25md", "Output/");
        IO.Writer.WritetoFile( Mauer.getRealDampfdruckprofilasMarkdown( 20.0, -5.0, 0.25, 0.04, 0.5, 0.8 , false),
                               "Mauer25md", "Output/");
    };
}
