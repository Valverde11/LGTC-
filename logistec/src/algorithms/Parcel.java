package algorithms;

/**
* Representa un paquete de entrega en el sistema LogísTEC.
 *
 * <p>Cada paquete tiene un identificador único, un vértice de destino,
 * un peso en kilogramos y una prioridad de entrega. Su estado cambia a lo
 * largo del proceso: inicia como PENDING, luego se asigna a un camión
 * (ASSIGNED) o se rechaza por ser inalcanzable o sin capacidad (REJECTED).
 *
 * @author Alejandro Arias Lopez
 */
public class Parcel {

    /** Estados posibles de un paquete a lo largo del proceso de planificación. */
    public enum Status { PENDING, ASSIGNED, REJECTED }

    private final String id;
    private final String destinationId;
    private int          destinationIndex;
    private final int    weight;
    private final int    priority;
    private Status       status = Status.PENDING;
    private String       assignedTruck = null;

    /**
     * Crea un nuevo paquete con los datos proporcionados.
     *
     * @param id            Identificador único del paquete (por ejemplo, "P01").
     * @param destinationId Identificador del vértice de destino en el grafo.
     * @param weight        Peso del paquete en kilogramos (entero positivo).
     * @param priority      Prioridad de entrega: 1 (alta), 2 (media), 3 (baja).
     */
    public Parcel(String id, String destinationId, int weight, int priority) {
        this.id            = id;
        this.destinationId = destinationId;
        this.weight        = weight;
        this.priority      = priority;
    }

    /**
     * Retorna el identificador único del paquete.
     *
     * @return Identificador del paquete.
     */
    public String getId() { return id; }

    /**
     * Retorna el identificador del vértice de destino (como String del JSON).
     *
     * @return Identificador del vértice destino.
     */
    public String getDestinationId() { return destinationId; }

    /**
     * Retorna el índice interno del vértice de destino en el grafo.
     *
     * @return Índice del vértice destino, o -1 si aún no ha sido resuelto.
     */
    public int getDestinationIndex() { return destinationIndex; }

    /**
     * Asigna el índice interno del vértice de destino una vez resuelto por el {@code CityLoader}.
     *
     * @param i Índice del vértice de destino en el grafo.
     */
    public void setDestinationIndex(int i) { destinationIndex = i; }

    /**
     * Retorna el peso del paquete en kilogramos.
     *
     * @return Peso del paquete.
     */
    public int getWeight() { return weight; }

    /**
     * Retorna la prioridad de entrega del paquete.
     *
     * @return Prioridad: 1 = alta, 2 = media, 3 = baja.
     */
    public int getPriority() { return priority; }

    /**
     * Retorna el estado actual del paquete en el proceso de planificación.
     *
     * @return Estado del paquete: PENDING, ASSIGNED o REJECTED.
     */
    public Status getStatus() { return status; }

    /**
     * Retorna el identificador del camión al que fue asignado el paquete.
     *
     * @return Identificador del camión asignado, o {@code null} si no ha sido asignado.
     */
    public String getAssignedTruck() { return assignedTruck; }

    /**
     * Marca el paquete como asignado al camión identificado por {@code truckId}
     * y actualiza su estado a ASSIGNED.
     *
     * @param truckId Identificador del camión que transportará este paquete.
     */
    public void assign(String truckId) {
        this.assignedTruck = truckId;
        this.status = Status.ASSIGNED;
    }

    /**
     * Marca el paquete como rechazado y actualiza su estado a REJECTED.
     * Un paquete se rechaza si su destino es inalcanzable desde el depósito
     * o si ningún camión tiene capacidad suficiente para cargarlo.
     */
    public void reject() { this.status = Status.REJECTED; }

    @Override
    public String toString() {
        return "Parcel{id='" + id + "', dest='" + destinationId
             + "', weight=" + weight + "kg, priority=" + priority
             + ", status=" + status + "}";
    }
}
