rem para crear el prj correctamente
del /Q ..\IOSerie\Circuito_FPGA.prj
echo vhdl work "Tx_serie.vhd" >> ..\IOSerie\Circuito_FPGA.prj
echo vhdl work "Rx_Serie.vhd" >> ..\IOSerie\Circuito_FPGA.prj
echo vhdl work "%1" >> ..\IOSerie\Circuito_FPGA.prj
echo vhdl work "Circuito_FPGA.vhd" >> ..\IOSerie\Circuito_FPGA.prj