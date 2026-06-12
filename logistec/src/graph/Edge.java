package graph;

/**
 * Representa una arista no dirigida ponderada (calle bidireccional) en el grafo de la ciudad.
 * El peso de la arista corresponde a la distancia en metros entre los dos vértices.
 *
 * <p>Implementa {@link Comparable} por peso para ser usada en el ordenamiento
 * que realiza el algoritmo de Kruskal.
 *
 * @author Pablo V
 * @version 1.0
 */
public class Edge implements Comparable<Edge> {

    private final int u;
    private final int v;
    private final int weight;

    /**
     * Crea una arista entre los vértices {@code u} y {@code v} con el peso indicado.
     *
     * @param u      Índice del primer extremo de la arista.
     * @param v      Índice del segundo extremo de la arista.
     * @param weight Peso de la arista en metros (debe ser un entero positivo).
     */
    public Edge(int u, int v, int weight) {
        this.u      = u;
        this.v      = v;
        this.weight = weight;
    }

    /**
     * Retorna el índice del primer extremo de la arista.
     *
     * @return Índice del vértice u.
     */
    public int getU() { return u; }

    /**
     * Retorna el índice del segundo extremo de la arista.
     *
     * @return Índice del vértice v.
     */
    public int getV() { return v; }

    /**
     * Retorna el peso de la arista (distancia en metros).
     *
     * @return Peso de la arista.
     */
    public int getWeight() { return weight; }

    /**
     * Dado un extremo de la arista, retorna el extremo opuesto.
     * Útil para recorrer la lista de adyacencia sin saber cuál es el origen.
     *
     * @param vertex Índice de uno de los extremos de la arista.
     * @return Índice del extremo opuesto.
     * @throws IllegalArgumentException Si {@code vertex} no es ninguno de los extremos.
     */
    public int other(int vertex) {
        if (vertex == u) return v;
        if (vertex == v) return u;
        throw new IllegalArgumentException("Vértice " + vertex + " no pertenece a la arista (" + u + "," + v + ")");
    }

    /**
     * Compara esta arista con otra por su peso en orden ascendente.
     * Usado por el algoritmo de Kruskal para ordenar las aristas.
     *
     * @param o La otra arista con la que se compara.
     * @return Valor negativo si esta arista pesa menos, positivo si pesa más, cero si son iguales.
     */
    @Override
    public int compareTo(Edge o) {
        return Integer.compare(this.weight, o.weight);
    }

    @Override
    public String toString() {
        return "Edge{" + u + " -- " + v + " [" + weight + "m]}";
    }
}
