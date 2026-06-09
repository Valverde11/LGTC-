package graph;

/**
 * Representa un vértice (intersección o punto de interés) en el grafo de la ciudad.
 *
 * <p>Cada vértice tiene un tipo que determina su rol en el sistema:
 * DEPOT para el depósito de LogísTEC, DELIVERY para los puntos de entrega,
 * e INTERSECTION para las intersecciones viales normales.
 * Las coordenadas {@code x} e {@code y} se usan para la visualización gráfica.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Vertex {

    /** Tipos de vértice posibles en el mapa de la ciudad. */
    public enum Type { DEPOT, INTERSECTION, DELIVERY }

    private final String id;
    private final Type   type;
    private final int    x;
    private final int    y;

    /**
     * Crea un nuevo vértice con los datos proporcionados.
     *
     * @param id   Identificador único del vértice (por ejemplo, "A", "DEPOT", "B2").
     * @param type Tipo del vértice: DEPOT, INTERSECTION o DELIVERY.
     * @param x    Coordenada horizontal en píxeles para la visualización gráfica.
     * @param y    Coordenada vertical en píxeles para la visualización gráfica.
     */
    public Vertex(String id, Type type, int x, int y) {
        this.id   = id;
        this.type = type;
        this.x    = x;
        this.y    = y;
    }

    /**
     * Retorna el identificador único del vértice.
     *
     * @return Identificador del vértice.
     */
    public String getId() { return id; }

    /**
     * Retorna el tipo del vértice.
     *
     * @return Tipo del vértice: DEPOT, INTERSECTION o DELIVERY.
     */
    public Type getType() { return type; }

    /**
     * Retorna la coordenada horizontal del vértice para la visualización.
     *
     * @return Coordenada x en píxeles.
     */
    public int getX() { return x; }

    /**
     * Retorna la coordenada vertical del vértice para la visualización.
     *
     * @return Coordenada y en píxeles.
     */
    public int getY() { return y; }

    /**
     * Indica si este vértice es el depósito (tipo DEPOT).
     *
     * @return {@code true} si el tipo es DEPOT.
     */
    public boolean isDepot() { return type == Type.DEPOT; }

    /**
     * Indica si este vértice es un punto de entrega (tipo DELIVERY).
     *
     * @return {@code true} si el tipo es DELIVERY.
     */
    public boolean isDelivery() { return type == Type.DELIVERY; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex v)) return false;
        return id.equals(v.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Vertex{id='" + id + "', type=" + type + ", (" + x + "," + y + ")}";
    }
}
