## Process this file with automake to produce Makefile.in

SUBDIRS = src po

EXTRA_DIST = \
	autogen.sh \
	search.glade \
	search.gladep \
	rpmbuild/* \
	bsdports/*

install-data-local:
	@$(NORMAL_INSTALL)
	if test -d $(srcdir)/pixmaps; then \
	  $(mkinstalldirs) $(DESTDIR)$(datadir)/pixmaps/$(PACKAGE); \
	  for pixmap in $(srcdir)/pixmaps/*; do \
	    if test -f $$pixmap; then \
	      $(INSTALL_DATA) $$pixmap $(DESTDIR)$(datadir)/pixmaps/$(PACKAGE); \
	    fi \
	  done; \
	  ln -sf $(DESTDIR)$(datadir)/pixmaps/$(PACKAGE)/searchmonkey-48x48.png $(DESTDIR)$(datadir)/pixmaps/searchmonkey.png; \
	fi

dist-hook:
	if test -d pixmaps; then \
	  mkdir $(distdir)/pixmaps; \
	  for pixmap in pixmaps/*; do \
	    if test -f $$pixmap; then \
	      cp -p $$pixmap $(distdir)/pixmaps; \
	    fi \
	  done \
	fi

# Special commands for creating Gnome menu item - does not add dependancy
install-gnome: install
	if test -f $(srcdir)/rpmbuild/@PACKAGE@.desktop; then \
	  if test -d $(DESTDIR)$(datadir)/applications; then \
 	    $(INSTALL_DATA) $(srcdir)/rpmbuild/@PACKAGE@.desktop $(DESTDIR)$(datadir)/applications; \
	  fi \
	fi

uninstall-gnome: uninstall
	if test -f $(DESTDIR)$(datadir)/applications/@PACKAGE@.desktop; then \
	  rm -f $(DESTDIR)$(datadir)/applications/@PACKAGE@.desktop; \
	fi
