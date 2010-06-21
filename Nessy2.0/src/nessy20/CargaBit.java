package nessy20;


import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Clase para que contiene la funcionalidad necesaria para
 * cargar un archivo .BIT con la aplicación IMPACT
 * @author User
 */
public class CargaBit {

    //private final static String RUTA_IMPACT = "C://Xilinx82//bin//nt//impact.exe";

    private String ficheroBit;

    GUIPrincipal interfaz;

    String rutaImpact;

    private boolean SalidaCargaBit;



    /**
     * Constructor de la clase.
     * @param interfaz Interfaz sobre la que estamos ejecutando
     * @param fich String con la ruta del fichero .bit que deseamos cargar.
     */
    CargaBit(GUIPrincipal interfaz, String fich, String rutaImpact){
        this.ficheroBit = fich;
        this.interfaz = interfaz;
        this.rutaImpact = rutaImpact;


    }

    /**
     * Comprueba si existe o no un fichero.
     * @param ruta Cadena de la ruta del fichero
     * @return Boolean,cierto si existe el fichero falso en caso contrario.
     */
    public boolean existeFichero(String ruta){
        File fichero = new File(ruta);
        return fichero.exists();
    }

    /**
     * Procedimiento que carga un archivo .bit en la FPGA.
     * Ejecuta el script de carga el archivp .bit y lanza dos hilos para
     * leer la salida que ha generado esta ejecución. Uno lee la salida de error
     * y otro la salida estándar.
     * @param escribirEnPantalla Para no escribir la salida en la pantalla
     * en caso de que estemos en el proceso de reconfiguración.
     * @return Cierto si se ha conseguido cargar correctamente, falso en caso contrario.
     * @throws java.io.FileNotFoundException
     */
    public boolean cargar(boolean escribirEnPantalla) throws FileNotFoundException{
        boolean correcto = true;
     
        if (existeFichero(rutaImpact)){
            try{
                SalidaCargaBit = false;
                interfaz.escribirEnPantalla("Cargando... "+ ficheroBit);
                FileOutputStream os = new FileOutputStream("carga.txt");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                /*String coms = "setMode -ss \nsetMode -sm \n" +
                        "setMode -sm \nsetMode -hw140 \nsetMode -spi\nsetMode -acecf\nsetMode -acempm\nsetMode -pff\n" +
                        "setMode -bs\nsetMode -bs\nsetCable -port auto\nIdentify\naddDevice -p 3 -file \""+ ficheroBit+"\"\n" +
                        "deleteDevice -p 4\nProgram -p 3 -defaultVersion 0\nexit";*/
                String coms = "setMode -bs \n" +
                        "setCable -port auto\n" +
                        "Identify\n" +
                        "identifyMPM\n" +
                        "assignFile -p 3 -file \""+ ficheroBit+"\"\n" +
                        "Program -p 3\n" +
                        "exit";
                bw.write(coms);
                bw.close();
                //rutaImpact = "C://Xilinx82//bin//nt//impact.exe";
                //rutaImpact = "C://Xilinx10.1//ISE//bin//nt//impact.exe";
                Process p;

                p = Runtime.getRuntime().exec(rutaImpact + " -batch carga.txt");
                
                HiloSalidaStandarCargarBit hilo = new HiloSalidaStandarCargarBit(interfaz,escribirEnPantalla,p,this);
                hilo.start();
                HiloSalidaErrorCargarBit hilo2 = new HiloSalidaErrorCargarBit(interfaz,escribirEnPantalla,p);
                hilo2.start();
                try {
                    hilo.join();
                    hilo2.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(CargaBit.class.getName()).log(Level.SEVERE, null, ex);
                }
                interfaz.escribirEnPantalla("Termina completamente la carga del archivo .Bit");
                return SalidaCargaBit;

            }catch(IOException e){
                correcto = false;
            }
        }else{
            correcto = false;
        }

        if (correcto){
            System.out.println("Programada correctamente");
        }else{
            System.out.println("Error al cargar la FPGA");
        }

        return correcto;
    }
    /**
     * Parámetro que establece si la carga del archivo .bit ha sido correcta.
     * @param SalidaCargaBit Boolean con el nuevo valor que indica si la
     * carga del archivo .bit ha sido correcta
     */
    public void setSalidaCargaBit(boolean SalidaCargaBit) {
        this.SalidaCargaBit = SalidaCargaBit;
    }
}
