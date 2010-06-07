@Echo off
cd comandosXilinx
set pwd=%cd%
rem borramos los archivos que no hacen falta
call clearFiles
rem después ejecutamos los procesos
call sintetizarXST.bat %1%
cd %pwd%
%pwd:~0,2%
call translateNGDBUILD.bat %1
cd %pwd%
%pwd:~0,2%
call map.bat %1
cd %pwd%
%pwd:~0,2%
call par.bat %1
cd %pwd%
%pwd:~0,2%
call bitgen.bat %1
cd %pwd%
%pwd:~0,2%
rem exit