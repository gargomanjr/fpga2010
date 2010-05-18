/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    public boolean comparar;

    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    public boolean isError() {
        return error;
    }

    //public Ejecucion(GUIPrincipal gui,Com ac_com){
    public Ejecucion(JTextField lj_jtf, int bits_entrada, int bits_salida, Com ac_com, JTextArea ata_textarea, boolean comparar,String nombreSalida,String nombreTraza) {
//        this.interfaz=gui;
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
    }

    public Ejecucion(JTextField lj_jtf, int bits_entrada, int bits_salida, Com ac_com, JTextArea ata_textarea, BufferedReader l_br,boolean comparar,String nombreSalida,String nombreTraza) {
        fichero_compararTraza = new File(rutafichero, "Traza.txt");
        fichero_escritura = new File(rutafichero, "Salida.txt");
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
    }

    public void setCadena(String as_cadenaajecutar) {
        ls_cadenaaejecutar = as_cadenaajecutar;

    }

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
        //n = n+peso;//el enable
        return n;
    }

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

    public boolean convierteCadenas() {
        StringTokenizer st;
        st = new StringTokenizer(this.ls_cadenaaejecutar, "\n\r");
        //int numBits = interfaz.getEntidad().getBitsEntrada();
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
                //datosEnviar[i] = traduceString(cadena);
                correcto = traduceString(cadena) >= 0;
                i++;
            } else {
                correcto = false;
            }
        }
        return correcto;
    }

    public void run() {
        synchronized (this) {
            if (entraDesdeFichero) {
                ejecuta2();
            } else {
                ejecuta();
            }
        }
    }

    private void ejecuta2() {
        try {
            CopiarSalida();
            FileReader fr = new FileReader(fichero_compararTraza);
            BufferedReader rw = new BufferedReader(fr);
            String linea_traza = "";
            int instruccion = 0;
            String datoaenviar = "";
            datoaenviar = bfr.readLine();
            fichero_escritura.createNewFile();
            file_wr = new FileWriter(fichero_escritura, false);
            while (ejecutando && datoaenviar != null) {
                if (this.setwait) {
                    System.out.println("Ejecución antes");
                    file_wr.close();
                    if(comparar && coincideTraza == false && mostrarMensaje){
                         JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
                         mostrarMensaje = false;
                    }
                    this.wait();
                    file_wr = new FileWriter(fichero_escritura, true);
                    System.out.println("Ejecución despues");
                    this.setwait = false;
                }
                this.enviarBinaria(datoaenviar); //TODO divido 32
                String c = this.recibirBinaria(this.li_bits_salida);
                if (instruccion < 250000) {
                    this.ata_textarea.append((instruccion+1)+". "+c + "\n");
                }
                file_wr.write(c + "\n");

                if(comparar && coincideTraza && linea_traza != null){
                    linea_traza = rw.readLine();
                    if ( linea_traza == null  || linea_traza.compareTo(c)!=0 ){
                        coincideTraza = false;
                        NumInstrNoCoincideTraza = instruccion + 1;
                    }
                }
                this.ata_textarea.setCaretPosition(this.ata_textarea.getText().length());
                this.ljtfield.setText(Integer.toString(instruccion + 1));
                instruccion++;
                datoaenviar = bfr.readLine();
            }
            file_wr.close();
            rw.close();
            if(comparar && coincideTraza){
               JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual coincide con la Traza", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                if (comparar && mostrarMensaje){
                                           JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
                     mostrarMensaje = false;
                }
            }
            if (instruccion == 250000) {
                JOptionPane.showMessageDialog(this.ata_textarea, "La Salida que se está generando al ser muy grande se volcará en Test//Salida.txt", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
    }

    public void pararrecepcionfpga() {
        this.ejecutando = false;
        if(coincideTraza == false && mostrarMensaje){
            JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String recibirBinaria(int numBitsSalida) throws Exception {
        int num;
        String s = "";
        for (int i = 0; i < 5; i++) {
            num = com1.receiveSingleDataInt();
            if (i > 0) {
                s = this.convertirCadenaBinaria(num, 8) + s;
            }
        }
        return s.substring(s.length() - numBitsSalida);
    }

    private String convertirCadenaBinaria(int recibido, int numBits) {
        String salida = "";
        int numero;
        numero = recibido;
        //int long_cadena = this.miInterfaz.getEntidad().getBitsSalida();
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

    private void ejecuta() {
        //TraduceString();
        try {
            CopiarSalida();
            FileReader fr = new FileReader(fichero_compararTraza);
            BufferedReader rw = new BufferedReader(fr);
            String linea_traza = "";
            int instruccion = 0;
            String datoaenviar;
            fichero_escritura.createNewFile();
            file_wr = new FileWriter(fichero_escritura, false);
            while (ejecutando && instruccion < this.cadenaaEnviar.size()) {
                if (this.setwait) {
                    System.out.println("Ejecución antes");
                    file_wr.close();
                    if(comparar && coincideTraza == false && mostrarMensaje){
                         JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual NO coincide con la salida generada por la última ejecución. Revise Instrucción num: " + NumInstrNoCoincideTraza, "Info", JOptionPane.INFORMATION_MESSAGE);
                         mostrarMensaje = false;
                    }
                    this.wait();
                    file_wr = new FileWriter(fichero_escritura, true);
                    System.out.println("Ejecución despues");
                    this.setwait = false;
                }
                datoaenviar = this.cadenaaEnviar.get(instruccion);
                this.enviarBinaria(datoaenviar);//TODO divido 32
                String c = this.recibirBinaria(this.li_bits_salida);
                this.ata_textarea.append((instruccion+1)+". "+c + "\n");
                file_wr.write(c + "\n");

                if(comparar && coincideTraza && linea_traza != null){
                    linea_traza = rw.readLine();
                    if ( linea_traza == null  || linea_traza.compareTo(c)!=0 ){
                        coincideTraza = false;
                        NumInstrNoCoincideTraza = instruccion + 1;
                    }
                }



                this.ata_textarea.setCaretPosition(this.ata_textarea.getText().length());
                // bits ebn 4 grupos y los envío
                //  this.interfaz.setNumeroInst(intruccion);
                this.ljtfield.setText(Integer.toString(instruccion + 1));
                //Thread.sleep(5000);           
                instruccion++;
            }
            file_wr.close();
            rw.close();
            if(comparar && coincideTraza){
               JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual coincide con la Traza", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                if (comparar && mostrarMensaje){
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
    
    // Función para copiar la salida en el otro fichero.
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
