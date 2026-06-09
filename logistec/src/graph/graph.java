package graph;

import util.LinkedList;

/**
 * TDA Grafo no dirigido ponderado con representación por lista de adyacencia.
 *
 * <p>Los vértices se identifican internamente por un índice entero (0..V-1)
 * y externamente por su identificador de tipo {@code String}. Las aristas son
 * bidireccionales y tienen un peso entero positivo (distancia en metros).
 *
 * <p>Complejidad de espacio: O(V + E).
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Graph {

    /**
     * Entrada en la lista de adyacencia: almacena el índice del vértice vecino
     * y el peso de la arista que los conecta.
     */
    public static class AdjEntry {
        /** Índice del vértice destino. */
        public final int target;
        /** Peso de la arista en metros. */
        public final int weight;

        /**
         * Crea una entrada de adyacencia.
         *
         * @param target Índice del vértice destino.
         * @param weight Peso de la arista en metros.
         */
        public AdjEntry(int target, int weight) {
            this.target = target;
            this.weight = weight;
        }

        @Override
        public String toString() { return "→" + target + "(" + weight + ")"; }
    }

    private final Vertex[]              vertices;
    private final LinkedList<AdjEntry>[] adj;
    private final LinkedList<Edge>      edges;
    private final int                   V;
    private int                         E;
    private int                         depotIndex = -1;

    /**
     * Crea un grafo vacío con capacidad para {@code V} vértices.
     *
     * @param V Número de vértices del grafo.
     */
    @SuppressWarnings("unchecked")
    public Graph(int V) {
        this.V   = V;
        vertices = new Vertex[V];
        adj      = new LinkedList[V];
        edges    = new LinkedList<>();
        for (int i = 0; i < V; i++) adj[i] = new LinkedList<>();
    }

    /**
     * Registra un vértice en la posición {@code index} del arreglo interno.
     * Si el vértice es de tipo DEPOT, actualiza el índice del depósito.
     *
     * @param index Posición en el arreglo de vértices (0 ≤ index < V).
     * @param v     Objeto {@link Vertex} que representa al vértice.
     */
    public void addVertex(int index, Vertex v) {
        vertices[index] = v;
        if (v.isDepot()) depotIndex = index;
    }

    /**
     * Agrega una arista no dirigida ponderada entre los vértices {@code u} y {@code v}.
     * La arista se registra en ambas listas de adyacencia (bidireccional).
     *
     * @param u      Índice del primer extremo de la arista.
     * @param v      Índice del segundo extremo de la arista.
     * @param weight Peso de la arista en metros (entero positivo).
     */
    public void addEdge(int u, int v, int weight) {
        adj[u].addLast(new AdjEntry(v, weight));
        adj[v].addLast(new AdjEntry(u, weight));
        edges.addLast(new Edge(u, v, weight));
        E++;
    }

    /**
     * Busca el índice de un vértice por su identificador de tipo {@code String}.
     * Recorre linealmente el arreglo de vértices. O(V).
     *
     * @param id Identificador del vértice a buscar.
     * @return Índice del vértice, o -1 si no existe ningún vértice con ese id.
     */
    public int indexOf(String id) {
        for (int i = 0; i < V; i++)
            if (vertices[i] != null && vertices[i].getId().equals(id)) return i;
        return -1;
    }

    /**
     * Retorna el número total de vértices del grafo.
     *
     * @return Número de vértices V.
     */
    public int numVertices() { return V; }

    /**
     * Retorna el número total de aristas del grafo.
     *
     * @return Número de aristas E.
     */
    public int numEdges() { return E; }

    /**
     * Retorna el vértice en la posición {@code i} del arreglo interno.
     *
     * @param i Índice del vértice (0 ≤ i < V).
     * @return Objeto {@link Vertex}, o {@code null} si no se ha registrado.
     */
    public Vertex getVertex(int i) { return vertices[i]; }

    /**
     * Retorna el índice del vértice de tipo DEPOT en el grafo.
     *
     * @return Índice del depósito, o -1 si no se ha registrado ningún depósito.
     */
    public int getDepotIndex() { return depotIndex; }

    /**
     * Retorna la lista de adyacencia del vértice {@code v}, es decir,
     * todas las entradas {@link AdjEntry} de sus vecinos directos.
     *
     * @param v Índice del vértice.
     * @return Lista enlazada con las entradas de adyacencia de v.
     */
    public LinkedList<AdjEntry> adj(int v) { return adj[v]; }

    /**
     * Retorna la lista plana de todas las aristas del grafo.
     * Útil para el algoritmo de Kruskal que necesita ordenar todas las aristas.
     *
     * @return Lista enlazada con todos los objetos {@link Edge} del grafo.
     */
    public LinkedList<Edge> getAllEdges() { return edges; }

    /**
     * Retorna un arreglo con todos los índices de vértices del grafo (0 hasta V-1).
     *
     * @return Arreglo de enteros {@code [0, 1, 2, ..., V-1]}.
     */
    public int[] vertexIndices() {
        int[] arr = new int[V];
        for (int i = 0; i < V; i++) arr[i] = i;
        return arr;
    }

    /**
     * Construye y retorna la matriz de adyacencia V×V con los pesos de las aristas.
     * {@code mat[i][j]} contiene el peso de la arista (i,j), 0 si i==j,
     * o {@code Integer.MAX_VALUE/2} si no existe arista entre i y j.
     * Usada como entrada inicial para Floyd-Warshall.
     *
     * @return Matriz de pesos V×V.
     */
    public int[][] adjacencyMatrix() {
        int INF = Integer.MAX_VALUE / 2;
        int[][] mat = new int[V][V];
        for (int i = 0; i < V; i++)
            for (int j = 0; j < V; j++) mat[i][j] = (i == j) ? 0 : INF;
        for (int i = 0; i < V; i++)
            for (AdjEntry e : adj[i])
                mat[i][e.target] = Math.min(mat[i][e.target], e.weight);
        return mat;
    }

    /**
     * Construye y retorna la matriz booleana de adyacencia V×V.
     * {@code mat[i][j] = true} si i==j o existe una arista directa entre i y j.
     * Usada como entrada inicial para el algoritmo de Warshall.
     *
     * @return Matriz booleana V×V de adyacencia directa.
     */
    public boolean[][] booleanMatrix() {
        boolean[][] mat = new boolean[V][V];
        for (int i = 0; i < V; i++) {
            mat[i][i] = true;
            for (AdjEntry e : adj[i]) mat[i][e.target] = true;
        }
        return mat;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grafo (V=").append(V).append(", E=").append(E).append(")\n");
        for (int i = 0; i < V; i++) {
            sb.append("  ").append(vertices[i] == null ? i : vertices[i].getId()).append(": ");
            sb.append(adj[i]).append("\n");
        }
        return sb.toString();
    }
}
