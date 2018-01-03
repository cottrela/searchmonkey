Summary: Graphical equivalent of find + grep
Name: searchmonkey
Version: 0.7.2
Release: 1%{?dist}
License: LGPL
Group: Applications/File
Source0: http://prdownloads.sourceforge.net/searchmonkey/searchmonkey-%{version}.tar.gz
URL: http://searchmonkey.sourceforge.net/
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root
BuildRequires: pkgconfig, gettext, gtk2-devel

%description
searchmonkey is a powerful, but fast GTK+ text search utility that allows
files and content to be searched using regular expressions.  It aims
to be the graphical equivalent of find + grep, but with the simplicity
of the Beagle search engine.

%prep
%setup -q

%build
%configure
make

%install
rm -fr %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -fr %{buildroot}

%files
%doc README INSTALL COPYING
%{_bindir}/searchmonkey
%{_datadir}/share/pixmaps/searchmonkey-32x32.png
# Only required for GNOME
%{_datadir}/share/applications/searchmonkey.desktop
