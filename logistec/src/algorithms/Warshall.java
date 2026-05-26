package algorithms;

import graph.Graph;


public class Warshall {

    private final boolean[][] reach;
    private final int         V;

  
    public Warshall(Graph g) {
        this.V     = g.numVertices();
        this.reach = g.booleanMatrix();  // initial: edge(i,j) → true, i==j → true
        compute();
    }

    /**
     * Construct from an already-built boolean matrix (useful for subgraphs).
     */
    public Warshall(boolean[][] initialMatrix) {
        this.V     = initialMatrix.length;
        this.reach = new boolean[V][V];
        for (int i = 0; i < V; i++)
            System.arraycopy(initialMatrix[i], 0, reach[i], 0, V);
        compute();
    }

    // ── Core algorithm ───────────────────────────────────────────────────────

    /**
     * Warshall recurrence:
     * P[k][i][j] = P[k-1][i][j] OR (P[k-1][i][k] AND P[k-1][k][j])
     *
     * Applied in-place (P[k] overwrites P[k-1] since we only read [i][k] and [k][j]
     * which are not modified in iteration k).
     */
    private void compute() {
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    reach[i][j] = reach[i][j] || (reach[i][k] && reach[k][j]);
                }
            }
        }
    }

    // ── Query ────────────────────────────────────────────────────────────────

    /**
     * Return true if vertex {@code dst} is reachable from vertex {@code src}.
     */
    public boolean canReach(int src, int dst) {
        return reach[src][dst];
    }

    /**
     * Return the full V×V reachability matrix.
     */
    public boolean[][] getMatrix() { return reach; }

    /**
     * Pretty-print the reachability matrix.
     */
    public String matrixToString(Graph g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Warshall Reachability Matrix (T=reachable, .=not):\n");
        // header
        sb.append("     ");
        for (int j = 0; j < V; j++)
            sb.append(String.format("%-4s", g.getVertex(j) != null ? g.getVertex(j).getId() : String.valueOf(j)));
        sb.append("\n");
        for (int i = 0; i < V; i++) {
            sb.append(String.format("%-4s ", g.getVertex(i) != null ? g.getVertex(i).getId() : String.valueOf(i)));
            for (int j = 0; j < V; j++)
                sb.append(reach[i][j] ? "T   " : ".   ");
            sb.append("\n");
        }
        return sb.toString();
    }
}
