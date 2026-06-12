package algorithms;

import graph.Graph;
import util.LinkedList;
import util.MinHeap;

/**
 * Algoritmo de Dijkstra para calcular el camino más corto desde un vértice origen
 * hacia todos los demás vértices en un grafo con pesos no negativos.
 *
 * <p>Usa la cola de prioridad propia {@link MinHeap} en lugar de {@code java.util.PriorityQueue}.
 * Complejidad: O((V + E) log V).
 *
 * @author Alejandro Arias Lopez
 */
public class Dijkstra {

    // Valor centinela para "distancia infinita" (vértice no alcanzado aún).
    // Usamos MAX_VALUE/2 para evitar desbordamiento al sumar pesos.
    private static final int INF = Integer.MAX_VALUE / 2;

    private final int[] dist; // dist[v] = distancia mínima conocida desde src hasta v
    private final int[] prev; // prev[v] = predecesor de v en el camino más corto (o -1)
    private final int   src;  // índice del vértice origen
    private final int   V;    // número de vértices en el grafo

    /**
     * Ejecuta Dijkstra desde {@code src} sobre el grafo {@code g}.
     *
     * @param g   Grafo no dirigido ponderado con pesos no negativos.
     * @param src Índice del vértice origen.
     */
    public Dijkstra(Graph g, int src) {
        this.src = src;
        this.V   = g.numVertices();
        dist     = new int[V];
        prev     = new int[V];

        // ── Inicialización ────────────────────────────────────────────────────
        // Todas las distancias inician en INF (desconocidas) y todos los
        // predecesores en -1 (ninguno). La distancia al origen es 0.
        for (int i = 0; i < V; i++) { dist[i] = INF; prev[i] = -1; }
        dist[src] = 0;

        // ── Cola de prioridad ─────────────────────────────────────────────────
        // Insertamos solo el origen con clave 0; los demás se añaden con
        // decreaseKey conforme se "relajan" sus aristas.
        MinHeap<Integer> pq = new MinHeap<>();
        pq.insert(src, 0);

        // ── Ciclo principal ───────────────────────────────────────────────────
        while (!pq.isEmpty()) {
            // Extraemos el vértice no procesado con menor distancia tentativa
            int u = pq.extractMin();

            // Recorremos todos los vecinos de u
            for (Graph.AdjEntry e : g.adj(u)) {
                int v   = e.target;       // vértice vecino
                int alt = dist[u] + e.weight; // distancia alternativa pasando por u

                // RELAJACIÓN: si encontramos un camino más corto hacia v, lo actualizamos
                if (alt < dist[v]) {
                    dist[v] = alt;     // nueva distancia mínima a v
                    prev[v] = u;       // u es ahora el predecesor de v en el camino óptimo
                    pq.decreaseKey(v, alt); // actualizamos v en la cola de prioridad
                }
            }
        }
        // Al terminar, dist[v] contiene la distancia mínima de src a todo v alcanzable.
    }

    /**
     * Retorna la distancia mínima desde el origen hasta {@code dst}.
     *
     * @param dst Índice del vértice destino.
     * @return Distancia en metros, o {@code Integer.MAX_VALUE/2} si no es alcanzable.
     */
    public int distanceTo(int dst) { return dist[dst]; }

    /**
     * Indica si existe un camino desde el origen hasta {@code dst}.
     *
     * @param dst Índice del vértice destino.
     * @return {@code true} si el destino es alcanzable.
     */
    public boolean hasPath(int dst) { return dist[dst] < INF; }

    /**
     * Reconstruye el camino más corto hasta {@code dst} usando el arreglo {@code prev}.
     *
     * @param dst Índice del vértice destino.
     * @return Lista de índices desde el origen hasta el destino, o lista vacía si no hay camino.
     */
    public LinkedList<Integer> pathTo(int dst) {
        LinkedList<Integer> path = new LinkedList<>();
        if (!hasPath(dst)) return path; // sin camino → lista vacía

        // Reconstruimos el camino al revés siguiendo los predecesores
        LinkedList<Integer> reversed = new LinkedList<>();
        for (int v = dst; v != -1; v = prev[v]) {
            reversed.addFirst(v); // addFirst invierte el orden
        }
        return reversed;
    }

    /**
     * Retorna el arreglo completo de distancias mínimas desde el origen.
     *
     * @return Arreglo {@code dist[0..V-1]}.
     */
    public int[] getDistances() { return dist; }

    /**
     * Retorna el arreglo de predecesores para reconstrucción de caminos.
     *
     * @return Arreglo {@code prev[0..V-1]}.
     */
    public int[] getPrev() { return prev; }

    /**
     * Retorna el índice del vértice origen usado al construir este objeto.
     *
     * @return Índice del origen.
     */
    public int getSource() { return src; }

    /**
     * Genera un texto legible con el resultado: camino y distancia total.
     *
     * @param g   Grafo para obtener los identificadores de los vértices.
     * @param dst Índice del vértice destino.
     * @return Cadena con el camino y la distancia, o mensaje de error si no existe.
     */
    public String resultToString(Graph g, int dst) {
        if (!hasPath(dst)) {
            return "No hay camino de " + g.getVertex(src).getId()
                 + " a " + g.getVertex(dst).getId();
        }
        LinkedList<Integer> path = pathTo(dst);
        StringBuilder sb = new StringBuilder();
        sb.append("Camino más corto [")
          .append(g.getVertex(src).getId())
          .append(" → ")
          .append(g.getVertex(dst).getId())
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
