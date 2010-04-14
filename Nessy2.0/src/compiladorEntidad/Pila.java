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
public class Pila<T> {

    private int cima;

    ArrayList<T> pila;

    public Pila(){
        pila = new ArrayList<T>();
        cima = -1;
    }

    public boolean esVacia(){
        return cima == -1;
    }

    public void apilar(T t){
        pila.set(++cima, t);
    }

    public T desapilar(){
        if (!esVacia()){
            return pila.remove(cima--);
        }
        return null;
    }

    public T getCima(){
        if (!esVacia()){
            return pila.get(cima--);
        }
        return null;
    }

    public int numElems(){
        return cima+1;
    }

}
