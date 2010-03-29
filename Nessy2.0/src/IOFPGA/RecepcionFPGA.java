package IOFPGA;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package MiLib;
import app.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author User
 */
public class RecepcionFPGA extends Thread {

    //  GUIPrincipal miInterfaz;
    Parameters param;
    Com com1;
    boolean recibiendo;
    private int enteroAnterior;
    private JTextArea ata_textarea;
    private int long_bits;
    private boolean setwait;
    private String salidafichero;
    private File fichero_salida;
    static String rutafichero = System.getProperties().getProperty("user.dir") + "\\test";
    private BufferedWriter bw;
    private String compararcontraza;
    private BufferedReader rw;

    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    public void setEnteroAnterior(int enteroAnterior) {
        this.enteroAnterior = enteroAnterior;
    }

    //public RecepcionFPGA(GUIPrincipal aux, Parameters a_param, Com a_com) {
    public RecepcionFPGA(JTextArea aux, int il_longitud, Parameters a_param, Com a_com) {
        //miInterfaz = aux;
        ata_textarea = aux;
        long_bits = il_longitud;
        com1 = a_com;
        param = a_param;
        recibiendo = true;
        setwait = false;
        salidafichero = "";
        fichero_salida = new File(rutafichero, "fichero.txt");
        if (!(fichero_salida.exists())) {
            try {
                fichero_salida.createNewFile();
                bw = new BufferedWriter(new FileWriter(fichero_salida));
                FileReader fr = new FileReader(fichero_salida);
                rw = new BufferedReader(new BufferedReader(fr));
                String linea = rw.readLine();
                while(linea != null){
                    this.compararcontraza= compararcontraza + linea + "\n";
                    linea = rw.readLine();
                }
                rw.close();
            } catch (IOException ex) {
                Logger.getLogger(RecepcionFPGA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void run() {
        synchronized (this) {
            try {
                this.recibirDatos();
            } catch (Exception ex) {
                Logger.getLogger(RecepcionFPGA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void recibirDatos() throws Exception {
        Character datoRecibido;
        int entero;
        enteroAnterior = -1;
        String cAnterior = "";
        while (recibiendo) {
            if (this.setwait) {
                try {
                    System.out.println("Recepcion antes");
                    wait();
                    System.out.println("Recepcion despues");
                    this.setwait = false;
                } catch (InterruptedException ex) {
                    Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            entero = com1.receiveSingleDataInt();
            //Si el dato ha cambiado con respecto al anterior.
            String c = convertirCadenaBinaria(entero);

            if (entero > 0 && !cAnterior.equals(c)) {
                cAnterior = c;
                //String c = convertirCadenaBinaria(entero);
                // miInterfaz.EscribirDatoPantalla(c);
                this.salidafichero = this.salidafichero + c + "\n";
                this.ata_textarea.setText(this.ata_textarea.getText() + c + "\n");
            }
          if(this.compararcontraza != null){
              if (this.compararcontraza.indexOf(this.salidafichero)!= -1){
              JOptionPane.showMessageDialog(this.ata_textarea, "La Salida actual no coincide con la salida generada por la anterior ejecución", "Info", JOptionPane.INFORMATION_MESSAGE);
              }
          }
        }
    }

    private String convertirCadenaBinaria(int entero) {
        String salida = "";
        int numero;
        numero = entero;
        //int long_cadena = this.miInterfaz.getEntidad().getBitsSalida();
        int long_cadena = this.long_bits;
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

    private char convertir(int entero) {
        entero -= 48;
        char c = (char) ('0' + entero);
        if (entero > 9) {
            c = (char) ('A' - 10 + entero);
        }
        return c;
    }

    public void pararrecepcionfpga() {
        try {
            bw.write(this.salidafichero, 0, this.salidafichero.length());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(RecepcionFPGA.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.recibiendo = false;
    }
}


