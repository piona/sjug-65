@echo off
:loop
if exist javacard.eeprom (
cref -i javacard.eeprom -o javacard.eeprom
) else (
cref -o javacard.eeprom
)
goto loop