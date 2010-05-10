library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;



entity Circuito_FPGA is
Port ( clk : in  STD_LOGIC;
reset : in  STD_LOGIC;
salida_serie : out  STD_LOGIC;
entrada_serie : in  STD_LOGIC;
ledsEntrada : out std_logic_vector(1 downto 0);
ledsSalida : out std_logic_vector(1 downto 0));
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


component CIRCUITO
	Port(
		ENTRADA: in STD_LOGIC_VECTOR(31 downto 0);
		CLK: in STD_LOGIC;

		SALIDA: out STD_LOGIC_VECTOR(31 downto 0)
);
end component;


signal enable: std_logic;  --enable general


signal mi_ENTRADA: STD_LOGIC_VECTOR(31 downto 0);
signal mi_CLK: STD_LOGIC;

signal mi_SALIDA: STD_LOGIC_VECTOR(31 downto 0);


signal mi_resetserie:std_logic;
signal mi_transmite:std_logic;
signal mi_datotxin:std_logic_vector(7 downto 0);
signal mi_datorxout:std_logic_vector(7 downto 0);
signal mi_avisoRx:std_logic;
signal mi_recibiendo:std_logic;


signal mi_transmitiendo:std_logic;
signal mi_datoserieout:std_logic;
signal mi_rxdatoserie:std_logic;


signal Reg_entradas: std_logic_vector(31 downto 0);
signal Reg_salidas: std_logic_vector(31 downto 0);


signal estadoEnt: integer;
signal estadoSal: integer;

signal ledsEnt:std_logic_vector(1 downto 0);
signal ledsSal:std_logic_vector(1 downto 0);


signal volcar : std_logic;
signal recibido,transmitido,biest_recibido, biest_transmitido: std_logic;
signal frecibido,ftransmitido,fin: std_logic;  --flancos de fin


begin


f: Tx_serie port map(mi_resetserie,clk,mi_transmite,mi_datotxin,mi_transmitiendo,mi_datoserieout);
R: Rx_serie port map(mi_resetserie,clk,mi_rxdatoserie,mi_datorxout,mi_avisorx,mi_recibiendo);


U: CIRCUITO port map(mi_ENTRADA,mi_CLK,mi_SALIDA);


process(mi_recibiendo,mi_resetserie)
begin
	if mi_resetserie = '0' then
		estadoEnt <= 0;
		volcar <= '0';
	elsif mi_recibiendo'event and mi_recibiendo = '0' then
		volcar <= '0';
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
			volcar <= '1';
		else
			Reg_entradas(7 downto 0) <= mi_datorxout;
			estadoEnt <= 1;
		end if;
	end if;
end process;


process(mi_transmitiendo,mi_resetserie)
begin
	if mi_resetserie = '0' then
		estadoSal<= 0;
		transmitido <= '0';
	elsif mi_transmitiendo'event and mi_transmitiendo = '0' then
		transmitido <= '0';
		if estadoSal = 0 then
			mi_datotxin <= Reg_salidas(7 downto 0);
			estadoSal <= 1;
		elsif estadoSal = 1 then
			mi_datotxin <= Reg_salidas(15 downto 8);
			estadoSal <= 2;
		elsif estadoSal = 2 then
			mi_datotxin <= Reg_salidas(23 downto 16);
			estadoSal <= 3;
		elsif estadoSal = 3 then
			mi_datotxin <= Reg_salidas(31 downto 24);
			estadoSal <= 4;
		elsif estadoSal = 4 then
			mi_datotxin <= Reg_salidas(7 downto 0);
			estadoSal <= 0;
			transmitido <= '1';
		else
			mi_datotxin <= Reg_salidas(7 downto 0);
			estadoSal <= 1;
		end if;
	end if;
end process;


process(clk,volcar)
begin
	if clk'event and clk='1' then
		if volcar = '1' then
			recibido <= '1';
		else
			recibido <= '0';
		end if;
	end if;
	end process;


process(clk)
begin
	if clk'event and clk='1' then
		biest_recibido <= recibido;
		biest_transmitido <= transmitido;
	end if;
end process;


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


process(enable)
begin
	if (enable = '0') then
		mi_clk <= '0';
	else
		mi_clk <= recibido;
	end if;
end process;


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


mi_resetserie <= reset;
salida_serie<=mi_datoserieout;
mi_rxdatoserie <= entrada_serie;


frecibido <= not biest_recibido and recibido; --flanco que indica fin de recepcion
ftransmitido <= not biest_transmitido and transmitido; --flanco que indica fin de transmision
fin <= frecibido or ftransmitido; --flanco que indica fin de envio o transmision


mi_ENTRADA(0) <= Reg_entradas(0);
mi_ENTRADA(1) <= Reg_entradas(1);
mi_ENTRADA(2) <= Reg_entradas(2);
mi_ENTRADA(3) <= Reg_entradas(3);
mi_ENTRADA(4) <= Reg_entradas(4);
mi_ENTRADA(5) <= Reg_entradas(5);
mi_ENTRADA(6) <= Reg_entradas(6);
mi_ENTRADA(7) <= Reg_entradas(7);
mi_ENTRADA(8) <= Reg_entradas(8);
mi_ENTRADA(9) <= Reg_entradas(9);
mi_ENTRADA(10) <= Reg_entradas(10);
mi_ENTRADA(11) <= Reg_entradas(11);
mi_ENTRADA(12) <= Reg_entradas(12);
mi_ENTRADA(13) <= Reg_entradas(13);
mi_ENTRADA(14) <= Reg_entradas(14);
mi_ENTRADA(15) <= Reg_entradas(15);
mi_ENTRADA(16) <= Reg_entradas(16);
mi_ENTRADA(17) <= Reg_entradas(17);
mi_ENTRADA(18) <= Reg_entradas(18);
mi_ENTRADA(19) <= Reg_entradas(19);
mi_ENTRADA(20) <= Reg_entradas(20);
mi_ENTRADA(21) <= Reg_entradas(21);
mi_ENTRADA(22) <= Reg_entradas(22);
mi_ENTRADA(23) <= Reg_entradas(23);
mi_ENTRADA(24) <= Reg_entradas(24);
mi_ENTRADA(25) <= Reg_entradas(25);
mi_ENTRADA(26) <= Reg_entradas(26);
mi_ENTRADA(27) <= Reg_entradas(27);
mi_ENTRADA(28) <= Reg_entradas(28);
mi_ENTRADA(29) <= Reg_entradas(29);
mi_ENTRADA(30) <= Reg_entradas(30);
mi_ENTRADA(31) <= Reg_entradas(31);
enable <= '1';


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
Reg_salidas(16) <= mi_SALIDA(16);
Reg_salidas(17) <= mi_SALIDA(17);
Reg_salidas(18) <= mi_SALIDA(18);
Reg_salidas(19) <= mi_SALIDA(19);
Reg_salidas(20) <= mi_SALIDA(20);
Reg_salidas(21) <= mi_SALIDA(21);
Reg_salidas(22) <= mi_SALIDA(22);
Reg_salidas(23) <= mi_SALIDA(23);
Reg_salidas(24) <= mi_SALIDA(24);
Reg_salidas(25) <= mi_SALIDA(25);
Reg_salidas(26) <= mi_SALIDA(26);
Reg_salidas(27) <= mi_SALIDA(27);
Reg_salidas(28) <= mi_SALIDA(28);
Reg_salidas(29) <= mi_SALIDA(29);
Reg_salidas(30) <= mi_SALIDA(30);
Reg_salidas(31) <= mi_SALIDA(31);


end Behavioral;

