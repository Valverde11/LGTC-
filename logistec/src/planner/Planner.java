package planner;

import algorithms.*;
import graph.*;
import util.LinkedList;

/**
 * Planificador central de LogísTEC: validación, asignación y ruteo.
 *
 * <p>Orquesta tres fases:
 * 1. Warshall detecta destinos inalcanzables.
 * 2. Best-Fit asigna paquetes a camiones por capacidad.
 * 3. Nearest Neighbor y MST-based generan rutas; se elige la mejor.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Planner {

    private final Graph         graph;    // mapa vial de la ciudad
    private final FloydWarshall fw;       // matriz de distancias mínimas precomputada
    private final Warshall      warshall; // matriz de alcanzabilidad precomputada
    private final int           depotIdx; // índice del depósito en el grafo

    /**
     * Crea el planificador con los componentes necesarios.
     *
     * @param graph    Grafo de la ciudad.
     * @param fw       Objeto Floyd-Warshall con distancias mínimas.
     * @param warshall Objeto Warshall con alcanzabilidad.
     */
    public Planner(Graph graph, FloydWarshall fw, Warshall warshall) {
        this.graph    = graph;
        this.fw       = fw;
        this.warshall = warshall;
        this.depotIdx = graph.getDepotIndex(); // guardamos el índice del depósito
    }

    // =========================================================================
    // FASE 1: VALIDACIÓN DE ALCANZABILIDAD
    // =========================================================================

    /**
     * Marca como REJECTED los paquetes cuyo destino no sea alcanzable desde el depósito.
     *
     * @param packages Lista de todos los paquetes.
     * @return Lista de paquetes rechazados por inalcanzabilidad.
     */
    public LinkedList<Parcel> validateReachability(LinkedList<Parcel> packages) {
        LinkedList<Parcel> unreachable = new LinkedList<>();

        for (Parcel p : packages) {
            // Consultamos la matriz de cierre transitivo: ¿puede llegar el depósito al destino?
            if (!warshall.canReach(depotIdx, p.getDestinationIndex())) {
                p.reject();                    // marcamos el paquete como rechazado
                unreachable.addLast(p);        // lo añadimos a la lista de rechazados
            }
        }
        return unreachable;
    }

    // =========================================================================
    // FASE 2: ASIGNACIÓN DE PAQUETES (Best-Fit)
    // =========================================================================

    /**
     * Asigna paquetes PENDING a camiones usando la heurística Best-Fit.
     *
     * <p>Orden de procesamiento: prioridad ASC (1 primero), luego peso DESC.
     * Para cada paquete: asignar al camión con mayor capacidad libre que lo acepte.
     *
     * @param packages Lista de paquetes.
     * @param trucks   Lista de camiones.
     * @return Paquetes rechazados por falta de capacidad.
     */
    public LinkedList<Parcel> assignPackages(LinkedList<Parcel> packages, LinkedList<Truck> trucks) {
        // Primero ordenamos los paquetes según la heurística
        Parcel[] arr = toSortedArray(packages);
        LinkedList<Parcel> rejected = new LinkedList<>();

        for (Parcel p : arr) {
            // Saltamos paquetes ya rechazados por inalcanzabilidad (Fase 1)
            if (p.getStatus() != Parcel.Status.PENDING) continue;

            // ── Best-Fit: buscamos el camión con más espacio libre que aún pueda cargarlo ──
            Truck best     = null;
            int   bestFree = -1; // capacidad libre del mejor candidato

            for (Truck t : trucks) {
                // canFit: verifica que el paquete no exceda la capacidad restante
                if (t.canFit(p) && t.getFreeCapacity() > bestFree) {
                    bestFree = t.getFreeCapacity();
                    best     = t;
                }
            }

            if (best != null) {
                best.addPackage(p); // asignamos el paquete al mejor camión
            } else {
                // Ningún camión puede cargarlo → rechazado por capacidad
                p.reject();
                rejected.addLast(p);
            }
        }
        return rejected;
    }

    // 
    // FASE 3: PLANIFICACIÓN DE RUTAS
    // 

    /**
     * Calcula la ruta de cada camión comparando NN y MST-based y eligiendo la mejor.
     * También expande la ruta por calles reales usando Floyd-Warshall.
     *
     * @param trucks Lista de camiones con paquetes asignados.
     */
    public void planRoutes(LinkedList<Truck> trucks) {
        for (Truck t : trucks) {
            // Solo planificamos si el camión tiene paquetes asignados
            if (!t.getPackages().isEmpty()) planRoutesForTruck(t);
        }
    }

    /**
     * Planifica la ruta de un camión específico.
     * Calcula ambas heurísticas, selecciona la mejor y construye la ruta expandida.
     *
     * @param truck Camión a planificar.
     */
    private void planRoutesForTruck(Truck truck) {
        // ── Recopilamos las paradas únicas del camión ─────────────────────────
        // Un destino puede aparecer en múltiples paquetes; usamos un flag para deduplicar.
        LinkedList<Integer> stopsRaw = new LinkedList<>();
        boolean[] seen = new boolean[graph.numVertices()];

        for (Parcel p : truck.getPackages()) {
            int idx = p.getDestinationIndex();
            if (!seen[idx]) {
                seen[idx] = true;
                stopsRaw.addLast(idx); // añadimos solo la primera ocurrencia
            }
        }

        // Convertimos a arreglo para facilitar el acceso por índice en las heurísticas
        int[] stops = new int[stopsRaw.size()];
        int   i     = 0;
        for (int s : stopsRaw) stops[i++] = s;

        // ── Calculamos ambas heurísticas y sus distancias ─────────────────────
        int[] routeNN  = nearestNeighbor(stops);
        int   distNN   = routeDistance(routeNN);

        int[] routeMST = mstBased(stops);
        int   distMST  = routeDistance(routeMST);

        // Guardamos los tiempos de ambas heurísticas para el reporte comparativo
        truck.setRouteDistanceNN(distNN);
        truck.setRouteDistanceMST(distMST);

        // ── Seleccionamos la heurística ganadora ──────────────────────────────
        int[] best = distNN <= distMST ? routeNN : routeMST;
        LinkedList<Integer> bestList = new LinkedList<>();
        for (int v : best) bestList.addLast(v);
        truck.setRoute(bestList); // ruta comprimida (solo paradas clave)

        // ── Construimos la ruta expandida (siguiendo calles reales) ──────────
        // Para cada par consecutivo de paradas, reconstruimos el camino real
        // de intersección en intersección usando Floyd-Warshall.
        LinkedList<Integer> expanded = new LinkedList<>();
        for (int s = 0; s < best.length - 1; s++) {
            int[] path = fw.reconstructPath(best[s], best[s + 1]);
            if (path == null) continue;
            // Añadimos todos los vértices del camino EXCEPTO el último,
            // para evitar duplicar el vértice de inicio del siguiente segmento
            for (int pi = 0; pi < path.length - 1; pi++) expanded.addLast(path[pi]);
        }
        expanded.addLast(best[best.length - 1]); // añadimos el vértice final (regreso al depósito)
        truck.setExpandedRoute(expanded);
    }

    // =========================================================================
    // HEURÍSTICA 1: NEAREST NEIGHBOR (Vecino más cercano) — O(n²)
    // =========================================================================

    /**
     * Desde el depósito, en cada paso visita la parada no visitada más cercana.
     * Simple y rápida pero no garantiza el óptimo.
     *
     * @param stops Paradas del camión (sin incluir el depósito).
     * @return Ruta completa: [depósito, parada1, ..., paradaN, depósito].
     */
    public int[] nearestNeighbor(int[] stops) {
        int n = stops.length;
        boolean[] visited = new boolean[n]; // visited[j] = parada j ya fue visitada
        int[]     route   = new int[n + 2]; // +2 para el depósito al inicio y al final

        route[0] = depotIdx; // la ruta siempre empieza en el depósito
        int cur  = depotIdx; // posición actual del camión

        for (int step = 0; step < n; step++) {
            int best     = -1;
            int bestDist = Integer.MAX_VALUE;

            // Buscamos la parada no visitada más cercana a la posición actual
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    int d = fw.dist(cur, stops[j]); // distancia según Floyd-Warshall
                    if (d < bestDist) {
                        bestDist = d;
                        best     = j;
                    }
                }
            }

            visited[best]    = true;      // marcamos la parada como visitada
            cur              = stops[best]; // avanzamos a la nueva posición
            route[step + 1]  = cur;        // registramos en la ruta
        }

        route[n + 1] = depotIdx; // la ruta siempre termina en el depósito
        return route;
    }

    // =========================================================================
    // HEURÍSTICA 2: MST-BASED (2-aproximación) — O(n² + n log n)
    // =========================================================================

    /**
     * Construye el MST del subgrafo {depósito + paradas} con distancias FW,
     * luego recorre el MST en DFS preorden para obtener el orden de visita.
     * Garantiza un resultado ≤ 2× el óptimo para distancias métricas.
     *
     * @param stops Paradas del camión (sin incluir el depósito).
     * @return Ruta completa: [depósito, parada1, ..., paradaN, depósito].
     */
    public int[] mstBased(int[] stops) {
        // ── Construimos el subgrafo completo sobre {depósito} ∪ paradas ───────
        int m    = stops.length + 1; // número de nodos en el subgrafo
        int[] subV = new int[m];      // subV[i] = índice real en el grafo principal
        subV[0] = depotIdx;
        for (int i = 0; i < stops.length; i++) subV[i + 1] = stops[i];

        Graph sub = new Graph(m);
        // Añadimos los vértices (sin datos de posición, solo para identificarlos)
        for (int i = 0; i < m; i++) sub.addVertex(i, graph.getVertex(subV[i]));

        // Añadimos todas las aristas posibles entre pares de vértices del subgrafo
        // con pesos iguales a las distancias mínimas de Floyd-Warshall.
        // Esto crea un grafo COMPLETO sobre las paradas: O(n²) aristas.
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++) {
                int d = fw.dist(subV[i], subV[j]);
                if (d < Integer.MAX_VALUE / 2)
                    sub.addEdge(i, j, d); // solo si hay camino entre ellos
            }
        }

        // ── Calculamos el MST del subgrafo completo con Kruskal ───────────────
        Kruskal kruskal  = new Kruskal(sub);
        Graph   mstGraph = kruskal.buildMSTGraph(m);

        // ── DFS preorden en el MST desde el depósito (índice 0 en el subgrafo) ─
        // El orden de visita del DFS preorden sobre el MST es la ruta del camión.
        boolean[]           vis      = new boolean[m];
        LinkedList<Integer> preorder = new LinkedList<>();
        GraphTraversal.dfsPreorder(mstGraph, 0, vis, preorder);

        // ── Traducimos índices del subgrafo a índices del grafo principal ─────
        int[] route = new int[m + 1]; // +1 para el regreso al depósito
        int   ri    = 0;
        for (int subIdx : preorder) {
            route[ri++] = subV[subIdx]; // convertimos índice local → índice real
        }
        route[ri] = depotIdx; // cerramos la ruta regresando al depósito

        return route;
    }

    // =========================================================================
    // AUXILIAR: DISTANCIA TOTAL DE UNA RUTA
    // =========================================================================

    /**
     * Suma las distancias mínimas (Floyd-Warshall) entre pares consecutivos de la ruta.
     *
     * @param route Arreglo de índices de vértices en orden de visita.
     * @return Distancia total en metros, o {@code Integer.MAX_VALUE/2} si algún tramo no existe.
     */
    public int routeDistance(int[] route) {
        int total = 0;
        for (int i = 0; i < route.length - 1; i++) {
            int d = fw.dist(route[i], route[i + 1]);
            if (d >= Integer.MAX_VALUE / 2) return Integer.MAX_VALUE / 2; // tramo inalcanzable
            total += d;
        }
        return total;
    }

    // =========================================================================
    // AUXILIAR: ORDENAMIENTO DE PAQUETES PARA BEST-FIT
    // =========================================================================

    /**
     * Copia los paquetes a un arreglo y los ordena con insertion sort
     * por prioridad ASC y luego por peso DESC.
     *
     * @param packages Lista de paquetes a ordenar.
     * @return Arreglo ordenado de paquetes.
     */
    private Parcel[] toSortedArray(LinkedList<Parcel> packages) {
        int     n   = packages.size();
        Parcel[] arr = new Parcel[n];
        int      i   = 0;
        for (Parcel p : packages) arr[i++] = p;

        // Insertion sort — O(n²) pero n es pequeño (número de paquetes)
        for (int j = 1; j < n; j++) {
            Parcel key = arr[j];
            int    k   = j - 1;
            // Desplazamos elementos mientras el actual tenga menor prioridad comparativa
            while (k >= 0 && comparePackages(arr[k], key) > 0) {
                arr[k + 1] = arr[k];
                k--;
            }
            arr[k + 1] = key;
        }
        return arr;
    }

    /**
     * Compara dos paquetes: primero por prioridad ASC (1 = más urgente),
     * luego por peso DESC (más pesado primero para mejor aprovechamiento del camión).
     *
     * @param a Primer paquete.
     * @param b Segundo paquete.
     * @return Negativo si a va antes, positivo si b va antes, 0 si son iguales.
     */
    private int comparePackages(Parcel a, Parcel b) {
        if (a.getPriority() != b.getPriority())
            return a.getPriority() - b.getPriority(); // prioridad 1 < 2 < 3 → ASC
        return b.getWeight() - a.getWeight();         // mayor peso primero → DESC
    }
}
