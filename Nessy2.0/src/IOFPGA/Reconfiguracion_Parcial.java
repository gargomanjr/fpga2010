/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package IOFPGA;

import app.Com;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import nessy20.GUIPrincipal;

/**
 *
 * @author User
 */
public class Reconfiguracion_Parcial extends Thread{

    private GUIPrincipal gui;
    private boolean ejecutandoReconfiguracion ;
    private javax.swing.JPanel panelOutPut;
    private String Ruta;
    private String ArchivoBit;
    private Com com1;

    public Reconfiguracion_Parcial(GUIPrincipal in_gui,String RutaIO,String in_archivobit){
        Ruta = RutaIO;
        gui = in_gui;
        ArchivoBit = in_archivobit;
    }

     public boolean procesoModificarFicheros() {
        int numBits = 32;
        int numFrames = 36194;
        /*int numBits = 3;
        int numFrames = 1;*/

        boolean b=false;
        ejecutandoReconfiguracion = true;
        if (gui.cargarBitConChooser() && gui.SeleccionTBModifFichero()){
            gui.seleccionaPanel(panelOutPut);
            int frame = 0;
            int bit = 0;
            try {

            FileWriter fw = new FileWriter(new File("test//logEjec.txt"));
            String fichero = gui.getFichero_bit();
            gui.setFw(fw);
            while (frame < numFrames && ejecutandoReconfiguracion){
                bit = 0;
                while (bit < numBits && ejecutandoReconfiguracion){                                          
                        fw.write("\n\nModificando FRAME: " + frame + " BIT: " + bit+"\n");
                        System.out.println("\n\n********** Modificando FRAME: " + frame + " BIT: " + bit+" ************");

                        String coms = "cmd.exe /C start java -jar Virtex_II_Partial_Reconfiguration.jar -i "+fichero+" -o "+Ruta+"\\circuito_fpga_modif -f "+ frame +" -b "+ bit;
                        fw.write("Ejecutando: " + coms+"\n");
                        System.out.println("Modificando .bit");
                        Process p = Runtime.getRuntime().exec(coms);

                        System.out.println("Cargando BIT: " + Ruta+"\\circuito_fpga_modif.bit");
                        fw.write("Cargando BIT: " + Ruta+"\\circuito_fpga_modif.bit\n");
                        if (this.gui.getCom1() == null)
                            gui.inicializarPuertoSerie();
                        gui.cargarBit(Ruta+"\\circuito_fpga_modif.bit",false);
                        if (this.gui.getCom1() == null) {
                            fw.write("Ejecutando...\n");
                            if (gui.inicializarPuertoSerie()) {
                                 gui.ejec(true);
                            }
                        } else {
                            gui.ejec(true);
                        }
                        System.out.println("Cargando BIT: " + Ruta+"\\circuito_fpga_modifRestorer.bit");
                        fw.write("Cargando BIT: " + Ruta+"\\circuito_fpga_modifRestorer.bit\n");
                        if (this.gui.getCom1() == null)
                            gui.inicializarPuertoSerie();
                        b = gui.cargarBit(Ruta+"\\circuito_fpga_modifRestorer.bit",false);

                    bit ++;
                }
                frame ++;
            }
            fw.close();
            } catch (FileNotFoundException ex) {
    //                        Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                     return false;
            } catch (IOException ex) {
               //     Logger.getLogger(GUIPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
            }
        }
        
        ejecutandoReconfiguracion = false;
        return b;
    }
    @Override
    public void run() {
        synchronized (this) {
             this.procesoModificarFicheros();
        }
    }
    public void pararreconfiguracionparcial() {
        ejecutandoReconfiguracion = false;
    }
}
