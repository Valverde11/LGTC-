package algorithms;

import graph.Edge;
import graph.Graph;
import util.LinkedList;
import util.MinHeap;

/**
 * Algoritmo de Prim para el Árbol de Expansión Mínima (MST).
 * Crece el árbol vértice por vértice eligiendo siempre la arista frontera de menor peso.
 * Complejidad: O((V + E) log V) con heap binario.
 *
 * @author Alejandro Arias Lopez
 */
public class Prim {

    // Valor centinela para "costo infinito" (vértice aún fuera del alcance del árbol)
    private static final int INF = Integer.MAX_VALUE / 2;

    private final LinkedList<Edge> mstEdges;   // aristas seleccionadas para el MST
    private final int              totalWeight; // suma de pesos del MST
    private final long             elapsedNano; // tiempo de ejecución

    /**
     * Ejecuta Prim sobre el grafo {@code g} comenzando desde el vértice 0.
     *
     * @param g Grafo no dirigido ponderado.
     */
    public Prim(Graph g) {
        long start = System.nanoTime(); // medimos tiempo empírico

        int V = g.numVertices();

        // ── Inicialización ────────────────────────────────────────────────────
        boolean[] inTree = new boolean[V]; // inTree[v] = v ya fue incorporado al MST
        int[]     key    = new int[V];     // key[v] = menor peso de arista frontera que conecta v al árbol
        int[]     parent = new int[V];     // parent[v] = vértice del árbol que conecta a v con el menor key

        for (int i = 0; i < V; i++) {
            key[i]    = INF; // inicialmente ningún vértice tiene arista frontera conocida
            parent[i] = -1;  // sin predecesor
        }
        // El vértice 0 inicia con clave 0 (ya está "en" el árbol como punto de partida)
        key[0] = 0;

        // ── Cola de prioridad ─────────────────────────────────────────────────
        // Insertamos TODOS los vértices con sus claves iniciales.
        // Esto es menos eficiente que la versión lazy (solo insertar cuando se actualiza),
        // pero simplifica la implementación para los fines del curso.
        MinHeap<Integer> pq = new MinHeap<>();
        for (int i = 0; i < V; i++) pq.insert(i, key[i]);

        mstEdges = new LinkedList<>();
        int total = 0;

        // ── Ciclo principal ───────────────────────────────────────────────────
        while (!pq.isEmpty()) {
            // Extraemos el vértice fuera del árbol con menor arista frontera
            int u = pq.extractMin();

            // Protección: si u ya fue incorporado (puede pasar con la inserción masiva),
            // lo saltamos. En grafos normales esto no ocurre, pero es buena práctica.
            if (inTree[u]) continue;

            inTree[u] = true; // incorporamos u al MST

            // Si u tiene predecesor, la arista (parent[u], u) entra al MST
            if (parent[u] != -1) {
                mstEdges.addLast(new Edge(parent[u], u, key[u]));
                total += key[u]; // acumulamos el costo de la arista
            }

            // ── Relajación de aristas frontera ────────────────────────────────
            // Revisamos todos los vecinos de u. Si alguno no está en el árbol
            // y la arista (u, v) tiene menor peso que el key actual de v, actualizamos.
            for (Graph.AdjEntry e : g.adj(u)) {
                int v = e.target;
                if (!inTree[v] && e.weight < key[v]) {
                    key[v]    = e.weight; // nueva arista frontera más barata para v
                    parent[v] = u;        // u es el nuevo predecesor de v en el MST
                    pq.decreaseKey(v, e.weight); // actualizamos la prioridad de v
                }
            }
        }

        totalWeight = total;
        elapsedNano = System.nanoTime() - start;
    }

    /** @return Aristas del MST. */
    public LinkedList<Edge> getMSTEdges()  { return mstEdges; }

    /** @return Peso total del MST en metros. */
    public int              getTotalWeight() { return totalWeight; }

    /** @return Tiempo de ejecución en nanosegundos. */
    public long             getElapsedNano() { return elapsedNano; }

    /** @return Tiempo de ejecución en milisegundos. */
    public double           getElapsedMs()   { return elapsedNano / 1_000_000.0; }

    /**
     * Construye un grafo con solo las aristas del MST, para recorridos posteriores.
     *
     * @param V Número de vértices.
     * @return Grafo MST.
     */
    public Graph buildMSTGraph(int V) {
        Graph mst = new Graph(V);
        for (Edge e : mstEdges)
            mst.addEdge(e.getU(), e.getV(), e.getWeight());
        return mst;
    }

    @Override
    public String toString() {
        return "Prim MST: totalWeight=" + totalWeight
             + "m, time=" + String.format("%.3f", getElapsedMs()) + "ms";
    }
}
