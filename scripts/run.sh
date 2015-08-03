#!/bin/sh

if [ -e "/dev/ttyAMA0" ] || [ -e "/dev/ttyO0" ]
then
  for port in `find /dev -name 'tty*'`
  do
    PORTS="$PORTS:$port"
  done
  JAVA_OPT="-Djava.library.path=/usr/lib/jni -Dgnu.io.rxtx.SerialPorts=$PORTS"
fi


java $JAVA_OPT -jar target/pololu-1.0-SNAPSHOT.jar "/dev/ttyACM0"