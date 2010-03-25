/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package IOFPGA;

import app.Com;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import nessy20.GUIPrincipal;

/**
 *
 * @author Tony
 */
public class Ejecucion extends Thread {

   // private GUIPrincipal interfaz;
    private ArrayList<Integer> cadenaaEnviar;
    private boolean ejecutando;
    private Com com1;
    private String ls_cadenaaejecutar;
    private boolean error;
    private int datosEnviar[];
    private int li_bits_entrada;
    private final JTextField ljtfield;
    private boolean setwait;

    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    public boolean isError() {
        return error;
    }

    //public Ejecucion(GUIPrincipal gui,Com ac_com){
    public Ejecucion(JTextField  lj_jtf,int bits_entrada,Com ac_com){
//        this.interfaz=gui;
        this.ljtfield = lj_jtf;
        this.ejecutando = true;
        this.com1= ac_com;
        this.li_bits_entrada = bits_entrada;
        cadenaaEnviar = new ArrayList();
        setwait = false;


    }
    public void setCadena(String as_cadenaajecutar){
        ls_cadenaaejecutar =  as_cadenaajecutar ;
        
    }

    /*public void TraduceString (){
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
    }*/

    public int traduceString(String s){
        int n = 0;
        int peso = 1;
        for (int i = s.length()-1; i >= 0; i--){
            if (s.charAt(i)!='0' && s.charAt(i)!='1')
                return -1;
            if (s.charAt(i)=='1'){
                n = n+peso;
            }
            peso = peso * 2;
        }
        n = n+peso;//el enable
        return n;
    }

    public boolean convierteCadenas(){
        StringTokenizer st;
        st = new StringTokenizer(this.ls_cadenaaejecutar,"\n\r");
        //int numBits = interfaz.getEntidad().getBitsEntrada();
        int numBits = this.li_bits_entrada;
        boolean correcto = true;
        datosEnviar = new int[st.countTokens()];
        cadenaaEnviar = new ArrayList<Integer>();
        int i = 0;
        while (st.hasMoreTokens() && correcto){
            String cadena = st.nextToken();
            if (cadena.length() == numBits){
                this.cadenaaEnviar.add(traduceString(cadena));
                //datosEnviar[i] = traduceString(cadena);
                correcto = cadenaaEnviar.get(i) >= 0;
                i++;
            }else{
                correcto = false;
            }
        }
        return correcto;
    }
    public void run(){
        synchronized(this)
        {
         ejecuta();
        }
    }

    private void ejecuta() {
       //TraduceString();
       int intruccion = 0;
       int datoaenviar;
       while(ejecutando && intruccion < this.cadenaaEnviar.size()){
            if (this.setwait){
                try {
                    System.out.println("Ejecución antes");
                    this.wait();
                    System.out.println("Ejecución despues");
                    this.setwait = false;
                } catch (InterruptedException ex) {
                    Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            datoaenviar = this.cadenaaEnviar.get(intruccion);
            try {
                this.com1.sendSingleData(datoaenviar);
              //  this.interfaz.setNumeroInst(intruccion);
                this.ljtfield.setText(Integer.toString(intruccion));
                Thread.sleep(5000);
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
