/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package ui;

import exceptions.DatosInvalidosException;
import exceptions.VehiculoDuplicadoException;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Automovil;
import model.Camion;
import model.Furgon;
import model.Motocicleta;
import model.Usuario;
import model.Vehiculo;
import service.GestorUsuarios;
import service.GestorVehiculos;

/**
 * Ventana de administracion de la flota (RF-04 y RF-05).
 *
 * Cubre las cuatro operaciones: registrar, consultar, modificar y dar de baja.
 * Solo la abre el administrador.
 *
 * El conductor se escoge de una lista que sale de la base, no se digita. Antes
 * se digitaba a mano y si el nombre no correspondia a un conductor registrado
 * el vehiculo quedaba sin conductor sin avisar.
 *
 * @author Grupo 6
 */
public class GestionVehiculos extends javax.swing.JDialog {

    //Opcion del combo para dejar el vehiculo sin conductor
    private static final String SIN_CONDUCTOR = "(Sin conductor)";

    private Principal principal;
    private GestorVehiculos gestorVehiculos;
    private GestorUsuarios gestorUsuarios;
    private DefaultTableModel modeloTabla;

    /**
     * Creates new form GestionVehiculos
     */

    //Constructor
    public GestionVehiculos(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        principal = (Principal) parent;
        gestorVehiculos = new GestorVehiculos();
        gestorUsuarios = new GestorUsuarios();

        setTitle("Gestión de Vehículos");
        setLocationRelativeTo(parent);

        cargarTabla();
        cargarConductores();
        actualizarTabla();
    }

    //Arma las columnas de la tabla
    private void cargarTabla() {

        String columnas[] = {"ID", "Placa", "Tipo", "Capacidad kg", "Conductor"};

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tblVehiculos.setModel(modeloTabla);
        tblVehiculos.setRowHeight(22);
        tblVehiculos.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
    }

    /*
     * Llena el combo con los usuarios que tienen rol de conductor. Asi no se
     * puede asignar un vehiculo a alguien que no es conductor.
     */
    private void cargarConductores() {

        cbConductor.removeAllItems();
        cbConductor.addItem(SIN_CONDUCTOR);

        try {

            for (Usuario conductor : gestorUsuarios.obtenerConductores()) {
                cbConductor.addItem(conductor.getUsuario());
            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los conductores.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //Trae la flota de la base y la pone en la tabla
    private void actualizarTabla() {

        try {

            modeloTabla.setRowCount(0);

            for (Vehiculo vehiculo : gestorVehiculos.obtenerVehiculos()) {

                String conductor = vehiculo.getConductor();

                if (conductor == null || conductor.isEmpty()) {
                    conductor = SIN_CONDUCTOR;
                }

                modeloTabla.addRow(new Object[]{
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getTipo(),
                    vehiculo.getCapacidad(),
                    conductor
                });
            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudo consultar la flota.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        txtPlaca.setText("");
        txtCapacidad.setText("");
        cbTipo.setSelectedIndex(0);
        cbConductor.setSelectedIndex(0);
        txtPlaca.setEnabled(true);
        tblVehiculos.clearSelection();
        txtPlaca.requestFocus();
    }

    /*
     * Segun el tipo escogido se crea la subclase que corresponde. Todas se
     * manejan como Vehiculo, que es el polimorfismo del modelo.
     */
    private Vehiculo crearVehiculo(String placa, double capacidad, String conductor, String tipo) {

        switch (tipo) {
            case "Motocicleta":
                return new Motocicleta(placa, capacidad, conductor);
            case "Camion":
                return new Camion(placa, capacidad, conductor);
            case "Furgon":
                return new Furgon(placa, capacidad, conductor);
            default:
                return new Automovil(placa, capacidad, conductor);
        }
    }

    //Devuelve el conductor escogido, o cadena vacia si es la opcion sin conductor
    private String obtenerConductorSeleccionado() {

        String conductor = cbConductor.getSelectedItem().toString();

        if (conductor.equals(SIN_CONDUCTOR)) {
            return "";
        }

        return conductor;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVehiculos = new javax.swing.JTable();
        lblPlaca = new javax.swing.JLabel();
        txtPlaca = new javax.swing.JTextField();
        lblTipo = new javax.swing.JLabel();
        cbTipo = new javax.swing.JComboBox<>();
        lblCapacidad = new javax.swing.JLabel();
        txtCapacidad = new javax.swing.JTextField();
        lblConductor = new javax.swing.JLabel();
        cbConductor = new javax.swing.JComboBox<>();
        lblAyuda = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        btnAgregar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Gestion de Vehiculos"));

        tblVehiculos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVehiculosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblVehiculos);

        lblPlaca.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblPlaca.setText("Placa:");

        lblTipo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTipo.setText("Tipo:");

        cbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Motocicleta", "Automovil", "Furgon", "Camion" }));

        lblCapacidad.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCapacidad.setText("Capacidad (kg):");

        lblConductor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblConductor.setText("Conductor:");

        lblAyuda.setText("Clic en una fila para cargarla");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btnAgregar.setText("Agregar");
        btnAgregar.setFocusable(false);
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAgregar);

        btnModificar.setText("Modificar");
        btnModificar.setFocusable(false);
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });
        jToolBar1.add(btnModificar);

        btnEliminar.setText("Eliminar");
        btnEliminar.setFocusable(false);
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEliminar);

        btnLimpiar.setText("Limpiar");
        btnLimpiar.setFocusable(false);
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });
        jToolBar1.add(btnLimpiar);

        btnCerrar.setText("Cerrar");
        btnCerrar.setFocusable(false);
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCerrar);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPlaca)
                            .addComponent(lblTipo)
                            .addComponent(lblCapacidad)
                            .addComponent(lblConductor))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCapacidad, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbConductor, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblAyuda)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPlaca)
                            .addComponent(txtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTipo)
                            .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCapacidad)
                            .addComponent(txtCapacidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblConductor)
                            .addComponent(cbConductor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblAyuda))
                .addGap(18, 18, 18)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Al hacer clic en una fila se cargan los datos en los campos
    private void tblVehiculosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVehiculosMouseClicked
        int fila = tblVehiculos.getSelectedRow();

        if (fila == -1) {
            return;
        }

        txtPlaca.setText(tblVehiculos.getValueAt(fila, 1).toString());
        cbTipo.setSelectedItem(tblVehiculos.getValueAt(fila, 2).toString());
        txtCapacidad.setText(tblVehiculos.getValueAt(fila, 3).toString());
        cbConductor.setSelectedItem(tblVehiculos.getValueAt(fila, 4).toString());

        //La placa no se cambia, es la llave con la que se busca el vehiculo
        txtPlaca.setEnabled(false);
    }//GEN-LAST:event_tblVehiculosMouseClicked

    //Registra un vehiculo nuevo
    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed

        if (txtPlaca.getText().trim().isEmpty() || txtCapacidad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe digitar la placa y la capacidad.");
            return;
        }

        try {

            double capacidad = Double.parseDouble(txtCapacidad.getText().trim());

            if (capacidad <= 0) {
                throw new DatosInvalidosException("La capacidad tiene que ser mayor a cero.");
            }

            gestorVehiculos.agregarVehiculo(crearVehiculo(
                    txtPlaca.getText().trim(),
                    capacidad,
                    obtenerConductorSeleccionado(),
                    cbTipo.getSelectedItem().toString()));

            JOptionPane.showMessageDialog(this, "Vehículo registrado correctamente");

            actualizarTabla();
            limpiar();
            principal.actualizarTabla();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(this, "La capacidad tiene que ser un valor numerico.");

        } catch (DatosInvalidosException | VehiculoDuplicadoException ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar el vehículo.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAgregarActionPerformed

    //Modifica el vehiculo seleccionado
    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed

        if (tblVehiculos.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla.");
            return;
        }

        if (txtCapacidad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe digitar la capacidad.");
            return;
        }

        try {

            double capacidad = Double.parseDouble(txtCapacidad.getText().trim());

            if (capacidad <= 0) {
                throw new DatosInvalidosException("La capacidad tiene que ser mayor a cero.");
            }

            gestorVehiculos.modificarVehiculo(crearVehiculo(
                    txtPlaca.getText().trim(),
                    capacidad,
                    obtenerConductorSeleccionado(),
                    cbTipo.getSelectedItem().toString()));

            JOptionPane.showMessageDialog(this, "Vehículo modificado correctamente");

            actualizarTabla();
            limpiar();
            principal.actualizarTabla();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(this, "La capacidad tiene que ser un valor numerico.");

        } catch (DatosInvalidosException ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudo modificar el vehículo.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnModificarActionPerformed

    //Da de baja el vehiculo seleccionado
    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

        if (tblVehiculos.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla.");
            return;
        }

        String placa = txtPlaca.getText().trim();

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea dar de baja el vehículo " + placa + "?\n"
                + "Sus ubicaciones e incidencias se conservan para la trazabilidad.",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            gestorVehiculos.eliminarVehiculo(placa);

            JOptionPane.showMessageDialog(this, "Vehículo dado de baja");

            actualizarTabla();
            limpiar();
            principal.actualizarTabla();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "No se pudo dar de baja el vehículo.\n" + ex.getLocalizedMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JComboBox<String> cbConductor;
    private javax.swing.JComboBox<String> cbTipo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblCapacidad;
    private javax.swing.JLabel lblConductor;
    private javax.swing.JLabel lblPlaca;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JTable tblVehiculos;
    private javax.swing.JTextField txtCapacidad;
    private javax.swing.JTextField txtPlaca;
    // End of variables declaration//GEN-END:variables
}
