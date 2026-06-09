package graph;

import util.LinkedList;
import algorithms.Parcel;
/**
 * Representa un camión de reparto en la flota de LogísTEC.
 *
 * <p>Almacena la capacidad máxima de carga, los paquetes asignados,
 * y dos versiones de la ruta planificada:
 * <ul>
 *   <li>Ruta comprimida ({@code route}): solo los puntos de parada clave.</li>
 *   <li>Ruta expandida ({@code expandedRoute}): el camino completo siguiendo
 *       las calles reales, reconstruido con Floyd-Warshall.</li>
 * </ul>
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Truck {

    private final String id;
    private final int    capacity;
    private int          currentLoad;

    private final LinkedList<Parcel>  packages      = new LinkedList<>();
    private       LinkedList<Integer> route         = new LinkedList<>();
    private       LinkedList<Integer> expandedRoute = new LinkedList<>();
    private       int                 routeDistanceNN  = 0;
    private       int                 routeDistanceMST = 0;

    /**
     * Crea un nuevo camión con el identificador y la capacidad indicados.
     *
     * @param id       Identificador único del camión (por ejemplo, "C01").
     * @param capacity Capacidad máxima de carga en kilogramos.
     */
    public Truck(String id, int capacity) {
        this.id       = id;
        this.capacity = capacity;
    }

    /**
     * Retorna el identificador único del camión.
     *
     * @return Identificador del camión.
     */
    public String getId() { return id; }

    /**
     * Retorna la capacidad máxima de carga del camión en kilogramos.
     *
     * @return Capacidad máxima en kg.
     */
    public int getCapacity() { return capacity; }

    /**
     * Retorna la carga actual acumulada del camión en kilogramos.
     *
     * @return Carga actual en kg.
     */
    public int getCurrentLoad() { return currentLoad; }

    /**
     * Retorna la capacidad libre disponible del camión (capacidad - carga actual).
     *
     * @return Capacidad libre en kg.
     */
    public int getFreeCapacity() { return capacity - currentLoad; }

    /**
     * Retorna la lista de paquetes asignados a este camión.
     *
     * @return Lista de objetos {@link Parcel} asignados.
     */
    public LinkedList<Parcel> getPackages() { return packages; }

    /**
     * Retorna la ruta comprimida del camión: lista de índices de vértices de parada.
     *
     * @return Lista de índices de vértices en el orden de visita (depósito → paradas → depósito).
     */
    public LinkedList<Integer> getRoute() { return route; }

    /**
     * Retorna la ruta expandida del camión, que sigue las calles reales del grafo
     * reconstruidas mediante Floyd-Warshall entre cada par de paradas consecutivas.
     *
     * @return Lista de índices de vértices con el camino completo por calles reales.
     */
    public LinkedList<Integer> getExpandedRoute() { return expandedRoute; }

    /**
     * Retorna la distancia total de la ruta calculada con la heurística Nearest Neighbor.
     *
     * @return Distancia en metros de la ruta NN.
     */
    public int getRouteDistanceNN() { return routeDistanceNN; }

    /**
     * Retorna la distancia total de la ruta calculada con la heurística MST-based.
     *
     * @return Distancia en metros de la ruta MST-based.
     */
    public int getRouteDistanceMST() { return routeDistanceMST; }

    /**
     * Establece la distancia total de la ruta Nearest Neighbor.
     *
     * @param d Distancia en metros.
     */
    public void setRouteDistanceNN(int d) { routeDistanceNN = d; }

    /**
     * Establece la distancia total de la ruta MST-based.
     *
     * @param d Distancia en metros.
     */
    public void setRouteDistanceMST(int d) { routeDistanceMST = d; }

    /**
     * Asigna la ruta comprimida (solo paradas clave) al camión.
     *
     * @param r Lista de índices de vértices representando la ruta de paradas.
     */
    public void setRoute(LinkedList<Integer> r) { route = r; }

    /**
     * Asigna la ruta expandida (camino completo por calles reales) al camión.
     *
     * @param r Lista de índices de vértices con el recorrido real por el grafo.
     */
    public void setExpandedRoute(LinkedList<Integer> r) { expandedRoute = r; }

    /**
     * Intenta agregar el paquete {@code p} al camión si hay capacidad suficiente.
     * Si se agrega exitosamente, actualiza la carga actual y marca el paquete como asignado.
     *
     * @param p Paquete a agregar.
     * @return {@code true} si el paquete fue agregado, {@code false} si no cabe.
     */
    public boolean addPackage(Parcel p) {
        if (currentLoad + p.getWeight() > capacity) return false;
        packages.addLast(p);
        currentLoad += p.getWeight();
        p.assign(id);
        return true;
    }

    /**
     * Indica si el paquete {@code p} puede ser cargado sin exceder la capacidad máxima.
     *
     * @param p Paquete a verificar.
     * @return {@code true} si el paquete cabe en el camión.
     */
    public boolean canFit(Parcel p) {
        return currentLoad + p.getWeight() <= capacity;
    }

    /**
     * Calcula el porcentaje de ocupación actual del camión.
     *
     * @return Porcentaje de capacidad en uso (0.0 a 100.0).
     */
    public double occupancyPercent() {
        return 100.0 * currentLoad / capacity;
    }

    @Override
    public String toString() {
        return "Truck{id='" + id + "', capacity=" + capacity + "kg, load=" + currentLoad + "kg}";
    }
}
