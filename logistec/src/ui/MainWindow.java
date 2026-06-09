package ui;

import algorithms.*;
import graph.*;
import io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import planner.Planner;
import util.LinkedList;

/**
 * Ventana principal de la aplicación LogísTEC.
 *
 * <p>Implementa la interfaz gráfica completa con cuatro pestañas:
 * <ul>
 *   <li><b>Grafo</b>: visualización del mapa vial, MST y rutas.</li>
 *   <li><b>Reporte</b>: texto completo generado por {@link ReportGenerator}.</li>
 *   <li><b>Camino mínimo</b>: consulta interactiva de Dijkstra entre dos vértices.</li>
 *   <li><b>Paquetes</b>: búsqueda de información de un paquete por su ID.</li>
 * </ul>
 *
 * <p>El flujo de uso es: Cargar JSON → Ejecutar Planificación → explorar resultados.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class MainWindow extends JFrame {

    // ── Constantes de colores del tema oscuro ────────────────────────────────

    /** Color de fondo principal de la ventana (azul muy oscuro). */
    private static final Color BG       = new Color(15, 19, 28);

    /** Color de fondo de los paneles internos (ligeramente más claro que BG). */
    private static final Color PANEL_BG = new Color(22, 28, 40);

    /** Color de los bordes y separadores (azul grisáceo). */
    private static final Color BORDER_C = new Color(40, 55, 75);

    /** Color del texto principal (blanco azulado). */
    private static final Color TEXT_C   = new Color(200, 215, 235);

    /** Color del texto secundario/atenuado (gris azulado). */
    private static final Color MUTED_C  = new Color(100, 120, 145);

    // ── Estado del dominio ────────────────────────────────────────────────────

    /** Grafo de la ciudad. Se inicializa al cargar el JSON. */
    private Graph              graph;

    /** Lista de paquetes cargados. Se inicializa al cargar el JSON. */
    private LinkedList<Parcel> packages;

    /** Lista de camiones cargados. Se inicializa al cargar el JSON. */
    private LinkedList<Truck>  trucks;

    /** Resultado de Floyd-Warshall. Se inicializa al ejecutar la planificación. */
    private FloydWarshall      fw;

    /** Resultado de Warshall (cierre transitivo). Se inicializa al ejecutar. */
    private Warshall           warshall;

    /** Resultado de Prim (MST). Se inicializa al ejecutar la planificación. */
    private Prim               prim;

    /** Resultado de Kruskal (MST, para comparación de tiempos). */
    private Kruskal            kruskal;

    // ── Componentes UI ───────────────────────────────────────────────────────

    /** Panel de visualización del grafo. */
    private final GraphPanel  graphPanel      = new GraphPanel();

    /** Área de texto donde se muestra el reporte generado. */
    private final JTextArea   reportArea      = new JTextArea();

    /** Campo de texto para el vértice origen en la consulta de Dijkstra. */
    private final JTextField  srcField        = new JTextField(6);

    /** Campo de texto para el vértice destino en la consulta de Dijkstra. */
    private final JTextField  dstField        = new JTextField(6);

    /** Campo para ingresar el ID del paquete a consultar. */
    private final JTextField  packageIdField  = new JTextField(10);

    /** Área donde se muestra la información del paquete consultado. */
    private final JTextArea   packageInfoArea = new JTextArea(6, 50);

    /** Área donde se muestra el resultado de la consulta de Dijkstra. */
    private final JTextArea   pathArea        = new JTextArea(5, 50);

    /** Botón para limpiar el camino mínimo resaltado en el panel del grafo. */
    private JButton           btnClearPath;

    /** Etiqueta de estado en la barra inferior de la ventana. */
    private final JLabel      statusLabel     = new JLabel("Sin caso cargado.");

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Crea y muestra la ventana principal de LogísTEC.
     * Configura el tamaño, posición y construye todos los componentes de la UI.
     */
    public MainWindow() {
        super("LogisTEC - Sistema de Distribucion Logistica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null); // centrar en la pantalla
        buildUI();
        setVisible(true);
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    /**
     * Construye y ensambla todos los paneles de la ventana:
     * barra superior, pestañas centrales y barra de estado inferior.
     */
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildTabs(),      BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    /**
     * Construye la barra superior con el logo y los botones principales.
     *
     * @return Panel configurado listo para agregar al layout.
     */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        bar.setBackground(new Color(10, 14, 22));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));

        // Logo de la aplicación
        JLabel logo = new JLabel("LogisTEC");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(new Color(52, 211, 153)); // verde esmeralda
        bar.add(logo);
        bar.add(Box.createHorizontalStrut(10));

        // Botones principales: cargar y ejecutar
        JButton btnLoad = makeBtn("Cargar JSON",            new Color(37, 99, 235));
        JButton btnRun  = makeBtn("Ejecutar Planificacion", new Color(22, 163, 74));
        btnLoad.addActionListener(e -> loadFile());
        btnRun.addActionListener(e  -> runPlanning());
        bar.add(btnLoad);
        bar.add(btnRun);
        return bar;
    }

    /**
     * Construye el panel de pestañas central con las cuatro secciones principales.
     *
     * @return JTabbedPane configurado con las cuatro pestañas.
     */
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(PANEL_BG);
        tabs.setForeground(TEXT_C);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        styleTabPane(tabs);

        tabs.addTab("  Grafo  ",         buildGraphPanel());
        tabs.addTab("  Reporte  ",       buildReportPanel());
        tabs.addTab("  Camino minimo  ", buildPathPanel());
        tabs.addTab("  Paquetes  ",      buildPackagePanel());
        return tabs;
    }

    /**
     * Construye la barra de estado inferior que muestra mensajes informativos.
     *
     * @return Panel de la barra de estado.
     */
    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        statusBar.setBackground(new Color(10, 14, 22));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        statusLabel.setForeground(MUTED_C);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.add(statusLabel);
        return statusBar;
    }

    /**
     * Construye la pestaña "Grafo" que contiene el panel de visualización
     * y el botón para limpiar el camino mínimo resaltado.
     *
     * @return Panel contenedor del grafo.
     */
    private JPanel buildGraphPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Barra de herramientas superior de la pestaña
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(PANEL_BG);

        // Botón para limpiar el resaltado de Dijkstra, inicialmente oculto
        btnClearPath = makeBtn("Eliminar camino mínimo calculado", new Color(220, 38, 38));
        btnClearPath.addActionListener(e -> clearCalculatedPath());
        btnClearPath.setVisible(false); // solo visible cuando hay un camino resaltado
        top.add(btnClearPath);

        p.add(top, BorderLayout.NORTH);
        p.add(graphPanel, BorderLayout.CENTER); // el panel de dibujo ocupa el centro
        return p;
    }

    /**
     * Construye la pestaña "Reporte" con el área de texto del reporte generado.
     *
     * @return Panel contenedor del reporte.
     */
    private JPanel buildReportPanel() {
        // Configurar área de texto para mostrar texto monoespaciado sin edición
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setBackground(new Color(14, 18, 27));
        reportArea.setForeground(new Color(190, 210, 230));
        reportArea.setCaretColor(Color.WHITE);
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(14, 16, 14, 16));
        reportArea.setLineWrap(false); // sin salto de línea automático

        JScrollPane sp = new JScrollPane(reportArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        sp.getVerticalScrollBar().setBackground(PANEL_BG);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(sp);
        return p;
    }

    /**
     * Construye la pestaña "Camino mínimo" con campos de origen/destino,
     * botón de cálculo con Dijkstra y área para mostrar el resultado.
     *
     * @return Panel de consulta de camino mínimo.
     */
    private JPanel buildPathPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Panel superior con los controles de entrada
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setBackground(PANEL_BG);

        styleField(srcField);
        styleField(dstField);
        top.add(label("Origen:"));  top.add(srcField);
        top.add(label("Destino:")); top.add(dstField);

        JButton btn = makeBtn("Calcular (Dijkstra)", new Color(109, 40, 217));
        btn.addActionListener(e -> calcPath());
        top.add(btn);
        p.add(top, BorderLayout.NORTH);

        // Área de resultado del camino calculado
        pathArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        pathArea.setBackground(new Color(14, 18, 27));
        pathArea.setForeground(new Color(190, 210, 230));
        pathArea.setEditable(false);
        pathArea.setMargin(new Insets(12, 14, 12, 14));

        JScrollPane sp = new JScrollPane(pathArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    /**
     * Construye la pestaña "Paquetes" con campo de búsqueda por ID y área de resultado.
     *
     * @return Panel de búsqueda de paquetes.
     */
    private JPanel buildPackagePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setBackground(PANEL_BG);

        styleField(packageIdField);
        top.add(label("ID del paquete:"));
        top.add(packageIdField);

        JButton btn = makeBtn("Buscar paquete", new Color(109, 40, 217));
        btn.addActionListener(e -> lookupPackage());
        top.add(btn);
        p.add(top, BorderLayout.NORTH);

        // Área de información del paquete encontrado
        packageInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        packageInfoArea.setBackground(new Color(14, 18, 27));
        packageInfoArea.setForeground(new Color(190, 210, 230));
        packageInfoArea.setEditable(false);
        packageInfoArea.setMargin(new Insets(12, 14, 12, 14));
        packageInfoArea.setLineWrap(true);
        packageInfoArea.setWrapStyleWord(true);

        JScrollPane sp2 = new JScrollPane(packageInfoArea);
        sp2.setBorder(BorderFactory.createLineBorder(BORDER_C));
        p.add(sp2, BorderLayout.CENTER);
        return p;
    }

    // ── Acciones de los botones ───────────────────────────────────────────────

    /**
     * Abre un diálogo de selección de archivo JSON, lo carga con {@link CityLoader}
     * y actualiza el estado interno con el grafo, paquetes y camiones leídos.
     * Si ocurre un error, muestra un diálogo de error.
     */
    private void loadFile() {
        JFileChooser fc = new JFileChooser(".");
        fc.setDialogTitle("Seleccionar archivo JSON");

        // Si el usuario cancela el diálogo, no hacer nada
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            // Cargar y parsear el archivo JSON
            CityLoader loader = new CityLoader();
            loader.load(fc.getSelectedFile().getAbsolutePath());

            // Actualizar estado del dominio con los datos cargados
            graph    = loader.getGraph();
            packages = loader.getParcels();
            trucks   = loader.getTrucks();

            // Mostrar el grafo sin rutas ni MST (aún no se ha planificado)
            graphPanel.setData(graph, null, null);

            // Ocultar el botón de limpiar camino (no hay camino calculado)
            if (btnClearPath != null) btnClearPath.setVisible(false);

            // Mostrar resumen del caso en la barra de estado
            status("Caso cargado: " + fc.getSelectedFile().getName()
                   + "  (" + graph.numVertices() + "V, " + graph.numEdges() + "E, "
                   + packages.size() + " paquetes, " + trucks.size() + " camiones)");

        } catch (Exception ex) {
            error("Error al cargar: " + ex.getMessage());
        }
    }

    /**
     * Ejecuta todos los algoritmos del sistema en orden:
     * Warshall, Floyd-Warshall, Prim, Kruskal, asignación de paquetes y ruteo.
     * Actualiza el reporte y la visualización del grafo con los resultados.
     * Requiere que se haya cargado un caso JSON previamente.
     */
    private void runPlanning() {
        // Verificar que hay un caso cargado antes de ejecutar
        if (graph == null) { error("Primero cargue un archivo JSON."); return; }

        try {
            status("Ejecutando algoritmos...");

            // ── Paso 1: Warshall — cierre transitivo ────────────────────────
            warshall = new Warshall(graph);

            // ── Paso 2: Floyd-Warshall — distancias mínimas ─────────────────
            fw = new FloydWarshall(graph);

            // ── Paso 3: Prim y Kruskal — árbol de expansión mínima ──────────
            prim    = new Prim(graph);
            kruskal = new Kruskal(graph);

            // ── Paso 4: planificación logística ─────────────────────────────
            Planner planner = new Planner(graph, fw, warshall);
            planner.validateReachability(packages); // marcar paquetes inalcanzables
            planner.assignPackages(packages, trucks); // best-fit por capacidad
            planner.planRoutes(trucks);               // NN vs MST-based por camión

            // ── Paso 5: reporte ──────────────────────────────────────────────
            ReportGenerator rg = new ReportGenerator(
                    graph, packages, trucks, fw, warshall, prim, kruskal);
            reportArea.setText(rg.generate());
            reportArea.setCaretPosition(0); // scroll al inicio del reporte

            // ── Paso 6: actualizar visualización ────────────────────────────
            graphPanel.setData(graph, trucks, prim);
            if (btnClearPath != null) btnClearPath.setVisible(false);

            // Mostrar resultado de la comparación Prim vs Kruskal en la barra de estado
            status("Planificacion completada.  Prim=" + prim.getTotalWeight()
                   + "m  |  Kruskal=" + kruskal.getTotalWeight() + "m");

        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error: " + ex.getMessage());
        }
    }

    /**
     * Calcula el camino mínimo entre el vértice origen y destino ingresados,
     * usando el algoritmo de Dijkstra. Muestra el resultado en el área de texto
     * y resalta el camino en el panel del grafo.
     * Requiere que la planificación haya sido ejecutada previamente.
     */
    private void calcPath() {
        // Verificar que la planificación fue ejecutada (fw no es null)
        if (graph == null || fw == null) {
            pathArea.setText("Primero ejecute la planificacion.");
            return;
        }

        // Resolver los identificadores ingresados a índices de vértice
        int src = graph.indexOf(srcField.getText().trim());
        int dst = graph.indexOf(dstField.getText().trim());

        // Verificar que ambos vértices existen en el grafo
        if (src == -1) {
            pathArea.setText("Vertice origen no encontrado.");
            graphPanel.setData(graph, trucks, prim, null);
            return;
        }
        if (dst == -1) {
            pathArea.setText("Vertice destino no encontrado.");
            graphPanel.setData(graph, trucks, prim, null);
            return;
        }

        // Ejecutar Dijkstra desde el vértice origen
        Dijkstra d = new Dijkstra(graph, src);

        // Manejar el caso en que el destino no sea alcanzable
        if (!d.hasPath(dst)) {
            String message = "Es imposible llegar del punto "
                    + graph.getVertex(src).getId()
                    + " al " + graph.getVertex(dst).getId();
            pathArea.setText(message);
            graphPanel.setData(graph, trucks, prim, null);
            if (btnClearPath != null) btnClearPath.setVisible(false);
            status(message);
            return;
        }

        // Obtener el camino como lista de vértices y resaltarlo en el grafo
        util.LinkedList<Integer> path = d.pathTo(dst);
        graphPanel.setData(graph, trucks, prim, path);
        if (btnClearPath != null) btnClearPath.setVisible(true);

        // Mostrar resultado textual con la distancia total
        String result = d.resultToString(graph, dst);
        pathArea.setText(result + "\n\nPuedes visualizar el camino marcado de anaranjado"
                + " en el mapa (Sección \"Grafo\").");
        status("Puedes visualizar el camino marcado de anaranjado en el mapa.");
    }

    /**
     * Elimina el resaltado del camino mínimo del panel del grafo
     * y oculta el botón de limpiar.
     */
    private void clearCalculatedPath() {
        if (graph != null) {
            // Redibujar el grafo sin ningún camino resaltado (null)
            graphPanel.setData(graph, trucks, prim, null);
            if (btnClearPath != null) btnClearPath.setVisible(false);
            status("Camino mínimo calculado eliminado.");
        }
    }

    /**
     * Busca un paquete por su ID en la lista cargada y muestra su información:
     * ID, vértice de destino y camión asignado (o "Sin camión asignado").
     */
    private void lookupPackage() {
        // Verificar que hay paquetes cargados
        if (packages == null) {
            packageInfoArea.setText("Primero cargue un archivo JSON.");
            return;
        }

        // Obtener el ID ingresado por el usuario
        String id = packageIdField.getText().trim();
        if (id.isEmpty()) {
            packageInfoArea.setText("Ingrese el ID del paquete.");
            return;
        }

        // Buscar el paquete en la lista (comparación insensible a mayúsculas)
        Parcel found = null;
        for (Parcel p : packages) {
            if (p.getId().equalsIgnoreCase(id)) {
                found = p;
                break;
            }
        }

        // Mostrar resultado de la búsqueda
        if (found == null) {
            packageInfoArea.setText("Paquete no encontrado: " + id);
            return;
        }

        // Formatear la información del paquete encontrado
        StringBuilder sb = new StringBuilder();
        sb.append("ID del paquete: ").append(found.getId()).append("\n");
        sb.append("Destino: ").append(found.getDestinationId()).append("\n");
        sb.append("Camión asignado: ")
          .append(found.getAssignedTruck() == null
                  ? "Sin camión asignado"
                  : found.getAssignedTruck())
          .append("\n");
        packageInfoArea.setText(sb.toString());
    }

    // ── Métodos auxiliares de UI ──────────────────────────────────────────────

    /**
     * Aplica opciones visuales al JTabbedPane para que coincida con el tema oscuro.
     *
     * @param t El componente de pestañas a estilizar.
     */
    private void styleTabPane(JTabbedPane t) {
        t.setBorder(BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPane.selected",            PANEL_BG);
        UIManager.put("TabbedPane.background",          BG);
        UIManager.put("TabbedPane.foreground",          TEXT_C);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
    }

    /**
     * Crea un botón estilizado con el texto y color de fondo indicados.
     * Aplica el tema oscuro: texto blanco, sin borde pintado, cursor de mano.
     *
     * @param text Texto a mostrar en el botón.
     * @param bg   Color de fondo del botón.
     * @return Botón configurado y listo para usar.
     */
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(7, 18, 7, 18)); // padding interno
        return b;
    }

    /**
     * Crea un JLabel estilizado con el tema oscuro para usar en formularios.
     *
     * @param t Texto de la etiqueta.
     * @return JLabel configurado con el color y fuente del tema.
     */
    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_C);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    /**
     * Aplica el estilo del tema oscuro a un campo de texto (JTextField).
     * Fondo oscuro, texto claro, borde de color del tema y padding interno.
     *
     * @param f Campo de texto a estilizar.
     */
    private void styleField(JTextField f) {
        f.setBackground(new Color(25, 32, 48));
        f.setForeground(TEXT_C);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(4, 8, 4, 8)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    /**
     * Actualiza el mensaje en la barra de estado inferior.
     *
     * @param msg Mensaje a mostrar.
     */
    private void status(String msg) { statusLabel.setText(msg); }

    /**
     * Muestra un diálogo de error modal con el mensaje indicado.
     *
     * @param msg Mensaje de error a mostrar al usuario.
     */
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
