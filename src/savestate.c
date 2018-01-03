/*
 * File: savestate.c
 * Description: Contains config save/restore specific functions so that widgets can keep their
 *              settings betweeen saves.
 * Todo: Make more of these files internal only, by merging the commands (where possible) into
 *       very specific top level save/restore commands just called once.
 *       Ideally this file should have only around 10 globally called functions - all the rest
 *       will become internal to this file.
 *       gConfigFile variable should become static to this file, and have functions modify its
 *       value - i.e. setConfigLocation, getConfigLocation, saveConfig, loadConfig, etc
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <gtk/gtk.h>
#include <glib.h>
#include <glib/gstdio.h> /* g_fopen, etc */

#include "interface.h" /* glade requirement */
#include "support.h" /* glade requirement */
#include "search.h" /* Add in column definitions */
#include "misc.h" /* Everything else */
#include "savestate.h"

/* Externals */
extern GtkWidget *mainWindowApp; /* Holds pointer to the main searchmonkey GUI. Declared in main.c */
extern GStaticMutex mutex_Data; /* Created in search.c to control access to search results data. */
extern GStaticMutex mutex_Control; /* Created in search.c to control access to search controls. */

/**************************************************************
 *    Keyfile interface commands
 **************************************************************/

/*
 * Creates a new config file
 */
void createNewKeyFile(GKeyFile *keyString)
{
    gchar* tmpStr = g_strdup_printf(_(" %s settings auto-generated - Do not edit!"), PACKAGE);
    g_key_file_set_comment (keyString, NULL, NULL, tmpStr, NULL);
    g_key_file_set_string(keyString, "version-info", "version", CURRENT_VERSION);
    g_free(tmpStr);
    return;
}
/*
 * Attempts to load the searchmonkey config.ini file.
 * If invalid, or non-existant, creates new config file.
 */
void createGKeyFile(GObject *object, const gchar *dataName)
{
  GKeyFile *keyString;
  keyString = g_key_file_new ();
  gchar *tmpStr;

  if (!g_key_file_load_from_file (keyString,
                                  gConfigFile,
                                  G_KEY_FILE_KEEP_COMMENTS,
                                  NULL)) {
    createNewKeyFile(keyString);
  } else {
    gchar* version = g_key_file_get_string(keyString, "version-info", "version", NULL);
    gint versionCmp = 1;

    if (version != NULL) {
      versionCmp = strcmp(version, CURRENT_VERSION);
    }
    
    if (versionCmp != 0) {
      GtkWidget* yesNoDialog;
      
      yesNoDialog = gtk_message_dialog_new(GTK_WINDOW(mainWindowApp), 
	                      (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT), 
			      GTK_MESSAGE_WARNING, GTK_BUTTONS_YES_NO, 
			      _("Configuration files are for a different version of Searchmonkey. This may cause errors. Delete old configuration files?"));
      
      if (gtk_dialog_run(GTK_DIALOG(yesNoDialog)) != GTK_RESPONSE_YES) {
	/*This is to prevent checking the next time */
	g_key_file_set_string(keyString, "version-info", "version", CURRENT_VERSION); 
      } else {
	g_key_file_free(keyString);
	g_remove(gConfigFile);
	keyString = g_key_file_new();
	createNewKeyFile(keyString);
      }
      gtk_widget_destroy(yesNoDialog);
    }
    g_free(version);
  }
  g_object_set_data_full(object, dataName, keyString, destroyGKeyFile);
}


/*
 * Saves config file (config.ini) to disk, and frees associated memory.
 * Automatically called when save data is destroyed (i.e. user closed searchmonkey).
 */
void destroyGKeyFile(gpointer data)
{
  GKeyFile *keyString = data;

  if (GPOINTER_TO_INT(g_object_get_data(G_OBJECT(mainWindowApp), CONFIG_DISABLE_SAVE_CONFIG_STRING)) != CONFIG_DISABLE_SAVE_CONFIG) {
    storeGKeyFile(keyString);
  }
  g_key_file_free(keyString);
  if (gConfigFile != NULL) {
    g_free(gConfigFile);
  }
}


/*
 * Internal helper: returns reference to current config.ini file (stored in RAM).
 */
GKeyFile *getGKeyFile(GtkWidget *widget)
{
  GObject *window1 = G_OBJECT(lookup_widget(GTK_WIDGET(widget), "window1"));
  GKeyFile *keyString = g_object_get_data(window1, MASTER_OPTIONS_DATA);
  return keyString;
}


/*
 * Internal helper: saves the config.ini file to disk.
 * If configured to, user-prompt is created so that automatic saving of state is an option.
 * If storage fails, quietly informs user via the command line - not by pop-up, as this could
 * cause an annoyance to users.
 */
void storeGKeyFile(GKeyFile *keyString)
{
  gsize length;
  gchar *outText;
  GError *error = NULL;
  gchar *folderName;
  GtkWidget *okCancelDialog;
  
  if (g_key_file_has_key(keyString, "configuration", "configPromptSave", NULL) &&
      (g_key_file_get_boolean(keyString, "configuration", "configPromptSave", NULL))) {
    okCancelDialog = gtk_message_dialog_new(GTK_WINDOW(mainWindowApp), (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                            GTK_MESSAGE_WARNING, GTK_BUTTONS_OK_CANCEL,
                                            _("About to overwrite save configuration. Do you want to proceed?"));
    if (gtk_dialog_run(GTK_DIALOG(okCancelDialog)) != GTK_RESPONSE_OK) {
      gtk_widget_destroy(okCancelDialog);
      return;
    }
    gtk_widget_destroy(okCancelDialog);
  }
    
  /* Write the configuration file to disk */
  folderName = g_path_get_dirname(gConfigFile);
  outText = g_key_file_to_data (keyString, &length, NULL);
  if (!g_file_set_contents2 (gConfigFile, outText, length, NULL)) {

    /* Unable to immediately write to file, so attempt to recreate folders */
    mkFullDir(folderName, S_IRWXU);
    if (!g_file_set_contents2 (gConfigFile, outText, length, &error)) { 
      g_print(_("Error saving %s: %s\n"), gConfigFile, error->message);
      g_error_free(error);
      error = NULL;
    }
  }
  
  g_free(outText);
  g_free(folderName);
}


/*
 * Callback/Internal helper: updates configuration panel with config file's location.
 * If no config file, or any error detected than [missing] is displayed instead.
 */
void setConfigFileLocation (GtkWidget *widget)
{
  gchar *fullPath;

  if (g_file_test(gConfigFile, G_FILE_TEST_EXISTS)) {
    gtk_entry_set_text(GTK_ENTRY(lookup_widget(widget, "configFileLocation")), gConfigFile);
  } else {
	fullPath = g_strconcat(gConfigFile, _(" [missing]"), NULL);
    gtk_entry_set_text(GTK_ENTRY(lookup_widget(widget, "configFileLocation")), fullPath);
    g_free(fullPath);
  }
}


/**************************************************************
 *    Top level save/restore commands
 **************************************************************/
/*
 * Callback helper: master realize function for searchmonkey main screen
 */
void realize_searchmonkeyWindow (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);
  GtkComboBox* tmpCombo;
  GtkTreeIter iter;

  /* Global stuff */
  realizeWindow(widget, keyString, "application", "window1"); /* restore window size */

  /* Search stuff */
  initComboBox2(lookup_widget(widget, "fileName"));
  initComboBox(lookup_widget(widget, "containingText"));
  initComboBox(lookup_widget(widget, "lookIn"));
  initComboBox2(lookup_widget(widget, "fileName2"));
  initComboBox(lookup_widget(widget, "containingText2"));
  initComboBox(lookup_widget(widget, "lookIn2"));
  realize_searchNotebook(widget);

  tmpCombo = GTK_COMBO_BOX(lookup_widget(widget, "lookIn"));
  g_assert(tmpCombo != NULL);
  if (gtk_tree_model_get_iter_first(gtk_combo_box_get_model(tmpCombo), &iter) == FALSE) {
    addUniqueRow(GTK_WIDGET(tmpCombo), g_get_home_dir()); /* Set default look in folder */
  }
  
  tmpCombo = GTK_COMBO_BOX(lookup_widget(widget, "lookIn2"));
  g_assert(tmpCombo != NULL);
  if (gtk_tree_model_get_iter_first(gtk_combo_box_get_model(tmpCombo), &iter) == FALSE) {
    addUniqueRow(GTK_WIDGET(tmpCombo), g_get_home_dir()); /* Set default look in folder */
  }
  
  /* Results stuff */
  setResultsViewHorizontal(widget, TRUE); /* Default to horizontal */
  gboolean autoColumnWidth = ((!g_key_file_has_key (keyString, "configuration", "autosize_columns", NULL)) ||
                              (g_key_file_get_boolean (keyString, "configuration", "autosize_columns", NULL)));
  initTreeView(widget);
  realizeTreeview(widget, keyString, "history", "treeview", autoColumnWidth); /* restore column widths, search order */
  realizeTreeviewColumns(widget, keyString, "history", "treeview", autoColumnWidth); /* restore column widths, search order */
  initTextView(widget);
  realizeTextviewFont(widget, keyString, "menuOptions", "font");
  realizeTextviewHighlight(widget, keyString, "menuOptions", "highlight");

  /* Status bar */
  createStatusbarData(G_OBJECT(mainWindowApp), MASTER_STATUSBAR_DATA);
  realize_statusbar (widget);

  /* Menu stuff (do this last so that all view options get set) */
  realize_menubar(widget);
  gtk_widget_grab_focus (lookup_widget(widget, "playButton1"));
}


/*
 * Callback helper: master unrealize function for searchmonkey main screen
 */
void unrealize_searchmonkeyWindow (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);
  
  unrealizeWindow(widget, keyString, "application", "window1"); /* Save window size */

  /* Menu stuff */
  unrealize_menubar(widget);

  /* Search stuff */
  unrealize_searchNotebook(widget);
  
  /* Results stuff */
  gboolean autoColumnWidth = ((!g_key_file_has_key (keyString, "configuration", "autosize_columns", NULL)) ||
                              (g_key_file_get_boolean (keyString, "configuration", "autosize_columns", NULL)));
  unrealizeTreeview(widget, keyString, "history", "treeview", autoColumnWidth);
  unrealizeTreeviewColumns(widget, keyString, "history", "treeview", autoColumnWidth);

  unrealizeTextviewFont(widget, keyString, "menuOptions", "font");
  unrealizeTextviewHighlight(widget, keyString, "menuOptions", "highlight");
}


/*
 * Callback helper: retrieve all configuration settings from config.ini
 */
void realize_configDialog (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(mainWindowApp);
  GtkFileChooser *chooser;
  gchar *fullPath;

  /* Save current treeview column sizes immediately */
  unrealizeTreeviewColumns (GTK_WIDGET(mainWindowApp), keyString, "history", "treeview", FALSE);

  /* Restore global settings */
  realizeToggle (widget, keyString, "configuration", "configForceSingle");
  realizeToggle (widget, keyString, "configuration", "configExtendedRegex");
  realizeToggle (widget, keyString, "configuration", "configPromptSave");
  realizeToggle (widget, keyString, "configuration", "configPromptDelete");
  realizeToggle (widget, keyString, "configuration", "configMatchBinary");

  /* Restore store results settings */
  realizeString (widget, keyString, "configuration", "configResultEOL");
  realizeString (widget, keyString, "configuration", "configResultEOF");
  realizeString (widget, keyString, "configuration", "configResultDelimiter");
  
  /* Restore store results settings */
  realizeString (widget, keyString, "configuration", "configFileLocation");

  /* Restore system call settings */
  realizeString (widget, keyString, "configuration", "configTextEditor");
  realizeString (widget, keyString, "configuration", "configTextEditorAttributes");
  realizeString (widget, keyString, "configuration", "configFileExplorer");
  realizeString (widget, keyString, "configuration", "configFileExplorerAttributes");
  realizeString (widget, keyString, "configuration", "configWebBrowser");
  realizeString (widget, keyString, "configuration", "configWebBrowserAttributes");

  setConfigFileLocation (widget);
}


/*
 * Callback helper: store all configuration settings into config.ini
 */
void unrealize_configDialog (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(mainWindowApp);
  GtkFileChooser *chooser;

  /* Restore global settings */
  unrealizeToggle (widget, keyString, "configuration", "configForceSingle");
  unrealizeToggle (widget, keyString, "configuration", "configExtendedRegex");
  unrealizeToggle (widget, keyString, "configuration", "configPromptSave");
  unrealizeToggle (widget, keyString, "configuration", "configPromptDelete");
  unrealizeToggle (widget, keyString, "configuration", "configMatchBinary");

  /* Restore store results settings */
  unrealizeString (widget, keyString, "configuration", "configResultEOL");
  unrealizeString (widget, keyString, "configuration", "configResultEOF");
  unrealizeString (widget, keyString, "configuration", "configResultDelimiter");
  
  /* Restore system call settings */
  unrealizeString (widget, keyString, "configuration", "configTextEditor");
  unrealizeString (widget, keyString, "configuration", "configTextEditorAttributes");
  unrealizeString (widget, keyString, "configuration", "configFileExplorer");
  unrealizeString (widget, keyString, "configuration", "configFileExplorerAttributes");
  unrealizeString (widget, keyString, "configuration", "configWebBrowser");
  unrealizeString (widget, keyString, "configuration", "configWebBrowserAttributes");
}

/*
 * Callback helper: store all search settings into config.ini
 */
void realize_searchNotebook (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);

  /* Store basic Tab */
  realizeComboBox2(widget, keyString, "history", "fileName2");
  realizeComboBox(widget, keyString, "history", "containingText2");
  realizeToggle(widget, keyString, "history", "containingTextCheck2");
  realizeComboBox(widget, keyString, "history", "lookIn2");
  realizeToggle(widget, keyString, "history", "searchSubfoldersCheck2");
  realizeToggle(widget, keyString, "history", "ignoreHiddenFiles");

  /* Store advanced Tab */
  realizeComboBox2(widget, keyString, "history", "fileName");
  realizeToggle(widget, keyString, "history", "expertUserCheck");
  realizeComboBox(widget, keyString, "history", "containingText");
  realizeToggle(widget, keyString, "history", "containingTextCheck");
  realizeComboBox(widget, keyString, "history", "lookIn");
  realizeToggle(widget, keyString, "history", "searchSubfoldersCheck");
  realizeToggle(widget, keyString, "history", "moreThanCheck");
  realizeString(widget, keyString, "history", "moreThanEntry");
  realizeToggle(widget, keyString, "history", "lessThanCheck");
  realizeString(widget, keyString, "history", "lessThanEntry");
  realizeToggle(widget, keyString, "history", "afterCheck");
  realizeString(widget, keyString, "history", "afterEntry");
  realizeToggle(widget, keyString, "history", "beforeCheck");
  realizeString(widget, keyString, "history", "beforeEntry");
  realizeToggle(widget, keyString, "history", "folderDepthCheck");
  realizeSpin(widget, keyString, "history", "folderDepthSpin");

  /* Store Advanced Options Tab */
  realizeToggle(widget, keyString, "history", "notExpressionCheckFile");
  realizeToggle(widget, keyString, "history", "matchCaseCheckFile");
  realizeToggle(widget, keyString, "history", "ignoreHiddenFiles");
  realizeToggle(widget, keyString, "history", "dosExpressionRadioFile");
  realizeToggle(widget, keyString, "history", "regularExpressionRadioFile");
  realizeToggle(widget, keyString, "history", "singlePhaseCheckContents");
  realizeToggle(widget, keyString, "history", "matchCaseCheckContents");
  realizeToggle(widget, keyString, "history", "wildcardCheckContents");
  realizeToggle(widget, keyString, "history", "regularExpressionRadioContents");
  realizeToggle(widget, keyString, "history", "limitResultsCheckResults");
  realizeSpin(widget, keyString, "history", "maxHitsSpinResults");
  realizeToggle(widget, keyString, "history", "showLinesCheckResults");
  realizeSpin(widget, keyString, "history", "showLinesSpinResults");
  realizeToggle(widget, keyString, "history","followSymLinksCheck");
  realizeToggle(widget, keyString, "history", "limitContentsCheckResults");
  realizeSpin(widget, keyString, "history", "maxContentHitsSpinResults");

  /* Store notebook global settings */
  realizeNotebook(widget, keyString, "history", "searchNotebook");  
}



/*
 * Callback helper: store all search settings into config.ini
 */
void unrealize_searchNotebook (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);
  GtkComboBox * combo = GTK_COMBO_BOX(lookup_widget(widget, "fileName"));

  /* Store notebook global settings */
  unrealizeNotebook(widget, keyString, "history", "searchNotebook");
  
  /* Store basic Tab */
  unrealizeComboBox2(widget, keyString, "history", "fileName2");
  unrealizeComboBox(widget, keyString, "history", "containingText2");
  unrealizeToggle(widget, keyString, "history", "containingTextCheck2");
  unrealizeComboBox(widget, keyString, "history", "lookIn2");
  unrealizeToggle(widget, keyString, "history", "searchSubfoldersCheck2");
  unrealizeToggle(widget, keyString, "history", "ignoreHiddenFiles");


  /* Store advanced Tab */
  unrealizeComboBox2(widget, keyString, "history", "fileName");
  unrealizeToggle(widget, keyString, "history", "expertUserCheck");
  unrealizeComboBox(widget, keyString, "history", "containingText");
  unrealizeToggle(widget, keyString, "history", "containingTextCheck");
  unrealizeComboBox(widget, keyString, "history", "lookIn");
  unrealizeToggle(widget, keyString, "history", "searchSubfoldersCheck");
  unrealizeToggle(widget, keyString, "history", "moreThanCheck");
  unrealizeString(widget, keyString, "history", "moreThanEntry");
  unrealizeToggle(widget, keyString, "history", "lessThanCheck");
  unrealizeString(widget, keyString, "history", "lessThanEntry");
  unrealizeToggle(widget, keyString, "history", "afterCheck");
  unrealizeString(widget, keyString, "history", "afterEntry");
  unrealizeToggle(widget, keyString, "history", "beforeCheck");
  unrealizeString(widget, keyString, "history", "beforeEntry");
  unrealizeToggle(widget, keyString, "history", "folderDepthCheck");
  unrealizeSpin(widget, keyString, "history", "folderDepthSpin");

  /* Store Options Tab */
  unrealizeToggle(widget, keyString, "history", "notExpressionCheckFile");
  unrealizeToggle(widget, keyString, "history", "matchCaseCheckFile");
  unrealizeToggle(widget, keyString, "history", "ignoreHiddenFiles");
  unrealizeToggle(widget, keyString, "history", "dosExpressionRadioFile");
  unrealizeToggle(widget, keyString, "history", "regularExpressionRadioFile");
  unrealizeToggle(widget, keyString, "history", "singlePhaseCheckContents");
  unrealizeToggle(widget, keyString, "history", "matchCaseCheckContents");
  unrealizeToggle(widget, keyString, "history", "wildcardCheckContents");
  unrealizeToggle(widget, keyString, "history", "regularExpressionRadioContents");
  unrealizeToggle(widget, keyString, "history", "limitResultsCheckResults");
  unrealizeSpin(widget, keyString, "history", "maxHitsSpinResults");
  unrealizeToggle(widget, keyString, "history", "showLinesCheckResults");
  unrealizeSpin(widget, keyString, "history", "showLinesSpinResults");
  unrealizeToggle(widget, keyString, "history","followSymLinksCheck");
  unrealizeToggle(widget, keyString, "history", "limitContentsCheckResults");
  unrealizeSpin(widget, keyString, "history", "maxContentHitsSpinResults");
}


/*
 * Callback helper: retrieve menubar settings from config
 */
void realize_menubar (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);
  /* Restore View */
  realizeMenuCheck(widget, keyString, "menuOptions", "toolbar2");
  realizeMenuCheck(widget, keyString, "menuOptions", "status_bar1");
  realizeMenuCheck(widget, keyString, "menuOptions", "horizontal_results1");
  realizeMenuCheck(widget, keyString, "menuOptions", "vertical_results1");
  
  /* Restore View->sort_Column */
  realizeMenuCheck(widget, keyString, "menuOptions", "file_name1");
  realizeMenuCheck(widget, keyString, "menuOptions", "location1");
  realizeMenuCheck(widget, keyString, "menuOptions", "size1");
  realizeMenuCheck(widget, keyString, "menuOptions", "type1");
  realizeMenuCheck(widget, keyString, "menuOptions", "modified1");
  realizeMenuCheck(widget, keyString, "menuOptions", "matches1");

  /* Restore Settings */
  realizeMenuCheck(widget, keyString, "menuOptions", "word_wrap1");
  realizeMenuCheck(widget, keyString, "menuOptions", "autosize_columns");
}


/*
 * Callback helper: store menubar settings to config
 */
void unrealize_menubar (GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);

  /* Store View */
  unrealizeMenuCheck(widget, keyString, "menuOptions", "toolbar2");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "status_bar1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "horizontal_results1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "vertical_results1");
  
  /* Store View->sort_Column */
  unrealizeMenuCheck(widget, keyString, "menuOptions", "file_name1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "location1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "size1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "type1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "modified1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "matches1");

  /* Store Settings */
  unrealizeMenuCheck(widget, keyString, "menuOptions", "word_wrap1");
  unrealizeMenuCheck(widget, keyString, "menuOptions", "autosize_columns");
}


/*
 * Callback helper: initialises the file name match result table columns.
 * Adds the "clicked" signals so that the sort columns can be chosen by the user.
 * Adds the "selection changed" signal so that the contents are changed when the user
 * clicks on a new row.
 */

void initTreeView_aux(GtkTreeView *treeview, GtkListStore *store, GtkTreeModel *sortedModel)
{
  GtkCellRenderer *renderer, *folderRenderer;
  GtkTreeViewColumn *column;
  GtkTreeSelection *select;

  g_assert(treeview != NULL);
  
  gtk_tree_view_set_model (treeview, GTK_TREE_MODEL (sortedModel));

  /* Create cell renderers */
  renderer = gtk_cell_renderer_text_new ();
  folderRenderer = gtk_cell_renderer_text_new ();
  g_object_set(folderRenderer,
               "ellipsize", PANGO_ELLIPSIZE_MIDDLE,
               "ellipsize-set", TRUE,
               NULL);

  /* Add columns to the list */
  column = gtk_tree_view_column_new_with_attributes (_("Name"),
                                                     renderer,
                                                     "text", FILENAME_COLUMN,
                                                     NULL);
  g_signal_connect ((gpointer) column, "clicked",
                    G_CALLBACK (on_column_click),
                    GINT_TO_POINTER(FILENAME_COLUMN));
  gtk_tree_view_append_column (treeview, column);
    
  column = gtk_tree_view_column_new_with_attributes (_("Location"),
                                                     folderRenderer,
                                                     "text", LOCATION_COLUMN,
                                                     NULL);
  g_signal_connect ((gpointer) column, "clicked",
                    G_CALLBACK (on_column_click),
                    GINT_TO_POINTER(LOCATION_COLUMN));
  gtk_tree_view_append_column (treeview, column);
    
  column = gtk_tree_view_column_new_with_attributes (_("Size"),
                                                     renderer,
                                                     "text", SIZE_COLUMN,
                                                     NULL);
  g_signal_connect ((gpointer) column, "clicked",
                    G_CALLBACK (on_column_click),
                    GINT_TO_POINTER(SIZE_COLUMN));
  gtk_tree_view_append_column (treeview, column);
    
  column = gtk_tree_view_column_new_with_attributes (_("Type"),
                                                     renderer,
                                                     "text", TYPE_COLUMN,
                                                     NULL);
  g_signal_connect ((gpointer) column, "clicked",
                    G_CALLBACK (on_column_click),
                    GINT_TO_POINTER(TYPE_COLUMN));
  gtk_tree_view_append_column (treeview, column);
    
  column = gtk_tree_view_column_new_with_attributes (_("Modified"),
                                                     renderer,
                                                     "text", MODIFIED_COLUMN,
                                                     NULL);
  g_signal_connect ((gpointer) column, "clicked",
                    G_CALLBACK (on_column_click),
                    GINT_TO_POINTER(MODIFIED_COLUMN));
  gtk_tree_view_append_column (treeview, column);

  column = gtk_tree_view_column_new_with_attributes (_("Matches"),
                                                     renderer,
                                                     "text", MATCHES_COUNT_STRING_COLUMN,
                                                     NULL);
  g_signal_connect ((gpointer) column, "clicked",
                    G_CALLBACK (on_column_click),
                    GINT_TO_POINTER(MATCHES_COUNT_STRING_COLUMN));
  gtk_tree_view_append_column (treeview, column);

  /* Set up select call back handler */
  /* Setup the selection handler */

  select = gtk_tree_view_get_selection (treeview);
  gtk_tree_selection_set_mode (select, GTK_SELECTION_SINGLE);
  g_signal_connect (G_OBJECT (select), "changed",
                    G_CALLBACK (tree_selection_changed_cb),
                    NULL);

  /* Allow clickable headers */
  g_object_set(treeview,
               "headers-clickable", TRUE,
               NULL);
}

void initTreeView(GtkWidget *widget)
{
  GtkListStore *store;
  GtkTreeModel *sortedModel;

/* Attach new list store object to treeview widget */
  store = gtk_list_store_new (N_COLUMNS,       /* Total number of columns */
                              G_TYPE_STRING,   /* File name               */
                              G_TYPE_STRING,   /* Location                */
                              G_TYPE_STRING,   /* File size               */
                              G_TYPE_STRING,   /* File Type               */
                              G_TYPE_STRING,   /* Modified date           */
                              G_TYPE_STRING,   /* Complete File Name      */
                              G_TYPE_STRING,   /* Match count (string)    */
                              G_TYPE_UINT,     /* File size in bytes      */
                              G_TYPE_UINT,     /* Modified date as int    */
                              G_TYPE_UINT,     /* Match count             */
                              G_TYPE_UINT);    /* Match index             */

  sortedModel = gtk_tree_model_sort_new_with_model (GTK_TREE_MODEL(store));
  
  initTreeView_aux(GTK_TREE_VIEW(lookup_widget(widget, "treeview1")), store, sortedModel);
  initTreeView_aux(GTK_TREE_VIEW(lookup_widget(widget, "treeview2")), store, sortedModel);
  g_object_unref(store);
}


/*
 * Callback helper: initialises the two fonts for the content and match results buffer
 */

void initTextView_aux(GtkTextView *textview)
{
  GtkTextBuffer *buffer;
  GtkTextTag *tag;

  g_assert(textview != NULL);
  
  buffer = gtk_text_view_get_buffer (textview);

  /* Used to highlight each match result */
  tag = gtk_text_buffer_create_tag (buffer, "word_highlight",
                                    "foreground", "blue",
				    "weight", PANGO_WEIGHT_NORMAL,
				    "weight-set", TRUE,
                                    "style", PANGO_STYLE_NORMAL,
				    "style-set", TRUE,
                                    "underline", PANGO_UNDERLINE_SINGLE,
                                    "underline-set", TRUE,
                                    NULL);

  /* Used to force the default font to standard i.e. no bold/italics */
  tag = gtk_text_buffer_create_tag (buffer, "results_text",
				    "weight", PANGO_WEIGHT_NORMAL,
				    "weight-set", TRUE,
                                    "style", PANGO_STYLE_NORMAL,
				    "style-set", TRUE,
				    "left-margin", 30,
				    "left-margin-set", TRUE,
                                    NULL);

  /* Used to make the heading line stand out i.e. bold and italics */
  tag = gtk_text_buffer_create_tag (buffer, "results_header",
				    "weight", PANGO_WEIGHT_BOLD,
				    "weight-set", TRUE,
                                    "style", PANGO_STYLE_ITALIC,
				    "style-set", TRUE,
                                    NULL);

  /* Used to make the line numbers stand out i.e. just italics */
  tag = gtk_text_buffer_create_tag (buffer, "results_line_number",
				    "pixels-above-lines", 15,
				    "pixels-above-lines-set", TRUE,
				    "weight", PANGO_WEIGHT_NORMAL,
				    "weight-set", TRUE,
                                    "style", PANGO_STYLE_ITALIC,
				    "style-set", TRUE,
                                    "underline", PANGO_UNDERLINE_SINGLE,
                                    "underline-set", TRUE,
                                    NULL);
  tag = gtk_text_buffer_create_tag (buffer, "no_context",
				    "weight", PANGO_WEIGHT_NORMAL,
				    "weight-set", TRUE,
                                    "style", PANGO_STYLE_NORMAL,
				    "style-set", TRUE,
                                    "foreground", "darkGrey",
                                    "underline-set", FALSE,
                                    NULL);
  return;
}

void initTextView(GtkWidget *widget)
{
  initTextView_aux(GTK_TEXT_VIEW(lookup_widget(widget, "textview1")));
  initTextView_aux(GTK_TEXT_VIEW(lookup_widget(widget, "textview4")));
}


/*
 * Callback helper: initialises the statusbar memory structure - automatically destroyed with parent
 */
void realize_statusbar (GtkWidget *widget)
{
  statusbarData *status;
  GtkStatusbar *statusbar = GTK_STATUSBAR(lookup_widget(widget, "statusbar1"));
  
  status = g_object_get_data(G_OBJECT(mainWindowApp), MASTER_STATUSBAR_DATA);
  g_sprintf(status->constantString, _("Ready"));
  gtk_statusbar_push(statusbar, STATUSBAR_CONTEXT_ID(statusbar), status->constantString);
}


/**************************************************************
 *    Generic save/restore commands
 **************************************************************/
/*
 * Callback helper: retrieve file dialog's filename from config.ini settings
 */
void realizeFileDialog(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    gchar *filename;
    GtkWidget *dialog = lookup_widget(widget, name);

    if (g_key_file_has_key(keyString, group, name, NULL)) {
        filename = g_key_file_get_string (keyString, group, name, NULL);
        if (filename != NULL) {
            gtk_file_chooser_set_filename (GTK_FILE_CHOOSER (dialog), filename);
            g_free(filename);
        }
    }
}


/*
 * Callback helper: store file dialog's filename into config.ini settings
 */
void unrealizeFileDialog(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    gchar *filename;
    GtkWidget *dialog = lookup_widget(widget, name);
    
    filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER (dialog));
    if (filename != NULL) {
        g_key_file_set_string (keyString, group, name, filename);
        g_free(filename);
    }
}


/*
 * Callback helper: retrieve generic menu item's checkbox value from config.ini settings
 */
void realizeMenuCheck(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    if (g_key_file_has_key(keyString, group, name, NULL)) {
        gtk_check_menu_item_set_active(GTK_CHECK_MENU_ITEM(lookup_widget(widget, name)),
                                     g_key_file_get_boolean (keyString, group, name, NULL));
    }
}


/*
 * Callback helper: store generic menu item's checkbox value into config.ini settings
 */
void unrealizeMenuCheck(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    g_key_file_set_boolean (keyString, group, name,                       
                            gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(lookup_widget(widget, name))));
}

/*
 * Callback helper: retrieve generic text entry box text from config.ini settings
 */
void realizeString(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  gchar *tmpString;
  
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    tmpString = g_key_file_get_string (keyString, group, name, NULL);
    if (tmpString != NULL) {
      gtk_entry_set_text(GTK_ENTRY(lookup_widget((widget), name)), tmpString);
      g_free(tmpString);
     }
  }
}


/*
 * Callback helper: store generic text entry box text into config.ini settings
 */
void unrealizeString(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  g_key_file_set_string (keyString, group, name,
                         gtk_entry_get_text(GTK_ENTRY(lookup_widget(widget, name))));
}


/*
 * Callback helper: retrieve file chooser button filename from config.ini settings
 */
gboolean realizeFileButton(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkFileChooser *chooser = GTK_FILE_CHOOSER(lookup_widget(widget, name));
  gchar *tmpString;
  gboolean retVal = FALSE;
  
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    tmpString = g_key_file_get_string (keyString, group, name, NULL);
    if (tmpString != NULL) {
      retVal = gtk_file_chooser_set_filename (chooser, tmpString);
      g_free(tmpString);
    }
  }
  return retVal;
}


/*
 * Callback helper: store file chooser button filename into config.ini settings
 */
void unrealizeFileButton(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkFileChooser *chooser = GTK_FILE_CHOOSER(lookup_widget(widget, name));
  gchar *tmpString = gtk_file_chooser_get_filename (chooser);

  if (tmpString != NULL) {
    g_key_file_set_string (keyString, group, name, tmpString);
    g_free(tmpString);
  }
}


/*
 * Callback helper: retrieve generic toggle button value from config.ini settings
 */
void realizeToggle(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(widget, name)),
                                 g_key_file_get_boolean (keyString, group, name, NULL));
  }
}


/*
 * Callback helper: store generic toggle button value into config.ini settings
 */
void unrealizeToggle(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  g_key_file_set_boolean (keyString, group, name,
                          gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(widget, name))));
}


/*
 * Callback helper: retrieve generic current notebook page number from config.ini settings
 */
void realizeNotebook(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    gtk_notebook_set_current_page(GTK_NOTEBOOK(lookup_widget(widget, name)),
                                  g_key_file_get_integer (keyString, group, name, NULL));
  }
}


/*
 * Callback helper: store generic current notebook page number into config.ini settings
 */
void unrealizeNotebook(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  g_key_file_set_integer (keyString, group, name,
                          gtk_notebook_get_current_page(GTK_NOTEBOOK(lookup_widget(widget, name))));
}


/*
 * Callback helper: retrieve generic toggle button value from config.ini settings
 */
void realizeSpin(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  gdouble spinValue;
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    spinValue = (gdouble)g_key_file_get_integer (keyString, group, name, NULL);
    gtk_spin_button_set_value(GTK_SPIN_BUTTON(lookup_widget(widget, name)), spinValue);
  }
}


/*
 * Callback helper: store generic toggle button value into config.ini settings
 */
void unrealizeSpin(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  gdouble spinValue = gtk_spin_button_get_value(GTK_SPIN_BUTTON(lookup_widget(widget, name)));
  g_key_file_set_integer (keyString, group, name, (gint)spinValue);
}




/**************************************************************
 *    Specific save/restore commands (for more complex widgets)
 **************************************************************/
/*
 * Callback helper: retrieve window's dimensions from config.ini settings
 */
void realizeWindow(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  gint *width_height;
  gsize length;
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    width_height = g_key_file_get_integer_list (keyString, group, name, &length, NULL);
    if (length == 2) {
      gtk_window_set_default_size (GTK_WINDOW(lookup_widget(widget,name)),
                                   width_height[0], width_height[1]);
    }
    g_free(width_height);
  }
}


/*
 * Callback helper: store window's dimensions into config.ini settings
 */
void unrealizeWindow(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  gint width_height[2];
  gtk_window_get_size (GTK_WINDOW(lookup_widget(widget, name)),
                       &width_height[0], &width_height[1]);
  g_key_file_set_integer_list (keyString, group, name, width_height, 2);
}


/*
 * Callback helper: retrieve content result's font description from config.ini settings
 */
void realizeTextviewFont(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkTextView *textview;
  if (getResultsViewHorizontal(widget)) {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview1"));
  } else {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview4"));
  }
  gchar *newFont;
  PangoFontDescription *desc;
  
  PangoContext* context = gtk_widget_get_pango_context (GTK_WIDGET(textview));
  
  g_assert(context != NULL);
  
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    newFont = g_key_file_get_string (keyString, group, name, NULL);
    if (newFont != NULL) {
      desc = pango_font_description_from_string(newFont);
      if (desc != NULL) {   
        gtk_widget_modify_font (GTK_WIDGET(textview), desc);
        pango_font_description_free(desc);
      }
      g_free(newFont);
    }
  }
}


/*
 * Callback helper: store content result's font description into config.ini settings
 */
void unrealizeTextviewFont(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkTextView *textview;
  if (getResultsViewHorizontal(widget)) {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview1"));
  } else {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview4"));
  }
  PangoContext* context = gtk_widget_get_pango_context (GTK_WIDGET(textview));
  PangoFontDescription *desc = pango_context_get_font_description(context);    
  gchar *newFont = pango_font_description_to_string(desc);
  
  if (newFont != NULL) {  
    g_key_file_set_string (keyString, group, name, newFont);
    g_free(newFont);
  }
}


/*
 * Callback helper: retrieve content result's highlight colour from config.ini settings
 */
void realizeTextviewHighlight(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkTextView *textview;
  if (getResultsViewHorizontal(widget)) {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview1"));
  } else {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview4"));
  }
  gchar *newColor;
  GdkColor cp;
  GtkTextBuffer* textBuf = gtk_text_view_get_buffer (textview);
  GtkTextTagTable* tagTable = gtk_text_buffer_get_tag_table(textBuf);
  GtkTextTag* tag = gtk_text_tag_table_lookup(tagTable, "word_highlight");
  
  g_assert(textview != NULL);
  g_assert(tag != NULL);
  g_assert(tagTable != NULL);
  g_assert(textBuf != NULL);
  
  if (g_key_file_has_key(keyString, group, name, NULL)) {
    newColor = g_key_file_get_string (keyString, group, name, NULL);
    if (newColor != NULL) {
      if (gdk_color_parse (newColor, &cp)) {
        g_object_set( G_OBJECT(tag), "foreground-gdk", &cp, NULL);
      }
      g_free(newColor);
    }
  }
}


/*
 * Callback helper: store content result's highlight colour into config.ini settings
 */
void unrealizeTextviewHighlight(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkTextView *textview;
  if (getResultsViewHorizontal(widget)) {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview1"));
  } else {
    textview = GTK_TEXT_VIEW(lookup_widget(widget, "textview4"));
  }
  gchar *newColor;
  GdkColor *cp;
  GtkTextBuffer* textBuf = gtk_text_view_get_buffer (textview);
  GtkTextTagTable* tagTable = gtk_text_buffer_get_tag_table(textBuf);
  GtkTextTag* tag = gtk_text_tag_table_lookup(tagTable, "word_highlight");
  
  g_object_get( G_OBJECT(tag), "foreground-gdk", &cp, NULL);
  newColor = gtk_color_selection_palette_to_string(cp, 1);
  
  g_key_file_set_string (keyString, group, name, newColor);
  gdk_color_free(cp);
  g_free(newColor);
}


/*
 * Callback helper: retrieve generic combo-model value from config.ini settings
 */
void realizeComboModel(GtkListStore *store, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    gchar **tmpString;
    gsize length;
    gint i;
    GtkTreeIter iter;
    
    g_assert(store != NULL);
    
    if (g_key_file_has_key(keyString, group, name, NULL)) {
        tmpString = g_key_file_get_string_list (keyString, group, name, &length, NULL);
        if (tmpString != NULL) {
            for (i=(gint)length; i>0; i--) {
                g_assert(store != NULL);
                gtk_list_store_prepend (store, &iter);
                gtk_list_store_set (store, &iter, 0, tmpString[i-1], -1);
            }
            g_strfreev(tmpString);
        }
    }
}


/*
 * Callback helper: retrieve generic Combo - box from config.ini settings
 */
void realizeComboBox(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    gchar *readString;
    gsize length;
    gint i;
    gint activeRow = 0;
    GtkComboBox *comboBox = GTK_COMBO_BOX(lookup_widget(widget, name));

    /*Retrieve model */
    realizeComboModel(GTK_LIST_STORE(gtk_combo_box_get_model(comboBox)), keyString, group, name);
    
    /* Retrieve active row */
    readString = g_strconcat(name, "-active", NULL);
    g_assert(readString != NULL);
    if (g_key_file_has_key(keyString, group, readString, NULL)) {
      activeRow = g_key_file_get_integer (keyString, group, readString, NULL);
      gtk_combo_box_set_active (comboBox, activeRow);
    }
    g_free(readString);
}


/*
 * Callback helper: retrieve Combobox with two models from config.ini settings
 */
void realizeComboBox2(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
    GtkComboBox *comboBox = GTK_COMBO_BOX(lookup_widget(widget, name));
    gint* setActive;
    gchar* keyname;

    /*Retrieve model */
    keyname = g_strconcat(name, "regex", NULL);
    realizeComboModel(GTK_LIST_STORE(g_object_get_data(G_OBJECT(comboBox), "regex")), keyString, group, keyname);
    g_free(keyname);
    keyname = g_strconcat(name, "noregex", NULL);
    realizeComboModel(GTK_LIST_STORE(g_object_get_data(G_OBJECT(comboBox), "noregex")), keyString, group, keyname);
    g_free(keyname);
    
    /* Retrieve active row */
    setActive = (gint *)g_malloc(sizeof(gint));
    keyname = g_strconcat(name, "regex-active", NULL);
    if (g_key_file_has_key(keyString, group, keyname, NULL)) {
      *setActive = g_key_file_get_integer (keyString, group, keyname, NULL);
    } else {
      *setActive = -1;
    }
    g_free(keyname);
    g_object_set_data_full(G_OBJECT(comboBox), "regex-active", (gpointer)setActive, &g_free);

    setActive = (gint *)g_malloc(sizeof(gint));
    keyname = g_strconcat(name, "noregex-active", NULL);
    if (g_key_file_has_key(keyString, group, keyname, NULL)) {
      *setActive = g_key_file_get_integer (keyString, group, keyname, NULL);
    } else {
      *setActive = -1;
    }
    g_free(keyname);
    g_object_set_data_full(G_OBJECT(comboBox), "noregex-active", (gpointer)setActive, &g_free);

    /* Default to Regex mode */
    gtk_combo_box_set_model(comboBox, GTK_TREE_MODEL(g_object_get_data(G_OBJECT(comboBox), "regex")));
    gtk_combo_box_set_active(comboBox, *((gint *)g_object_get_data(G_OBJECT(comboBox),"regex-active")));
}


/*
 * Callback helper: store generic combo-box model into config.ini settings
 */
void unrealizeComboModel(GtkListStore *store, GKeyFile *keyString, const gchar *group, const gchar *name, gint activeRow)
{
  GtkTreeIter iter;
  gchar *readString = NULL;
  gchar **outString;
  gint i=0;

  g_assert(store != NULL);

  /* Store active row */
  readString = g_strconcat(name, "-active", NULL);
  g_key_file_set_integer (keyString, group, readString, activeRow);
  g_free(readString);
  
  /* Find first, and then loop through all until duplicate found */
  if (gtk_tree_model_get_iter_first (GTK_TREE_MODEL(store), &iter)) {
    outString = g_malloc((1+gtk_tree_model_iter_n_children(GTK_TREE_MODEL(store), NULL))* sizeof(gchar *));
      do {
          gtk_tree_model_get (GTK_TREE_MODEL(store), &iter,
                              0, &readString,
                              -1);
          if (readString != NULL) {
              outString[i++] = readString;
          }
      } while (gtk_tree_model_iter_next(GTK_TREE_MODEL(store), &iter));
      outString[i] = NULL; /* Terminate array */
      
      g_key_file_set_string_list (keyString, group, name, (const gchar**)outString, i);

      /* Clean up */
      g_strfreev(outString);
  }
}


/*
 * Callback helper: store generic combo box into config.ini settings
 */
void unrealizeComboBox(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkComboBox *comboBox = GTK_COMBO_BOX(lookup_widget(widget, name));
  gint activeRow = gtk_combo_box_get_active(comboBox);

  unrealizeComboModel(GTK_LIST_STORE(gtk_combo_box_get_model(comboBox)), keyString, group, name, activeRow);
}


/*
 * Callback helper: store combo box with two models into config.ini settings
 */
void unrealizeComboBox2(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name)
{
  GtkComboBox *comboBox = GTK_COMBO_BOX(lookup_widget(widget, name));
  gint regexActive;
  gint noregexActive;

  if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(widget, "regularExpressionRadioFile")))) {
    regexActive = gtk_combo_box_get_active(comboBox);
    noregexActive = *((gint *)g_object_get_data(G_OBJECT(comboBox), "noregex-active"));
  } else {
    regexActive = *((gint *)g_object_get_data(G_OBJECT(comboBox), "regex-active"));
    noregexActive = gtk_combo_box_get_active(comboBox);
  }

  gchar * keyname = g_strconcat(name, "regex", NULL);
  unrealizeComboModel(GTK_LIST_STORE(g_object_get_data(G_OBJECT(comboBox), "regex")), keyString, group, keyname, regexActive);
  g_free(keyname);

  keyname = g_strconcat(name, "noregex", NULL);
  unrealizeComboModel(GTK_LIST_STORE(g_object_get_data(G_OBJECT(comboBox), "noregex")), keyString, group, keyname, noregexActive);
  g_free(keyname);
}


/*
 * Callback helper: retrieve file results table's sort column plus order from config.ini settings
 */
void realizeTreeview(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth)
{
  gchar *fullName;
  GtkCheckMenuItem *toggle_button;
  gint colNum;
  GtkSortType colOrder;

  /* restore sort column */
  fullName = g_strconcat(name, "Sortcol", NULL);
  if (g_key_file_has_key(keyString, group, fullName, NULL)) {
    colNum = g_key_file_get_integer (keyString, group, fullName, NULL);
  }
  g_free(fullName);

  /* restore sort order */
  fullName = g_strconcat(name, "SortcolOrder", NULL);
  if (g_key_file_has_key(keyString, group, fullName, NULL)) {
    colOrder = g_key_file_get_integer (keyString, group, fullName, NULL);
  }
  g_free(fullName);
  setSortMenuItem(colNum, colOrder);
}


/*
 * Callback helper: store file results table's column sort column plus order into config.ini settings
 */
void unrealizeTreeview(GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth)
{
  gchar *fullName;
  GtkCheckMenuItem *toggle_button;
  gint colNum;
  GtkSortType sortOrder;
  
  /* save sort column + order */
  getSortMenuItem(&colNum, &sortOrder);

  fullName = g_strconcat(name, "Sortcol", NULL);
  g_key_file_set_integer (keyString, group, fullName, colNum);
  g_free(fullName);
  
  fullName = g_strconcat(name, "SortcolOrder", NULL);
  g_key_file_set_integer (keyString, group, fullName, sortOrder);
  g_free(fullName);
}


/*
 * Callback helper: retrieve file results table's column width from config.ini settings
 * TODO: Replace strcmp command with index (i.e. numerical location) based test, as string name is liable to change with locale..
 */
void realizeTreeviewColumns (GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth)
{
  GtkTreeView *treeview;
  if (getResultsViewHorizontal(widget)) {
    treeview = GTK_TREE_VIEW(lookup_widget(widget, "treeview1"));
  } else {
    treeview = GTK_TREE_VIEW(lookup_widget(widget, "treeview2"));
  }
  GList *allColumns = gtk_tree_view_get_columns(treeview);
  GtkTreeViewColumn *column;
  gchar *fullName;
  gint colWidth;

  g_assert(treeview != NULL); /* Internal error if unable to find treeview */
  if (allColumns == NULL) {
    return; /* Not an error, just means treeview is not yet set up */
  }

  /* restore column widths */
  allColumns = g_list_first(allColumns);
  do {
    column =  allColumns->data;
    if (autoColumnWidth) {
      gtk_tree_view_column_set_resizable (column, FALSE);
      if (strcmp(gtk_tree_view_column_get_title(column), _("Location")) == 0) {
        gtk_tree_view_column_set_min_width (column, 350);
      } else {
        gtk_tree_view_column_set_min_width (column, -1);
      }
      gtk_tree_view_column_set_max_width (column, -1);
      gtk_tree_view_column_set_fixed_width (column, 1);
      gtk_tree_view_column_set_sizing(column, GTK_TREE_VIEW_COLUMN_AUTOSIZE);
    } else {
      gtk_tree_view_column_set_min_width (column, 50);
      gtk_tree_view_column_set_max_width (column, -1);
      gtk_tree_view_column_set_resizable (column, TRUE);
      gtk_tree_view_column_set_sizing(column, GTK_TREE_VIEW_COLUMN_FIXED);
      fullName = g_strconcat(name, gtk_tree_view_column_get_title(column), NULL);
      if (g_key_file_has_key(keyString, group, fullName, NULL)) {
        colWidth = g_key_file_get_integer (keyString, group, fullName, NULL);
        gtk_tree_view_column_set_fixed_width (column, colWidth);
      }
      g_free(fullName);
    }
  } while ((allColumns = g_list_next(allColumns)) != NULL);
  g_list_free(allColumns);
}


/*
 * Callback helper: store file results table's column width into config.ini settings
 */
void unrealizeTreeviewColumns (GtkWidget *widget, GKeyFile *keyString, const gchar *group, const gchar *name, gboolean autoColumnWidth)
{
  GtkTreeView *treeview;
  if (getResultsViewHorizontal(widget)) {
    treeview = GTK_TREE_VIEW(lookup_widget(widget, "treeview1"));
  } else {
    treeview = GTK_TREE_VIEW(lookup_widget(widget, "treeview2"));
  }
  GList *allColumns = gtk_tree_view_get_columns(treeview);
  GtkTreeViewColumn *column;
  gchar *fullName;
  gint colWidth;

  g_assert(treeview != NULL); /* Internal error if unable to find treeview */
  if (allColumns == NULL) {
    return; /* Not an error, just means treeview is not yet set up */
  }

  /* store column widths */
  if (!autoColumnWidth) {
    allColumns = g_list_first(allColumns);
    do {
      column =  allColumns->data;
      fullName = g_strconcat(name, gtk_tree_view_column_get_title(column), NULL);
      colWidth = gtk_tree_view_column_get_width (column);
      g_key_file_set_integer (keyString, group, fullName, colWidth);
      g_free(fullName);
    } while ((allColumns = g_list_next(allColumns)) != NULL);
    g_list_free(allColumns);
  }
}


/*********************************************************
 * Tree view handlers (Right click + sort columns)
 *********************************************************/
/*
 * Internal helper: destroys pop-up menu created with right-click on results table.
 */
void undo_popup_menu(GtkWidget *attach_widget, GtkMenu *menu)
{  
  gtk_widget_destroy(GTK_WIDGET(menu));
}


/*
 * Callback helper: creates pop-up menu (user presses right-click on results table).
 * Sets maximum time, and links destructor to undo_popup_menu 
 */
void do_popup_menu (GtkWidget *widget, GdkEventButton *event)
{
  GtkWidget *menu;
  int button, event_time;
  
  menu = create_menu1();
  
  if (event) {
    button = event->button;
    event_time = event->time;
  } else {
    button = 0;
    event_time = gtk_get_current_event_time ();
  }
  
  gtk_menu_attach_to_widget (GTK_MENU (menu), widget, undo_popup_menu);
  gtk_menu_popup (GTK_MENU (menu), NULL, NULL, NULL, NULL, 
                  event->button, event->time);
  return;
}

/*
 * Callback helper: detaches the pop-up menu (user presses right-click on results table).
 * In turn, detached menu is destroyed by undo_popup_menu destructor. 
 */
void detachMenu(GtkMenuItem *menuitem)
{
  GtkWidget *treeView;
  if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
    treeView = (lookup_widget(GTK_WIDGET(menuitem), "treeview1"));
  } else {
    treeView = (lookup_widget(GTK_WIDGET(menuitem), "treeview2"));
  }
  GList *menuList = gtk_menu_get_for_attach_widget (treeView);
  gtk_menu_detach(menuList->data); /* always first item */
}

/*
 * Internal callback: signal set by initTreeView function.
 * Causes file's content matches to be displayed, and formatted.
 */
void tree_selection_changed_cb (GtkTreeSelection *selection, gpointer data)
{
  GtkWidget *textBox;
  GtkTextBuffer *buffer;
  searchData *mSearchData; /* Master search data */
  searchControl *mSearchControl; /* Master search control */
  lineMatch *newTextMatch, *prevTextMatch = NULL;
  GtkTreeIter iter;
  GtkTreeModel *model;
  gchar *fullFileName, *size, *mdate, tmpString[MAX_FILENAME_STRING + 1];
  gchar *tmpString2;
  GtkTextIter txtIter, tmpIter;
  GtkTextIter start, end;
  gsize count, tmpCount;
  guint matchIndex;
  gint i = 0; 
  gint lineCount = 2; /* Heading, plus options (i.e. 2-lines) */
  GObject *window1;
  gboolean setWordWrap;
  gchar *t1;
  gchar *tmpStr = g_object_get_data(G_OBJECT(mainWindowApp), "noContextSearchString");
  gint errorCount = 0; /* Temp debug */
  gint a,b;

  g_assert(tmpStr != NULL);
  g_assert(selection != NULL);
  
  if (gtk_tree_selection_get_selected (selection, &model, &iter)) {
    g_assert(model != NULL);
    if (getResultsViewHorizontal(GTK_WIDGET(gtk_tree_selection_get_tree_view(selection)))) {
      textBox = lookup_widget(GTK_WIDGET(gtk_tree_selection_get_tree_view(selection)), "textview1");
    } else {
      textBox = lookup_widget(GTK_WIDGET(gtk_tree_selection_get_tree_view(selection)), "textview4");
    }

    window1 = G_OBJECT(lookup_widget(GTK_WIDGET(textBox), "window1"));
    setWordWrap = gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(lookup_widget(textBox, "word_wrap1")));
    gtk_text_view_set_wrap_mode(GTK_TEXT_VIEW(textBox), setWordWrap);

    g_assert(textBox != NULL);
    g_assert(window1 != NULL);
    
    buffer = gtk_text_view_get_buffer (GTK_TEXT_VIEW (textBox)); 
    g_assert(buffer != NULL);
    gtk_text_buffer_set_text(buffer, "", -1); /* Clear text! */
    gtk_text_buffer_get_start_iter(buffer, &txtIter); /* Find start iter.. */

    /* Get global data from the text view widget pointer */
//    g_static_mutex_lock(&mutex_Data);
    mSearchData = g_object_get_data(window1, MASTER_SEARCH_DATA);
    g_assert(mSearchData != NULL); 
    //g_static_mutex_unlock(&mutex_Data);
    //g_static_mutex_lock(&mutex_Control);
    mSearchControl = g_object_get_data(window1, MASTER_SEARCH_CONTROL);
    g_assert(mSearchControl != NULL);
    //g_static_mutex_unlock(&mutex_Control);
    gtk_tree_model_get (model, &iter, MATCHES_COUNT_COLUMN, &count, -1);
    if (count > 0) {
      gtk_tree_model_get (model, &iter, FULL_FILENAME_COLUMN, &fullFileName,
                                        SIZE_COLUMN, &size,
                                        MODIFIED_COLUMN, &mdate,
                                        MATCH_INDEX_COLUMN, &matchIndex,                                      -1);

      g_assert(fullFileName != NULL);
      g_assert(size != NULL);
      g_assert(mdate != NULL);
      
      tmpString2 = g_strconcat(fullFileName, " (", size, " ", mdate, ")\n", NULL);
      gtk_text_buffer_insert_with_tags_by_name (buffer, &txtIter, tmpString2, -1, "results_header", NULL);
      g_free(tmpString2);

      /* Add line to describe options applied at run-time */
      tmpString2 = generateContentOptionsString(mSearchControl);
      gtk_text_buffer_insert_with_tags_by_name (buffer, &txtIter, tmpString2, -1, "no_context", NULL);
      g_free(tmpString2);

      //g_static_mutex_lock(&mutex_Data);
      if (mSearchControl->flags & SEARCH_EXTRA_LINES) {
	for (i=0; i<count; i++) {
      g_assert((i+matchIndex) <= mSearchData->lineMatchArray->len);
	  newTextMatch = g_ptr_array_index(mSearchData->lineMatchArray, (i + matchIndex));
	  g_assert(newTextMatch != NULL);
	  
	  if ((prevTextMatch == NULL) || (prevTextMatch->lineNum != newTextMatch->lineNum)) {
	    
	    tmpCount = g_snprintf(tmpString, MAX_FILENAME_STRING, _("Line Number: %d\n"), newTextMatch->lineNum);
	    gtk_text_buffer_insert_with_tags_by_name (buffer, &txtIter, tmpString, -1, "results_line_number", NULL);
	    gtk_text_buffer_insert_with_tags_by_name (buffer, &txtIter, newTextMatch->pLine, -1, "results_text", NULL);
	    gtk_text_buffer_insert (buffer, &txtIter, "\n", -1);
	    
	    lineCount += newTextMatch->lineCountBefore;
	  } else {
	    lineCount -= (prevTextMatch->lineCountAfter + 1);
	  }
	  
	  gtk_text_buffer_get_iter_at_line (buffer, &tmpIter, lineCount);
	  a = gtk_text_iter_get_chars_in_line(&tmpIter);
	  b = gtk_text_iter_get_bytes_in_line(&tmpIter);
	  
	  if (((a >= (newTextMatch->offsetEnd)) &&
	       (a >= (newTextMatch->offsetStart))) &&
	      ((b >= (newTextMatch->offsetEnd)) &&
	       (b >= (newTextMatch->offsetStart)))) {
	    gtk_text_buffer_get_iter_at_line_offset (buffer, &start, lineCount, (newTextMatch->offsetStart));
	    gtk_text_buffer_get_iter_at_line_offset (buffer, &end, lineCount, (newTextMatch->offsetEnd));
	    gtk_text_buffer_apply_tag_by_name(buffer, "word_highlight", &start, &end);
	  } else {
	    errorCount ++;
	    g_printf(_("\nInternal error %d! Unable to highlight line - offset beyond line-end.\n"), errorCount);
	    g_printf(_("Please email all debug text to cottrela@users.sf.net.\n"));
	    g_printf(_("  Debug: fn='%s'\n"), fullFileName);
	    g_printf(_("  Debug: tc=%d + os=%d || oe=%d\n"), tmpCount, newTextMatch->offsetStart, newTextMatch->offsetEnd);
	    g_printf(_("  Debug: %d) '%s'\n"), newTextMatch->lineNum, newTextMatch->pLine);
	    if (errorCount > 3) {
	      break; /* Exit loop as the rest of the file is likely to be corrupt too...*/
	    }
	  }
	  lineCount += (newTextMatch->lineCountAfter + 1);
	  prevTextMatch = newTextMatch;
	}	
      } else { /* Otherise, only display line numbers - no actual file contents */
	for (i=0; i<count; i++) {
	  newTextMatch = g_ptr_array_index(mSearchData->lineMatchArray, (i + matchIndex));	  
	  g_assert(newTextMatch != NULL);
	  
	  if ((prevTextMatch == NULL) || (prevTextMatch->lineNum != newTextMatch->lineNum)) {
	    
	    tmpCount = g_snprintf(tmpString, MAX_FILENAME_STRING, _("Line Number: %d\n"), newTextMatch->lineNum);
	    gtk_text_buffer_insert (buffer, &txtIter, tmpString, -1);
	    lineCount += newTextMatch->lineCountBefore;
	  } else {
	    lineCount -= (prevTextMatch->lineCountAfter + 1);
	  }
	  lineCount += (newTextMatch->lineCountAfter + 1);
	  prevTextMatch = newTextMatch;
	}
      }
      //g_static_mutex_unlock(&mutex_Data);
      g_assert(fullFileName != NULL);
      g_assert(size != NULL);
      g_assert(mdate != NULL);
      
      g_free (fullFileName);
      g_free (size);
      g_free (mdate);
    } else { /* Print warning out just to fill the space */
      gtk_tree_model_get (model, &iter, FULL_FILENAME_COLUMN, &fullFileName, 
                                        SIZE_COLUMN, &size,
                                        MODIFIED_COLUMN, &mdate,
                                        MATCH_INDEX_COLUMN, &matchIndex, -1);
      
      tmpString2 = g_strconcat(fullFileName, " (", size, " ", mdate, ")\n", NULL);
      gtk_text_buffer_insert (buffer, &txtIter, tmpString2, -1);
      g_free(tmpString2);
      
      gtk_text_buffer_insert_with_tags_by_name (buffer, &txtIter, tmpStr, -1, "no_context", NULL);
    }
  }
}


/*
 * Internal helper: returns new string containing a textual description for all content specific options
 *    e.g. Options: None
 *    e.g. Options: 2 extra lines shown per hit; case-sensitive; 
 */
gchar *generateContentOptionsString(searchControl *mSearchControl)
{
  GString *retGString = g_string_new(_("Options: ")); /* Return string */
  guint options_flag;
  gint count1, count2;
  gboolean noFlags = TRUE;
  
  //g_static_mutex_lock(&mutex_Control);
  options_flag = mSearchControl->flags;
  count1 = mSearchControl->numExtraLines;
  count2 = mSearchControl->limitContentResults;
  //g_static_mutex_unlock(&mutex_Control);

  if (options_flag & SEARCH_CASE_SENSITIVE) {
    g_string_append(retGString, _("case sensitive; "));
    noFlags = FALSE;
  }

  if (options_flag & SEARCH_EXTRA_LINES) {
    if (count1 == 1) {
      g_string_append(retGString, _("display 1 extra line around match; "));
      noFlags = FALSE;
    } else if (count1 > 1) {
      g_string_append_printf(retGString, _("display %d extra lines around match; "), count1);
      noFlags = FALSE;
    }
  } else {
    g_string_append(retGString, _("only showing line numbers; "));
    noFlags = FALSE;
  }

  if (options_flag & SEARCH_LIMIT_CONTENT_SHOWN) {
    if (count2 == 1) {
      g_string_append(retGString, _("only showing first content match; "));
      noFlags = FALSE;
    } else {
      g_string_append_printf(retGString, _("only showing first %d content matches; "), count2);
      noFlags = FALSE;
    }
  }

  if (noFlags) {
    g_string_append(retGString, _("none"));
  } else {
    g_string_truncate(retGString, (retGString->len - 2));
  }
  g_string_append(retGString, _(".\n"));

  return g_string_free(retGString, FALSE);
}


/*
 * Internal callback: signal set by initTreeView function.
 * Causes table to sort with respect to the user_data column.
 */
void on_column_click (GtkTreeViewColumn *treeviewcolumn,
                      gpointer user_data)
{
  setSortMenuItem(GPOINTER_TO_INT(user_data), -1);
}


/*
 * Callback helper: common function called whenever the sort order is changed in the menu.
 * This function causes the table to get up/down arrows, and for the data to actually be re-sorted.
 * All other sort-functions will eventually cause this function to be executed.
 */
void columnClick(GtkWidget *widget, gint column)
{
  gint oldColumn;
  GtkSortType direction;
  GtkTreeView *listview;
  if (getResultsViewHorizontal(widget)) {
    listview = GTK_TREE_VIEW(lookup_widget(widget, "treeview1"));
  } else {
    listview = GTK_TREE_VIEW(lookup_widget(widget, "treeview2"));
  }
  GtkTreeSortable *sortable = GTK_TREE_SORTABLE(gtk_tree_view_get_model(listview));
  GtkTreeViewColumn *treeviewcolumn;
  GList *columnList = gtk_tree_view_get_columns(GTK_TREE_VIEW(listview));
  gint i=0;

  /* Change the sorted column */
  if (GTK_IS_TREE_SORTABLE (sortable)) {
      gtk_tree_sortable_get_sort_column_id (sortable, &oldColumn, &direction);
      if ((oldColumn == column) && (direction == GTK_SORT_ASCENDING)) {
	  direction = GTK_SORT_DESCENDING;
      } else {
	  direction = GTK_SORT_ASCENDING;
      }
      gtk_tree_sortable_set_sort_column_id (sortable, column, direction);

      /* Update the treeview heading indicators */
      if (column == INT_MODIFIED_COLUMN) {
	  oldColumn = MODIFIED_COLUMN;
      } else if (column == INT_SIZE_COLUMN) {
        oldColumn = SIZE_COLUMN;
      } else if (column == MATCHES_COUNT_COLUMN)  {
        oldColumn = MATCHES_COUNT_STRING_COLUMN;
      } else {
	  oldColumn = column;
      }
      columnList = g_list_first (columnList);
      do {
	  treeviewcolumn = GTK_TREE_VIEW_COLUMN(columnList->data);
	  if (i == oldColumn) {
	      gtk_tree_view_column_set_sort_indicator(treeviewcolumn, TRUE);
	      gtk_tree_view_column_set_sort_order(treeviewcolumn, direction);
	  } else {
	      gtk_tree_view_column_set_sort_indicator(treeviewcolumn, FALSE);
	      gtk_tree_view_column_set_sort_order(treeviewcolumn, GTK_SORT_ASCENDING);
	  }
	  i++;
      } while ((columnList = g_list_next(columnList)) != NULL);
  }
}


/*
 * Internal helper: called from unrealizeTreeview function.
 * Given column number (columnId), returns the sort order (ascending/descending).
 */
void getSortMenuItem(gint *columnId, GtkSortType *sortOrder)
{
  GtkTreeView *treeview;
  if (getResultsViewHorizontal(GTK_WIDGET(mainWindowApp))) {
    treeview = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(mainWindowApp), "treeview1"));
  } else {
    treeview = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(mainWindowApp), "treeview2"));
  }
  GtkTreeSortable *sortable = GTK_TREE_SORTABLE(gtk_tree_view_get_model(treeview));
  gtk_tree_sortable_get_sort_column_id (sortable, columnId, sortOrder);
}


/*
 * Internal helper: called from unrealizeTreeview function.
 * Given column number (columnId), checks the corresponding menu-item for sort order.
 * In turn, this causes Gtk to emit the activate signal, which calls the function columnClick()
 */
void setSortMenuItem(gint columnId, GtkSortType order)
{
  GtkCheckMenuItem *toggle_button;
  switch(columnId) {
  case LOCATION_COLUMN:
    toggle_button = GTK_CHECK_MENU_ITEM(lookup_widget(GTK_WIDGET(mainWindowApp), "location1"));
    break;
  case INT_SIZE_COLUMN:  /* Force to SIZE column */
  case SIZE_COLUMN: 
    toggle_button = GTK_CHECK_MENU_ITEM(lookup_widget(GTK_WIDGET(mainWindowApp), "size1"));
    break;
  case TYPE_COLUMN:
    toggle_button = GTK_CHECK_MENU_ITEM(lookup_widget(GTK_WIDGET(mainWindowApp), "type1"));
    break;
  case INT_MODIFIED_COLUMN: /* Force to MODIFIED column */
  case MODIFIED_COLUMN:
    toggle_button = GTK_CHECK_MENU_ITEM(lookup_widget(GTK_WIDGET(mainWindowApp), "modified1"));
    break;
  case MATCHES_COUNT_STRING_COLUMN: /* Force to MATCHES column */
  case MATCHES_COUNT_COLUMN:
    toggle_button = GTK_CHECK_MENU_ITEM(lookup_widget(GTK_WIDGET(mainWindowApp), "matches1"));
    break;
  case FILENAME_COLUMN: /* This is the default option */
  default:
    toggle_button = GTK_CHECK_MENU_ITEM(lookup_widget(GTK_WIDGET(mainWindowApp), "file_name1"));
    break;
  }
  
  /* Special case, only works first time a button is selected */
  if (order == GTK_SORT_DESCENDING) {
    gtk_check_menu_item_set_active (toggle_button, FALSE);
    gtk_check_menu_item_set_active (toggle_button, TRUE);
  }
  
  /* Hack to get the buttons working as expected */
  gtk_check_menu_item_set_active (toggle_button, FALSE);
  gtk_check_menu_item_set_active (toggle_button, TRUE);
}


/*
 * Internal helper: set to TRUE to enable extended regex mode
 */
void setExtendedRegexMode(GtkWidget *widget, gboolean extended)
{
  GKeyFile *keyString = getGKeyFile(widget);
  
  g_key_file_set_boolean(keyString, "configuration", "configExtendedRegex", extended);
}

/*
 * Internal helper: returns TRUE if extended regex mode is enabled
 */
gboolean getExtendedRegexMode(GtkWidget *widget)
{
  GKeyFile *keyString = getGKeyFile(widget);
  
  if (g_key_file_has_key(keyString, "configuration", "configExtendedRegex", NULL)) {
    return (g_key_file_get_boolean(keyString, "configuration", "configExtendedRegex", NULL));
  }
  return TRUE; /* default enable extended regex mode */
}
  

