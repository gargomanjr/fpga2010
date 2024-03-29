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
-- De pendencies: 
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
	Generic(a : in integer := 3;
			b : in integer := 5);
    Port ( reset : in  STD_LOGIC;
			clk : in  STD_LOGIC;
			enable : in STD_LOGIC;
			load : in STD_LOGIC;
			data_load : in  STD_LOGIC_VECTOR (15 downto 0);
			salida : out  STD_LOGIC_VECTOR (15 downto 0));
end Contador;
------------------------------------------------------------------------------


architecture Behavioral of Contador is


   -- Se�ales auxiliares -- 
	
	signal mienable, mireset, miload: std_logic;
	--signal aux: std_logic_vector(31 downto 0);
	signal aux: integer;
	signal misalida,midata_load: std_logic_vector(15 downto 0);

begin

	process (clk,reset,mienable,load)                                  
  	begin
		if mireset = '1' then
			misalida <= x"0111";
		elsif clk'event and clk = '1' then
			if mienable = '1' then
				misalida <= misalida +1;
			elsif miload = '1' then
				misalida <= midata_load;
			end if;
		end if;
  	end process;
	
	salida <= misalida;
	mireset <= reset;
   mienable <= enable;
	miload <= load;
	midata_load<=data_load;
	
end Behavioral;

------------------------------------------------------------------------------