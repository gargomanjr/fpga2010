package nessy20;


import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class CargaBit {

    private final static String RUTA_IMPACT = "C://Xilinx82//bin//nt//impact.exe";

    //private String rutaBatch;
    private String ficheroBit;

    GUIPrincipal interfaz;
   /* public String getRutaBatch() {
        return rutaBatch;
    }

    public void setRutaBit(String rutaBatch) {
        this.rutaBatch = rutaBatch;
    }*/


    CargaBit(GUIPrincipal interfaz, String fich){
        this.ficheroBit = fich;
        this.interfaz = interfaz;
   
        String prueba = Dir_impact();

    }

    /**
     *
     * @param ruta
     * @return
     */
    public boolean existeFichero(String ruta){
        File fichero = new File(ruta);
        return fichero.exists();
    }

    /**
     *
     * @return
     * @throws java.io.FileNotFoundException
     */
    public boolean cargar() throws FileNotFoundException{
        boolean correcto = true;


        
        if (existeFichero(RUTA_IMPACT)){
            try{
                interfaz.escribirEnPantalla("Cargando... "+ ficheroBit);
                FileOutputStream os = new FileOutputStream("carga2.txt");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                String coms = "setMode -ss \nsetMode -sm \n" +
                        "setMode -sm \nsetMode -hw140 \nsetMode -spi\nsetMode -acecf\nsetMode -acempm\nsetMode -pff\n" +
                        "setMode -bs\nsetMode -bs\nsetCable -port auto\nIdentify\naddDevice -p 3 -file \""+ ficheroBit+"\"\n" +
                        "deleteDevice -p 4\nProgram -p 3 -defaultVersion 0\nexit";
                bw.write(coms);
                bw.close();
                Process p = Runtime.getRuntime().exec(RUTA_IMPACT+ " -batch carga2.txt");
                
                
                InputStream is = p.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
                String s=br.readLine();
                boolean errorCarga = true;
                while(s!=null && errorCarga){
                   // System.out.println(s);
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
    /**
     *
     * @return
     */
    public static String Dir_impact() {
       return System.getenv("PATH");
    }

}
