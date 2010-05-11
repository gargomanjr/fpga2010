-- ------------------------------------------------------- --
--  Description: TRISTAT_REG for storing the failure       --
--               detected                                  --
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

entity TRISTATE_REG is	 
	GENERIC (
		Priority		: IN boolean := FALSE;  -- priority (in case of data mismatch and error_a = error_b) 
		AccuBits		: IN integer := 4;      -- extra bits ofr the sum
		CoefficientsBits : IN integer := 10;	-- bits number of coefficients 	
		DataBits		: IN integer := 8); 	-- Rxin data bit width
	port (	  
		clk				: in std_logic;
		reset			: in std_logic;
		tristate_in 	: in std_logic;
		error_in 		: in std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		dataout_in			: in std_logic;	
		tristate_r	 	: out std_logic;
		error_r 		: out std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		dataout_r		: out std_logic			
		);
end TRISTATE_REG;

--}} End of automatically maintained section

architecture TRISTATE_REG of TRISTATE_REG is
	
	signal tristate_reg : std_logic;  
	signal error_reg 	: std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);	 
	signal dataout_reg	: std_logic;
	
begin						   
	----------------------------------------		
	----registers----------------------------
	----------------------------------------  	
	
	REG_TRISTATE : process (reset,clk)
	begin  			
		if reset = '1' then
			tristate_reg <= '0';
		elsif rising_edge(clk) then
			if tristate_reg = '0' then
				tristate_reg <= tristate_in;
			end if;
		end if;
	end process REG_TRISTATE;	
	
	REG_ERROR : process (reset,clk)	 	
	begin		
		if reset = '1' then
			error_reg <= (others => '0'); 
		elsif rising_edge(clk) then
			---Output registers
			error_reg <= error_in;  
		end if;	  	   
	end process REG_ERROR;  
	
	REG_DATAOUT : process (reset,clk)	 	
	begin	 
		if reset = '1' then
			dataout_reg <= '0';
		elsif rising_edge(clk) then
			dataout_reg <= dataout_in;   
		end if;	  				
	end process REG_DATAOUT;  	   
	
	tristate_r <= tristate_reg;
	error_r 	<= error_reg; 
	dataout_r 	<= dataout_reg; 
	
end TRISTATE_REG;
