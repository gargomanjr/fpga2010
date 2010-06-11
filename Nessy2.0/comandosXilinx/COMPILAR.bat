@Echo off
cd comandosXilinx
set pwd=%cd%
rem borramos los archivos que no hacen falta
call clearFiles
rem despu�s ejecutamos los procesos
call sintetizarXST.bat %1%
cd %pwd%
%pwd:~0,2%
call translateNGDBUILD.bat %1%
cd %pwd%
%pwd:~0,2%
call map.bat %1%
cd %pwd%
%pwd:~0,2%
call par.bat %1%
cd %pwd%
%pwd:~0,2%
call bitgen.bat %1%
cd %pwd%
%pwd:~0,2%
rem copiamos el fichero .BIT generado en el directorio que introduce el usuario
copy ..\IOSerie\circuito_fpga.bit %2%
rem borramos los archivos que no hacen falta
call clearFiles
rem exit
