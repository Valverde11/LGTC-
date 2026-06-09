package io;

import graph.*;
import io.JsonParser.*;
import io.JsonParser.JsonArray;
import io.JsonParser.JsonObject;
import java.io.IOException;
import util.LinkedList;

/**
 * Cargador de configuración para el sistema LogísTEC.
 *
 * <p>Lee un archivo JSON con el esquema del proyecto y construye los objetos
 * de dominio: el grafo de la ciudad, la lista de paquetes y la flota de camiones.
 *
 * <p>Esquema JSON esperado:
 * <pre>
 * {
 *   "ciudad": {
 *     "vertices": [{"id":"A","tipo":"DEPOT","x":100,"y":100}, ...],
 *     "aristas":  [{"u":"A","v":"B","distancia":320}, ...]
 *   },
 *   "paquetes": [{"id":"P01","destino":"G","peso":5,"prioridad":1}, ...],
 *   "camiones": [{"id":"C01","capacidad":50}, ...]
 * }
 * </pre>
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class CityLoader {

    private Graph              graph;
    private LinkedList<Parcel> packages;
    private LinkedList<Truck>  trucks;

    /**
     * Lee y parsea el archivo JSON en la ruta indicada, construyendo todos
     * los objetos de dominio del sistema.
     *
     * @param filePath Ruta absoluta o relativa al archivo JSON de configuración.
     * @throws IOException      Si el archivo no existe o no se puede leer.
     * @throws RuntimeException Si el JSON tiene formato inválido o referencia vértices inexistentes.
     */
    public void load(String filePath) throws IOException {
        String json = JsonParser.readFile(filePath);
        JsonParser parser = new JsonParser(json);
        JsonObject root = parser.parseObject();

        JsonObject city = root.getObject("ciudad");
        JsonArray  vArr = city.getArray("vertices");
        JsonArray  eArr = city.getArray("aristas");

        int V = vArr.size();
        graph = new Graph(V);

        for (int i = 0; i < V; i++) {
            JsonObject vo = vArr.getObject(i);
            String id   = vo.getString("id");
            String tipo = vo.getString("tipo");
            int    x    = vo.getInt("x", 0);
            int    y    = vo.getInt("y", 0);

            Vertex.Type type = switch (tipo.toUpperCase()) {
                case "DEPOT"    -> Vertex.Type.DEPOT;
                case "DELIVERY" -> Vertex.Type.DELIVERY;
                default         -> Vertex.Type.INTERSECTION;
            };

            graph.addVertex(i, new Vertex(id, type, x, y));
        }

        for (int i = 0; i < eArr.size(); i++) {
            JsonObject eo = eArr.getObject(i);
            String uId = eo.getString("u");
            String vId = eo.getString("v");
            int    dist = eo.getInt("distancia");

            int u = graph.indexOf(uId);
            int v = graph.indexOf(vId);
            if (u == -1) throw new RuntimeException("Vértice desconocido: " + uId);
            if (v == -1) throw new RuntimeException("Vértice desconocido: " + vId);
            graph.addEdge(u, v, dist);
        }

        packages = new LinkedList<>();
        JsonArray pArr = root.getArray("paquetes");
        for (int i = 0; i < pArr.size(); i++) {
            JsonObject po = pArr.getObject(i);
            String pid   = po.getString("id");
            String dest  = po.getString("destino");
            int    peso  = po.getInt("peso");
            int    prior = po.getInt("prioridad");

            Parcel pkg = new Parcel(pid, dest, peso, prior);
            int destIdx = graph.indexOf(dest);
            if (destIdx == -1) throw new RuntimeException("Vértice destino desconocido: " + dest);
            pkg.setDestinationIndex(destIdx);
            packages.addLast(pkg);
        }

        trucks = new LinkedList<>();
        JsonArray tArr = root.getArray("camiones");
        for (int i = 0; i < tArr.size(); i++) {
            JsonObject to = tArr.getObject(i);
            trucks.addLast(new Truck(to.getString("id"), to.getInt("capacidad")));
        }
    }

    /**
     * Retorna el grafo de la ciudad construido tras la carga.
     *
     * @return Objeto {@link Graph} con los vértices y aristas del mapa vial.
     */
    public Graph getGraph() { return graph; }

    /**
     * Retorna la lista de paquetes cargados desde el JSON.
     *
     * @return Lista enlazada de objetos {@link Parcel}.
     */
    public LinkedList<Parcel> getParcels() { return packages; }

    /**
     * Retorna la lista de camiones cargados desde el JSON.
     *
     * @return Lista enlazada de objetos {@link Truck}.
     */
    public LinkedList<Truck> getTrucks() { return trucks; }
}
