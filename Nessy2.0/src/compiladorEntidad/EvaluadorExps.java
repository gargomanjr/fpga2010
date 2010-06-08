
package compiladorEntidad;

/**
 * Clase que contiene métodos para evaluar una expresión aritmética
 *
 * @author Carlos, David y Tony.
 */
public class EvaluadorExps {

    /**
     * Método que devuelve un booleano dependiendo si la entrada es un Operador
     * @param t Entero a consultar.
     * @return Booleano que indica si la entrada es un Operador o no.
     */
    public static boolean esOperador(int t){
        return t == (LexicoEntidad.SUMA) || t == (LexicoEntidad.RESTA) ||
                t == (LexicoEntidad.MULT) || t == (LexicoEntidad.DIV);
    }

    /**
     * Método que devuelve un booleano dependiendo si la entrada es un Operando
     * @param t Entero a consultar.
     * @return Booleano que indica si la entrada es un Operando o no.
     */
    public static boolean esOperando(int t){
        return t == LexicoEntidad.ENTERO || t == LexicoEntidad.IDENTIFICADOR;
    }

    /**
     * Método de consulta de preferencia de operadores.
     * @param t1 Primer Operador.
     * @param t2 Segundo Operador.
     * @return Bololean Devuelve true si el primer operador tiene menor o igual preferencia
     * que el segundo operador.
     */
    public static boolean esMenorIg(int t1, int t2){
        return t1 == t2 ||
                ((t1 == LexicoEntidad.SUMA || t1 == LexicoEntidad.RESTA) && (t1 == LexicoEntidad.SUMA || t1 == LexicoEntidad.RESTA)) ||
                (t2 == LexicoEntidad.MULT ||t2 == LexicoEntidad.DIV);
    }

    /**
     * Reduce una Expresión y devuelve el resultado
     * @param x Operador a aplicar.
     * @param op1 Primer operando.
     * @param op2 Segundo operando.
     * @return Resultado de la operación.
     */
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
