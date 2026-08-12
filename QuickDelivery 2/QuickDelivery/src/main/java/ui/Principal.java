package ui;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Paquete;
import model.Usuario;
import service.GestorIncidencias;
import service.GestorLogin;
import service.GestorPaquetes;
import service.GestorUsuarios;
import service.GestorVehiculos;
/**
 *
 * @author ssanc
 */
public class Principal extends javax.swing.JFrame {

    // Cada cuanto se refresca el monitor, en milisegundos
    private static final int SEGUNDOS_REFRESCO = 5000;

    private DefaultTableModel TableModel;
    private GestorPaquetes gestorPaquetes;
    private GestorVehiculos gestorVehiculos;
    private GestorUsuarios gestorUsuarios;
    private GestorLogin gestorLogin;
    private GestorIncidencias gestorIncidencias;

    // Usuario que tiene la sesion abierta
    private Usuario usuarioSesion;

    // Mientras este en true el hilo del monitor sigue refrescando la tabla
    private boolean monitorActivo = true;


    //Constructor
    public Principal(Usuario usuarioSesion, GestorUsuarios gestorUsuarios, GestorVehiculos gestorVehiculos, GestorPaquetes gestorPaquetes) {
        initComponents();

        this.usuarioSesion = usuarioSesion;
        this.gestorUsuarios = gestorUsuarios;
        this.gestorVehiculos = gestorVehiculos;
        this.gestorPaquetes = gestorPaquetes;
        this.gestorLogin = new GestorLogin();
        this.gestorIncidencias = new GestorIncidencias();

        setTitle("Sistema de Logística - Monitor  [" + usuarioSesion.getUsuario()
                + " - " + usuarioSesion.getrol() + "]");
        setLocationRelativeTo(null);
        setResizable(false);

        //Habilita las opciones que le corresponden al rol
        aplicarPermisos();

        //Carga y actualiza la tabla
        cargarTabla();
        actualizarTabla();

        //Deja el monitor refrescandose solo
        iniciarMonitor();
    }


    /*
     * RF-02: cada rol ve unicamente las funciones que le tocan.
     * El administrador maneja la flota, el despachador maneja los paquetes y
     * el conductor solo consulta el monitor.
     */
    private void aplicarPermisos() {

        String rol = usuarioSesion.getrol();

        boolean administrador = rol.equals("Administrador");
        boolean despachador = rol.equals("Despachador");

        btnRegistrarVehiculo.setEnabled(administrador);

        btnRegistrarPaquete.setEnabled(administrador || despachador);
        btnEditarPaquete.setEnabled(administrador || despachador);
        btnEliminarPaquete.setEnabled(administrador || despachador);

        /*
         * Registrar incidencia queda habilitado para los tres roles. La HU-07
         * dice que el conductor las reporta, y el despachador tambien las
         * anota cuando el conductor avisa por telefono.
         */
        btnRegistrarIncidencia.setEnabled(true);

        /*
         * Enviar ubicacion es solo del conductor (RF-12). Es el unico que anda
         * en la calle y el unico que tiene vehiculos asignados.
         */
        btnEnviarUbicacion.setEnabled(rol.equals("Conductor"));
    }


    //Carga las columnas de la tabla
    private void cargarTabla() {

        String columnas[] = {
            "ID",
            "Destinatario",
            "Ruta",
            "Peso kg",
            "Estado",
            "Vehículo"
        };

        /*
         * El modelo no permite editar las celdas. La tabla es solo de consulta:
         * los cambios se hacen con los botones de registrar, editar y eliminar.
         */
        TableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tblPaquetes.setModel(TableModel);
        tblPaquetes.setRowHeight(22);
        tblPaquetes.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        //Ancho de cada columna, para que la direccion no salga cortada
        tblPaquetes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblPaquetes.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblPaquetes.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblPaquetes.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblPaquetes.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblPaquetes.getColumnModel().getColumn(5).setPreferredWidth(150);

    }

    //Actualiza los datos de la tabla
    public void actualizarTabla() {

        try {

            llenarTabla(gestorPaquetes.obtenerPaquetes());

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudieron consultar los paquetes.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Refresco que usa el hilo del monitor. Si la base falla no levanta
     * ventanas de error, solo lo deja anotado en la consola, porque estaria
     * saliendo un mensaje cada cinco segundos.
     */
    private void actualizarTablaSilencioso() {

        try {

            llenarTabla(gestorPaquetes.obtenerPaquetes());

        } catch (SQLException ex) {
            System.out.println("Monitor: no se pudo refrescar. " + ex.getLocalizedMessage());
        }
    }

    private void llenarTabla(ArrayList<Paquete> paquetes) {

        TableModel.setRowCount(0);

        for (Paquete paquete : paquetes) {

            String asignacion = "Sin asignar";

            if (paquete.getVehiculo() != null) {
                asignacion = paquete.getVehiculo().getPlaca()
                        + " (" + paquete.getVehiculo().getTipo() + ")";
            }

            TableModel.addRow(new Object[]{
                paquete.getId(),
                paquete.getDestinatario(),
                paquete.getDireccion(),
                paquete.getPeso(),
                paquete.getEstado(),
                asignacion
            });
        }

        actualizarEstadisticas(paquetes);
        actualizarAlertas(paquetes);
    }

    /*
     * Hilo que mantiene el monitor al dia. Mientras el despachador tiene la
     * ventana abierta, los cambios que reportan los conductores desde el
     * servidor van apareciendo solos en la tabla.
     */
    private void iniciarMonitor() {

        Thread hiloMonitor = new Thread(new Runnable() {
            @Override
            public void run() {

                while (monitorActivo) {

                    try {
                        Thread.sleep(SEGUNDOS_REFRESCO);
                    } catch (InterruptedException ex) {
                        System.out.println("Monitor interrumpido: " + ex.getLocalizedMessage());
                    }

                    if (monitorActivo) {
                        actualizarTablaSilencioso();
                    }
                }
            }
        });

        hiloMonitor.start();
    }

    public GestorPaquetes getGestorPaquetes() {
        return gestorPaquetes;
    }

    public GestorVehiculos getGestorVehiculos() {
        return gestorVehiculos;
    }

    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    private void actualizarEstadisticas(ArrayList<Paquete> paquetes) {

        int pendientes = 0;
        int enRuta = 0;
        int entregados = 0;

        for (Paquete paquete : paquetes) {

            switch (paquete.getEstado()) {

                case PENDIENTE:
                    pendientes++;
                    break;

                case EN_TRANSITO:
                    enRuta++;
                    break;

                case ENTREGADO:
                    entregados++;
                    break;
            }
        }

        txtPendientes.setText(String.valueOf(pendientes));
        txtEnRuta.setText(String.valueOf(enRuta));
        txtEntregadosHoy.setText(String.valueOf(entregados));

        actualizarResumen(paquetes.size(), entregados);
    }

    /*
     * Llena el panel de estadisticas rapidas. Antes ese panel solo decia
     * "proximamente", ahora muestra el tamaño de la flota, la cantidad de
     * incidencias y el porcentaje de paquetes ya entregados.
     */
    private void actualizarResumen(int totalPaquetes, int entregados) {

        lblValPaquetes.setText(String.valueOf(totalPaquetes));

        int porcentaje = 0;

        if (totalPaquetes > 0) {
            porcentaje = entregados * 100 / totalPaquetes;
        }

        lblValAvance.setText(porcentaje + " %");

        try {

            lblValFlota.setText(String.valueOf(gestorVehiculos.obtenerVehiculos().size()));
            lblValIncidencias.setText(String.valueOf(gestorIncidencias.obtenerIncidencias().size()));

        } catch (SQLException ex) {
            System.out.println("No se pudo calcular el resumen: " + ex.getLocalizedMessage());
        }
    }

    /*
     * Llena el area de alertas criticas con los paquetes sin vehiculo y con
     * las incidencias que reportaron los conductores.
     */
    private void actualizarAlertas(ArrayList<Paquete> paquetes) {

        StringBuilder alertas = new StringBuilder();

        for (Paquete paquete : paquetes) {
            if (paquete.getVehiculo() == null) {
                alertas.append("Paquete ").append(paquete.getId())
                       .append(" sin vehiculo asignado\n");
            }
        }

        try {

            for (String incidencia : gestorIncidencias.obtenerIncidencias()) {
                alertas.append(incidencia).append("\n");
            }

        } catch (SQLException ex) {
            System.out.println("No se pudieron consultar las incidencias: " + ex.getLocalizedMessage());
        }

        txtAlertasCriticas.setText(alertas.toString());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblLogo = new javax.swing.JLabel();
        lblEntregadosHoy = new javax.swing.JLabel();
        lblEnRuta = new javax.swing.JLabel();
        lblPendientes = new javax.swing.JLabel();
        txtEntregadosHoy = new javax.swing.JTextField();
        txtEnRuta = new javax.swing.JTextField();
        txtPendientes = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPaquetes = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAlertasCriticas = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        lblTitPaquetes = new javax.swing.JLabel();
        lblValPaquetes = new javax.swing.JLabel();
        lblTitFlota = new javax.swing.JLabel();
        lblValFlota = new javax.swing.JLabel();
        lblTitIncidencias = new javax.swing.JLabel();
        lblValIncidencias = new javax.swing.JLabel();
        lblTitAvance = new javax.swing.JLabel();
        lblValAvance = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        btnRegistrarVehiculo = new javax.swing.JButton();
        btnRegistrarPaquete = new javax.swing.JButton();
        btnEditarPaquete = new javax.swing.JButton();
        btnRegistrarIncidencia = new javax.swing.JButton();
        btnEnviarUbicacion = new javax.swing.JButton();
        btnEliminarPaquete = new javax.swing.JButton();
        btnVerAlertas = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        btnSalir = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        miAcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Sistema de Logística - Monitor"));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setText("Monitor Principal de Logística");

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/QuickDeliveryLogo.png"))); // NOI18N

        lblEntregadosHoy.setText("Entregados Hoy:");

        lblEnRuta.setText("En ruta:");

        lblPendientes.setText("Pendientes:");

        tblPaquetes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID Envio", "Ruta", "Estado", "Asignacion"
            }
        ));
        jScrollPane1.setViewportView(tblPaquetes);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Alertas Críticas"));

        txtAlertasCriticas.setEditable(false);
        txtAlertasCriticas.setColumns(20);
        txtAlertasCriticas.setLineWrap(true);
        txtAlertasCriticas.setRows(5);
        txtAlertasCriticas.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtAlertasCriticas);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Estadísticas Rápidas"));

        lblTitPaquetes.setText("Paquetes:");

        lblValPaquetes.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblValPaquetes.setText("0");

        lblTitFlota.setText("Vehículos:");

        lblValFlota.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblValFlota.setText("0");

        lblTitIncidencias.setText("Incidencias:");

        lblValIncidencias.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblValIncidencias.setText("0");

        lblTitAvance.setText("Entregado:");

        lblValAvance.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblValAvance.setText("0 %");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitPaquetes)
                    .addComponent(lblTitFlota)
                    .addComponent(lblTitIncidencias)
                    .addComponent(lblTitAvance))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblValPaquetes, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValFlota, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValIncidencias, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValAvance, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitPaquetes)
                    .addComponent(lblValPaquetes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitFlota)
                    .addComponent(lblValFlota))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitIncidencias)
                    .addComponent(lblValIncidencias))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitAvance)
                    .addComponent(lblValAvance))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btnRegistrarVehiculo.setText("Registrar Vehículo");
        btnRegistrarVehiculo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarVehiculoActionPerformed(evt);
            }
        });
        jToolBar1.add(btnRegistrarVehiculo);

        btnRegistrarPaquete.setText("Registrar Paquete");
        btnRegistrarPaquete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarPaqueteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnRegistrarPaquete);

        btnEditarPaquete.setText("Editar Paquete");
        btnEditarPaquete.setFocusable(false);
        btnEditarPaquete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEditarPaquete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEditarPaquete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarPaqueteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEditarPaquete);

        btnRegistrarIncidencia.setText("Registrar Incidencia");
        btnRegistrarIncidencia.setFocusable(false);
        btnRegistrarIncidencia.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRegistrarIncidencia.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRegistrarIncidencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarIncidenciaActionPerformed(evt);
            }
        });
        jToolBar1.add(btnRegistrarIncidencia);

        btnEnviarUbicacion.setText("Enviar Ubicación");
        btnEnviarUbicacion.setFocusable(false);
        btnEnviarUbicacion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEnviarUbicacion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEnviarUbicacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarUbicacionActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEnviarUbicacion);

        btnEliminarPaquete.setText("Eliminar Paquete");
        btnEliminarPaquete.setFocusable(false);
        btnEliminarPaquete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEliminarPaquete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEliminarPaquete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPaqueteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEliminarPaquete);

        btnVerAlertas.setText("Ver Alertas");
        btnVerAlertas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerAlertasActionPerformed(evt);
            }
        });
        jToolBar1.add(btnVerAlertas);

        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCerrarSesion);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(160, 160, 160)
                        .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblEntregadosHoy)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtEntregadosHoy, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblEnRuta)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtEnRuta, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblPendientes)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPendientes, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblTitulo)
                    .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEntregadosHoy)
                    .addComponent(lblEnRuta)
                    .addComponent(lblPendientes)
                    .addComponent(txtEntregadosHoy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEnRuta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPendientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jMenu1.setText("Archivo");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });
        jMenu1.add(btnSalir);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Ayuda");

        miAcercaDe.setText("Acerca de...");
        miAcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAcercaDeActionPerformed(evt);
            }
        });
        jMenu2.add(miAcercaDe);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Cierra la sesion
    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed

        //Detiene el hilo del monitor antes de soltar la ventana
        monitorActivo = false;

        //Deja la salida registrada en la auditoria
        gestorLogin.cerrarSesion(usuarioSesion);

        Login login = new Login();
        login.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    //Abre la ventana del registro del vehiculo
    private void btnRegistrarVehiculoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarVehiculoActionPerformed
        VehiculoDialog dialog = new VehiculoDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnRegistrarVehiculoActionPerformed

    //Muestra las alertas
    private void btnVerAlertasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerAlertasActionPerformed

        try {

            StringBuilder alertas = new StringBuilder();

            for (Paquete paquete : gestorPaquetes.obtenerPaquetes()) {

                if (paquete.getVehiculo() == null) {
                    alertas.append("- El paquete ")
                            .append(paquete.getId())
                            .append(" no tiene vehículo asignado.\n");
                }

                if (paquete.getEstado() == model.EstadoPaquete.PENDIENTE) {
                    alertas.append("- El paquete ")
                            .append(paquete.getId())
                            .append(" está pendiente de entrega.\n");
                }
            }

            //Incidencias que reportaron los conductores desde el cliente vehiculo
            for (String incidencia : gestorIncidencias.obtenerIncidencias()) {
                alertas.append("- Incidencia: ").append(incidencia).append("\n");
            }

            if (alertas.length() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No existen alertas.");
            } else {
                JOptionPane.showMessageDialog(this,
                        alertas.toString(),
                        "Alertas",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudieron consultar las alertas.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_btnVerAlertasActionPerformed

    //Abre la ventana del registro de paquetes
    private void btnRegistrarPaqueteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarPaqueteActionPerformed
        PaqueteDialog dialog = new PaqueteDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnRegistrarPaqueteActionPerformed

    //Abre la ventana de "Acerca de..."
    private void miAcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAcercaDeActionPerformed
        AcercaDialog dialog = new AcercaDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_miAcercaDeActionPerformed

    
    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed

    }//GEN-LAST:event_jMenu1ActionPerformed

    //Cierra la aplicacion
    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        int opcion = JOptionPane.showConfirmDialog(this, "Desa salir del sistema?", "Salir", JOptionPane.YES_NO_OPTION);
        if(opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
            
        }
    }//GEN-LAST:event_btnSalirActionPerformed
    
    //Edita el paquete
    private void btnEditarPaqueteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarPaqueteActionPerformed
        int fila = tblPaquetes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un paquete");
            return;
        }

        int id = (int) tblPaquetes.getValueAt(fila, 0);

        try {

            Paquete paquete = gestorPaquetes.buscarPaquete(id);

            if (paquete != null) {
                PaqueteDialog dialog = new PaqueteDialog(this, true, paquete);
                dialog.setVisible(true);
            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudo consultar el paquete.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEditarPaqueteActionPerformed

    /*
     * Registra una incidencia del paquete seleccionado (HU-07).
     * Es la misma operacion que hace el conductor desde el cliente vehiculo,
     * pero desde el monitor, para cuando avisa por telefono o por radio.
     */
    private void btnRegistrarIncidenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarIncidenciaActionPerformed
        int fila = tblPaquetes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione el paquete al que le va a registrar la incidencia");
            return;
        }

        int id = (int) tblPaquetes.getValueAt(fila, 0);

        try {

            Paquete paquete = gestorPaquetes.buscarPaquete(id);

            if (paquete == null) {
                JOptionPane.showMessageDialog(this, "El paquete ya no existe.");
                return;
            }

            //Sin vehiculo no hay a quien cargarle la incidencia
            if (paquete.getVehiculo() == null) {
                JOptionPane.showMessageDialog(this,
                        "El paquete " + id + " no tiene vehiculo asignado.\n"
                        + "Primero se le asigna un vehiculo con el boton Editar Paquete.",
                        "No se puede registrar",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            IncidenciaDialog dialog = new IncidenciaDialog(this, true, paquete);
            dialog.setVisible(true);

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudo consultar el paquete.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRegistrarIncidenciaActionPerformed

    /*
     * Abre la ventana donde el conductor reporta su ubicacion (RF-12).
     * Cada envio queda como una fila nueva en ubicaciones_vehiculo.
     */
    private void btnEnviarUbicacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarUbicacionActionPerformed
        UbicacionDialog dialog = new UbicacionDialog(this, true, usuarioSesion);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnEnviarUbicacionActionPerformed

    //Elimina el paquete
    private void btnEliminarPaqueteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarPaqueteActionPerformed
        int fila = tblPaquetes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un paquete");
            return;

        }

        int id = (int) tblPaquetes.getValueAt(fila, 0);
        int opcion = JOptionPane.showConfirmDialog(this, "Desea eliminar este paquete", "Confimar", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {

            try {

                gestorPaquetes.eliminarPaquete(id);
                actualizarTabla();

                JOptionPane.showMessageDialog(this, "Paquete eliminado");

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(this,
                        "No se pudo eliminar el paquete.\n" + ex.getLocalizedMessage(),
                        "Error de base de datos",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnEliminarPaqueteActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

    /*
     * El monitor ya no se abre directo porque necesita saber que usuario
     * inicio sesion. La entrada del sistema es la ventana de Login.
     */
    java.awt.EventQueue.invokeLater(new Runnable() {

        public void run() {

            new Login().setVisible(true);

        }

    });

}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnEditarPaquete;
    private javax.swing.JButton btnEliminarPaquete;
    private javax.swing.JButton btnEnviarUbicacion;
    private javax.swing.JButton btnRegistrarIncidencia;
    private javax.swing.JButton btnRegistrarPaquete;
    private javax.swing.JButton btnRegistrarVehiculo;
    private javax.swing.JMenuItem btnSalir;
    private javax.swing.JButton btnVerAlertas;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblEnRuta;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblEntregadosHoy;
    private javax.swing.JLabel lblPendientes;
    private javax.swing.JLabel lblTitAvance;
    private javax.swing.JLabel lblTitFlota;
    private javax.swing.JLabel lblTitIncidencias;
    private javax.swing.JLabel lblTitPaquetes;
    private javax.swing.JLabel lblValAvance;
    private javax.swing.JLabel lblValFlota;
    private javax.swing.JLabel lblValIncidencias;
    private javax.swing.JLabel lblValPaquetes;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JMenuItem miAcercaDe;
    private javax.swing.JTable tblPaquetes;
    private javax.swing.JTextArea txtAlertasCriticas;
    private javax.swing.JTextField txtEnRuta;
    private javax.swing.JTextField txtEntregadosHoy;
    private javax.swing.JTextField txtPendientes;
    // End of variables declaration//GEN-END:variables
}
