/*
 * File: misc.c
 * Description: Contains helpere functions, and everything that doesn't fit elsewhere
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <gtk/gtk.h>
#include <glib.h>

#include "interface.h" /* glade requirement */
#include "support.h" /* glade requirement */
#include "misc.h"
#include <regex.h>


/*
 * callback/internal helper: returns the text stored within supplied columnNumber
 * Return string must be free'd.
 * TODO: convert column number to enumerator for table view results.
 * TODO: Consider renaming function to be more specific
 */
gchar *getFullFileName(GtkTreeView *treeView, gint columnNumber)
{
  GtkTreeSelection *selection = gtk_tree_view_get_selection(treeView); /* get selection */
  gchar *myString;
  GtkTreeIter iter;
  GtkTreeModel *model;

  if (gtk_tree_selection_get_selected (selection, &model, &iter)) {
    g_assert (model != NULL);
    gtk_tree_model_get (model, &iter,  columnNumber, &myString, -1);
    return myString;
  }
  return NULL;
}


/* Initializes Combo Box having two models */
void initComboBox2(GtkWidget *comboBox)
{
  GtkListStore* store;
  gint* setActive;


  store = gtk_list_store_new (1, G_TYPE_STRING); /* Create a simple 1-wide column */
  g_object_set_data(G_OBJECT(comboBox), "noregex", store);
  setActive = (gint *)g_malloc(sizeof(gint));
  *setActive = -1;
  g_object_set_data_full(G_OBJECT(comboBox), "noregex-active", (gpointer)setActive, &g_free);
  store = gtk_list_store_new (1, G_TYPE_STRING); /* Create a simple 1-wide column */
  g_object_set_data(G_OBJECT(comboBox), "regex", store);
  setActive = (gint *)g_malloc(sizeof(gint));
  *setActive = -1;
  g_object_set_data_full(G_OBJECT(comboBox), "regex-active", (gpointer)setActive, &g_free);
  gtk_combo_box_set_model(GTK_COMBO_BOX(comboBox), GTK_TREE_MODEL(store));
}


/*
 * Callback helper: intialise generic combo box with new model, and single width column storage
 */
void initComboBox(GtkWidget *comboBox)
{
  GtkListStore *store = gtk_list_store_new (1, G_TYPE_STRING); /* Create a simple 1-wide column */
  gtk_combo_box_set_model (GTK_COMBO_BOX(comboBox), GTK_TREE_MODEL(store));
  g_object_unref(store);
}

/*
 * Callback helper: clear all text entries (except active one) from a combo box with two models.
 */
void clearComboBox2(GtkWidget *comboBox)
{
    GtkTreeIter iter;
    gchar *readString;
    GtkListStore *store = GTK_LIST_STORE(gtk_combo_box_get_model (GTK_COMBO_BOX(comboBox)));
    GtkListStore *store2 = GTK_LIST_STORE(g_object_get_data(G_OBJECT(comboBox), "regex"));
    gchar * clearActive = "regex-active";
    gchar * zeroActive = "noregex-active";

    g_assert(store != NULL);
    g_assert(store2 != NULL);

    if (!gtk_combo_box_get_active_iter (GTK_COMBO_BOX(comboBox), &iter)) {
        return;
    }
    
    gtk_tree_model_get (GTK_TREE_MODEL(store), &iter,
                        0, &readString,
                        -1);
    gtk_list_store_clear (GTK_LIST_STORE(store));
    if (readString != NULL) {
        gtk_list_store_prepend (store, &iter);
        gtk_list_store_set (store, &iter,
                            0, readString,
                            -1);
        g_free(readString);
        gtk_combo_box_set_active_iter (GTK_COMBO_BOX(comboBox), &iter);
    }

    if (store == store2) {
      clearActive = "noregex-active";
      zeroActive = "regex-active";
      store2 = GTK_LIST_STORE(g_object_get_data(G_OBJECT(comboBox), "noregex"));
      g_assert(store2 != NULL);
    }
    gtk_list_store_clear(store2);

    gint * storeActive = (gint *)g_malloc(sizeof(gint));
    *storeActive = -1;
    g_object_set_data_full(G_OBJECT(comboBox), clearActive, storeActive, g_free);

    storeActive = (gint *)g_malloc(sizeof(gint));
    *storeActive = 0;
    g_object_set_data_full(G_OBJECT(comboBox), zeroActive, storeActive, g_free);
}


/*
 * Callback helper: clear all text entries (except active one) from a generic combo box model.
 */
void clearComboBox(GtkWidget *comboBox)
{
    GtkTreeIter iter;
    gchar *readString;
    GtkListStore *store = GTK_LIST_STORE(gtk_combo_box_get_model (GTK_COMBO_BOX(comboBox)));

    g_assert(store != NULL);

    if (!gtk_combo_box_get_active_iter (GTK_COMBO_BOX(comboBox), &iter)) {
        return;
    }
    
    gtk_tree_model_get (GTK_TREE_MODEL(store), &iter,
                        0, &readString,
                        -1);
    gtk_list_store_clear (GTK_LIST_STORE(store));
    if (readString != NULL) {
        gtk_list_store_prepend (store, &iter);
        gtk_list_store_set (store, &iter,
                            0, readString,
                            -1);
        g_free(readString);
        gtk_combo_box_set_active_iter (GTK_COMBO_BOX(comboBox), &iter);
    }
}


/*
 * Callback/internal helper: add a text entry to a combo box model, making sure that no duplicates exist
 * Duplicate entries cause existing entry to be re-displayed - transparant to users.
 * TODO: replace strcmp function with GTK equivalent for UTF-8 strings
 */
gboolean addUniqueRow(GtkWidget *comboBox, const gchar *entry)
{
    GtkTreeIter iter;
    gchar *readString;
    GtkListStore *store = GTK_LIST_STORE(gtk_combo_box_get_model (GTK_COMBO_BOX(comboBox)));

    g_assert(store != NULL);

	/* Test for NULL/Empty using GTK API */
    if ((entry == NULL) || g_ascii_isspace(entry[0])) {
      return TRUE;
    }

    /* Find first, and then loop through all until duplicate found */
    if (gtk_tree_model_get_iter_first (GTK_TREE_MODEL(store), &iter)) {
        do {
            gtk_tree_model_get (GTK_TREE_MODEL(store), &iter,
                                0, &readString,
                                -1);
            if (strcmp(readString, entry) == 0) {
                g_free(readString);
                gtk_combo_box_set_active_iter (GTK_COMBO_BOX(comboBox), &iter);
                return FALSE;
            }
            g_free(readString);    
        } while (gtk_tree_model_iter_next(GTK_TREE_MODEL(store), &iter));
    }

/* Else unique, so add a new row to the model */
    gtk_list_store_prepend (store, &iter);
    gtk_list_store_set (store, &iter,
                        0, entry,
                        -1);
    gtk_combo_box_set_active_iter (GTK_COMBO_BOX(comboBox), &iter);
    return TRUE;
}


/*
 * Low-tech replacement for standard strlen library for counting string bytes, assuming NULL terminated.
 * Note that "g_utf8_strlen" should be used if *character* count is required, as UTF8 chars are often multibyte
 */
gint g_strlen(const gchar *string)
{
  gint i = 0;
  while (string[i] != '\0') {
    i++;
  }
  return i;
}


/*
 * Internal convenience function to copy info across between basic and advanced modes
 */
void copySettings(GtkWidget *widget, gboolean expertMode)
{
    /* in each, index zero is for the basic mode, and 1 is for advanced mode*/
    gint source = 0;
    gint target = 1;
    GtkWidget* filename[2];
    GtkWidget* containingText[2];
    GtkToggleButton* containingTextCheck[2];
    GtkToggleButton* recursive[2];
    GtkWidget* lookin[2];
    gchar *tmpString;
    guint tmpFlags = 0;

    filename[0] = lookup_widget(widget, "fileName2");
    filename[1] = lookup_widget(widget, "fileName");
    containingText[0] = lookup_widget(widget, "containingText2");
    containingText[1] = lookup_widget(widget, "containingText");
    containingTextCheck[0] = GTK_TOGGLE_BUTTON(lookup_widget(widget, "containingTextCheck2"));
    containingTextCheck[1] = GTK_TOGGLE_BUTTON(lookup_widget(widget, "containingTextCheck"));
    recursive[0] = GTK_TOGGLE_BUTTON(lookup_widget(widget, "searchSubfoldersCheck2"));
    recursive[1] = GTK_TOGGLE_BUTTON(lookup_widget(widget, "searchSubfoldersCheck"));
    lookin[0] = lookup_widget(widget, "lookIn2");
    lookin[1] = lookup_widget(widget, "lookIn");

    if (expertMode == FALSE) {
	source = 1;
	target = 0;
    }
    tmpString = gtk_combo_box_get_active_text(GTK_COMBO_BOX(filename[source]));
    if (getExtendedRegexMode(widget)) {
      tmpFlags |= REG_EXTENDED;
    }
    if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(widget, "regularExpressionRadioFile")))) {
      if (test_regexp(tmpString, tmpFlags, _("Error! Invalid File Name regular expression"))) {
        addUniqueRow(filename[target], tmpString);
        addUniqueRow(filename[source], tmpString);
      }
    }
    g_free(tmpString);
    
    gtk_toggle_button_set_active(containingTextCheck[target],gtk_toggle_button_get_active(containingTextCheck[source]));
    
    tmpString = gtk_combo_box_get_active_text(GTK_COMBO_BOX(containingText[source]));
    if (test_regexp(tmpString, tmpFlags, _("Error! Invalid Containing Text regular expression"))) {
      addUniqueRow(containingText[target], tmpString);
      addUniqueRow(containingText[source], tmpString);
    }
    g_free(tmpString);

    tmpString = gtk_combo_box_get_active_text(GTK_COMBO_BOX(lookin[source]));
    if (validate_folder(tmpString)) {
      addUniqueRow(lookin[target], tmpString);
      addUniqueRow(lookin[source], tmpString);
    }
    g_free(tmpString);

    gtk_toggle_button_set_active(recursive[target], gtk_toggle_button_get_active(recursive[source]));
}


/*
 * Callback helper: Switch mode between expert/beginner
 */
void setExpertSearchMode (GtkWidget *widget, gboolean expertMode)
{
  copySettings(widget, expertMode);
}


/*
 * Callback helper: Return TRUE if in expert mode
 */
gboolean getExpertSearchMode (GtkWidget *widget)
{
  GtkWidget *searchNotebook = lookup_widget(widget, "searchNotebook");
  if (gtk_notebook_get_current_page(GTK_NOTEBOOK(searchNotebook)) == 1) {
    return TRUE;
  } else {
    return FALSE;
  }
}

void sel (GtkTreeModel *model, GtkTreePath *path,GtkTreeIter *iter, gpointer data)
{
  gtk_tree_selection_select_path((GtkTreeSelection *)data, path);
}

/*
 * Callback helper: Switch between horizontal and vertical results display
 */
void setResultsViewHorizontal(GtkWidget *widget, gboolean horizontal)
{
  GtkTreeSelection *treeview1 = gtk_tree_view_get_selection(GTK_TREE_VIEW(lookup_widget(widget, "treeview1")));
  GtkTreeSelection *treeview2 = gtk_tree_view_get_selection(GTK_TREE_VIEW(lookup_widget(widget, "treeview2")));

  if (horizontal) {
    gtk_widget_show(lookup_widget(widget, "resultsHPane"));
    gtk_widget_hide(lookup_widget(widget, "resultsVPane"));
    gtk_tree_selection_selected_foreach(treeview2, &sel, treeview1);
  } else { /* Vertical - default */
    gtk_widget_hide(lookup_widget(widget, "resultsHPane"));
    gtk_widget_show(lookup_widget(widget, "resultsVPane"));
    gtk_tree_selection_selected_foreach(treeview1, &sel, treeview2);
  }
}


/*
 * Callback helper: Return TRUE if horizontal, else vertical display
 */
gboolean getResultsViewHorizontal (GtkWidget *widget)
{
  GtkWidget *menuitem = lookup_widget(widget, "horizontal_results1");
  return (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem)));
}


/* 
 * Callback helper: change the model for filename combo boxes when user changes regexp/glob preference
 */
void changeModel(GtkWidget *widget, const gchar * from, const gchar * to)
{
  gint* storeActive;
  GtkComboBox *advancedCombo = GTK_COMBO_BOX(lookup_widget(widget, "fileName"));
  GtkComboBox *basicCombo = GTK_COMBO_BOX(lookup_widget(widget, "fileName2"));
  GtkListStore *store = GTK_LIST_STORE(gtk_combo_box_get_model(advancedCombo));
  GtkListStore *setStore;

  setStore = GTK_LIST_STORE(g_object_get_data(G_OBJECT(advancedCombo), to));
  if (store != setStore) {
    gchar * setActiveKey = g_strconcat(from, "-active", NULL);
    gchar * getActiveKey = g_strconcat(to, "-active", NULL);
   
    /* Change the model */
    /* Advanced tab */
    storeActive = (gint *)g_malloc(sizeof(gint));
    *storeActive = gtk_combo_box_get_active(advancedCombo);
    g_object_set_data_full(G_OBJECT(advancedCombo), setActiveKey, (gpointer)storeActive, &g_free);
    gtk_entry_set_text(GTK_ENTRY(GTK_BIN (advancedCombo)->child), "");
    gtk_combo_box_set_model(advancedCombo, GTK_TREE_MODEL(setStore));
    gtk_combo_box_set_active(advancedCombo, *((gint *)g_object_get_data(G_OBJECT(advancedCombo), getActiveKey)));

    /* Basic tab */
    storeActive = (gint *)g_malloc(sizeof(gint));
    *storeActive = gtk_combo_box_get_active(basicCombo);

    g_object_set_data_full(G_OBJECT(basicCombo), setActiveKey, (gpointer)storeActive, &g_free);
    gtk_entry_set_text(GTK_ENTRY(GTK_BIN (basicCombo)->child), "");
    gtk_combo_box_set_model(basicCombo, GTK_TREE_MODEL(GTK_LIST_STORE(g_object_get_data(G_OBJECT(basicCombo), to))));
    gtk_combo_box_set_active(basicCombo, *((gint *)g_object_get_data(G_OBJECT(basicCombo), getActiveKey)));

    g_free((gpointer)setActiveKey);
    g_free((gpointer)getActiveKey);
  }
}

/*Callback helper: Displays a calendar popup and returns selected date */
gchar *getDate(const gchar *curDate)
{
  GtkWidget * calendarDialog = create_calendarDialog();
  GtkCalendar *calendar = GTK_CALENDAR(lookup_widget(calendarDialog, "calendar1"));
  gchar* result = g_strdup(curDate);
  guint year, month, day;
  GDate date;
  
  g_date_set_parse(&date, curDate);
    
  if (g_date_valid(&date)) {
    year = g_date_get_year(&date);
    month = g_date_get_month(&date) - 1; /*Glib has months from 1-12 while GtkCalendar uses 0 - 11 */
    day = g_date_get_day(&date);
    gtk_calendar_select_day(calendar, day);
    gtk_calendar_select_month(calendar, month, year);
  }
    
  if (gtk_dialog_run(GTK_DIALOG(calendarDialog)) == GTK_RESPONSE_OK) {

    gtk_calendar_get_date(calendar, &year, &month, &day);
    gtk_widget_destroy(calendarDialog);

    result = (gchar *)g_malloc(sizeof(gchar) * 12);
    g_date_strftime(result, 11, "%d %b %y", g_date_new_dmy(day, month +1, year));
  } else {
    gtk_widget_destroy(calendarDialog);
  }

  return result;
}


gchar *get_regerror(int errcode, regex_t *compiled)
{
  size_t length = regerror (errcode, compiled, NULL, 0);
  gchar *buffer = (gchar *)g_malloc (sizeof(gchar) * length);
  (void) regerror (errcode, compiled, buffer, length);
  return buffer;
}

gboolean test_regexp(gchar *regexp, guint flags, gchar *error)
{
  gint regerr;
  regex_t testRegEx;
  GtkWidget *dialog;
  extern GtkWidget * mainWindowApp;
  GObject *window1 = G_OBJECT(mainWindowApp);

  regerr = regcomp(&testRegEx, regexp, flags);
  if (regerr != 0) {
    gchar* errorString = get_regerror(regerr, &testRegEx);
    gchar * msg = g_strconcat(error, "\n", errorString, NULL);

    dialog = gtk_message_dialog_new (GTK_WINDOW(window1),
                                     GTK_DIALOG_DESTROY_WITH_PARENT,
                                     GTK_MESSAGE_ERROR,
                                     GTK_BUTTONS_CLOSE,
                                     msg);
    gtk_dialog_run (GTK_DIALOG (dialog));
    gtk_widget_destroy (dialog);
    g_free(errorString);
    g_free(msg);
    regfree(&testRegEx);
    return FALSE;
  } else {
    regfree(&testRegEx);
    return TRUE;
  }
}

/*
 * Used within misc, and search.c to validate that look-in folder exists and is not blank.
 * Produces pop-up error messages, and returns FALSE if folder not found.
 */
gboolean validate_folder(const gchar *folderName) {
  GtkWindow *window1 = GTK_WINDOW(mainWindowApp);
  GtkWidget *dialog;

  /* Test starting folder exists */
  if (folderName == NULL) {
    dialog = gtk_message_dialog_new (window1,
                                     GTK_DIALOG_DESTROY_WITH_PARENT,
                                     GTK_MESSAGE_ERROR,
                                     GTK_BUTTONS_CLOSE,
                                     _("Error! Look In directory cannot be blank."));
    gtk_dialog_run (GTK_DIALOG (dialog));
    gtk_widget_destroy (dialog);
    return FALSE;
  }
  
  if (!g_file_test(folderName, G_FILE_TEST_IS_DIR)) {
    dialog = gtk_message_dialog_new (window1,
                                     GTK_DIALOG_DESTROY_WITH_PARENT,
                                     GTK_MESSAGE_ERROR,
                                     GTK_BUTTONS_CLOSE,
                                     _("Error! Look In directory is invalid."));
    gtk_dialog_run (GTK_DIALOG (dialog));
    gtk_widget_destroy (dialog);
    return FALSE;
  }
  return TRUE;
}
