md %temp%\tempvhdlprj
copy ..\IOSerie\Circuito_FPGA.prj %temp%\tempvhdlprj
copy ..\IOSerie\*.vhd %temp%\tempvhdlprj
copy ..\IOSerie\ensayo.ucf %temp%\tempvhdlprj
del /Q ..\IOSerie\.
copy %temp%\tempvhdlprj\ ..\IOSerie 
del /Q %temp%\tempvhdlprj\. 
rd %temp%\tempvhdlprj