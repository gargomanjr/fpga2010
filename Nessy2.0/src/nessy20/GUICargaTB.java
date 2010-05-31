/*
 * GUICargaVHDL.java
 *
 * Created on 10 de mayo de 2010, 15:35
 */

package nessy20;

import compiladorEntidad.Entidad;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 *
 * @author  David
 */
public class GUICargaTB extends javax.swing.JDialog {
    
    private Seleccion sel;
    private JFrame padre;

    /** Creates new form GUICargaVHDL
     * @param jf
     * @param sel
     * @param bol
     */
    public GUICargaTB(JFrame jf,boolean bol,Seleccion sel) {
        super(jf,bol);
        initComponents();
        this.sel=sel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        _btn_CargarPantalla = new javax.swing.JRadioButton();
        _btn_CargarFichero = new javax.swing.JRadioButton();
        _btnOK = new javax.swing.JButton();
        _btnCancelar = new javax.swing.JButton();

        setTitle("Cargar VHDL");
        setIconImage(null);
        setResizable(false);

        _btn_CargarPantalla.setSelected(true);
        _btn_CargarPantalla.setText("Cargar TB en Pantalla");
        buttonGroup1.add(_btn_CargarPantalla);
        _btn_CargarPantalla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btn_CargarPantallaActionPerformed(evt);
            }
        });

        _btn_CargarFichero.setText("Cargar TB desde Fichero y Ejecutar");
        buttonGroup1.add(_btn_CargarFichero);

        _btnOK.setText("OK");
        _btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnOKActionPerformed(evt);
            }
        });

        _btnCancelar.setText("Cancelar");
        _btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(79, Short.MAX_VALUE)
                .addComponent(_btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67)
                .addComponent(_btnCancelar)
                .addGap(88, 88, 88))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_btn_CargarPantalla)
                    .addComponent(_btn_CargarFichero))
                .addContainerGap(123, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {_btnCancelar, _btnOK});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addComponent(_btn_CargarPantalla)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_btn_CargarFichero)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_btnCancelar)
                    .addComponent(_btnOK))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {_btnCancelar, _btnOK});

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

private void _btn_CargarPantallaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btn_CargarPantallaActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event__btn_CargarPantallaActionPerformed

private void _btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnOKActionPerformed
    
    if(_btn_CargarPantalla.isSelected())
    {
        sel.selTB=SeleccionTB.CARGA_PANTALLA;
    }
    else
    {
        if(_btn_CargarFichero.isSelected())
        {   
            sel.selTB=SeleccionTB.CARGA_FICHERO;
        }
        else
            sel.selTB=SeleccionTB.NADA;

    }
    this.dispose();
}//GEN-LAST:event__btnOKActionPerformed

private void _btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCancelarActionPerformed
    this.setVisible(false);
}//GEN-LAST:event__btnCancelarActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _btnCancelar;
    private javax.swing.JButton _btnOK;
    private javax.swing.JRadioButton _btn_CargarFichero;
    private javax.swing.JRadioButton _btn_CargarPantalla;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
