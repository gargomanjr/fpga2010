echo run -ifn %cd%\..\IOSerie\Circuito_FPGA.prj -ifmt mixed -top Circuito_FPGA -ofn %cd%\..\IOSerie\Circuito_FPGA.ngc -ofmt NGC -p xc2vp30-7ff896 -opt_mode Speed -opt_level 1 > sint.txt
echo %1\ISE\bin\nt\xst.exe
%1\ISE\bin\nt\xst.exe -ifn "%cd%\sint.txt"