
package compiladorEntidad;

import java.util.ArrayList;

/**
 *
 * Clase para almacenar los errores de compilación que se puedan ir
 * produciendo
 * @author Carlos,Tony y David
 */
public class Errores {


    /**
     * Estructura que almacena los errores en forma de cadena
     */
    private ArrayList<String> errores;

    /**
     * Constructor de la Clase. Inicializa el atributo errores.
     */
    public Errores(){
        errores = new ArrayList<String>();
    }

    /**
     * Añade un nuevo error al arrayList errores, donde se encuentran todos los fallos.
     * @param error Nuevo error a insertar en la clase.
     */
    public void error(String error){
        errores.add(error);
    }

    /**
     * Devuelve todos los errores que tiene la clase.
     * @return ArrayList con todos los errores de la clase.
     */
    public ArrayList<String> getErrores() {
        return errores;
    }

}
