inherit autotools texinfo update-alternatives gettext

LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://GPL.txt;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = " \
	http://www.apsis.ch/pound/Pound-${PV}.tgz \
	file://pound.cfg \
	file://mycert.pem \
"

SRC_URI[md5sum] = "ec8298aa3e4aee3ffbecdc0639d7f14a"
SRC_URI[sha256sum] = "cdfbf5a7e8dc8fbbe0d6c1e83cd3bd3f2472160aac65684bb01ef661c626a8e4"

S = "${WORKDIR}/Pound-${PV}"

DEPENDS = "kernel-devsrc linux-adi openssl10 openssl10-native"

TARGET_CFLAGS += " -I${STAGING_KERNEL_DIR}/include -DHAVE_SYSLOG_H=1 -DVERSION=${PV} -pthread -DNEED_STACK -DEMBED -D_REENTRANT -D_THREAD_SAFE -DUPER"

do_configure_append(){

	#Make sure we use OpenSSL 1.0.x to build these, otherwise compilation errors will be thrown
	#May need to modify OPENSSL path below to make this more portable

	cd ${B}
	OPENSSL=${BASE_WORKDIR}/${SDKMACHINE}-linux/openssl10-native/*/*/apps/openssl
	${OPENSSL} version > /home/nathan/opensslver
	${OPENSSL} dhparam -5 -C -noout 512 > ${B}/dh512.h
	${OPENSSL} dhparam -5 -C -noout 2048 > ${B}/dh2048.h
}

do_compile(){
	cd ${B}
	${CC} ${CFLAGS} -c -o ${B}/svc.o ${S}/svc.c -I${B} 
	${CC} ${CFLAGS} -c -o ${B}/http.o ${S}/http.c -I${B}
	${CC} ${CFLAGS} -c -o ${B}/config.o ${S}/config.c -I${B}
	${CC} ${CFLAGS} -c -o ${B}/pound.o ${S}/pound.c -I${B}
	${CC} ${CFLAGS} -c -o ${B}/poundctl.o ${S}/poundctl.c -I${B}
	${CC} ${LDFLAGS} -o ${B}/pound ${B}/pound.o ${B}/http.o ${B}/config.o ${B}/svc.o -l:libssl.a -l:libcrypto.a -lresolv -ldl -lm -lpthread
	${CC} ${LDFLAGS} -o ${B}/poundctl ${B}/poundctl.o -l:libssl.a -l:libcrypto.a -lresolv -ldl -lm -lpthread
}

do_install(){
	install -d ${D}/usr/bin
	install -m 755 ${B}/pound ${D}/usr/bin/
	install -m 755 ${B}/poundctl ${D}/usr/bin/

	install -d ${D}/etc/pound
	install -m 755 ${WORKDIR}/mycert.pem ${D}/etc/pound/mycert.pem

	install -d ${D}/usr/local/etc
	install -m 755 ${WORKDIR}/pound.cfg ${D}/usr/local/etc/pound.cfg
}

FILES_${PN} += " \
  /usr/local \
  /usr/local/etc \
  /usr/local/etc/pound.cfg \
  /etc/pound \
  /etc/pound/mycert.pem \
"
