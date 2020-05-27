#!/bin/bash

NAME=${1// /\\ }
DOMAIN=$2
echo "NAME($NAME)"

SED=$(which sed)
$SED -i .ip "s/\%{DOMAIN}/$DOMAIN/" app/src/main/java/fi/casa/webapp/MainActivity.java
rv_ma=$?
#echo $?

#if [[ $? -eq 0 ]]; then
#    rm app/src/main/java/fi/casa/webapp/MainActivity.java.ip
#fi

$SED -i .ip "s/\%{APP_NAME}/$NAME/" app/src/main/res/values/strings.xml
rv_str=$?

if [[ $rv_str -eq 0 && rv_ma -eq 0 ]]; then
    rm app/src/main/java/fi/casa/webapp/MainActivity.java.ip
    rm app/src/main/res/values/strings.xml.ip
else
    cp app/src/main/java/fi/casa/webapp/MainActivity.java.ip app/src/main/java/fi/casa/webapp/MainActivity.java
    cp app/src/main/res/values/strings.xml.ip app/src/main/res/values/strings.xml
fi


exit 0
