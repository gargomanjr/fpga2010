/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiladorEntidad;

/**
 *
 * @author User
 */
public class EvaluadorExps {

    public static int evaluarExpresion(){

        return -1;
    }

    /*public static String pasarAPostFija(String cadena){
        Pila pila = new Pila();
        String post = "";
        char c;
        int i = 0;
        while (i < cadena.length()){
            c = cadena.charAt(i);
            i++;
            if (esNumero(c)){
                post = post+c;
            }else if(c == '('){
                pila.apilar(c+"");
            }else if(c == ')'){
                while (pila.getCima() != '('){
                    post = post + pila.desapilar();
                }
                pila.desapilar();
            }else if(esOperador(c)){

            }
        }
        return post;
    }*/

    public boolean esOperador(int t){
        return t == (LexicoEntidad.SUMA) || t == (LexicoEntidad.RESTA) ||
                t == (LexicoEntidad.MULT) || t == (LexicoEntidad.DIV);
    }

    public boolean esOperando(int t){
        return t == LexicoEntidad.ENTERO || t == LexicoEntidad.IDENTIFICADOR;
    }

    public static boolean esNumero(char c){
        return c >= '0' && c <= '9';
    }

    public static boolean esOperador(char c){
        return c == '+' || c == '-' || c == '/' || c =='*';
    }

}
