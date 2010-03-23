----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    18:31:36 11/18/2009 
-- Design Name: 
-- Module Name:    Rx_Serie - Behavioral 
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

entity Rx_Serie is
	 generic (
				gFrecClk : integer :=100000000;--31125;--100000000; --31125; --100MHz
				gBaud : integer :=9600--3--9600;--3 --9600bps			
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
	-- Señales
	
	signal Cuenta,CuentaBits: integer;
	signal ClkBaud,EnableCont,Dsplza,FinDsplza8bits,ClkBaudMedio :  std_logic;
	
	signal Estado : t_Estado;
	signal Rx_Registro: STD_LOGIC_VECTOR (7 downto 0);
	signal MiDatoTxIn:STD_LOGIC_VECTOR (7 downto 0);
	signal SalidaSeleccion:std_logic;
	signal RxDatoReg : std_logic;
	
begin

----------------------------------------------------------------------------------	
	-- Proceso encargado de dividir la frecuencia del reloj de la placa y 
	-- generar ClkBaud
	
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
		Recibiendo <= '1';
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
			when eBitsDato =>
				EnableCont <= '1';
				if ClkBaudMedio = '1' then
					Dsplza <= '1';
				end if;
			when eBitFin =>	
				if ClkBaud = '1' then
					EnableCont <= '0';
				else
					EnableCont <= '1';
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






