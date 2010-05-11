-- ------------------------------------------------------- --
--  Description: SMARTVOTER for activating the tristate    --
--               buffers to only forward the majority      --
--               result                                    --
--                                                         --
--  Specifications: Constants signals are used to specify  --
--                  generic values of the designed filter  --
--  Author: Shih-Fu Liu                                    --
--          Universidad Antonio de Nebrija. Madrid. Spain  --
--  Contact:sliu@nebrija.es                                --
-- ------------------------------------------------------  -- 

LIBRARY ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
--use ieee.std_logic_arith.all;
use ieee.std_logic_signed.all;
use work.all;

entity SMARTVOTER is	   
	GENERIC (
		Priority		: IN boolean := FALSE;  -- priority (in case of data mismatch and error_a = error_b) 
		AccuBits		: IN integer := 4;      -- extra bits ofr the sum
		CoefficientsBits : IN integer := 10;	-- bits number of coefficients 	
		DataBits		: IN integer := 8); 	-- Rxin data bit width
	port (
		tristate_ina 	: in std_logic; 
		error_a	 	: in std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);	
		datain_a 	: in std_logic;
		tristate_inb 	: in std_logic; 
		error_b	 	: in std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0); 
		datain_b 	: in std_logic;
		tristate 	: out std_logic);
end SMARTVOTER;

--}} End of automatically maintained section

architecture SMARTVOTER of SMARTVOTER is
begin				 
	
	VOTER: process (error_a,error_b,datain_a,datain_b,tristate_ina,tristate_inb) 
	begin
		if tristate_ina = '0' then
			if tristate_inb = '0' then
				tristate <= '1';  
				if (datain_a = datain_b) then
					if (abs(conv_integer(error_a)) < 6144) then
						tristate <= '0'; 
					else
						tristate <= '1';
					end if;
				elsif (abs(conv_integer(error_a)) < abs(conv_integer(error_b))) then
					tristate <= '0';
				elsif (abs(conv_integer(error_a)) = abs(conv_integer(error_b))) then 
					if Priority = TRUE then
						tristate <= '0';
					else
						tristate <= '1';	 
					end if;
				else
					tristate <= '1';
				end if;	
			else
				if (abs(conv_integer(error_a)) < 6144) then
					tristate <= '0'; 
				else
					tristate <= '1';
				end if;
			end if;
		else 
			tristate <= '1';
		end if;
	end process;  	 
	
end SMARTVOTER;
