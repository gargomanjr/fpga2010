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

entity MiAnd is
    Port ( a : in  STD_LOGIC;
           b : in  STD_LOGIC;
			c : out STD_LOGIC);
end MiAnd;

architecture Behavioral of MiAnd is

begin

c <= a and b;

end Behavioral;
