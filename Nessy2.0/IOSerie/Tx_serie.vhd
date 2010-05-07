----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    19:27:16 11/05/2009 
-- Design Name: 
-- Module Name:    Tx_serie - Behavioral 
-- Project Name: 
-- Target Devices: 
-- Tool versions: 
-- Description: 
--
-- Dependencies: 
--
-- Revision: 
-- Revision 0.01 - File Created
-- Additional Comments: 
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
	
	-- Señales
	
	signal Cuenta,CuentaBits: integer;
	signal ClkBaud,EnableCont,Dsplza,FinDsplza8bits,CargaDato :  std_logic;
	signal Estado : t_Estado;
	signal Registro: STD_LOGIC_VECTOR (7 downto 0);
	signal SalReg: std_logic;
	signal MiDatoTxIn:STD_LOGIC_VECTOR (7 downto 0);
	signal SalidaSeleccion:std_logic;
	
begin

----------------------------------------------------------------------------------	
	-- Proceso encargado de dividir la frecuencia del reloj de la placa y 
	-- generar ClkBaud
	
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
			--Transmitiendo <= '0';
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

