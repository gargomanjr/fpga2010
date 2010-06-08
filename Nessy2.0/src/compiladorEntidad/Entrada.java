package compiladorEntidad;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Carlos, David y Tony
 */
public class Entrada extends Puerto{


    private boolean esReloj;
    private boolean esReset;

    /**
     * Método de consulta para saber si la Entrada es el reloj
     * @return Cierto o Falso dependiendo si se trata o no del reloj.
     */
    public boolean getEsReloj() {
        return esReloj;
    }

    /**
     * Método que establece la Entrada como reloj.
     */
    public void ponerComoReloj() {
        this.esReloj = true;
    }
    
    /**
     * Método para quitar a la Entrada la propiedad de que es el reloj.
     */
    public void quitarComoReloj(){
        this.esReloj = false;
    }
}
