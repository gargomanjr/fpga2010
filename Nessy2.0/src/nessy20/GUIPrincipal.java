/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GUIPrincipal.java
 *
 * Created on 01-mar-2010, 22:50:42
 */
package nessy20;

import compiladorEntidad.Errores;
import compiladorEntidad.SintacticoEntidad;

import IOFPGA.Ejecucion;
import IOFPGA.RecepcionFPGA;
import app.*;
import compiladorEntidad.Entidad;
import core.SerialPort;
import generadorVHDL.GeneraVhdl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Tony
 */
public class GUIPrincipal extends javax.swing.JFrame {
    //private EstadoContador estado_cont;

    private Parameters param;
    private Com com1;
    private RecepcionFPGA hiloreceptor;
    private Ejecucion ejec;
    private String ruta;
    private String fichero;
    private Entidad entidad;
    private int top;

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
    private static Logger logger = Logger.getLogger("GUIPrincipal.class");

    public Entidad getEntidad() {
        return entidad;
    }

    public boolean compilarEntidad() {
        boolean correcto = true;
        SintacticoEntidad compilador = null;
        Errores errores = new Errores();
        GeneraVhdl generador;

        try {
            /*StringReader rd = new StringReader(this._TxtEntityVHD.getText());
            BufferedReader brd = new BufferedReader(rd);*/

            compilador = new SintacticoEntidad(fichero, errores);
            compilador.inicia();

            boolean error = compilador.Entidad();
            if (!error) {
                compilador.getEntidad().muestra();
                this.entidad = compilador.getEntidad();
                generador = new GeneraVhdl("IOSerie//Circuito_FPGA.vhd", compilador.getEntidad(), errores);
                if (generador.abrir()) {
                    generador.crearFichero();
                    generador.cerrar();
                    System.out.println("Fichero vhdl creado correctamente");
                    //compilación y creación del .bit
                    // Process p = Runtime.getRuntime().exec("cmd.exe /C start comandosXilinx\\compilar.bat " + fichero);

                } else {
                    this.muestraErroresConsola(errores);
                    correcto = false;
                }
            } else {
                this.muestraErroresConsola(errores);
                correcto = false;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                errores.error(e.getMessage());
                this.muestraErroresConsola(errores);
            } else {
                e.printStackTrace();
            }
            correcto = false;

        }
        if (compilador != null) {
            compilador.cerrar();
        }
        return correcto;
    }

    /** Creates new form GUIPrincipal */
    public GUIPrincipal() {
        logger.info("Ejecutando Nessy 2.0...");
        initComponentsAux();
        initComponents();
        this._btnReanudar.setEnabled(false);
        this._btnPararEjecucion.setEnabled(false);
    }

    public boolean inicializarPuertoSerie() {
        boolean correcto = true;
        try {
            param = new Parameters();
            param.setPort("COM1");
            param.setBaudRate("9600");
            //Comprobamos que el puerto COM1 está libre, o si la maquina
            // en la que ejecutamos posee puerto COM1
            SerialPort puerto = new SerialPort();
            if (puerto.getStateSerialPortC("COM1").equals("free")) {
                com1 = new Com(param);
            } else {
                JOptionPane.showMessageDialog(this, "El puerto COM1 no se encuentra libre o " + "el PC no posee puerto COM1", "Info", JOptionPane.INFORMATION_MESSAGE);
                correcto = false;
            }
        } catch (Exception ex) {
            System.out.println(ex);
            JOptionPane.showMessageDialog(this, "La aplicación ya se encuentra ejecutándose, ciérrela para ejecutar nuevamente la aplicación.", "Info", JOptionPane.INFORMATION_MESSAGE);
            logger.info("La aplicacion ya se encuentra ejecutandose" + ex);
            System.exit(0);
        }
        return correcto;
    }

    private void cargaTopVHDL() {
        boolean error = false;
        JFileChooser chooser;
        this._TxtEntityVHD.setText("");
        chooser = new JFileChooser();
        Filtro filter = new Filtro("vhd");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Seleccionar Archivo VHDL");
        chooser.setAcceptAllFileFilterUsed(false);
        //chooser.setMultiSelectionEnabled(true);
       
       if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //try {
            fichero = chooser.getSelectedFile().getAbsolutePath();

            
            error = error || !compilarEntidad();
            if (!error) {
                if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelVHD)) {
                    jTabbedPane1.setSelectedComponent(panelVHD);
                } else {
                    _TxtEntityVHD.setColumns(20);
                    _TxtEntityVHD.setEditable(false);
                    _TxtEntityVHD.setRows(5);
                    jScrollPane1.setViewportView(_TxtEntityVHD);

                    javax.swing.GroupLayout panelVHDLayout = new javax.swing.GroupLayout(panelVHD);
                    panelVHD.setLayout(panelVHDLayout);
                    panelVHDLayout.setHorizontalGroup(
                            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE));
                    panelVHDLayout.setVerticalGroup(
                            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

                    jTabbedPane1.addTab("Entity VHDL", panelVHD);
                    jTabbedPane1.setSelectedComponent(panelVHD);
                }
                this._TxtEntityVHD.setText(this.entidad.toString());
                JOptionPane.showMessageDialog(this, "Entity cargada correctamente", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar el fichero de la entity", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("No Selection ");
        }
    }

    private void cargaVariosVHDL() {
         boolean error = false;
        JFileChooser chooser;
        this._TxtEntityVHD.setText("");
        chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        Filtro filter = new Filtro("vhd");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Seleccionar Archivos VHDL");
        chooser.setAcceptAllFileFilterUsed(false);
        //chooser.setMultiSelectionEnabled(true);
       
       if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //try {
            File[] files; 
            String[] ficheros;
            fichero = chooser.getSelectedFile().getAbsolutePath();
            files=chooser.getSelectedFiles();
            ficheros = new String[files.length];
            for(int i=0; i<files.length;i++)
            {
                ficheros[i]= files[i].getName();
            }
            GUISeleccionTop selTop=new  GUISeleccionTop(this,true,ficheros);
            selTop.setVisible(true);
            
       }   
            
            
            
            
           /* error = error || !compilarEntidad();
            if (!error) {
                if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelVHD)) {
                    jTabbedPane1.setSelectedComponent(panelVHD);
                } else {
                    _TxtEntityVHD.setColumns(20);
                    _TxtEntityVHD.setEditable(false);
                    _TxtEntityVHD.setRows(5);
                    jScrollPane1.setViewportView(_TxtEntityVHD);

                    javax.swing.GroupLayout panelVHDLayout = new javax.swing.GroupLayout(panelVHD);
                    panelVHD.setLayout(panelVHDLayout);
                    panelVHDLayout.setHorizontalGroup(
                            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE));
                    panelVHDLayout.setVerticalGroup(
                            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

                    jTabbedPane1.addTab("Entity VHDL", panelVHD);
                    jTabbedPane1.setSelectedComponent(panelVHD);
                }
                this._TxtEntityVHD.setText(this.entidad.toString());
                JOptionPane.showMessageDialog(this, "Entity cargada correctamente", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar el fichero de la entity", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("No Selection ");
        }*/
    }

    private void ejec(){
        if (this.ejec != null) {// || this.ejec.getState() == State.WAITING) {
                this._TextSalida.setText("");
                //this.hiloreceptor.pararrecepcionfpga();
        }

        if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelOutPut)) {
            jTabbedPane1.setSelectedComponent(panelOutPut);
        } else {
            _TextSalida.setColumns(20);
            _TextSalida.setEditable(false);
            _TextSalida.setRows(5);
            jScrollPane4.setViewportView(_TextSalida);

            javax.swing.GroupLayout panelOutPutLayout = new javax.swing.GroupLayout(panelOutPut);
            panelOutPut.setLayout(panelOutPutLayout);
            panelOutPutLayout.setHorizontalGroup(
                    panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
            panelOutPutLayout.setVerticalGroup(
                    panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

            jTabbedPane1.addTab("OutPut", panelOutPut);
            jTabbedPane1.setSelectedComponent(panelOutPut);
        }

        if (this.entidad != null) {//si la entidad está definida
            //this.hiloreceptor = new RecepcionFPGA(this._TextSalida, this.entidad.getBitsSalida(), param, com1);
            //hiloreceptor.start();
            String ls_cadenaaejecutar = this._txtTB.getText();
            this.ejec = new Ejecucion(this._lblnInst, this.entidad.getBitsEntrada(), this.com1, this._TextSalida);
            this.ejec.setCadena(ls_cadenaaejecutar);
            //this.ejec.TraduceString();
            if (ejec.convierteCadenas()) {
                ejec.start();
                //this.jTabbedPane1.setSelectedIndex(3);
                this._btnReanudar.setEnabled(false);
                this._btnPararEjecucion.setEnabled(true);
            } else {
                //JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n"+"Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a "+ Integer.toString(4)+" .", "Error", JOptionPane.ERROR_MESSAGE);
                JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n" + "Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a " + Integer.toString(this.getEntidad().getBitsEntrada()) + " .", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "La entidad no está definida", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void initComponentsAux() {
        jTabbedPane1 = new JTabbedPaneWithCloseIcon();
    }

    private void muestraErroresConsola(Errores errores) {
        this._TxtEntityVHD.setText("");
        for (int i = 0; i < errores.getErrores().size(); i++) {
            this._TxtEntityVHD.append(errores.getErrores().get(i) + "\n");
        }
    }

    public void EscribirDatoPantalla(char c) {
        this._TextSalida.setText(this._TextSalida.getText() + c + " ");
    }

    public void EscribirDatoPantalla(String c) {
        this._TextSalida.setText(this._TextSalida.getText() + c + "\n");
    }

    void escribirEnPantalla(String str) {
        this._TextCargarbit.append(str + "\n");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        _btnCargarVhd = new javax.swing.JButton();
        _btnCrearBit = new javax.swing.JButton();
        _btnCargarBit = new javax.swing.JButton();
        _btnCargarTB = new javax.swing.JButton();
        _btnEjecutar = new javax.swing.JButton();
        _btnPararEjecucion = new javax.swing.JButton();
        _btnReanudar = new javax.swing.JButton();
        jTabbedPane1 = new JTabbedPaneWithCloseIcon();
        panelVHD = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _TxtEntityVHD = new javax.swing.JTextArea();
        panelCargar = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        _TextCargarbit = new javax.swing.JTextArea();
        panelTB = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        _txtTB = new javax.swing.JTextArea();
        panelOutPut = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        _TextSalida = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        _lblnInst = new javax.swing.JTextField();
        _btnClear = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuOpciones = new javax.swing.JMenu();
        menuOpcionesCargarVHD = new javax.swing.JMenuItem();
        menuOpcionesCrearBit = new javax.swing.JMenuItem();
        menuOpcionesCargarBit = new javax.swing.JMenuItem();
        menuOpcionesCargarTB = new javax.swing.JMenuItem();
        menuOpcionesEjec = new javax.swing.JMenuItem();
        menuOpcionesPararEjec = new javax.swing.JMenuItem();
        menuOpcionesReanudarEjec = new javax.swing.JMenuItem();
        menuVistas = new javax.swing.JMenu();
        menuVistasEntityVHD = new javax.swing.JMenuItem();
        menuVistasCargar = new javax.swing.JMenuItem();
        menuVistasTB = new javax.swing.JMenuItem();
        menuVistasOutPut = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nessy 2.0");
        setIconImage(new ImageIcon("src/recursos/Nessy.png").getImage());
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jToolBar1.setBorder(null);
        jToolBar1.setRollover(true);

        _btnCargarVhd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnCargarVhdl.png"))); // NOI18N
        _btnCargarVhd.setText("Cargar VHD");
        _btnCargarVhd.setFocusable(false);
        _btnCargarVhd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCargarVhd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCargarVhd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCargarVhdActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCargarVhd);

        _btnCrearBit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnCrearBit.png"))); // NOI18N
        _btnCrearBit.setText("Crear .Bit");
        _btnCrearBit.setFocusable(false);
        _btnCrearBit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCrearBit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCrearBit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCrearBitActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCrearBit);

        _btnCargarBit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnCargarBit.png"))); // NOI18N
        _btnCargarBit.setText("Cargar .Bit");
        _btnCargarBit.setFocusable(false);
        _btnCargarBit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCargarBit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCargarBit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCargarBitActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCargarBit);

        _btnCargarTB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnCargarTB.png"))); // NOI18N
        _btnCargarTB.setText("Cargar TB");
        _btnCargarTB.setFocusable(false);
        _btnCargarTB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCargarTB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCargarTB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCargarTBActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCargarTB);

        _btnEjecutar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnEjec.png"))); // NOI18N
        _btnEjecutar.setText("Ejecutar");
        _btnEjecutar.setContentAreaFilled(false);
        _btnEjecutar.setFocusable(false);
        _btnEjecutar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnEjecutar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnEjecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnEjecutarActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnEjecutar);

        _btnPararEjecucion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnPararEjec.png"))); // NOI18N
        _btnPararEjecucion.setText("Parar Ejecución");
        _btnPararEjecucion.setFocusable(false);
        _btnPararEjecucion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnPararEjecucion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnPararEjecucion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnPararEjecucionActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnPararEjecucion);

        _btnReanudar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnReanudarEjec.png"))); // NOI18N
        _btnReanudar.setText("Reanudar Ejecución");
        _btnReanudar.setFocusable(false);
        _btnReanudar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnReanudar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnReanudar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnReanudarActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnReanudar);

        _TxtEntityVHD.setColumns(20);
        _TxtEntityVHD.setEditable(false);
        _TxtEntityVHD.setRows(5);
        jScrollPane1.setViewportView(_TxtEntityVHD);

        javax.swing.GroupLayout panelVHDLayout = new javax.swing.GroupLayout(panelVHD);
        panelVHD.setLayout(panelVHDLayout);
        panelVHDLayout.setHorizontalGroup(
            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
        );
        panelVHDLayout.setVerticalGroup(
            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Entity VHDL", panelVHD);

        _TextCargarbit.setColumns(20);
        _TextCargarbit.setRows(5);
        _TextCargarbit.setMaximumSize(getMaximumSize());
        jScrollPane2.setViewportView(_TextCargarbit);

        javax.swing.GroupLayout panelCargarLayout = new javax.swing.GroupLayout(panelCargar);
        panelCargar.setLayout(panelCargarLayout);
        panelCargarLayout.setHorizontalGroup(
            panelCargarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
        );
        panelCargarLayout.setVerticalGroup(
            panelCargarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Cargar", panelCargar);

        _txtTB.setColumns(20);
        _txtTB.setRows(5);
        jScrollPane3.setViewportView(_txtTB);

        javax.swing.GroupLayout panelTBLayout = new javax.swing.GroupLayout(panelTB);
        panelTB.setLayout(panelTBLayout);
        panelTBLayout.setHorizontalGroup(
            panelTBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
        );
        panelTBLayout.setVerticalGroup(
            panelTBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("TestBench", panelTB);

        _TextSalida.setColumns(20);
        _TextSalida.setEditable(false);
        _TextSalida.setRows(5);
        jScrollPane4.setViewportView(_TextSalida);

        javax.swing.GroupLayout panelOutPutLayout = new javax.swing.GroupLayout(panelOutPut);
        panelOutPut.setLayout(panelOutPutLayout);
        panelOutPutLayout.setHorizontalGroup(
            panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
        );
        panelOutPutLayout.setVerticalGroup(
            panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("OutPut", panelOutPut);

        jLabel1.setText("Número de Instrucción");
        jLabel1.setAutoscrolls(true);

        _lblnInst.setEditable(false);

        _btnClear.setText("Clear");
        _btnClear.setAutoscrolls(true);
        _btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnClearActionPerformed(evt);
            }
        });

        menuOpciones.setText("Opciones");

        menuOpcionesCargarVHD.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCargarVHD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCargarVhdl.png"))); // NOI18N
        menuOpcionesCargarVHD.setText("Cargar VHD");
        menuOpcionesCargarVHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCargarVHDActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCargarVHD);

        menuOpcionesCrearBit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCrearBit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCrearBit.png"))); // NOI18N
        menuOpcionesCrearBit.setText("Crear .Bit");
        menuOpcionesCrearBit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCrearBitActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCrearBit);

        menuOpcionesCargarBit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCargarBit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCargarBit.png"))); // NOI18N
        menuOpcionesCargarBit.setText("Cargar .Bit");
        menuOpcionesCargarBit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCargarBitActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCargarBit);

        menuOpcionesCargarTB.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCargarTB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCargarTB.png"))); // NOI18N
        menuOpcionesCargarTB.setText("Cargar TB");
        menuOpcionesCargarTB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCargarTBActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCargarTB);

        menuOpcionesEjec.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesEjec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuEjec.png"))); // NOI18N
        menuOpcionesEjec.setText("Ejecutar");
        menuOpcionesEjec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesEjecActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesEjec);

        menuOpcionesPararEjec.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesPararEjec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuPararEjec.png"))); // NOI18N
        menuOpcionesPararEjec.setText("Parar Ejecucion");
        menuOpcionesPararEjec.setEnabled(false);
        menuOpcionesPararEjec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesPararEjecActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesPararEjec);

        menuOpcionesReanudarEjec.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesReanudarEjec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuReanudarEjec.png"))); // NOI18N
        menuOpcionesReanudarEjec.setText("Reanudar Ejecucion");
        menuOpcionesReanudarEjec.setEnabled(false);
        menuOpcionesReanudarEjec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesReanudarEjecActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesReanudarEjec);

        jMenuBar1.add(menuOpciones);

        menuVistas.setText("Vistas");

        menuVistasEntityVHD.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.ALT_MASK));
        menuVistasEntityVHD.setText("Entity VHD");
        menuVistasEntityVHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuVistasEntityVHDActionPerformed(evt);
            }
        });
        menuVistas.add(menuVistasEntityVHD);

        menuVistasCargar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        menuVistasCargar.setText("Cargar");
        menuVistasCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuVistasCargarActionPerformed(evt);
            }
        });
        menuVistas.add(menuVistasCargar);

        menuVistasTB.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK));
        menuVistasTB.setText("TestBench");
        menuVistasTB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuVistasTBActionPerformed(evt);
            }
        });
        menuVistas.add(menuVistasTB);

        menuVistasOutPut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        menuVistasOutPut.setText("OutPut");
        menuVistasOutPut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuVistasOutPutActionPerformed(evt);
            }
        });
        menuVistas.add(menuVistasOutPut);

        jMenuBar1.add(menuVistas);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(377, Short.MAX_VALUE)
                .addComponent(_btnClear)
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(_lblnInst, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77))
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(_lblnInst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_btnClear))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addGap(36, 36, 36))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _btnCargarVhdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarVhdActionPerformed

      
         Seleccion sel=new Seleccion();
         new GUICargaVHDL(this,true,sel).setVisible(true);
            if(sel.seleccion.equals(SeleccionCargaVHD.SELECCION_VHDL_TOP))
            {cargaTopVHDL();}
            else{
                if(sel.seleccion.equals(SeleccionCargaVHD.SELECCION_VARIOS_VHDL))
                {
                    cargaVariosVHDL();
                }
            }
    }//GEN-LAST:event__btnCargarVhdActionPerformed

    private void _btnCrearBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCrearBitActionPerformed
        try {
            //compilación y creación del .bit
            Process p = Runtime.getRuntime().exec("cmd.exe /C start comandosXilinx\\compilar.bat " + fichero);
        } catch (IOException ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event__btnCrearBitActionPerformed

    private void _btnCargarBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarBitActionPerformed
        //this.jTabbedPane1.setSelectedIndex(1);
        this._TextCargarbit.setText("Cargando ..........");
        String fichero_bit;
        boolean error;
        JFileChooser chooser;
        chooser = new JFileChooser();
        Filtro filter = new Filtro("bit");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Cargar BitStream");
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                fichero_bit = chooser.getSelectedFile().getAbsolutePath();
                CargaBit cargaBit = new CargaBit(this, fichero_bit);
                error = !cargaBit.cargar();
                if (!error) {
                    JOptionPane.showMessageDialog(this, "Bitstream cargado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cargar el fichero", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Selecc ");
            if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelCargar)) {
                jTabbedPane1.setSelectedComponent(panelCargar);
            } else {
                _TextCargarbit.setColumns(20);
                _TextCargarbit.setRows(5);
                _TextCargarbit.setMaximumSize(getMaximumSize());
                jScrollPane2.setViewportView(_TextCargarbit);

                javax.swing.GroupLayout panelCargarLayout = new javax.swing.GroupLayout(panelCargar);
                panelCargar.setLayout(panelCargarLayout);
                panelCargarLayout.setHorizontalGroup(
                        panelCargarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
                panelCargarLayout.setVerticalGroup(
                        panelCargarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

                jTabbedPane1.addTab("Cargar", panelCargar);
                jTabbedPane1.setSelectedComponent(panelCargar);
            }
            this._TextCargarbit.setText("No ha seleccionado el .bit, puede que si no lo ha cargado con anterioridad la aplicación no funcione.");
        }




    }//GEN-LAST:event__btnCargarBitActionPerformed

    private void _btnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnEjecutarActionPerformed
        if (this.com1 == null){
            if (this.inicializarPuertoSerie()){
                ejec();
            }
        }else{
            ejec();
        }
    }//GEN-LAST:event__btnEjecutarActionPerformed

    private void _btnPararEjecucionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnPararEjecucionActionPerformed
        try {
            // TODO: mandar enable a la placa
            if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelOutPut)) {
                jTabbedPane1.setSelectedComponent(panelOutPut);
            } else {
                _TextSalida.setColumns(20);
                _TextSalida.setEditable(false);
                _TextSalida.setRows(5);
                jScrollPane4.setViewportView(_TextSalida);

                javax.swing.GroupLayout panelOutPutLayout = new javax.swing.GroupLayout(panelOutPut);
                panelOutPut.setLayout(panelOutPutLayout);
                panelOutPutLayout.setHorizontalGroup(
                        panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
                panelOutPutLayout.setVerticalGroup(
                        panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

                jTabbedPane1.addTab("OutPut", panelOutPut);
                jTabbedPane1.setSelectedComponent(panelOutPut);
            }
            int longitud = this.entidad.getBitsEntrada();
            int DatoAEnviar = (int) Math.pow(2, longitud - 1);
            this.com1.sendSingleData(DatoAEnviar);
            System.out.println("PARANDO HILOS..");
            this.ejec.setSetwait(true);
            //this.hiloreceptor.setSetwait(true);
            this._btnReanudar.setEnabled(true);
            this._btnPararEjecucion.setEnabled(false);
        } catch (Exception ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event__btnPararEjecucionActionPerformed

    private void _btnReanudarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnReanudarActionPerformed
        try {
            if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelOutPut)) {
                jTabbedPane1.setSelectedComponent(panelOutPut);
            } else {
                _TextSalida.setColumns(20);
                _TextSalida.setEditable(false);
                _TextSalida.setRows(5);
                jScrollPane4.setViewportView(_TextSalida);

                javax.swing.GroupLayout panelOutPutLayout = new javax.swing.GroupLayout(panelOutPut);
                panelOutPut.setLayout(panelOutPutLayout);
                panelOutPutLayout.setHorizontalGroup(
                        panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
                panelOutPutLayout.setVerticalGroup(
                        panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

                jTabbedPane1.addTab("OutPut", panelOutPut);
                jTabbedPane1.setSelectedComponent(panelOutPut);
            }
            this.com1.sendSingleData(0);
            /*synchronized (this.hiloreceptor) {
                this.hiloreceptor.notify();
            }*/
            synchronized (this.ejec) {
                this.ejec.notify();
            }
            this._btnReanudar.setEnabled(false);
            this._btnPararEjecucion.setEnabled(true);
            //TODO : Función que volviera activar la fpga.
        } catch (Exception ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
        //TODO : Función que volviera activar la fpga.
    }//GEN-LAST:event__btnReanudarActionPerformed

    private void _btnCargarTBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarTBActionPerformed

        boolean error = false;
        JFileChooser chooser;

        String fichero_tb;

        this._txtTB.setText("");
        chooser =
                new JFileChooser();
        Filtro filter = new Filtro("txt");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Seleccionar TestBench");
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                fichero_tb = chooser.getSelectedFile().getAbsolutePath();

                FileReader fr = new FileReader(fichero_tb);
                BufferedReader br = new BufferedReader(fr);
                String linea = br.readLine();
                if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panelTB)) {
                    jTabbedPane1.setSelectedComponent(panelTB);
                } else {
                    _txtTB.setColumns(20);
                    _txtTB.setRows(5);
                    jScrollPane3.setViewportView(_txtTB);

                    javax.swing.GroupLayout panelTBLayout = new javax.swing.GroupLayout(panelTB);
                    panelTB.setLayout(panelTBLayout);
                    panelTBLayout.setHorizontalGroup(
                            panelTBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
                    panelTBLayout.setVerticalGroup(
                            panelTBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

                    jTabbedPane1.addTab("TestBench", panelTB);
                    jTabbedPane1.setSelectedComponent(panelTB);
                }
                while (linea != null) {
                    this._txtTB.append(linea + "\n");
                    linea = br.readLine();
                }

                br.close();
            } catch (IOException ex) {
                error = true;
                Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);


                this._txtTB.append(
                        "Error al cargar el banco de pruebas");
            }
            if (!error) {
                JOptionPane.showMessageDialog(this, "TestBench cargado correctamente", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar el fichero de test", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            System.out.println("No Selection ");
        }

//        this.jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event__btnCargarTBActionPerformed

    private void _btnClearActionPerformed(java.awt.event.ActionEvent evt) {

        javax.swing.JPanel panel = (javax.swing.JPanel) jTabbedPane1.getSelectedComponent();
        javax.swing.JScrollPane scrPanel = (javax.swing.JScrollPane) panel.getComponent(0);
        javax.swing.JViewport viewPort = (javax.swing.JViewport) scrPanel.getComponent(0);
        javax.swing.JTextArea txtArea = (javax.swing.JTextArea) viewPort.getComponent(0);
        txtArea.setText("");
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        ejec.pararrecepcionfpga();
        //this.hiloreceptor.pararrecepcionfpga();
    }//GEN-LAST:event_formWindowClosed

    private void menuOpcionesCargarVHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesCargarVHDActionPerformed
        _btnCargarVhdActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesCargarVHDActionPerformed

    private void menuOpcionesCrearBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesCrearBitActionPerformed
        _btnCrearBitActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesCrearBitActionPerformed

    private void menuOpcionesCargarBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesCargarBitActionPerformed
        _btnCargarBitActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesCargarBitActionPerformed

    private void menuOpcionesCargarTBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesCargarTBActionPerformed
        _btnCargarTBActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesCargarTBActionPerformed

    private void menuOpcionesEjecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesEjecActionPerformed
        _btnEjecutarActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesEjecActionPerformed

    private void menuOpcionesPararEjecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesPararEjecActionPerformed
        _btnPararEjecucionActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesPararEjecActionPerformed

    private void menuOpcionesReanudarEjecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesReanudarEjecActionPerformed
        _btnReanudarActionPerformed(evt);
    }//GEN-LAST:event_menuOpcionesReanudarEjecActionPerformed

    private void menuVistasEntityVHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuVistasEntityVHDActionPerformed
        try {
            jTabbedPane1.setSelectedComponent(panelVHD);
        } catch (IllegalArgumentException ex) {

            _TxtEntityVHD.setColumns(20);
            _TxtEntityVHD.setEditable(false);
            _TxtEntityVHD.setRows(5);
            jScrollPane1.setViewportView(_TxtEntityVHD);

            javax.swing.GroupLayout panelVHDLayout = new javax.swing.GroupLayout(panelVHD);
            panelVHD.setLayout(panelVHDLayout);
            panelVHDLayout.setHorizontalGroup(
                    panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE));
            panelVHDLayout.setVerticalGroup(
                    panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

            jTabbedPane1.addTab("Entity VHDL", panelVHD);
            jTabbedPane1.setSelectedComponent(panelVHD);
        }
    }//GEN-LAST:event_menuVistasEntityVHDActionPerformed

    private void menuVistasCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuVistasCargarActionPerformed
        try {
            jTabbedPane1.setSelectedComponent(panelCargar);
        } catch (IllegalArgumentException ex) {

            _TextCargarbit.setColumns(20);
            _TextCargarbit.setRows(5);
            _TextCargarbit.setMaximumSize(getMaximumSize());
            jScrollPane2.setViewportView(_TextCargarbit);

            javax.swing.GroupLayout panelCargarLayout = new javax.swing.GroupLayout(panelCargar);
            panelCargar.setLayout(panelCargarLayout);
            panelCargarLayout.setHorizontalGroup(
                    panelCargarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
            panelCargarLayout.setVerticalGroup(
                    panelCargarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

            jTabbedPane1.addTab("Cargar", panelCargar);
            jTabbedPane1.setSelectedComponent(panelCargar);

        }
    }//GEN-LAST:event_menuVistasCargarActionPerformed

    private void menuVistasTBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuVistasTBActionPerformed
        try {
            jTabbedPane1.setSelectedComponent(panelTB);
        } catch (IllegalArgumentException ex) {
            _txtTB.setColumns(20);
            _txtTB.setRows(5);
            jScrollPane3.setViewportView(_txtTB);

            javax.swing.GroupLayout panelTBLayout = new javax.swing.GroupLayout(panelTB);
            panelTB.setLayout(panelTBLayout);
            panelTBLayout.setHorizontalGroup(
                    panelTBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
            panelTBLayout.setVerticalGroup(
                    panelTBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

            jTabbedPane1.addTab("TestBench", panelTB);
            jTabbedPane1.setSelectedComponent(panelTB);

        }
    }//GEN-LAST:event_menuVistasTBActionPerformed

    private void menuVistasOutPutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuVistasOutPutActionPerformed
        try {
            jTabbedPane1.setSelectedComponent(panelOutPut);
        } catch (IllegalArgumentException ex) {

            _TextSalida.setColumns(20);
            _TextSalida.setEditable(false);
            _TextSalida.setRows(5);
            jScrollPane4.setViewportView(_TextSalida);

            javax.swing.GroupLayout panelOutPutLayout = new javax.swing.GroupLayout(panelOutPut);
            panelOutPut.setLayout(panelOutPutLayout);
            panelOutPutLayout.setHorizontalGroup(
                    panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE));
            panelOutPutLayout.setVerticalGroup(
                    panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));

            jTabbedPane1.addTab("OutPut", panelOutPut);
            jTabbedPane1.setSelectedComponent(panelOutPut);
        }
    }//GEN-LAST:event_menuVistasOutPutActionPerformed
    public void setNumeroInst(int inst) {
        this._lblnInst.setText(Integer.toString(inst));
    }
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea _TextCargarbit;
    private javax.swing.JTextArea _TextSalida;
    private javax.swing.JTextArea _TxtEntityVHD;
    private javax.swing.JButton _btnCargarBit;
    private javax.swing.JButton _btnCargarTB;
    private javax.swing.JButton _btnCargarVhd;
    private javax.swing.JButton _btnClear;
    private javax.swing.JButton _btnCrearBit;
    private javax.swing.JButton _btnEjecutar;
    private javax.swing.JButton _btnPararEjecucion;
    private javax.swing.JButton _btnReanudar;
    private javax.swing.JTextField _lblnInst;
    private javax.swing.JTextArea _txtTB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenu menuOpciones;
    private javax.swing.JMenuItem menuOpcionesCargarBit;
    private javax.swing.JMenuItem menuOpcionesCargarTB;
    private javax.swing.JMenuItem menuOpcionesCargarVHD;
    private javax.swing.JMenuItem menuOpcionesCrearBit;
    private javax.swing.JMenuItem menuOpcionesEjec;
    private javax.swing.JMenuItem menuOpcionesPararEjec;
    private javax.swing.JMenuItem menuOpcionesReanudarEjec;
    private javax.swing.JMenu menuVistas;
    private javax.swing.JMenuItem menuVistasCargar;
    private javax.swing.JMenuItem menuVistasEntityVHD;
    private javax.swing.JMenuItem menuVistasOutPut;
    private javax.swing.JMenuItem menuVistasTB;
    private javax.swing.JPanel panelCargar;
    private javax.swing.JPanel panelOutPut;
    private javax.swing.JPanel panelTB;
    private javax.swing.JPanel panelVHD;
    // End of variables declaration//GEN-END:variables
    // private JTabbedPaneWithCloseIcon jTabbedPane1;
}
