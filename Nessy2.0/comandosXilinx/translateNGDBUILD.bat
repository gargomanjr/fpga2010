set ioserie=%cd%\..\IOSerie
set homexilinx=%1
%homexilinx:~0,2%

cd %homexilinx%\ISE\lib\nt

set XILINX=%homexilinx%\ISE\

%1\ISE\bin\nt\unwrapped\ngdbuild.exe -uc %ioserie%\constraints.ucf -intstyle xflow -dd _ngo -nt timestamp -p xc2vp30-ff896-7 %ioserie%\Circuito_FPGA.ngc %ioserie%\Circuito_FPGA.ngd
%homexilinx%\ISE\bin\nt\unwrapped\ngdbuild.exe -uc %ioserie%\constraints.ucf -intstyle xflow -dd _ngo -nt timestamp -p xc2vp30-ff896-7 %ioserie%\Circuito_FPGA.ngc %ioserie%\Circuito_FPGA.ngd
