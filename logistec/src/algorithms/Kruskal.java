package algorithms;

import graph.Edge;
import graph.Graph;
import util.LinkedList;
import util.UnionFind;

public class Kruskal {

    private final LinkedList<Edge> mstEdges;
    private final int              totalWeight;
    private final long             elapsedNano;

    /**
     * Run Kruskal's algorithm on graph {@code g}.
     */
    public Kruskal(Graph g) {
        long start = System.nanoTime();

        int V = g.numVertices();

        // 1. Collect all edges into an array and sort by weight
        LinkedList<Edge> allEdges = g.getAllEdges();
        Edge[] edgeArr = new Edge[allEdges.size()];
        int idx = 0;
        for (Edge e : allEdges) edgeArr[idx++] = e;
        sortEdges(edgeArr, 0, edgeArr.length - 1); // merge sort

        // 2. Process edges greedily
        UnionFind uf = new UnionFind(V);
        mstEdges = new LinkedList<>();
        int total = 0;

        for (Edge e : edgeArr) {
            if (uf.union(e.getU(), e.getV())) {
                mstEdges.addLast(e);
                total += e.getWeight();
                if (mstEdges.size() == V - 1) break; // MST complete
            }
        }

        totalWeight = total;
        elapsedNano = System.nanoTime() - start;
    }

    // ── Merge sort (ascending by weight) ─────────────────────────────────────

    private void sortEdges(Edge[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        sortEdges(arr, lo, mid);
        sortEdges(arr, mid + 1, hi);
        merge(arr, lo, mid, hi);
    }

    private void merge(Edge[] arr, int lo, int mid, int hi) {
        Edge[] tmp = new Edge[hi - lo + 1];
        int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi)
            tmp[k++] = arr[i].compareTo(arr[j]) <= 0 ? arr[i++] : arr[j++];
        while (i <= mid) tmp[k++] = arr[i++];
        while (j <= hi)  tmp[k++] = arr[j++];
        System.arraycopy(tmp, 0, arr, lo, tmp.length);
    }

    // ── Results ─────────────────────────────────────────────────────────────

    public LinkedList<Edge> getMSTEdges()    { return mstEdges; }
    public int              getTotalWeight() { return totalWeight; }
    public long             getElapsedNano() { return elapsedNano; }
    public double           getElapsedMs()   { return elapsedNano / 1_000_000.0; }

    /**
     * Build a Graph containing only the MST edges (for DFS traversal).
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
        return "Kruskal MST: totalWeight=" + totalWeight + "m, time=" + String.format("%.3f", getElapsedMs()) + "ms";
    }
}
