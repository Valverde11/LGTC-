package algorithms;


public class Parcel {

    public enum Status { PENDING, ASSIGNED, REJECTED }

    private final String id;
    private final String destinationId;   // vertex id
    private int    destinationIndex;      // resolved vertex index
    private final int    weight;          // kg
    private final int    priority;        // 1 (highest) … 3 (lowest)
    private Status status = Status.PENDING;
    private String assignedTruck = null;

    public Parcel(String id, String destinationId, int weight, int priority) {
        this.id            = id;
        this.destinationId = destinationId;
        this.weight        = weight;
        this.priority      = priority;
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public String getId()                { return id; }
    public String getDestinationId()     { return destinationId; }
    public int    getDestinationIndex()  { return destinationIndex; }
    public void   setDestinationIndex(int i) { destinationIndex = i; }
    public int    getWeight()            { return weight; }
    public int    getPriority()          { return priority; }
    public Status getStatus()            { return status; }
    public String getAssignedTruck()     { return assignedTruck; }

    public void assign(String truckId) {
        this.assignedTruck = truckId;
        this.status = Status.ASSIGNED;
    }

    public void reject() { this.status = Status.REJECTED; }

    @Override
    public String toString() {
        return "Parcel{id='" + id + "', dest='" + destinationId
             + "', weight=" + weight + "kg, priority=" + priority
             + ", status=" + status + "}";
    }
}