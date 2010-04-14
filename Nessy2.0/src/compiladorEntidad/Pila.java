/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiladorEntidad;

import java.util.ArrayList;

/**
 *
 * @author User
 */
public class Pila {

    int cima;

    ArrayList<String> pila;

    public Pila(){
        pila = new ArrayList<String>();
        cima = -1;
    }

    public boolean esVacia(){
        return cima == -1;
    }

    public void apilar(String c){
        pila.set(++cima, c);
    }

    public String desapilar(){
        if (!esVacia()){
            return pila.remove(cima--);
        }
        return null;
    }

    public String getCima(){
        if (!esVacia()){
            return pila.get(cima--);
        }
        return null;
    }

    public int numElems(){
        return cima+1;
    }

}
