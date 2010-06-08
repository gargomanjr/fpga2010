package compiladorEntidad;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Clase que analiza léxicamente un fichero en el que está contenida
 * una entidad en lenguaje VHDL
 *
 * @author Carlos, David y Tony
 */
public class LexicoEntidad {

    /*Código de las palabras reservadas */
    /**
     * Código de la palabra reservada Entidad  -> 0
     */
    public static final int ENTITY = 0;

    /**
     * Código de la palabra reservada IS  -> 1
     */

    public static final int IS = 1;

    /**
     * Código de la palabra reservada Port  -> 2
     */
    public static final int PORT = 2;

    /**
     * Código de la palabra reservada STD_LOGIC  -> 3
     */
    public static final int STD_LOGIC = 3;

    /**
     * Código de la palabra reservada STD_LOGIC_VECTOR  -> 4
     */
    public static final int STD_LOGIC_VECTOR = 4;

    /**
     * Código de la palabra reservada IN  -> 5
     */
    public static final int IN = 5;

    /**
     * Código de la palabra reservada OUT  -> 6
     */
    public static final int OUT = 6;

    /**
     * Código de la palabra reservada DOWNTO  -> 7
     */
    public static final int DOWNTO = 7;

    /**
     * Código de la palabra reservada END  -> 8
     */
    public static final int END = 8;

    /**
     * Código del elemento PUNTO_Y_COMA  -> 9
     */
    public static final int PUNTO_Y_COMA = 9;

    /**
     * Código del elemento DOS_PUNTOS  -> 10
     */
    public static final int DOS_PUNTOS = 10;

    /**
     * Código del elemento ABRE_PARENTESIS  -> 11
     */
    public static final int ABRE_PARENTESIS = 11;

    /**
     * Código del elemento CIERRA_PARENTESIS  -> 12
     */
    public static final int CIERRA_PARENTESIS = 12;

    /**
     * Código del elemento ENTERO  -> 13
     */
    public static final int ENTERO = 13;

    /**
     * Código del elemento IDENTIFICADOR  -> 14
     */
    public static final int IDENTIFICADOR = 14;

    /**
     * Código del elemento EOF  -> 15
     */
    public static final int EOF = 15;

    /**
     * Código del elemento OTRO  -> 16
     */
    public static final int OTRO = 16;

    /**
     * Código del elemento GENERIC  -> 17
     */
    public static final int GENERIC = 17;

    /**
     * Código del elemento INTEGER  -> 18
     */
    public static final int INTEGER = 18;

    /**
     * Código del elemento ASIG_GENERIC  -> 19
     */
    public static final int ASIG_GENERIC = 19;

    /**
     * Código del elemento SUMA  -> 20
     */
    public static final int SUMA = 20;

    /**
     * Código del elemento RESTA  -> 21
     */
    public static final int RESTA = 21;

    /**
     * Código del elemento MULT  -> 22
     */
    public static final int MULT = 22;

    /**
     * Código del elemento DIV  -> 23
     */
    public static final int DIV = 23;

    /**
     * Lector de fichero
     */
    private BufferedReader reader;

    /**
     * Posibles errores
     */
    private Errores errores;

    /**
     * Almacena el último caracter leído del fichero
     */
    private Character ultimoCharLeido;

    /**
     * Almacena la cadena leída hasta el momento
     */
    private String cadena;

    /**
     * Número de linea que se está leyendo
     */
    private int numLinea;
    private int estado;

    /**
     * Getter que devuelve el número de línea por la que va el Analizador.
     * @return Integer Número de línea.
     */
    public int getNumLinea() {
        return this.numLinea;
    }

    /**
     * Estructura que almacena el conjunto de palabras reservadas
     */
    private static final HashMap<String, Integer> palabrasReservadas = new HashMap<String, Integer>(25);

    static {
        palabrasReservadas.put("ENTITY", ENTITY);
        palabrasReservadas.put("IS", IS);
        palabrasReservadas.put("PORT", PORT);
        palabrasReservadas.put("STD_LOGIC", STD_LOGIC);
        palabrasReservadas.put("STD_LOGIC_VECTOR", STD_LOGIC_VECTOR);
        palabrasReservadas.put("IN", IN);
        palabrasReservadas.put("OUT", OUT);
        palabrasReservadas.put("DOWNTO", DOWNTO);
        palabrasReservadas.put("END", END);
        palabrasReservadas.put("GENERIC", GENERIC);
        palabrasReservadas.put("INTEGER", INTEGER);
    }
    /**
     * Constructor de la clase.
     * @param fichero Código a analizar.
     * @param errores Errores que se han detectado
     * @throws FileNotFoundException
     * @throws IOException
     */
    public LexicoEntidad(String fichero, Errores errores) throws FileNotFoundException, IOException {
        reader = new BufferedReader(new FileReader(fichero));
        numLinea = 1;
        this.errores = errores;
        leerCaracter();
    }

    /**
     * Inicia el análisis, llama a la función sigToken()
     * @return El primer Token.
     * @throws IOException
     */
    public Token iniciar() throws IOException {
        return sigToken();
    }

    /**
     * Cierra el Buffer de lectura del fichero.
     * @throws IOException
     */
    public void cerrar() throws IOException {
        this.reader.close();
    }

    /**
     * Para saber si un caracter determinado es una letra
     * @param ch El caracter del que se quiere saber si es una letra
     * @return true si ch es una letra y false en caso contrario
     */
    private boolean esLetra(Character ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /**
     * Para saber si un caracter determinado es un dígito
     * @param ch El caracter del que se quiere saber si es un dígito
     * @return true si ch es un dígito y false en caso contrario
     */
    private boolean esDigito(Character ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Lee un caracter del fichero y lo devuelve
     * @return El caracter leído o null en caso de que no se haya podido leer
     * @throws IOException
     */
    private Character leerCaracter() throws IOException {
        if (reader == null) {
            errores.error("No se asigno ningún reader");
            throw new IOException("No se asigno ningún reader");
        }
        if (reader.ready()) {
            int ch = reader.read();
            if (ch != -1) //si no es fin de fichero
            {
                ultimoCharLeido = new Character((char) ch);
            } else {
                ultimoCharLeido = null;
            }

            if (ultimoCharLeido.charValue() == '\n') { //Si es un salto de lÃ­nea
                numLinea++; //incrementa la linea
            } 
            return ultimoCharLeido;
        } else {
            return null;
        }
    }

    /**
     * Método que gestiona el autómata finito que ocntrola el Analizador Léxico
     * @return El siguiente Token a leer.
     * @throws IOException
     */
    public Token sigToken() throws IOException {
        estado = 0;
        cadena = "";
        while (true) {
            char caracterLeido = 0;
            if (ultimoCharLeido != null) {
                caracterLeido = Character.toLowerCase(ultimoCharLeido);
            }
            switch (estado) {
                case 0:
                    if (this.ultimoCharLeido == null) {
                        transita(6);

                    } else if ((ultimoCharLeido.charValue() == ' ') || (ultimoCharLeido.charValue() == '\n') || (ultimoCharLeido.charValue() == '\r') || (ultimoCharLeido.charValue() == '\t')) {
                        transita(0); //salta los blancos
                        cadena = "";
                    } else if (esLetra(caracterLeido)) {
                        transita(1);
                    } else if (esDigito(caracterLeido)) {
                        transita(2);
                    } else if (caracterLeido == ';') {
                        transita(3);
                    } else if (caracterLeido == '(') {
                        transita(4);
                    } else if (caracterLeido == ')') {
                        transita(5);
                    } else if (caracterLeido == ':') {
                        transita(7);
                    } else if (caracterLeido == '-') {//posible comentario
                        transita(8);
                    } else if (caracterLeido == '+') {
                        transita(12);
                    } else if (caracterLeido == '*') {
                        transita(13);
                    }else if (caracterLeido == '/') {
                        transita(14);
                    } else {//caracter desconocido
                        transita(10);
                    }
                    break;
                case 1:
                    if (esLetra(caracterLeido) || esDigito(caracterLeido) || caracterLeido == '_') {
                        transita(1);
                    } else {
                        Integer codigo = palabrasReservadas.get(cadena.toUpperCase());
                        if (codigo != null) {//si es una palabra reservada
                            return new Token(codigo, cadena, numLinea);
                        } else {//si es el nombre de una entrada/salida/entidad
                            return new Token(IDENTIFICADOR, cadena, numLinea);
                        }
                    }
                    break;
                case 2:
                    if (esDigito(caracterLeido)) {//continua el número
                        transita(2);
                    } else {//guardar el numero
                        return new Token(ENTERO, cadena, numLinea);
                    }
                    break;
                case 3:
                    return new Token(PUNTO_Y_COMA, cadena, numLinea);
                case 4:
                    return new Token(ABRE_PARENTESIS, cadena, numLinea);
                case 5:
                    return new Token(CIERRA_PARENTESIS, cadena, numLinea);
                case 6:
                    return new Token(EOF, cadena, numLinea);
                case 7:
                    if (caracterLeido == '=') {
                        transita(11);
                    } else {
                        return new Token(DOS_PUNTOS, cadena, numLinea);
                    }
                    break;
                case 8:
                    if (caracterLeido == '-') {
                        transita(9);
                    } else {
                        return new Token(RESTA, cadena, numLinea);
                    }
                    break;
                case 9://comentarios
                    if (caracterLeido == '\n' || caracterLeido == '\r') {
                        transita(0);
                    } else {
                        transita(9);
                    }
                    break;
                case 10://desconocido
                    return new Token(OTRO, cadena, numLinea);
                case 11:
                    return new Token(ASIG_GENERIC, cadena, numLinea);
                case 12:
                    return new Token(SUMA, cadena, numLinea);
                case 13:
                    return new Token(MULT, cadena, numLinea);
                case 14:
                    return new Token(DIV, cadena, numLinea);
            }
        }
    }

    /**
     * Cambia el estado actual del analizador léxico por el que se le pasa por
     * parámetro. Además lee el siguiente caracter para dejarlo preparado para
     * ser analizado.
     * @param sigEstado Siguiente estado al que va el analizador.
     * @throws IOException
     */
    public void transita(int sigEstado) throws IOException {
        estado = sigEstado;
        if (ultimoCharLeido != null) {
            cadena = cadena.concat(ultimoCharLeido.toString());
            ultimoCharLeido = leerCaracter();
        }
    }

    /**
     * Método de Consulta sobre el último carácter leído.
     * @return El último carácter leído.
     */
    public Character getUltimoCharLeido() {
        return ultimoCharLeido;
    }
}
