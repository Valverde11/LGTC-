package graph;

/**
 * Represents an undirected weighted edge (street) in the city graph.
 * Weight is distance in metres (positive integer).
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Edge implements Comparable<Edge> {

    private final int    u;        // index of first vertex
    private final int    v;        // index of second vertex
    private final int    weight;   // distance in metres

    public Edge(int u, int v, int weight) {
        this.u      = u;
        this.v      = v;
        this.weight = weight;
    }

    public int getU()      { return u; }
    public int getV()      { return v; }
    public int getWeight() { return weight; }

    /** The other endpoint given one endpoint. */
    public int other(int vertex) {
        if (vertex == u) return v;
        if (vertex == v) return u;
        throw new IllegalArgumentException("Vertex " + vertex + " not in edge (" + u + "," + v + ")");
    }

    /** Natural ordering by weight — used by Kruskal's sorting step. */
    @Override
    public int compareTo(Edge o) {
        return Integer.compare(this.weight, o.weight);
    }

    @Override
    public String toString() {
        return "Edge{" + u + " -- " + v + " [" + weight + "m]}";
    }
}
