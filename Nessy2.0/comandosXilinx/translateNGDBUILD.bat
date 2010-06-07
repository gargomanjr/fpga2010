set ioserie=%cd%\..\IOSerie

C:

cd C:\Xilinx10.1\ISE\lib\nt

set XILINX=C:\Xilinx10.1\ISE\

C:\Xilinx10.1\ISE\bin\nt\unwrapped\ngdbuild.exe -uc %ioserie%\constraints.ucf -intstyle xflow -dd _ngo -nt timestamp -p xc2vp30-ff896-7 %ioserie%\Circuito_FPGA.ngc %ioserie%\Circuito_FPGA.ngd