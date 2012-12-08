/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork;

import tariffnetwork.datastructures.Arc;
import tariffnetwork.datastructures.Commodity;
import tariffnetwork.datastructures.Graph;
import branchandbound.*;

/**
 *
 * @author daniel
 */
public class TariffNetwork {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        GraphGenerator g = new GraphGenerator();
        DataFileWriter dfw = new DataFileWriter();

        try {


            //Graph graph = g.getGraph(11);
//
//            Arc[][] mat = new Arc[12][12];
//
//            for (int i = 0; i < mat.length; i++) {
//                for (int j = 0; j < mat.length; j++) {
//                    mat[i][j] = new Arc();
//                }
//            }
//
//            mat[0][3] = new Arc(3);
//            mat[0][4] = new Arc(3);
//
//            mat[1][2] = new Arc(7);
//            mat[1][3] = new Arc(4, true);
//            mat[1][4] = new Arc(4);
//
//            mat[2][5] = new Arc(6, true);
//            mat[2][7] = new Arc(4);
//            mat[2][10] = new Arc(4);
//
//            mat[3][9] = new Arc(7);
//
//            mat[4][6] = new Arc(3, true);
//            mat[4][7] = new Arc(6);
//            mat[4][8] = new Arc(4);
//
//            mat[6][2] = new Arc(3);
//            mat[6][4] = new Arc(5, true);
//            mat[6][7] = new Arc(3, true);
//            mat[6][9] = new Arc(4);
//
//            mat[7][5] = new Arc(2);
//            mat[7][9] = new Arc(5);
//
//            mat[8][3] = new Arc(2);
//            mat[8][6] = new Arc(2);
//
//            mat[11][8] = new Arc(3);
//
//            int[] labels = new int[12];
//            for (int i = 0; i < 12; i++) {
//                labels[i] = i;
//            }
//
//            Commodity[] commodities = {new Commodity(0, 5, 3), new Commodity(1, 9), new Commodity(11, 10)};

            //Graph graph = new Graph(mat, commodities, labels);
            Graph graph = g.getGraph(10, 2, 20, 3);
            g.printGraph(graph);

            BranchAndBoundSolver.Solve(graph, PathReducer.getReducedPathList(graph));
            System.out.println();
            System.out.println();
            

            Graph SPGMGraph = GraphReducer.reduceGraphToSPGM(graph);
            branchandbound.BranchAndBoundSolver.Solve(SPGMGraph, PathReducer.getReducedPathList(SPGMGraph));
            System.out.println();
            System.out.println();

            Graph SPGMSenzaSemplificazione = GraphReducer.getTotalSPGMWithoutSemplification(SPGMGraph);
            //dfw.writeArcDataFile("filearchi5.dat", SPGMSenzaSemplificazione);
            branchandbound.BranchAndBoundSolver.Solve(SPGMSenzaSemplificazione, PathReducer.getReducedPathList(SPGMSenzaSemplificazione));

            System.out.println();
            System.out.println();
            Graph SPGMSemplificato = GraphReducer.getTotalSPGM(SPGMGraph);
            branchandbound.BranchAndBoundSolver.Solve(SPGMSemplificato, PathReducer.getReducedPathList(SPGMSemplificato));
            
//            System.out.println();
//            GraphReducer.printGraph(SPGMGraph, 10);
//            System.out.println();
//            GraphReducer.printGraph(SPGMSenzaSemplificazione, 10);
//            System.out.println();
//            GraphReducer.printGraph(SPGMSemplificato, 10);

            
            dfw.writeArcDataFile("filearchi5.dat", SPGMSemplificato);
            dfw.writeReducedPathDataFile("PathAnalisysVecchioSPGMReduced.dat", SPGMSenzaSemplificazione);
            
            dfw.writeReducedPathDataFile("PathAnalisysSPGMReduced.dat", SPGMSemplificato);
            

            //dfw.writeReducedPathDataFile("PathAnalisysReduced.dat", graph);
            //dfw.writePathDataFile("PathAnalisysSPGM.dat", newGraph);
            //dfw.writePathDataFile("PathAnalisys.dat", graph);
            
            
            System.out.println();

        } catch (Exception e) {
            System.out.println("Errore nel Main di TariffNetwork: " + e.getMessage());
        }

    }
}
