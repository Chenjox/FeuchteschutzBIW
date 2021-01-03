package IO;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public abstract class Reader {
    public static JSONObject readJSONFromFile(String pfilepath){
        if(new File( pfilepath).exists()){return new JSONObject( getFileString( pfilepath ));}else return new JSONObject( "{\"content\":\"Eintrag existiert nicht!\"}");
    }
    private static String getFileString(String filepath){
        StringBuilder contentBuilder = new StringBuilder();
        //UTF-8 Encoding sollte gelernt sein... Die Normale FileReader Klasse hat ein Problem damit
        try (Stream<String> stream = Files.lines( Paths.get( filepath), StandardCharsets.UTF_8 )){
            stream.forEach(
                    s -> contentBuilder.append( s ).append( '\n' )
            );
        }catch (IOException e){
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}
