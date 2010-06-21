/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cargarBit;

import cargarBit.CargaBit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import nessy20.GUIPrincipal;

/**
 *
 * Hio encargado de leer la salida estándar generada por el proceso que carga
 * el archivo .bit en la FPGA.
 */


public class HiloSalidaStandarCargarBit extends Thread {

    private GUIPrincipal interfaz;
    private boolean escribirEnPantalla;
    private Process proc ;
    private CargaBit cargabit;

    /**
     * Contructora de la clase HiloSalidaStandarCargarBit
     * @param in_interfaz GUIPrincipal de la aplicación.
     * @param in_escribirEnPantalla Boolean que indica si la aplicacón tiene que escribir
     * el resultado en la pantalla da la aplicación.
     * @param f Process que se ha encargado de intentar cargar el archivo .bit.
     * @param in_carga Objeto de la clase Carga_bit que ha llamado al hilo
     */
    public HiloSalidaStandarCargarBit(GUIPrincipal in_interfaz, boolean in_escribirEnPantalla, Process f,CargaBit in_carga) {
        interfaz = in_interfaz;
        escribirEnPantalla = in_escribirEnPantalla;
        proc = f;
        cargabit = in_carga;
    }
    /**
     * Proceso encargado de leer la salida estandar generada por el proceso de
     * cargar el archivo .bit y establecer dependiendo la salida, en la clase
     * CargaBit si ha habido algún error o no.
     */
    private void cargabit(){
    boolean correcto;
    try {
        InputStream is = proc.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s;
        s = br.readLine();
        boolean errorCarga = true;
        while(s!=null && errorCarga){
            if(escribirEnPantalla)
                interfaz.escribirEnPantalla(s);
           // System.out.println(s);
            if (s.contains("Programmed successfully")){
                errorCarga = false;
            }
            s=br.readLine();
        }
        interfaz.escribirEnPantalla("Termina la Lectura de la Salida al Cargar en la FPGA el .Bit");
        correcto = !errorCarga;
        cargabit.setSalidaCargaBit(correcto);
    } catch (IOException ex) {
            Logger.getLogger(HiloSalidaStandarCargarBit.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    @Override
    public void run() {
        this.cargabit();
    }
}