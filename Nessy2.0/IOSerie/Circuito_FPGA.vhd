--------------------------------------------------------------------------------
--Descripción:
--	Este fichero ha sido generado automáticamente por la aplicación Nessy2.0
--	Se trata del fichero que describe la entidad top generada para cualquier circuto que quiera ser ejecutado en la FPGA
--
--
--Especificaciones:
--	Circuito a ejecutar:
--		Num. Entradas: 19
--		Num. Salidas: 16
--Autor:
--	Carlos Sanchez-Vellisco Sanchez
--	Facultad de Informatica. Universidad Complutense de Madrid
--Fecha: 
--	Fri Jun 11 12:27:02 CEST 2010
--------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;



--Descripcion de la entidad
entity Circuito_FPGA is
Port ( clk : in  STD_LOGIC;
reset : in  STD_LOGIC;
salida_serie : out  STD_LOGIC;
entrada_serie : in  STD_LOGIC;
ledsEntrada : out std_logic_vector(1 downto 0);
ledsSalida : out std_logic_vector(1 downto 0));
end Circuito_FPGA;


architecture Behavioral of Circuito_FPGA is


--Transmisor serie
component Tx_serie
	Port ( RstN : in  STD_LOGIC;
	clk : in  STD_LOGIC;
	Transmite : in  STD_LOGIC;
	DatoTxIn : in  STD_LOGIC_VECTOR (7 downto 0);
	Transmitiendo : out  STD_LOGIC;
	DatoSerieOut : out  STD_LOGIC);
end component;


--Receptor Serie
component Rx_Serie
	Port ( Rstn : in  STD_LOGIC;
	Clk : in  STD_LOGIC;
	RxDatoSerie : in  STD_LOGIC;
	DatoRxOut : out  STD_LOGIC_VECTOR (7 downto 0);
	AvisoRx : out  STD_LOGIC;
	Recibiendo : out  STD_LOGIC);
end component ;


--Entidad que se quiere ejecutar
component CONTADOR
	Port(
		RESET: in STD_LOGIC;
		CLK: in STD_LOGIC;
		ENABLE: in STD_LOGIC;
		LOAD: in STD_LOGIC;
		DATA_LOAD: in STD_LOGIC_VECTOR(15 downto 0);

		SALIDA: out STD_LOGIC_VECTOR(15 downto 0)
);
end component;


--Señales del circuito principal

--Entradas
signal mi_RESET: STD_LOGIC;
signal mi_CLK: STD_LOGIC;
signal mi_ENABLE: STD_LOGIC;
signal mi_LOAD: STD_LOGIC;
signal mi_DATA_LOAD: STD_LOGIC_VECTOR(15 downto 0);

--Salidas
signal mi_SALIDA: STD_LOGIC_VECTOR(15 downto 0);


--Señales para la Recepción Serie
signal mi_resetserie:std_logic;
signal mi_transmite:std_logic;
signal mi_datotxin:std_logic_vector(7 downto 0);
signal mi_datorxout:std_logic_vector(7 downto 0);
signal mi_avisoRx:std_logic;
signal mi_recibiendo:std_logic;


--Señales para la Transmisión Serie
signal mi_transmitiendo:std_logic;
signal mi_datoserieout:std_logic;
signal mi_rxdatoserie:std_logic;


--Señales intermedias para la entrada y la salida. Se conectarán a las entradas y las salidas del circuito principal
signal Reg_entradas: std_logic_vector(31 downto 0);
signal Reg_salidas: std_logic_vector(31 downto 0);


--Señales para los estados del emisor/receptor de 32 bits
signal estadoEnt: integer;
signal estadoSal: integer;

signal ledsEnt:std_logic_vector(1 downto 0);
signal ledsSal:std_logic_vector(1 downto 0);


--Señales necesarias para la correcta entrada/salida con 32 bits
signal fin_recepcion : std_logic;
signal recibido,transmitido,biest_recibido, biest_transmitido: std_logic;
signal frecibido,ftransmitido,fin: std_logic;  --flancos de fin


begin


--Asignación de señales a los componentes de la entrada/salida
f: Tx_serie port map(mi_resetserie,clk,mi_transmite,mi_datotxin,mi_transmitiendo,mi_datoserieout);
R: Rx_serie port map(mi_resetserie,clk,mi_rxdatoserie,mi_datorxout,mi_avisorx,mi_recibiendo);


--Asignación de señales al componente del circuito principal
U: CONTADOR port map(mi_RESET,mi_CLK,mi_ENABLE,mi_LOAD,mi_DATA_LOAD,mi_SALIDA);


--Proceso encargado de la correcta recepción de datos (32 bits)
--Cada vez que se reciba un byte, se irá asignando a las entradas desde las menos significativas a las más significatias
process(mi_recibiendo,mi_resetserie)
begin
	if mi_resetserie = '0' then
		estadoEnt <= 0;
		fin_recepcion <= '0';
	elsif mi_recibiendo'event and mi_recibiendo = '0' then
		fin_recepcion <= '0';
		if estadoEnt = 0 then
			Reg_entradas(7 downto 0) <= mi_datorxout;
			estadoEnt <= 1;
		elsif estadoEnt = 1 then
			Reg_entradas(15 downto 8) <= mi_datorxout;
			estadoEnt <= 2;
		elsif estadoEnt = 2 then
			Reg_entradas(23 downto 16) <= mi_datorxout;
			estadoEnt <= 3;
		elsif estadoEnt = 3 then
			Reg_entradas(31 downto 24) <= mi_datorxout;
			estadoEnt <= 0;
			fin_recepcion <= '1'; --fin de la recepción
		else
			Reg_entradas(7 downto 0) <= mi_datorxout;
			estadoEnt <= 1;
		end if;
	end if;
end process;


--Proceso encargado de que la salida correspondiente del circuito esté conectada a la salida serie antes de que la transmisión se produzca
process(estadoSal, clk, mi_resetserie)
begin
	if mi_resetserie = '0' then
		mi_datotxin <= Reg_salidas(7 downto 0);
	elsif clk'event and clk = '1' then
		if estadoSal = 0 then
			mi_datotxin <= Reg_salidas(7 downto 0);
		elsif estadoSal = 1 then
			mi_datotxin <= Reg_salidas(15 downto 8);
		elsif estadoSal = 2 then
			mi_datotxin <= Reg_salidas(23 downto 16);
		elsif estadoSal = 3 then
			mi_datotxin <= Reg_salidas(31 downto 24);
		end if;
	end if;
end process;


--Proceso encargado de cambiar de estado cada vez que se comienza a transmitir un byte
process(mi_transmitiendo, mi_resetserie)
begin
	if mi_resetserie = '0' then
		estadoSal<= 0;
		transmitido <= '0';
	elsif mi_transmitiendo'event and mi_transmitiendo = '1' then
		if estadoSal = 3 then
			transmitido <= '1';
			estadoSal <= 0;
		else
			estadoSal <= estadoSal+1;
			transmitido <= '0';
		end if;
	end if;
end process;


--Proceso encargado de registrar que la recepción ha terminado. La salida del biestable ('recibido') se usará como reloj del circuito principal
process(clk,fin_recepcion)
begin
	if clk'event and clk='1' then
		if fin_recepcion = '1' then
			recibido <= '1'; --flanco positivo que hará funcionar el circuito principal
		else
			recibido <= '0';
		end if;
	end if;
	end process;


--Proceso encargado de registrar el fin de la recepción y de la transmisión
process(clk)
begin
	if clk'event and clk='1' then
		biest_recibido <= recibido;
		biest_transmitido <= transmitido;
	end if;
end process;


--Proceso que indica que, o bien ha terminado una recepción de datos, o bien ha terminado una transmisión
--Cuando se detecte un flanco positivo, se negará la señal que hace que se transmita, de tal forma que si se estaba transitiendo,
--se deje de transmitir y si no se estaba transmitiendo se comience una nueva transmisión
process(fin)
begin
	if fin'event and fin = '1' then
		if mi_transmite = '0' then
			mi_transmite <= '1';
		else
			mi_transmite <= '0';
		end if;
	end if;
end process;


--Este proceso es prescindible. Sólo a efectos de visualizar estado de entrada
process(estadoEnt)
begin
	if estadoEnt = 0 then
		ledsEnt <= "00";
	elsif estadoEnt = 1 then
		ledsEnt <= "01";
	elsif estadoEnt = 2 then
		ledsEnt <= "10";
elsif estadoEnt = 3 then
		ledsEnt <= "11";
	end if;
end process;


--Este proceso es prescindible. Sólo a efectos de visualizar estado de salida
process(estadoSal)
begin
	if estadoSal = 0 then
		ledsSal <= "00";
	elsif estadoSal = 1 then
		ledsSal <= "01";
	elsif estadoSal = 2 then
		ledsSal <= "10";
elsif estadoSal = 3 then
		ledsSal <= "11";
	end if;
end process;


ledsEntrada <= ledsEnt;
ledsSalida <= ledsSal;


--El reloj del circuito principal será el flanco que indique el fin de la recepción
mi_CLK <= recibido;

--Asignación de las señales del circuito general
mi_resetserie <= reset;
salida_serie <= mi_datoserieout;
mi_rxdatoserie <= entrada_serie;


--Asignación de las señales necesarias para la transmisión correcta
frecibido <= not biest_recibido and recibido; --flanco que indica fin de recepcion
ftransmitido <= not biest_transmitido and transmitido; --flanco que indica fin de transmision
fin <= frecibido or ftransmitido; --flanco que indica fin de envio o transmision


--Asignación de las señales intermedias de entrada/salida a las del circuito principal
mi_RESET <= Reg_entradas(0);
mi_ENABLE <= Reg_entradas(1);
mi_LOAD <= Reg_entradas(2);
mi_DATA_LOAD(0) <= Reg_entradas(3);
mi_DATA_LOAD(1) <= Reg_entradas(4);
mi_DATA_LOAD(2) <= Reg_entradas(5);
mi_DATA_LOAD(3) <= Reg_entradas(6);
mi_DATA_LOAD(4) <= Reg_entradas(7);
mi_DATA_LOAD(5) <= Reg_entradas(8);
mi_DATA_LOAD(6) <= Reg_entradas(9);
mi_DATA_LOAD(7) <= Reg_entradas(10);
mi_DATA_LOAD(8) <= Reg_entradas(11);
mi_DATA_LOAD(9) <= Reg_entradas(12);
mi_DATA_LOAD(10) <= Reg_entradas(13);
mi_DATA_LOAD(11) <= Reg_entradas(14);
mi_DATA_LOAD(12) <= Reg_entradas(15);
mi_DATA_LOAD(13) <= Reg_entradas(16);
mi_DATA_LOAD(14) <= Reg_entradas(17);
mi_DATA_LOAD(15) <= Reg_entradas(18);


Reg_salidas(0) <= mi_SALIDA(0);
Reg_salidas(1) <= mi_SALIDA(1);
Reg_salidas(2) <= mi_SALIDA(2);
Reg_salidas(3) <= mi_SALIDA(3);
Reg_salidas(4) <= mi_SALIDA(4);
Reg_salidas(5) <= mi_SALIDA(5);
Reg_salidas(6) <= mi_SALIDA(6);
Reg_salidas(7) <= mi_SALIDA(7);
Reg_salidas(8) <= mi_SALIDA(8);
Reg_salidas(9) <= mi_SALIDA(9);
Reg_salidas(10) <= mi_SALIDA(10);
Reg_salidas(11) <= mi_SALIDA(11);
Reg_salidas(12) <= mi_SALIDA(12);
Reg_salidas(13) <= mi_SALIDA(13);
Reg_salidas(14) <= mi_SALIDA(14);
Reg_salidas(15) <= mi_SALIDA(15);


end Behavioral;

