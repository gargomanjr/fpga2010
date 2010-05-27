/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiladorEntidad;

import java.util.ArrayList;

/**
 *
 * @param <T>
 * @author User
 */
public class Pila<T> {

    private int cima;

    private ArrayList<T> pila;

    /**
     * Método constructor de la clase.
     */
    public Pila(){
        pila = new ArrayList<T>();
        cima = -1;
    }

    /**
     * Método para saber si es pila vacía.
     * @return Boolean si es pila vacía.
     */
    public boolean esVacia(){
        return cima == -1;
    }

    /**
     * Apilar un nuevo elemento en la pila.
     * @param t Elemento a apilar.
     */
    public void apilar(T t){
        pila.add(t);
        cima++;
    }

    /**
     * Desapila un elemento de la pila
     * @return devuelve el elemento desapilado.
     */
    public T desapilar(){
        if (!esVacia()){
            return pila.remove(cima--);
        }
        return null;
    }

    /**
     * Consulta la cima de la pila
     * @return el elemento de la cima de la Pila
     */
    public T getCima(){
        if (!esVacia()){
            return pila.get(cima);
        }
        return null;
    }

    /**
     * Método que consulta el número de elementos de la pila.
     * @return El número de elementos de la Pila.
     */
    public int numElems(){
        return cima+1;
    }

}
