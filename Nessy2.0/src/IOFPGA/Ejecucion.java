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
    private boolean error;

    public boolean isError() {
        return error;
    }

    public Ejecucion(GUIPrincipal gui,Com ac_com){

        this.interfaz=gui;
        this.ejecutando = true;
        this.com1=ac_com;
        cadenaaEnviar = new ArrayList();


    }
    public void setCadena(String as_cadenaajecutar){
        ls_cadenaaejecutar =  as_cadenaajecutar ;
        TraduceString();
    }

    private void TraduceString (){
        int i = 0;
        int potencia = 1;
        this.error = false;
        int longcadena = interfaz.getEntidad().getBitsEntrada();
        //int longcadena = 4;
        // num_bit controlara que la cadena tiene la misma logitud que longcadena
        int num_bit = longcadena ;
        // al ser el bit mas significativo el enable le sumamos la posición del enable elevado al longcadena
        int enteroaenviar = (int) Math.pow(2, longcadena);
        potencia = (int) Math.pow(2, longcadena - 1);
        while (i < this.ls_cadenaaejecutar.length() && this.error==false){
            char lc_caracter  = ls_cadenaaejecutar.charAt(i);
            if ((lc_caracter == ' ') || (lc_caracter == '\t') || (lc_caracter == '\n') || (lc_caracter == '\r')){
                potencia = 0;
                if (i > 0){
                    if (num_bit ==0){
                        cadenaaEnviar.add(i);
                        System.out.println(enteroaenviar);
                        enteroaenviar = (int) Math.pow(2, longcadena);
                        num_bit = longcadena ;
                        potencia = (int) Math.pow(2, longcadena - 1);
                    }
                    else{
                        this.error = true;
                    }
                }
            }
            else {
                if ((lc_caracter == '1') || (lc_caracter == '0')) {
                    if (lc_caracter == '1') {
                        enteroaenviar = enteroaenviar +  potencia ;
                    }
                    potencia = potencia / 2;
                    num_bit = num_bit -1;
                }
                else{
                    this.error = true;
                }

            }
         i++;
        }
        if (i > 0){
            if (num_bit ==0){
                cadenaaEnviar.add(i);
                System.out.println(enteroaenviar);
                enteroaenviar = (int) Math.pow(2, longcadena);
                num_bit = longcadena ;
                potencia = (int) Math.pow(2, longcadena - 1);
            }
            else{
                this.error = true;
            }
        }
    }
    public void run(){
        ejecuta();
    }

    private void ejecuta() {
       //TraduceString();
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
