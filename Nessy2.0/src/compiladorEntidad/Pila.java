/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiladorEntidad;

import java.util.ArrayList;

/**
 * Representa una pila de elementos genéricos.
 * Utilizada para la evaluación de expresiones.
 *
 *
 * @param <T>
 * @author User
 */
public class Pila<T> {

    /**
     * La posición el la que se encuentra la cima de la pila
     */
    private int cima;

    /**
     * Estructura para almacenar el contenido de la pila
     */
    private ArrayList<T> pila;

    /**
     * Método constructor de la clase. Crea una pila vacía.
     */
    public Pila(){
        pila = new ArrayList<T>();
        cima = -1;
    }

    /**
     * Método para saber si es pila vacía.
     * @return Boolean, true si es pila vacía, false en caso contrario.
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
     * @return Devuelve el elemento desapilado.
     */
    public T desapilar(){
        if (!esVacia()){
            return pila.remove(cima--);
        }
        return null;
    }

    /**
     * Consulta la cima de la pila
     * @return El elemento de la cima de la Pila
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
