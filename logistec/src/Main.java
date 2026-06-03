

import algorithms.*;
import graph.*;
import io.*;
import javax.swing.*;
import planner.Planner;
import ui.MainWindow;
import util.LinkedList;

/**
 * LogísTEC — Entry point.
 *
 * <p>Usage:
 * <ul>
 *   <li>{@code java -jar logistec.jar}         — Opens the GUI.</li>
 *   <li>{@code java -jar logistec.jar <file>}  — Runs headless on the given JSON and prints the report.</li>
 * </ul>
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            // GUI mode
            SwingUtilities.invokeLater(MainWindow::new);
        } else {
            // Headless mode
            headless(args[0]);
        }
    }

    private static void headless(String jsonPath) throws Exception {
        System.out.println("LogísTEC — Modo texto");
        System.out.println("Cargando: " + jsonPath);

        // 1. Load
        CityLoader loader = new CityLoader();
        loader.load(jsonPath);
        Graph               graph    = loader.getGraph();
        LinkedList<Parcel> packages = loader.getParcels();
        LinkedList<Truck>   trucks   = loader.getTrucks();

        System.out.println(graph);

        // 2. Algorithms
        Warshall      warshall = new Warshall(graph);
        FloydWarshall fw       = new FloydWarshall(graph);
        Prim          prim     = new Prim(graph);
        Kruskal       kruskal  = new Kruskal(graph);

        System.out.println(prim);
        System.out.println(kruskal);

        // 3. Plan
        Planner planner = new Planner(graph, fw, warshall);
        planner.validateReachability(packages);
        planner.assignPackages(packages, trucks);
        planner.planRoutes(trucks);

        // 4. Report
        ReportGenerator rg = new ReportGenerator(graph, packages, trucks, fw, warshall, prim, kruskal);
        System.out.println(rg.generate());
    }
}
