set ioserie=%cd%\..\IOSerie
%1%\ISE\bin\nt\map -intstyle xflow -p xc2vp30-ff896-7 -cm area -pr off -k 4 -c 100 -tx off -o %ioserie%/Circuito_FPGA_map.ncd %ioserie%/Circuito_FPGA.ngd %ioserie%/Circuito_FPGA.pcf