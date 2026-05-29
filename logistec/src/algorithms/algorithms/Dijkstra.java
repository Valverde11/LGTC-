package algorithms;

import graph.Graph;
import util.LinkedList;
import util.MinHeap;

public class Dijkstra {

    private static final int INF = Integer.MAX_VALUE / 2;

    private final int[]   dist;    // dist[v] = shortest distance from src to v
    private final int[]   prev;    // prev[v] = predecessor on shortest path
    private final int     src;
    private final int     V;

    
    public Dijkstra(Graph g, int src) {
        this.src  = src;
        this.V    = g.numVertices();
        this.dist = new int[V];
        this.prev = new int[V];

        for (int i = 0; i < V; i++) { dist[i] = INF; prev[i] = -1; }
        dist[src] = 0;

        MinHeap<Integer> pq = new MinHeap<>();
        pq.insert(src, 0);

        while (!pq.isEmpty()) {
            int u = pq.extractMin();

            for (Graph.AdjEntry e : g.adj(u)) {
                int v   = e.target;
                int alt = dist[u] + e.weight;
                if (alt < dist[v]) {
                    dist[v] = alt;
                    prev[v] = u;
                    pq.decreaseKey(v, alt);
                }
            }
        }
    }

    // ── Query ─────────────────────────────────────────────────────────────

    /**
     * Shortest distance from src to dst, or {@code Integer.MAX_VALUE/2} if
     * unreachable.
     */
    public int distanceTo(int dst) {
        return dist[dst];
    }

    /** True if dst is reachable from src. */
    public boolean hasPath(int dst) { return dist[dst] < INF; }

    /**
     * Reconstruct the shortest path from src to dst as a list of vertex indices.
     * Returns empty list if unreachable.
     */
    public LinkedList<Integer> pathTo(int dst) {
        LinkedList<Integer> path = new LinkedList<>();
        if (!hasPath(dst)) return path;
        // Trace back from dst to src
        LinkedList<Integer> reversed = new LinkedList<>();
        for (int v = dst; v != -1; v = prev[v]) reversed.addFirst(v);
        return reversed;
    }

    /** Full distance array (indexed by vertex). */
    public int[] getDistances() { return dist; }

    /** Full predecessor array. */
    public int[] getPrev() { return prev; }

    public int getSource() { return src; }

    /**
     * Format the result as a human-readable string for the given graph.
     */
    public String resultToString(Graph g, int dst) {
        if (!hasPath(dst)) {
            return "No path from " + g.getVertex(src).getId()
                 + " to " + g.getVertex(dst).getId();
        }
        LinkedList<Integer> path = pathTo(dst);
        StringBuilder sb = new StringBuilder();
        sb.append("Shortest path [").append(g.getVertex(src).getId())
          .append(" → ").append(g.getVertex(dst).getId())
          .append("]: ");
        boolean first = true;
        for (int v : path) {
            if (!first) sb.append(" → ");
            sb.append(g.getVertex(v).getId());
            first = false;
        }
        sb.append("  (").append(dist[dst]).append(" m)");
        return sb.toString();
    }
}
