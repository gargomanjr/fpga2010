library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;



entity Circuito_FPGA is
Port ( clk : in  STD_LOGIC;
reset : in  STD_LOGIC;
salida_serie : out  STD_LOGIC;
entrada_serie : in  STD_LOGIC);
end Circuito_FPGA;


architecture Behavioral of Circuito_FPGA is


component Tx_serie
	Port ( RstN : in  STD_LOGIC;
	clk : in  STD_LOGIC;
	Transmite : in  STD_LOGIC;
	DatoTxIn : in  STD_LOGIC_VECTOR (7 downto 0);
	Transmitiendo : out  STD_LOGIC;
	DatoSerieOut : out  STD_LOGIC);
end component;


component Rx_Serie
	Port ( Rstn : in  STD_LOGIC;
	Clk : in  STD_LOGIC;
	RxDatoSerie : in  STD_LOGIC;
	DatoRxOut : out  STD_LOGIC_VECTOR (7 downto 0);
	AvisoRx : out  STD_LOGIC;
	Recibiendo : out  STD_LOGIC);
end component ;


component CONTADOR
	Port(
		RESET: in STD_LOGIC;
		CLK: in STD_LOGIC;
		ENABLE: in STD_LOGIC;
		LOAD: in STD_LOGIC;
		DATA_LOAD: in STD_LOGIC_VECTOR(3 downto 0);

		CAMBIANDO: out STD_LOGIC;
		SALIDA: out STD_LOGIC_VECTOR(3 downto 0)
);
end component;


signal enable: std_logic;  --señal enable general


signal mi_RESET: STD_LOGIC;
signal mi_CLK: STD_LOGIC;
signal mi_ENABLE: STD_LOGIC;
signal mi_LOAD: STD_LOGIC;
signal mi_DATA_LOAD: STD_LOGIC_VECTOR(3 downto 0);

signal mi_CAMBIANDO: STD_LOGIC;
signal mi_SALIDA: STD_LOGIC_VECTOR(3 downto 0);


signal mi_resetserie:std_logic;
signal mi_transmite:std_logic;
signal mi_datotxin:std_logic_vector(7 downto 0);
signal mi_datorxout:std_logic_vector(7 downto 0);
signal mi_avisoRx:std_logic;
signal mi_recibiendo:std_logic;


signal mi_transmitiendo:std_logic;
signal mi_datoserieout:std_logic;
signal mi_rxdatoserie:std_logic;


signal Reg_entradas: std_logic_vector(7 downto 0);
signal Reg_salidas: std_logic_vector(7 downto 0);


begin


f: Tx_serie port map(mi_resetserie,clk,not mi_recibiendo,mi_datotxin,mi_transmitiendo,mi_datoserieout);
R: Rx_serie port map(mi_resetserie,clk,mi_rxdatoserie,mi_datorxout,mi_avisorx,mi_recibiendo);


U: CONTADOR port map(mi_RESET,mi_CLK,mi_ENABLE,mi_LOAD,mi_DATA_LOAD,mi_CAMBIANDO,mi_SALIDA);


mi_resetserie <= reset;
salida_serie<=mi_datoserieout;
mi_rxdatoserie <= entrada_serie;


process(mi_recibiendo)
begin
	if mi_recibiendo = '0' then
		Reg_entradas <= mi_datorxout;
	end if;
end process;


process(mi_transmitiendo)
begin
	if mi_transmitiendo = '0' then
		mi_datotxin <= Reg_salidas;
	end if;
end process;


process(ENABLE)
begin
	if (enable = '0') then
		mi_clk <= '0';
	else
		mi_clk <= clk;
	end if;
end process;


mi_RESET <= Reg_entradas(0);
mi_ENABLE <= Reg_entradas(1);
mi_LOAD <= Reg_entradas(2);
mi_DATA_LOAD(0) <= Reg_entradas(3);
mi_DATA_LOAD(1) <= Reg_entradas(4);
mi_DATA_LOAD(2) <= Reg_entradas(5);
mi_DATA_LOAD(3) <= Reg_entradas(6);
enable <= Reg_entradas(7);


Reg_salidas(0) <= mi_CAMBIANDO;
Reg_salidas(1) <= mi_SALIDA(0);
Reg_salidas(2) <= mi_SALIDA(1);
Reg_salidas(3) <= mi_SALIDA(2);
Reg_salidas(4) <= mi_SALIDA(3);


end Behavioral;

