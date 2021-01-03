package Util.Table;

import java.util.ArrayList;
import java.util.Arrays;

public class StringTable extends Table<String> {

    public StringTable addSpalte(String name){
        ArrayList<String> sp = new ArrayList<>();
        sp.add( name );
        addSpalte( sp );
        return this;
    }
    public String getSpaltenname(int index){
        return getSpalte( index ).get( 0 );
    }
    public void addZeile(String...s1){
        if(s1.length==getAnzahlSpalten()){
            addZeile( new ArrayList<String>(Arrays.asList(s1)) );
        }
    }
    @Override
    public String TableToString() {
        ArrayList<Integer> spaltenbreiten = new ArrayList<>();
        for (int i = 0; i < getAnzahlSpalten(); i++) {
            spaltenbreiten.add( getLongestEntryInSpalte( i ) );
        }
        int totalbreite = spaltenbreiten.stream().mapToInt( Integer::intValue ).sum()+getAnzahlSpalten()*3+1;
        StringBuilder b = new StringBuilder();
        //Tabellenkopf
        for (int i = 0; i < totalbreite; i++) {
            b.append( '-' );
        }
        b.append( '\n' ).append( '|' );
        for (int i = 0; i < getAnzahlSpalten(); i++) {
            b.append( ' ' ).append( StringZuBreite( getSpaltenname( i ), spaltenbreiten.get( i ) )).append( ' ' ).append( '|' );
        }
        b.append( '\n' );
        for (int i = 0; i < totalbreite; i++) {
            b.append( '-' );
        }
        b.append( '\n' );
        //Jetzt die Zeilen
        for (int i = 1; i < getAnzahlZeilen(); i++) {
            b.append( '|' );
            for (int j = 0; j < getAnzahlSpalten(); j++) {
                b.append( ' ' ).append( StringZuBreite( getSpalte( j ).get( i ), spaltenbreiten.get( j )) ).append( ' ' ).append( '|' );
            }
            b.append( '\n' );
        }
        for (int i = 0; i < totalbreite; i++) {
            b.append( '-' );
        }
        return b.toString();
    }
    public String TabletoMarkdown(){
        ArrayList<Integer> spaltenbreiten = new ArrayList<>();
        for (int i = 0; i < getAnzahlSpalten(); i++) {
            spaltenbreiten.add( getLongestEntryInSpalte( i ) );
        }
        StringBuilder b = new StringBuilder();
        //Tabellenkopf
        b.append( '\n' ).append( '|' );
        for (int i = 0; i < getAnzahlSpalten(); i++) {
            b.append( ' ' ).append( StringZuBreite( getSpaltenname( i ), spaltenbreiten.get( i ) )).append( ' ' ).append( '|' );
        }
        b.append( '\n' ).append( '|' );
        for (int i = 0; i < getAnzahlSpalten(); i++) {
            b.append( ' ' ).append( CharAsBreite('-', spaltenbreiten.get( i ) )).append( ' ' ).append( '|' );
        }
        b.append( '\n' );
        //Jetzt die Zeilen
        for (int i = 1; i < getAnzahlZeilen(); i++) {
            b.append( '|' );
            for (int j = 0; j < getAnzahlSpalten(); j++) {
                b.append( ' ' ).append( StringZuBreite( getSpalte( j ).get( i ), spaltenbreiten.get( j )) ).append( ' ' ).append( '|' );
            }
            b.append( '\n' );
        }
        return b.toString();
    }
    private int getlongestEntryinSpalte(String Spaltenname){
        int result = 0;
        ArrayList<String> point = null;
        for (int i = 0; i < getAnzahlSpalten(); i++) {
            if(getSpalte( i ).get( 0 ).equals( Spaltenname )){point = getSpalte( i ); break;}
        }
        if(point!=null) for (String s : point) {
            if (result < s.length()) result = s.length();
        }
        return result;
    }
    private int getLongestEntryInSpalte(int index){
        int result = 0;
        for (String s : getSpalte( index )) {
            if (result < s.length()) result = s.length();
        }
        return result;
    }
    private static String StringZuBreite(String s, int breite){
        StringBuilder white = new StringBuilder();
        int ergaenzung = breite-s.length();
        for (int i = 0; i < ergaenzung; i++) {
            white.append( ' ' );
        }
        return s.concat( white.toString() );
    }
    private static String CharAsBreite(char s, int breite){
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < breite; i++) {
            b.append( s );
        }
        return b.toString();
    }
}
