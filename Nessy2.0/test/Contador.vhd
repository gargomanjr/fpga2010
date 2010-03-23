----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    16:56:23 10/20/2009 
-- Design Name: 
-- Module Name:    Contador - Behavioral 
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

-- Entidad del contador Ppal. Aqui se genera el pulso de transmite que usa
-- Tx_serie para transmitir un nuevo dato.

----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

---- Uncomment the following library declaration if instantiating
---- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;

-- Entidad del contador Ppal. Aqui se genera el pulso de transmite que usa
-- Tx_serie para transmitir un nuevo dato.

------------------------------------------------------------------------------

entity Contador is
    Port ( reset : in  STD_LOGIC;
           clk : in  STD_LOGIC;
			  enable : in STD_LOGIC;
			  load : in STD_LOGIC;
			  data_load : in  STD_LOGIC_VECTOR (3 downto 0);
			  cambiando : out std_logic;
           salida : out  STD_LOGIC_VECTOR (3 downto 0));
end Contador;
------------------------------------------------------------------------------


architecture Behavioral of Contador is


   -- Señales auxiliares -- 
	
	signal mienable, mireset, micambiando,miload: std_logic;
	--signal aux: std_logic_vector(31 downto 0);
	signal aux: integer;
	signal misalida,midata_load: std_logic_vector(3 downto 0);
	signal a:std_logic_vector(24 downto 0);

begin

	process (clk,reset,enable,load)                                  
  	begin
	if miload='1' then 
			misalida <= midata_load;
			micambiando <= '1';
			aux<=0;
	else
			if mienable='1' then 
				if mireset='1' then                     -- reset -> inicialización
					aux<=0;
					misalida <= "0000";
					micambiando <= '1';
				elsif(clk'event and clk='1') then     -- flanco de reloj ascendente
					if(aux=100000000) then           -- cada 100.000.000 ciclos (a 1 sg.)
						aux<=0;               -- vuelvo a comenzar
						micambiando <= '1';
						if misalida = "1111" then
							misalida <= "0000";
						else
							misalida <= misalida +1;
						end if;
					else
						aux<=aux+1;                     -- cuento
						micambiando <= '0';		
					end if;
				end if;
	end if;
	end if;
  	                                     -- saco la salida
  	end process;
	

	-- Asignación de señales --
	
	-- Como en la placa los botones están negados, negamos el reset.
	
	--salida(3 downto 0)<=aux(31 downto 28);
	salida <= misalida;
	mireset <= not reset;
   mienable <= enable;
	cambiando <= micambiando;
	miload <= load;
	midata_load<=data_load;
	
end Behavioral;

------------------------------------------------------------------------------