----------------------------------------------------------------------------------
-- Company:  UCM Facultad de Informática
-- Engineer:	Carlos Sánchez-Vellisco Sánchez
--				Antonio José García Martínez
--				David Fernández Maiquez	
-- 
-- Create Date:     19:27:16 11/05/2009 
-- Design Name:     Transmisor Serie
-- Module Name:     Tx_serie - Behavioral 
-- Project Name: 	Nessy 2.0
-- Target Devices:  XC2VP30
-- Tool versions:   Xilinx 10.1  
-- Description:		Transmisor serie  ( Protocolo RS232)
--					Lo dividiremos en cuatro bloques principales:
--						
--						->DivFrec: Un divisor de frecuencia. Dividirá la frecuencia
--						de reloj tantas veces como indique gFrecClk.
--						->Control: Una máquina de estados finitos.
--						->Carga_desplaz: Un registro de carga en paralelo.
--						->Selección: Un multiplexor que selecciona la señal de salida
--						según el estado actual. Este multiplexor termina en un biestable
--						para evitar pulsos no deseados, ya que su salida (DatoSerieOut)
--						es la salida del circuito.
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

---- Uncomment the following library declaration if instantiating
---- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;



----------------------------------------------------------------------------------

----------------------------------------------------------------------------------
--	PUERTO    	NºBits		INFO
--	
--	RstN		   1	Entrada	Señal de reset asíncrono (activo a ‘0’)
--	Clk			   1	Entrada	Señal de reloj de la placa, en principio de 100MHz,
--						pero configurable por gFrecClk
--	Transmite	   1	Entrada	Señal del sistema que ordena al módulo la transmisión
--						del dato que se encuentra en DatoTxIn.
--	DatoTxIn	   8	Entrada	El dato a enviar. Se proporciona de manera simultánea
 --						cuando Transmite = ‘1’
--	Transmitiendo  1	Salida	Señal del sistema que indica que en ese instante se está
--						transmitiendo un dato.
--	DatoSerieOut   1	Salida	Trama de datos que se envía al PC y que sigue el 
--						protocolo RS232
--
----------------------------------------------------------------------------------
entity Tx_serie is
--
 generic (
				gFrecClk : integer := 100000000; --31125;-- --100MHz
				gBaud : integer := 9600 --9600bps
			);
    Port ( RstN : in  STD_LOGIC;
           clk : in  STD_LOGIC;
           Transmite : in  STD_LOGIC;
           DatoTxIn : in  STD_LOGIC_VECTOR (7 downto 0);
           Transmitiendo : out  STD_LOGIC;
           DatoSerieOut : out  STD_LOGIC);
end Tx_serie;

----------------------------------------------------------------------------------

architecture Behavioral of Tx_serie is


   -- Tipo nuevo para los estados --
	
	type t_Estado is (eBitInit,eBitsDato,eBitFin,eInit);
	
	constant cFinCuenta : natural := (gFrecClk/gBaud)-1;
	
	-- Señales auxiliares
	
	signal Cuenta,CuentaBits: integer;
	signal ClkBaud,EnableCont,Dsplza,FinDsplza8bits,CargaDato :  std_logic;
	signal Estado : t_Estado;
	signal Registro: STD_LOGIC_VECTOR (7 downto 0);
	signal SalReg: std_logic;
	signal MiDatoTxIn:STD_LOGIC_VECTOR (7 downto 0);
	signal SalidaSeleccion:std_logic;
	
begin

----------------------------------------------------------------------------------	
	-- A partir del reloj de la placa de 100 MHz (Clk), queremos proporcionar una 
	-- señal con frecuencia de 9600 Hz (ClkBaud). Este reloj tendrá por tanto un 
	-- periodo de 104,167 µs, y estará a ‘1’ durante un solo ciclo de reloj, estando
	-- el resto de tiempo a ‘0’.
	
	P_DivFrec: Process (RstN, Clk)
	begin
			if RstN = '0' then
				Cuenta <= 0;
				ClkBaud <= '0';
			elsif Clk'event and Clk='1' then
				if EnableCont = '1' then
					if Cuenta = cFinCuenta then
						Cuenta <= 0;
						ClkBaud <= '1';
					else
						Cuenta <= Cuenta + 1;
						ClkBaud <= '0';
					end if;
				end if;
			end if;
	end process;
----------------------------------------------------------------------------------	
	
	-- Proceso encargado de contar los 8 bits de datos.
	-- Solo cuenta cuando estamos enviando datos, es decir en el estado eBitsDato 
	
	P_CuentaBits: Process (RstN, Clk)
	begin
		if RstN = '0' then
			CuentaBits <= 0;
		elsif Clk'event and Clk='1' then
			if Estado = eBitsDato then
				if ClkBaud = '1' then
					if CuentaBits = 7 then
						CuentaBits <= 0;
					else
						CuentaBits <= CuentaBits + 1;
					end if;
				end if;
			else
				CuentaBits <= 0;
			end if;
		end if;
	end process;

----------------------------------------------------------------------------------	
	
	-- Proceso que controla las señales internas necesarias para el 
	-- registro de desplazamiento y para el contador de los 8 bits de datos
	
	POut: Process (Estado, Transmite, ClkBaud)
	begin
		Dsplza <= '0';
		CargaDato <= '0';
		EnableCont <= '1';
		
		case Estado is
			when eInit =>
				EnableCont <= '0';
				Transmitiendo <= '0';
				if Transmite = '1' then
					CargaDato <= '1';
					EnableCont <= '1';
				end if;
			when eBitInit => 	Transmitiendo <= '1';		
			when eBitsDato => Transmitiendo <= '1';
				if ClkBaud = '1' then 
					Dsplza <= '1';
				end if;
			when eBitFin =>
				Transmitiendo <= '1';
				if ClkBaud = '1' then
					EnableCont <= '0';
					
				end if;
			end case;
	end process;
----------------------------------------------------------------------------------	

-- Proceso de la máquina de estados 
-- eInit:    Es el estado inicial. El sistema está en reposo esperando la orden de
-- 		     transmitir. Cuando la señal Transmite se ponga a ‘1’, se pasará a enviar el 
--           bit de inicio, pasando para ello al siguiente estado (eBitInit). En ese momento
--           se dará la orden de cargar el dato (DatoTxIn) en el registro de desplazamiento.
--           También tendremos que sincronizar el contador del divisor de frecuencia; para 
--           esto haremos que en el estado inicial no cuente, y en el resto se habilite el 
--           contador.
-- eBitInit: En este estado se está enviando el bit de inicio. Se saldrá de este estado 
--			 al recibir un pulso de ClkBaud, que nos dirá que debemos pasar a enviar los bits
--      	 de dato. El siguiente estado es eBitsDato.
-- eBitsDato:Este estado se encarga de enviar los 8 bits del dato. Utilizando un contador,
--           llevaremos la cuenta del número de bits que se han enviado. Cuando se hayan 
--           enviado los 8 bits –es decir, cuando hayan llegado 8 pulsos de Clkbaud-  se 
--           activará la señal FinDsplza8bits que hará que cambiemos al siguiente estado 
--           (eBitFin).
-- eBitFin:  Este estado envía el bit de fin. Al llegar el siguiente pulso de ClkBaud, 
--           cambiaremos al estado inicial eInit.

	
	P_Control_FSM: Process (RstN, Clk)
	begin
		if RstN = '0' then
			Estado <= eInit;
		elsif Clk'event and Clk='1' then
			case Estado is
				when eInit =>
					if Transmite = '1' then
						Estado <= eBitInit;
					end if;
				when eBitInit =>
					if ClkBaud = '1' then
						Estado <= eBitsDato;
					end if;
				when eBitsDato =>
					if FinDsplza8bits = '1' then
						Estado <= eBitFin;
					end if;
				when eBitFin =>
					if ClkBaud = '1' then
						Estado <= eInit;
					end if;
			end case;
		end if;
	end process;
----------------------------------------------------------------------------------		
	
	-- Proceso que se encarga de ir poniendo en SalReg el bit del registro 
	-- de desplazamiento concecreto.
	
	carga_desplazamiento: process(Dsplza,CargaDato)
	begin
		if CargaDato='1' then
			Registro <= DatoTxIn;
		else
			SalReg <= Registro(CuentaBits);
		end if;	
	end process;
	
----------------------------------------------------------------------------------	
	-- Proceso encargado de elegir la salida en función del Estado en el que
	-- estemos
	
	seleccion: process(Estado)
		begin
		if Estado = eInit then
			SalidaSeleccion <= '1';
		elsif Estado = eBitInit then
			SalidaSeleccion <= '0';
		elsif Estado = eBitsDato then
			SalidaSeleccion <= SalReg;
		elsif Estado = eBitFin then
			SalidaSeleccion <= '1';
		end if;
		--	end if;
	end process;
	
	DatoSerieOut <= SalidaSeleccion;
	FinDsplza8bits <= '1' when CuentaBits=7 and ClkBaud = '1' else '0';
	
	
end Behavioral;

