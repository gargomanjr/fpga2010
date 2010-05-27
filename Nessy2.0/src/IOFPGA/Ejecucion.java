/*
 * 
 * 
 */
package IOFPGA;

import app.Com;
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
 * @author Tony
 *
 * Clase que Ejecuta las operaciones que se le pasan.
 * hereda de la clase Thread.
 */
public class Ejecucion extends Thread {

    // private GUIPrincipal interfaz;
    private ArrayList<String> cadenaaEnviar;
    private boolean ejecutando;
    private Com com1;
    private String ls_cadenaaejecutar;
    private boolean error;
    private int datosEnviar[];
    private int li_bits_entrada;
    private int li_bits_salida;
    private final JTextField ljtfield;
    private boolean setwait;
    private JTextArea ata_textarea;
    private boolean entraDesdeFichero;
    private BufferedReader bfr;
    static String rutafichero = System.getProperties().getProperty("user.dir") + "\\test";
    private final File fichero_escritura;
    private final File fichero_compararTraza;
    private FileWriter file_wr;
    private boolean coincideTraza;
    private boolean mostrarMensaje;
    private int NumInstrNoCoincideTraza;
    private boolean comparar;
    private boolean recunfiguracion_pracial;


   /**
     * Crea un objeto de la clase Ejecución y escribe el resultado en un JTextField .
     * @param lj_jtf JTextField en el que se escribirá la salida que reciba de la FPGA al ejecutar las instrucciones.
     * @param bits_entrada Número de bits de entrada de la entidad (top) sobre la que vamos a ejecutar.
     * @param bits_salida Número de bits de salida de la entidad (top).
     * @param ac_com Puerto con el que nos comunicamos con la FPGA.
     * @param ata_textarea TextArea de la aplicación del cual se recogerán las instrucciones a ejecutar.
     * @param comparar Booleano que indica si hay que comparar los ficheros de traza y el de salida, para detectar diferencias.
     * @param nombreSalida Fichero en el que se guardará el resultado de la ejecución.
     * @param nombreTraza Fichero de traza con el que se podrá comparar la salida que está generando este objeto.
     * @param ab_recunfiguracion_pracial Boolean que indica si estamos ejecutando con la opción de reconfiguración parcial.
     *  Si es falso se ejecutará el hilo directamente y no mostrará mensajes emergentes.
     */
    public Ejecucion(JTextField lj_jtf, int bits_entrada, int bits_salida, Com ac_com, JTextArea ata_textarea, boolean comparar, String nombreSalida, String nombreTraza,boolean ab_recunfiguracion_pracial) {
        fichero_escritura = new File(rutafichero, nombreSalida);
        fichero_compararTraza = new File(rutafichero, nombreTraza);
        this.ljtfield = lj_jtf;
        this.ejecutando = true;
        this.com1 = ac_com;
        this.li_bits_entrada = bits_entrada;
        this.li_bits_salida = bits_salida;
        cadenaaEnviar = new ArrayList();
        setwait = false;
        this.ata_textarea = ata_textarea;
        entraDesdeFichero = false;
        coincideTraza = true;
        mostrarMensaje = true;
        NumInstrNoCoincideTraza = 0;
        this.comparar = comparar;
        recunfiguracion_pracial = ab_recunfiguracion_pracial;
        if(recunfiguracion_pracial){
            ejecuta();
        }
    }

   /**
     * Crea un objeto de la clase Ejecución y escribe el resultado en un JTextField .
     * @param lj_jtf JTextField en el que se escribirá la salida que reciba de la FPGA al ejecutar las instrucciones.
     * @param bits_entrada Número de bits de entrada de la entidad (top) sobre la que vamos a ejecutar.
     * @param bits_salida Número de bits de salida de la entidad (top).
     * @param ac_com Puerto con el que nos comunicamos con la FPGA.
     * @param ata_textarea TextArea de la aplicación del cual se podrían haber recogido las instrucciones (en este caso no se utiliza).
     * @param l_br (BufferedReader)del que se cogerán las instrucciones a ejecutar.
     * @param comparar Booleano que indica si hay que comparar los ficheros de traza y el de salida, para detectar diferencias.
     * @param nombreSalida Fichero en el que se guardará el resultado de la ejecución.
     * @param nombreTraza Fichero de traza con el que se podrá comparar la salida que está generando este objeto.
     * @param ab_recunfiguracion_pracial Boolean que indica si estamos ejecutando con la opción de reconfiguración parcial.
     *  Si es falso se ejecutará el hilo directamente y no mostrará mensajes emergentes.
     */
    public Ejecucion(JTextField lj_jtf, int bits_entrada, int bits_salida, Com ac_com, JTextArea ata_textarea, BufferedReader l_br, boolean comparar, String nombreSalida, String nombreTraza,boolean ab_recunfiguracion_pracial) {

        fichero_escritura = new File(rutafichero, nombreSalida);
        fichero_compararTraza = new File(rutafichero, nombreTraza);
        this.ljtfield = lj_jtf;
        this.ejecutando = true;
        this.com1 = ac_com;
        this.li_bits_entrada = bits_entrada;
        this.li_bits_salida = bits_salida;
        cadenaaEnviar = new ArrayList();
        setwait = false;
        this.ata_textarea = ata_textarea;
        bfr = l_br;
        entraDesdeFichero = true;
        coincideTraza = true;
        mostrarMensaje = true;
        NumInstrNoCoincideTraza = 0;
        this.comparar = comparar;
        recunfiguracion_pracial = ab_recunfiguracion_pracial;
        if(recunfiguracion_pracial){
            ejecuta();
        }
    }
   /**
     * Asigna la cadena que se quiere ejecutar.
     * @param as_cadenaajecutar Cadena a ejecutar.
     */
    public void setCadena(String as_cadenaajecutar) {
        ls_cadenaaejecutar = as_cadenaajecutar;
    }

   /**
     * Transforma un String en du entero equivalente
     * @param s Cadena a traducir.
     * @return Entero equivalente al String introducido.
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
     * @param setwait boolean que indica si se tiene que detener el hilo o continuar.
     */
    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    /**
     * Transforma un String en un entero que será el que se mande a la FPGA
     * @return Booleano que indica si hay diferencias entre el fichero de traza y la salida que se está generando.
     */
    public boolean isError() {
        return error;
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
     * Envía una cadena binaria a la FPGA en 4 partes, porque la instrucción es de 32 bits
     * @return boolean si todo es correcto.
     */
    public boolean convierteCadenas() {
        StringTokenizer st;
        st = new StringTokenizer(this.ls_cadenaaejecutar, "\n\r");
        int numBits = this.li_bits_entrada;
        boolean correcto = true;
        datosEnviar = new int[st.countTokens()];
        cadenaaEnviar = new ArrayList<String>();
        int i = 0;
        correcto = datosEnviar.length > 0;
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
     * @return Boolean estado de la ejecución.
     */
    public boolean getejecutando() {
        return ejecutando;
    }
   /**
     * Método que termina la ejecución de un hilo.
     */
    public void pararrecepcionfpga() {
        this.ejecutando = false;
        if (coincideTraza == false && mostrarMensaje && recunfiguracion_pracial==false) {
            JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
   /**
     * Procedimiento que lee de la FPGA un entero y lo transforma a la cadena de bits.
     * @return Cadena de bits recibida.
     */
    private String recibirBinaria(int numBitsSalida) throws Exception {
        int num;
        String s = "";
        for (int i = 0; i < 4; i++) {
            num = com1.receiveSingleDataInt();
            s = this.convertirCadenaBinaria(num, 8) + s;
        }
        return s.substring(s.length() - numBitsSalida);
    }
   /**
     * Procedimiento que transforma un entero, que tiene una longitud de bits a una cadena.
     * @param recibido Entero a transformar.
     * @param numBits que tiene la cadena que se va a generar.
     * @return Cadena equivalente en bits al entero recibido con la longitud numBits.
     */
    private String convertirCadenaBinaria(int recibido, int numBits) {
        String salida = "";
        int numero;
        numero = recibido;
        int long_cadena = numBits;
        for (int i = 0; i < long_cadena; i++) {
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
    private void ejecuta() {
        try {
            FileReader fr = new FileReader(fichero_compararTraza);
            BufferedReader rw = new BufferedReader(fr);
            String linea_traza = "";
            int instruccion = 0;
            String datoaenviar =null;
            fichero_escritura.createNewFile();
            boolean seguir;
            if (entraDesdeFichero) {
                datoaenviar = bfr.readLine();
                seguir = datoaenviar != null;
            } else {
                seguir = instruccion < this.cadenaaEnviar.size();
            }
            file_wr = new FileWriter(fichero_escritura, false);
            while (ejecutando && seguir) {
                if (this.setwait) {
                    System.out.println("Ejecución antes");
                    file_wr.close();
                    if (comparar && coincideTraza == false && mostrarMensaje) {
                        if(recunfiguracion_pracial==false)
                             JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
                        mostrarMensaje = false;
                    }
                    this.wait();
                    file_wr = new FileWriter(fichero_escritura, true);
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


                    if (comparar && coincideTraza && linea_traza != null) {
                        linea_traza = rw.readLine();
                        if (linea_traza == null || linea_traza.compareTo(c) != 0) {
                            coincideTraza = false;
                            NumInstrNoCoincideTraza = instruccion + 1;
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
                if(recunfiguracion_pracial==false)
                    JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual coincide con la Traza", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (comparar && mostrarMensaje) {
                    if(recunfiguracion_pracial==false)
                         JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
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
     * Función para copiar un fichero en otro fichero.
    * @throws IOException
    */
    public void CopiarSalida() throws IOException {

        InputStream in = new FileInputStream(fichero_escritura);
        OutputStream out = new FileOutputStream(fichero_compararTraza);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
