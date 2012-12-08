//Progetto Modelli di Ottimizzazione           Prof. Castelli Lorenzo
//Buriola Matteo   -   Case Roberto

package branchandbound;
import java.util.*;
import java.io.*;
import tariffnetwork.datastructures.Graph;

/**
 * @author Buriola Matteo
 * @author Case Roberto
 */
public class BranchAndBoundSolver {
    //variabili globali accessibili da tutte le funzioni
    static Map<Integer,Commodity> datiPronti = new HashMap<Integer,Commodity>();    //dati pronti per l'elaborazione del branche and bound
    static Double ottimoAttuale = 0.0;                                              //guadagno del leader
    static Double incognitaOttima = 0.0;                                            //valore ottimo dell'incognita t, costo per unità di lunghezza


    public static void Solve(Graph graph, ArrayList<tariffnetwork.datastructures.PathInfo> pathInfoList) throws IOException,ClassNotFoundException{

        tariffnetwork.datastructures.Commodity[] commoditiesTariffNetwork = graph.getCommodities();
        Map<Integer,Double[][]> commoditiesMap = new HashMap<Integer, Double[][]>();

        //creo gli input per il Solve originale:
        int[] cardinalitaCommodity = new int[commoditiesTariffNetwork.length];
        for (int i=0; i<commoditiesTariffNetwork.length; i++){
            cardinalitaCommodity[i] = commoditiesTariffNetwork[i].getCardinality();

            int numberOfPathForThisCommodity = 0;
            for (int j=0; j<pathInfoList.size(); j++){
                //Se il path attuale e la commodity hanno sorgente e destinazione uguali, conto quanti ce ne sono
                if (pathInfoList.get(j).getSource() == commoditiesTariffNetwork[i].getSource() &&
                        pathInfoList.get(j).getDestination() == commoditiesTariffNetwork[i].getDestination()){
                     numberOfPathForThisCommodity++;
                }
            }
            Double[][] pathListDataForOneCommodity = new Double[numberOfPathForThisCommodity][4];

            int pathIndex = 0;
            for (int j=0; j<pathInfoList.size(); j++){
                //Se il path attuale e la commodity hanno sorgente e destinazione uguali, devo salvere costo e lunghezza tassabile di ogni path
                if (pathInfoList.get(j).getSource() == commoditiesTariffNetwork[i].getSource() &&
                        pathInfoList.get(j).getDestination() == commoditiesTariffNetwork[i].getDestination()){
                     pathListDataForOneCommodity[pathIndex][0] = (double)pathInfoList.get(j).getCost();
                     pathListDataForOneCommodity[pathIndex][1] = (double)pathInfoList.get(j).getTariffedLength();
                     pathListDataForOneCommodity[pathIndex][2] = 0.0;
                     pathListDataForOneCommodity[pathIndex][3] = 0.0;
                     pathIndex++;
                }

            }

            commoditiesMap.put(i, pathListDataForOneCommodity);
        }


        Solve(commoditiesMap, cardinalitaCommodity);

    }


    public static void Solve(Map<Integer,Double[][]> commoditiesMap, int[] cardinalitaCommodity) throws IOException,ClassNotFoundException{

        Double ubTot=0.0;                                                   //upper bound totale

        int numeroCommodity = commoditiesMap.size();                                    //leggo il numero delle commodity
        

        Double[][] tmax = new Double[numeroCommodity][3];                   //definisco la matrice tmax

        //fine lettura del file dati

        GregorianCalendar inizio = new GregorianCalendar();                 //tempo di inizio elaborazione
        long inizioInMill = inizio.getTimeInMillis();                       //in millisecondi

        //calcolo degli intervalli per ogni percorso e dei tmax per ogni commodities
        for(int i=0;i<numeroCommodity;i++){                                 //ciclo sul numero delle commodities
            Double[][] temp=commoditiesMap.get(i);                                    //estraggo i percorsi dalla struttura dati
            tmax[i][0]=(double)i;
            tmax[i][1]=0.0;                                                 //inizializzo il tmax a 0
            double min=temp[0][0];                                          //e il minimo dei costi fissi a 0
            for(int j=1;j<temp.length;j++){                                 //ciclo sui percorsi, parto da 1,ignoro il toll free path
                trovaLimiti(temp,j);                                        //calcolo limiti per il percorso j
                if(temp[j][3]>=tmax[i][1])tmax[i][1]=temp[j][3];            //controllo se è nuovo massimo, se si aggiorno tmax
                if(temp[j][0]<=min)min=temp[j][0];                          //controllo se è nuovo minimo, se si aggiorni min
            }                             
            tmax[i][2]=(temp[0][0]-min)*cardinalitaCommodity[i];            //calcolo upper bound per la commodity i con relativa cardinalità
        }

        //ordinamento della matrice tmax
        ordina(tmax);                                                  
        //printArray(tmax);
        for(int i=0;i<numeroCommodity;i++){                                 //inserisco tutto nella map datiPronti
            datiPronti.put(i,new Commodity(commoditiesMap.get(tmax[i][0].intValue()),tmax[i][2],cardinalitaCommodity[tmax[i][0].intValue()]));
        }

        //calcolo dell'upper bound totale
        for(int i=0; i<numeroCommodity; i++){                           
//            System.out.println("quantità commodity " + i + " : "+ datiPronti.get(i).getQuantità());
            ubTot += datiPronti.get(i).getUpBound();
            //printArray(datiPronti.get(i).getPercorsi());                    //metto a video le matrici delle commodity
        }

        //esecuzione del branch and bound
        BB(0, ubTot, 0.0, 0.0, tmax[0][1], 0.0);

        GregorianCalendar fine = new GregorianCalendar();                   //tempo di fine elaborazione
        long fineInMill = fine.getTimeInMillis();                           //in millisecondi

        System.out.print("tempo esecuzione: " + (fineInMill-inizioInMill) + "\n");
        //output dei risultati
//        System.out.print("upper bound totale: " + ubTot + "\n");
        System.out.print("t ottimo: " + incognitaOttima + "\n");
        System.out.print("guadagno totale: " + ottimoAttuale + "\n");
        for(int i=0; i<numeroCommodity; i++){                               //ciclo sulle commodities per scrivere i rispettivi percorsi ottimi
            Double[] sottima = datiPronti.get(i).getPercorsoOttimo();
            double costo= sottima[0]+sottima[1]*incognitaOttima;
//            System.out.print("il costo minimo per una unità della commodity " + i + " è: "+ costo +"\n");
//            System.out.print("usando il percorso: " + sottima[0] + " + "+ sottima[1] +"*t \n");
        }
    }//fine main

    //Branch and Bound
    public static boolean BB(int livello, Double ubSup, Double lbSup, Double minIntSup, Double maxIntSup, Double lunghPrec){

        //System.out.print("esamino la commodity: " + livello + "\n");
        Commodity k = datiPronti.get(livello);                                                              //estraggo dalla Map la commoditi k
        boolean ottimoSucc= false;                                                                          //è true se è stato trovato un nuovo valore ottimo nelle chiamate successive di bb

        for (int i=1; i<k.getPercorsi().length; i++){                                                       //ciclo su i percorsi della commodity k tranne il tfp
           // System.out.print("esamino il percorso : " + i + " della commodity "+livello+"\n");
            boolean ottimoLivello = false;                                                                  //è true se è stato trovato un valore ottimo totale in questo livello
            Double[] newInt = new Double[2];                                                                //estremi del nuovo intervallo soluzione
            if(k.getPercorsi()[i][3]==0)continue;                                                           //se il percorso è dominato passo a quello successivo
            boolean soluzione = trovaInt(minIntSup, maxIntSup, k.getPercorsi()[i][2], k.getPercorsi()[i][3], newInt);//calcolo l'intervallo soluzione tra quello precedente e quello del percorso attuale

            if(soluzione==false){                                                                           //se non c'è soluzione passo al percorso successivo
           //     System.out.println("intervalli non compatibili, analizzo il percorso successivo");
                continue;}
            else{                                                                                           //altrimenti calcolo i nuovi bound
                Double lunghAttuale = k.getQuantità()*k.getPercorsi()[i][1]+lunghPrec;                      //nuova lunghezza, tiene conto anche delle cardinalità
                Double lb = newInt[1]*(lunghAttuale);                                                       //nuovo lower bound
                Double ub = ubSup - k.getUpBound() + lb - lbSup;                                            //nuovo upper bound

                if(ub <= ottimoAttuale){                                                                    //se l'upper bound attuale e minore dell'ottimo passo al percorso successivo
        //            System.out.println("ub < ottimo, analizzo il percorso successivo");
                    continue;}
               if(lb > ottimoAttuale){                                                                      //se il lower bound è migliore dell'ottimo attuale aggiorno i valori
        //           System.out.println("Trovato nuovo ottimo");
                    ottimoAttuale = lb;
                    incognitaOttima = newInt[1];
                    ottimoLivello = true;                                                                   //trovato nuovo ottimo in questo livello
                    for(int j=livello+1; j<datiPronti.size(); j++){                                         //se è stato trovato l'ottimo in questo livello le commodity successive useranno il toll free path
                        datiPronti.get(j).setPercorsoOttimo(0);
                    }
               }

                if(livello+1 < datiPronti.size()){                                                          //se ci sono ancora commodity da analizzare si chiamo di nuovo il bb
       //             System.out.println("chiamo BB");
                    ottimoSucc = BB(livello + 1, ub, lb, newInt[0], newInt[1], lunghAttuale);               //con i nuovi parametri del percorso appena analizzato
      //              System.out.println("return BB");
                   }
                ottimoSucc = ottimoLivello | ottimoSucc;                                                    //se è stato trovato un ottimo in questo livello o in uno inferiore
                if(ottimoSucc)                                                                              //si aggiorna il percorso ottimo di questa commodity
                    k.setPercorsoOttimo(i);
            }
        }
        return ottimoSucc;                                                                                  //torna true se è stato trovato un valore ottimo su questo livello o in uno inferiore
    }
    
    //ordino la matrice secondo i valori della seconda colonna, il limite superiore dei percorsi
    public static void ordina(Double[][] matrice ){
        boolean swapped = true;
        int j = 0;
        Double tmp[];
        while (swapped) {
            swapped = false;
            j++;
            for (int i = 0; i < matrice.length - j; i++) {
                  if (matrice[i][1] < matrice[i + 1][1]) {
                        tmp = matrice[i];
                        matrice[i] = matrice[i + 1];
                        matrice[i + 1] = tmp;
                        swapped = true;
                  }
            }
        }
    }
    
    //trovo i limiti su t perchè un percorso sia minimo, se il percorso è dominato la soluzione sarà [0,0]
    public static void trovaLimiti(Double[][] p, int a){
        double t;
        double inf=0;
        double sup=650000.0;                     //valore arbitrariamente grande
        for(int i=0;i<p.length;i++){
            if(i==a)continue;
            if(p[a][1] >= p[i][1]){
                if(p[i][0] > p[a][0]){
                    t=(p[i][0]-p[a][0])/(p[a][1]-p[i][1]);
                    if(t<sup)sup=t;
                }
                else{
                    inf = 0;
                    sup = 0;
                    break;
                }
            }
            else{
                t=(p[a][0]-p[i][0])/(p[i][1]-p[a][1]);
                if(t>inf)inf=t;
            }
        }
        if(inf > sup){
            inf=0;
            sup=0;
        }
        p[a][2]=inf;
        p[a][3]=sup;
    }

    //confronto di intervalli
    public static boolean trovaInt(Double minSup, Double maxSup, Double minPer, Double maxPer, Double[] newInt){

        if(maxPer < minSup || minPer > maxSup)
            return false;                           //se gli intervalli sono incompatibili ritorno false
        else{
            if(minPer > minSup)
                newInt[0]= minPer;
            else
                newInt[0]=minSup;
            if(maxPer < maxSup)
                newInt[1]=maxPer;
            else
                newInt[1]=maxSup;
        }
        return true;                                //ritorno true se c'è soluzione
    }
    
   //stampo la matrice a video
    public static void printArray(Double matrice[][]){
        for(int i=0;i<matrice.length;i++){
            for(int j=0;j<matrice[i].length;j++){
                System.out.print(matrice[i][j]+" ");
            }
        System.out.println("");
        }
    }
}



