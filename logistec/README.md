# LogísTEC — CE1103 Proyecto 2

Sistema de planificación logística sobre grafos no dirigidos ponderados.
Tecnológico de Costa Rica — Primer Semestre 2026.

## Requisitos
- Java 17 LTS o superior (probado con OpenJDK 21)
- Sistema operativo: Windows / Linux / macOS

## Compilar y ejecutar

```bash
# Compilar y crear el JAR
bash build.sh

# Modo GUI (interfaz gráfica)
bash build.sh run
# o bien
java -jar logistec.jar

# Modo texto (headless) con un archivo JSON
bash build.sh test
# o bien
java -jar logistec.jar data/caso_prueba.json
```

## Estructura del proyecto

```
logistec/
├── build.sh                          # Script de compilación
├── data/
│   └── caso_prueba.json              # Caso mínimo (31V, 54E, 15 paquetes, 3 camiones)
└── src/main/java/cr/ac/tec/ce1103/logistec/
    ├── Main.java                     # Punto de entrada
    ├── graph/
    │   ├── Vertex.java               # TDA Vértice
    │   ├── Edge.java                 # TDA Arista
    │   ├── Graph.java                # TDA Grafo (lista de adyacencia)
    │   ├── Parcel.java               # Modelo paquete
    │   └── Truck.java                # Modelo camión
    ├── algorithms/
    │   ├── GraphTraversal.java       # BFS y DFS
    │   ├── Warshall.java             # Cierre transitivo O(V³)
    │   ├── Dijkstra.java             # Camino mínimo O((V+E)logV)
    │   ├── FloydWarshall.java        # Todos los pares O(V³)
    │   ├── Prim.java                 # MST O((V+E)logV)
    │   └── Kruskal.java              # MST O(E log E)
    ├── planner/
    │   └── Planner.java              # Asignación (Best-Fit) + ruteo (NN, MST-based)
    ├── io/
    │   ├── JsonParser.java           # Parser JSON propio (sin librerías externas)
    │   ├── CityLoader.java           # Cargador de casos
    │   └── ReportGenerator.java      # Generador de reporte final
    └── ui/
        ├── MainWindow.java           # Ventana principal Swing
        └── GraphPanel.java           # Panel de visualización del grafo
```

## Formato del JSON de entrada

```json
{
  "ciudad": {
    "vertices": [
      {"id": "A", "tipo": "DEPOT",        "x": 100, "y": 100},
      {"id": "B", "tipo": "INTERSECCION", "x": 250, "y": 150},
      {"id": "C", "tipo": "DELIVERY",     "x": 400, "y": 200}
    ],
    "aristas": [
      {"u": "A", "v": "B", "distancia": 320},
      {"u": "B", "v": "C", "distancia": 180}
    ]
  },
  "paquetes": [
    {"id": "P01", "destino": "C", "peso": 5, "prioridad": 1}
  ],
  "camiones": [
    {"id": "C01", "capacidad": 50},
    {"id": "C02", "capacidad": 30}
  ]
}
```

### Tipos de vértice
| tipo           | descripción                      |
|----------------|----------------------------------|
| `DEPOT`        | Único depósito (punto de inicio) |
| `INTERSECCION` | Intersección vial                |
| `DELIVERY`     | Punto de entrega                 |

### Prioridades de paquete
| prioridad | significado      |
|-----------|------------------|
| 1         | Máxima prioridad |
| 2         | Media            |
| 3         | Mínima           |

## Algoritmos implementados

| Algoritmo       | Complejidad    | Uso                                  |
|-----------------|----------------|--------------------------------------|
| BFS             | O(V + E)       | Componentes conexas, recorrido       |
| DFS             | O(V + E)       | Pre-orden MST, detección ciclos      |
| Warshall        | O(V³)          | Cierre transitivo / alcanzabilidad   |
| Dijkstra        | O((V+E) log V) | Camino mínimo desde un origen        |
| Floyd-Warshall  | O(V³)          | Todos los pares de distancias        |
| Prim            | O((V+E) log V) | Árbol de expansión mínima            |
| Kruskal         | O(E log E)     | Árbol de expansión mínima            |
| Nearest Neighbor| O(n²)          | Heurística ruteo TSP                 |
| MST-based       | O(n² + n log n)| Heurística ruteo 2-aproximación TSP  |

## Estructuras de datos implementadas

| Estructura     | Clase          | Complejidad clave                    |
|----------------|----------------|--------------------------------------|
| Lista enlazada | `LinkedList`   | addLast O(1), get O(n)               |
| Cola FIFO      | `Queue`        | enqueue/dequeue O(1)                 |
| Pila LIFO      | `Stack`        | push/pop O(1)                        |
| Cola prioridad | `MinHeap`      | insert/extractMin O(log n)           |
| Union-Find     | `UnionFind`    | find/union O(α(n)) ≈ O(1) amortizado |

## Notas de implementación

- **Sin java.util en el núcleo algorítmico**: no se usa ArrayList, LinkedList, PriorityQueue,
  HashMap, HashSet ni similares. Solo `String`, arreglos primitivos y la API de E/S.
- **Parser JSON propio**: `JsonParser` implementa un parser recursivo descendente completo,
  sin dependencias externas.
- La clase de paquete se llama `Parcel` (en vez de `Package`) para evitar conflicto con
  `java.lang.Package`.
