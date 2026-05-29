package algorithms;

import graph.Edge;
import graph.Graph;
import util.LinkedList;
import util.MinHeap;


public class Prim {

    private static final int INF = Integer.MAX_VALUE / 2;

    private final LinkedList<Edge> mstEdges;
    private final int              totalWeight;
    private final long             elapsedNano;   // empirical timing

    /**
     * Run Prim's algorithm on graph {@code g} starting from vertex 0.
     */
    public Prim(Graph g) {
        long start = System.nanoTime();

        int V = g.numVertices();
        boolean[] inTree = new boolean[V];
        int[]     key    = new int[V];     // minimum edge weight connecting v to tree
        int[]     parent = new int[V];

        for (int i = 0; i < V; i++) { key[i] = INF; parent[i] = -1; }
        key[0] = 0;

        MinHeap<Integer> pq = new MinHeap<>();
        for (int i = 0; i < V; i++) pq.insert(i, key[i]);

        mstEdges = new LinkedList<>();
        int total = 0;

        while (!pq.isEmpty()) {
            int u = pq.extractMin();
            if (inTree[u]) continue;
            inTree[u] = true;

            if (parent[u] != -1) {
                mstEdges.addLast(new Edge(parent[u], u, key[u]));
                total += key[u];
            }

            for (Graph.AdjEntry e : g.adj(u)) {
                int v = e.target;
                if (!inTree[v] && e.weight < key[v]) {
                    key[v]    = e.weight;
                    parent[v] = u;
                    pq.decreaseKey(v, e.weight);
                }
            }
        }

        totalWeight = total;
        elapsedNano = System.nanoTime() - start;
    }

    // ── Results ────────────────────────────────────────────────────────────

    public LinkedList<Edge> getMSTEdges()  { return mstEdges; }
    public int              getTotalWeight() { return totalWeight; }
    public long             getElapsedNano() { return elapsedNano; }
    public double           getElapsedMs()   { return elapsedNano / 1_000_000.0; }

    /**
     * Build a new Graph containing only the MST edges (for DFS traversal).
     */
    public Graph buildMSTGraph(int V) {
        Graph mst = new Graph(V);
        for (Edge e : mstEdges) {
            mst.addEdge(e.getU(), e.getV(), e.getWeight());
        }
        return mst;
    }

    @Override
    public String toString() {
        return "Prim MST: totalWeight=" + totalWeight + "m, time=" + String.format("%.3f", getElapsedMs()) + "ms";
    }
}
