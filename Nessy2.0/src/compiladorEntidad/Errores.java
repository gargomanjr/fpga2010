/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiladorEntidad;

import java.util.ArrayList;

/**
 *
 * @author Carlos
 */
public class Errores {


    private ArrayList<String> errores;

    public Errores(){
        errores = new ArrayList<String>();
    }

    public void error(String error){
        errores.add(error);
    }

    public ArrayList<String> getErrores() {
        return errores;
    }

}
