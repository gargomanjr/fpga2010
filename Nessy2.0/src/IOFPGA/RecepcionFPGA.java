package IOFPGA;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package MiLib;
import app.*;
import java.awt.TextArea;
import nessy20.GUIPrincipal;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private TextArea ata_textarea;
    private int long_bits;
    private boolean setwait;

    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    public void setEnteroAnterior(int enteroAnterior) {
        this.enteroAnterior = enteroAnterior;
    }

    //public RecepcionFPGA(GUIPrincipal aux, Parameters a_param, Com a_com) {
    public RecepcionFPGA(TextArea aux, int il_longitud, Parameters a_param, Com a_com) {
        //miInterfaz = aux;
        ata_textarea = aux;
        long_bits = il_longitud;
        com1 = a_com;
        param = a_param;
        recibiendo = true;
        setwait = false;
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
            if (this.setwait){
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
                this.ata_textarea.setText(this.ata_textarea.getText() + c + "\n");
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
        this.recibiendo = false;
    }

}


