/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nessy20;

/**
 * Clase donde tenemos los tipos de selecciones que se hacen en la aplicación.
 * Ya sea al elegir el tipo de carga de VHDL's o del TestBench
 * @author David,Tony y Carlos
 */
public class Seleccion {

    /**
     * Seleccion de VHD
     */
    public SeleccionNumIter selIter;

    public int numIter;

    public SeleccionCargaVHD seleccion;
    /**
     * Seleccion de carga de banco de pruebas
     */

    public SeleccionTB selTB;
    /**
     *
     */
    
    public Seleccion()
    {
        numIter=1;
        selIter=SeleccionNumIter.NADA;
        seleccion=SeleccionCargaVHD.NADA;
        selTB=SeleccionTB.NADA;
    }
}
