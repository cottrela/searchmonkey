/*
 * File: systemio.c
 * Description: This file deals with CSV file creation, and parsing
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <gtk/gtk.h>
#include <glib.h>
#include <errno.h> /* UNIX style errno support */
#include <glib/gstdio.h> /* g_fopen,etc */
#include <string.h>

#include "interface.h" /* glade requirement */
#include "support.h" /* glade requirement */
#include "search.h" /* POSIX threaded search header */
#include "savestate.h" /* allow config file to be read */
#include "misc.h" /* Everything else */
#include "systemio.h"

/*
 * Default list of binary files for automatic searching - this list should cover all OSs in preference order.
 * Make sure that exeList is symetrical, and that empty pointers are set to NULL.
 */
const gchar *G_EXE_LIST[G_EXE_LIST_MAX_DEPTH][G_EXE_LIST_MAX_WIDTH] = {
  {"firefox", "iexplore","opera"},
  {"gedit", "emacs", "notepad"},
  {"nautilus", "iexplore", NULL}
};

/*
 * Internal helper: save results to CSV.
 * Iterates through table results (if any), and then returns a CSV string
 * containing all results, and formatted to boot.
 */
gchar *resultsToCsvString(GtkWidget *widget)
{
  gchar *retString = NULL;
  gchar **retStringArray;
  GtkTreeIter iter;
  GtkTreeView *treeview;
  GtkTreeModel *model;
  gint lineCount;
  gint i, rowCount = 0;
  gchar **tmpData;
  tmpData = g_malloc0(7 * sizeof(gchar *));
  gchar *columnSeparator, *rowSeparator, *oldString, *stringDelimiter;
  GKeyFile *keyString = getGKeyFile(widget);

  if (getResultsViewHorizontal(widget)) {
    treeview = GTK_TREE_VIEW(lookup_widget(widget, "treeview1"));
  } else {
    treeview = GTK_TREE_VIEW(lookup_widget(widget, "treeview2"));
  }

  model = gtk_tree_view_get_model(treeview);
  lineCount = gtk_tree_model_iter_n_children (model, NULL);

  g_assert(model != NULL);
  g_assert(treeview != NULL);
  g_assert(keyString != NULL);

  /* Count number of lines in output window */
  if (lineCount <= 0) {
      return NULL;
  }
  retStringArray = g_malloc((2 + lineCount) * sizeof(gchar *));
  
  /* Get column/row separator strings */
  if (g_key_file_has_key(keyString, "configuration", "configResultEOF", NULL)) {
    oldString = g_key_file_get_string(keyString, "configuration", "configResultEOF", NULL);
    columnSeparator = g_strcompress (oldString);
    g_free(oldString);
  } else {
    columnSeparator = g_strdup(",");
  }
  if (g_key_file_has_key(keyString, "configuration", "configResultEOL", NULL)) {
    oldString = g_key_file_get_string(keyString, "configuration", "configResultEOL", NULL);
    rowSeparator = g_strcompress (oldString);
    g_free(oldString);
  } else {
    rowSeparator = g_strdup("\n");
  }
  if (g_key_file_has_key(keyString, "configuration", "configResultDelimiter", NULL)) {
    oldString = g_key_file_get_string(keyString, "configuration", "configResultDelimiter", NULL);
    stringDelimiter = g_strcompress (oldString);
    g_free(oldString);
  } else {
    stringDelimiter = g_strdup("\"");
  }

  /* Create header */
  gtk_tree_model_get_iter_first (model, &iter);
  tmpData[0] = quoteString(stringDelimiter, _("File name"));
  tmpData[1] = quoteString(stringDelimiter, _("Location"));
  tmpData[2] = quoteString(stringDelimiter, _("File size"));
  tmpData[3] = quoteString(stringDelimiter, _("File type"));
  tmpData[4] = quoteString(stringDelimiter, _("Modified date"));
  tmpData[5] = NULL;
  retStringArray[rowCount++] = g_strjoinv(columnSeparator, tmpData);

  /* Loop through to collect data */
  do {
      gtk_tree_model_get (model, &iter,
                          FILENAME_COLUMN, &tmpData[0],
                          LOCATION_COLUMN, &tmpData[1],
                          SIZE_COLUMN, &tmpData[2],
                          TYPE_COLUMN, &tmpData[3],
                          MODIFIED_COLUMN, &tmpData[4],
                          -1);
      /* Put quotes around LOCATION and FILENAME (just in case) */
      for (i=0; i<=4; i++) {
        oldString = quoteString(stringDelimiter, tmpData[i]);
        g_free(tmpData[i]);
        tmpData[i] = oldString;
      }

      retStringArray[rowCount++] = g_strjoinv(columnSeparator, tmpData);
      for (i=0; i<=4; i++) {
        g_free(tmpData[i]);
      }
  } while (gtk_tree_model_iter_next(model, &iter));
  retStringArray[rowCount++] = NULL;

  retString = g_strjoinv(rowSeparator, retStringArray);
  g_strfreev(retStringArray);

  g_free(rowSeparator);
  g_free(columnSeparator);
  return retString;
}

/*
 * Internal helper: save results to CSV.
 * Adds delimiter (usually " symbol) to either side of given string.
 * Returns new string, that must be free'd
 */
gchar *quoteString(const gchar *delimiter, const gchar *string)
{
  return g_strconcat(delimiter, string, delimiter, NULL);  
} 

/*
 * callback helper: save results to CSV.
 * Stores all results to Csv file. Delimeters, and other options
 * can be configured by the user (e.g. comma or semicolon between fields)
 */
void saveResults(GtkWidget *widget)
{
  GError *error = NULL;
  gchar *filename;
  GtkWidget *warnMsg;
  GtkWidget *errMsg;
  GtkWidget *dialog = create_saveFileDialog();
  GKeyFile *keyString = g_object_get_data(G_OBJECT(mainWindowApp), MASTER_OPTIONS_DATA);
  gchar *saveData = resultsToCsvString(widget);

  if (saveData == NULL) {
      errMsg = gtk_message_dialog_new(GTK_WINDOW(dialog),
                                      (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                      GTK_MESSAGE_INFO,
                                      GTK_BUTTONS_OK,
                                      _("No results to save!"));
      gtk_dialog_run(GTK_DIALOG(errMsg));
      gtk_widget_destroy(errMsg);
      return;
  }

  /* Set defaults, or get saved values*/
  gtk_file_chooser_set_current_folder (GTK_FILE_CHOOSER (dialog), g_get_home_dir());
  gtk_file_chooser_set_current_name (GTK_FILE_CHOOSER (dialog), "results.csv");
  realizeFileDialog(dialog, keyString, "saveResults", "saveFileDialog");
  
  while (1) {
    if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_OK) {
      filename = gtk_file_chooser_get_filename (GTK_FILE_CHOOSER (dialog));
      if (g_file_test(filename, G_FILE_TEST_EXISTS)) {
        warnMsg = gtk_message_dialog_new(GTK_WINDOW(dialog),
                                         (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                         GTK_MESSAGE_WARNING,
                                         GTK_BUTTONS_OK_CANCEL,
                                         _("A file named \"%s\" already exists. Are you sure you want to overwrite it?"),
                                         filename);
        if (gtk_dialog_run(GTK_DIALOG(warnMsg)) != GTK_RESPONSE_OK) {
          gtk_widget_destroy(warnMsg);
          g_free(filename);
          continue;
        }
        gtk_widget_destroy(warnMsg);
      }
      if (g_file_set_contents2 (filename, saveData, -1, &error)) {
        unrealizeFileDialog(dialog, keyString, "saveResults", "saveFileDialog");
        g_free(filename);
        break;
      } else {
        errMsg = gtk_message_dialog_new(GTK_WINDOW(dialog),
                                        (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                        GTK_MESSAGE_ERROR,
                                        GTK_BUTTONS_OK,
                                        error->message);
        gtk_dialog_run(GTK_DIALOG(errMsg));
        gtk_widget_destroy(errMsg);
        g_free(filename);
        g_error_free(error);
        error = NULL;
        continue;
      }
    } else {
      break;
    }
  }
  gtk_widget_destroy (dialog);
  g_free(saveData);
  return;
}


/* IMPORT EXPORT FUNCTIONS  */


/*
 * Callback helper: import criteria from local text file, and parse user options.
 * TODO: Save state of import dialog so that users do not need to keep modifying options between sessions.
 */
void importCriteria(GtkWidget *widget) 
{
  GtkWidget *import = create_importCriteria();
  GList *allCriteria;
  GtkWidget *dropDownBox;
  gchar *filename;
  gchar **splitResult;
  gchar *contents;
  gsize length;
  gint i;
  gchar *orString;
  GError *error = NULL;
  GtkWidget *warnDialog;
  
  while (gtk_dialog_run(GTK_DIALOG(import)) == GTK_RESPONSE_OK) {
    filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(lookup_widget(import, "filechooserwidgetImport")));
    if (filename != NULL) {
      /* Choose the drop down box based on radio selection */
      if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(import, "fileNameRadioImport")))) {
        dropDownBox = lookup_widget(widget, "fileName");
      } else { /* Assume containing text import */
        dropDownBox = lookup_widget(widget, "containingText");      
      }
      
      if (g_file_get_contents (filename, &contents, &length, &error)) {
        splitResult = g_strsplit (contents, "\n", -1); /* Split completely */
        if (splitResult != NULL) {
          if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(import, "singleImportRadio")))) {
            orString = g_strjoinv("|", splitResult);
            if (orString[g_strlen(orString) - 1] == '|') {
              orString[g_strlen(orString) - 1] = '\0';
            }
            addUniqueRow(dropDownBox, orString);
            g_free(orString);
          } else { /* multiImportRadio */
            i = 0;
            while (splitResult[i] != NULL) {
              addUniqueRow(dropDownBox, splitResult[i]);
              i++;
            }
          }
          g_strfreev(splitResult);
          break;
        } else { /* !splitResult */
          warnDialog = gtk_message_dialog_new(GTK_WINDOW(import),
                                              (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                              GTK_MESSAGE_ERROR,
                                              GTK_BUTTONS_OK,
                                              _("File was empty/invalid."));
          gtk_dialog_run(GTK_DIALOG(warnDialog));
          gtk_widget_destroy(warnDialog);
        }
        
      } else { /* !g_file_get_contents */
        warnDialog = gtk_message_dialog_new(GTK_WINDOW(import),
                                            (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                            GTK_MESSAGE_ERROR,
                                            GTK_BUTTONS_OK,
                                            error->message);
        gtk_dialog_run(GTK_DIALOG(warnDialog));
        g_error_free(error);
        error = NULL;
        gtk_widget_destroy(warnDialog);
      }
    } else { /* !filename */
      warnDialog = gtk_message_dialog_new(GTK_WINDOW(import),
                                          (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                          GTK_MESSAGE_ERROR,
                                          GTK_BUTTONS_OK,
                                          _("Error! No valid file selected."));
      gtk_dialog_run(GTK_DIALOG(warnDialog));
      gtk_widget_destroy(warnDialog);
    }
  }
  gtk_widget_destroy(import);
}


/*
 * Callback helper: export criteria to local text file, depending on user options.
 * TODO: Save state of export dialog so that users do not need to keep modifying options between sessions.
 */
void exportCriteria(GtkWidget *widget) 
{
  GtkWidget *export = create_exportCriteria();
  gchar *filename;
  GtkWidget *dropDownBox;
  GtkTreeIter iter;
  GtkTreeModel *model;
  GString *gstr = NULL;
  gchar *outputString;
  gchar *readString;
  GError *error;
  GtkWidget *warnDialog;

  while (gtk_dialog_run(GTK_DIALOG(export)) == GTK_RESPONSE_OK) {
    filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(lookup_widget(export, "filechooserwidgetExport")));
    if (filename != NULL) {
      /* Choose the drop down box based on radio selection */
      if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(export, "fileNameRadioExport")))) {
        dropDownBox = lookup_widget(widget, "fileName");
      } else { /* Assume containing text export */
        dropDownBox = lookup_widget(widget, "containingText");      
      }
      gstr = g_string_new ("");
      if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(export, "singleExportRadio")))) {
        if ( gtk_combo_box_get_active_iter(GTK_COMBO_BOX(dropDownBox), &iter)) {
          model = gtk_combo_box_get_model(GTK_COMBO_BOX(dropDownBox));
          g_assert(model != NULL);
          gtk_tree_model_get (model, &iter,
                              0, &readString,
                              -1);
          if (readString != NULL) {  
            g_string_append(gstr, readString);
            g_string_append(gstr, "\n");
            g_free(readString);
          }
        } else { /* !gtk_combo_box_get_active_iter */
          warnDialog = gtk_message_dialog_new(GTK_WINDOW(export),
                                              (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                              GTK_MESSAGE_ERROR,
                                              GTK_BUTTONS_OK,
                                              _("Error! You must select at least one entry in order to save drop down list."));
          gtk_dialog_run(GTK_DIALOG(warnDialog));
          gtk_widget_destroy(warnDialog);
        }
      } else { /* multiExportRadio */
        model = gtk_combo_box_get_model(GTK_COMBO_BOX(dropDownBox));
        g_assert(model != NULL);
        if (gtk_tree_model_get_iter_first(model, &iter)) {
          do {
            gtk_tree_model_get (model, &iter,
                                0, &readString,
                                -1);
            if (readString != NULL) {  
              g_string_append(gstr, readString);
              g_string_append(gstr, "\n");
              g_free(readString);
            }
            /* Save each line to an open text file */
          } while (gtk_tree_model_iter_next(model, &iter));
        } else { /* !gtk_tree_model_get_iter_first */
          warnDialog = gtk_message_dialog_new(GTK_WINDOW(export),
                                              (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                              GTK_MESSAGE_ERROR,
                                              GTK_BUTTONS_OK,
                                              _("Error! Unable to find first combo entry. Is drop down list blank?"));
          gtk_dialog_run(GTK_DIALOG(warnDialog));
          gtk_widget_destroy(warnDialog);
        }
      }

      /* Save the text file to */
      if (gstr != NULL)
        outputString = g_string_free(gstr, FALSE);
      if (g_file_set_contents2(filename, outputString, -1, &error)) {
        g_free(outputString);
        g_free(filename);
        break; /* My work here is done! */
      } else {
        warnDialog = gtk_message_dialog_new(GTK_WINDOW(export),
                                            (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                            GTK_MESSAGE_ERROR,
                                            GTK_BUTTONS_OK,
                                            error->message);
        g_error_free(error);
        error = NULL;
        gtk_dialog_run(GTK_DIALOG(warnDialog));
        gtk_widget_destroy(warnDialog);        
      }
      
      g_free(outputString);
      g_free(filename);
    } else { /* !filename */
      warnDialog = gtk_message_dialog_new(GTK_WINDOW(export),
                                          (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                          GTK_MESSAGE_ERROR,
                                          GTK_BUTTONS_OK,
                                          _("Error! No valid file selected."));
      gtk_dialog_run(GTK_DIALOG(warnDialog));
      gtk_widget_destroy(warnDialog);
    }
  }
  gtk_widget_destroy(export);  
}


/*
 * Callback helper: configuration panel helper.
 * Checks that delimeter entered in CSV delimeter boxes is a valid charactor.
 * Expects a single charactor, but can be a special char e.g. \n for newline, etc.
 * This function is called after user focuses away from the entry box.
 * Errors and warnings produce pop-up messages immediately.
 * TODO: Update to allow Windows file endings i.e. \r\n
 */
void checkCsvEntry(GtkEntry *entry)
{
  gchar *orig = g_strdup(gtk_entry_get_text(entry));
  gchar *newExpanded;
  GtkWidget *errorMsg;

  if (orig[0] == '\\') {
    switch (orig[1]) {
    case 'b':
    case 'n':
    case 'r':
    case 't':
    case '"':
    case '\\':
      break; /* Everything is fine! */
    default:
      errorMsg = gtk_message_dialog_new(GTK_WINDOW(lookup_widget(GTK_WIDGET(entry), "configDialog")),
                                        GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT,
                                        GTK_MESSAGE_WARNING,
                                        GTK_BUTTONS_OK,
                                        _("Illegal escape sequence: '\\%c'.\n Valid escape characters are: b n r t \" \\"), orig[1]);
      g_signal_connect ((gpointer) errorMsg, "response",
                        G_CALLBACK (on_errorMsg_response),
                        NULL);
      gtk_widget_show(errorMsg);
      gtk_entry_set_text(entry, "");
      break;
    }
  } else {
    newExpanded = g_strescape(orig, NULL);
    if (newExpanded[0] == '\\') {
      if (g_strlen(newExpanded) == 2) {
        gtk_entry_set_text(entry, newExpanded); /* Automatically expand quotes, etc. */
      } else { /* Non-ASCII expansion e.g. GBP key */
        errorMsg = gtk_message_dialog_new(GTK_WINDOW(lookup_widget(GTK_WIDGET(entry), "configDialog")),
                                          GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT,
                                          GTK_MESSAGE_WARNING,
                                          GTK_BUTTONS_OK,
                                          _("Input string expands into non ASCII, or multi-byte key sequence: '%s'.\n Please try to use ASCII safe keys"), newExpanded);
        g_signal_connect ((gpointer) errorMsg, "response",
                          G_CALLBACK (on_errorMsg_response),
                          NULL);
        gtk_widget_show(errorMsg);
        gtk_entry_set_text(entry, "");
      }
    } else if (orig[1] != '\0') {
      errorMsg = gtk_message_dialog_new(GTK_WINDOW(lookup_widget(GTK_WIDGET(entry), "configDialog")),
                                        GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT,
                                        GTK_MESSAGE_WARNING,
                                        GTK_BUTTONS_OK,
                                        _("Multi-byte string entered: '%s'.\nTruncating extra character (%c)"), orig, orig[1]);
      g_signal_connect ((gpointer) errorMsg, "response",
                        G_CALLBACK (on_errorMsg_response),
                        NULL);
      gtk_widget_show(errorMsg);
      orig[1] = '\0';
      gtk_entry_set_text(entry, orig);
    }
    g_free(newExpanded);
  }
  g_free(orig);
}


/*
 * Internal helper: dialog destructor (automatically called from checkCsvEntry)
 * This function is required otherwise Focus out event is held up by user entry requirement.
 * This forces widget_show to be used in checkCsvEntry, rather than dialog_run (which halts function).
 */
void on_errorMsg_response (GtkDialog *dialog, gint arg1, gpointer user_data)
{
  gtk_widget_destroy(GTK_WIDGET(dialog));
}

/*
 * Clean up userExeData structure - callback from preferences pop-up destroy.
 */
void g_free_exeData(gpointer user_data)
{
  userExeData *exeData = user_data;
  gint i,j;

  for (i=0; i<G_EXE_LIST_MAX_DEPTH; i++) {
    for (j=0; j<G_EXE_LIST_MAX_WIDTH; j++) {
      if (exeData->retStr[i][j] != NULL) {
        g_free(exeData->retStr[i][j]);
      }
    }
  }
  g_free(exeData);
}

/*
 * get exeData - called via a timeout function from preferences.
 */
gboolean getExeData(gpointer user_data)
{
  GtkWidget *dialog = user_data;
  GtkProgressBar *pbar = GTK_PROGRESS_BAR(lookup_widget(dialog, "progressbar3"));
  userExeData *exeData = g_object_get_data(G_OBJECT(dialog), "exeData");
  gdouble tmpDbl = gtk_progress_bar_get_fraction(pbar) + PROGRESS_BAR_NUDGE_VALUE;
  gchar *tmpStr = g_strdup_printf("%.0lf%%", (tmpDbl * 100));

  /* Change progress bar */
  gtk_progress_bar_set_fraction (pbar, tmpDbl);
  gtk_progress_bar_set_text (pbar, tmpStr);
  g_free(tmpStr);

  /* Find all possible filenames */
  tmpStr = (char *)G_EXE_LIST[exeData->i][exeData->j];
  if (tmpStr != NULL) {
    exeData->retStr[exeData->i][exeData->j] = g_find_program_in_path(tmpStr);
  }

  /* Set next exe pointer */
  (exeData->i)++;
  if ((exeData->i) >= G_EXE_LIST_MAX_DEPTH) {
    (exeData->i)=0;
    (exeData->j)++;
    if ((exeData->j) >= G_EXE_LIST_MAX_WIDTH) {
      gtk_progress_bar_set_fraction (pbar, 1.0);
      gtk_progress_bar_set_text (pbar, _("100%"));
      gtk_dialog_set_response_sensitive(GTK_DIALOG(dialog), GTK_RESPONSE_OK, TRUE);
      return FALSE;
    }
  }
  return TRUE;
}


/*
 * Callback/Internal helper: uses the user defined default application
 * to open up the supplied address. This function enables many different
 * operation systems to open 3rd party applications (e.g. web browsers,
 * text editors, etc).
 */
gboolean SMsyscall (const gchar *address, syscallType type)
{
  GKeyFile *keyString = getGKeyFile(GTK_WIDGET(mainWindowApp));
  gchar *executable, *attributes;
  gchar *s1, *s2;
  gboolean retVal = FALSE;
  GtkWidget *okDialog;

  g_assert(address != NULL);
  g_assert((type == BROWSER_LIST) ||
           (type == TEXTEDITOR_LIST) ||
           (type == FILEEXPLORER_LIST));

  /* Read in executable string, plus attributes*/
  switch (type) {
  case BROWSER_LIST:
    if ((!g_key_file_has_key(keyString, "configuration", "configWebBrowser", NULL)) || 
          (!g_key_file_has_key(keyString, "configuration", "configWebBrowserAttributes", NULL))) {
      okDialog = gtk_message_dialog_new(GTK_WINDOW(mainWindowApp), (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                        GTK_MESSAGE_WARNING, GTK_BUTTONS_OK,
                                        _("Run configuration to set default web-browser:\n\"%s\""),address);
      gtk_dialog_run (GTK_DIALOG(okDialog));
      gtk_widget_destroy(GTK_WIDGET(okDialog));
      return -1;
    }
    executable = g_key_file_get_string (keyString, "configuration", "configWebBrowser", NULL);
    attributes = g_key_file_get_string (keyString, "configuration", "configWebBrowserAttributes", NULL);
    break;
  case TEXTEDITOR_LIST: 
    if ((!g_key_file_has_key(keyString, "configuration", "configTextEditor", NULL)) || 
          (!g_key_file_has_key(keyString, "configuration", "configTextEditorAttributes", NULL))) {
      okDialog = gtk_message_dialog_new(GTK_WINDOW(mainWindowApp), (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                        GTK_MESSAGE_WARNING, GTK_BUTTONS_OK,
                                        _("Run configuration to set default text editor:\n\"%s\""),address);
      gtk_dialog_run (GTK_DIALOG(okDialog));
      gtk_widget_destroy(GTK_WIDGET(okDialog));
      return -1;
    }
    executable = g_key_file_get_string (keyString, "configuration", "configTextEditor", NULL);
    attributes = g_key_file_get_string (keyString, "configuration", "configTextEditorAttributes", NULL);
    break;
  case FILEEXPLORER_LIST:
    if ((!g_key_file_has_key(keyString, "configuration", "configFileExplorer", NULL)) || 
          (!g_key_file_has_key(keyString, "configuration", "configFileExplorerAttributes", NULL))) {
      okDialog = gtk_message_dialog_new(GTK_WINDOW(mainWindowApp), (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                        GTK_MESSAGE_WARNING, GTK_BUTTONS_OK,
                                        _("Run configuration to set default directory/folder browser:\n\"%s\""), address);
      gtk_dialog_run (GTK_DIALOG(okDialog));
      gtk_widget_destroy(GTK_WIDGET(okDialog));
      return -1;
    }
    executable = g_key_file_get_string (keyString, "configuration", "configFileExplorer", NULL);
    attributes = g_key_file_get_string (keyString, "configuration", "configFileExplorerAttributes", NULL);
    break;
  default:
    return -1;
  }

  /* Execute the command (replacing %f with filename) */
  s1 = replaceAttributeString(attributes, address);
  if (s1 != NULL) {
    s2 = g_strconcat(executable, s1, NULL);
    g_free(s1);
    if (s2 != NULL) {
      g_spawn_command_line_async (s2, NULL);
      g_free(s2);
      retVal = TRUE;
    }
  }
 
  /* Clean up */
  g_free(executable);
  g_free(attributes);
  return retVal;
}

/*
 * Callback helper: wrapper for on_about1_activate.
 * Allows URL link (in about box) to call web link when clicked.
 */
void
openUrlLinkFunc (GtkAboutDialog *about, const gchar *link, gpointer data)
{
  /* TODO: Call up configuration if syscall returns FALSE */
  SMsyscall(link, BROWSER_LIST);
}


/*
 * Internal helper: converts %f/%d charactors within rawString to supplied replacement string
 * Returned string must be free'd.
 */
gchar *replaceAttributeString(gchar *rawString, const gchar *replacement)
{
  GString *s1 = g_string_new (rawString);
  gchar *pRawString = rawString;
  gchar *ptrAttribute;
  gssize len = g_strlen(rawString);
  gint pos;

  ptrAttribute = g_strstr_len (pRawString, len, "%");
  pos = (ptrAttribute - pRawString);
  if (ptrAttribute != NULL) {
      switch (*(ptrAttribute + 1)) {
	  case 'f': /* Filename */
	  case 'd': /* Directory */
	      g_string_erase(s1, pos, 2);
	      g_string_insert(s1, pos, replacement);
	      break;
          default:
              g_printf("Warning! Unexpected field %%%c supplied in executable argument.\n", *(ptrAttribute + 1));
              break;
      }
  } else {
      if (rawString[len - 1] != ' ') {
	  g_string_append(s1, " ");
      }
      g_string_insert(s1, pos, replacement);
  }

  if (rawString[0] != ' ') {
      g_string_prepend(s1, " ");
  }

  return g_string_free (s1, FALSE);  
}


/*
 * Callback helper: start new instance of searchmonkey in the background.
 * TODO: Save currently typed, but not executed searches (i.e. non stored searches)
 * TODO: Save window size/column search data, etc.
 */
void spawnNewSearchmonkey(void)
{
  GtkWidget *widget = GTK_WIDGET(mainWindowApp);
  gchar *searchmonkeyExe = g_object_get_data(G_OBJECT(mainWindowApp), "argvPointer");
  GKeyFile *keyString = getGKeyFile(widget);

  unrealize_searchNotebook(widget); /* Store all search settings to the keyString */
  unrealize_menubar(widget);
  storeGKeyFile(keyString); /* Attempt to save config first */
  g_spawn_command_line_async (searchmonkeyExe, NULL); /* Assume it worked */
}

/*
 * Internal helper: equivalent to mkdir -p on linux.
 * Will recursively create missing folders to make the required foldername, and mode.
 * Always returns true!
 */
gboolean mkFullDir(gchar *folderName, gint mode)
{
  gchar *partialFolderName, *pPartialFolderName;
  gchar **folderParts;
  gint i = 0;
  
  /* Completely split folderName into parts */
  folderParts = g_strsplit_set(folderName, G_DIR_SEPARATOR_S, -1);
  partialFolderName = g_strdup(folderName);
  pPartialFolderName = partialFolderName;

  while (folderParts[i] != NULL) {
    pPartialFolderName = g_stpcpy(pPartialFolderName, folderParts[i]);
    pPartialFolderName = g_stpcpy(pPartialFolderName, G_DIR_SEPARATOR_S);
    
    if (!g_file_test (partialFolderName, G_FILE_TEST_IS_DIR)) {
      g_mkdir(partialFolderName, mode);
    }
    i++;
  }
  
  return TRUE;
}


/*
 * callback helper: deletes the filename currently selected in the results table
 * If the user accepts, then file is deleted from disk, and removed from table too.
 * If row is not selected, then error message is displayed.
 */
void deleteFile(GtkWidget *widget)
{
  GtkTreeView *treeView;
  gchar *fullFileName;
  GtkTreeSelection *selection;
  GtkTreeIter child_iter, iter;
  GtkWidget *dialog;
  GtkTreeModel *model;
  GKeyFile *keyString = getGKeyFile(widget);
  GtkListStore *store;

  if (getResultsViewHorizontal(widget)) {
    treeView = GTK_TREE_VIEW(lookup_widget(widget, "treeview1"));
  } else {
    treeView = GTK_TREE_VIEW(lookup_widget(widget, "treeview2"));
  }
  model = gtk_tree_view_get_model(treeView);
  selection = gtk_tree_view_get_selection(treeView); /* get selection */

  g_assert(model != NULL);
  g_assert(selection != NULL);

  /* Get the line currently pointed at */
  if (!gtk_tree_selection_get_selected (selection, &model, &iter)) {
    dialog = gtk_message_dialog_new (GTK_WINDOW(lookup_widget(widget, "window1")),
                                     (GTK_DIALOG_DESTROY_WITH_PARENT | GTK_DIALOG_MODAL),
                                     GTK_MESSAGE_INFO,
                                     GTK_BUTTONS_OK,
                                     _("Cannot delete file as name was not selected."),
                                     NULL);
    gtk_dialog_run (GTK_DIALOG (dialog));
    gtk_widget_destroy(dialog);
    return;
  }

  /* Get underlying model from the sort model */
  gtk_tree_model_get (model, &iter,  FULL_FILENAME_COLUMN, &fullFileName, -1);
  gtk_tree_model_sort_convert_iter_to_child_iter (GTK_TREE_MODEL_SORT(model), &child_iter, &iter);
  store = GTK_LIST_STORE(gtk_tree_model_sort_get_model(GTK_TREE_MODEL_SORT(model)));

  /* Prompt (or don't) and delete file */
  if (fullFileName != NULL) {
    if (g_key_file_has_key(keyString, "configuration", "configPromptDelete", NULL) &&
        (!g_key_file_get_boolean(keyString, "configuration", "configPromptDelete", NULL))) {
      g_remove (fullFileName);
      gtk_list_store_remove(store, &child_iter);
    } else {
      dialog = gtk_message_dialog_new (GTK_WINDOW(lookup_widget(widget, "window1")),
                                       (GTK_DIALOG_DESTROY_WITH_PARENT | GTK_DIALOG_MODAL),
                                       GTK_MESSAGE_WARNING,
                                       GTK_BUTTONS_OK_CANCEL,
                                       _("Are you sure you want to delete:\n\t'%s'"),
                                       fullFileName);
      if (gtk_dialog_run (GTK_DIALOG (dialog)) == GTK_RESPONSE_OK) {
        g_remove (fullFileName);
        gtk_list_store_remove(store, &child_iter);
      }
      gtk_widget_destroy (dialog);
    }
    g_free(fullFileName);
  }
}

/*
 * callback helper: copies the full filename currently selected to clipboard
 * If row is not selected, then error message is displayed.
 * TODO: Allow users to configure which clipboard(s) are used (Linux only).
 */
void copyFile(GtkWidget *widget)
{
  GtkTreeView *treeView;
  gchar *fullFileName;
  GtkClipboard *clipboard = gtk_clipboard_get(GDK_SELECTION_CLIPBOARD);
  GtkWidget *dialog;

  if (getResultsViewHorizontal(widget)) {
    treeView = GTK_TREE_VIEW(lookup_widget(widget, "treeview1"));
  } else {
    treeView = GTK_TREE_VIEW(lookup_widget(widget, "treeview2"));
  }

  fullFileName = getFullFileName(treeView, FULL_FILENAME_COLUMN);

  if (fullFileName != NULL) {
    gtk_clipboard_set_text (clipboard, fullFileName, -1);
    g_free(fullFileName);
  } else {
    dialog = gtk_message_dialog_new (GTK_WINDOW(lookup_widget(widget, "window1")),
                                     (GTK_DIALOG_DESTROY_WITH_PARENT | GTK_DIALOG_MODAL),
                                     GTK_MESSAGE_INFO,
                                     GTK_BUTTONS_OK,
                                     _("Cannot copy full file name as name was not selected."),
                                     NULL);
    gtk_dialog_run (GTK_DIALOG (dialog));
    gtk_widget_destroy(dialog);
  }  
}



/*
 * internal helper: Wrapper function for g_file_get_contents()
 * In addition to calling GTK library, the resulting text, if valid
 * is converted to UTF-8 from its native format (usually UTF-8 also)
 * to remove illegal charactors
 * Luc A. 29 dec 2017 : this function is called from search.c module by the function 
 * phaseTwoSearch()
 * perhaps I can add here a parser/decoder for doc-x and ODT files ?
 * in current lINUX distros, we dont' have to use g_str_escape() so I commented the code
 *
 */
gboolean g_file_get_contents2 (const gchar *filename,
                               gchar **contents,
                               gsize *length,
                               GError **error)
{
  gboolean retVal;
  gchar *retContents;
  
  retVal = g_file_get_contents(filename, contents, length, error);
  // printf("++++ je suis dans g_file_contents2() ****\n");
  return retVal;
  /* Remove unknown ASCII */
/* bugged : remove all accented chars in modern Linux, so i've commented, but leaved the code for documentation
Luc A., 29 dec 2017
  if (retVal) {
    retContents = g_strescape(*contents, "\"\\\t\n\b\f\r");
printf("contenu nettoyé:%s\n",retContents);
    g_free(*contents);
    *contents = retContents;
    *length = g_strlen(retContents);
  }

  return retVal;*/

#ifdef NOTYET
/*
 * Eventually, replace above with below.
 * Currently this code will causes seg. faults on certain files (e.g. with non-ASCII chars in).
 * Exact cause needs to be investigated, but is likely to be due to regex library requirements.
 */
  gboolean retVal = TRUE;
  gchar *retContents;
  gsize bytes_read;
  gsize bytes_written;
  gchar *charset;

  /* Use built-in text file open utility to read raw text */
  g_get_charset ((G_CONST_RETURN char **)&charset);
  if (g_file_get_contents(filename, contents, length, error)) {
    retContents = g_convert_with_fallback (*contents, *length, "UTF-8", charset,
                                           NULL, &bytes_read, &bytes_written, error);
    g_free(*contents);
    if (retContents == NULL) {
      *contents = NULL;
      *length = 0;
      retVal = FALSE;
    } else {
      *contents = retContents;
      *length = bytes_written;
    }
  }
printf("*** notyet ****\n");
  return retVal;
#endif /* NOTYET */
}


/*
 * internal helper: Equivalent function to g_file_set_contents() only available in library 2.8.x
 * Converts UTF-8 (internal) to locale format (usually UTF-8 too on modern systems) and writes
 * contents (assumed text) into the supplied filename. All errors are returned via error.
 * TODO: Check for library 2.8.x and if present, use the built-in g_file_set_contents command.
 */
gboolean g_file_set_contents2 (const gchar *filename,
                               const gchar *contents,
                               gssize length,
                               GError **error)
{
  FILE *fileSD;
  gssize wrote_size;
  gboolean retVal = TRUE;
  gchar *charset;
  gsize bytes_read;
  gsize bytes_written;
  gchar *cContents; /* Contents, but coverted to the locale */

  if (length == -1) {
    length = g_strlen(contents);
    if (length < 0) {
      set_internal_error(error, errno);
      return FALSE;
    }
  }
  
  fileSD = g_fopen(filename, "w");
  if (fileSD == NULL) {
    set_internal_error(error, errno);
    return FALSE;
  }
  
  g_get_charset ((G_CONST_RETURN char **)&charset); /* Get charactor set locale */
  cContents = g_convert_with_fallback (contents, length, charset, "UTF-8",
                                       NULL, &bytes_read, &bytes_written, error);
  if (cContents == NULL) {
    retVal = FALSE;
  } else {
    wrote_size = fwrite(cContents, 1, bytes_written, fileSD);
    g_free(cContents);
    if (bytes_written != wrote_size) {
      set_internal_error(error, errno);
      retVal = FALSE;
    }
  }

  fclose(fileSD);
  
  return retVal;
}
/*
 * internal helper: Given filesystem locale error number, convert to GError format.
 */
void set_internal_error(GError **error, const gint err_no)
{
  GQuark domain = g_quark_from_string(PACKAGE "Error");
  GFileError  gerrno = g_file_error_from_errno (err_no);
  gchar *gstrerr = strerror(err_no);

  g_assert (gstrerr != NULL);
  
  if (error != NULL) {
    g_assert (*error == NULL);
    *error = g_error_new_literal(domain, gerrno, gstrerr);
  }
}


/*
 * internal helper: Folder name clean up function (only tested on Linux - but should work for other OS too).
 * Attempts to fix hand typed folder names e.g. "/foo//test/" becomes "/foo/test"
 * TODO: Simplify function - should be const gchar * input, not combobox
 * Return: NULL on error, or newly allocated (and cleaned) folder name
 */
gchar *comboBoxReadCleanFolderName(GtkComboBox *combobox)
{
#if 1 /* Creating simplified function as over-complicated */
 
  gchar *tmpStr[2];
  gchar *retStr = NULL;
  
  /* Retrieve look-in directory, ensuring terminated with (extra?) directory separator */
  tmpStr[0] = gtk_combo_box_get_active_text(combobox);
  g_assert(tmpStr[0] != NULL);
  tmpStr[1] = g_strconcat(tmpStr[0], G_DIR_SEPARATOR_S, NULL);
  g_assert(tmpStr[1] != NULL);

  /* Expects absolute folder names e.g. "/usr/bin/" or "C:\WINNT\" */
  if ((*tmpStr[0] == '\0') || (g_unichar_isspace(*tmpStr[0]))) { /* If empty string or space */
    retStr = g_strdup(g_get_home_dir());
  } else if (g_path_is_absolute(tmpStr[1])) { /* If absolute folder/file name */
    if (g_file_test(tmpStr[0], G_FILE_TEST_IS_DIR)) { /* Already a folder, so lets just clean it up */
      retStr = g_path_get_dirname(tmpStr[1]);    
    } else { /* Otherwise, not looking great, but give it one more shot.. */
      retStr = g_path_get_dirname(tmpStr[0]);
      if (!g_file_test(retStr, G_FILE_TEST_IS_DIR)) { /* Still no-where near being a folder name so exit */
        g_free(retStr);
        return NULL;
      }
    }
  } else { /* not absolute string, so try best to fix */
    if ((tmpStr[0][0] == '.') && (tmpStr[0][1] == *G_DIR_SEPARATOR_S)) {
      g_free(tmpStr[1]);
      tmpStr[1] = g_strconcat(g_get_current_dir(), &tmpStr[0][2], NULL);
      if (g_file_test(tmpStr[1], G_FILE_TEST_IS_DIR)) {
        retStr = strdup(tmpStr[1]);
      }
    } else if ((tmpStr[0][0] == '~') && (tmpStr[0][1] == *G_DIR_SEPARATOR_S)) {
      g_free(tmpStr[1]);
      tmpStr[1] = g_strconcat(g_get_home_dir(), &tmpStr[0][2], NULL);
      if (g_file_test(tmpStr[1], G_FILE_TEST_IS_DIR)) {
        retStr = strdup(tmpStr[1]);
      }
    }
  }

  /* Clean exit */
  g_free(tmpStr[0]);
  g_free(tmpStr[1]);
  return retStr;
#else
  gchar *retStr = NULL;
  gchar *tmpStr[3];
  gchar **test, **test2;
  tmpStr[0] = gtk_combo_box_get_active_text(combobox);

  /* Test that it is a proper filename */
  if (g_file_test (tmpStr[0], G_FILE_TEST_IS_DIR)) {
    if (g_path_is_absolute (tmpStr[0])) {
      retStr = g_filename_display_name(tmpStr[0]);
    } else {
      tmpStr[2] = g_filename_display_name(tmpStr[0]);
      if (tmpStr[2] != NULL) {
        retStr = g_build_path(G_DIR_SEPARATOR_S, g_get_current_dir(), tmpStr[2], NULL);
        g_free(tmpStr[2]);
      }
    }
  }
  if (retStr == NULL) {
    return tmpStr[0]; /* Failed! */
  }
  g_free(tmpStr[0]);
  
  /* Split directory into parts */
  test = g_strsplit (retStr, G_DIR_SEPARATOR_S, -1);
  if (test == NULL) {
    g_print(_("%s Error! Unable to split %s with %c\n"), __func__, retStr, G_DIR_SEPARATOR_S);
    return retStr;
  }
  g_assert(test != NULL);
  
  /* Clean up any of the extra path separators */
  test2 = strvdup(test);
  g_strfreev(test);
  if (test2 == NULL){
    g_print(_("%s Error! Unable to duplicate string vector\n"), __func__);
    return retStr;
  }
  g_free(retStr);

  /* Recreate the cleaned up path name */
  tmpStr[1] = g_strjoinv (G_DIR_SEPARATOR_S, test2);  
  g_strfreev(test2);

  return tmpStr[1];
#endif /* Replaced code.. */
}

/*
 * internal helper: local duplicate of GLIB function g_strdupv
 * TODO: Replace occurances with g_strdupv -- may require minor adjustments to
 *       comboBoxReadCleanFolderName() function as it caused errors previously.
 */
gchar **strvdup(gchar **strv)
{
  gchar **retVal;
  gint i = 0, j = 0;

  g_assert(strv != NULL);
  
  retVal = malloc(sizeof(gchar *) * (1 + g_strv_length(strv)));

  retVal[j++] = g_strdup(strv[i++]); /* Always copy first element */
  while (strv[i] != NULL) {
    if (*strv[i] != '\0') {
      retVal[j++] = g_strdup(strv[i]);
    }
    i++;
  }
  retVal[j] = NULL; /* NULL terminate */

  return retVal;
}
