package graph;

import util.LinkedList;


public class Graph {

    // ── Adjacency list entry ──────────────────────────────────────────────────

    /** A neighbour entry in the adjacency list: target index + edge weight. */
    public static class AdjEntry {
        public final int    target;
        public final int    weight;
        public AdjEntry(int target, int weight) {
            this.target = target;
            this.weight = weight;
        }
        @Override public String toString() { return "→" + target + "(" + weight + ")"; }
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Vertex[]             vertices;
    private final LinkedList<AdjEntry>[] adj;    // adjacency lists
    private final LinkedList<Edge>     edges;    // all edges (for Kruskal)
    private final int                  V;        // number of vertices
    private int                        E;        // number of edges
    private int                        depotIndex = -1;

    // ── Constructor ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Graph(int V) {
        this.V    = V;
        vertices  = new Vertex[V];
        adj       = new LinkedList[V];
        edges     = new LinkedList<>();
        for (int i = 0; i < V; i++) adj[i] = new LinkedList<>();
    }

    // ── Building the graph ────────────────────────────────────────────────────

    /**
     * Add a vertex at a given index.
     */
    public void addVertex(int index, Vertex v) {
        vertices[index] = v;
        if (v.isDepot()) depotIndex = index;
    }

    /**
     * Add an undirected weighted edge between indices u and v.
     */
    public void addEdge(int u, int v, int weight) {
        adj[u].addLast(new AdjEntry(v, weight));
        adj[v].addLast(new AdjEntry(u, weight));
        edges.addLast(new Edge(u, v, weight));
        E++;
    }

    // ── Vertex lookup ─────────────────────────────────────────────────────────

    /**
     * Find the index of a vertex by its String id. Returns -1 if not found.
     * O(V) — acceptable since V is small in city graphs.
     */
    public int indexOf(String id) {
        for (int i = 0; i < V; i++)
            if (vertices[i] != null && vertices[i].getId().equals(id)) return i;
        return -1;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public int    numVertices()           { return V; }
    public int    numEdges()              { return E; }
    public Vertex getVertex(int i)        { return vertices[i]; }
    public int    getDepotIndex()         { return depotIndex; }
    public LinkedList<AdjEntry> adj(int v){ return adj[v]; }
    public LinkedList<Edge> getAllEdges()  { return edges; }

    /** Return all vertex indices as a plain array. */
    public int[] vertexIndices() {
        int[] arr = new int[V];
        for (int i = 0; i < V; i++) arr[i] = i;
        return arr;
    }

    // ── Adjacency matrix (for Floyd-Warshall / Warshall) ─────────────────────

    /**
     * Build and return the V×V adjacency/weight matrix.
     * Cell [i][j] = weight of edge (i,j), or Integer.MAX_VALUE/2 if none.
     * Diagonal = 0.
     */
    public int[][] adjacencyMatrix() {
        int INF = Integer.MAX_VALUE / 2;
        int[][] mat = new int[V][V];
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) mat[i][j] = (i == j) ? 0 : INF;
        }
        for (int i = 0; i < V; i++) {
            for (AdjEntry e : adj[i]) {
                mat[i][e.target] = Math.min(mat[i][e.target], e.weight);
            }
        }
        return mat;
    }

    /**
     * Build and return the V×V boolean reachability matrix (for Warshall).
     * Initial value: true if i==j or edge (i,j) exists.
     */
    public boolean[][] booleanMatrix() {
        boolean[][] mat = new boolean[V][V];
        for (int i = 0; i < V; i++) {
            mat[i][i] = true;
            for (AdjEntry e : adj[i]) mat[i][e.target] = true;
        }
        return mat;
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph (V=").append(V).append(", E=").append(E).append(")\n");
        for (int i = 0; i < V; i++) {
            sb.append("  ").append(vertices[i] == null ? i : vertices[i].getId()).append(": ");
            sb.append(adj[i]).append("\n");
        }
        return sb.toString();
    }
}

