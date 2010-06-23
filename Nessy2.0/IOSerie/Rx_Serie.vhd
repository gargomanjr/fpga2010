----------------------------------------------------------------------------------
-- Company:  		UCM Facultad de Informática
-- Engineer:		Carlos Sánchez-Vellisco Sánchez
--					Antonio José García Martínez
--					David Fernández Maiquez
-- 
-- Create Date:     18:31:36 11/18/2009 
-- Design Name: 	Transmisor Serie
-- Module Name:    	Rx_Serie - Behavioral 
-- Project Name: 	Nessy 2.0
-- Target Devices:  XC2VP30
-- Tool versions: 	Xilinx 10.1
-- Description: 	Receptor Serie ( Protocolo RS232)
--					Se divide el diseño en bloques	
--
--						->Divisor de frecuencia
--						->Registro de desplazamiento: El registro de desplazamiento 
--						Serial In/Parallel Out (Desplz_SIPO) es un registro al que se
--						le van cargando los datos en serie y los devuelve en paralelo.
--						Como el primer bit que se recibe es el 0, si la carga serie se 
--						hace por el bit más significativo del registro y se hacen 
--						desplazar los bits hacia la derecha, en el último desplazamiento
--						el bit 0 recibido estará en el bit menos significativo del 
--						registro, estando así todos los bits ordenados. La carga y el 
--						desplazamiento se realizan bajo la orden de la señal Desplaza
--						que sale del bloque de control.	
--						->Registro de entrada: Como la entrada de la comunicación serie 
--						RxDatoserie es asíncrona, se almacena en un registro RxDatoReg
--						para evitar pulsos y entradas no deseadas.
--						->Circuito de control
------------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

---- Uncomment the following library declaration if instantiating
---- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;
-----------------------------------------------------------------------------------
-- PUERTO    	NºBits		INFO
--
-- RstN				1		Entrada	Señal de reset asíncrono (activo a ‘0’)
-- Clk				1		Entrada	Señal de reloj de la placa, en principio de 100MHz,
--							pero configurable por gFrecClk
-- RxDatoSerie		1		Entrada	Trama que recibe del PC, que sigue el protocolo RS232
-- DatoRxOut		8		Salida	El dato que se ha recibido. Este dato sólo es válido
--							desde que AvisoRx vale ‘1’ y mientras recibiendo sea ‘0’.
-- AvisoRx			1		Salida	Aviso de que se ha recibido un nuevo dato y que está
--							disponible en DatoRxOut. El aviso se dará poniendo la señal a
--							‘1’ durante un ciclo de reloj.
-- Recibiendo		1		Salida	Indica que el receptor se encuentra recibiendo un dato
--							y por lo tanto el valor de DatorxOut no es válido.

-----------------------------------------------------------------------------------
entity Rx_Serie is
	 generic (
				gFrecClk : integer :=100000000; --31125; --100MHz
				gBaud : integer :=9600--3 --9600bps			
				);
    Port ( Rstn : in  STD_LOGIC;
           Clk : in  STD_LOGIC;
           RxDatoSerie : in  STD_LOGIC;
           DatoRxOut : out  STD_LOGIC_VECTOR (7 downto 0);
           AvisoRx : out  STD_LOGIC;
           Recibiendo : out  STD_LOGIC);
end Rx_Serie;

----------------------------------------------------------------------------------

architecture Behavioral of Rx_Serie is



   -- Tipo nuevo para los estados --
	
	type t_Estado is (eInit,eBitInit,eBitsDato,eBitFin);
	constant cFinCuenta : natural := (gFrecClk/gBaud)-1;
	constant cFinCuenta_Medio : natural := cFinCuenta / 2 ;
	
	-- Señales auxiliares
	
	signal Cuenta,CuentaBits: integer;
	signal ClkBaud,EnableCont,Dsplza,FinDsplza8bits,ClkBaudMedio :  std_logic;
	
	signal Estado : t_Estado;
	signal Rx_Registro: STD_LOGIC_VECTOR (7 downto 0);
	signal MiDatoTxIn:STD_LOGIC_VECTOR (7 downto 0);
	signal SalidaSeleccion:std_logic;
	signal RxDatoReg : std_logic;
	
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
				ClkBaudMedio <= '0';
				--Rx_Registro<="11111111";----?¿?
			elsif EnableCont = '1' then
				if Clk'event and Clk='1' then
						if Cuenta = cFinCuenta then
							Cuenta <= 0;
							ClkBaud <= '1';
							ClkBaudMedio <= '0'; 
						elsif Cuenta = cFinCuenta_Medio then
							ClkBaudMedio <= '1';
							Cuenta <= Cuenta + 1;
							ClkBaud <= '0';    
						else
							Cuenta <= Cuenta + 1;
							ClkBaud <= '0';
							ClkBaudMedio <= '0';
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
	
	POut: Process (Estado, RxDatoReg, ClkBaudMedio,ClkBaud) --RxDatoSerie
	begin
		EnableCont <= '0';
		Dsplza <= '0';
		--recibiendo <= '1';
		case Estado is
			when eInit =>
				EnableCont <= '0';
				Recibiendo <= '0';
				AvisoRx <= '0';
				Dsplza <= '0';
				if RxDatoReg = '0' then
				   AvisoRx <= '1';
					EnableCont <= '1';
				end if;
			when eBitInit => 
				EnableCont <= '1';
				Recibiendo <= '1';
			when eBitsDato =>
				EnableCont <= '1';
				Recibiendo <= '1';
				if ClkBaudMedio = '1' then
					Dsplza <= '1';
				end if;
			when eBitFin =>
				Recibiendo <= '1';
				if ClkBaud = '1' then
					EnableCont <= '0';
				else
					EnableCont <= '1';
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
					if RxDatoReg = '0' then
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
	
	recibe_desplazamiento: process(Dsplza)
	begin
		if Estado = eBitsDato and Dsplza = '1' then
			Rx_Registro(CuentaBits) <= RxDatoReg ;
		end if;	
	end process;
	
----------------------------------------------------------------------------------			
	-- Proceso que funciona como un biestable con la señal de entrada
	-- RxDatoReg seleccionando un 1 cuando no hay nada en la línea.
	
	biestable: process(Clk,RstN)
	begin
		if (RstN = '0') then
			RxDatoReg <= '1';
		elsif (Clk'event and Clk = '1') then
			RxDatoReg <= RxDatoSerie;
		end if;
	end process;

DatoRxOut <= Rx_Registro;
FinDsplza8bits <= '1' when CuentaBits=7 and ClkBaud = '1' else '0';

end Behavioral;






