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

import cargarBit.CargaBit;
import compiladorEntidad.Errores;
import compiladorEntidad.SintacticoEntidad;



import IOFPGA.Ejecucion;
import IOFPGA.ReconfiguracionParcial;
import app.*;
//import com.sun.org.apache.bcel.internal.util.ClassPath;
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
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.log4j.*;

/**
 * Frame principal de la aplicación
 * @author Tony, David y Carlos.
 */
public class GUIPrincipal extends javax.swing.JFrame {
    //private EstadoContador estado_cont;

    /**
     * Fichero de log de la aplicación
     */
    private static Logger log = Logger.getLogger(GUIPrincipal.class);
    /**
     * Ruta en la que se encuentran los ficheros necesarios para la entrada/salida
     */
    private final static String RUTA_IOSERIE = System.getProperties().getProperty("user.dir") + "\\IOSerie";

    /*
     * Ruta en la que se encuentra el Xilinx. Será introducida por el usuario
     * o cargada mediante el fichero de configuración
     */
    private String RUTA_XILINX;
    /**
     * Parametros de configuración que se introducen al puerto serie
     */
    private Parameters param;
    /**
     * Interfaz para el manejo del puerto RS232
     */
    private Com com1;
    /**
     * Clase encargada de la ejecución de circuitos
     */
    private Ejecucion ejec;
    /**
     * Fichero VHD en el que está definida la entidad top que se desea ejecutar.
     */
    private String ficheroEntidadVHD;
    /**
     * Representación interna de la entidad que se quiere ejecutar
     */
    private Entidad entidad;
    /**
     * Número de fichero que representa el top dentro del conjunto de ficheros
     */
    private int top;
    /**
     * Conjunto de todos los ficheros VHD que pueden formar parte de un proyecto
     * a la hora de generar un .BIT
     */
    private ArrayList<File> ficherosVHD;
    /**
     * Indica que el menú de selección top se ha cerrado
     */
    private boolean cerradoTop;
    
    /**
     * Indica la ruta fichero de banco de pruebas
     */
    String fichero_tb;

    /**
     * Indica la ruta del fichero .bit que se desea cargar
     */
    String fichero_bit;

    /**
     * Indica si, para ejecutar, la selección de un banco de pruebas se obtiene
     * desde la pantalla o directamente desde un fichero.
     */
    private boolean SeleccionTBFich;

    /**
     * Lector de fichero para el banco de pruebas
     */
    private BufferedReader brTB;
    /**
     * Escritor de fichero para el log del proceso de reconfiguración
     */
    private FileWriter fwLog;
    
    /**
     * Proceso de reconfiguración
     */
    private ReconfiguracionParcial reconfiguracion;

    /** Constructor de la clase.
     *  Los botones reanudar y parar ejecución se ponen a no visibles.
     */
    public GUIPrincipal() {


        PropertyConfigurator.configure("conf/log4j.properties");

        config("conf/Config.properties", false);
        initComponentsAux();
        initComponents();
        this._btnReanudar.setEnabled(false);
        this._btnPararEjecucion.setEnabled(false);
        this.menuOpcionesReanudarEjec.setEnabled(false);
        this.menuOpcionesPararEjec.setEnabled(false);
        this.ficherosVHD = new ArrayList<File>();

        log.info("=====================================================");
        log.info("Inicializado Nessy 2.0");
        _btnPararReconf.setEnabled(false);
        _btnPararReconf.setVisible(false);
    }

    /**
     * Devuelve el Array de ficheros Vhdl Cargados en la aplicación.
     * @return Valor del atributo ficherosVHD
     */
    public ArrayList<File> getFiles() {
        return ficherosVHD;
    }

    /**
     * Establece el fichero Top
     * @param top Índice del archivo TOP.
     */
    public void setTop(int top) {
        this.top = top;
    }

    /**
     * Devuelve la Entidad con la que se está trabajado
     * @return Entidad con la que se está trabajando.
     */
    public Entidad getEntidad() {
        return entidad;
    }

    /**
     * Establece el campo cerradoTop, con el valor del argumento de la función.
     * @param cerradoTop Nuevo valor de tipo boolean de cerradoTop.
     */
    public void setCerradoTop(boolean cerradoTop) {
        this.cerradoTop = cerradoTop;
    }

    /**
     * Genera el archivo Golden.txt que será el fichero con el que compararemos nuestras salidas.
     * @return Cierto si todo ha sido correcto, falso si ha habido algún error.
     */
    public boolean generarGolden() {
        boolean correcto = true;
        if (this.ejec != null) {
            ejec.pararrecepcionfpga();
            this._TextSalida.setText("");
        }
        seleccionaPanel(panelOutPut);

        if (this.entidad != null) {//si la entidad está definida
            System.out.println("Generando salida golden");
            if (SeleccionTBFich) {
                try {
                    brTB = new BufferedReader(new FileReader(fichero_tb));
                } catch (FileNotFoundException ex) {
                    correcto = false;
                }
                this.ejec = new Ejecucion(this._lblnInst, this.entidad, this.com1, this._TextSalida, brTB, false, "Golden.txt", "Traza.txt", false);
                this.ejec.setCadena("");
                ejec.start();
                this._btnReanudar.setEnabled(false);
                this._btnPararEjecucion.setEnabled(true);
                this.menuOpcionesReanudarEjec.setEnabled(false);
                this.menuOpcionesPararEjec.setEnabled(true);
            } else {
                String ls_cadenaaejecutar = this._txtTB.getText();
                this.ejec = new Ejecucion(this._lblnInst, this.entidad, this.com1, this._TextSalida, false, "Golden.txt", "Traza.txt", false);
                this.ejec.setCadena(ls_cadenaaejecutar);
                if (ejec.convierteCadenas()) {
                    ejec.start();
                    //this.jTabbedPane1.setSelectedIndex(3);
                    this._btnReanudar.setEnabled(false);
                    this._btnPararEjecucion.setEnabled(true);
                    this.menuOpcionesReanudarEjec.setEnabled(false);
                    this.menuOpcionesPararEjec.setEnabled(true);
                } else {
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
     * Función para cargar el archivo .bit. Abre un JFileChooser para elegir el
     * archivo a cargar. Tras la elección intenta cargar el fichero de
     * configuración en la FPGA.
     * @return Cierto si todo ha sido correcto, falso si ha habido algún error.
     */
    public boolean cargarBitConChooser() {
        this._TextCargarbit.setText("Cargando .........." + "\n");
        boolean error = false;
        JFileChooser chooser;
        chooser = new JFileChooser();
        Filtro filter = new Filtro("bit");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Cargar BitStream");
        chooser.setAcceptAllFileFilterUsed(false);
        seleccionaPanel(panelCargar);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fichero_bit = chooser.getSelectedFile().getAbsolutePath();
            error = !this.cargarBit(fichero_bit, true);
        } else {
            System.out.println("Selecc ");
            this._TextCargarbit.setText("No ha seleccionado el .bit, puede que si no lo ha cargado con anterioridad la aplicación no funcione.");
            error = true;
        }
        return !error;
    }

    /**
     * Devuelve la ruta en la que se encuentra el fichero .BIT elegido por el
     * usuario
     * @return El valor del atributo fichero_bit
     */
    public String getFichero_bit() {
        return fichero_bit;
    }

    /**
     * Compila la entidad y genera el archivo VHDL a partir de ella de tal forma
     * que se interconecte con el módulo de entrada/salida.
     * @return Cierto si todo ha sido correcto, falso si ha habido algún error.
     */
    public boolean compilarEntidad() {
        boolean correcto = true;
        SintacticoEntidad compilador = null;
        Errores errores = new Errores();
        GeneraVhdl generador;

        try {
            compilador = new SintacticoEntidad(ficheroEntidadVHD, errores);
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

    /**
     * Función que inicializa el puerto serie necesario para la comuncicación 
     * con la FPGA. Comprueba que esté libre el puerto y que la maquina sobre
     * la que estamos ejecutando tenga el puerto COM1.
     * @return Cierto si todo ha sido correcto, falso si ha habido algún error.
     */
    public boolean inicializarPuertoSerie() {
        boolean correcto = true;
        try {
            param = new Parameters();
            param.setPort("COM1");
            param.setBaudRate("9600");
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
            log.info("La aplicacion ya se encuentra ejecutandose" + ex);
            System.exit(0);
        }
        return correcto;
    }

    /**
     * Trata de compilar un fichero VHD y muestra un mensaje con lo
     * ocurrido en una ventana emergente
     */
    private void cargarVHDL() {
        boolean error = !compilarEntidad();
        if (!error) {
            seleccionaPanel(panelVHD);
            this._TxtEntityVHD.setText(this.entidad.toString());
            JOptionPane.showMessageDialog(this, "Entity cargada correctamente", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            log.error("Error al cargar VHDL");
            JOptionPane.showMessageDialog(this, "Error al cargar el fichero de la entity", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Carga un fichero VHD como top
     */
    private void cargaTopVHDL() {
        JFileChooser chooser;
        this._TxtEntityVHD.setText("");
        chooser = new JFileChooser();
        Filtro filter = new Filtro("vhd");
        chooser.addChoosableFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Seleccionar Archivo VHDL");
        chooser.setAcceptAllFileFilterUsed(false);
        ficherosVHD = new ArrayList<File>();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            deshabilitarBtnYmenu();
            ficherosVHD.add(chooser.getSelectedFile());
            ficheroEntidadVHD = ficherosVHD.get(0).getAbsolutePath();
            _lbl_VHDLCargado.setText("Ultimo Top VHDL cargado : "
                    + chooser.getSelectedFile().getName());
            this.cargarVHDL();
        } else {
            log.info("Seleccion no llevada a cabo");
        }
    }

    /**
     * Carga varios ficheros VHDL de los cuales elegirá uno como top
     */
    private void cargaVariosVHDL() {
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
            deshabilitarBtnYmenu();
            ArrayList<String> ficheros = new ArrayList<String>();
            File[] f = chooser.getSelectedFiles();
            ficherosVHD = new ArrayList<File>();
            for (int i = 0; i < f.length; i++) {
                ficherosVHD.add(f[i]);
            }

            for (int i = 0; i < f.length; i++) {
                ficheros.add(f[i].getName());
            }
            cerradoTop = false;
            GUISeleccionTop selTop = new GUISeleccionTop(this, true, ficheros);
            selTop.setVisible(true);
            if (!cerradoTop) {
                ficheroEntidadVHD = ficherosVHD.get(top).getAbsolutePath(); //el fichero es el absoluto
                _lbl_VHDLCargado.setText("Ultimo Top VHDL cargado : "
                        + ficherosVHD.get(top).getName());
                this.cargarVHDL();
            }

        }
    }

    /**
     * Copia un fichero en otro
     * @param fich_lectura Fichero a copiar
     * @param fich_escritura Fichero a crear
     */
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
            ex.printStackTrace();
        }
    }

    /**
     * Crea el fichero (.PRJ)necesario para generar un .BIT con los comandos de
     * Xilinx. En concreto este fichero es necesario para el proceso de síntesis
     * implementado en XST de Xilinx.
     */
    private void creaPrj() {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("IOSerie//Circuito_FPGA.prj"));
            bw.write("vhdl work \"Tx_serie.vhd\"\n");
            bw.write("vhdl work \"Rx_serie.vhd\"\n");
            for (int i = 0; i < ficherosVHD.size(); i++) {
                bw.write("vhdl work \"" + ficherosVHD.get(i).getAbsolutePath() + "\"\n");
            }
            bw.write("vhdl work \"Circuito_FPGA.vhd\"");
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Realiza la ejecución de un circuito. Para ello lee las entradas del banco
     * de pruebas, las cuales pueden ser ofrecidas desde la pantalla de la
     * aplicación o directamente desde fichero. A continuación comprueba que
     * el formato de las entradas es correcto (están compuestas por 0's y 1's
     * de la misma longitud que el número de entradas que la entidad cargada).
     * Si la ejecución actual no pertence a uno de los pasos de la reconfiguración
     * parcial, entonces por cada ejecución se lanzará un nuevo hilo, de tal forma
     * que podamos visualizar la salida a medida que se va produciendo. Además
     * este hilo podrá ser detenido en cualquier momento y reanudado más adelante.
     * Si por el contrario sí pertenece a la reconfiguración parcial, no queremos
     * que nada más ocurra mientras se está ejecutando, para evitar así que por
     * ejemplo se cargue el próximo .BIT mientras se está aun ejecutando con el
     * anterior. Por lo tanto no se lanzará un nuevo hilo sino que se hará de
     * forma secuencial.
     * @param lb_reconfiguracionParcial Indica si pertence a un paso de la
     * reconfiguración parcial
     */
    public boolean ejec(boolean lb_reconfiguracionParcial) {

        if (this.ejec != null) {
            ejec.pararrecepcionfpga();
            this._TextSalida.setText("");
        }
        //Selecciona panel
        seleccionaPanel(panelOutPut);

        if (this.entidad != null) {//si la entidad está definida
            if (SeleccionTBFich) {
                try {
                    brTB = new BufferedReader(new FileReader(fichero_tb));

                } catch (FileNotFoundException ex) {
                }
                this.ejec = new Ejecucion(this._lblnInst, this.entidad, this.com1, this._TextSalida, brTB, true, "Salida.txt", "Golden.txt", lb_reconfiguracionParcial);
                if (ejec.formatoCorrectoFicheroTB(fichero_tb)) {
                    this.ejec.setCadena("");
                    if (!lb_reconfiguracionParcial) {
                        ejec.start();
                    } else {
                        ejec.setFileLogEjec(fwLog);
                        //ejec.ejecuta();
                        ejec.start();
                        try {
                            ejec.join();
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(GUIPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }
                    this._btnReanudar.setEnabled(false);
                    this._btnPararEjecucion.setEnabled(true);
                    this.menuOpcionesReanudarEjec.setEnabled(false);
                    this.menuOpcionesPararEjec.setEnabled(true);
                    return true;

                } else {
                    JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n" + "Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a " + Integer.toString(this.getEntidad().getBitsEntrada()) + " .", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                String ls_cadenaaejecutar = this._txtTB.getText();
                this.ejec = new Ejecucion(this._lblnInst, this.entidad, this.com1, this._TextSalida, true, "Salida.txt", "Golden.txt", lb_reconfiguracionParcial);
                this.ejec.setCadena(ls_cadenaaejecutar);
                if (ejec.convierteCadenas()) {
                    if (!lb_reconfiguracionParcial) {
                        ejec.start();
                    }
                    this._btnReanudar.setEnabled(false);
                    this._btnPararEjecucion.setEnabled(true);
                    this.menuOpcionesReanudarEjec.setEnabled(false);
                    this.menuOpcionesPararEjec.setEnabled(true);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Error en el formato del banco de pruebas, revíselo por favor.\n" + "Sugerencia: se deben pasar cadenas de bits 0's y 1's de longitud igual a " + Integer.toString(this.getEntidad().getBitsEntrada()) + " .", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "La entidad no está definida", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

    }

    /**
     * Establece el fichero de escritura para el fichero de log del proceso de
     * reconfiguracion
     * @param fw
     */
    public void setFwLog(FileWriter fw) {
        this.fwLog = fw;
    }

    private void initComponentsAux() {
        jTabbedPane1 = new JTabbedPaneWithCloseIcon();
    }


    /**
     * Habilita o deshabilita el boton Detener Inyeccion de errores.
     */
    public void setEnabledBtnDetenerInyeccion(boolean b)
    {
            _btnPararReconf.setEnabled(b);
            _btnPararReconf.setVisible(b);
    }

    /**
     * Muestra los errores obtenidos en el proceso de compilación de la entidad
     * por la pestaña de EntityVHD.
     * @param errores
     */
    private void muestraErroresConsola(Errores errores) {
        this._TxtEntityVHD.setText("");
        for (int i = 0; i < errores.getErrores().size(); i++) {
            this._TxtEntityVHD.append(errores.getErrores().get(i) + "\n");
        }
    }

   
    /**
     * Escribe una cadena que se le pasa como argumento en el text Area correspondiente al proceso
     * de cargar un archivo .bit en la FPGA
     * @param str Cadena a escribir.
     */
    public void escribirEnPantalla(String str) {
        this._TextCargarbit.append(str + "\n");
    }

    /**
     * Carga un fichero .BIT utilizando la interfaz de carga. Realiza 6 intentos
     * de carga por hubiera algún error ajeno a la aplicación que impidiera
     * su carga. Si no estamos en el proceso de reconfiguración parcial, se
     * mostrará un mensaje indicando que la carga del bitstream se ha producido
     * correctamente.
     * @param fichero_bit El fichero a cargar
     * @param ab_mostrar_mensajes Indica si hay que mostrar mensajes de
     * información de lo ocurrido.
     * @return true si ha sido correcta la ejecucion y false en caso contrario
     */
    public boolean cargarBit(String fichero_bit, boolean ab_mostrar_mensajes) {
        boolean error = false;
        int intentos = 2;
        CargaBit cargaBit = new CargaBit(this, fichero_bit, this.RUTA_XILINX + "\\ISE\\bin\\nt\\impact.exe");
        try {
            do {//si hay un error lo vuelve a intentar
                error = !cargaBit.cargar(ab_mostrar_mensajes);               
                if (!error) {
                    if (ab_mostrar_mensajes) {
                        JOptionPane.showMessageDialog(this, "Bitstream cargado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                    if (com1 != null) {
                        com1.close();
                        com1 = null;
                    }
                } else {
                    intentos--;
                }
            } while (error && intentos > 0);//intenta cargar 6 veces el .bit
        } catch (Exception e) {
            error = true;
        }
        if (!error) {
            _lbl_BitCargado.setText("Ultimo Archivo .BIT cargado : "
                    + fichero_bit);
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
        _btnCargarBit = new javax.swing.JButton();
        _btnCargarTB = new javax.swing.JButton();
        _btnEjecutar = new javax.swing.JButton();
        _btnPararEjecucion = new javax.swing.JButton();
        _btnReanudar = new javax.swing.JButton();
        _btnGenerarGolden = new javax.swing.JButton();
        _btnCargarGolden = new javax.swing.JButton();
        _btnCargBitReconfParcial = new javax.swing.JButton();
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
        _btnPararReconf = new javax.swing.JButton();
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
        menuOpcionesGeneraGolden = new javax.swing.JMenuItem();
        menuOpcionesCargarGolden = new javax.swing.JMenuItem();
        menuOpcionesReconfParcial = new javax.swing.JMenuItem();
        menuVistas = new javax.swing.JMenu();
        menuVistasEntityVHD = new javax.swing.JMenuItem();
        menuVistasCargar = new javax.swing.JMenuItem();
        menuVistasTB = new javax.swing.JMenuItem();
        menuVistasOutPut = new javax.swing.JMenuItem();
        menuConfig = new javax.swing.JMenu();
        menuConfigNessy = new javax.swing.JMenuItem();
        menuConfigFichConf = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nessy 2.0");
        setIconImage(new ImageIcon("src/recursos/Nessy.png").getImage());
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
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
        _btnCrearBit.setEnabled(false);
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
        _btnCargarBit.setEnabled(false);
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
        _btnCargarTB.setText("Cargar Test Bench");
        _btnCargarTB.setEnabled(false);
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
        _btnEjecutar.setEnabled(false);
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
        _btnPararEjecucion.setEnabled(false);
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
        _btnReanudar.setEnabled(false);
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
        _btnGenerarGolden.setEnabled(false);
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
        _btnCargarGolden.setEnabled(false);
        _btnCargarGolden.setFocusable(false);
        _btnCargarGolden.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCargarGolden.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCargarGolden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCargarGoldenActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCargarGolden);

        _btnCargBitReconfParcial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/reconParc.JPG"))); // NOI18N
        _btnCargBitReconfParcial.setText("Inyección de Errores");
        _btnCargBitReconfParcial.setEnabled(false);
        _btnCargBitReconfParcial.setFocusable(false);
        _btnCargBitReconfParcial.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnCargBitReconfParcial.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnCargBitReconfParcial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCargBitReconfParcialActionPerformed(evt);
            }
        });
        jToolBar1.add(_btnCargBitReconfParcial);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 862, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Pantalla"));
        jPanel1.setOpaque(false);

        _btnClear.setText("Limpiar Pantalla");
        _btnClear.setAutoscrolls(true);
        _btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnClearActionPerformed(evt);
            }
        });

        _lblnInst.setEditable(false);

        jLabel1.setText("Ciclo de Ejecución");
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

        jTabbedPane1.addTab("Test Bench", panelTB);

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

        _btnPararReconf.setText("Detener Inyección de Errores");
        _btnPararReconf.setEnabled(false);
        _btnPararReconf.setVisible(false);
        _btnPararReconf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnPararReconfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_lbl_BitCargado, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                    .addComponent(_lbl_VHDLCargado, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(469, 469, 469))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(217, 217, 217)
                .addComponent(_btnPararReconf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 281, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(_lblnInst, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(78, 78, 78))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 836, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(433, Short.MAX_VALUE)
                    .addComponent(_btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(301, 301, 301)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_lblnInst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(_btnPararReconf))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 373, Short.MAX_VALUE)
                .addComponent(_lbl_VHDLCargado, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_lbl_BitCargado, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(_btnClear)
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
        menuOpcionesCrearBit.setEnabled(false);
        menuOpcionesCrearBit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCrearBitActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCrearBit);

        menuOpcionesCargarBit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCargarBit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCargarBit.png"))); // NOI18N
        menuOpcionesCargarBit.setText("Cargar .Bit");
        menuOpcionesCargarBit.setEnabled(false);
        menuOpcionesCargarBit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCargarBitActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCargarBit);

        menuOpcionesCargarTB.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCargarTB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCargarTB.png"))); // NOI18N
        menuOpcionesCargarTB.setText("Cargar Test Bench");
        menuOpcionesCargarTB.setEnabled(false);
        menuOpcionesCargarTB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCargarTBActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCargarTB);

        menuOpcionesEjec.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesEjec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuEjec.png"))); // NOI18N
        menuOpcionesEjec.setText("Ejecutar");
        menuOpcionesEjec.setEnabled(false);
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

        menuOpcionesGeneraGolden.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesGeneraGolden.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuGeneraGolden.png"))); // NOI18N
        menuOpcionesGeneraGolden.setText("Generar Golden");
        menuOpcionesGeneraGolden.setEnabled(false);
        menuOpcionesGeneraGolden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesGeneraGoldenActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesGeneraGolden);

        menuOpcionesCargarGolden.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesCargarGolden.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menuCargaGolden.jpg"))); // NOI18N
        menuOpcionesCargarGolden.setText("Cargar Golden");
        menuOpcionesCargarGolden.setEnabled(false);
        menuOpcionesCargarGolden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesCargarGoldenActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesCargarGolden);

        menuOpcionesReconfParcial.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        menuOpcionesReconfParcial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/menureconParc.JPG"))); // NOI18N
        menuOpcionesReconfParcial.setText("Inyección de Errores");
        menuOpcionesReconfParcial.setEnabled(false);
        menuOpcionesReconfParcial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpcionesReconfParcialActionPerformed(evt);
            }
        });
        menuOpciones.add(menuOpcionesReconfParcial);

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
        menuVistasTB.setText("Test Bench");
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

        menuConfig.setText("Configuración");

        menuConfigNessy.setText("Configurar Nessy");
        menuConfigNessy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuConfigNessyActionPerformed(evt);
            }
        });
        menuConfig.add(menuConfigNessy);

        menuConfigFichConf.setText("Cargar Fichero Configuración");
        menuConfigFichConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuConfigFichConfActionPerformed(evt);
            }
        });
        menuConfig.add(menuConfigFichConf);

        jMenuBar1.add(menuConfig);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 940, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            _btnCrearBit.setEnabled(true);
            _btnCargarBit.setEnabled(true);
            _btnCargarTB.setEnabled(true);
            _btnCargBitReconfParcial.setEnabled(true);
            this.menuOpcionesCrearBit.setEnabled(true);
            this.menuOpcionesCargarBit.setEnabled(true);
            this.menuOpcionesCargarTB.setEnabled(true);
            this.menuOpcionesReconfParcial.setEnabled(true);

        } else {
            if (sel.seleccion.equals(SeleccionCargaVHD.SELECCION_VARIOS_VHDL)) {
                cargaVariosVHDL();
                _btnCrearBit.setEnabled(true);
                _btnCargarBit.setEnabled(true);
                _btnCargarTB.setEnabled(true);
                _btnCargBitReconfParcial.setEnabled(true);
                this.menuOpcionesCrearBit.setEnabled(true);
                this.menuOpcionesCargarBit.setEnabled(true);
                this.menuOpcionesCargarTB.setEnabled(true);
                this.menuOpcionesReconfParcial.setEnabled(true);
            }

            else
            {
                if (sel.seleccion.equals(SeleccionCargaVHD.NADA))
                {}

            }
        }
        
    }//GEN-LAST:event__btnCargarVhdActionPerformed

    private void _btnCrearBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCrearBitActionPerformed
        try {
            JFileChooser chooser;
            chooser = new JFileChooser();
            Filtro filter = new Filtro("bit");
            chooser.addChoosableFileFilter(filter);
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Guardar el .BIT");
            chooser.setAcceptAllFileFilterUsed(false);
            String rutaDestino;
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaDestino = chooser.getSelectedFile().getAbsolutePath();
                if (rutaDestino.lastIndexOf(".bit") + 4 != rutaDestino.length()) {
                    rutaDestino = rutaDestino + ".bit";
                }
                //creamos el prj para poder crear el .bit
                this.creaPrj();
                //compilación y creación del .bit
                Process p = Runtime.getRuntime().exec("cmd.exe /C start comandosXilinx\\compilar.bat " + this.RUTA_XILINX + " " + rutaDestino);
                //Process copiar = Runtime.getRuntime().exec("cmd.exe /C start comandosXilinx\\copiararchivo.bat " + this.RUTA_XILINX);
            }
        } catch (IOException ex) {
            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.FATAL, null, ex);
        }

    }//GEN-LAST:event__btnCrearBitActionPerformed

    private void _btnCargarBitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarBitActionPerformed

        //POsibilidad de ver si se carga con exito
        if (this.cargarBitConChooser()) {
            log.info("Cargar .BIT : Cargado archivo .bit correctamente");
        } else {
            log.warn("Cargar .BIT : No se ha podido Cargar el archivo .bit correctamente");
        }

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

        seleccionaPanel(panelOutPut);

        int longitud = this.entidad.getBitsEntrada();
        System.out.println("PARANDO EL HILO..");
        this.ejec.setSetwait(true);
        this._btnReanudar.setEnabled(true);
        this._btnPararEjecucion.setEnabled(false);
        this.menuOpcionesReanudarEjec.setEnabled(true);
        this.menuOpcionesPararEjec.setEnabled(false);

    }//GEN-LAST:event__btnPararEjecucionActionPerformed

    private void _btnReanudarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnReanudarActionPerformed

        seleccionaPanel(panelOutPut);

        synchronized (this.ejec) {
            this.ejec.notify();
        }
        this._btnReanudar.setEnabled(false);
        this._btnPararEjecucion.setEnabled(true);
        this.menuOpcionesReanudarEjec.setEnabled(false);
        this.menuOpcionesPararEjec.setEnabled(true);

    }//GEN-LAST:event__btnReanudarActionPerformed

    private void _btnCargarTBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargarTBActionPerformed
        Seleccion sel = new Seleccion();
        new GUICargaTB(this, true, sel).setVisible(true);
        if (sel.selTB.equals(SeleccionTB.CARGA_FICHERO)) {
            SeleccionTBFich = true;
            cargaFicheroTB();
            if (this.com1 == null) {
                if (this.inicializarPuertoSerie()) {
                    if(ejec(false))
                        habilitarBtnsCargarTB();
                }
            } else {
                if(ejec(false))
                    habilitarBtnsCargarTB();
            }
            

        } else {
            if (sel.selTB.equals(SeleccionTB.CARGA_PANTALLA)) {
                SeleccionTBFich = false;
                cargarTextArea();
                habilitarBtnsCargarTB();
            }
        }

        
    }//GEN-LAST:event__btnCargarTBActionPerformed

    private void _btnClearActionPerformed(java.awt.event.ActionEvent evt) {

        if (jTabbedPane1.getComponentCount() > 0) {
            javax.swing.JPanel panel = (javax.swing.JPanel) jTabbedPane1.getSelectedComponent();
            javax.swing.JScrollPane scrPanel = (javax.swing.JScrollPane) panel.getComponent(0);
            javax.swing.JViewport viewPort = (javax.swing.JViewport) scrPanel.getComponent(0);
            javax.swing.JTextArea txtArea = (javax.swing.JTextArea) viewPort.getComponent(0);
            txtArea.setText("");
        }
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        ejec.pararrecepcionfpga();

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

            jTabbedPane1.addTab("Test Bench", panelTB);
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
    if (this.com1 == null) {
        if (this.inicializarPuertoSerie()) {
            if (generarGolden()) {
                log.info("Generar Golden : Generado correctamente el archivo golden");
            } else {
                log.warn("Generar Golden : No se ha podido generar correctamente el archivo golden");
            }
        }
    } else {
        if (generarGolden()) {
            log.info("Generar Golden : Generado correctamente el archivo golden");
        } else {
            log.warn("Generar Golden : No se ha podido generar correctamente el archivo golden");
        }
    }
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

        copiaArchivo(chooser.getSelectedFile().getAbsolutePath(), "salidas//Golden.txt");

        log.info("Cargar Golden : Se ha cargado archivo golden correctamente");
    } else {
        log.info("Cargar Golden : No se ha seleccionado archivo");
    }

}//GEN-LAST:event__btnCargarGoldenActionPerformed

private void menuConfigNessyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuConfigNessyActionPerformed
    String ruta = "";
    if (RUTA_XILINX != null) {
        ruta = RUTA_XILINX;
    }
    GUIConfig config = new GUIConfig(this, true, ruta);
    config.setVisible(true);

}//GEN-LAST:event_menuConfigNessyActionPerformed

private void menuConfigFichConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuConfigFichConfActionPerformed

    JFileChooser chooser;
    chooser = new JFileChooser();
    Filtro filter = new Filtro("properties");
    chooser.addChoosableFileFilter(filter);
    chooser.setCurrentDirectory(new java.io.File("."));
    chooser.setDialogTitle("Seleccionar Archivo Configuración");
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setMultiSelectionEnabled(false);
    ficherosVHD = new ArrayList<File>();
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        config(chooser.getSelectedFile().getAbsolutePath(), true);

    }

}//GEN-LAST:event_menuConfigFichConfActionPerformed

private void _btnCargBitReconfParcialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCargBitReconfParcialActionPerformed
    

    /* if(this.reconfiguracion != null){
    this.reconfiguracion.pararreconfiguracionparcial();
    }*/
    seleccionaPanel(panelOutPut);
    reconfiguracion = new ReconfiguracionParcial(this, RUTA_IOSERIE);
    reconfiguracion.start();
    //setEnabledBtnDetenerInyeccion(false);
    /* if(procesoModificarFicheros())
    log.info("Reconfiguración Parcial : Ejecutado Reconfiguración Parcial");
    else
    log.warn("Reconfiguración Parcial :No se ha podido ejecutar " +
    "correctamente Reconfiguración Parcial");*/

}//GEN-LAST:event__btnCargBitReconfParcialActionPerformed

private void _btnPararReconfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnPararReconfActionPerformed
    this.reconfiguracion.pararreconfiguracionparcial();
    this._btnPararReconf.setEnabled(false);
    this._btnPararReconf.setVisible(false);
    //this.ejecutandoReconfiguracion = false;
}//GEN-LAST:event__btnPararReconfActionPerformed

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

    log.info("Finalizado Nessy 2.0");
    log.info("=====================================================\n");
}//GEN-LAST:event_formWindowClosing

private void menuOpcionesGeneraGoldenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesGeneraGoldenActionPerformed
    _btnGenerarGoldenActionPerformed(evt);
}//GEN-LAST:event_menuOpcionesGeneraGoldenActionPerformed

private void menuOpcionesCargarGoldenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesCargarGoldenActionPerformed
    _btnCargarGoldenActionPerformed(evt);
}//GEN-LAST:event_menuOpcionesCargarGoldenActionPerformed

private void menuOpcionesReconfParcialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpcionesReconfParcialActionPerformed
    _btnCargBitReconfParcialActionPerformed(evt);
}//GEN-LAST:event_menuOpcionesReconfParcialActionPerformed
    /**
     * Actualiza el numero de instrucción que se está ejecutando.
     * @param inst Número de instruccion actual.
     */
    public void setNumeroInst(int inst) {
        this._lblnInst.setText(Integer.toString(inst));
    }
    /**
     * 
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea _TextCargarbit;
    private javax.swing.JTextArea _TextSalida;
    private javax.swing.JTextArea _TxtEntityVHD;
    private javax.swing.JButton _btnCargBitReconfParcial;
    private javax.swing.JButton _btnCargarBit;
    private javax.swing.JButton _btnCargarGolden;
    private javax.swing.JButton _btnCargarTB;
    private javax.swing.JButton _btnCargarVhd;
    private javax.swing.JButton _btnClear;
    private javax.swing.JButton _btnCrearBit;
    private javax.swing.JButton _btnEjecutar;
    private javax.swing.JButton _btnGenerarGolden;
    private javax.swing.JButton _btnPararEjecucion;
    private javax.swing.JButton _btnPararReconf;
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
    private javax.swing.JMenu menuConfig;
    private javax.swing.JMenuItem menuConfigFichConf;
    private javax.swing.JMenuItem menuConfigNessy;
    private javax.swing.JMenu menuOpciones;
    private javax.swing.JMenuItem menuOpcionesCargarBit;
    private javax.swing.JMenuItem menuOpcionesCargarGolden;
    private javax.swing.JMenuItem menuOpcionesCargarTB;
    private javax.swing.JMenuItem menuOpcionesCargarVHD;
    private javax.swing.JMenuItem menuOpcionesCrearBit;
    private javax.swing.JMenuItem menuOpcionesEjec;
    private javax.swing.JMenuItem menuOpcionesGeneraGolden;
    private javax.swing.JMenuItem menuOpcionesPararEjec;
    private javax.swing.JMenuItem menuOpcionesReanudarEjec;
    private javax.swing.JMenuItem menuOpcionesReconfParcial;
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
//                Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);


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

        } else {
            log.info("Seleccion TB no realizada ");
            error = true;
        }
        return !error;
    }

    /**
     * Selecciona uno de las cuatro pestañas según lo pasado por parámetro
     * @param panel Panel al que se quiere cambiar
     */
    public void seleccionaPanel(JPanel panel) {
        try {
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

                    jTabbedPane1.addTab("Test Bench", panelTB);
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
            log.error("Selecciona Panel() : " + ex);
//            Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Abre un selector de ficheros para seleccionar un test bench y a continuación
     * genera una salida golden a partir de él. Utilizado para el comienzo
     * del proceso de reconfiguración parcial
     * @return true si ha sido correcto y false en caso contrario
     */
    public boolean SeleccionTBModifFichero() {
        boolean correcto = false;
        SeleccionTBFich = true;
        if (cargaFicheroTB()) {
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

    /**
     * Vuelve a cargar la configuracón de la aplicación
     * @param fichConf El fichero de configuración
     * @param cargaFich Indica si hay que carga desde fichero
     */
    private void config(String fichConf, boolean cargaFich) {

        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(fichConf);
            prop.load(is);
            // Enumeration enume=prop.propertyNames();

            RUTA_XILINX = prop.getProperty("HomeXilinx");
            if (cargaFich) {
                if (RUTA_XILINX == null || RUTA_XILINX.equals("")) {
                    JOptionPane.showMessageDialog(this, ""
                            + "El fichero de configuración seleccionado "
                            + "no es valido", "Info", JOptionPane.INFORMATION_MESSAGE);
                }


            } else {
                boolean cierre=false;
                while ((RUTA_XILINX == null || RUTA_XILINX.equals(""))&& !cierre) {
                    GUIConfig config = new GUIConfig(this, true, "");
                    config.setVisible(true);
                    is = new FileInputStream(fichConf);
                    prop.load(is);
                    RUTA_XILINX = prop.getProperty("HomeXilinx");
                    cierre=config.getCierre();
                }
            }

        } catch (IOException ioe) {

             JOptionPane.showMessageDialog(this, "No ha sido posible configurar" +
                     " nessy, debido a que no se ha encontrado archivo de " +
                     "configuración, para poder ejecutar Nessy correctamente" +
                     " acceda a configuración.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private void deshabilitarBtnYmenu() {
        _btnCrearBit.setEnabled(false);
        _btnCargarBit.setEnabled(false);
        _btnEjecutar.setEnabled(false);
        _btnPararEjecucion.setEnabled(false);
        _btnReanudar.setEnabled(false);
        _btnGenerarGolden.setEnabled(false);
        _btnCargarGolden.setEnabled(false);
        _btnCargarTB.setEnabled(false);
        _btnCargBitReconfParcial.setEnabled(false);

        menuOpcionesCrearBit.setEnabled(false);
        menuOpcionesCargarBit.setEnabled(false);
        menuOpcionesEjec.setEnabled(false);
        menuOpcionesPararEjec.setEnabled(false);
        menuOpcionesReanudarEjec.setEnabled(false);
        menuOpcionesGeneraGolden.setEnabled(false);
        menuOpcionesCargarGolden.setEnabled(false);
        menuOpcionesCargarTB.setEnabled(false);
        menuOpcionesReconfParcial.setEnabled(false);

    }

    /**
     * Obtiene el interfaz de puerto serie
     * @return El valor del atributo com1
     */
    public Com getCom1() {
        return com1;
    }

    private void habilitarBtnsCargarTB() {
        _btnEjecutar.setEnabled(true);
        _btnCargarGolden.setEnabled(true);
        _btnGenerarGolden.setEnabled(true);
        this.menuOpcionesEjec.setEnabled(true);
        this.menuOpcionesCargarGolden.setEnabled(true);
        this.menuOpcionesGeneraGolden.setEnabled(true);
    }

    public int dameNumIter() {
        Seleccion sel = new Seleccion();
        new GUISelNumIter(this, true, sel).setVisible(true);
        if (sel.selIter.equals(SeleccionNumIter.CANCELAR)) {
            return 0;
        } else if (sel.selIter.equals(SeleccionNumIter.OK)) {
            return sel.numIter;
    }
        return 0;
    }
}
