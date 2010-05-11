@Echo off
cd comandosXilinx
set pwd=%cd%
rem borramos los archivos que no hacen falta
call clearFiles
rem creamos el fichero prj con el nombre adecuado
rem call creaPrj %1%
rem después ejecutamos los procesos
call sintetizarXST.bat
cd %pwd%
%pwd:~0,2%
call translateNGDBUILD.bat
cd %pwd%
%pwd:~0,2%
call map.bat
cd %pwd%
%pwd:~0,2%
call par.bat
cd %pwd%
%pwd:~0,2%
call bitgen.bat
cd %pwd%
%pwd:~0,2%
rem exit