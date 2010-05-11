-- ------------------------------------------------------- --
--  Description: vhdl code used to test the behavior of    --
--               the design                                --
--  Specifications: Constants signals are used to specify  --
--                  generic values of the designed filter  --
--  Author: Universidad Antonio de Nebrija. Madrid. Spain  --
--  Contact: sliu@nebrija.es                             --
-- ------------------------------------------------------  -- 


LIBRARY ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
--use ieee.std_logic_arith.all;
use ieee.std_logic_signed.all;
use work.all;

use std.textio.all;
use work.txt_util.all;

entity FFE_FPGA_tb is
end FFE_FPGA_tb;

--}} End of automatically maintained section

architecture FFE_FPGA_tb of FFE_FPGA_tb is	  
	
	---FFE0 and FFE1 shared parameters
	
	constant N : integer := 16;
	constant AccuBits : integer := 4;
	constant CoefficientsBits : integer := 10;
	constant DataBits : integer := 8;	 
	
	-- Component declaration of the "ffe_fpga_v0_1(ffe_fpga_v0_1)" unit defined in
	-- file: "./src/design/ffe_fpga_v0_1.vhd"
	component ffe_fpga
		--		generic(
		--			N : INTEGER := 16;
		--			AccuBits : INTEGER := 4;
		--			CoefficientsBits : INTEGER := 10;
		--			DataBits : INTEGER := 8);
		port(	  
			Rx_DR0 : in STD_LOGIC_VECTOR(DataBits-1 downto 0);
			clk_DR0 : in STD_LOGIC;
			reset_DR0 : in STD_LOGIC;
			error_out_0 : out STD_LOGIC_VECTOR(DataBits+CoefficientsBits+AccuBits-1 downto 0);
			dataout_0 : out STD_LOGIC;
			scrub_0 : out STD_LOGIC;
			dataout_DR0 : out STD_LOGIC;
			Rx_DR1 : in STD_LOGIC_VECTOR(DataBits-1 downto 0);
			clk_DR1 : in STD_LOGIC;
			reset_DR1 : in STD_LOGIC;
			error_out_1 : out STD_LOGIC_VECTOR(DataBits+CoefficientsBits+AccuBits-1 downto 0);
			dataout_1 : out STD_LOGIC;
			scrub_1 : out STD_LOGIC;
			dataout_DR1 : out STD_LOGIC);
	end component;	 	
	
	-- // SIGNAL ASSIGMENT // --	
	signal Rx : std_logic_vector ((DataBits-1) downto 0):= (others => '0');
	signal clk : std_logic := '0';
	signal Rx_from_file : std_logic := '0';
	signal datain_from_file : std_logic := '0';
	signal reset : std_logic := '0';
	signal Tx : std_logic := '0';
	SIGNAL done: std_logic := '0';	  

  
        signal cycles : real := -12.0;
        signal error_count : real := 0.0;
        signal percentage : real := 0.0;
	signal error_out_0 : std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_0 : std_logic; 
	signal scrub_0 : std_logic; 
	signal dataout_DR0 : std_logic; 
	signal error_out_1 : std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
	signal dataout_1 : std_logic; 
	signal scrub_1 : std_logic; 
	signal dataout_DR1 : std_logic; 
	
	SIGNAL dataout_DR : std_logic;
	
begin	
	DUT : ffe_fpga
	--	generic map(
	--		N => N,
	--		AccuBits => AccuBits,
	--		CoefficientsBits => CoefficientsBits,
	--		DataBits => DataBits
	--		)
	port map(		
		Rx_DR0 => Rx,
		clk_DR0 => clk,
		reset_DR0 => reset,
		error_out_0 => error_out_0,
		dataout_0 => dataout_0,
		scrub_0 => scrub_0,
		dataout_DR0 => dataout_DR0,
		Rx_DR1 => Rx,
		clk_DR1 => clk,
		reset_DR1 => reset,
		error_out_1 => error_out_1,
		dataout_1 => dataout_1,
		scrub_1 => scrub_1,
		dataout_DR1 => dataout_DR1
		);	
	
	-- provide stimulus and check the result
	TESTCLK : process
		
	begin
		clk <= transport '0';
		wait for 50 ns;
		clk <= transport '1';
		wait for 50 ns;	
	end process;
	
	-- data_from_file signal determines the time when input file --    
	DATAINVALID : process
		
	begin
		datain_from_file <= not datain_from_file after 2 ns;
		wait for 50 ns;
	end process;
	
	RESETSIGNAL : process		
	begin	
		reset <= '1';
		wait for 100 ns;
		reset <= '0';
		wait;	
	end process;
	
	
    READ_INPUTS: process   
    
        --file vector_file: text;
        
        file Rx_stimulus_file: TEXT open read_mode is "Rx.txt";
        file Tx_stimulus_file: TEXT open read_mode is "Tx.txt";
        
        variable var_Rx: std_logic_vector((DataBits-1) downto 0);
        variable var_Tx: std_logic;

        -- one variable for each input file --
        variable str_Rx: string(DataBits downto 1);
        variable str_Tx: character;
        -- count errors and current file line of input and output file --
        variable err_cnt: integer := 0;
        variable file_line: line;

 
    begin
        --file_open(vector_file,"Rx.txt",READ_MODE);
        wait until rising_edge(datain_from_file);
        while not endfile(Rx_stimulus_file) loop
            -- read datain stimulus --
            readline (Rx_stimulus_file,file_line);
            read (file_line,str_Rx) ;
            -- read dataout matlab results --
            readline (Tx_stimulus_file,file_line);
            read (file_line,str_Tx) ;
	   
            var_Rx := to_std_logic_vector(str_Rx);
            var_Tx := to_std_logic(str_Tx);
            
            --wait for 1 ns;
            --Get input side of vector...
            -- and expected outputs ...
     
             
            Rx <= var_Rx;
            Tx <= var_Tx;
            
            
            
            wait until rising_edge(clk);
            
            cycles <= cycles +1.0;
	    if (cycles > 0.0) then
	      if (Tx /= dataout_0) then
	        error_count <= error_count +1.0;
	      end if;
	      percentage <= ((error_count*100.0)/(cycles)); 
	    end if;  
        end loop;

        file_close(Rx_stimulus_file);
        file_close(Tx_stimulus_file);
        
        done <= '1';
        --reset <= '1';

        wait;
    end process READ_INPUTS;
	
	
	dataout_DR <= dataout_DR1;
	dataout_DR <= dataout_DR0;
	
end FFE_FPGA_tb;
