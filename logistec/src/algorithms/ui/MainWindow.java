package ui;

import algorithms.*;
import graph.*;
import io.*;
import planner.Planner;
import util.LinkedList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MainWindow extends JFrame {

    // ── State ─────────────────────────────────────────────────────────────

    private Graph               graph;
    private LinkedList<Parcel> packages;
    private LinkedList<Truck>   trucks;
    private FloydWarshall       fw;
    private Warshall            warshall;
    private Prim                prim;
    private Kruskal             kruskal;
    private Planner             planner;

    // ── UI components ─────────────────────────────────────────────────────

    private final GraphPanel  graphPanel  = new GraphPanel();
    private final JTextArea   reportArea  = new JTextArea();
    private final JTextField  srcField    = new JTextField(6);
    private final JTextField  dstField    = new JTextField(6);
    private final JTextArea   pathArea    = new JTextArea(4, 40);
    private final JLabel      statusLabel = new JLabel("Sin caso cargado.");

    // ── Constructor ───────────────────────────────────────────────────────

    public MainWindow() {
        super("LogísTEC — Sistema de Distribución Logística");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    // ── UI Builder ────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        root.setBackground(new Color(40, 40, 50));

        // ── Top toolbar ───────────────────────────────────────────────
        root.add(buildToolbar(), BorderLayout.NORTH);

        // ── Center: tabs (graph | report) ─────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(50, 50, 65));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("🗺  Grafo", graphPanel);

        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setBackground(new Color(25, 25, 35));
        reportArea.setForeground(new Color(200, 220, 200));
        reportArea.setEditable(false);
        tabs.addTab("📄 Reporte", new JScrollPane(reportArea));

        tabs.addTab("🔍 Camino mínimo", buildPathPanel());

        root.add(tabs, BorderLayout.CENTER);

        // ── Bottom status ─────────────────────────────────────────────
        statusLabel.setForeground(new Color(180, 220, 180));
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        root.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(new Color(30, 30, 45));

        JLabel logo = new JLabel("LogísTEC");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(new Color(100, 200, 120));
        bar.add(logo);

        JButton btnLoad = makeButton("Cargar JSON", new Color(60, 120, 200));
        btnLoad.addActionListener(e -> loadFile());
        bar.add(btnLoad);

        JButton btnRun = makeButton("Ejecutar Planificación", new Color(60, 160, 60));
        btnRun.addActionListener(e -> runPlanning());
        bar.add(btnRun);

        return bar;
    }

    private JPanel buildPathPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(30, 30, 40));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBackground(new Color(30, 30, 40));
        top.add(label("Origen:")); top.add(srcField);
        top.add(label("Destino:")); top.add(dstField);
        JButton btn = makeButton("Calcular (Dijkstra)", new Color(140, 80, 180));
        btn.addActionListener(e -> calcPath());
        top.add(btn);
        p.add(top, BorderLayout.NORTH);

        pathArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        pathArea.setBackground(new Color(25, 25, 35));
        pathArea.setForeground(Color.WHITE);
        pathArea.setEditable(false);
        p.add(new JScrollPane(pathArea), BorderLayout.CENTER);
        return p;
    }

    // ── Actions ───────────────────────────────────────────────────────────

    private void loadFile() {
        JFileChooser fc = new JFileChooser(".");
        fc.setDialogTitle("Seleccionar archivo JSON del caso");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            CityLoader loader = new CityLoader();
            loader.load(f.getAbsolutePath());
            graph    = loader.getGraph();
            packages = loader.getPackages();
            trucks   = loader.getTrucks();
            status("Caso cargado: " + f.getName()
                   + " (" + graph.numVertices() + "V, " + graph.numEdges() + "E, "
                   + packages.size() + " paquetes, " + trucks.size() + " camiones)");
            graphPanel.setData(graph, null, null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runPlanning() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this, "Primero cargue un archivo JSON.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            status("Ejecutando algoritmos…");

            // 1. Warshall
            warshall = new Warshall(graph);

            // 2. Floyd-Warshall
            fw = new FloydWarshall(graph);

            // 3. MST — Prim & Kruskal
            prim    = new Prim(graph);
            kruskal = new Kruskal(graph);

            // 4. Planner
            planner = new Planner(graph, fw, warshall);
            planner.validateReachability(packages);
            planner.assignPackages(packages, trucks);
            planner.planRoutes(trucks);

            // 5. Report
            ReportGenerator rg = new ReportGenerator(graph, packages, trucks, fw, warshall, prim, kruskal);
            reportArea.setText(rg.generate());

            // 6. Refresh graph panel
            graphPanel.setData(graph, trucks, prim);

            status("Planificación completada. Prim=" + prim.getTotalWeight()
                   + "m | Kruskal=" + kruskal.getTotalWeight() + "m");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error en planificación:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcPath() {
        if (graph == null || fw == null) {
            pathArea.setText("Primero cargue y ejecute la planificación.");
            return;
        }
        String srcId = srcField.getText().trim();
        String dstId = dstField.getText().trim();
        int src = graph.indexOf(srcId);
        int dst = graph.indexOf(dstId);
        if (src == -1) { pathArea.setText("Vértice origen '" + srcId + "' no encontrado."); return; }
        if (dst == -1) { pathArea.setText("Vértice destino '" + dstId + "' no encontrado."); return; }

        Dijkstra d = new Dijkstra(graph, src);
        pathArea.setText(d.resultToString(graph, dst));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }
}
