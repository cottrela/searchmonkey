## Process this file with automake to produce Makefile.in

INCLUDES = \
	-DPACKAGE_DATA_DIR=\""$(datadir)"\" \
	-DPACKAGE_LOCALE_DIR=\""$(prefix)/$(DATADIRNAME)/locale"\" \
	@PACKAGE_CFLAGS@

bin_PROGRAMS = searchmonkey

searchmonkey_SOURCES = \
	main.c \
	support.c support.h \
	interface.c interface.h \
	callbacks.c callbacks.h \
	search.c search.h \
	savestate.c savestate.h \
	regexwizard.c regexwizard.h \
	systemio.c systemio.h \
	misc.c misc.h \
	lgpl.h

searchmonkey_LDADD = @PACKAGE_LIBS@ $(INTLLIBS)

