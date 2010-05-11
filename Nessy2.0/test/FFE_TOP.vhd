-- ------------------------------------------------------- --
--  Description: Buffering the data before forwarding to   --
--               the design                                --
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
--synopsis translate_off
library UNISIM;			  
--synopsis translate_on

entity FFE_TOP is	
	GENERIC (N 			: IN integer := 16;    	-- delay taps
		AccuBits		: IN integer := 4;      -- extra bits ofr the sum
		CoefficientsBits 	: IN integer := 10;	-- bits number of coefficients 	
		DataBits		: IN integer := 8); 	-- Rxin data bit width
	port ( 		
		reset 	  : in std_logic;
		clk	  	  : in std_logic;
		Rx		  : in std_logic_vector (DataBits-1 downto 0); 	
		error_out : out std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		dataout   : out std_logic
		);
end FFE_TOP;

architecture FFE_TOP of FFE_TOP is		  
	
	---Input registers
	signal Rx_reg 		  :  std_logic_vector (DataBits-1 downto 0); 
	---Output registers
	signal error_out_reg   :  std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_reg     :  std_logic;  	   	
	---------INTERNAL SIGNALS	FFE0  
	signal error_int    : std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_int  : std_logic;  	
	
	attribute IOB : string;		   
	attribute IOB of Rx_reg : signal is "TRUE";
	
begin		
	
	----------------------------------------		
	----registers----------------------------
	----------------------------------------  	
	
	REG : process (reset,clk)	 	
	begin		
		if reset = '1' then
			---Input registers
			--Rx_reg <= (others =>'0');	 
			Rx_reg <= "00001100";	 
			---Output registers
			error_out_reg <= (others => '0'); 
			dataout_reg <= '0';
		elsif rising_edge(clk) then	
			---Input registers
			Rx_reg <= Rx;	 
			---Output registers
			error_out_reg <= error_int; 
			dataout_reg <= dataout_int;   
		end if;	
	end process REG;
	
	----------------------------------------		
	----FILTERS-----------------------------
	----------------------------------------	   
	FFE0 : entity work.ffe
	generic map(
		N => N,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		Rx => Rx_reg,
		clk => clk,
		reset => reset,
		error_out => error_int,
		dataout => dataout_int
		);	   
	----------------------------------------		
	----OUTPUTS REGISTERS-------------------
	---------------------------------------- 	
	----------SIGNAL OUTPUTS FFE
	error_out	<= error_out_reg;
	dataout 	<= dataout_reg;	  
	
end FFE_TOP;
