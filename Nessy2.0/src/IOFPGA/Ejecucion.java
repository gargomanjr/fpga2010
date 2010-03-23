/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package IOFPGA;

import app.Com;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import nessy20.GUIPrincipal;


/**
 *
 * @author Tony
 */
public class Ejecucion extends Thread {

    private GUIPrincipal interfaz;
    private ArrayList<Integer> cadenaaEnviar;
    private boolean ejecutando;
    private Com com1;
    private String ls_cadenaaejecutar;

    public Ejecucion(GUIPrincipal gui,Com ac_com){

        this.interfaz=gui;
        this.ejecutando = true;
        this.com1=ac_com;
        cadenaaEnviar = new ArrayList();
    

    }
    public void setCadena(String as_cadenaajecutar){
        ls_cadenaaejecutar =  as_cadenaajecutar ;
    }

    private void TraduceString (){
        int i = 0;
        int potencia = 1;
        int enteroaenviar = 0;
        int longcadena = interfaz.getEntidad().getBitsEntrada();
        while (i < this.ls_cadenaaejecutar.length()){
            char lc_caracter  = ls_cadenaaejecutar.charAt(i);
            if ((lc_caracter == ' ') || (lc_caracter == '\t') || (lc_caracter == '\n') || (lc_caracter == '\r')){
                potencia = 0;
                if (i > 0){
                    cadenaaEnviar.add(i);
                    System.out.println(enteroaenviar);
                    enteroaenviar = 0 ;                   
                }
            }
            else {
                if ((lc_caracter == '1') || (lc_caracter == '0')) {
                    if (lc_caracter == '1') {
                        enteroaenviar = enteroaenviar +  potencia ;
                    }
                    potencia = potencia * 2;
                }

            }
         i++;
        }
    }
    public void run(){
        ejecuta();
    }

    private void ejecuta() {
       TraduceString();
       int intruccion = 0;
       int datoaenviar;
       while(ejecutando && intruccion < this.cadenaaEnviar.size()){
            datoaenviar = this.cadenaaEnviar.get(intruccion);
            try {
                this.com1.sendSingleData(datoaenviar);
                this.interfaz.setNumeroInst(intruccion);
            //    Thread.sleep(5000);
            } catch (Exception ex) {
                Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
            }
           intruccion ++;          
       }
    }
    public void pararrecepcionfpga(){
        this.ejecutando=false;
    }
}
