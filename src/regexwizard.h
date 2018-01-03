/*
 * File: regexwizard.c header
 * Description: Contains functions that control the regular expression wizard
 */
#ifndef REGEXWIZARD_H
#define REGEXWIZARD_H

#include <gtk/gtk.h>

/*
 * Regular expression wizard enumerations and constants
 */
#define MAX_SUB_MATCHES 1 /* Control number of sub-matches regexec should return when searching text string */
enum { /* Enumerate the type selection combo box for expression wizard */
  REGWIZ_NONE=-1,
  REGWIZ_DONT_KNOW,
  REGWIZ_ANY_CHAR,
  REGWIZ_THE_CHAR,
  REGWIZ_ANY_ONE_CHAR,
  REGWIZ_ANY_CHAR_EXCEPT,
  REGWIZ_THE_PHRASE,
  REGWIZ_SPACE,
  REGWIZ_ANY_NUMERIC,
  REGWIZ_ANY_TEXT,
  REGWIZ_START_TYPE,
  REGWIZ_END_TYPE
};
enum { /* Enumerate the repeat selection combo box for expression wizard */
  REGWIZ_REPEAT_ONCE=0,
  REGWIZ_REPEAT_ONE_PLUS,
  REGWIZ_REPEAT_ZERO_PLUS,
  REGWIZ_REPEAT_ZERO_ONE
};
enum { /* Enumerate the expression wizard type */
    FILE_REGEX_TYPE=1,
    CONTEXT_REGEX_TYPE
};
enum { /* Enumerate the mid table columns for expression wizard */
   REGEX_TYPE_COLUMN,
   REGEX_ENTRY_COLUMN,
   REGEX_REPEAT_COLUMN,
   REGEX_TYPE_INT_COLUMN,
   REGEX_REPEAT_INT_COLUMN,
   REGEX_N_COLUMNS
};
enum { /* Internal enumeration for convertRegex2 function*/
  INC_RIGHT_BRKT = 0x1,
  INC_CARET      = 0x2,
  INC_MINUS      = 0x4
};

/* Regex Wizard control functions */
void refreshTestResults(GtkWidget *widget);
void updateTypeChangeEntry(GtkComboBox *comboBox, GtkWidget *entry, GtkWidget *repeat);
void updateRegExWizard(GtkWidget *widget);
gchar *makeInterimRegex(GtkComboBox *type, GtkEntry *entry, GtkComboBox *repeat, gint position);
gchar *makeInterimRegex2(GtkWidget *widget, gint type, const gchar *pEntry, gint repeatc, gint position);
GString *convertRegex(const gchar *input, gboolean change);
GString *convertRegex2(const gchar *input, gboolean change);
void appendTableRow(GtkWidget *widget, gint num, ...);

/* Internal macros */
#define DEFAULT_GSTRING_SIZE 10 /* Default size for newly allocated GString types */

#endif /* REGEXWIZARD_H */
