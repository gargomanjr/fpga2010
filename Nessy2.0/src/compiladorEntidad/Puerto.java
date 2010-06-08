package compiladorEntidad;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Representa el puerto de una entidad el cual puede ser tanto
 * una entrada como una salida
 * @author Carlos, David y Tony
 */
public abstract class Puerto {

    /**
     * Número de bits del puerto (entrada o salida)
     */
    private int numBits;

    /**
     * Nombre del puerto
     */
    private String nombre;

    /**
     * Método que obtiene el nombre del Puerto.
     * @return El nombre del Puerto.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del Puerto.
     * @param nombre Nuevo nombre del puerto.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    /**
     * Consulta el número de bits que utiliza el puerto.
     * @return Número de bits del puerto.
     */
    public int getNumBits() {
        return numBits;
    }

    /**
     * Establece el número de bits del Puerto
     * @param numBits Nuevo número de bits del Puerto.
     */
    public void setNumBits(int numBits) {
        this.numBits = numBits;
    }





}
