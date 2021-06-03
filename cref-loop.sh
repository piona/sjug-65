#!/bin/bash
while true
do
  if [[ -f javacard.eeprom ]]; then
    cref -i javacard.eeprom -o javacard.eeprom
  else
    cref -o javacard.eeprom
  fi
done
