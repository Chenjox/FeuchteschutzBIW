package Util.Table;

import java.util.ArrayList;

public abstract class Table<T> {
    ///Spalte < Zeilen >
    private ArrayList<ArrayList<T>> table;

    public Table(){
        this.table = new ArrayList<>();
    }
    public ArrayList<ArrayList<T>> getTable() {
        return table;
    }
    protected void addSpalte(ArrayList<T> eintraege){
        this.table.add( eintraege );
    }
    public void addZeile(ArrayList<T> Zeile){
        for (int i = 0; i < this.table.size(); i++) {
            table.get( i ).add( Zeile.get(i) );
        }
    }
    public int getAnzahlZeilen(){
        assert !this.table.isEmpty();
        return this.table.get( 0 ).size();
    }
    public int getAnzahlSpalten(){
        return this.table.size();
    }
    protected ArrayList<T> getSpalte(int index){
        return table.get( index );
    }
    public abstract String TableToString();
}
