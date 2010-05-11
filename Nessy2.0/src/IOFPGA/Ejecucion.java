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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nessy20.GUIPrincipal;

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

    public void setSetwait(boolean setwait) {
        this.setwait = setwait;
    }

    public boolean isError() {
        return error;
    }

    //public Ejecucion(GUIPrincipal gui,Com ac_com){
    public Ejecucion(JTextField  lj_jtf,int bits_entrada,int bits_salida,Com ac_com,JTextArea ata_textarea){
//        this.interfaz=gui;
        this.ljtfield = lj_jtf;
        this.ejecutando = true;
        this.com1= ac_com;
        this.li_bits_entrada = bits_entrada;
        this.li_bits_salida = bits_salida;
        cadenaaEnviar = new ArrayList();
        setwait = false;
        this.ata_textarea = ata_textarea;



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
        //n = n+peso;//el enable
        return n;
    }

    private void enviarBinaria(String s) throws Exception{
        String cad3, cad2,cad1,cad0;
        int dif = 0;
        if (s.length() < 32){
            dif = 32-s.length();
        }
        for (int i = 0; i < dif; i++){
            s = "0"+s; //se añaden 0's por la izquierda
        }
        cad3 = s.substring(0,8);
        cad2 = s.substring(8,16);
        cad1 = s.substring(16,24);
        cad0 = s.substring(24);

        com1.sendSingleData(traduceString(cad0));
        com1.sendSingleData(traduceString(cad1));
        com1.sendSingleData(traduceString(cad2));
        com1.sendSingleData(traduceString(cad3));
    }

    public boolean convierteCadenas(){
        StringTokenizer st;
        st = new StringTokenizer(this.ls_cadenaaejecutar,"\n\r");
        //int numBits = interfaz.getEntidad().getBitsEntrada();
        int numBits = this.li_bits_entrada;
        boolean correcto = true;
        datosEnviar = new int[st.countTokens()];
        cadenaaEnviar = new ArrayList<String>();
        int i = 0;
        correcto = datosEnviar.length > 0;
        while (st.hasMoreTokens() && correcto){
            String cadena = st.nextToken();
            if (cadena.length() == numBits){
                this.cadenaaEnviar.add(cadena);
                //datosEnviar[i] = traduceString(cadena);
                correcto = traduceString(cadena) >= 0;
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
       String datoaenviar;
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
                this.enviarBinaria(datoaenviar);//TODO divido 32
                String c = this.recibirBinaria(this.li_bits_salida);
                this.ata_textarea.append(c + "\n");
                this.ata_textarea.setCaretPosition(this.ata_textarea.getText().length());
                // bits ebn 4 grupos y los envío
              //  this.interfaz.setNumeroInst(intruccion);
                this.ljtfield.setText(Integer.toString(intruccion));
                //Thread.sleep(5000);
            } catch (Exception ex) {
                Logger.getLogger(Ejecucion.class.getName()).log(Level.SEVERE, null, ex);
            }
           intruccion ++;
       }

    }
    public void pararrecepcionfpga(){
        this.ejecutando=false;
    }


    private String recibirBinaria(int numBitsSalida) throws Exception{
        int num ;
        String s = "";
        for (int i = 0; i < 5; i++){
            num= com1.receiveSingleDataInt();
            if (i > 0)
                s = this.convertirCadenaBinaria(num,8)+s;
        }

        System.out.println(s.substring(s.length()-numBitsSalida));
        return s.substring(s.length()-numBitsSalida);


    }


    private String convertirCadenaBinaria(int recibido,int numBits) {
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

}
