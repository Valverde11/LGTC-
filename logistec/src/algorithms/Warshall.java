package algorithms;

import graph.Graph;

/**
 * Algoritmo de Warshall para el cierre transitivo de un grafo.
 * Determina alcanzabilidad entre todos los pares de vértices en O(V³).
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Warshall {

    private final boolean[][] reach; // reach[i][j] = true si j es alcanzable desde i
    private final int         V;     // número de vértices

    /**
     * Ejecuta Warshall sobre el grafo {@code g}.
     *
     * @param g Grafo a analizar.
     */
    public Warshall(Graph g) {
        this.V     = g.numVertices();
        // Partimos de la matriz booleana de adyacencia directa:
        // reach[i][j] = true si i==j o hay arista directa i→j
        this.reach = g.booleanMatrix();
        compute();
    }

    /**
     * Construye el cierre transitivo desde una matriz booleana inicial.
     *
     * @param initialMatrix Matriz booleana V×V de adyacencia directa.
     */
    public Warshall(boolean[][] initialMatrix) {
        this.V     = initialMatrix.length;
        this.reach = new boolean[V][V];
        // Copiamos para no modificar la matriz original
        for (int i = 0; i < V; i++)
            System.arraycopy(initialMatrix[i], 0, reach[i], 0, V);
        compute();
    }

    /**
     * Aplica la recurrencia de Warshall in-place:
     * P[i][j] = P[i][j] OR (P[i][k] AND P[k][j])
     *
     * <p>Interpretación: "puedo ir de i a j si ya podía antes, O si ahora
     * puedo pasar por el vértice intermedio k".
     *
     * <p>Al terminar, reach[i][j] es true si y solo si existe algún camino
     * (de cualquier longitud) de i a j.
     */
    private void compute() {
        // k = vértice intermedio candidato en esta iteración
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                // Optimización: si no se puede llegar de i a k, ningún j mejora en esta fila
                if (!reach[i][k]) continue;
                for (int j = 0; j < V; j++) {
                    // Si ya era alcanzable O si podemos ir i→k→j
                    reach[i][j] = reach[i][j] || reach[k][j];
                    // (reach[i][k] ya es true por el if anterior, así que lo omitimos)
                }
            }
        }
    }

    /**
     * Indica si {@code dst} es alcanzable desde {@code src}.
     *
     * @param src Origen.
     * @param dst Destino.
     * @return {@code true} si existe algún camino de src a dst.
     */
    public boolean canReach(int src, int dst) { return reach[src][dst]; }

    /**
     * Retorna la matriz de alcanzabilidad V×V completa.
     *
     * @return Matriz booleana {@code reach}.
     */
    public boolean[][] getMatrix() { return reach; }

    /**
     * Genera una representación en texto de la matriz de alcanzabilidad.
     * T = alcanzable, . = no alcanzable.
     *
     * @param g Grafo para obtener los ids de los vértices.
     * @return Cadena con la matriz formateada.
     */
    public String matrixToString(Graph g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Matriz de cierre transitivo Warshall (T=alcanzable, .=no):\n");

        // Encabezado de columnas
        sb.append("     ");
        for (int j = 0; j < V; j++)
            sb.append(String.format("%-4s", g.getVertex(j) != null ? g.getVertex(j).getId() : j));
        sb.append("\n");

        // Filas
        for (int i = 0; i < V; i++) {
            sb.append(String.format("%-4s ", g.getVertex(i) != null ? g.getVertex(i).getId() : i));
            for (int j = 0; j < V; j++)
                sb.append(reach[i][j] ? "T   " : ".   "); // T o punto para legibilidad
            sb.append("\n");
        }
        return sb.toString();
    }
}
