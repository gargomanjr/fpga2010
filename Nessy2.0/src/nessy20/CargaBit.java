package nessy20;


import java.io.*;
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
     * @return Cierto si se ha conseguido cargar correctamente, falso en caso contrario.
     * @throws java.io.FileNotFoundException
     */
    public boolean cargar(boolean escribirEnPantalla) throws FileNotFoundException{
        boolean correcto = true;
     
        if (existeFichero(rutaImpact)){
            try{
                interfaz.escribirEnPantalla("Cargando... "+ ficheroBit);
                FileOutputStream os = new FileOutputStream("carga2.txt");
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
                rutaImpact = "C://Xilinx82//bin//nt//impact.exe";
                Process p = Runtime.getRuntime().exec(rutaImpact+ " -batch carga2.txt");
                
                
                InputStream is = p.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
                String s=br.readLine();
                boolean errorCarga = true;
                while(s!=null && errorCarga){
                    if(escribirEnPantalla)
                        interfaz.escribirEnPantalla(s);
                    if (s.contains("Programmed successfully")){
                        errorCarga = false;
                    }                   
                    s=br.readLine();                   
                }
                correcto = !errorCarga;

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

}
