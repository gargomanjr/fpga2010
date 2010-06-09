/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package generadorVHDL;

import compiladorEntidad.*;
import java.io.*;
import java.util.Date;

/**
 * Fichero encargado de generar automáticamente el VHDL de circuito top
 * (Circuito_FPGA.vhd) que será el que conecte el circuito que queramos ejecutar
 * en la FPGA con nuestro módulo de entrada/salida. Además también contendrá
 * los procesos necesarios para leer y escribir 32 bits desde/hacia la FPGA.
 * esta clase está estructurada en diferentes métodos cada uno de los cuales
 * escriben un bloque concreto del fichero. 
 *
 * @author Carlos, Tony y David.
 */
public class GeneraVhdl {

    /**
     * Entidad que se quiere conectar al módulo de entrada/salida
     */
    private Entidad entidad;

    /**
     * Nombre de la entidad superior. Va a ser siempre Circuito_FPGA
     */
    private String nomEntidadGeneral;

    /**
     * Escritor de fichero
     */
    private BufferedWriter bw;

    /**
     * Ruta del fichero en la que se va a escribir
     */
    private String fichero;

    /**
     * Errores surgidos durante la generación del fichero
     */
    private Errores errores;

    /**
     * Constructor de la clase
     * @param fichero Sobre el que vamos a generar el nuevo vhdl.
     * @param entidad Entidad con la que estamos trabajando y a partir de la cual se genera el vhdl.
     * @param errores Objeto de la clase Errores para ir almacenando los errores encontrados.
     * @throws java.io.IOException
     */
    public GeneraVhdl(String fichero, Entidad entidad, Errores errores) throws IOException {
        this.fichero = fichero;
        this.entidad = entidad;
        this.errores = errores;
        nomEntidadGeneral = "Circuito_FPGA"; //de momento lo llamamos asi
    }

    /**
     * Abre el fichero para escribir el nuevo archivo vhdl.
     * @return Cierto si no ha habido ningún error, falso en caso contrario.
     */
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

    /**
     * Cierra el fichero VHDL que estamos construyendo
     * @return Cierto si no ha habido ningún error, falso en caso contrario.
     */
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

    /**
     * Escribe una cadena en el fichero junto con un salto de linea
     * @param linea Cadena a escribir
     */
    private void escribirLinea(String linea) {
        try {
            bw.write(linea);
            bw.newLine();
        } catch (IOException ex) {
            errores.error("Error al escribir en fichero");
        }
    }

    /**
     * Método para saber si hay reloj
     * @return
     */
    private boolean hayReloj(){
        return this.entidad.getPosicionClk() >= 0;
    }

    /**
     * Hace la distinción entre is es un puerto con varios bits o solamente uno.
     * En cada caso escribirá STD_LOGIC o STD_LOGIC_VECTOR.
     * @param p Puerto del que se desea saber su tipo
     * @return String con el tipo de puerto
     */
    private String tipoPuerto(Puerto p) {
        int numBits = p.getNumBits();
        if (numBits > 1) {
            return "STD_LOGIC_VECTOR(" + (numBits - 1) + " downto " + "0)";
        }
        return "STD_LOGIC";
    }

    /**
     * Devuelve la lista de señales la entidad concatenadas con el prefijo
     * "mi_" y separadas por comas. Se utilizará para hacer la asignación
     * de puertos al declarar el componente.
     * @return Cadena con lista de señales de la entidad separadas por comas
     */
    private String listaSenales() {
        String s = "(";
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            Entrada e = entidad.getEntrada(i);
            s += "mi_" + e.getNombre() + ",";
        }
        for (int i = 0; i < entidad.getNumSalidas() - 1; i++) {
            Salida sal = entidad.getSalida(i);
            s += "mi_" + sal.getNombre() + ",";
        }//la entidad tiene que tener al menos alguna salida
        s += "mi_" + entidad.getSalida(entidad.getNumSalidas() - 1).getNombre() + ")";
        return s;
    }

    /**
     * Escribe comentarios en la cabecera del fichero
     */
    private void comentariosCabecera(){
        escribirLinea("--------------------------------------------------------------------------------");
        escribirLinea("--Descripción:");
        escribirLinea("--\tEste fichero ha sido generado automáticamente por la aplicación Nessy2.0");
        escribirLinea("--\tSe trata del fichero que describe la entidad top generada para cualquier circuto que quiera ser ejecutado en la FPGA");
        escribirLinea("--");
        escribirLinea("--");
        escribirLinea("--Especificaciones:");
        escribirLinea("--\tCircuito a ejecutar:");
        escribirLinea("--\t\tNum. Entradas: "+ entidad.getBitsEntrada());
        escribirLinea("--\t\tNum. Salidas: " + entidad.getBitsSalida());
        escribirLinea("--Autor:");
        escribirLinea("--\tCarlos Sanchez-Vellisco Sanchez");
        escribirLinea("--\tFacultad de Informatica. Universidad Complutense de Madrid");
        Date date = new Date();
        escribirLinea("--Fecha: ");
        escribirLinea("--\t"+date.toString());
        escribirLinea("--------------------------------------------------------------------------------");
        escribirLinea("");
    }

    /**
     * Escribe la inclusión de librerías en el fichero
     */
    private void librerias() {
        escribirLinea("library IEEE;");
        escribirLinea("use IEEE.STD_LOGIC_1164.ALL;");
        escribirLinea("use IEEE.STD_LOGIC_ARITH.ALL;");
        escribirLinea("use IEEE.STD_LOGIC_UNSIGNED.ALL;");
        escribirLinea("");
        escribirLinea("");
    }

    /**
     * Descripción de la entidad
     */
    private void entidadGeneral() {
        escribirLinea("");
        escribirLinea("--Descripcion de la entidad");
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

    /**
     * Inicio de la arquitectura
     */
    private void inicioArquitectura() {
        escribirLinea("");
        escribirLinea("architecture Behavioral of " + nomEntidadGeneral + " is");
        escribirLinea("");
    }

    /**
     * Declara el componente del transmisor
     */
    private void compSalidaSerie() {
        escribirLinea("");
        escribirLinea("--Transmisor serie");
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

    /**
     * Declara el componente del receptor
     */
    private void compEntradaSerie() {
        escribirLinea("");
        escribirLinea("--Receptor Serie");
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

    /**
     * Declara el componente del circuito principal
     */
    private void componentePrincipal() {
        escribirLinea("");
        escribirLinea("--Entidad que se quiere ejecutar");
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

    /**
     * Declara las señales del circuiti principal. Concatena el nombre original
     * de la entrada o salida con el prefijo "mi_" de tal forma que tengamos
     * señales intermedias
     */
    private void SenalesCircuitoPrincipal() {
        escribirLinea("");
        escribirLinea("--Señales del circuito principal");
        escribirLinea("");
        escribirLinea("--Entradas");
        for (int i = 0; i < entidad.getNumEntradas(); i++) {
            Entrada e = entidad.getEntrada(i);
            escribirLinea("signal mi_" + e.getNombre() + ": " + tipoPuerto(e) + ";");
        }
        escribirLinea("");
        escribirLinea("--Salidas");
        for (int i = 0; i < entidad.getNumSalidas(); i++) {
            Salida s = entidad.getSalida(i);
            escribirLinea("signal mi_" + s.getNombre() + ": " + tipoPuerto(s) + ";");
        }
        escribirLinea("");
    }

    /**
     * Declara señales intermedias para la entrada serie
     */
    private void SenalesEntradaSerie() {
        escribirLinea("");
        escribirLinea("--Señales para la Recepción Serie");
        escribirLinea("signal mi_resetserie:std_logic;");
        escribirLinea("signal mi_transmite:std_logic;");
        escribirLinea("signal mi_datotxin:std_logic_vector(7 downto 0);");
        escribirLinea("signal mi_datorxout:std_logic_vector(7 downto 0);");
        escribirLinea("signal mi_avisoRx:std_logic;");
        escribirLinea("signal mi_recibiendo:std_logic;");
        escribirLinea("");
    }

    /**
     * Declara señales intermedias para la salida serie
     */
    private void SenalesSalidaSerie() {
        escribirLinea("");
        escribirLinea("--Señales para la Transmisión Serie");
        escribirLinea("signal mi_transmitiendo:std_logic;");
        escribirLinea("signal mi_datoserieout:std_logic;");
        escribirLinea("signal mi_rxdatoserie:std_logic;");
        escribirLinea("");
    }

    /**
     * Declara las señales intermedias para conectar las entradas y las salidas
     * Éstas se conectarán a su vez a los módulos de entrada/salida y al 
     * circuito principal
     */
    private void regsEntradaSalida() {
        escribirLinea("");
        escribirLinea("--Señales intermedias para la entrada y la salida. Se conectarán a las entradas y las salidas del circuito principal");
        escribirLinea("signal Reg_entradas: std_logic_vector(31 downto 0);");
        escribirLinea("signal Reg_salidas: std_logic_vector(31 downto 0);");
        escribirLinea("");
    }

    /**
     * Señales para visualizar los estados
     */
    private void SenalesEstados(){
        escribirLinea("");
        escribirLinea("--Señales para los estados del emisor/receptor de 32 bits");
        escribirLinea("signal estadoEnt: integer;");
        escribirLinea("signal estadoSal: integer;");
        escribirLinea("");
        escribirLinea("signal ledsEnt:std_logic_vector(1 downto 0);");
        escribirLinea("signal ledsSal:std_logic_vector(1 downto 0);");
        escribirLinea("");
    }

    /**
     * Señales necesarias para la correcta entrada/salida con 32 bits
     */
    private void SenalesIO(){
        escribirLinea("");
        escribirLinea("--Señales necesarias para la correcta entrada/salida con 32 bits");
        escribirLinea("signal fin_recepcion : std_logic;");
        escribirLinea("signal recibido,transmitido,biest_recibido, biest_transmitido: std_logic;");
        escribirLinea("signal frecibido,ftransmitido,fin: std_logic;  --flancos de fin");
        escribirLinea("");

    }

    /**
     * Comienzo de la descripcion
     */
    private void begin() {
        escribirLinea("");
        escribirLinea("begin");
        escribirLinea("");
    }

    /**
     * Se asignan los puertos de los componentes de entrada/salida
     */
    private void asigSenalesCompsSerie() {
        escribirLinea("");
        escribirLinea("--Asignación de señales a los componentes de la entrada/salida");
        escribirLinea("f: Tx_serie port map(mi_resetserie,clk,mi_transmite,mi_datotxin,mi_transmitiendo,mi_datoserieout);");
        escribirLinea("R: Rx_serie port map(mi_resetserie,clk,mi_rxdatoserie,mi_datorxout,mi_avisorx,mi_recibiendo);");
        escribirLinea("");
    }

    /**
     * Se asignan las señales al componente del cricuito principal. Utilizaremos
     * la lista de señales cobtenida más
     */
    private void asigSenalesCompPrinc() {
        escribirLinea("");
        escribirLinea("--Asignación de señales al componente del circuito principal");
        escribirLinea("U: " + entidad.getNombre() + " port map" + listaSenales() + ";");
        escribirLinea("");
    }

    /**
     * Proceso encargado de la correcta recepción de datos con 32 bits
     */
    private void procesoEntradas() {
        escribirLinea("");
        escribirLinea("--Proceso encargado de la correcta recepción de datos (32 bits)");
        escribirLinea("--Cada vez que se reciba un byte, se irá asignando a las entradas desde las menos significativas a las más significatias");
        escribirLinea("process(mi_recibiendo,mi_resetserie)");
        escribirLinea("begin");
        escribirLinea("\tif mi_resetserie = '0' then");
        escribirLinea("\t\testadoEnt <= 0;");
        escribirLinea("\t\tfin_recepcion <= '0';");
        escribirLinea("\telsif mi_recibiendo'event and mi_recibiendo = '0' then");
        escribirLinea("\t\tfin_recepcion <= '0';");
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
        escribirLinea("\t\t\tfin_recepcion <= '1'; --fin de la recepción");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\tReg_entradas(7 downto 0) <= mi_datorxout;");
        escribirLinea("\t\t\testadoEnt <= 1;");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");

    }

    /**
     * Proceso encargado de que la salida correspondiente del circuito esté conectada
     * a la salida serie antes de que la transmisión se produzca
     */
    private void procesoSalidas(){
        escribirLinea("");
        escribirLinea("--Proceso encargado de que la salida correspondiente del circuito esté conectada a la salida serie antes de que la transmisión se produzca");
        escribirLinea("process(estadoSal, clk, mi_resetserie)");
        escribirLinea("begin");
        escribirLinea("\tif mi_resetserie = '0' then");
        escribirLinea("\t\tmi_datotxin <= Reg_salidas(7 downto 0);");
        escribirLinea("\telsif clk'event and clk = '1' then");
        escribirLinea("\t\tif estadoSal = 0 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(7 downto 0);");
        escribirLinea("\t\telsif estadoSal = 1 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(15 downto 8);");
        escribirLinea("\t\telsif estadoSal = 2 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(23 downto 16);");
        escribirLinea("\t\telsif estadoSal = 3 then");
        escribirLinea("\t\t\tmi_datotxin <= Reg_salidas(31 downto 24);");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesoEstadoSalidas(){
        escribirLinea("");
        escribirLinea("--Proceso encargado de cambiar de estado cada vez que se comienza a transmitir un byte");
        escribirLinea("process(mi_transmitiendo, mi_resetserie)");
        escribirLinea("begin");
        escribirLinea("\tif mi_resetserie = '0' then");
        escribirLinea("\t\testadoSal<= 0;");
        escribirLinea("\t\ttransmitido <= '0';");
        escribirLinea("\telsif mi_transmitiendo'event and mi_transmitiendo = '1' then");
        escribirLinea("\t\tif estadoSal = 3 then");
        escribirLinea("\t\t\ttransmitido <= '1';");
        escribirLinea("\t\t\testadoSal <= 0;");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\testadoSal <= estadoSal+1;");
        escribirLinea("\t\t\ttransmitido <= '0';");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("end process;");
        escribirLinea("");
    }

    private void procesofin_recepcion(){
        escribirLinea("");
        escribirLinea("--Proceso encargado de registrar que la recepción ha terminado. La salida del biestable ('recibido') se usará como reloj del circuito principal");
        escribirLinea("process(clk,fin_recepcion)");
        escribirLinea("begin");
        escribirLinea("\tif clk'event and clk='1' then");
        escribirLinea("\t\tif fin_recepcion = '1' then");
        escribirLinea("\t\t\trecibido <= '1'; --flanco positivo que hará funcionar el circuito principal");
        escribirLinea("\t\telse");
        escribirLinea("\t\t\trecibido <= '0';");
        escribirLinea("\t\tend if;");
        escribirLinea("\tend if;");
        escribirLinea("\tend process;");
        escribirLinea("");

    }

    private void procesoBiest(){
        escribirLinea("");
        escribirLinea("--Proceso encargado de registrar el fin de la recepción y de la transmisión");
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
        escribirLinea("--Proceso que indica que, o bien ha terminado una recepción de datos, o bien ha terminado una transmisión");
        escribirLinea("--Cuando se detecte un flanco positivo, se negará la señal que hace que se transmita, de tal forma que si se estaba transitiendo,");
        escribirLinea("--se deje de transmitir y si no se estaba transmitiendo se comience una nueva transmisión");
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

    

    private void procesoLedsEnt(){
        escribirLinea("");
        escribirLinea("--Este proceso es prescindible. Sólo a efectos de visualizar estado de entrada");
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
        escribirLinea("--Este proceso es prescindible. Sólo a efectos de visualizar estado de salida");
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
        if (this.hayReloj()){
            escribirLinea("--El reloj del circuito principal será el flanco que indique el fin de la recepción");
            escribirLinea("mi_"+entidad.getEntrada(entidad.getPosicionClk())+ " <= recibido;");
        }
        escribirLinea("");
        escribirLinea("--Asignación de las señales del circuito general");
        escribirLinea("mi_resetserie <= reset;");
        escribirLinea("salida_serie <= mi_datoserieout;");
        escribirLinea("mi_rxdatoserie <= entrada_serie;");
        escribirLinea("");
    }

    private void asigSenalesTransmision(){
        escribirLinea("");
        escribirLinea("--Asignación de las señales necesarias para la transmisión correcta");
        escribirLinea("frecibido <= not biest_recibido and recibido; --flanco que indica fin de recepcion");
        escribirLinea("ftransmitido <= not biest_transmitido and transmitido; --flanco que indica fin de transmision");
        escribirLinea("fin <= frecibido or ftransmitido; --flanco que indica fin de envio o transmision");
        escribirLinea("");
    }

    private void asigEntradasCircuitoPrinc() {
        escribirLinea("");
        escribirLinea("--Asignación de las señales intermedias de entrada/salida a las del circuito principal");
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

    /**
     * Crea el nuevo fichero VHDL a partir de la entidad que tiene la clase.
     */
    public void crearFichero() {
        comentariosCabecera();
        librerias();
        entidadGeneral();
        inicioArquitectura();
        compSalidaSerie();
        compEntradaSerie();
        componentePrincipal();
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
        procesoEstadoSalidas();
        procesofin_recepcion();
        procesoBiest();
        procesoFin();
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
