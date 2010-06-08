package compiladorEntidad;


/**
 *
 * @author Carlos, Tony y David
 */
public class Token {

    private String lexema;
    private int codigo;
    private int numLinea;

    /**
     * Constructor de la clase.
     * @param codigo Código del Token.
     * @param lexema String del Token
     * @param numLinea Entero que indica la línea donde se encuentra el token dentro del fichero.
     * @param numColumna Entero donde se encuentra el token dentro del fichero.
     */
    public Token(int codigo, String lexema, int numLinea) {

        this.codigo = codigo;
        this.lexema = lexema.toUpperCase();
        this.numLinea = numLinea;
        //System.out.println("Token("+codigo+","+lexema+","+numLinea+","+numColumna+")");

    }

    /**
     * Contructura que crea una copia de un Token a partir de otro.
     * @param otro Token a copiar.
     */
    public Token(Token otro){
        this.codigo = otro.codigo;
        this.lexema = otro.lexema;
        this.numLinea = otro.numLinea;
    }

    /**
     * Obtiene el código de un Token
     * @return Entero que codifica el Token.
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Obtiene la cadena del String.
     * @return El String del Token asociado.
     */
    public String getLexema() {
        return lexema;
    }

    /**
     * Obtiene el número de línea donde se encuentra el Token.
     * @return El entero con la línea donde se encuentra el Token.
     */
    public int getNumLinea() {
        return numLinea;
    }
}
