-- ------------------------------------------------------- --
--  Description: The top-level file for joining all the    --
--               modules to complete the FFE_DMR           --
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
--synopsis translate_off
library UNISIM;			  
--synopsis translate_on


use work.all;

entity FFE_FPGA is		 
	GENERIC (N 			: IN integer := 16;    	-- delay taps
		AccuBits		: IN integer := 4;      -- extra bits ofr the sum
		CoefficientsBits 	: IN integer := 10;	-- bits number of coefficients 	
		DataBits		: IN integer := 8); 	-- Rxin data bit width
	port ( 
		Rx_DR0 		  : in std_logic_vector (DataBits-1 downto 0); 
		clk_DR0 	  : in std_logic;
		reset_DR0 	  : in std_logic;
		error_out_0   : out std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		dataout_0     : out std_logic;  
		scrub_0  	  : out std_logic; 
		dataout_DR0   : out std_logic;	
		----------------------------------------------------------------------------------
		Rx_DR1 		  : in std_logic_vector (DataBits-1 downto 0); 
		clk_DR1 	  : in std_logic;
		reset_DR1 	  : in std_logic;
		error_out_1   : out std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		dataout_1     : out std_logic;  
		scrub_1  	  : out std_logic; 
		dataout_DR1   : out std_logic	
		);		   
	
end FFE_FPGA;

architecture FFE_FPGA of FFE_FPGA is	  	
	
	---------INTERNAL SIGNALS	FFE0  
	signal error_int_0    		: std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_int_0  		: std_logic;  	
	signal tristate_int_0 		: std_logic;  
	signal tristate_r_0 		: std_logic;
	signal error_r_0    		: std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_r_0  		: std_logic;  
	----------INTERNAL SIGNALS	FFE1  
	signal error_int_1    		: std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_int_1  		: std_logic;  	
	signal tristate_int_1 		: std_logic;  
	signal tristate_r_1 		: std_logic;
	signal error_r_1   			: std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_r_1  		: std_logic;  
	---Clock from IBUFG	
	signal clk_DR0_BUF   : std_logic;
	signal clk_DR1_BUF   : std_logic;
	
	-- Component declaration of the "ibufg(ibufg_v)" unit defined in
	-- file: "./src/unisim_vital.vhd"
	component ibufg
		generic(
			CAPACITANCE : STRING := "DONT_CARE";
			IBUF_DELAY_VALUE : STRING := "0";
			IOSTANDARD : STRING := "DEFAULT");
		port(
			O : out STD_ULOGIC;
			I : in STD_ULOGIC);
	end component;		
	-- Component declaration of the "obuft(obuft_v)" unit defined in
	-- file: "./src/unisim_vital.vhd"
	component obuft
		generic(
			CAPACITANCE : STRING := "DONT_CARE";
			DRIVE : INTEGER := 12;
			IOSTANDARD : STRING := "DEFAULT";
			SLEW : STRING := "SLOW");
		port(
			O : out STD_ULOGIC;
			I : in STD_ULOGIC;
			T : in STD_ULOGIC);
	end component;		
	
	
begin		
	
	clk_DR0_IBUFG : ibufg
	port map(
		O => clk_DR0_BUF,
		I => clk_DR0
		);
	clk_DR1_IBUFG : ibufg
	port map(
		O => clk_DR1_BUF,
		I => clk_DR1
		);
	----------------------------------------		
	----FILTERS-----------------------------
	----------------------------------------	   
	FFE0 : entity work.ffe_top
	generic map(
		N => N,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		reset => reset_DR0,
		clk => clk_DR0_BUF,
		Rx => Rx_DR0,
		error_out => error_int_0,
		dataout => dataout_int_0
		);	
	FFE1 : entity work.ffe_top
	generic map(
		N => N,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		reset => reset_DR1,
		clk => clk_DR1_BUF,
		Rx => Rx_DR1,
		error_out => error_int_1,
		dataout => dataout_int_1
		);	 
	----------------------------------------		
	----SMART VOTERS------------------------
	----------------------------------------
	SMARTVOTER0 : entity work.smartvoter
	generic map(
		Priority => TRUE,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		tristate_ina => tristate_r_0,
		tristate_inb => tristate_r_1,
		error_a => error_int_0,
		datain_a => dataout_int_0,	
		error_b => error_int_1,
		datain_b => dataout_int_1,
		tristate => tristate_int_0
		);	
	
	SMARTVOTER1 : entity work.smartvoter
	generic map(
		Priority => FALSE,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		tristate_ina => tristate_r_1, 
		tristate_inb => tristate_r_0,
		error_a => error_int_1,
		datain_a => dataout_int_1,	
		error_b => error_int_0,
		datain_b => dataout_int_0,
		tristate => tristate_int_1
		);	  
	----------------------------------------		
	----tristate reg------------------------
	----------------------------------------		 
	
	TRISTATE_REG0 : entity work.tristate_reg
	generic map(
		Priority => FALSE,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		clk => clk_DR0_BUF,
		reset => reset_DR0,
		tristate_in => tristate_int_0,
		error_in => error_int_0,
		dataout_in => dataout_int_0,
		tristate_r => tristate_r_0,
		error_r => error_r_0,
		dataout_r => dataout_r_0
		);
	TRISTATE_REG1 : entity work.tristate_reg
	generic map(
		Priority => FALSE,
		AccuBits => AccuBits,
		CoefficientsBits => CoefficientsBits,
		DataBits => DataBits
		)
	port map(
		clk => clk_DR1_BUF,
		reset => reset_DR1,
		tristate_in => tristate_int_1,
		error_in => error_int_1,
		dataout_in => dataout_int_1,
		tristate_r => tristate_r_1,
		error_r => error_r_1,
		dataout_r => dataout_r_1
		);
	----------------------------------------		
	----tristate buffers--------------------
	----------------------------------------
	BUF0 : obuft
	port map(
		O => dataout_DR0,
		I => dataout_r_0,
		T => tristate_int_0
		); 
	
	BUF1 : obuft
	port map(
		O => dataout_DR1,
		I => dataout_r_1,
		T => tristate_int_1
		);				
	----------------------------------------		
	----OUTPUTS REGISTERS-------------------
	---------------------------------------- 	
	
	----------SIGNAL OUTPUTS FFE0
	error_out_0	<= error_r_0;
	dataout_0 <= dataout_r_0;	  
	scrub_0 <= tristate_r_0;	   
	
	----------SIGNAL OUTPUTS FFE1
	error_out_1	<= error_r_1;
	dataout_1 <= dataout_r_1;	  
	scrub_1 <= tristate_r_1;	
	
end FFE_FPGA;
