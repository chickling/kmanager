#!/bin/sh

for arg in "$@"
do
	key=${arg%%=*}
	value=${arg#*=}
	case $key in
	--jvmopt)
		jvmopt="$value"
		;;
	*)
		option="$option $value"
		;;
	esac
done

java $jvmopt -jar ChilklingKManager.jar $option
