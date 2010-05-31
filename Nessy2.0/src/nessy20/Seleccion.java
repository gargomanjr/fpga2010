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
     *
     */
    public SeleccionCargaVHD seleccion;
    /**
     *
     */
    public SeleccionTB selTB;
    /**
     *
     */
    public Seleccion()
    {
        seleccion=SeleccionCargaVHD.NADA;
        selTB=SeleccionTB.NADA;
    }
}
