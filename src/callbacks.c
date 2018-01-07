#ifdef HAVE_CONFIG_H
#  include <config.h>
#endif

#include <gtk/gtk.h>
#include <gdk-pixbuf/gdk-pixbuf.h>
#include <gdk/gdk.h>
#include <gdk/gdkkeysyms.h>

#include "interface.h"
#include "support.h"
#include "search.h"
#include "lgpl.h" /* Lesser GPL license*/
#include "savestate.h" /* Allows config.ini to save/restore state*/
#include "regexwizard.h" /* Add support for regular expression wizard */
#include "systemio.h" /* System stuff, file import and export code */
#include "misc.h" /* Everything else */
#include "systemio.h" /* Add structure */
#include "callbacks.h"


/* Top level definitions */
GtkWidget *testRegExDialog1 = NULL;
GtkWidget *fileRegexWizard = NULL;
GtkWidget *contextRegexWizard = NULL;

void
on_window1_destroy                     (GtkObject       *object,
                                        gpointer         user_data)
{
  gtk_main_quit();
}


void
on_open_criteria1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  importCriteria(GTK_WIDGET(menuitem));
}


void
on_save_criteria1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  exportCriteria(GTK_WIDGET(menuitem));
}


void
on_save_results1_activate              (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  saveResults(GTK_WIDGET(menuitem));
}


void
on_print1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_print_preview1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_print_setup1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_quit1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GObject *window1 = G_OBJECT(lookup_widget(GTK_WIDGET(menuitem), "window1"));
  gtk_object_destroy(GTK_OBJECT(window1));
}


void
on_word_wrap1_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
    gboolean setWordWrap;
    GtkTextView *textBox;
    if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
      textBox = GTK_TEXT_VIEW(lookup_widget(GTK_WIDGET(menuitem), "textview1"));
    } else {
      textBox = GTK_TEXT_VIEW(lookup_widget(GTK_WIDGET(menuitem), "textview4"));
    }
    
    setWordWrap = gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem));
    gtk_text_view_set_wrap_mode(textBox, setWordWrap);
}


void
on_set_font1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
    gchar *newFont;
    GtkWidget *textView;
    if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
      textView = lookup_widget(GTK_WIDGET(menuitem), "textview1");
    } else {
      textView = lookup_widget(GTK_WIDGET(menuitem), "textview4");
    }
    
    GtkFontSelectionDialog *dialog = GTK_FONT_SELECTION_DIALOG(create_fontSelectionDialog());
    PangoContext* context = gtk_widget_get_pango_context  (textView);
    PangoFontDescription *desc = pango_context_get_font_description(context);    
    newFont = pango_font_description_to_string(desc);
    
    gtk_font_selection_dialog_set_font_name(dialog, newFont);
    if (gtk_dialog_run (GTK_DIALOG (dialog)) == GTK_RESPONSE_OK) {
      newFont = gtk_font_selection_dialog_get_font_name(dialog);
      if (newFont != NULL) { 
        desc = pango_font_description_from_string (newFont);
        if (desc != NULL) {
          gtk_widget_modify_font (GTK_WIDGET(textView), desc);
          pango_font_description_free(desc);
        }
        g_free(newFont);
      }
    }
    gtk_widget_destroy(GTK_WIDGET(dialog));
}


void
on_set_highligting_colour1_activate    (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
    GdkColor color, *cp;
    GtkTextView *textView;
    if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
      textView = GTK_TEXT_VIEW(lookup_widget(GTK_WIDGET(menuitem), "textview1"));
    } else {
      textView = GTK_TEXT_VIEW(lookup_widget(GTK_WIDGET(menuitem), "textview4"));
    }
    GtkTextBuffer* textBuf = gtk_text_view_get_buffer (textView);
    GtkTextTagTable* tagTable = gtk_text_buffer_get_tag_table(textBuf);
    GtkTextTag* tag = gtk_text_tag_table_lookup(tagTable, "word_highlight");
    GtkWidget *dialog = create_highlightColourDialog();
    GtkColorSelection *colorsel = GTK_COLOR_SELECTION(lookup_widget(dialog, "color_selection1"));

    g_assert(textView != NULL);
    g_assert(textBuf != NULL);
    g_assert(tagTable != NULL);
    g_assert(tag != NULL);
    g_assert(dialog != NULL);
    g_assert(colorsel != NULL);
    
    g_object_get( G_OBJECT(tag), "foreground-gdk", &cp, NULL);
    gtk_color_selection_set_current_color(colorsel, cp);
    gdk_color_free(cp);
    if (gtk_dialog_run (GTK_DIALOG (dialog)) == GTK_RESPONSE_OK) {
        GtkColorSelection *colorsel = GTK_COLOR_SELECTION(lookup_widget(dialog, "color_selection1"));
        gtk_color_selection_get_current_color(colorsel, &color);
        g_object_set( G_OBJECT(tag), "foreground-gdk", &color, NULL);
    }
    gtk_widget_destroy(dialog);
}


void
on_cl_ear_history1_activate            (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
    GtkToggleButton *check;
    GtkComboBox *combo;
    gchar *activeText;
    GtkEntry *entry;
    GtkWidget *dialog = create_clearSearchHistoryDialog();

    if (gtk_dialog_run (GTK_DIALOG (dialog)) == GTK_RESPONSE_OK) {
        /* Clear file names? */
        check = GTK_TOGGLE_BUTTON(lookup_widget(dialog, "clearFileNamesCheck"));
        if(gtk_toggle_button_get_active(check)) {
          clearComboBox2(lookup_widget(GTK_WIDGET(menuitem), "fileName"));
          clearComboBox2(lookup_widget(GTK_WIDGET(menuitem), "fileName2"));
        }

        /* Clear containing text? */
        check = GTK_TOGGLE_BUTTON(lookup_widget(dialog, "clearContainingTextCheck"));
        if(gtk_toggle_button_get_active(check)) {
            clearComboBox(lookup_widget(GTK_WIDGET(menuitem), "containingText"));
            clearComboBox(lookup_widget(GTK_WIDGET(menuitem), "containingText2"));
        }

        /* Clear look in? */
        check = GTK_TOGGLE_BUTTON(lookup_widget(dialog, "clearLookInCheck"));
        if(gtk_toggle_button_get_active(check)) {
            clearComboBox(lookup_widget(GTK_WIDGET(menuitem), "lookIn"));
            clearComboBox(lookup_widget(GTK_WIDGET(menuitem), "lookIn2"));
        }

        /* Clear size/modified? */
        check = GTK_TOGGLE_BUTTON(lookup_widget(dialog, "resetSizeModifiedCheck"));
        if(gtk_toggle_button_get_active(check)) {
            gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(menuitem), "lessThanCheck")), FALSE);
            gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(menuitem), "moreThanCheck")), FALSE);
            gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(menuitem), "afterCheck")), FALSE);
            gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(menuitem), "beforeCheck")), FALSE);

            entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(menuitem), "lessThanEntry"));
            gtk_entry_set_text(entry, "");
            entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(menuitem), "moreThanEntry"));
            gtk_entry_set_text(entry, "");
            entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(menuitem), "afterEntry"));
            gtk_entry_set_text(entry, "");
            entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(menuitem), "beforeEntry"));
            gtk_entry_set_text(entry, "");
        }
    }
    gtk_widget_destroy(dialog);
}


void
on_delete1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  deleteFile (GTK_WIDGET(menuitem));
}


void
on_copy2_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  copyFile (GTK_WIDGET(menuitem));
}


void
on_toolbar2_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
    GtkWidget *widget = lookup_widget(GTK_WIDGET(menuitem), "toolbar1");
    if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
        gtk_widget_show(widget);
    } else {
        gtk_widget_hide(widget);
    }
}


void
on_status_bar1_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GtkWidget *widget = lookup_widget(GTK_WIDGET(menuitem), "hbox41"); /* Contains status plus progress bar */
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    gtk_widget_show(widget);
  } else {
    gtk_widget_hide(widget);
  }
}


void
on_file_name1_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    columnClick(GTK_WIDGET(menuitem), FILENAME_COLUMN);
  }
}


void
on_location1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    columnClick(GTK_WIDGET(menuitem), LOCATION_COLUMN);
  }
}


void
on_size1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    columnClick(GTK_WIDGET(menuitem), INT_SIZE_COLUMN);
  }
}


void
on_type1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    columnClick(GTK_WIDGET(menuitem), TYPE_COLUMN);
  }
}


void
on_modified1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    columnClick(GTK_WIDGET(menuitem), INT_MODIFIED_COLUMN);
  }
}


void
on_search1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_configuration1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GtkWidget *dialog = create_configDialog();

  realize_configDialog(dialog);

  switch (gtk_dialog_run(GTK_DIALOG(dialog))) {
  case GTK_RESPONSE_OK:
    unrealize_configDialog(dialog);
    break;
  default:
    break;
  }
  gtk_widget_destroy(dialog);
}


void
on_test1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_reg_expression1_activate            (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (testRegExDialog1 == NULL) {
    testRegExDialog1 = create_testRegExDialog();
    gtk_widget_show(testRegExDialog1);
  } else {
    gtk_widget_grab_focus(lookup_widget(testRegExDialog1, "testEntry"));
  }
}


void
on_1_search1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_contents1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  SMsyscall(_("http://searchmonkey.sourceforge.net/index.php/SearchMonkey_User_Guide"), BROWSER_LIST);
}


void
on_support1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  SMsyscall(_("http://sourceforge.net/support/getsupport.php?group_id=175143"), BROWSER_LIST);
}


void
on_about1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GtkWidget *aboutDialog = create_aboutSearchmonkey();
  GtkWidget *tmpWidget;
  gchar *tmpString;
  PangoAttrList *list;
  PangoAttribute *attr;
  
  /* Set searchmonkey version text and font size */
  tmpWidget = lookup_widget(aboutDialog, "aboutVersion");
  tmpString = g_strdup_printf(_("searchmonkey %s"), VERSION);/* defined in "configure" line 1644 - luc A 3 janv 2018 */
  gtk_label_set_text(GTK_LABEL(tmpWidget), tmpString);
  g_free(tmpString);
  list = pango_attr_list_new(); /* Create list with 1 reference */
  attr = pango_attr_scale_new(PANGO_SCALE_X_LARGE);
  pango_attr_list_change(list, attr); /* pango_attr_list_insert */
  gtk_label_set_attributes(GTK_LABEL(tmpWidget), list);
  pango_attr_list_unref(list); /* Destroy 1 attribute, plus list */

  /* Start widget */
  gtk_widget_show(aboutDialog);
}


void
on_printResult_clicked                 (GtkButton       *button,
                                        gpointer         user_data)
{
  //    g_print("Creating XML data...\n");
  //    g_print("Printing XML data...\n");
}


void
on_Question_clicked                    (GtkButton       *button,
                                        gpointer         user_data)
{
  SMsyscall(_("http://sourceforge.net/forum/?group_id=175143"), BROWSER_LIST);
}


void
on_containingText_changed              (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
  GtkToggleButton *checkBox;

  if (getExpertSearchMode(GTK_WIDGET(combobox)) == TRUE) {
    checkBox = GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(combobox), "containingTextCheck"));
  } else {
    checkBox = GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(combobox), "containingTextCheck2"));
  }

  gchar *test = gtk_combo_box_get_active_text   (combobox);

  if (test == NULL){
    gtk_toggle_button_set_active(checkBox, FALSE);
    return;
  } else {
    if (*test == '\0') {
      gtk_toggle_button_set_active(checkBox, FALSE);
    } else {
      gtk_toggle_button_set_active(checkBox, TRUE);
    }
    g_free(test);
  }
}


void
on_folderSelector_clicked              (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkWidget *dialog;
  GtkComboBox *fileWidget;
  gint result;
  gchar *currentFolderName = gtk_combo_box_get_active_text(GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "lookIn")));
  
  dialog = create_folderChooserDialog();
  gtk_file_chooser_set_current_folder(GTK_FILE_CHOOSER(dialog), currentFolderName);
  g_free(currentFolderName);

  if (gtk_dialog_run (GTK_DIALOG (dialog)) == GTK_RESPONSE_OK) {
    char *filename;
    
    filename = gtk_file_chooser_get_filename (GTK_FILE_CHOOSER (dialog));
    if (getExpertSearchMode(GTK_WIDGET(button)) == TRUE) {
    	fileWidget = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "lookIn"));
    } else {
    	fileWidget = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "lookIn2"));
    }
    addUniqueRow(GTK_WIDGET(fileWidget), filename);
    g_free (filename);
  }
  gtk_widget_destroy (dialog);
}


void
on_lessThanCheck_toggled               (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  GtkWidget *pTextBox = lookup_widget(GTK_WIDGET(togglebutton), "lessThanEntry");
  gtk_widget_set_sensitive(pTextBox, gtk_toggle_button_get_active(togglebutton));
}


void
on_beforeCheck_toggled                 (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  GtkWidget *pTextBox = lookup_widget(GTK_WIDGET(togglebutton), "beforeEntry");
  gtk_widget_set_sensitive(pTextBox, gtk_toggle_button_get_active(togglebutton));
  gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(togglebutton), "beforeCalendarBtn"), gtk_toggle_button_get_active(togglebutton));
}


void
on_moreThanCheck_toggled               (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  GtkWidget *pTextBox = lookup_widget(GTK_WIDGET(togglebutton), "moreThanEntry");
  gtk_widget_set_sensitive(pTextBox, gtk_toggle_button_get_active(togglebutton));
}


void
on_afterCheck_toggled                  (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  GtkWidget *pTextBox = lookup_widget(GTK_WIDGET(togglebutton), "afterEntry");
  gtk_widget_set_sensitive(pTextBox, gtk_toggle_button_get_active(togglebutton));
  gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(togglebutton), "afterCalendarBtn"), gtk_toggle_button_get_active(togglebutton));
}


void
on_expertUserCheck_toggled             (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  GtkWidget *menuitem;

  if (gtk_toggle_button_get_active(togglebutton)) { /* Set expert mode */
    gtk_notebook_set_current_page(GTK_NOTEBOOK(lookup_widget(GTK_WIDGET(togglebutton), "searchNotebook")), 1);
  } else { /* Set basic mode */
    gtk_notebook_set_current_page(GTK_NOTEBOOK(lookup_widget(GTK_WIDGET(togglebutton), "searchNotebook")), 0);
  }
}


gboolean
on_treeview1_button_press_event        (GtkWidget       *widget,
                                        GdkEventButton  *event,
                                        gpointer         user_data)
{
  GtkTreeView *treeview = GTK_TREE_VIEW(widget);
  GtkTreePath *path;
  GtkTreeViewDropPosition *pos;


  GtkTreeSelection *selection = gtk_tree_view_get_selection (treeview);

  if (gtk_tree_selection_count_selected_rows(selection)<1)
    {
     printf("*** Warning, no selection ! ***\n");
     // return FALSE;/* added by Luc A., 28 dec 2017 */
    }
  /* Capture right button click */
  if ((event->button == 3) && (event->type == GDK_BUTTON_PRESS)) 
   {
    if (gtk_tree_view_get_path_at_pos (treeview,
                                       event->x, event->y,
                                       &path, NULL, NULL, NULL)) 
     {
       gtk_tree_selection_unselect_all(selection); /* added by Luc A., 28 dec 2017 */
       gtk_tree_selection_select_path (selection, path);
       if (path!=NULL) 
         {
          gtk_tree_path_free(path);
         }/* if path!=NULL */
       do_popup_menu(widget, event);
     }/* if test path at pos OK */   
    return TRUE;
   }/* endif right-click */ 
/* capture double-click */
 if ((event->button == 1) && (event->type == GDK_2BUTTON_PRESS)) 
   {
    gchar *fullFileName = getFullFileName(treeview, FULL_FILENAME_COLUMN);
    if (fullFileName != NULL) 
      {
        SMsyscall(fullFileName, TEXTEDITOR_LIST);
        g_free(fullFileName);
      }
    return TRUE;
    }/* endif double-click */

/* capture simple-left-click, i.e. select a row */
 if ((event->button == 1) && (event->type == GDK_BUTTON_PRESS)) 
   {
     if (gtk_tree_view_get_path_at_pos (treeview,event->x, event->y,
                                    &path, NULL, NULL, NULL)) 
      {
// printf("** je considère que c'est un simple-clic **\n");
             // printf("* Je suis avant le select path * \n");
             gtk_tree_selection_unselect_all(selection); /* ajout */
             // printf("* passé un-select * \n");
            if (gtk_tree_selection_count_selected_rows(selection)==1)
             {
               gtk_tree_selection_select_path (selection, path);
               // printf("* Je suis après le select path *\n");
               if (path!=NULL) 
                {
		 // printf("* avant libère mémoire *\n");
                 gtk_tree_path_free(path);
                 // printf("* après libère mémoire *\n");
                }    
              return TRUE;/* pb ici si met FALSE interdit sélections */
             }
      }
   }/* endif left-click */
 return FALSE;
}


gboolean
on_treeview1_popup_menu                (GtkWidget       *widget,
                                        gpointer         user_data)
{
  do_popup_menu(widget, NULL);
  return FALSE;
}


void
on_open1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GtkTreeView *treeView;
  gchar *fullFileName;
  
  if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview1"));
  } else {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview2"));
  }
  fullFileName = getFullFileName(treeView, FULL_FILENAME_COLUMN);

  if (fullFileName != NULL) {
    SMsyscall(fullFileName, TEXTEDITOR_LIST);
    g_free(fullFileName);
  }
  
  detachMenu(menuitem);
}


void
on_copy3_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  copyFile(GTK_WIDGET(menuitem));
    
  detachMenu(menuitem);
}


void
on_delete2_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  deleteFile (GTK_WIDGET(menuitem));
  detachMenu(menuitem);
}


void
on_explore1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GtkTreeView *treeView;
  gchar *location;
  
  if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview1"));
  } else {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview2"));
  }
  location = getFullFileName(treeView, LOCATION_COLUMN);

  if (location != NULL) {
    SMsyscall(location, FILEEXPLORER_LIST);
    g_free(location);
  }
  
  detachMenu(menuitem);
}


void
on_cancel1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  detachMenu(menuitem);
}


void
on_testRegExDialog_response            (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data)
{
    switch (response_id) {
        case GTK_RESPONSE_APPLY:
            refreshTestResults(GTK_WIDGET(dialog));
            break;
        default:
            testRegExDialog1 = NULL;
            gtk_widget_destroy(GTK_WIDGET(dialog));
            break;
    }
}


void
on_SampleTextView_realize              (GtkWidget       *widget,
                                        gpointer         user_data)
{
  GtkTextBuffer *buffer = gtk_text_view_get_buffer (GTK_TEXT_VIEW(widget));
  GtkTextTag *tag;
  
  tag = gtk_text_buffer_create_tag (buffer, "word_highlight",
                                    "foreground", "blue",
                                    "underline", PANGO_UNDERLINE_SINGLE,
                                    "underline-set", TRUE,
                                    NULL);
#ifdef NOT_YET
  tag = gtk_text_buffer_create_tag (buffer, "word_highlight1",
                                    "foreground", "lightBlue",
                                    NULL);
#endif
}


void
on_expWizard_response                  (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data)
{
    gint typeRegex = (gint)g_object_get_data(G_OBJECT(dialog), "regexType");
    GtkEntry *output = GTK_ENTRY(lookup_widget(GTK_WIDGET(dialog), "resultExp"));/* resulExp = name of GtkEntry for the final RegEx formula */
    gchar *finalRegex;
    GtkComboBox *retCombo;/* contains a pointer on main Window, for GtkCombo for files OR GtkCombo for containing text */
    
    if (typeRegex == FILE_REGEX_TYPE) {
        retCombo = GTK_COMBO_BOX(lookup_widget(mainWindowApp, "fileName"));/* filename = GtkWidget, field of the combo in main window */
    } else if (typeRegex == CONTEXT_REGEX_TYPE) {
        retCombo = GTK_COMBO_BOX(lookup_widget(mainWindowApp, "containingText"));
    } else {
        g_print (_("Internal Error! Unable to find calling wizard type!"));
        gtk_widget_destroy(GTK_WIDGET(dialog));
        return;
    }
    
    switch (response_id) {
        case GTK_RESPONSE_HELP:
            SMsyscall(_("http://searchmonkey.sourceforge.net/index.php/Regular_expression_builder"), BROWSER_LIST);
            break;
        case GTK_RESPONSE_OK:
            finalRegex = (gchar *)gtk_entry_get_text(output);/* read text in Wizard dialog, resulting formula */
            if (*finalRegex != '\0') {
                addUniqueRow(GTK_WIDGET(retCombo), finalRegex);/* modify combobox in main window */
            }
            /* Do not break here! We want the widget to be destroyed! */

        default:
            if (typeRegex == FILE_REGEX_TYPE) {
                fileRegexWizard = NULL;
            } else if (typeRegex == CONTEXT_REGEX_TYPE) {
                contextRegexWizard = NULL;
            }
            gtk_widget_destroy(GTK_WIDGET(dialog));
            break;
    }
}


void
on_startType_changed                   (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
    GtkWidget *entry = lookup_widget(GTK_WIDGET(combobox), "startEntry");
    GtkWidget *repeat = lookup_widget(GTK_WIDGET(combobox), "startOccurance");
    updateTypeChangeEntry(combobox, entry, repeat);
    updateRegExWizard(GTK_WIDGET(combobox));
}


void
on_startEntry_changed                  (GtkEditable     *editable,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(editable));
}


void
on_startOccurance_changed              (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(combobox));
}


void
on_midType_changed                     (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
    GtkWidget *entry = lookup_widget(GTK_WIDGET(combobox), "midEntry");
    GtkWidget *repeat = lookup_widget(GTK_WIDGET(combobox), "midOccurance");

    updateTypeChangeEntry(combobox, entry, repeat);
    updateRegExWizard(GTK_WIDGET(combobox));
    
}


void
on_midEntry_changed                    (GtkEditable     *editable,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(editable));
}


void
on_midOccurance_changed                (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(combobox));
}


void
on_endType_changed                     (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
    GtkWidget *entry = lookup_widget(GTK_WIDGET(combobox), "endEntry");
    GtkWidget *repeat = lookup_widget(GTK_WIDGET(combobox), "endOccurance");
    updateTypeChangeEntry(combobox, entry, repeat);
    updateRegExWizard(GTK_WIDGET(combobox));
}


void
on_endEntry_changed                    (GtkEditable     *editable,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(editable));
}


void
on_endOccurance_changed                (GtkComboBox     *combobox,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(combobox));
}


void
on_regExpWizard1_clicked               (GtkButton       *button,
                                        gpointer         user_data)
{
    /* Create filename wizard */
    if (fileRegexWizard == NULL) {
        fileRegexWizard = create_expWizard();
        g_object_set_data(G_OBJECT(fileRegexWizard), "regexType", (gpointer)FILE_REGEX_TYPE); /* file type */
        gtk_widget_show(fileRegexWizard);
    } else {
        gtk_widget_grab_focus(lookup_widget(fileRegexWizard, "startEntry"));
    }
}


void
on_regExpWizard2_clicked               (GtkButton       *button,
                                        gpointer         user_data)
{
    /* Create filename wizard */
    if (contextRegexWizard == NULL) {
        contextRegexWizard = create_expWizard();
        g_object_set_data(G_OBJECT(contextRegexWizard), "regexType", (gpointer)CONTEXT_REGEX_TYPE); /* file type */
        gtk_widget_show(contextRegexWizard);
    } else {
        gtk_widget_grab_focus(lookup_widget(contextRegexWizard, "startEntry"));
    }
}


void
on_convertRegex_toggled                (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
    updateRegExWizard(GTK_WIDGET(togglebutton));
}


void
on_addMidContents_clicked              (GtkButton       *button,
                                        gpointer         user_data)
{
    GtkComboBox *type = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midType"));    
    GtkEntry *entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midEntry"));
    GtkComboBox *repeat = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midOccurance"));
    GtkEntry *iterStr = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection"));
    
    appendTableRow(lookup_widget(GTK_WIDGET(button), "midTreeView"),
                   5,
                   gtk_combo_box_get_active_text(type),
                   gtk_entry_get_text(entry),
                   gtk_combo_box_get_active_text(repeat),
                   gtk_combo_box_get_active(type),
                   gtk_combo_box_get_active(repeat));
    gtk_combo_box_set_active(type, 0);
    gtk_entry_set_text(entry, "");
    gtk_combo_box_set_active(repeat, 0);
    gtk_entry_set_text(iterStr, "");
    gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "deleteSelectedContents"), TRUE);
    gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "modifiedSelectedContents"), TRUE);
}


void
on_modifiedSelectedContents_clicked    (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkTreeView *treeview = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(button), "midTreeView"));
  GtkTreeSelection *selection = gtk_tree_view_get_selection (treeview);
  GtkTreeIter iter;
  GtkTreeModel *model;
  gint type = REGWIZ_NONE;
  gchar *entry = NULL;
  gint repeat = REGWIZ_NONE;
  gchar *iterStr;

  g_assert(treeview != NULL);
  g_assert(selection != NULL);
  
  if (gtk_tree_selection_get_selected (selection, &model, &iter)) {
    g_assert(model != NULL);
    gtk_tree_model_get (model, &iter, REGEX_TYPE_INT_COLUMN, &type,
                        REGEX_ENTRY_COLUMN, &entry,
                        REGEX_REPEAT_INT_COLUMN, &repeat,
                        -1);
    
    gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midType")), type);
    gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midEntry")), entry);
    gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midOccurance")), repeat);
    g_free (entry);

    iterStr = gtk_tree_model_get_string_from_iter (model, &iter);
    if (iterStr != NULL) {
      gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection")), iterStr);
      g_free(iterStr);
    }
    
  }
}


void
on_deleteSelectedContents_clicked      (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkTreeView *treeview = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(button), "midTreeView"));
  GtkTreeSelection *selection = gtk_tree_view_get_selection (treeview);
  GtkTreeIter iter;
  GtkTreeModel *model;
  GtkComboBox *type = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midType"));    
  GtkEntry *entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midEntry"));
  GtkEntry *iterC = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection"));
  GtkComboBox *repeat = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midOccurance"));
  
  if (gtk_tree_selection_get_selected (selection, &model, &iter)) {
    gtk_list_store_remove(GTK_LIST_STORE(model), &iter);
    gtk_combo_box_set_active(type, 0);
    gtk_entry_set_text(entry, "");
    gtk_combo_box_set_active(repeat, 0);
    gtk_entry_set_text(iterC, "");
  }
  if (gtk_tree_model_iter_n_children (model, NULL) <= 0) {
    gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "modifiedSelectedContents"), FALSE);
    gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "deleteSelectedContents"), FALSE);
  }
  updateRegExWizard(GTK_WIDGET(button));
}

/* Regex Wizard : middle part of the expression */
void
on_midTreeView_realize                 (GtkWidget       *widget,
                                        gpointer         user_data)
{
    GtkTreeViewColumn *column;
    GtkCellRenderer *renderer = gtk_cell_renderer_text_new ();

    GtkListStore *store = gtk_list_store_new (REGEX_N_COLUMNS,/* number of total columns in Regex wizard- 1 janv 2018 Luc A*/                                            
                                              G_TYPE_STRING,
                                              G_TYPE_STRING,
                                              G_TYPE_STRING,
                                              G_TYPE_INT,
                                              G_TYPE_INT,
                                              G_TYPE_INT);
    gtk_tree_view_set_model (GTK_TREE_VIEW(widget), GTK_TREE_MODEL(store));
    g_object_unref (G_OBJECT (store));

    /* Create columns */
    column = gtk_tree_view_column_new_with_attributes (_("_(Type)"), renderer,
                                                       "text", REGEX_TYPE_COLUMN,
                                                       NULL);
    gtk_tree_view_append_column (GTK_TREE_VIEW (widget), column);

    column = gtk_tree_view_column_new_with_attributes (_("_(Entry)"), renderer,
                                                       "text", REGEX_ENTRY_COLUMN,
                                                       NULL);
    gtk_tree_view_append_column (GTK_TREE_VIEW (widget), column);

    column = gtk_tree_view_column_new_with_attributes (_("_(Repeat)"), renderer,
                                                       "text", REGEX_REPEAT_COLUMN,
                                                       NULL);
    gtk_tree_view_append_column (GTK_TREE_VIEW (widget), column);
}


void
on_updateSelectedContents_clicked      (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkTreeView *treeview = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(button), "midTreeView"));
  GtkTreeIter iter;
  GtkTreeModel *model = gtk_tree_view_get_model (treeview);
  GtkComboBox *type = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midType"));    
  GtkEntry *entry = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midEntry"));
  GtkEntry *iterC = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection"));
  GtkComboBox *repeat = GTK_COMBO_BOX(lookup_widget(GTK_WIDGET(button), "midOccurance"));
  const gchar *iterStr = gtk_entry_get_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection")));

  g_assert (treeview != NULL);
  g_assert (model != NULL);
  g_assert (type != NULL);
  g_assert (entry != NULL);
  g_assert (repeat != NULL);
  g_assert (iterStr != NULL);
  
  if (*iterStr == '\0') {
    return;
  }
  
  gtk_tree_model_get_iter_from_string (model, &iter, iterStr);
  if (gtk_combo_box_get_active(type) == REGWIZ_DONT_KNOW) {
    gtk_list_store_remove(GTK_LIST_STORE(model), &iter);
    if (gtk_tree_model_iter_n_children (model, NULL) <= 0) {
      gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "modifiedSelectedContents"), FALSE);
      gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "deleteSelectedContents"), FALSE);
    }
    gtk_combo_box_set_active(type, 0);
    gtk_entry_set_text(entry, "");
    gtk_combo_box_set_active(repeat, 0);
    gtk_entry_set_text(iterC, "");
    gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection")), "");
    return; /* Delete entry instead */
  }
    
  gtk_list_store_set (GTK_LIST_STORE(model), &iter,
                      REGEX_TYPE_COLUMN, gtk_combo_box_get_active_text(type),
                      REGEX_ENTRY_COLUMN, gtk_entry_get_text(entry),
                      REGEX_REPEAT_COLUMN, gtk_combo_box_get_active_text(repeat),
                      REGEX_TYPE_INT_COLUMN, gtk_combo_box_get_active(type),
                      REGEX_REPEAT_INT_COLUMN, gtk_combo_box_get_active(repeat),
                      -1);
    gtk_combo_box_set_active(type, 0);
    gtk_entry_set_text(entry, "");
    gtk_combo_box_set_active(repeat, 0);
    gtk_entry_set_text(iterC, "");
    gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "midSelection")), "");
}


void
on_midSelection_changed                (GtkEditable     *editable,
                                        gpointer         user_data)
{
  const gchar *selection = gtk_entry_get_text(GTK_ENTRY(editable));
  
  if (*selection != '\0') {
    gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(editable), "updateSelectedContents"), TRUE);
  } else {
    gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(editable), "updateSelectedContents"), FALSE);
  }
}


void
on_expWizard_realize                   (GtkWidget       *widget,
                                        gpointer         user_data)
{
  gint widgetType = (gint)g_object_get_data(G_OBJECT(widget), "regexType");

  if (widgetType == FILE_REGEX_TYPE) {
    gtk_window_set_title (GTK_WINDOW (fileRegexWizard), _("File Expression Wizard"));
  } else if (widgetType == CONTEXT_REGEX_TYPE) {
    gtk_window_set_title (GTK_WINDOW (contextRegexWizard), _("Context Expression Wizard"));
  }
  
  gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(widget, "startType")), REGWIZ_DONT_KNOW);
  gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(widget, "startOccurance")), REGWIZ_REPEAT_ONCE);
  gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(widget, "midType")), REGWIZ_DONT_KNOW);
  gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(widget, "midOccurance")), REGWIZ_REPEAT_ONCE);
  gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(widget, "endType")), REGWIZ_DONT_KNOW);
  gtk_combo_box_set_active(GTK_COMBO_BOX(lookup_widget(widget, "endOccurance")), REGWIZ_REPEAT_ONCE);
}


void
on_midTreeView_drag_end                (GtkWidget       *widget,
                                        GdkDragContext  *drag_context,
                                        gpointer         user_data)
{
    updateRegExWizard(widget);
}


void
on_configDialog_realize                (GtkWidget       *widget,
                                        gpointer         user_data)
{
    realize_configDialog(widget);
}


void
on_autoFindExe_clicked                 (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkWidget *dialog = create_autoComplete();
  GtkProgressBar *pbar = GTK_PROGRESS_BAR(lookup_widget(dialog, "progressbar3"));
  userExeData *exeData = g_malloc0(sizeof(userExeData));

  /* Attach data to the dialog */
  exeData->parent = GTK_WIDGET(button);
  gtk_dialog_set_response_sensitive(GTK_DIALOG(dialog), GTK_RESPONSE_OK, FALSE);
  gtk_progress_bar_set_fraction(pbar, 0);
  gtk_progress_bar_set_text(pbar, _("0%"));
  g_object_set_data_full(G_OBJECT(dialog), "exeData", exeData, g_free_exeData);

  /* Spawn idle iterator, and show status dialog */
  exeData->gid = g_timeout_add(40, getExeData, dialog); /* Slow iterations down by using timeouts */
  gtk_widget_show(dialog);
}


void
on_online_release_notes1_activate      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  /* Browse to a new release notes subset. Note redirect required by website */
  gchar *website = g_strdup_printf(_("http://%s.sf.net/index.php?title=V%s_Release_Notes"), PACKAGE, VERSION);
  SMsyscall(website, BROWSER_LIST);
}


void
on_configResetAll_clicked              (GtkButton       *button,
                                        gpointer         user_data)
{
  gchar *searchmonkeyExe = g_object_get_data(G_OBJECT(mainWindowApp), "argvPointer");
  GtkWidget *confirmDialog = gtk_message_dialog_new (GTK_WINDOW(lookup_widget(GTK_WIDGET(button), "configDialog")),
                                                     (GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                                     GTK_MESSAGE_WARNING,
                                                     GTK_BUTTONS_OK_CANCEL,
                                                     _("Are you sure that you wish to delete the config file, and restart searchmonkey?"));

  if (gtk_dialog_run (GTK_DIALOG (confirmDialog)) == GTK_RESPONSE_OK) {

    /* Delete the config file, and disable saving on exit - attempt to restart searchmonkey too? */
    if (g_remove(gConfigFile) != 0) {
      g_print(_("Error! Unable to remove config file %s.\n"), gConfigFile);    
    }
    g_object_set_data(G_OBJECT(mainWindowApp), CONFIG_DISABLE_SAVE_CONFIG_STRING, GINT_TO_POINTER(CONFIG_DISABLE_SAVE_CONFIG));
    g_spawn_command_line_async (searchmonkeyExe, NULL); /* Assume it worked */
    gtk_dialog_response (GTK_DIALOG(lookup_widget(GTK_WIDGET(button), "configDialog")), GTK_RESPONSE_REJECT);
    gtk_main_quit(); /* Exit this instance */
    return;
  }
  gtk_widget_destroy (confirmDialog);  
}


void
on_configSaveNow_clicked               (GtkButton       *button,
                                        gpointer         user_data)
{
  GKeyFile *keyString = getGKeyFile(GTK_WIDGET(mainWindowApp));

  /* Immediately unrealize check button */
  unrealizeToggle(GTK_WIDGET(button), keyString, "configuration", "configPromptSave");
  
  storeGKeyFile(keyString);
  setConfigFileLocation (GTK_WIDGET(button));
}


void
on_forums1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  SMsyscall(_("http://sf.net/forum/?group_id=175143"), BROWSER_LIST);
}


void
on_matches1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    columnClick(GTK_WIDGET(menuitem), MATCHES_COUNT_COLUMN);
  }
}


void
on_window1_unrealize                   (GtkWidget       *widget,
                                        gpointer         user_data)
{
  unrealize_searchmonkeyWindow(widget); /* Initialise everything possible immediately */
}


void
on_window1_realize                     (GtkWidget       *widget,
                                        gpointer         user_data)
{
  realize_searchmonkeyWindow(widget); /* Initialise everything possible immediately */
}

gboolean
on_configResultEOL_focus_out_event     (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data)
{
  checkCsvEntry(GTK_ENTRY(lookup_widget(widget, "configResultEOL")));
  return FALSE;
}



gboolean
on_configResultEOF_focus_out_event     (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data)
{
  checkCsvEntry(GTK_ENTRY(lookup_widget(widget, "configResultEOF")));
  return FALSE;
}


gboolean
on_configResultDelimiter_focus_out_event
                                        (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data)
{
  checkCsvEntry(GTK_ENTRY(lookup_widget(widget, "configResultDelimiter")));
  return FALSE;
}


void
on_newInstance1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  spawnNewSearchmonkey();
}


void
on_playButton_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  start_search_thread(GTK_WIDGET(menuitem));
}


void
on_stopButton_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  stop_search_thread(GTK_WIDGET(menuitem));
}


void
on_horizontal_results1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    setResultsViewHorizontal(GTK_WIDGET(menuitem), TRUE);
  }
}


void
on_vertical_results1_activate          (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{ 
  if (gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem))) {
    setResultsViewHorizontal(GTK_WIDGET(menuitem), FALSE); /* default to vertical */
  }
}


void
on_importCriteria_clicked              (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{
  importCriteria(GTK_WIDGET(toolbutton));
}


void
on_exportCriteria_clicked              (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{
  exportCriteria(GTK_WIDGET(toolbutton));
}


void
on_saveResults_clicked                 (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{
  saveResults(GTK_WIDGET(toolbutton));
}


void
on_printResults_clicked                (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{

}


void
on_newInstance2_clicked                (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{
  spawnNewSearchmonkey();
}


void
on_playButton2_clicked                 (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{
  start_search_thread(GTK_WIDGET(toolbutton));
}


void
on_stopButton2_clicked                 (GtkToolButton   *toolbutton,
                                        gpointer         user_data)
{
  stop_search_thread(GTK_WIDGET(toolbutton));
}


void
on_playButton_clicked                  (GtkButton       *button,
                                        gpointer         user_data)
{
  start_search_thread(GTK_WIDGET(button));
}


void
on_stopButton_clicked                  (GtkButton       *button,
                                        gpointer         user_data)
{
  stop_search_thread(GTK_WIDGET(button));
}


void
on_searchNotebook_switch_page          (GtkNotebook     *notebook,
                                        GtkNotebookPage *page,
                                        guint            page_num,
                                        gpointer         user_data)
{
  guint old_page_num = gtk_notebook_get_current_page(notebook);
  
  if (old_page_num == 0) { /* If leaving basic mode - save settings */
    setExpertSearchMode(GTK_WIDGET(notebook), TRUE);
  } else if (old_page_num == 1) { /* If leaving expert mode - save settings */
    setExpertSearchMode(GTK_WIDGET(notebook), FALSE);
  } else {
    if (page_num == 0) { /* If going to basic mode - restore settings */
      setExpertSearchMode(GTK_WIDGET(notebook), FALSE);
    } else if (page_num == 1) { /* If going to expert mode - restore settings */
      setExpertSearchMode(GTK_WIDGET(notebook), TRUE);
    }
  }
}


void
on_aboutSearchmonkey_response          (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data)
{
  GtkWidget *creditsDialog;
  
  if (response_id == GTK_RESPONSE_OK) { /* Show credits */
    creditsDialog = create_creditsDialog();
    gtk_widget_show(creditsDialog);
  } else { /* Otherwise destroy about box */
    gtk_widget_destroy(GTK_WIDGET(dialog));
  }
}


void
on_autosize_columns_activate           (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
  GKeyFile *keyString = getGKeyFile(GTK_WIDGET(mainWindowApp));
  gboolean autoColumnWidth = gtk_check_menu_item_get_active(GTK_CHECK_MENU_ITEM(menuitem));

  g_assert(keyString != NULL);
  
  realizeTreeviewColumns (GTK_WIDGET(mainWindowApp), keyString, "history", "treeview", autoColumnWidth);  
}

/* surprise : menu empty at last 2017
 so I've made the job - :-) Luc A. - 4 janv 2018
*/


void
on_edit_file1_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
 GtkTreeView *treeView;
 gchar *fullFileName;
  
  if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview1"));
  } else {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview2"));
  }
  fullFileName = getFullFileName(treeView, FULL_FILENAME_COLUMN);

  if (fullFileName != NULL) {
    SMsyscall(fullFileName, TEXTEDITOR_LIST);
    g_free(fullFileName);
  }

}

/* surprise : menu empty at last 2017
 so I've made the job - :-) Luc A. - 4 janv 2018
*/


void
on_open_folder1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
 GtkTreeView *treeView;
  gchar *location;
  
  if (getResultsViewHorizontal(GTK_WIDGET(menuitem))) {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview1"));
  } else {
    treeView = GTK_TREE_VIEW(lookup_widget(GTK_WIDGET(menuitem), "treeview2"));
  }
  location = getFullFileName(treeView, LOCATION_COLUMN);

  if (location != NULL) {
    SMsyscall(location, FILEEXPLORER_LIST);
    g_free(location);
  }
}


void
on_folderDepthCheck_toggled            (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  gboolean checked = gtk_toggle_button_get_active(togglebutton);

  gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(togglebutton), "folderDepthSpin"), checked); /* Control spin sensitivity */
  if (checked) { /* Re-enable folder recursion enable, if depth set */
    gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(togglebutton), "searchSubfoldersCheck")), TRUE);
  }
}


void
on_searchSubfoldersCheck_toggled       (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  if (!gtk_toggle_button_get_active(togglebutton)) { /* Disable depth limit, if recurse disabled */
    gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(lookup_widget(GTK_WIDGET(togglebutton), "folderDepthCheck")), FALSE);
  }
}


void
on_dosExpressionRadioFile_clicked      (GtkButton       *button,
                                        gpointer         user_data)
{
  if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(button))) {
    changeModel(GTK_WIDGET(button), "regex", "noregex");
  }
}


void
on_regularExpressionRadioFile_clicked  (GtkButton       *button,
                                        gpointer         user_data)
{
  if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(button))) {
    changeModel(GTK_WIDGET(button), "noregex", "regex");
  }
}


void
on_afterCalenderBtn_clicked            (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkEntry *dateEntry = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "afterEntry"));
  gchar* newDate = getDate(gtk_entry_get_text(dateEntry));
  gtk_entry_set_text(dateEntry, newDate);
  g_free(newDate);
}


void
on_beforeCalendatBtn_clicked           (GtkButton       *button,
                                        gpointer         user_data)
{
  GtkEntry *dateEntry = GTK_ENTRY(lookup_widget(GTK_WIDGET(button), "beforeEntry"));
  gchar* newDate = getDate(gtk_entry_get_text(dateEntry));
  gtk_entry_set_text(dateEntry, newDate);
  g_free(newDate);
}


gboolean
on_regexp_focus_out_event              (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data)
{
  gchar * regexp;
  guint flags;
  gchar* error = _("Error! Invalid 'file name' regular expression");
  
  if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(widget, "regularExpressionRadioFile")))) {
    if (getExtendedRegexMode(widget)) {
      flags |= REG_EXTENDED;
    }
    flags |= REG_NOSUB;

    regexp = gtk_combo_box_get_active_text(GTK_COMBO_BOX(widget));
  
    if (test_regexp(regexp, flags, error)) {
      addUniqueRow(widget, regexp);
    }
  }
  return FALSE;
}

gboolean
on_regexp2_focus_out_event             (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data)
{
  gchar * regexp;
  guint flags;
  gchar* error = _("Error! Invalid 'containing text' regular expression");
  
  if (getExtendedRegexMode(widget)) {
    flags |= REG_EXTENDED;
  }
  flags |= REG_NOSUB;

  regexp = gtk_combo_box_get_active_text(GTK_COMBO_BOX(widget));
  
  if (test_regexp(regexp, flags, error)) {
    addUniqueRow(widget, regexp);
  }
  return FALSE;
}


void
on_limitResultsCheckResults_toggled    (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  gboolean active = gtk_toggle_button_get_active(togglebutton);
  gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(togglebutton), "limit_results_hbox"), active);
}


void
on_showLinesCheckResults_toggled       (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  gboolean active = gtk_toggle_button_get_active(togglebutton);
  gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(togglebutton), "show_line_contents_hbox"), active);
}


gboolean
on_searchNotebook_focus_out_event      (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data)
{
  GtkWidget *notebook = lookup_widget(widget, "searchNotebook");
  guint old_page_num = gtk_notebook_get_current_page(GTK_NOTEBOOK(notebook));
  
  if (old_page_num == 0) { /* If currently showing basic mode - then save these settings  */
    setExpertSearchMode(notebook, TRUE);
  } else { /* Otherwise use expert settings as the default */
    setExpertSearchMode(notebook, FALSE);
  }
  return FALSE;
}


void
on_limitContentsCheckResults_toggled   (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
  gboolean active = gtk_toggle_button_get_active(togglebutton);
  gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(togglebutton), "limit_contents_hbox"), active);	
}


void
on_autoComplete_response               (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data)
{
  userExeData *exeData = g_object_get_data(G_OBJECT(dialog), "exeData");
  GtkWidget *parent = exeData->parent;
  GtkWidget *widget = GTK_WIDGET(dialog);
  GtkWidget *choosers[3];
  gint i,j;

  if (response_id == GTK_RESPONSE_OK) { /* Can only happen with valid data.. */
    /* Store new values into configuration */  
    choosers[BROWSER_LIST] = lookup_widget(parent, "configWebBrowser");
    choosers[TEXTEDITOR_LIST] = lookup_widget(parent, "configTextEditor");
    choosers[FILEEXPLORER_LIST] = lookup_widget(parent, "configFileExplorer");
    for (j=0; j<G_EXE_LIST_MAX_DEPTH; j++) {
      for (i=0; i<G_EXE_LIST_MAX_WIDTH; i++) {
        if (exeData->retStr[j][i] != NULL) {
          gtk_entry_set_text(GTK_ENTRY(choosers[j]), exeData->retStr[j][i]);
          break;
        }
      }
    }
  } else { /* Assume cancelled */
    g_source_remove(exeData->gid); /* Stop timeout (just in case) */
  }

  /* Clean exit */
  gtk_widget_destroy(GTK_WIDGET(dialog));
}

