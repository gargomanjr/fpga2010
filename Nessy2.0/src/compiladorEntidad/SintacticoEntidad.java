/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiladorEntidad;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Carlos
 */
public class SintacticoEntidad {

    public static final int MAX_ENTRADAS = 8;
    public static final int MAX_SALIDAS = 8;

    private Token token;
    private Entidad entidad;
    private LexicoEntidad lector;
    private Errores errores;
    private HashMap<String,Integer> tablaSimbolos;

    public Entidad getEntidad() {
        return entidad;
    }

   public SintacticoEntidad(BufferedReader brd, Errores errores) throws IOException {
        lector = new LexicoEntidad(brd, errores);
        this.errores = errores;
        entidad = new Entidad();//creamos una entidad vacía

     }

     public void inicia() throws IOException {
        token = lector.iniciar();
    }

    public void cerrar() {
        try {
            lector.cerrar();
        } catch (IOException ex) {
            errores.error("Error al leer el fichero de entity");
        }
    }

    public void Cabecera() throws IOException{
        while(token != null && token.getCodigo() != LexicoEntidad.ENTITY
                && token.getCodigo() != LexicoEntidad.GENERIC){
            token = lector.sigToken();
        }
    }

    public boolean Entidad() throws Exception{
        Cabecera();
        boolean error = false;
        /*if (token.getCodigo() == LexicoEntidad.GENERIC){
            error = error | Generic();
        }*/
        empareja(LexicoEntidad.ENTITY);
        String nomEntidadInicio = token.getLexema();
        empareja(LexicoEntidad.IDENTIFICADOR);
        empareja(LexicoEntidad.IS);
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

    public boolean Generic() throws Exception{
        empareja(LexicoEntidad.GENERIC);
        empareja(LexicoEntidad.ABRE_PARENTESIS);
        boolean error = Variables();
        empareja(LexicoEntidad.CIERRA_PARENTESIS);
        return error;
    }

    public boolean Variables() throws Exception{
        boolean error = Variable();
        error = error | RVariables();
        return error;
    }

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

    public boolean RVariables() throws Exception{
        boolean error = false;
        if (token.getCodigo() == LexicoEntidad.PUNTO_Y_COMA){
            empareja(LexicoEntidad.PUNTO_Y_COMA);
            error = Variable() | RVariables();
        }
        return error;
    }

    public boolean Puertos() throws Exception{
        empareja(LexicoEntidad.PORT);
        empareja(LexicoEntidad.ABRE_PARENTESIS);
        boolean error = Senales();
        empareja(LexicoEntidad.CIERRA_PARENTESIS);
        empareja(LexicoEntidad.PUNTO_Y_COMA);
        return error;
    }

    public boolean Senales() throws Exception{
        boolean error = Senal();
        error = error | RSenales();
        return error;
    }

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
            throw new Exception("Error sintactico en la fila " + token.getNumLinea() + ", columna " + token.getNumColumna() + ". No se esperaba \"" + token.getLexema() + "\".");
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
                    errores.error("No se permite un número de entradas mayor de " + MAX_ENTRADAS);
                }
            }else if(entradaSalida == LexicoEntidad.OUT){//si es una salida
                es = new Salida();
                es.setNombre(ident);
                es.setNumBits(tamano);
                entidad.anadeSalida((Salida)es);
                if (entidad.getBitsSalida() > MAX_SALIDAS){
                    error = true;
                    errores.error("No se permite un número de salidas mayor de " + MAX_SALIDAS);
                }
            }
           

        }else{
            errores.error("Linea: " + token.getNumLinea() + ". Tamaño incorrecto");//es un error que permite continuar
            error = true;
        }
            tamano = 0;
        return error;
    }

    public boolean RSenales() throws Exception{
        boolean error = false;
        if (token.getCodigo() == LexicoEntidad.PUNTO_Y_COMA){
            empareja(LexicoEntidad.PUNTO_Y_COMA);
            error = Senal() | RSenales();
        }
        return error;
    }

    public int Tipo() throws Exception{
        int tamano = 0;
        if (token.getCodigo() == LexicoEntidad.STD_LOGIC){
            empareja(LexicoEntidad.STD_LOGIC);
            tamano = 1;
        }else if(token.getCodigo() == LexicoEntidad.STD_LOGIC_VECTOR){
            empareja(LexicoEntidad.STD_LOGIC_VECTOR);
            empareja(LexicoEntidad.ABRE_PARENTESIS);
            String cadenaEnt1, cadenaEnt2;
            cadenaEnt1 = token.getLexema();
            empareja(LexicoEntidad.ENTERO);
            empareja(LexicoEntidad.DOWNTO);
            cadenaEnt2 = token.getLexema();
            empareja(LexicoEntidad.ENTERO);
            //si ha llegado hasta aqui es que ambas cadenas son enteros
            tamano = Integer.parseInt(cadenaEnt1)- Integer.parseInt(cadenaEnt2)+1;
            empareja(LexicoEntidad.CIERRA_PARENTESIS);
        }else{
            throw new Exception("Error sintactico en la fila " + token.getNumLinea() + ", columna " + token.getNumColumna() + ". No se esperaba \"" + token.getLexema() + "\".");
        }
        return tamano;
    }

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


    public void errorSint() throws Exception{
       errores.error("Error sintactico en la fila " + token.getNumLinea() + ", columna " + token.getNumColumna() + ". No se esperaba \"" + token.getLexema() + "\".");
       throw new Exception("Error sintactico en la fila " + token.getNumLinea() + ", columna " + token.getNumColumna() + ". No se esperaba \"" + token.getLexema() + "\".");
    }




}
