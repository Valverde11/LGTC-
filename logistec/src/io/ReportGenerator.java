package io;

import algorithms.*;
import graph.*;
import util.LinkedList;

/**
 * Generates the final plain-text report for a LogísTEC run.
 * No decorative lines or special characters — clean, readable output.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class ReportGenerator {

    private final Graph              graph;
    private final LinkedList<Parcel> packages;
    private final LinkedList<Truck>  trucks;
    private final FloydWarshall      fw;
    private final Warshall           warshall;
    private final Prim               prim;
    private final Kruskal            kruskal;

    public ReportGenerator(Graph graph,
                           LinkedList<Parcel> packages,
                           LinkedList<Truck>  trucks,
                           FloydWarshall      fw,
                           Warshall           warshall,
                           Prim               prim,
                           Kruskal            kruskal) {
        this.graph    = graph;
        this.packages = packages;
        this.trucks   = trucks;
        this.fw       = fw;
        this.warshall = warshall;
        this.prim     = prim;
        this.kruskal  = kruskal;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();

        title(sb, "LogisTEC - Reporte Final");

        // ── Grafo ──────────────────────────────────────────────────────────
        section(sb, "GRAFO DE LA CIUDAD");
        sb.append("  Vertices : ").append(graph.numVertices()).append("\n");
        sb.append("  Aristas  : ").append(graph.numEdges()).append("\n");
        sb.append("  Deposito : ").append(graph.getVertex(graph.getDepotIndex()).getId()).append("\n");

        // ── Warshall ───────────────────────────────────────────────────────
        section(sb, "CIERRE TRANSITIVO (Warshall)");
        sb.append(warshall.matrixToString(graph)).append("\n");

        // ── Paquetes ───────────────────────────────────────────────────────
        section(sb, "PAQUETES");
        sb.append(String.format("  %-6s  %-6s  %5s  %5s  %s\n",
                "ID", "Dest", "Peso", "Prior", "Estado"));
        sb.append("  " + "-".repeat(40) + "\n");
        for (Parcel p : packages) {
            String dest = graph.getVertex(p.getDestinationIndex()).getId();
            sb.append(String.format("  %-6s  %-6s  %4dkg  P%d     %s\n",
                    p.getId(), dest, p.getWeight(), p.getPriority(), p.getStatus()));
        }

        // ── MST ────────────────────────────────────────────────────────────
        section(sb, "ARBOL DE EXPANSION MINIMA");
        sb.append(String.format("  Prim    : %dm  (%.3f ms)\n",
                prim.getTotalWeight(), prim.getElapsedMs()));
        sb.append(String.format("  Kruskal : %dm  (%.3f ms)\n",
                kruskal.getTotalWeight(), kruskal.getElapsedMs()));
        if (prim.getTotalWeight() == kruskal.getTotalWeight())
            sb.append("  Verificacion: ambos producen el mismo costo total.\n");
        else
            sb.append("  ADVERTENCIA: costos distintos entre Prim y Kruskal.\n");

        sb.append("\n  Aristas MST (Prim):\n");
        for (Edge e : prim.getMSTEdges()) {
            String u = vId(e.getU()), v = vId(e.getV());
            sb.append(String.format("    %-4s -- %-4s  [%dm]\n", u, v, e.getWeight()));
        }

        // ── Floyd-Warshall ─────────────────────────────────────────────────
        section(sb, "MATRIZ DE DISTANCIAS MINIMAS (Floyd-Warshall)");
        sb.append(fw.matrixToString(graph)).append("\n");

        // ── Rutas ──────────────────────────────────────────────────────────
        section(sb, "RUTAS DE CAMIONES");
        for (Truck t : trucks) {
            sb.append("\n  Camion : ").append(t.getId()).append("\n");
            sb.append(String.format("  Carga  : %dkg / %dkg  (%.1f%%)\n",
                    t.getCurrentLoad(), t.getCapacity(), t.occupancyPercent()));

            if (t.getPackages().isEmpty()) {
                sb.append("  Sin paquetes asignados.\n");
                continue;
            }

            sb.append("  Paquetes: ");
            for (Parcel p : t.getPackages()) sb.append(p.getId()).append(" ");
            sb.append("\n");

            sb.append(String.format("  Distancia NN      : %dm\n", t.getRouteDistanceNN()));
            sb.append(String.format("  Distancia MST     : %dm\n", t.getRouteDistanceMST()));

            int best = Math.min(t.getRouteDistanceNN(), t.getRouteDistanceMST());
            String winner = t.getRouteDistanceMST() <= t.getRouteDistanceNN()
                          ? "MST-based" : "Nearest Neighbor";
            sb.append("  Heuristica ganadora: ").append(winner).append("\n");

            if (t.getRouteDistanceNN() > 0) {
                double saving = 100.0 * (t.getRouteDistanceNN() - t.getRouteDistanceMST())
                                      / t.getRouteDistanceNN();
                sb.append(String.format("  Ahorro MST vs NN  : %.1f%%\n", saving));
            }

            sb.append("  Ruta: ");
            boolean first = true;
            for (int v : t.getRoute()) {
                if (!first) sb.append(" -> ");
                sb.append(vId(v));
                first = false;
            }
            sb.append(String.format("  (%dm)\n", best));
            sb.append("  " + "-".repeat(48) + "\n");
        }

        // ── Paquetes rechazados ────────────────────────────────────────────
        section(sb, "PAQUETES RECHAZADOS");
        boolean any = false;
        for (Parcel p : packages) {
            if (p.getStatus() == Parcel.Status.REJECTED) {
                sb.append(String.format("  %-6s  dest=%-4s  %dkg  P%d  [%s]\n",
                        p.getId(), graph.getVertex(p.getDestinationIndex()).getId(),
                        p.getWeight(), p.getPriority(), p.getStatus()));
                any = true;
            }
        }
        if (!any) sb.append("  Ninguno.\n");

        return sb.toString();
    }

    // ── Format helpers ────────────────────────────────────────────────────────

    private void title(StringBuilder sb, String text) {
        sb.append("\n").append(text).append("\n");
        sb.append("=".repeat(text.length())).append("\n");
    }

    private void section(StringBuilder sb, String text) {
        sb.append("\n").append(text).append("\n");
        sb.append("-".repeat(text.length())).append("\n");
    }

    private String vId(int i) {
        Vertex v = graph.getVertex(i);
        return v != null ? v.getId() : String.valueOf(i);
    }
}
