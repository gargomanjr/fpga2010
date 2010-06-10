/*
 * 
 * 
 */
package IOFPGA;

import app.Com;
import compiladorEntidad.Entidad;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Carlos
 *
 * Clase encargada de hacer que se ejecute correctamente el circuito cargado
 * en la FPGA. Contiene los métodos necesarios para el envío y la recepción de
 * datos cn dicho dispositivo. La ejecución se hará a través de un Test Bench o
 * Banco de Pruebas que podrá leerse bien desde fichero, o bien desde la pantalla
 * de la aplicación (pestaña TestBench).
 * Hereda de la clase Thread puesto que en ocasiones queremos que la ejecución
 * se realice en paralelo con la escritura en la interfaz gráfica, operaciones que
 * no pueden ser realizadas a la vez si no es en hilos diferentes.
 */
public class Ejecucion extends Thread {

    /**
     * Estructura para almacenar las cadenas del banco de pruebas cuando éste se
     * ha introducido por pantalla.
     */
    private ArrayList<String> cadenaaEnviar;

    /**
     * Indica que el hilo de ejecución se encuentra activo cuando está a true
     */
    private boolean ejecutando;

    /**
     * Objeto para controlar al puerto serie RS232
     */
    private Com com1;

    /**
     * Almacena en un solo String todas las cadenas leidas de la pantalla de
     * texto del Test Bench
     */
    private String ls_cadenaaejecutar;

    /**
     * Indica el número de bits de entrada que tiene la entidad sobre la que se va a
     * ejecutar
     */
    private int li_bits_entrada;

    /**
     * Indica el número de bits de salida que tiene la entidad sobre la que se va a
     * ejecutar
     */
    private int li_bits_salida;

    /**
     * Elemento gráfico sobre el que se desea escribir el número de instruccion
     * mientras se ejecuta el  hilo de ejecución
     */
    private final JTextField ljtfield;

    /**
     * Indica si se ha ordenado al hilo correspondiente a esta ejecución
     * que espere.
     */
    private boolean setwait;

    /**
     * Elemento gráfico sobre el que se desea escribir la salida de la ejecución
     * mientras se ejecuta este hilo
     */
    private JTextArea ata_textarea;

    /**
     * Indica a la ejecución que el banco de pruebas hay que leerlo desde fichero
     */
    private boolean entraDesdeFichero;

    /**
     * Lectura de ficheros
     */
    private BufferedReader bfr;

    /**
     * Ruta donde serán almacenados los ficheros correspondientes a la ejecución
     * tales como la salida de la última ejecución y la salida golden.
     */
    static String rutaficherosSalida = System.getProperties().getProperty("user.dir") + "\\salidas";

    /**
     * Fichero donde se volcará la salida
     */
    private final File fichero_salida;

    /**
     * Fichero con el cual se comparará la ejecución
     */
    private final File fichero_compararTraza;

    /**
     * Fichero para la escritura de lo que va sucediendo
     * durante la ejecución. Esto será útil a la hora
     * de visualizar lo sucedido en la reconfiguración
     * parcial.
     */
    private FileWriter ficheroLogEjec;

    /**
     * Indica si la ejecución coincide con la traza almacenada
     */
    private boolean coincideTraza;

    /**
     * Indica si hay que mostrar mensajes con pantallas modales al ejecutar ya que
     * en el caso de reconfigurar varios ficheros, no querremos ninguna pantalla
     * que se nos muestre entre ejecuciones.
     */
    private boolean mostrarMensaje;

    /**
     * Almacena la primera instrucción en la que no coincide la traza
     */
    private int NumInstrNoCoincideTraza;

    /**
     * Indica si en la ejecuición hay que comparar o no con la traza. Por ejemplo,
     * cuando se está generando la salida golden no hay que hacer ninguna comparación.
     */
    private boolean comparar;

    /**
     * Indica si se trata de una ejecución especial, que se da cuando estamos
     * realizando ejecuciones sucesivas cambiando un solo bit de la FPGA. Para esos
     * casos no querremos que la ejecución sea un hilo diferente ni que ninguna
     * pantalla se nos muestre. Simplemente querremos volcar la salida a un fichero
     * y guardar el resultado del método en un fichero de log.
     */
    private boolean reconfiguracionParcial;

    /**
     * Indica la posición en la que se encuentra el bit de reset en la entidad que
     * se está ejecutando. La razón de almacenar esto es la de tener la posibilidad
     * de resetear el circuito al comenzar cada ejecución. Para ello mandaremos una
     * cadena de todo 0's excepto un 1 en la posición del reset
     */
    private int posicionResetEntidad;

    /**
     * Crea un objeto de la clase Ejecución y escribe el resultado en un JTextField . Este constructor
     * está definido para el caso en el que el test bench es leido desde la pantalla de la GUI
     * @param lj_jtf JTextField en el que se escribirá la salida que reciba de la FPGA al ejecutar las instrucciones.
     * @param e Entidad (top) sobre la que vamos a ejecutar.
     * @param ac_com Puerto con el que nos comunicamos con la FPGA.
     * @param ata_textarea TextArea de la aplicación del cual se recogerán las instrucciones a ejecutar.
     * @param comparar Booleano que indica si hay que comparar los ficheros de traza y el de salida, para detectar diferencias.
     * @param nombreSalida Fichero en el que se guardará el resultado de la ejecución.
     * @param nombreTraza Fichero de traza con el que se podrá comparar la salida que está generando este objeto.
     * @param ab_reconfiguracionParcial Boolean que indica si estamos ejecutando con la opción de reconfiguración parcial.
     *  Si es falso se ejecutará el hilo directamente y no mostrará mensajes emergentes.
     */
    public Ejecucion(JTextField lj_jtf, Entidad e, Com ac_com, JTextArea ata_textarea, boolean comparar, String nombreSalida, String nombreTraza, boolean ab_reconfiguracionParcial) {
        fichero_salida = new File(rutaficherosSalida, nombreSalida);
        fichero_compararTraza = new File(rutaficherosSalida, nombreTraza);
        this.ljtfield = lj_jtf;
        this.ejecutando = true;
        this.com1 = ac_com;
        this.li_bits_entrada = e.getBitsEntrada();
        this.li_bits_salida = e.getBitsSalida();
        cadenaaEnviar = new ArrayList();
        setwait = false;
        this.ata_textarea = ata_textarea;
        entraDesdeFichero = false;
        coincideTraza = true;
        mostrarMensaje = true;
        NumInstrNoCoincideTraza = 0;
        this.comparar = comparar;
        reconfiguracionParcial = ab_reconfiguracionParcial;
        this.posicionResetEntidad = e.getPosicionReset();
    }

    /**
     * Crea un objeto de la clase Ejecución y escribe el resultado en un JTextField. Este constructor está definido
     * para el caso en el que el test bench es leido desde fichero
     * @param lj_jtf JTextField en el que se escribirá la salida que reciba de la FPGA al ejecutar las instrucciones.
     * @param e Entidad top sobre la que vamos a ejecutar.
     * @param ac_com Puerto con el que nos comunicamos con la FPGA.
     * @param ata_textarea TextArea de la aplicación del cual se podrían haber recogido las instrucciones (en este caso no se utiliza).
     * @param l_br (BufferedReader)del que se cogerán las instrucciones a ejecutar.
     * @param comparar Booleano que indica si hay que comparar los ficheros de traza y el de salida, para detectar diferencias.
     * @param nombreSalida Fichero en el que se guardará el resultado de la ejecución.
     * @param nombreTraza Fichero de traza con el que se podrá comparar la salida que está generando este objeto.
     * @param ab_reconfiguracionParcial Boolean que indica si estamos ejecutando con la opción de reconfiguración parcial.
     *  Si es falso se ejecutará el hilo directamente y no mostrará mensajes emergentes.
     */
    public Ejecucion(JTextField lj_jtf, Entidad e, Com ac_com, JTextArea ata_textarea, BufferedReader l_br, boolean comparar, String nombreSalida, String nombreTraza, boolean ab_reconfiguracionParcial) {

        fichero_salida = new File(rutaficherosSalida, nombreSalida);
        fichero_compararTraza = new File(rutaficherosSalida, nombreTraza);
        this.ljtfield = lj_jtf;
        this.ejecutando = true;
        this.com1 = ac_com;
        this.li_bits_entrada = e.getBitsEntrada();
        this.li_bits_salida = e.getBitsSalida();
        cadenaaEnviar = new ArrayList();
        setwait = false;
        this.ata_textarea = ata_textarea;
        bfr = l_br;
        entraDesdeFichero = true;
        coincideTraza = true;
        mostrarMensaje = true;
        NumInstrNoCoincideTraza = 0;
        this.comparar = comparar;
        reconfiguracionParcial = ab_reconfiguracionParcial;
        this.posicionResetEntidad = e.getPosicionReset();
    }

    /**
     * Asigna la cadena que se quiere ejecutar para el caso de ejecución desde
     * la pantalla de la GUI. Todas las cadenas a ejecutar estarán contenidas
     * en as_cadenaaejecutar
     * @param as_cadenaajecutar Cadena a ejecutar.
     */
    public void setCadena(String as_cadenaajecutar) {
        ls_cadenaaejecutar = as_cadenaajecutar;
    }

    /**
     * Convierte a entero una una cadena en formato binario
     * @param s Cadena a traducir.
     * @return Entero equivalente al String introducido o -1 si el formato no es
     * correcto (no son 0's y 1's)
     */
    public int traduceString(String s) {
        int n = 0;
        int peso = 1;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) != '0' && s.charAt(i) != '1') {
                return -1;
            }
            if (s.charAt(i) == '1') {
                n = n + peso;
            }
            peso = peso * 2;
        }
        return n;
    }

    /**
     * Indica si el hilo que se está ejecutando tiene que pararse o reanudarse, según el argumento que recibe.
     * @param setwait boolean, valor true que indica que el hilo tiene que continuar, false para pararlo.
     */
    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    public void setFileLogEjec(FileWriter wr){
        this.ficheroLogEjec = wr;
    }

    /**
     * Envía una cadena binaria a la FPGA en 4 partes, porque la instrucción es de 32 bits
     * y solo podemos enviar 8 por cada ciclo
     * @param Cadena de 32 de bits que deseamos enviar.
     */
    private void enviarBinaria(String s) {
        String cad3, cad2, cad1, cad0;
        int dif = 0;
        if (s.length() < 32) {
            dif = 32 - s.length();
        }
        for (int i = 0; i < dif; i++) {
            s = "0" + s; //se añaden 0's por la izquierda
        }
        cad3 = s.substring(0, 8);
        cad2 = s.substring(8, 16);
        cad1 = s.substring(16, 24);
        cad0 = s.substring(24);
        try {
            com1.sendSingleData(traduceString(cad0));
            com1.sendSingleData(traduceString(cad1));
            com1.sendSingleData(traduceString(cad2));
            com1.sendSingleData(traduceString(cad3));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Comprueba que una serie de cadenas tiene el formato correcto, es decir,
     * tiene el número de bits adecuado y sólo está compuesta por 0's y 1's
     * @return boolean si todo es correcto.
     */
    public boolean convierteCadenas() {
        StringTokenizer st;
        st = new StringTokenizer(this.ls_cadenaaejecutar, "\n\r");
        int numBits = this.li_bits_entrada;
        boolean correcto = true;
        int numCadenas = st.countTokens();
        cadenaaEnviar = new ArrayList<String>();
        int i = 0;
        correcto = numCadenas > 0;
        while (st.hasMoreTokens() && correcto) {
            String cadena = st.nextToken();
            if (cadena.length() == numBits) {
                this.cadenaaEnviar.add(cadena);
                correcto = traduceString(cadena) >= 0;
                i++;
            } else {
                correcto = false;
            }
        }
        return correcto;
    }

    /**
     * Método que comienza a ejecutar el hilo.
     *
     */
    @Override
    public void run() {
        synchronized (this) {
            ejecuta();
        }
    }

    /**
     * Método que consulta el estado de la ejecucución del hilo.
     * @return Boolean estado de la ejecución. Cierto si está ejecutando, falso en caso contrario.
     */
    public boolean getejecutando() {
        return ejecutando;
    }

    /**
     * Método que termina la ejecución de un hilo.
     */
    public void pararrecepcionfpga() {
        this.ejecutando = false;
        if (!coincideTraza && mostrarMensaje && !reconfiguracionParcial) {
            JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Procedimiento que lee de la FPGA un entero y lo transforma a la cadena de bits.
     * @return Cadena de bits recibida.
     */
    private String recibirBinaria(int numBitsSalida) throws Exception {
        int num;
        String cadenaRecibida = "";
        for (int i = 0; i < 4; i++) {
            num = com1.receiveSingleDataInt();
            cadenaRecibida = this.convertirByteBinario(num) + cadenaRecibida;
        }
        return cadenaRecibida.substring(cadenaRecibida.length() - numBitsSalida);
    }

    /**
     * Procedimiento que transforma un byte (representado mediante un entero)
     * a una cadena de ceros y  unos. Utiliza el algoritmo de la división por 2.
     * @param recibido Byte a traducir.
     * @return Cadena equivalente en bits al byte recibido.
     */
    private String convertirByteBinario(int recibido) {
        String salida = "";
        int numero;
        numero = recibido;
        int long_byte = 8;
        for (int i = 0; i < long_byte; i++) {
            if (numero % 2 == 0) {
                salida = "0" + salida;
            } else {
                salida = "1" + salida;
            }
            numero = numero / 2;
        }
        return salida;
    }

    /**
     * Procedimiento que ejecuta el hilo.
     * @throws InterruptedException ,Exception.
     */
    public void ejecuta() {
        try {
            if(!fichero_compararTraza.exists()){
                fichero_compararTraza.createNewFile();
            }
            FileReader fr = new FileReader(fichero_compararTraza);
            BufferedReader rw = new BufferedReader(fr);
            FileWriter file_wr;
            String linea_traza = "";
            int instruccion = 0;
            String datoaenviar = null;
            fichero_salida.createNewFile();
            boolean seguir;
            if (entraDesdeFichero) {
                datoaenviar = bfr.readLine();
                seguir = datoaenviar != null;
            } else {
                seguir = instruccion < this.cadenaaEnviar.size();
            }
            file_wr = new FileWriter(fichero_salida, false);
            //Enviamos un reset antes de la ejecución.
            this.enviaReset();
            this.recibirBinaria(li_bits_salida);
            while (ejecutando && seguir) {
                if (this.setwait) {
                    System.out.println("Ejecución antes");
                    file_wr.close();
                    if (comparar && !coincideTraza && mostrarMensaje) {
                        if (!reconfiguracionParcial) {
                            JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida Golden. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                        this.escribeEnLog("La salida no coincide con Salida Golden en la instruccion: " + NumInstrNoCoincideTraza+"\n\n");
                        System.out.println("LA EJECUCION HA SIDO MODIFICADA. Ver log al finalizar");
                        mostrarMensaje = false;
                    }
                    this.wait();
                    file_wr = new FileWriter(fichero_salida, true);
                    System.out.println("Ejecución despues");
                    this.setwait = false;
                } else {
                    if (!entraDesdeFichero) {
                        datoaenviar = this.cadenaaEnviar.get(instruccion);
                    }
                    this.enviarBinaria(datoaenviar);//TODO divido 32
                    String c = this.recibirBinaria(this.li_bits_salida);
                    if (instruccion < 250000) {
                        this.ata_textarea.append((instruccion + 1) + ". " + c + "\n");
                    }
                    file_wr.write(c + "\n");
                    this.escribeEnLog(c);

                    if (comparar && coincideTraza && linea_traza != null) {
                        linea_traza = rw.readLine();
                        this.escribeEnLog("\n");
                        if (linea_traza == null || linea_traza.compareTo(c) != 0) {
                            coincideTraza = false;
                            NumInstrNoCoincideTraza = instruccion + 1;
                            this.escribeEnLog("<----- falla aqui\n");

                        }
                    }
                }
                this.ata_textarea.setCaretPosition(this.ata_textarea.getText().length());
                this.ljtfield.setText(Integer.toString(instruccion + 1));
                instruccion++;
                if (entraDesdeFichero) {
                    datoaenviar = bfr.readLine();
                    seguir = datoaenviar != null;
                } else {
                    seguir = instruccion < this.cadenaaEnviar.size();
                }

            }
            file_wr.close();
            rw.close();
            if (comparar && coincideTraza) {
                if (!reconfiguracionParcial) {
                    JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual coincide con la Golden", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
                this.escribeEnLog("La Salida actual coincide con la Golden\n\n");
                System.out.println("Ejecucion correcta");
            } else {
                if (comparar && mostrarMensaje) {
                    if (!reconfiguracionParcial) {
                        JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                    this.ficheroLogEjec.write("La Salida actual NO coincide con la salida Golden. Revise Instrucción num: " + NumInstrNoCoincideTraza+"\n\n");
                    System.out.println("LA EJECUCION HA SIDO MODIFICADA. Ver log al finalizar");

                    mostrarMensaje = false;
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Función para copiar un fichero en otro fichero. En nuestro caso copiamos el
     *  archivo de escritura en el de comparar con traza.
     * @throws IOException
     */
    public void CopiarSalida() throws IOException {

        InputStream in = new FileInputStream(fichero_salida);
        OutputStream out = new FileOutputStream(fichero_compararTraza);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Comprueba si un fichero de Test Bench tiene el formato correcto, es decir,
     * si sus cadenas tienen el tamaño correcto correspondiente con las entradas
     * de la entidad a ejecutar, y si se trata sólamente de cadenas de 0's y 1's
     * @param br Fichero de lectura
     * @return true si el formato es correcto y false en caso contrario
     */
    public boolean formatoCorrectoFicheroTB(String ficheroTB) {
        String linea = null;
        boolean correcto = true;
        try {
            BufferedReader bf;
            bf = new BufferedReader(new FileReader(ficheroTB));
            do {
                linea = bf.readLine();
                if (linea != null) {
                    if (linea.length() == this.li_bits_entrada) {
                        int i = 0;
                        while (i < linea.length() && correcto) {
                            correcto = linea.charAt(i) == '0' || linea.charAt(i) == '1';
                            i++;
                        }
                    } else {
                        correcto = false;
                    }
                }
            } while (linea != null && correcto);
            bf.close();
        } catch (IOException ex) {
            correcto = false;
        }
        return correcto;
    }

    /**
     * Realiza el envío de reset a la entidad, mandando para ello una cadena
     * con todo ceros excepto un uno en la posición correspondiente al reset
     */
    public void enviaReset() {
        String cadenaReset = "";
        for (int i = 0; i < 32; i++) {
            if (i == this.posicionResetEntidad) {
                cadenaReset = "1" + cadenaReset;
            } else {
                cadenaReset = "0" + cadenaReset;
            }
        }
        this.enviarBinaria(cadenaReset);
    }

    /**
     * Escribe la cadena s en el fichero de log en el que se introducen todos
     * los detalles de ejecución para el caso del proceso de reconfiguración.
     * Se controla que el fichero esté inicializado para el caso de ejecuciones
     * en las que no se escribe en el log (ejecuciones simples).
     * @param s La cadena que se desea escribir en el fichero de log.
     */
    public void escribeEnLog(String s){
        if (this.ficheroLogEjec != null){
            try {
                this.ficheroLogEjec.write(s);
            } catch (IOException ex) {
                Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
