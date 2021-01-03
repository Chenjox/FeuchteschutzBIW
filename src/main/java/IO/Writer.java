package IO;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Writer {
    public static boolean WritetoFile(String o, String name, String dir){
        File f = new File( dir );
        if(f.exists()) {
            String Filename = dir + name + ".txt";
            f = new File( Filename );
            try {
                if (f.createNewFile()) {
                    try (java.io.Writer out = new BufferedWriter( new OutputStreamWriter(
                            new FileOutputStream( Filename ), StandardCharsets.UTF_8 ) )) {
                        out.write( o );
                    }
                } else {
                    System.out.println( "File " + Filename + " could not be created" );
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println( "Directory not found" );
        }
        return false;
    }
}
