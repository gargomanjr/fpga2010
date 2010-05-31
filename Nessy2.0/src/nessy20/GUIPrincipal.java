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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Constructor de la clase
 * @author Tony
 */
public class GUIPrincipal extends javax.swing.JFrame {
    //private EstadoContador estado_cont;
    private final static String RUTA_IOSERIE = System.getProperties().getProperty("user.dir") + "\\IOSerie";
    private Parameters param;
    private Com com1;
    private RecepcionFPGA hiloreceptor;
    private Ejecucion ejec;
    private String ruta;
    private String fichero;
    private Entidad entidad;
    private int top;
    private ArrayList<File> files;
    private boolean cerradoTop;
    String fichero_tb;
    String fichero_bit;
    private boolean SeleccionTBFich;
    private BufferedReader bf;

    /**
     * Devuelve el Array de ficheros Vhdl Cargados en la aplicación.
     * @return
     */
    public ArrayList<File> getFiles() {
        return files;
    }

    /**
     * Establece el Array de ficheros Vhdl Cargados en la aplicación.
     * @param files ArrayList de ficheros vhdl de la aplicación.
     */
    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    /**
     * Devuelve el índice del fichero top dentro del ArrayList
     * @return
     */
    public int getTop() {
        return top;
    }

    /**
     * Establece el fichero Top
     * @param top
     */
    public void setTop(int top) {
        this.top = top;
    }
    private static Logger logger = Logger.getLogger("GUIPrincipal.class");

    /**
     * Devuelve la Entidad con la que se está trabajado
     * @return Entidad con la que se está trabajando.
     */
    public Entidad getEntidad() {
        return entidad;
    }

    /**
     * Devuelve el atributo cerradoTop
     * @return
     */
    public boolean isCerradoTop() {
        return cerradoTop;
    }

    /**
     * Establece el campo cerradoTop, con el valor del argumento de la función.
     * @param cerradoTop Nuevo valor de tipo boolean de cerradoTop.
     */
    public void setCerradoTop(boolean cerradoTop) {
        this.cerradoTop = cerradoTop;
    }
    /**
     * Proceso encargado de ir modificando bit a bit el fichero .bit que tenemos cargado en la placa.
     * No muestra mensajes de aviso.
     */
    public void procesoModificarFicheros() {
        int numBits = 32;
        int numFrames = 361942;

        if (cargarBitConChooser() && SeleccionTBModifFichero()){
            seleccionaPanel(panelOutPut);
            for(int frame = 1; frame < numFrames; frame++){
                for(int bit = 0; bit < numBits; bit++){
                    try {
                        /*File ficheroParametros = new File(RUTA_IOSERIE, "paramsReconfig.txt");
                        FileOutputStream os = new FileOutputStream(ficheroParametros);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                        
                        bw.write(coms);
                        bw.close();
                        os.close();*/
                        String coms = "java -jar Virtex_II_Partial_Reconfiguration.jar -i "+fichero_bit+" -o "+RUTA_IOSERIE+"\\circuito_fpga_modif -f "+ frame +" -b "+ bit;
                        Process p = Runtime.getRuntime().exec("cmd.exe /C start " + coms);
    //                    this.wait();
    //                    synchronized(this)
    //	            {
                        this.cargarBit(RUTA_IOSERIE+"\\circuito_fpga_modif.bit",false);
                        //cargarBitConChooser();//pide un fichero
                        if (this.com1 == null) {
                            if (this.inicializarPuertoSerie()) {
                                 ejec(true);
                            }
                        } else {
                            ejec(true);
                        }
                        //TODO cargar restorer
                        this.cargarBit(RUTA_IOSERIE+"\\circuito_fpga_modifRestorer.bit",false);

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    }
    //                catch (InterruptedException ex) {
    //                        Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
    //                }

                }
            }
        }
   

    }

    /**
     *
     * @return
     */
    public boolean generarGolden() {
        boolean correcto = true;
        if (this.ejec != null) {// || this.ejec.getState() == State.WAITING) {
            ejec.pararrecepcionfpga();
            this._TextSalida.setText("");
            //this.hiloreceptor.pararrecepcionfpga();
        }

        //Selecciona panel
        seleccionaPanel(panelOutPut);

        if (this.entidad != null) {//si la entidad está definida
            //this.hiloreceptor = new RecepcionFPGA(this._TextSalida, this.entidad.getBitsSalida(), param, com1);
            //hiloreceptor.start();

            if (SeleccionTBFich) {
                try {
                    bf = new BufferedReader(new FileReader(fichero_tb));
                } catch (FileNotFoundException ex) {
                    correcto = false;
                }
                this.ejec = new Ejecucion(this._lblnInst, this.entidad.getBitsEntrada(), this.getEntidad().getBitsSalida(), this.com1, this._TextSalida, bf, false, "Golden.txt", "Traza.txt",false);
                this.ejec.setCadena("");
                ejec.start();
                this._btnReanudar.setEnabled(false);
                this._btnPararEjecucion.setEnabled(true);
            } else {
                String ls_cadenaaejecutar = this._txtTB.getText();
                this.ejec = new Ejecucion(this._lblnInst, this.entidad.getBitsEntrada(), this.getEntidad().getBitsSalida(), this.com1, this._TextSalida, false, "Golden.txt", "Traza.txt",false);
                this.ejec.setCadena(ls_cadenaaejecutar);
                if (ejec.convierteCadenas()) {
                    ejec.start();
                    //this.jTabbedPane1.setSelectedIndex(3);
                    this._btnReanudar.setEnabled(false);
                    this._btnPararEjecucion.setEnabled(true);
                } else {
                    //JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n"+"Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a "+ Integer.toString(4)+" .", "Error", JOptionPane.ERROR_MESSAGE);
                    JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n" + "Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a " + Integer.toString(this.getEntidad().getBitsEntrada()) + " .", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "La entidad no está definida", "Error", JOptionPane.ERROR_MESSAGE);
            correcto = false;
        }
        return correcto;
    }

    /**
     * 
     * @return
     */
    public boolean cargarBitConChooser() {
        //this.jTabbedPane1.setSelectedIndex(1);
        this._TextCargarbit.setText("Cargando ..........");
        boolean error = false;
        JFileChooser chooser;
        chooser = new JFileChooser();
        Filtro filter = new Filtro("bit");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Cargar BitStream");
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fichero_bit = chooser.getSelectedFile().getAbsolutePath();
            error = !this.cargarBit(fichero_bit,true);
        } else {
            System.out.println("Selecc ");
            //Selecciona panel
            seleccionaPanel(panelCargar);
            this._TextCargarbit.setText("No ha seleccionado el .bit, puede que si no lo ha cargado con anterioridad la aplicación no funcione.");
            error = true;
        }
        return !error;
    }

    /**
     *
     * @return
     */
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
        this.files = new ArrayList<File>();
    }

    /**
     *
     * @return
     */
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

    private void cargarVHDL() {
        boolean error = !compilarEntidad();
        if (!error) {
            //Selecciona panel
            seleccionaPanel(panelVHD);
            this._TxtEntityVHD.setText(this.entidad.toString());
            JOptionPane.showMessageDialog(this, "Entity cargada correctamente", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error al cargar el fichero de la entity", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void cargaTopVHDL() {
        JFileChooser chooser;
        this._TxtEntityVHD.setText("");
        chooser = new JFileChooser();
        Filtro filter = new Filtro("vhd");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Seleccionar Archivo VHDL");
        chooser.setAcceptAllFileFilterUsed(false);
        //chooser.setMultiSelectionEnabled(true);
        files = new ArrayList<File>();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //try {
            files.add(chooser.getSelectedFile());
            fichero = files.get(0).getAbsolutePath();
            _lbl_VHDLCargado.setText("Ultimo Top VHDL cargado : "+
                    chooser.getSelectedFile().getName());
            this.cargarVHDL();
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

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //try {

            ArrayList<String> ficheros = new ArrayList<String>();
            // fichero = chooser.getSelectedFile().getAbsolutePath();
            File[] f = chooser.getSelectedFiles();
            files = new ArrayList<File>();
            for (int i = 0; i < f.length; i++) {
                files.add(f[i]);
            }

            for (int i = 0; i < f.length; i++) {
                ficheros.add(f[i].getName());
            }
            cerradoTop = false;
            GUISeleccionTop selTop = new GUISeleccionTop(this, true, ficheros);
            selTop.setVisible(true);
            if (!cerradoTop) {
                fichero = files.get(top).getAbsolutePath(); //el fichero es el absoluto
                 _lbl_VHDLCargado.setText("Ultimo Top VHDL cargado : "
                         +files.get(top).getName());
                this.cargarVHDL();
            }

        }
    }

    private void copiaArchivo(String fich_lectura, String fich_escritura) {

        try {
            InputStream in;
            OutputStream out = new FileOutputStream(fich_escritura);

            byte[] buf = new byte[1024];
            int len;

            in = new FileInputStream(fich_lectura);

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void creaPrj() {
        BufferedWriter bw = null;
        boolean correcto = true;
        try {
//            JFileChooser chooser = new JFileChooser();
//            Filtro filter = new Filtro("bit");
//            chooser.addChoosableFileFilter(filter);
//            chooser.setCurrentDirectory(new java.io.File("."));
//            chooser.setDialogTitle("Guardar .bit");
//            chooser.setFileFilter(filter);
//            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//                String rutaFichBit = chooser.getSelectedFile().getAbsolutePath();
                bw = new BufferedWriter(new FileWriter("IOSerie//Circuito_FPGA.prj"));
               // bw = new BufferedWriter(new FileWriter(rutaFichBit));
                bw.write("vhdl work \"Tx_serie.vhd\"\n");
                bw.write("vhdl work \"Rx_serie.vhd\"\n");
                for (int i = 0; i < files.size(); i++) {
                    bw.write("vhdl work \"" + files.get(i).getAbsolutePath() + "\"\n");
                }

                
                bw.write("vhdl work \"Circuito_FPGA.vhd\"");
                bw.close();
            //}
        } catch (IOException ex) {
            ex.printStackTrace();
            //TODO Mostrar error
        }
    }

    private void ejec(boolean lb_recunfiguracion_pracial) {
        if (this.ejec != null) {// || this.ejec.getState() == State.WAITING) {
            ejec.pararrecepcionfpga();
            this._TextSalida.setText("");
            //this.hiloreceptor.pararrecepcionfpga();
        }

        //Selecciona panel
        seleccionaPanel(panelOutPut);

        if (this.entidad != null) {//si la entidad está definida
            //this.hiloreceptor = new RecepcionFPGA(this._TextSalida, this.entidad.getBitsSalida(), param, com1);
            //hiloreceptor.start();
            String ls_cadenaaejecutar = this._txtTB.getText();
            this.ejec = new Ejecucion(this._lblnInst, this.entidad.getBitsEntrada(), this.getEntidad().getBitsSalida(), this.com1, this._TextSalida, true, "Salida.txt", "Golden.txt",lb_recunfiguracion_pracial);
            this.ejec.setCadena(ls_cadenaaejecutar);

            if (SeleccionTBFich) {
                try {
                    bf = new BufferedReader(new FileReader(fichero_tb));
                } catch (FileNotFoundException ex) {
                }
                this.ejec = new Ejecucion(this._lblnInst, this.entidad.getBitsEntrada(), this.getEntidad().getBitsSalida(), this.com1, this._TextSalida, bf, true, "Salida.txt", "Golden.txt",lb_recunfiguracion_pracial);
                this.ejec.setCadena("");
                ejec.start();
                this._btnReanudar.setEnabled(false);
                this._btnPararEjecucion.setEnabled(true);
            } else {
                this.ejec = new Ejecucion(this._lblnInst, this.entidad.getBitsEntrada(), this.getEntidad().getBitsSalida(), this.com1, this._TextSalida, true, "Salida.txt", "Golden.txt",lb_recunfiguracion_pracial);
                this.ejec.setCadena(ls_cadenaaejecutar);
                if (ejec.convierteCadenas()) {
                    ejec.start();
                    //this.jTabbedPane1.setSelectedIndex(3);
                    this._btnReanudar.setEnabled(false);
                    this._btnPararEjecucion.setEnabled(true);
                } else {
                    //JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n"+"Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a "+ Integer.toString(4)+" .", "Error", JOptionPane.ERROR_MESSAGE);
                    JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n" + "Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a " + Integer.toString(this.getEntidad().getBitsEntrada()) + " .", "Error", JOptionPane.ERROR_MESSAGE);
                }
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

    /**
     *
     * @param c
     */
    public void EscribirDatoPantalla(char c) {
        this._TextSalida.setText(this._TextSalida.getText() + c + " ");
    }

    /**
     *
     * @param c
     */
    public void EscribirDatoPantalla(String c) {
        this._TextSalida.setText(this._TextSalida.getText() + c + "\n");
    }

    void escribirEnPantalla(String str) {
        this._TextCargarbit.append(str + "\n");
    }

    private boolean cargarBit(String fichero_bit,boolean ab_mostrar_mensajes) {
        boolean error = false;
        int intentos = 6;
        CargaBit cargaBit = new CargaBit(this, fichero_bit);
        try {
            do {//si hay un error lo vuelve a intentar
                error = !cargaBit.cargar();
                if (!error) {
                    if (ab_mostrar_mensajes){
                        JOptionPane.showMessageDialog(this, "Bitstream cargado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                    if (com1 != null) {
                        com1.close();
                        com1 = null;
                    }
                 }else{
                    intentos--;
                 }
            } while (error && intentos > 0);//intenta cargar 6 veces el .bit
        } catch (Exception e) {
            error = true;
        }
        return !error;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        _btnCargarVhd = new javax.swing.JButton();
        _btnCrearBit = new javax.swing.JButton();
        _btnEjecutar = new javax.swing.JButton();
        _btnCargarBit = new javax.swing.JButton();
        _btnCargarTB = new javax.swing.JButton();
        _btnPararEjecucion = new javax.swing.JButton();
        _btnReanudar = new javax.swing.JButton();
        _btnGenerarGolden = new javax.swing.JButton();
        _btnCargarGolden = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        _btnClear = new javax.swing.JButton();
        _lblnInst = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
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
        _lbl_BitCargado = new javax.swing.JLabel();
        _lbl_VHDLCargado = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Menu"));
        jPanel2.setOpaque(false);

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

        _btnGenerarGolden.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnGeneraGolden.png"))); // NOI18N
        _btnGenerarGolden.setText("Generar Golden");
        _btnGenerarGolden.setFocusable(false);
        _btnGenerarGolden.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnGenerarGolden.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnGenerarGolden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnGenerarGoldenActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnGenerarGolden);

        _btnCargarGolden.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/btnCargaGolden.jpg"))); // NOI18N
        _btnCargarGolden.setText("Cargar Golden");
        _btnCargarGolden.setFocusable(false);
        _btnCargarGolden.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCargarGolden.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCargarGolden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCargarGoldenActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCargarGolden);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 862, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Pantalla"));
        jPanel1.setOpaque(false);

        _btnClear.setText("Clear");
        _btnClear.setAutoscrolls(true);
        _btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnClearActionPerformed(evt);
            }
        });

        _lblnInst.setEditable(false);

        jLabel1.setText("Número de Instrucción");
        jLabel1.setAutoscrolls(true);

        _TxtEntityVHD.setColumns(20);
        _TxtEntityVHD.setEditable(false);
        _TxtEntityVHD.setRows(5);
        jScrollPane1.setViewportView(_TxtEntityVHD);

        javax.swing.GroupLayout panelVHDLayout = new javax.swing.GroupLayout(panelVHD);
        panelVHD.setLayout(panelVHDLayout);
        panelVHDLayout.setHorizontalGroup(
            panelVHDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
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
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
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
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
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
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
        );
        panelOutPutLayout.setVerticalGroup(
            panelOutPutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("OutPut", panelOutPut);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_lbl_VHDLCargado)
                    .addComponent(_lbl_BitCargado, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(804, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_btnClear)
                            .addGap(31, 31, 31)
                            .addComponent(jLabel1)
                            .addGap(18, 18, 18)
                            .addComponent(_lblnInst, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(67, 67, 67))
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 836, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(402, Short.MAX_VALUE)
                .addComponent(_lbl_VHDLCargado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_lbl_BitCargado)
                .addGap(6, 6, 6))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(_lblnInst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(_btnClear))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("");

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
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 916, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _btnCargarVhdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarVhdActionPerformed


        Seleccion sel = new Seleccion();
        new GUICargaVHDL(this, true, sel).setVisible(true);
        if (sel.seleccion.equals(SeleccionCargaVHD.SELECCION_VHDL_TOP)) {
            cargaTopVHDL();
        } else {
            if (sel.seleccion.equals(SeleccionCargaVHD.SELECCION_VARIOS_VHDL)) {
                cargaVariosVHDL();
            }
        }
    }//GEN-LAST:event__btnCargarVhdActionPerformed

    private void _btnCrearBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCrearBitActionPerformed
        try {
            //creamos el prj para poder crear el .bit
            this.creaPrj();
            //compilación y creación del .bit
            Process p = Runtime.getRuntime().exec("cmd.exe /C start comandosXilinx\\compilar.bat " + fichero);
        } catch (IOException ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event__btnCrearBitActionPerformed

    private void _btnCargarBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarBitActionPerformed
        //this.cargarBitConChooser();
        procesoModificarFicheros();


    }//GEN-LAST:event__btnCargarBitActionPerformed

    private void _btnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnEjecutarActionPerformed
        if (this.com1 == null) {
            if (this.inicializarPuertoSerie()) {
                ejec(false);
            }
        } else {
                ejec(false);
        }
    }//GEN-LAST:event__btnEjecutarActionPerformed

    private void _btnPararEjecucionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnPararEjecucionActionPerformed




        // TODO: mandar enable a la placa
        //Selecciona el panel
        seleccionaPanel(panelOutPut);

        int longitud = this.entidad.getBitsEntrada();
        //int DatoAEnviar = (int) Math.pow(2, longitud - 1);
        //this.com1.sendSingleData(DatoAEnviar);
        System.out.println("PARANDO HILOS..");
        this.ejec.setSetwait(true);
        //this.hiloreceptor.setSetwait(true);
        this._btnReanudar.setEnabled(true);
        this._btnPararEjecucion.setEnabled(false);

    }//GEN-LAST:event__btnPararEjecucionActionPerformed

    private void _btnReanudarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnReanudarActionPerformed

        // this.com1.sendSingleData(0);
            /*synchronized (this.hiloreceptor) {
        this.hiloreceptor.notify();
        }*/

        //Selecciona panel
        //this.inicializarPuertoSerie();
        seleccionaPanel(panelOutPut);

        synchronized (this.ejec) {
            this.ejec.notify();
        }
        this._btnReanudar.setEnabled(false);
        this._btnPararEjecucion.setEnabled(true);
        //TODO : Función que volviera activar la fpga.

        //TODO : Función que volviera activar la fpga.
    }//GEN-LAST:event__btnReanudarActionPerformed

    private void _btnCargarTBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarTBActionPerformed
        Seleccion sel = new Seleccion();
        new GUICargaTB(this, true, sel).setVisible(true);
        if (sel.selTB.equals(SeleccionTB.CARGA_FICHERO)) {
            SeleccionTBFich = true;
            cargaFicheroTB();
            if (this.com1 == null) {
                if (this.inicializarPuertoSerie()) {
                    ejec(false);
                }
            } else {
                ejec(false);
            }

        } else {
            if (sel.selTB.equals(SeleccionTB.CARGA_PANTALLA)) {
                SeleccionTBFich = false;
                cargarTextArea();
            }
        }

        this.jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event__btnCargarTBActionPerformed

    private void _btnClearActionPerformed(java.awt.event.ActionEvent evt) {

        if(jTabbedPane1.getComponentCount()>0)
        {
            javax.swing.JPanel panel = (javax.swing.JPanel) jTabbedPane1.getSelectedComponent();
            javax.swing.JScrollPane scrPanel = (javax.swing.JScrollPane) panel.getComponent(0);
            javax.swing.JViewport viewPort = (javax.swing.JViewport) scrPanel.getComponent(0);
            javax.swing.JTextArea txtArea = (javax.swing.JTextArea) viewPort.getComponent(0);
            txtArea.setText("");
        }
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

private void _btnGenerarGoldenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnGenerarGoldenActionPerformed
    generarGolden();
}//GEN-LAST:event__btnGenerarGoldenActionPerformed

private void _btnCargarGoldenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarGoldenActionPerformed


    JFileChooser chooser;
    this._TxtEntityVHD.setText("");
    chooser = new JFileChooser();
    Filtro filter = new Filtro("txt");
    chooser.addChoosableFileFilter(filter);
    chooser.setCurrentDirectory(new java.io.File("."));
    chooser.setDialogTitle("Seleccionar Archivo Golden");
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

        copiaArchivo(chooser.getSelectedFile().getAbsolutePath(), "test//Golden.txt");

    } else {
        System.out.println("No Selection ");
    }

}//GEN-LAST:event__btnCargarGoldenActionPerformed
/**
 *
 * @param inst
 */
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
    private javax.swing.JButton _btnCargarGolden;
    private javax.swing.JButton _btnCargarTB;
    private javax.swing.JButton _btnCargarVhd;
    private javax.swing.JButton _btnClear;
    private javax.swing.JButton _btnCrearBit;
    private javax.swing.JButton _btnEjecutar;
    private javax.swing.JButton _btnGenerarGolden;
    private javax.swing.JButton _btnPararEjecucion;
    private javax.swing.JButton _btnReanudar;
    private javax.swing.JLabel _lbl_BitCargado;
    private javax.swing.JLabel _lbl_VHDLCargado;
    private javax.swing.JTextField _lblnInst;
    private javax.swing.JTextArea _txtTB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
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

    private void cargarTextArea() {
        boolean error = false;
        JFileChooser chooser;

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

                int num_linea = 1;

                //Selecciona panel
                seleccionaPanel(panelTB);

                while (linea != null) {
                    if (num_linea == 280000) {
                        JOptionPane.showMessageDialog(this, "Sobrepasado el número máximo de líneas en este modo de TB. Sugerencia: Seleccione la otra opción para poder ejecutar el fichero por completo", "Error", JOptionPane.ERROR_MESSAGE);
                        br.close();
                        return;
                    }
                    this._txtTB.append(linea + "\n");
                    linea = br.readLine();
                    num_linea++;
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
    }

    private boolean cargaFicheroTB() {
        boolean error = false;
        JFileChooser chooser;



        this._txtTB.setText("");
        chooser =
                new JFileChooser();
        Filtro filter = new Filtro("txt");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Seleccionar TestBench");
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fichero_tb = chooser.getSelectedFile().getAbsolutePath();

            //Selecciona panel
            seleccionaPanel(panelTB);

            if (!error) {
                JOptionPane.showMessageDialog(this, "TestBench cargado correctamente", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar el fichero de test", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            System.out.println("No Selection ");
            error = true;
        }
        return !error;
    }

    private void seleccionaPanel(JPanel panel) {
        try {
            // TODO: mandar enable a la placa
            if ((Boolean) ((JTabbedPaneWithCloseIcon) jTabbedPane1).getTablaPaneles().get(panel)) {
                jTabbedPane1.setSelectedComponent(panel);
            } else {

                if (panel.getName().equals(panelOutPut)) {
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
                } else if (panel.getName().equals(panelCargar)) {
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
                } else if (panel.getName().equals(panelTB)) {
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

            }
        } catch (Exception ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // private JTabbedPaneWithCloseIcon jTabbedPane1;

  private boolean SeleccionTBModifFichero (){
        boolean correcto = false;
        SeleccionTBFich = true;
        if (cargaFicheroTB()){
            if (this.com1 == null) {
                if (this.inicializarPuertoSerie()) {
                    return generarGolden();
                }
            } else {
                return generarGolden();
            }
        }
        return correcto;
  }




}
