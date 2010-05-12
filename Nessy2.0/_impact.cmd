setMode -ss
setMode -sm
setMode -sm
setMode -hw140
setMode -spi
setMode -acecf
setMode -acempm
setMode -pff
setMode -bs
setMode -bs
setCable -port auto
Identify 
identifyMPM 
addDevice -p 3 -file "D:\PFC\Nessy2.0\test\bits\FFE.bit"
deleteDevice -position 4
Program -p 3 -defaultVersion 0 
quit
