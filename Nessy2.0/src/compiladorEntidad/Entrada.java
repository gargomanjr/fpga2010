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

    private boolean esReset;

    public boolean getEsReloj() {
        return esReloj;
    }

    public void ponerComoReloj() {
        this.esReloj = true;
    }
    
    public void quitarComoReloj(){
        this.esReloj = false;
    }

    public boolean getEsReset(){
        return this.esReset;
    }

    public void ponerComoReset(boolean valor){
        this.esReset = valor;
    }




    
    

}
