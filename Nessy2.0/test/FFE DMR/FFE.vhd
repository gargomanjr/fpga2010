-- ------------------------------------------------------- --
--  Description: This is the code for the Feed Forward     --
--               Equalizer which is already adapted to the --
--               attached stimulus.                        --
--                                                         --
--                                                         --
--  Specifications: Filter Taps: 16                        --
--                  Datain: 8 bits                         -- 
--                  Accumulator: 30bits                    --
--                                                         -- 
--  Author: Shih-Fu Liu                                    --
--          Universidad Antonio de Nebrija. Madrid. Spain  --
--  Contact:sliu@nebrija.es                                --
-- ------------------------------------------------------  -- 

LIBRARY ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_signed.all;
use work.all;

entity FFE is
	
	GENERIC (N 			: IN integer := 16;    	-- delay taps
		AccuBits		: IN integer := 4;      -- extra bits ofr the sum
		CoefficientsBits 	: IN integer := 10;	-- bits number of coefficients 	
		DataBits		: IN integer := 8); 	-- Rxin data bit width
	port (
		Rx		: in std_logic_vector (DataBits-1 downto 0); 
		clk, reset 	: in std_logic;
		error_out	: out std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		dataout 	: out std_logic);
end FFE;


architecture behavioral of FFE is
	
	CONSTANT CYCLES : integer := 16*16*16*16-10;   -- 2^16 = 65536
	
	constant bits30_1    :std_logic_vector (2*DataBits+CoefficientsBits+AccuBits-1 downto 0) := "010000000000000000000000000000";
	constant bits22_1    :std_logic_vector (DataBits+CoefficientsBits+AccuBits-1 downto 0) := "0000000010000000000000";
	constant bits22_neg1 :std_logic_vector (DataBits+CoefficientsBits+AccuBits-1 downto 0) := "1111111110000000000000";
	
	type t_DelayLine is array (0 to N-1) of std_logic_vector(DataBits-1 downto 0);
	type t_Coefficient_accum is array (0 to N-1) of std_logic_vector(2*DataBits+CoefficientsBits+AccuBits-1 downto 0);
	type t_Product is array (0 to N-1) of std_logic_vector(DataBits+CoefficientsBits-1 downto 0);
	signal Rx_current	: t_DelayLine;
	signal Rx_next		: t_DelayLine;
	signal product_out	: t_Product;
	signal dataout_next 	: std_logic_vector (DataBits+CoefficientsBits+AccuBits-1 downto 0);
	
	signal alpha		: std_logic;
	signal coefficients_accum_current	: t_Coefficient_accum;
	signal coefficients_accum_next	: t_Coefficient_accum;
	
	
begin
	-- -:-:-:-:-:-:-:-:-: --
	-- SEQUENTIAL PROCESS --
	-- -:--:-:-:-:-:-:-:- --
	
	SEQUENTIAL : process (clk, reset)
		
	begin 	 
		if (reset = '1') then 
			coefficients_accum_current(0) <=  "000000000000111100000011110000";
			coefficients_accum_current(1) <=  "000000000000101100110010110111";
			coefficients_accum_current(2) <=  "000000000000010110100110001010";
			coefficients_accum_current(3) <=  "000000000001110000011011110101";
			coefficients_accum_current(4) <=  "000000000010010011111010010011";
			coefficients_accum_current(5) <=  "000000000011010001001000011010";
			coefficients_accum_current(6) <=  "000000000000010100000111101000";
			coefficients_accum_current(7) <=  "000000000011010110101011110101";
			coefficients_accum_current(8) <=  "010000000000001010100001100111";
			coefficients_accum_current(9) <=  "111000000011101011010010010011";
			coefficients_accum_current(10) <= "000000000000011001111011110111";
			coefficients_accum_current(11) <= "111111100111110110101111110010";
			coefficients_accum_current(12) <= "111111110100010010110000001001";
			coefficients_accum_current(13) <= "111111110100100001111100011000";
			coefficients_accum_current(14) <= "111111110111111110000001010111";
			coefficients_accum_current(15) <= "000000011000000001100001111010";
			Rx_current(0) <=  "11010111";
			Rx_current(1) <=  "11110000";
			Rx_current(2) <=  "00011101";
			Rx_current(3) <=  "11110111";
			Rx_current(4) <=  "00101110";
			Rx_current(5) <=  "00011100";
			Rx_current(6) <=  "11111000";
			Rx_current(7) <=  "00110011";
			Rx_current(8) <=  "00101000";
			Rx_current(9) <=  "00010000";
			Rx_current(10) <= "11011101";
			Rx_current(11) <= "11111000";
			Rx_current(12) <= "00110000";
			Rx_current(13) <= "00011100";
			Rx_current(14) <= "11111000";
			Rx_current(15) <= "00110000";
			--count <= (others => '0');
			alpha <= '1';  
		elsif rising_edge(clk) then	
			for i in 0 to N-1 loop
				Rx_current(i) <= Rx_next(i);
				coefficients_accum_current(i) <= coefficients_accum_next(i);
			end loop;
			alpha <= '0';
			--count <= count_next;
		end if; 
	end process;
	
	-- -:-:-:-:-:-:-:-:-:-:-:- --
	-- COMBINATIONAL PROCESSES --
	-- -:-:-:-:-:-:-:-:-:-:-:- --
	
	-- Delayline -- 
	SHIFT : process (Rx_current, Rx) 
		
	begin    
		Rx_next(0) <= Rx;
		for i in 1 to N-1 loop
			Rx_next(i) <= Rx_current(i-1);
		end loop;
	end process;
	
	-- Multiplication of DataBits * CoeffcientBits, yiedling DataBits+CoefficientBits --  
	PRODUCT : process (coefficients_accum_current, Rx_current)
		
	begin
		for i in 0 to N-1 loop
			product_out(i) <= Rx_current(i)  * coefficients_accum_current(i)(2*DataBits+CoefficientsBits+AccuBits-1 downto 2*DataBits+CoefficientsBits+AccuBits-CoefficientsBits);
		end loop;
	end process;  
	
	-- Accumulator, adding up all N taps achieving sqrt(N) additional bits -> AccuBits --  
	SUM : process (product_out) 
		variable CumulativeSum : std_logic_vector (DataBits+CoefficientsBits+AccuBits-1 downto 0);
	begin
		CumulativeSum := (others => '0');	
		for i in 0 to N-1 loop
			CumulativeSum := product_out(i) + CumulativeSum; 
		end loop;
		
		--#-#-# slice: if pos = 1 and if neg =-1 coded as 0, therfore it is the negation of the sign #-#-#-       
		dataout <= not(CumulativeSum(DataBits+CoefficientsBits+AccuBits-1));
		
		dataout_next <= CumulativeSum;    
	end process;
	
	--  
	
	COEFFICIENTS_ADJUSTMENT : process (alpha, dataout_next, Rx_current, coefficients_accum_current)
		
		variable Error : std_logic_vector (DataBits+CoefficientsBits+AccuBits-1 downto 0);
		variable dataout_next_aux : std_logic_vector (DataBits+CoefficientsBits+AccuBits-1 downto 0);
		--variable Tx_current_aux : std_logic;
		variable Rx_current_aux : t_DelayLine;
		variable coefficients_accum_aux : t_Coefficient_accum;
		variable alpha_Error_Rx : std_logic_vector(2*DataBits+CoefficientsBits+AccuBits-1 downto 0);
		variable intermediate : std_logic_vector(2*DataBits+CoefficientsBits+AccuBits-1 downto 0);
		variable sign : std_logic;
		variable sub: std_logic_vector(DataBits+CoefficientsBits+AccuBits-1 downto 0);
		
	begin
		for i in 0 to N-1 loop
			coefficients_accum_aux(i):= coefficients_accum_current(i);
			Rx_current_aux(i) := Rx_current(i);
		end loop;
		
		dataout_next_aux:= dataout_next;
		
		-- calculating the slicer error
		sub := not(dataout_next_aux) + CONV_STD_LOGIC_VECTOR(1,DataBits+CoefficientsBits+AccuBits);
		if (dataout_next_aux(DataBits+CoefficientsBits+AccuBits-1) = '0') then
			Error :=  bits22_1 + sub;
		else
			Error :=  bits22_neg1 + sub;
		end if;
		
		-- coeffcient update process --
		for i in 0 to N-1 loop
			intermediate := Error*Rx_current_aux(i); -- s[10,-18] = s[7,-13] * s[1,-5]
			sign := intermediate(2*DataBits+CoefficientsBits+AccuBits-1);
			
			-- deviding by alpha by right shifting 
			if (alpha = '1') then
				-- alpha 2^12 ==> s[-1,-40]
				alpha_Error_Rx := sign&sign & intermediate(2*DataBits+CoefficientsBits+AccuBits-1 downto 2);   
				-- updating the coeffcients; s[0,-18] + s[-1,-40]
				
			else 
				-- alpha 2^16 ==> s[-5,-44]
				alpha_Error_Rx := sign&sign&sign&sign&sign&sign & intermediate(2*DataBits+CoefficientsBits+AccuBits-1 downto 6);
				-- updating the coeffcients; s[0,-18] + s[-5,-44]
				
			end if;
			coefficients_accum_aux(i) := coefficients_accum_aux(i) + alpha_Error_Rx;
		end loop;     
		
		for i in 0 to N-1 loop
			coefficients_accum_next(i)<= coefficients_accum_aux(i);
		end loop;
		
		error_out <= Error;
	end process;       
end behavioral;