package algorithms;

import graph.Graph;

public class FloydWarshall {

    private static final int INF = Integer.MAX_VALUE / 2;

    private final int[][] dist;   // dist[i][j] = shortest path i → j
    private final int[][] next;   // next[i][j] = first step on path i → j
    private final int     V;


    public FloydWarshall(Graph g) {
        this.V    = g.numVertices();
        this.dist = g.adjacencyMatrix(); // initialised with edge weights / INF / 0
        this.next = new int[V][V];

        // Initialise next matrix
        for (int i = 0; i < V; i++)
            for (int j = 0; j < V; j++)
                next[i][j] = (dist[i][j] < INF && i != j) ? j : -1;

        compute();
    }

    // ── Core algorithm ────────────────────────────────────────────────────────

    /**
     * Floyd-Warshall recurrence:
     * D[k][i][j] = min( D[k-1][i][j], D[k-1][i][k] + D[k-1][k][j] )
     *
     * Applied in-place — safe because we only use dist[i][k] and dist[k][j]
     * which are not updated in the k-th outer iteration.
     */
    private void compute() {
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                if (dist[i][k] == INF) continue; // optimisation
                for (int j = 0; j < V; j++) {
                    if (dist[k][j] == INF) continue;
                    long candidate = (long) dist[i][k] + dist[k][j];
                    if (candidate < dist[i][j]) {
                        dist[i][j] = (int) candidate;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
    }

    // ── Query ──────────────────────────────────────────────────────────────

    /**
     * Shortest distance between i and j. Returns INF (Integer.MAX_VALUE/2)
     * if unreachable.
     */
    public int dist(int i, int j) { return dist[i][j]; }

    /** True if j is reachable from i. */
    public boolean reachable(int i, int j) { return dist[i][j] < INF; }

    /** Full V×V distance matrix. */
    public int[][] getDistMatrix() { return dist; }

    /**
     * Reconstruct the shortest path from i to j as an array of vertex indices.
     * Returns null if unreachable.
     */
    public int[] reconstructPath(int i, int j) {
        if (next[i][j] == -1 && i != j) return null;
        // count path length first
        int count = 1;
        int cur = i;
        while (cur != j) { cur = next[cur][j]; count++; }
        int[] path = new int[count];
        path[0] = i;
        cur = i;
        for (int p = 1; p < count; p++) {
            cur = next[cur][j];
            path[p] = cur;
        }
        return path;
    }

    /**
     * Pretty-print the distance matrix using vertex ids from graph g.
     */
    public String matrixToString(Graph g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Floyd-Warshall Distance Matrix (metres):\n");
        sb.append("      ");
        for (int j = 0; j < V; j++)
            sb.append(String.format("%-6s", g.getVertex(j) != null ? g.getVertex(j).getId() : j));
        sb.append("\n");
        for (int i = 0; i < V; i++) {
            sb.append(String.format("%-4s  ", g.getVertex(i) != null ? g.getVertex(i).getId() : i));
            for (int j = 0; j < V; j++) {
                if (dist[i][j] >= INF) sb.append(String.format("%-6s", "∞"));
                else sb.append(String.format("%-6d", dist[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
