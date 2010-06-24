/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IOFPGA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;
import nessy20.GUIPrincipal;
import nessy20.SeleccionNumIter;
import org.apache.log4j.Logger;

/**
 * Clase que contiene el proceso en el que se incluye el algoritmo para la
 * reconfiguración parcial de la FPGA. Necesita ser creado en una nueva clase
 * que herede de un hilo, de tal forma que la ejecución pueda ser parada en
 * cualquier momento desde la interfaz gráfica de usuario.
 *
 * @author Carlos, Tony, David
 */
public class ReconfiguracionParcial extends Thread {


    private static Logger log = Logger.getLogger(ReconfiguracionParcial.class);
    /**
     * Necesitamos tener la interfaz gráfica ya que contiene algunos
     * métodos de los que vamos a utilizar.
     */
    private GUIPrincipal gui;
    /**
     * Indica si se está ejecutando la reconfiguración para poder parar
     * en cualquier momento el proceso
     */
    private boolean ejecutandoReconfiguracion;
    /**
     * Panel que será activado mientras se ejecuta el proceso
     */
    private javax.swing.JPanel panelOutPut;
    /**
     * La ruta del fichero .BIT que se va a modificar
     */
    private String rutaBit;
    /**
     * Número de iteraciones que se quiere ejecutar la reconfiguración parcial
     */
    private int numIteraciones;

    /**
     * Inicia el hilo de la reconfiguración parcial
     * @param in_gui GUI de entrada a copiar
     * @param rutaBit Ruta del .BIT a modificar
     */
    public ReconfiguracionParcial(GUIPrincipal in_gui, String rutaBit) {
        this.rutaBit = rutaBit;
        this.gui = in_gui;
        this.numIteraciones = 10;
    }

    /**
     * Método que contiene el algoritmo que ejecuta la reconfiguración parcial
     * sucesiva.En primer lugar se cargará una entidad seguida de su fichero
     * .BIT correspondiente. A continuación se pedirá al usuario un fichero
     * para cargar el banco de pruebas. Tras cargarlo, y sabiendo que en la
     * FPGA está cargado el fichero .BIT correcto (sin modificar), se generará
     * una salida especial, llamada salida Golden, la cual será con la que
     * comparemos en el resto de ejecuciones. Este paso sólo lo realizaremos
     * una vez, al comienzo del proceso.
     * A continuación entraremos en un bucle, en el cual se irá iterando para cada bit
     * y para cada frame distinto. En cada una de las iteraciones se ejecutará
     * la aplicación de reconfiguración aplicada siempre al mismo fichero .BIT
     * original (introducido por el usuario) en el que iremos cambiando los
     * parámetros de frame y bit. Cada vez que se ejecute la aplicación se
     * generarán dos ficheros de configuración: fichero_modif.bit y fichero_
     * modifRestore.bit. Cargaremos el primer fichero en la FPGA, lo que es
     * equivalente a que una partícula solar modificara el valor del bit de la
     * LUT que estamos modificando. Una vez inyectado el error (cargado el
     * fichero de configuración) volveremos a ejecutar el circuito en la FPGA,
     * con las mismas entradas con las que habíamos generado la salida Golden.
     * Compararemos las nuevas salidas obtenidas con la salida Golden para ver
     * si el error ha incidido en la ejecución del circuito.
     * Después tendremos que devolver a la FPGA a su “estado anterior”.
     * Es decir, tenemos que restaurar el bit que acabamos de modificar.
     * Para ello ordenaremos la carga del segundo fichero que se había generado:
     * fichero_modifRestore.bit.

     * @return true si la ejecución ha sido correcta y false en caso contrario
     */
    public boolean reconfiguracionParcial() {
        int numBits = 32;
        int numFrames = 36194;

        boolean b = false;
        ejecutandoReconfiguracion = true;
        if (gui.cargarBitConChooser() && gui.SeleccionTBModifFichero()) {
            gui.setEnabledBtnDetenerInyeccion(true);
            gui.seleccionaPanel(panelOutPut);
            int frame = 0;
            int bit = 0;
            try {

                FileWriter fw = new FileWriter(new File("salidas//logEjec.txt"));
                String fichero = gui.getFichero_bit();
                gui.setFwLog(fw);
                while (frame < numFrames && ejecutandoReconfiguracion) {
                    bit = 0;
                    while (bit < numBits && ejecutandoReconfiguracion) {
                        fw.write("\n\nModificando FRAME: " + frame + " BIT: " + bit + "\n");
                        log.info("\n\n********** Modificando FRAME: " + frame + " BIT: " + bit + " ************");

                        String coms = "cmd.exe /K java -jar Virtex_II_Partial_Reconfiguration.jar -i " + fichero + " -o " + rutaBit + "\\circuito_fpga_modif -f " + frame + " -b " + bit;
                        fw.write("Ejecutando: " + coms + "\n");
                        log.info("Modificando .bit");
                        Process p = Runtime.getRuntime().exec(coms);
                        log.info("Cargando BIT: " + rutaBit + "\\circuito_fpga_modif.bit");
                        fw.write("Cargando BIT: " + rutaBit + "\\circuito_fpga_modif.bit\n");
                        if (this.gui.getCom1() == null) {
                            gui.inicializarPuertoSerie();
                        }
                        gui.cargarBit(rutaBit + "\\circuito_fpga_modif.bit", false);
                        if (this.gui.getCom1() == null) {
                            fw.write("Ejecutando...\n");
                            if (gui.inicializarPuertoSerie()) {
                                gui.ejec(true);
                            }
                        } else {
                            gui.ejec(true);
                        }
                        log.info("Cargando BIT: " + rutaBit + "\\circuito_fpga_modifRestorer.bit");
                        fw.write("Cargando BIT: " + rutaBit + "\\circuito_fpga_modifRestorer.bit\n");
                        if (this.gui.getCom1() == null) {
                            gui.inicializarPuertoSerie();
                        }
                        b = gui.cargarBit(rutaBit + "\\circuito_fpga_modifRestorer.bit", false);

                        bit++;
                    }
                    frame++;
                }
                fw.close();
            } catch (FileNotFoundException ex) {
                //                        Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (IOException ex) {
                //     Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }/* catch (InterruptedException ex) {
            return false;
            }*/
        }

        ejecutandoReconfiguracion = false;
        return b;
    }

    public boolean reconfiguracionParcialAleatoria() {
        int numBits = 32;
        int numFrames = 36194;
        int numIteracion = 0;
        boolean errorCarga = false;
        ejecutandoReconfiguracion = true;
        int iter=gui.dameNumIter();
        if (iter > 0) {
            this.numIteraciones = iter;
            errorCarga = !gui.cargarBitConChooser();
            if (!errorCarga && gui.SeleccionTBModifFichero()) {


                gui.seleccionaPanel(panelOutPut);
                //  gui.setEnabledBtnDetenerInyeccion(true);
                int frame = 0;
                int bit = 0;
                try {
                    FileWriter fw = new FileWriter(new File("salidas//logEjec.txt"));
                    String fichero = gui.getFichero_bit();
                    gui.setFwLog(fw);
                    while (numIteracion < this.numIteraciones && ejecutandoReconfiguracion && !errorCarga) {
                        bit = (int) (Math.random() * numBits);
                        frame = (int) (Math.random() * numFrames);

                        fw.write("\n\nModificando FRAME: " + frame + " BIT: " + bit + "\n");
                        log.info("\n\nIteracion: " + numIteracion + "\n********** Modificando FRAME: " + frame + " BIT: " + bit + " ************");

                        String coms = "cmd.exe /K java -jar Virtex_II_Partial_Reconfiguration.jar -i " + fichero + " -o " + rutaBit + "\\circuito_fpga_modif -f " + frame + " -b " + bit;
                        fw.write("Ejecutando: " + coms + "\n");
                        log.info("Modificando .bit");
                        Process p = Runtime.getRuntime().exec(coms);
                        log.info("Cargando BIT: " + rutaBit + "\\circuito_fpga_modif.bit");
                        fw.write("Cargando BIT: " + rutaBit + "\\circuito_fpga_modif.bit\n");
                        if (this.gui.getCom1() == null) {
                            gui.inicializarPuertoSerie();
                        }
                        errorCarga = errorCarga || !gui.cargarBit(rutaBit + "\\circuito_fpga_modif.bit", false);
                        if (this.gui.getCom1() == null) {
                            fw.write("Ejecutando...\n");
                            if (gui.inicializarPuertoSerie()) {
                                gui.ejec(true);
                            }
                        } else {
                            gui.ejec(true);
                        }
                        log.info("Cargando BIT: " + rutaBit + "\\circuito_fpga_modifRestorer.bit");
                        fw.write("Cargando BIT: " + rutaBit + "\\circuito_fpga_modifRestorer.bit\n");
                        if (this.gui.getCom1() == null) {
                            gui.inicializarPuertoSerie();
                        }
                        errorCarga = errorCarga || !gui.cargarBit(rutaBit + "\\circuito_fpga_modifRestorer.bit", false);
                        numIteracion++;
                    }
                    if (errorCarga){
                        JOptionPane.showMessageDialog(gui, "Error durante el proceso de inyección de errores. No ha finalizado correctamente", "Información", JOptionPane.ERROR_MESSAGE);
                    }
                    fw.close();
                    gui.setEnabledBtnDetenerInyeccion(false);
                } catch (FileNotFoundException ex) {
                    gui.setInyeccErr(false);
                    //                        Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                     gui.setInyeccErr(false);
                    //     Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        gui.setInyeccErr(false);
        return !errorCarga;
    }


@Override
    public void

run() {
        synchronized (this) {
            this.reconfiguracionParcialAleatoria();
        }

}

    /**
     * Detiene el proceso de reconfiguración parcial sucesiva
     */
    public void

pararreconfiguracionparcial() {
        ejecutandoReconfiguracion = false;
    }
}
