
package compiladorEntidad;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que analiza sintácticamente una entidad definida en VHDL. Además
 * mientras la analiza, almacena los datos leídos en nuestra estructura de datos
 * de la entidad. La forma en la que funciona este analizador sintáctico está
 * determinada en la gramática que se especifica en la memoria.
 *
 * @author Carlos,David y Tony
 */
public class SintacticoEntidad {

    /**
     * Número de entradas máximo. En nuestra Aplicación son 32
     */
    public static final int MAX_ENTRADAS = 32;
    /**
     * Número de salidas máximo. En nuestra Aplicación son 32
     */
    public static final int MAX_SALIDAS = 32;

    /**
     * Token actual que se está analizando
     */
    private Token token;

    /**
     * Entidad donde se va a almacenar lo leido del fichero
     */
    private Entidad entidad;

    /**
     * Analizador léxico que nos va a proveer de los tokens
     */
    private LexicoEntidad lector;

    /**
     * Posibles errores que se puedan producir
     */
    private Errores errores;

    /**
     * Tabla de símbolos necesaria para almacenar las variables
     * de los posibles generics
     */
    private HashMap<String,Integer> tablaSimbolos;

    /**
     * Getter para consultar la Entidad.
     * @return Devuelve el Objeto de la clase Entidad
     */
    public Entidad getEntidad() {
        return entidad;
    }

    /**
     * Constructor de la clase
     * @param fichero Ruta del fichero que vamos a analizar sintácticamente.
     * @param errores ArrayList de errores encontrados
     * @throws IOException
     */
    public SintacticoEntidad(String fichero, Errores errores) throws IOException {
        lector = new LexicoEntidad(fichero, errores);
        this.errores = errores;
        entidad = new Entidad();//creamos una entidad vacía
        tablaSimbolos = new HashMap<String,Integer>();

     }

   /**
    * Inicia el análisis sintáctico.
    * @throws IOException
    */
   public void inicia() throws IOException {
        token = lector.iniciar();
    }

   /**
    * Función para cerrar el fichero que estamos analizando.
    */
   public void cerrar() {
        try {
            lector.cerrar();
        } catch (IOException ex) {
            errores.error("Error al leer el fichero de entity");
        }
    }

   /**
    * Lee del fichero mientras no encuentra la palabra 'Entity'. Con esto
    * descartamos todos los comentarios iniciales además de la incluisión de
    * librerías además de cualquier otra cosa que no nos interese del fichero.
    *
    * @throws IOException
    */
   public void Cabecera() throws IOException{
        while(token != null && token.getCodigo() != LexicoEntidad.ENTITY
                && token.getCodigo() != LexicoEntidad.GENERIC){
            token = lector.sigToken();
        }
    }

    /**
     * Analiza la entidad del fichero.
     * @return Devuelve True si ha habido algún error, si no devuelve Falso.
     * @throws Exception
     */
    public boolean Entidad() throws Exception{
        Cabecera();
        boolean error = false;
        empareja(LexicoEntidad.ENTITY);
        String nomEntidadInicio = token.getLexema();
        empareja(LexicoEntidad.IDENTIFICADOR);
        empareja(LexicoEntidad.IS);
        if (token.getCodigo() == LexicoEntidad.GENERIC){
            error = error | Generic();
        }
        error = error | Puertos();
        empareja(LexicoEntidad.END);
        String nomEntidadEnd = token.getLexema();
        empareja(LexicoEntidad.IDENTIFICADOR);
        if (nomEntidadInicio.equals(nomEntidadEnd)){
            entidad.setNombre(nomEntidadInicio);//daría igula uno que otro
        }else{
            errores.error("El nombre de la entidad no coincide");
            error = true;
        }
        return error;
    }

    /**
     * Analiza la parte de un componente genérico.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean Generic() throws Exception{
        empareja(LexicoEntidad.GENERIC);
        empareja(LexicoEntidad.ABRE_PARENTESIS);
        boolean error = Variables();
        empareja(LexicoEntidad.CIERRA_PARENTESIS);
        empareja(LexicoEntidad.PUNTO_Y_COMA);
        return error;
    }

    /**
     * Analiza la parte de declaración de variables dentro de un genérico.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean Variables() throws Exception{
        boolean error = Variable();
        error = error | RVariables();
        return error;
    }

    /**
     * Analiza la declaración de una variable y la inserta en la tabla de símbolos.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean Variable() throws Exception{
        String var = token.getLexema();
        empareja(LexicoEntidad.IDENTIFICADOR);
        empareja(LexicoEntidad.DOS_PUNTOS);
        empareja(LexicoEntidad.IN);
        empareja(LexicoEntidad.INTEGER);
        empareja(LexicoEntidad.ASIG_GENERIC);
        String valor = token.getLexema();
        empareja(LexicoEntidad.ENTERO);
        tablaSimbolos.put(var,new Integer(valor));
        return false;
    }

    /**
     * Analiza el resto de variables.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean RVariables() throws Exception{
        boolean error = false;
        if (token.getCodigo() == LexicoEntidad.PUNTO_Y_COMA){
            empareja(LexicoEntidad.PUNTO_Y_COMA);
            error = Variable() | RVariables();
        }
        return error;
    }

    /**
     * Analiza la parte correspondiente a los puertos de entrada y salida
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean Puertos() throws Exception{
        empareja(LexicoEntidad.PORT);
        empareja(LexicoEntidad.ABRE_PARENTESIS);
        boolean error = Senales();
        empareja(LexicoEntidad.CIERRA_PARENTESIS);
        empareja(LexicoEntidad.PUNTO_Y_COMA);
        return error;
    }

    /**
     * Analiza la parte donde se declaran todas las señales.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean Senales() throws Exception{
        boolean error = Senal();
        error = error | RSenales();
        return error;
    }

    /**
     * Analiza si está bien declarada una señal. Además la añade
     * a la entidad. Si el número de entradas o de salidas es excedido
     * se mostrará el error al final del análisis.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean Senal() throws Exception{
        String ident = token.getLexema();
        boolean error = false;
        int entradaSalida;
        empareja(LexicoEntidad.IDENTIFICADOR);
        empareja(LexicoEntidad.DOS_PUNTOS);
        entradaSalida = token.getCodigo();
        if (entradaSalida == LexicoEntidad.IN || entradaSalida== LexicoEntidad.OUT){
            empareja(entradaSalida);
        }else{
            throw new Exception("Error sintactico en la fila " + token.getNumLinea() +  ". No se esperaba \"" + token.getLexema() + "\".");
        }
        int tamano = Tipo();
        if (tamano > 0){
            Puerto es = null;
            if (entradaSalida == LexicoEntidad.IN){//si es una entrada
                es = new Entrada();
                es.setNombre(ident);
                es.setNumBits(tamano);
                entidad.anadeEntrada((Entrada)es);
                if (entidad.getBitsEntrada() > MAX_ENTRADAS){
                    error = true;
                    errores.error("Error al añadir la entrada: " + es.getNombre()+". No se permite un número de entradas mayor de " + MAX_ENTRADAS);
                }
            }else if(entradaSalida == LexicoEntidad.OUT){//si es una salida
                es = new Salida();
                es.setNombre(ident);
                es.setNumBits(tamano);
                entidad.anadeSalida((Salida)es);
                if (entidad.getBitsSalida() > MAX_SALIDAS){
                    error = true;
                    errores.error("Error al añadir la salida: " + es.getNombre()+". No se permite un número de salidas mayor de " + MAX_SALIDAS);
                }
            }
           

        }else{
            errores.error("Linea: " + token.getNumLinea() + ". Tamaño incorrecto");//es un error que permite continuar
            error = true;
        }
            tamano = 0;
        return error;
    }

    /**
     * Analiza el resto de señales.
     * @return Cierto si ha habido algún error o falso si está correcto.
     * @throws Exception
     */
    public boolean RSenales() throws Exception{
        boolean error = false;
        if (token.getCodigo() == LexicoEntidad.PUNTO_Y_COMA){
            empareja(LexicoEntidad.PUNTO_Y_COMA);
            error = Senal() | RSenales();
        }
        return error;
    }

    /**
     * Analiza la parte correspondiente al tipo de una señal. Ésta puede ser
     * STD_LOGIC o STD_LOGIC_VECTOR. En función de ello, devuelve el tamaño
     * de esa entrada o salida.
     * @return devuelve el tamaño de una entrada o salida.
     * @throws Exception
     */
    public int Tipo() throws Exception{
        int tamano = 0;
        if (token.getCodigo() == LexicoEntidad.STD_LOGIC){
            empareja(LexicoEntidad.STD_LOGIC);
            tamano = 1;
        }else if(token.getCodigo() == LexicoEntidad.STD_LOGIC_VECTOR){
            empareja(LexicoEntidad.STD_LOGIC_VECTOR);
            empareja(LexicoEntidad.ABRE_PARENTESIS);
            int inicio = Exp();
            empareja(LexicoEntidad.DOWNTO);
            int fin = 0;//TODO
            empareja(LexicoEntidad.ENTERO);
            if (inicio >= 0 && fin >= 0)
                tamano = inicio - fin +1;
            empareja(LexicoEntidad.CIERRA_PARENTESIS);
        }else{
            throw new Exception("Error sintactico en la fila " + token.getNumLinea() + ". No se esperaba \"" + token.getLexema() + "\".");
        }
        return tamano;
    }

    /**
     * Analiza una expresión aritmética.
     * @return Devuelve el valor de la expresión.
     * @throws Exception
     */
    public int Exp() throws Exception{
        return evaluar();
    }

    /**
     * Transforma una expresión a PostFija para poder evaluarla.
     * @return Devuelve el ArrayList con la expresión transformada a PostFija.
     * @throws Exception
     */
    public ArrayList<Token> pasarAPostFija() throws Exception{
        Pila<Token> pila = new Pila<Token>();
        boolean finExp = false;
        ArrayList<Token> post = new ArrayList<Token>();
        Token t;
        while (!finExp){
            t = new Token(token);//hace una copia del token
            if (EvaluadorExps.esOperando(t.getCodigo())){
                post.add(t);
                empareja(t.getCodigo());
            }else if(t.getCodigo() == LexicoEntidad.ABRE_PARENTESIS){
                pila.apilar(t);
                empareja(LexicoEntidad.ABRE_PARENTESIS);
            }else if(t.getCodigo() == LexicoEntidad.CIERRA_PARENTESIS){
                while (pila.getCima().getCodigo() != LexicoEntidad.ABRE_PARENTESIS){
                    post.add(pila.desapilar());
                }
                pila.desapilar();
                empareja(LexicoEntidad.CIERRA_PARENTESIS);
            }else if(EvaluadorExps.esOperador(t.getCodigo())){
                while(!pila.esVacia() && EvaluadorExps.esMenorIg(t.getCodigo(),pila.getCima().getCodigo())){
                    post.add(pila.desapilar());
                }
                pila.apilar(t);
                empareja(t.getCodigo());
            }else{
                finExp = true;
            }
        }
        while (!pila.esVacia()){
            post.add(pila.desapilar());
        }
        return post;
    }

    /**
     * Evalúa una expresión y devuelve el resultado.
     * @return El resultado de la expresión.
     * @throws Exception
     */
    public int evaluar() throws Exception{
        int valor = -1;
        Pila<Integer> pila = new Pila<Integer>();
        ArrayList<Token> post = pasarAPostFija();
        int i = 0;
        Token t;
        while (i < post.size()){
            t = post.get(i);
            i++;
            if (t.getCodigo() == LexicoEntidad.IDENTIFICADOR){
                if (tablaSimbolos.get(t.getLexema()) != null){
                    int v = tablaSimbolos.get(t.getLexema()); //el valor de la variable
                    pila.apilar(v);
                }else{
                    errores.error("La variable " + t.getLexema() + " no está definida");
                    return -1;
                }
            }else if(t.getCodigo() == LexicoEntidad.ENTERO){
                pila.apilar(Integer.parseInt(t.getLexema()));//sabemos seguro que es un entero
            }else{ //operador
                int op2 = pila.desapilar();
                int op1 = pila.desapilar();
                valor = EvaluadorExps.aplicar(t.getCodigo(),op1,op2);
                pila.apilar(valor);
            }
        }
        return pila.desapilar();
    }

    

    /**
     * Intenta emparejar el token actual del analizador con el del fichero. Además
     * actualiza el siguiente token que se debe leer, pidiéndoselo al analizador
     * léxico.
     * @param tk Token a emparejar
     * @throws Exception
     */
    public void empareja(int tk) throws Exception {
        if (token != null)//si no ha habido error lexico
        {
            if (tk == token.getCodigo()) {
                token = lector.sigToken();
            } else {
                errorSint();
            }
        }else{
            throw new Exception("Fila " + lector.getNumLinea() + ": Error, caracter " + lector.getUltimoCharLeido() + " desconocido");
        }
        return;
    }


    /**
     * Añade el error al atributo errores y lanza la excepción indicando el error.
     * @throws Exception
     */
    public void errorSint() throws Exception{
       errores.error("Error sintactico en la fila " + token.getNumLinea() + ". No se esperaba \"" + token.getLexema() + "\".");
       throw new Exception("Error sintactico en la fila " + token.getNumLinea() + ". No se esperaba \"" + token.getLexema() + "\".");
    }




}
