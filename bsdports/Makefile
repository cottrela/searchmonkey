# New ports collection makefile for:	xsearch
# Date created:		26 August 2006
# Whom:			Andrew Pantyukhin <infofarmer@FreeBSD.org>
#
# $FreeBSD: /repoman/r/pcvs/ports/sysutils/xsearch/Makefile,v 1.1 2006/08/26 13:20:28 sat Exp $
#

PORTNAME=	xsearch
PORTVERSION=	0.4.1
CATEGORIES=	sysutils
MASTER_SITES=	SF

MAINTAINER=	cottrela@users.sourceforge.net
COMMENT=	Search using regexes for file names and text

USE_GNOME=	gtk20
USE_GCC=	3.3+
USE_GETTEXT=	yes
GNU_CONFIGURE=	yes
CONFIGURE_ARGS=	--with-libintl-prefix=${LOCALBASE}
PLIST_FILES=	bin/xsearch

.include <bsd.port.mk>
