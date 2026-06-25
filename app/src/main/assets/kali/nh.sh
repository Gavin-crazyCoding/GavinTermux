#!/data/data/com.termux/files/usr/bin/bash -e
cd ${HOME}
# 注意：我们保留 LD_PRELOAD，因为 Kali 内部的执行需要 libtermux-exec.so 来加上 linker64 绕过 W^X
if [ ! -f kali-arm64/root/.version ]; then
    touch kali-arm64/root/.version
fi
user="kali"
home="/home/$user"
start="$LINKER /usr/bin/sudo -u kali $LINKER /bin/bash"
if grep -q "kali" kali-arm64/etc/passwd; then
    KALIUSR="1";
else
    KALIUSR="0";
fi
if [[ $KALIUSR == "0" || ("$#" != "0" && ("$1" == "-r" || "$1" == "-R")) ]];then
    user="root"
    home="/$user"
    start="$LINKER /usr/bin/sudo -u kali $LINKER /bin/bash --login"
    if [[ "$#" != "0" && ("$1" == "-r" || "$1" == "-R") ]];then
        shift
    fi
fi
if [[ "aarch64" == "aarch64" || "aarch64" == "x86_64" ]]; then
    LINKER="/system/bin/linker64"
else
    LINKER="/system/bin/linker"
fi

export PROOT_NO_SECCOMP=1
if [ "$#" == "0" ];then
    exec $LINKER /data/data/com.termux/files/usr/bin/proot -q $LINKER -0 --link2symlink -r kali-arm64 -b /dev -b /proc -b /sdcard -b /data/data/com.termux/files/usr/tmp:/tmp -b kali-arm64$home:/dev/shm -w $home $LINKER /usr/bin/env -i HOME=$home PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin TERM=$TERM LANG=zh_CN.UTF-8 $start
else
    exec $LINKER /data/data/com.termux/files/usr/bin/proot -q $LINKER -0 --link2symlink -r kali-arm64 -b /dev -b /proc -b /sdcard -b /data/data/com.termux/files/usr/tmp:/tmp -b kali-arm64$home:/dev/shm -w $home $LINKER /usr/bin/env -i HOME=$home PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin TERM=$TERM LANG=zh_CN.UTF-8 $start -c "$@"
fi
