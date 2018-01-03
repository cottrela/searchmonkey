/*
 * File: misc.c header
 * Description: Contains helpere functions, and everything that doesn't fit elsewhere
 */
#ifndef MISC_H
#define MISC_H

extern GtkWidget *mainWindowApp; /* Holds pointer to the main searchmonkey GUI. Declared in main.c */

gchar *getFullFileName(GtkTreeView *treeView, gint columnNumber);
void initComboBox2(GtkWidget *comboBox);
void initComboBox(GtkWidget *comboBox);
void clearComboBox(GtkWidget *comboBox);
gboolean addUniqueRow(GtkWidget *comboBox, const gchar *entry);
gint g_strlen(const gchar *string);
void copySettings(GtkWidget *widget, gboolean expertMode);
void setExpertSearchMode (GtkWidget *widget, gboolean expertMode);
gboolean getExpertSearchMode (GtkWidget *widget);
void setResultsViewHorizontal(GtkWidget *widget, gboolean horizontal);
gboolean getResultsViewHorizontal (GtkWidget *widget);
void changeModel(GtkWidget *widget, const gchar *from, const gchar *to);
gchar * getDate(const gchar *curDate);
gboolean test_regexp(gchar *regexp, guint flags, gchar *error);
gboolean validate_folder(const gchar *folderName);
#endif /* MISC_H */
