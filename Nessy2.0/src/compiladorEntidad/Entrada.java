package compiladorEntidad;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Carlos
 */
public class Entrada extends Puerto{


    private boolean esReloj;

    public boolean getEsReloj() {
        return esReloj;
    }

    public void ponerComoReloj() {
        this.esReloj = true;
    }
    
    public void quitarComoReloj(){
        this.esReloj = false;
    }




    
    

}
