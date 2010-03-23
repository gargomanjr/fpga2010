package compiladorEntidad;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Vellisco
 */
public class Token {

    private String lexema;
    private int codigo;
    private int numLinea;
    private int numColumna;

    public Token(int codigo, String lexema, int numLinea, int numColumna) {

        this.codigo = codigo;
       /*if (codigo == 1) //Si es un ID
        {*/
            this.lexema = lexema.toUpperCase();
        /*} else {
            this.lexema = lexema;
        }*/
        this.numLinea = numLinea;
        this.numColumna = numColumna;
        //System.out.println("Token("+codigo+","+lexema+","+numLinea+","+numColumna+")");

    }

    public int getCodigo() {
        return codigo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getNumLinea() {
        return numLinea;
    }

    public int getNumColumna() {
        return numColumna;
    }
}
