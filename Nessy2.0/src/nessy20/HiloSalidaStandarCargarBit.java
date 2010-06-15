/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nessy20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author User
 */


public class HiloSalidaStandarCargarBit extends Thread {

    private GUIPrincipal interfaz;
    private boolean escribirEnPantalla;
    private Process proc ;
    private CargaBit cargabit;

    public HiloSalidaStandarCargarBit(GUIPrincipal in_interfaz, boolean in_escribirEnPantalla, Process f,CargaBit in_carga) {
        interfaz = in_interfaz;
        escribirEnPantalla = in_escribirEnPantalla;
        proc = f;
        cargabit = in_carga;
    }

    private void cargabit(){

    boolean correcto;
    try {
       // Process p = Runtime.getRuntime().exec(rutaImpact+ " -batch carga.txt");
        InputStream is = proc.getInputStream();
        //InputStream is = proc.getErrorStream();
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