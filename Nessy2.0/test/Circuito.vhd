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

entity Circuito is
    Port ( entrada: in  STD_LOGIC_VECTOR (31 downto 0);
			  clk: in std_logic;
           salida : out  STD_LOGIC_VECTOR (31 downto 0));
end Circuito;
------------------------------------------------------------------------------


architecture Behavioral of Circuito is


   -- Señales auxiliares -- 
	

begin

	process (clk)                                  
  	begin
			if (clk'event and clk = '1') then 
				salida(31 downto 24) <= entrada(31 downto 24) + '1';
				salida(23 downto 0) <= entrada(23 downto 0);
			end if;
  	end process;	
end Behavioral;

------------------------------------------------------------------------------