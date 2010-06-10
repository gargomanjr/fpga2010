package compiladorEntidad;


/**
 * Clase que representa la entrada de una entidad
 *
 * @author Carlos, David y Tony
 */
public class Entrada extends Puerto{


    /**
     * Indica si es una entrada de reloj
     */
    private boolean esReloj;

    /**
     * Indica si es una entrada de reset
     */
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

    /**
     * Getter de para saber si la entrada se trata del reset.
     * @return Boolean si es cierto que la entrada es un reset.
     */
    public boolean getEsReset(){
        return this.esReset;
    }

    /**
     * Establece la entrada como Reset o le quita la propiedad.
     * @param valor Nuevo valor para el parámetro esReset.
     */
    public void ponerComoReset(boolean valor){
        this.esReset = valor;
    }    

}
