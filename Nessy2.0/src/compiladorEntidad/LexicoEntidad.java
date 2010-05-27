/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorEntidad;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
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
     * Código de la palabra reservada OUT  -> 6
     */
    public static final int DOWNTO = 7;
    /**
     * Código de la palabra reservada DOWNTO  -> 7
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
    private BufferedReader reader;
    private Errores errores;
    private Character ultimoCharLeido;
    private String cadena;
    private int numLinea;
    private int numColumna;
    private int estado;

    /**
     * Getter que devuelve el número de línea por la que va el Analizador.
     * @return Integer Número de línea.
     */
    public int getNumLinea() {
        return this.numLinea;
    }
    //Conjunto de dígitos
    private static final HashSet<Character> digitos = new HashSet<Character>(10);

    static {
        for (char c = '0'; c <= '9'; c++) {
            digitos.add(new Character(c));
        }
    }
    //Conjunto de letras
    private static final char[] listLetras = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final HashSet<Character> letras = new HashSet<Character>(listLetras.length);

    static {
        for (char ch : listLetras) {
            letras.add(new Character(ch));
        }
    }
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
        numColumna = 0;
        this.errores = errores;
        leerCaracter();
    }

    /**
     * Llama a la función sigToken()
     * @return El siguiente Token.
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

    private boolean esLetra(Character ch) {
        return letras.contains(ch);
    }

    private boolean esDigito(Character ch) {
        return digitos.contains(ch);
    }

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
                numColumna = 0;
            } 
            return ultimoCharLeido;
        } else {
            return null;
        }
    }

    /**
     * Método que devuelve el siguiente Token del Analizador Léxico.
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
                            return new Token(codigo, cadena, numLinea, numColumna);
                        } else {//si es el nombre de una entrada/salida/entidad
                            return new Token(IDENTIFICADOR, cadena, numLinea, numColumna);
                        }
                    }
                    break;
                case 2:
                    if (esDigito(caracterLeido)) {//continua el número
                        transita(2);
                    } else {//guardar el numero
                        return new Token(ENTERO, cadena, numLinea, numColumna);
                    }
                    break;
                case 3:
                    return new Token(PUNTO_Y_COMA, cadena, numLinea, numColumna);
                case 4:
                    return new Token(ABRE_PARENTESIS, cadena, numLinea, numColumna);
                case 5:
                    return new Token(CIERRA_PARENTESIS, cadena, numLinea, numColumna);
                case 6:
                    return new Token(EOF, cadena, numLinea, numColumna);
                case 7:
                    if (caracterLeido == '=') {
                        transita(11);
                    } else {
                        return new Token(DOS_PUNTOS, cadena, numLinea, numColumna);
                    }
                    break;
                case 8:
                    if (caracterLeido == '-') {
                        transita(9);
                    } else {
                        return new Token(RESTA, cadena, numLinea, numColumna);
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
                    return new Token(OTRO, cadena, numLinea, numColumna);
                case 11:
                    return new Token(ASIG_GENERIC, cadena, numLinea, numColumna);
                case 12:
                    return new Token(SUMA, cadena, numLinea, numColumna);
                case 13:
                    return new Token(MULT, cadena, numLinea, numColumna);
                case 14:
                    return new Token(DIV, cadena, numLinea, numColumna);
            }
        }
    }

    /**
     * Transita la máquina de estados del Analizador Léxico.
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
