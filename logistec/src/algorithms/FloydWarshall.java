package algorithms;

import graph.Graph;

/**
 * Algoritmo de Floyd-Warshall para calcular caminos mínimos entre todos los pares de vértices.
 * Complejidad: O(V³) tiempo, O(V²) espacio.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class FloydWarshall {

    // Valor centinela: "sin camino directo". Usamos MAX_VALUE/2 para evitar
    // desbordamiento al sumar dos distancias (INF + INF causaría overflow).
    private static final int INF = Integer.MAX_VALUE / 2;

    private final int[][] dist; // dist[i][j] = distancia mínima de i a j tras el algoritmo
    private final int[][] next; // next[i][j] = primer paso en el camino mínimo de i a j
    private final int     V;    // número de vértices

    /**
     * Ejecuta Floyd-Warshall sobre el grafo {@code g}.
     *
     * @param g Grafo no dirigido ponderado.
     */
    public FloydWarshall(Graph g) {
        this.V    = g.numVertices();
        // Obtenemos la matriz de adyacencia como punto de partida:
        // dist[i][j] = peso(i,j) si hay arista directa, 0 si i==j, INF si no hay arista
        this.dist = g.adjacencyMatrix();
        this.next = new int[V][V];

        // Inicializamos la matriz de reconstrucción de caminos:
        // next[i][j] = j si hay arista directa de i a j (primer paso es ir directo)
        // next[i][j] = -1 si no hay arista (sin camino conocido aún)
        for (int i = 0; i < V; i++)
            for (int j = 0; j < V; j++)
                next[i][j] = (dist[i][j] < INF && i != j) ? j : -1;

        compute(); // ejecutamos el algoritmo principal
    }

    /**
     * Aplica la recurrencia de Floyd-Warshall in-place.
     *
     * <p>Recurrencia: D[i][j] = min(D[i][j],  D[i][k] + D[k][j])
     * para cada vértice intermedio k de 0 a V-1.
     *
     * <p>Nota: es seguro operar in-place porque en la iteración k solo
     * leemos dist[i][k] y dist[k][j], que no se modifican en esa misma pasada.
     */
    private void compute() {
        for (int k = 0; k < V; k++) {
            // k es el vértice intermedio candidato en esta iteración
            for (int i = 0; i < V; i++) {
                // Optimización: si no hay camino de i a k, ninguna fila de j mejora
                if (dist[i][k] == INF) continue;
                for (int j = 0; j < V; j++) {
                    // Optimización: si no hay camino de k a j, tampoco mejora
                    if (dist[k][j] == INF) continue;

                    // Calculamos la distancia pasando por k
                    // Usamos long para evitar overflow al sumar dos int grandes
                    long candidate = (long) dist[i][k] + dist[k][j];

                    if (candidate < dist[i][j]) {
                        // Encontramos un camino más corto de i a j pasando por k
                        dist[i][j] = (int) candidate;
                        // El primer paso de i a j ahora es el mismo que de i a k
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
    }

    /**
     * Retorna la distancia mínima de {@code i} a {@code j}.
     *
     * @param i Origen.
     * @param j Destino.
     * @return Distancia mínima en metros, o {@code INF} si no son alcanzables.
     */
    public int dist(int i, int j) { return dist[i][j]; }

    /**
     * Indica si {@code j} es alcanzable desde {@code i}.
     *
     * @param i Origen.
     * @param j Destino.
     * @return {@code true} si existe algún camino.
     */
    public boolean reachable(int i, int j) { return dist[i][j] < INF; }

    /**
     * Retorna la matriz completa de distancias mínimas V×V.
     *
     * @return Matriz {@code dist}.
     */
    public int[][] getDistMatrix() { return dist; }

    /**
     * Reconstruye el camino mínimo de {@code i} a {@code j} usando la matriz {@code next}.
     *
     * @param i Origen.
     * @param j Destino.
     * @return Arreglo de índices de vértices desde i hasta j, o {@code null} si no hay camino.
     */
    public int[] reconstructPath(int i, int j) {
        if (next[i][j] == -1 && i != j) return null; // sin camino

        // Primero contamos la longitud del camino para dimensionar el arreglo
        int count = 1; // comenzamos en 1 para incluir el nodo i
        int cur   = i;
        while (cur != j) {
            cur = next[cur][j]; // avanzamos al siguiente nodo en el camino
            count++;
        }

        // Ahora llenamos el arreglo con los índices del camino
        int[] path = new int[count];
        path[0] = i;
        cur = i;
        for (int p = 1; p < count; p++) {
            cur     = next[cur][j]; // siguiente nodo según la matriz next
            path[p] = cur;
        }
        return path;
    }

    /**
     * Genera una representación en texto de la matriz de distancias mínimas.
     *
     * @param g Grafo para obtener los ids de los vértices.
     * @return Cadena con la matriz formateada.
     */
    public String matrixToString(Graph g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Matriz de distancias mínimas Floyd-Warshall (metros):\n");

        // Encabezado de columnas
        sb.append("      ");
        for (int j = 0; j < V; j++)
            sb.append(String.format("%-6s", g.getVertex(j) != null ? g.getVertex(j).getId() : j));
        sb.append("\n");

        // Filas: cada vértice i como origen
        for (int i = 0; i < V; i++) {
            sb.append(String.format("%-4s  ", g.getVertex(i) != null ? g.getVertex(i).getId() : i));
            for (int j = 0; j < V; j++) {
                // Mostramos ∞ cuando no hay camino
                if (dist[i][j] >= INF) sb.append(String.format("%-6s", "∞"));
                else                   sb.append(String.format("%-6d", dist[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
