//Progetto Modelli di Ottimizzazione           Prof. Castelli Lorenzo
//Buriola Matteo   -   Case Roberto

package branchandbound;


/**
 * @author Buriola Matteo
 * @author Case Roberto
 */
//classe per memorizzare le commodities
public class Commodity {

    private Double upBound;                                 //upper bound della commodity
    private int percorsoOttimo;                             //indice del percorso ottimo attuale
    private int quantità;                                   //cardinalità della commodity
    private Double[][] percorsi;                            //matrice dei percorsi

    //costruttori
    Commodity(Double[][] p, Double up, int po, int q){
        percorsi = p;
        percorsoOttimo = po;
        upBound = up;
        quantità = q;
    }
    Commodity(Double[][] p, Double up, int q){
        percorsi = p;
        percorsoOttimo = 0;
        upBound = up;
        quantità = q;
    }
    //funzione per leggere la matrice dei percorsi
    public Double[][] getPercorsi(){
        return percorsi;
    }
    //funzione per leggere l'upper bound
    public Double getUpBound(){
        return upBound;
    }
    //funzione per leggere la quantità
    public int getQuantità(){
        return quantità;
    }
    //funzione per ottnere un array con il percorso ottimo
    public Double[] getPercorsoOttimo(){
        return percorsi[percorsoOttimo];
    }
    //funzione per modificare l'indice del percorso ottimo
    public void setPercorsoOttimo(int po){
        percorsoOttimo = po;
    }
}

