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
        escribirLinea("entrada_serie : in  STD_LOGIC;");
        escribirLinea("ledsEntrada : out std_logic_vector(1 downto 0);");
        escribirLinea("ledsSalida : out std_logic_vector(1 downto 0));");
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
        escribirLinea("signal enable: std_logic;  --enable general");
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
        escribirLinea("signal Reg_entradas: std_logic_vector(31 downto 0);");
        escribirLinea("signal Reg_salidas: std_logic_vector(31 downto 0);");
        escribirLinea("");
    }

    private void SenalesEstados(){
        escribirLinea("");
        escribirLinea("signal estadoEnt: integer;");
        escribirLinea("signal estadoSal: integer;");
        escribirLinea("");
        escribirLinea("signal ledsEnt:std_logic_vector(1 downto 0);");
        escribirLinea("signal ledsSal:std_logic_vector(1 downto 0);");
        escribirLinea("");
    }

    private void SenalesIO(){
        escribirLinea("");
        escribirLinea("signal volcar : std_logic;");
        escribirLinea("signal recibido,transmitido,biest_recibido, biest_transmitido: std_logic;");
        escribirLinea("signal frecibido,ftransmitido,fin: std_logic;  --flancos de fin");
        escribirLinea("");

    }

    private void begin() {
        escribirLinea("");
        escribirLinea("begin");
        escribirLinea("");
    }

    private void asigSenalesCompsSerie() {
        escribirLinea("");
        escribirLinea("f: Tx_serie port map(mi_resetserie,clk,mi_transmite,mi_datotxin,mi_transmitiendo,mi_datoserieout);");
        escribirLinea("R: Rx_serie port map(mi_resetserie,clk,mi_rxdatoserie,mi_datorxout,mi_avisorx,mi_recibiendo);");
        escribirLinea("");
    }

    private void asigSenalesCompPrinc() {
        escribirLinea("");
        escribirLinea("U: " + entidad.getNombre() + " port map" + listaSenales() + ";");
        escribirLinea("");
    }

    

    private void procesoEntradas() {
        escribirLinea("");
        escribirLinea("process(mi_recibiendo,mi_resetserie)");
        escribirLinea("begin");
        escribirLinea("\tif mi_resetserie = '0' then");
        escribirLinea("\t\testadoEnt <= 0;");
        escribirLinea("\t\tvolcar <= '0';");
        escribirLinea("\telsif mi_recibiendo'event and mi_recibiendo = '0' then");
        escribirLinea("\t\tvolcar <= '0';");
        escribirLinea("\t\tif estadoEnt = 0 then");
        escribirLinea("\t\t\tReg_entradas(7 downto 0) <= mi_datorxout;");
        escribirLinea("\t\t\testadoEnt <= 1;");
        escribirLinea("\t\telsif estadoEnt = 1 then");
        escribirLinea("\t\t\tReg_entradas(15 downto 8) <= mi_datorxout;");
        escribirLinea("\t\t\testadoEnt <= 2;");
        escribirLinea("\t\telsif estadoEnt = 2 then");
        escribirLinea("\t\t\tReg_entradas(23 downto 16) <= mi_datorxout;");
        escribirLinea("\t\t\testadoEnt <= 3;");
        escribirLinea("\t\telsif estadoEnt = 3 then");
        escribirLinea("\t\t\tReg_entradas(31 downto 24) <= mi_datorxout;");
        escribirLinea("\t\t\testadoEnt <= 0;");
        escribirLinea("\t\t\tvolcar <= '1';");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\tReg_entradas(7 downto 0) <= mi_datorxout;");
        escribirLinea("\t\t\testadoEnt <= 1;");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");

    }

    private void procesoSalidas() {
        escribirLinea("");
        escribirLinea("process(mi_transmitiendo,mi_resetserie)");
        escribirLinea("begin");
        escribirLinea("\tif mi_resetserie = '0' then");
        escribirLinea("\t\testadoSal<= 0;");
        escribirLinea("\t\ttransmitido <= '0';");
        escribirLinea("\telsif mi_transmitiendo'event and mi_transmitiendo = '0' then");
        escribirLinea("\t\ttransmitido <= '0';");
        escribirLinea("\t\tif estadoSal = 0 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(7 downto 0);");
        escribirLinea("\t\t\testadoSal <= 1;");
        escribirLinea("\t\telsif estadoSal = 1 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(15 downto 8);");
        escribirLinea("\t\t\testadoSal <= 2;");
        escribirLinea("\t\telsif estadoSal = 2 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(23 downto 16);");
        escribirLinea("\t\t\testadoSal <= 3;");
        escribirLinea("\t\telsif estadoSal = 3 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(31 downto 24);");
        escribirLinea("\t\t\testadoSal <= 4;");
        escribirLinea("\t\telsif estadoSal = 4 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(7 downto 0);");
        escribirLinea("\t\t\testadoSal <= 0;");
        escribirLinea("\t\t\ttransmitido <= '1';");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(7 downto 0);");
        escribirLinea("\t\t\testadoSal <= 1;");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesoVolcar(){
        escribirLinea("");
        escribirLinea("process(clk,volcar)");
        escribirLinea("begin");
        escribirLinea("\tif clk'event and clk='1' then");
        escribirLinea("\t\tif volcar = '1' then");
        escribirLinea("\t\t\trecibido <= '1';");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\trecibido <= '0';");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("\tend process;");
        escribirLinea("");

    }

    private void procesoBiest(){
        escribirLinea("");
        escribirLinea("process(clk)");
        escribirLinea("begin");
        escribirLinea("\tif clk'event and clk='1' then");
        escribirLinea("\t\tbiest_recibido <= recibido;");
        escribirLinea("\t\tbiest_transmitido <= transmitido;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesoFin(){
        escribirLinea("");
        escribirLinea("process(fin)");
        escribirLinea("begin");
        escribirLinea("\tif fin'event and fin = '1' then");
        escribirLinea("\t\tif mi_transmite = '0' then");
        escribirLinea("\t\t\tmi_transmite <= '1';");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\tmi_transmite <= '0';");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesoEnable() {
        if (hayReloj()) {
            escribirLinea("");
            escribirLinea("process(enable)");
            escribirLinea("begin");
            escribirLinea("\tif (enable = '0') then");
            escribirLinea("\t\tmi_clk <= '0';");
            escribirLinea("\telse");
            escribirLinea("\t\tmi_clk <= recibido;");
            escribirLinea("\tend if;");
            escribirLinea("end process;");
            escribirLinea("");
        }
    }

    private void procesoLedsEnt(){
        escribirLinea("");
        escribirLinea("process(estadoEnt)");
        escribirLinea("begin");
        escribirLinea("\tif estadoEnt = 0 then");
        escribirLinea("\t\tledsEnt <= \"00\";");
        escribirLinea("\telsif estadoEnt = 1 then");
        escribirLinea("\t\tledsEnt <= \"01\";");
        escribirLinea("\telsif estadoEnt = 2 then");
        escribirLinea("\t\tledsEnt <= \"10\";");
        escribirLinea("elsif estadoEnt = 3 then");
        escribirLinea("\t\tledsEnt <= \"11\";");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesoLedsSal(){
        escribirLinea("");
        escribirLinea("process(estadoSal)");
        escribirLinea("begin");
        escribirLinea("\tif estadoSal = 0 then");
        escribirLinea("\t\tledsSal <= \"00\";");
        escribirLinea("\telsif estadoSal = 1 then");
        escribirLinea("\t\tledsSal <= \"01\";");
        escribirLinea("\telsif estadoSal = 2 then");
        escribirLinea("\t\tledsSal <= \"10\";");
        escribirLinea("elsif estadoSal = 3 then");
        escribirLinea("\t\tledsSal <= \"11\";");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void asigLeds(){
        escribirLinea("");
        escribirLinea("ledsEntrada <= ledsEnt;");
        escribirLinea("ledsSalida <= ledsSal;");
        escribirLinea("");
    }

    private void entSalPuertoSerie() {
        escribirLinea("");
        escribirLinea("mi_resetserie <= reset;");
        escribirLinea("salida_serie<=mi_datoserieout;");
        escribirLinea("mi_rxdatoserie <= entrada_serie;");
        escribirLinea("");
    }

    private void asigSenalesTransmision(){
        escribirLinea("");
        escribirLinea("frecibido <= not biest_recibido and recibido; --flanco que indica fin de recepcion");
        escribirLinea("ftransmitido <= not biest_transmitido and transmitido; --flanco que indica fin de transmision");
        escribirLinea("fin <= frecibido or ftransmitido; --flanco que indica fin de envio o transmision");
        escribirLinea("");
    }

    private void asigEntradasCircuitoPrinc() {
        escribirLinea("");
        int numEntrada = 0;
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            Entrada e = entidad.getEntrada(i);
            if (!e.getEsReloj() && !e.getEsReset()) {
                if (e.getNumBits() > 1) {
                    for (int j = 0; j < e.getNumBits(); j++) {
                        escribirLinea("mi_" + e.getNombre() + "(" + j + ") <= Reg_entradas(" + numEntrada++ + ");");
                    }

                } else {
                    escribirLinea("mi_" + e.getNombre() + " <= Reg_entradas(" + numEntrada++ + ");");
                }
            }
        }
        String reset = entidad.getNombreReset();
        if (reset != null){
            escribirLinea("MI_"+reset + " <= not reset;");
        }
        escribirLinea("enable <= '1';");
        //TODO escribirLinea("enable <= Reg_entradas(" + numEntrada + ");");
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
        SenalesEstados();
        SenalesIO();
        begin();
        asigSenalesCompsSerie();
        asigSenalesCompPrinc();
        procesoEntradas();
        procesoSalidas();
        procesoVolcar();
        procesoBiest();
        procesoFin();
        procesoEnable();
        procesoLedsEnt();
        procesoLedsSal();
        asigLeds();
        entSalPuertoSerie();
        asigSenalesTransmision();
        asigEntradasCircuitoPrinc();
        asigSalidasCircuitoPrinc();
        end();
    }
}
