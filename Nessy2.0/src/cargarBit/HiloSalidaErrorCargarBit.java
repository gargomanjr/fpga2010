/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cargarBit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import nessy20.GUIPrincipal;

/**
 *
 * Hio encargado de leer la salida de error generada por el proceso que carga
 * el archivo .bit en la FPGA.
 */


public class HiloSalidaErrorCargarBit extends Thread {

    private GUIPrincipal interfaz;
    private boolean escribirEnPantalla;
    private Process proc ;


    /**
     * Contructora de la clase HiloSalidaErrorCargarBit
     * @param in_interfaz GUIPrincipal de la aplicación.
     * @param in_escribirEnPantalla Boolean que indica si la aplicacón tiene que escribir
     * el resultado en la pantalla da la aplicación.
     * @param f Process que se ha encargado de intentar cargar el archivo .bit.
     */
    public HiloSalidaErrorCargarBit(GUIPrincipal in_interfaz, boolean in_escribirEnPantalla, Process f) {
        interfaz = in_interfaz;
        escribirEnPantalla = in_escribirEnPantalla;
        proc = f;
    }

   /**
     * Proceso encargado de leer la salida de error generada por el proceso de
     * cargar el archivo .bit.
     */
    private void cargabit(){
    boolean correcto;
    try {
        InputStream is = proc.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String s;
        s = br.readLine();
        boolean errorCarga = true;
        while(s!=null && errorCarga){
            if(escribirEnPantalla)
                interfaz.escribirEnPantalla(s);
                System.out.println(s);
            if (s.contains("Programmed successfully")){
                errorCarga = false;
            }
            s=br.readLine();
        }
        interfaz.escribirEnPantalla("Termina la Lectura de la Salida de Error al cargar en la FPGA el archivo .Bit");
        correcto = !errorCarga;
    } catch (IOException ex) {
            Logger.getLogger(HiloSalidaErrorCargarBit.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    @Override
    public void run() {
        this.cargabit();
      
    }
}