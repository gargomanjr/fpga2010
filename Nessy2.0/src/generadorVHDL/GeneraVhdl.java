/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package generadorVHDL;

import compiladorEntidad.*;
import java.io.*;

/**
 *
 * @author Carlos
 */
public class GeneraVhdl {

    private Entidad entidad;
    private String nomEntidadGeneral;
    private BufferedWriter bw;
    private String fichero;
    private Errores errores;

    public GeneraVhdl(String fichero, Entidad entidad, Errores errores) throws IOException {
        this.fichero = fichero;
        this.entidad = entidad;
        this.errores = errores;
        nomEntidadGeneral = "Circuito_FPGA"; //de momento lo llamamos asi
    }

    public boolean abrir() {
        boolean correcto = fichero != null;
        try {
            bw = new BufferedWriter(new FileWriter(fichero));
        } catch (IOException e) {
            errores.error("Error al abrir el fichero " + fichero);
            correcto = false;
        }
        return correcto;
    }

    public boolean cerrar() {
        boolean correcto = bw != null;
        try {
            bw.close();
        } catch (IOException e) {
            errores.error("Error al cerrar el fichero " + fichero);
            correcto = false;
        }
        return correcto;
    }

    private void escribirLinea(String linea) {
        try {
            bw.write(linea);
            bw.newLine();
        } catch (IOException ex) {
            errores.error("Error al escribir en fichero");
        }
    }

    private boolean hayReloj(){
        int i = 0;
        boolean encontrado = false;
        while (i < this.entidad.getBitsEntrada() && !encontrado){
            encontrado = this.entidad.getEntrada(i).getEsReloj();
            i++;
        }
        return encontrado;
    }

    private String tipoPuerto(Puerto p) {
        int numBits = p.getNumBits();
        if (numBits > 1) {
            return "STD_LOGIC_VECTOR(" + (numBits - 1) + " downto " + "0)";
        }
        return "STD_LOGIC";
    }

    private String listaSenales() {
        String s = "(";
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            Entrada e = entidad.getEntrada(i);
            //if (!e.getEsReloj())// si no es la entrada de reloj//
            s += "mi_" + e.getNombre() + ",";
            //else
            //    s += "clk,";
        }
        for (int i = 0; i < entidad.getNumSalidas() - 1; i++) {
            Salida sal = entidad.getSalida(i);
            s += "mi_" + sal.getNombre() + ",";
        }//la entidad tiene que tener al menos alguna salida
        s += "mi_" + entidad.getSalida(entidad.getNumSalidas() - 1).getNombre() + ")";
        return s;
    }

    private void librerias() {
        escribirLinea("library IEEE;");
        escribirLinea("use IEEE.STD_LOGIC_1164.ALL;");
        escribirLinea("use IEEE.STD_LOGIC_ARITH.ALL;");
        escribirLinea("use IEEE.STD_LOGIC_UNSIGNED.ALL;");
        escribirLinea("");
        escribirLinea("");
    }

    private void entidadGeneral() {
        escribirLinea("");
        escribirLinea("entity " + nomEntidadGeneral + " is");
        escribirLinea("Port ( clk : in  STD_LOGIC;");
        escribirLinea("reset : in  STD_LOGIC;");
        escribirLinea("salida_serie : out  STD_LOGIC;");
        escribirLinea("entrada_serie : in  STD_LOGIC);");
        escribirLinea("end " + nomEntidadGeneral + ";");
        escribirLinea("");
    }

    private void inicioArquitectura() {
        escribirLinea("");
        escribirLinea("architecture Behavioral of " + nomEntidadGeneral + " is");
        escribirLinea("");
    }

    private void compSalidaSerie() {
        escribirLinea("");
        escribirLinea("component Tx_serie");
        escribirLinea("\tPort ( RstN : in  STD_LOGIC;");
        escribirLinea("\tclk : in  STD_LOGIC;");
        escribirLinea("\tTransmite : in  STD_LOGIC;");
        escribirLinea("\tDatoTxIn : in  STD_LOGIC_VECTOR (7 downto 0);");
        escribirLinea("\tTransmitiendo : out  STD_LOGIC;");
        escribirLinea("\tDatoSerieOut : out  STD_LOGIC);");
        escribirLinea("end component;");
        escribirLinea("");
    }

    private void compEntradaSerie() {
        escribirLinea("");
        escribirLinea("component Rx_Serie");
        escribirLinea("\tPort ( Rstn : in  STD_LOGIC;");
        escribirLinea("\tClk : in  STD_LOGIC;");
        escribirLinea("\tRxDatoSerie : in  STD_LOGIC;");
        escribirLinea("\tDatoRxOut : out  STD_LOGIC_VECTOR (7 downto 0);");
        escribirLinea("\tAvisoRx : out  STD_LOGIC;");
        escribirLinea("\tRecibiendo : out  STD_LOGIC);");
        escribirLinea("end component ;");
        escribirLinea("");
    }

    private void componentePrincipal() {
        escribirLinea("");
        escribirLinea("component " + entidad.getNombre());
        escribirLinea("\tPort(");
        //Escribir las entradas
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            escribirLinea("\t\t" + entidad.getEntrada(i).getNombre() + ": in " + tipoPuerto(entidad.getEntrada(i)) + ";");
        }
        escribirLinea("");
        //Escribir las salidas
        for (int i = 0; i < entidad.getNumSalidas() - 1; i++) {
            escribirLinea("\t\t" + entidad.getSalida(i).getNombre() + ": out " + tipoPuerto(entidad.getSalida(i)) + ";");
        }
        escribirLinea("\t\t" + entidad.getSalida(entidad.getNumSalidas() - 1).getNombre() + ": out " + tipoPuerto(entidad.getSalida(entidad.getNumSalidas() - 1)));
        escribirLinea(");");
        escribirLinea("end component;");
        escribirLinea("");
    }

    private void senalEnableGeneral() {
        escribirLinea("");
        escribirLinea("signal enable: std_logic;  --señal enable general");
        escribirLinea("");
    }

    private void SenalesCircuitoPrincipal() {
        escribirLinea("");
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            Entrada e = entidad.getEntrada(i);
            //if (!e.getEsReloj())//si no es la entrada de reloj
            escribirLinea("signal mi_" + e.getNombre() + ": " + tipoPuerto(e) + ";");
        }
        escribirLinea("");
        for (int i = 0; i < entidad.getNumSalidas(); i++) {
            Salida s = entidad.getSalida(i);
            escribirLinea("signal mi_" + s.getNombre() + ": " + tipoPuerto(s) + ";");
        }
        escribirLinea("");
    }

    private void SenalesEntradaSerie() {
        escribirLinea("");
        escribirLinea("signal mi_resetserie:std_logic;");
        escribirLinea("signal mi_transmite:std_logic;");
        escribirLinea("signal mi_datotxin:std_logic_vector(7 downto 0);");
        escribirLinea("signal mi_datorxout:std_logic_vector(7 downto 0);");
        escribirLinea("signal mi_avisoRx:std_logic;");
        escribirLinea("signal mi_recibiendo:std_logic;");
        escribirLinea("");
    }

    private void SenalesSalidaSerie() {
        escribirLinea("");
        escribirLinea("signal mi_transmitiendo:std_logic;");
        escribirLinea("signal mi_datoserieout:std_logic;");
        escribirLinea("signal mi_rxdatoserie:std_logic;");
        escribirLinea("");
    }

    private void regsEntradaSalida() {
        escribirLinea("");
        escribirLinea("signal Reg_entradas: std_logic_vector(7 downto 0);");
        escribirLinea("signal Reg_salidas: std_logic_vector(7 downto 0);");
        escribirLinea("");
    }

    private void begin() {
        escribirLinea("");
        escribirLinea("begin");
        escribirLinea("");
    }

    private void asigSenalesCompsSerie() {
        escribirLinea("");
        escribirLinea("f: Tx_serie port map(mi_resetserie,clk,not mi_recibiendo,mi_datotxin,mi_transmitiendo,mi_datoserieout);");
        escribirLinea("R: Rx_serie port map(mi_resetserie,clk,mi_rxdatoserie,mi_datorxout,mi_avisorx,mi_recibiendo);");
        escribirLinea("");
    }

    private void asigSenalesCompPrinc() {
        escribirLinea("");
        escribirLinea("U: " + entidad.getNombre() + " port map" + listaSenales() + ";");
        escribirLinea("");
    }

    private void entSalPuertoSerie() {
        escribirLinea("");
        escribirLinea("mi_resetserie <= reset;");
        escribirLinea("salida_serie<=mi_datoserieout;");
        escribirLinea("mi_rxdatoserie <= entrada_serie;");
        escribirLinea("");
    }

    private void procesoEntradas() {
        escribirLinea("");
        escribirLinea("process(mi_recibiendo)");
        escribirLinea("begin");
        escribirLinea("\tif mi_recibiendo = '0' then");
        escribirLinea("\t\tReg_entradas <= mi_datorxout;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");

    }

    private void procesoSalidas() {
        escribirLinea("");
        escribirLinea("process(mi_transmitiendo)");
        escribirLinea("begin");
        escribirLinea("\tif mi_transmitiendo = '0' then");
        escribirLinea("\t\tmi_datotxin <= Reg_salidas;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesoEnable() {
        if (hayReloj()) {
            escribirLinea("");
            escribirLinea("process(ENABLE)");
            escribirLinea("begin");
            escribirLinea("\tif (enable = '0') then");
            escribirLinea("\t\tmi_clk <= '0';");
            escribirLinea("\telse");
            escribirLinea("\t\tmi_clk <= clk;");
            escribirLinea("\tend if;");
            escribirLinea("end process;");
            escribirLinea("");
        }
    }

    private void asigEntradasCircuitoPrinc() {
        escribirLinea("");
        int numEntrada = 0;
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            Entrada e = entidad.getEntrada(i);
            if (!e.getEsReloj()) {
                if (e.getNumBits() > 1) {
                    for (int j = 0; j < e.getNumBits(); j++) {
                        escribirLinea("mi_" + e.getNombre() + "(" + j + ") <= Reg_entradas(" + numEntrada++ + ");");
                    }

                } else {
                    escribirLinea("mi_" + e.getNombre() + " <= Reg_entradas(" + numEntrada++ + ");");
                }
            }
        }
        escribirLinea("enable <= Reg_entradas(" + numEntrada + ");");
        escribirLinea("");
    }

    private void asigSalidasCircuitoPrinc() {
        escribirLinea("");
        int numSalida = 0;
        for (int i = 0; i < entidad.getNumSalidas(); i++) {
            Salida s = entidad.getSalida(i);
            if (s.getNumBits() > 1) {
                for (int j = 0; j < s.getNumBits(); j++) {
                    escribirLinea("Reg_salidas(" + numSalida++ + ") <= mi_" + s.getNombre() + "(" + j + ");");
                }
            } else {
                escribirLinea("Reg_salidas(" + numSalida++ + ") <= mi_" + s.getNombre() + ";");
            }
        }
        escribirLinea("");
    }

    private void end() {
        escribirLinea("");
        escribirLinea("end Behavioral;");
        escribirLinea("");
    }

    public void crearFichero() {
        librerias();
        entidadGeneral();
        inicioArquitectura();
        compSalidaSerie();
        compEntradaSerie();
        componentePrincipal();
        senalEnableGeneral();
        SenalesCircuitoPrincipal();
        SenalesEntradaSerie();
        SenalesSalidaSerie();
        regsEntradaSalida();
        begin();
        asigSenalesCompsSerie();
        asigSenalesCompPrinc();
        entSalPuertoSerie();
        procesoEntradas();
        procesoSalidas();
        procesoEnable();
        asigEntradasCircuitoPrinc();
        asigSalidasCircuitoPrinc();
        end();
    }
}
