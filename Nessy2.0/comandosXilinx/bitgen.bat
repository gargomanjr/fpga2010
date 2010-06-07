set ioserie=%cd%\..\IOSerie
set homexilinx=%1
%homexilinx:~0,2%

cd %homexilinx%\ISE\lib\nt

SET XILINX=%homexilinx%\ISE

%homexilinx%\ISE\bin\nt\unwrapped\bitgen.exe -intstyle ise -w -g ActiveReconfig:Yes -g Persist:yes -g DebugBitstream:No -g Binary:no -g CRC:Enable -g ConfigRate:4 -g CclkPin:PullUp -g M0Pin:PullUp -g M1Pin:PullUp -g M2Pin:PullUp -g ProgPin:PullUp -g DonePin:PullUp -g PowerdownPin:PullUp -g TckPin:PullUp -g TdiPin:PullUp -g TdoPin:PullNone -g TmsPin:PullUp -g UnusedPin:PullDown -g UserID:0xFFFFFFFF -g DCMShutdown:Disable -g DisableBandgap:No -g DCIUpdateMode:AsRequired -g StartUpClk:JtagClk -g DONE_cycle:4 -g GTS_cycle:5 -g GWE_cycle:6 -g LCK_cycle:NoWait -g Match_cycle:Auto -g Security:None -g DonePipe:No -g DriveDone:No -g Encrypt:No %ioserie%/Circuito_FPGA.ncd