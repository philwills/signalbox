#!/bin/bash

SBT_BOOT_DIR=$HOME/.sbt/boot/

if [ ! -d "$SBT_BOOT_DIR" ]; then
  mkdir -p $SBT_BOOT_DIR
fi

# echo "extra params: $SBT_EXTRA_PARAMS"

java -Dfile.encoding=UTF8 -Xmx1536M -XX:+CMSClassUnloadingEnabled -XX:+UseCompressedOops -XX:MaxPermSize=512m \
	-Dhttp.proxyHost=devscreen.gudev.gnl -Dhttp.proxyPort=3128 \
	$SBT_EXTRA_PARAMS \
	-Dsbt.boot.directory=$SBT_BOOT_DIR \
	-jar `dirname $0`/sbt-launch-0.11.2.jar "$@"
