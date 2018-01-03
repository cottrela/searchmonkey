/*
 * File: savestate.c header
 * Description: Contains config save/restore specific functions so that widgets can keep their
 *              settings betweeen saves.
 *
 *
 *
 */
#ifndef SAVESTATE_H
#define SAVESTATE_H

/* Externals */
extern gchar *gConfigFile; /* Holds pointer to the configuration file location. Declared in main.c*/

/* Local macros */
#define CONFIG_DISABLE_SAVE_CONFIG_STRING "CONFIG_DISABLE_SAVE_CONFIG" /* Used by config->restart to check for reset */
#define CONFIG_DISABLE_SAVE_CONFIG 1 /* Used by config->restart to check for reset */
#define MASTER_OPTIONS_DATA "options1" /* Storage for the Phase One data inside the g_object */
#define CURRENT_VERSION "0.8.1"  /*Current version of searchmonkey */

/* Keyfile interface commands */
void createGKeyFile(GObject *object, const gchar *dataName);
void destroyGKeyFile(gpointer data);
GKeyFile *getGKeyFile(GtkWidget *widget);
void storeGKeyFile(GKeyFile *keyString);
void setConfigFileLocation (GtkWidget *widget);

/* Top level save/restore commands */
void realize_searchmonkeyWindow (GtkWidget *widget);
void unrealize_searchmonkeyWindow (GtkWidget *widget);
void realize_configDialog (GtkWidget *widget);
void unrealize_configDialog (GtkWidget *widget);
void initTreeView(GtkWidget *widget);
void initTextView(GtkWidget *widget);
void realize_searchNotebook (GtkWidget *widget);
void unrealize_searchNotebook (GtkWidget *widget);
void realize_menubar (GtkWidget *widget);
void unrealize_menubar (GtkWidget *widget);
void realize_statusbar (GtkWidget *widget); /* Auto destroyed */

/* Generic save/restore commands */
void realizeFileDialog(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeFileDialog(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeMenuCheck(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeMenuCheck(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeString(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeString(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
gboolean realizeFileButton(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeFileButton(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeToggle(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeToggle(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeNotebook(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeNotebook(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeSpin(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeSpin(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);

/* Specific save/restore commands (for more complex widgets) */
void realizeWindow(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeWindow(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeTextviewFont(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeTextviewFont(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeTextviewHighlight(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeTextviewHighlight(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeComboModel(GtkListStore *store, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeComboBox(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeComboBox2(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeComboModel(GtkListStore *store, GKeyFile *keyString, const gchar *group, const gchar *name, gint activeRow);
void unrealizeComboBox(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void unrealizeComboBox2(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name);
void realizeTreeview(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth);
void unrealizeTreeview(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth);
void realizeTreeviewColumns (GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth);
void unrealizeTreeviewColumns (GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth);

/* Tree view handlers (Right click + left click + sort columns) */
void undo_popup_menu(GtkWidget *attach_widget, GtkMenu *menu);
void do_popup_menu (GtkWidget *widget, GdkEventButton *event);
void detachMenu(GtkMenuItem *menuitem);
void tree_selection_changed_cb (GtkTreeSelection *selection, gpointer data);
gchar *generateContentOptionsString(searchControl *mSearchControl); /* searchControl*/
void on_column_click (GtkTreeViewColumn *treeviewcolumn, gpointer user_data);
void columnClick(GtkWidget *widget, gint column);
void getSortMenuItem(gint *columnId, GtkSortType *sortOrder);
void setSortMenuItem(gint columnId, GtkSortType order);
void setExtendedRegexMode(GtkWidget *widget, gboolean extended);
gboolean getExtendedRegexMode(GtkWidget *widget);

#endif /* SAVESTATE_H */
