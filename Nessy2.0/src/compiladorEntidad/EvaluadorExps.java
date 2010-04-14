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

    public static boolean esOperador(int t){
        return t == (LexicoEntidad.SUMA) || t == (LexicoEntidad.RESTA) ||
                t == (LexicoEntidad.MULT) || t == (LexicoEntidad.DIV);
    }

    public static boolean esOperando(int t){
        return t == LexicoEntidad.ENTERO || t == LexicoEntidad.IDENTIFICADOR;
    }

    public static boolean esMenorIg(int t1, int t2){
        return t1 == t2 ||
                ((t1 == LexicoEntidad.SUMA || t1 == LexicoEntidad.RESTA) && (t1 == LexicoEntidad.SUMA || t1 == LexicoEntidad.RESTA)) ||
                (t2 == LexicoEntidad.MULT ||t2 == LexicoEntidad.DIV);
    }

    public static int aplicar(int x, int op1, int op2){
        switch(x){
            case LexicoEntidad.SUMA:
                return op1 + op2;
            case LexicoEntidad.RESTA:
                return op1 - op2;
            case LexicoEntidad.MULT:
                return op1 * op2;
            case LexicoEntidad.DIV:
                return op1 / op2;
            default:
                return -1;
        }
    }

}
