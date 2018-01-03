/*
 * File: regexwizard.c
 * Description: Contains functions that control the regular expression wizard
 * Todo: Improve regular expression building, and extensively test code to find
 *       bugs in the regular expression building.
 *       Advanced syntax (e.g. {min, max} occurances) are missing
 */
#include <gtk/gtk.h>
#include <regex.h> /* Add regular expression support */
//#include <glib.h>

#include "interface.h" /* glade requirement */
#include "support.h" /* glade requirement */
#include "regexwizard.h"


/*
 * Callback helper: within test regular expression dialog box.
 * Update the regular expression test panel with matches within the user typed sample text.
 * TODO: This function should really reuse some of the existing search functions, as currently it re-writes
 *       nearly every aspect of searching a text string for matches.
 */
void refreshTestResults(GtkWidget *widget)
{
    GtkTextBuffer *tBuffer = gtk_text_view_get_buffer(GTK_TEXT_VIEW(lookup_widget(widget, "SampleTextView")));
    GtkTextIter startIter, endIter;
    gtk_text_buffer_get_start_iter (tBuffer, &startIter);
    gtk_text_buffer_get_end_iter (tBuffer, &endIter);
    gchar *sampleText = gtk_text_buffer_get_text (tBuffer,
                                                  &startIter,
                                                  &endIter,
                                                  FALSE);
    regmatch_t subMatches[MAX_SUB_MATCHES];
    const gchar *testExpression = gtk_entry_get_text(GTK_ENTRY(lookup_widget(widget, "testEntry")));
    regex_t test;
    gint i;
    gchar test2[256+1];        
    gint errorCode;
    gchar *charMatch = sampleText;
    gsize intMatch = 0;
    gint matchCount = 0;
    GtkEntry *status= GTK_ENTRY(lookup_widget(widget, "testResultStatus"));
    
    /* Clear tags, and compile regex */
    gtk_entry_set_text(status, "");
    gtk_text_buffer_remove_all_tags (tBuffer, &startIter, &endIter);
    errorCode = regcomp(&test, testExpression, REG_ICASE | REG_EXTENDED | REG_NEWLINE);
    if (errorCode != 0) {
        regerror (errorCode, &test, test2, 256);
        gtk_entry_set_text(status, test2);
        g_free(sampleText);
        return;
    }

    
    /* Find matches */
    while (regexec(&test, charMatch, MAX_SUB_MATCHES, subMatches, 0) == 0) {
      
      /* Highlight code */
#ifdef NOT_YET
      for (i=0; i<MAX_SUB_MATCHES; i++) {
#endif
        i=0;
        if (subMatches[i].rm_so == -1) {
          break;
        }
        gtk_text_buffer_get_iter_at_offset (tBuffer, &startIter, intMatch + subMatches[i].rm_so);
        gtk_text_buffer_get_iter_at_offset (tBuffer, &endIter, intMatch + subMatches[i].rm_eo);
        switch(i) {
#ifdef NOT_YET
        case 0:
          gtk_text_buffer_apply_tag_by_name (tBuffer, "word_highlight", &startIter, &endIter);
          break;
#endif
        default:
          gtk_text_buffer_apply_tag_by_name (tBuffer, "word_highlight", &startIter, &endIter);
          break;
        }
#ifdef NOT_YET
      }
#endif
      matchCount ++;
      intMatch += subMatches[0].rm_eo;
      charMatch += subMatches[0].rm_eo;
    }
    
    if (matchCount == 0) {
      gtk_entry_set_text(status, _("Did not match any."));
    } else if (matchCount == 1) {
      gtk_entry_set_text(status, _("Found 1 match."));
    } else {
      charMatch = g_strdup_printf(_("Found %d matches."), matchCount);
      gtk_entry_set_text(status, charMatch);
      g_free(charMatch);
    }

    g_free(sampleText);
    regfree(&test);
}


/*
 * Callback helper: within regular expression wizard.
 * Updates the entry/repeat widgets depending on which type of entry the user wishes to make.
 * E.g. If they select ANY_CHAR disable everything except for repeat quantity
 *      or, if they select THE_PHRASE enable the entry box to allow text to be typed.
 */
void updateTypeChangeEntry(GtkComboBox *combobox, GtkWidget *entry, GtkWidget *repeat)
{
    switch (gtk_combo_box_get_active (combobox)) {
        case REGWIZ_THE_CHAR:
            gtk_widget_set_sensitive (entry, TRUE);
            gtk_widget_set_sensitive(repeat, TRUE);
            gtk_entry_set_max_length (GTK_ENTRY(entry), 1);
            break;
            
        case REGWIZ_ANY_ONE_CHAR:
        case REGWIZ_ANY_CHAR_EXCEPT:
        case REGWIZ_THE_PHRASE:
            gtk_widget_set_sensitive(entry, TRUE);
            gtk_widget_set_sensitive(repeat, TRUE);
            gtk_entry_set_max_length (GTK_ENTRY(entry), 0);
            break;

        case REGWIZ_ANY_CHAR:
        case REGWIZ_SPACE:
        case REGWIZ_ANY_NUMERIC:
        case REGWIZ_ANY_TEXT:
            gtk_widget_set_sensitive(repeat, TRUE);
            gtk_widget_set_sensitive(entry, FALSE);
            break;
            
        case REGWIZ_DONT_KNOW:
        default: 
            gtk_widget_set_sensitive(entry, FALSE);
            gtk_widget_set_sensitive(repeat, FALSE);
            break;
    }
}


/*
 * Callback helper: within regular expression wizard.
 * Updates the output regular expression box depending on all user entries.
 * This function creates slightly different expressions depending on whether
 * it is a start, mid, or end occurance of the string.
 */
void updateRegExWizard(GtkWidget *widget)
{
  GtkEntry *output = GTK_ENTRY(lookup_widget(widget, "resultExp"));
  GtkTreeView *treeView = GTK_TREE_VIEW(lookup_widget(widget, "midTreeView"));
  GtkTreeModel *treemodel = gtk_tree_view_get_model (treeView);
  GtkTreeIter iter;
  GtkComboBox *type[3];
  GtkEntry *entry[3];
  GtkComboBox *repeat[3];
  GString *tmpGStr = NULL;
  gchar *tmpStr;
  gint iType, iRepeat;
  gchar *pEntry;

  /* Make sure that GTK is up and running */
  g_assert(output != NULL);
  g_assert(treeView != NULL);
  if (treemodel == NULL) {
    return;
  }
  
  /* Set pointers to all of the entry boxes */
  type[0] = GTK_COMBO_BOX(lookup_widget(widget, "startType"));
  entry[0] = GTK_ENTRY(lookup_widget(widget, "startEntry"));
  repeat[0] = GTK_COMBO_BOX(lookup_widget(widget, "startOccurance"));
  type[1] = GTK_COMBO_BOX(lookup_widget(widget, "midType"));
  entry[1] = GTK_ENTRY(lookup_widget(widget, "midEntry"));
  repeat[1] = GTK_COMBO_BOX(lookup_widget(widget, "midOccurance"));
  type[2] = GTK_COMBO_BOX(lookup_widget(widget, "endType"));
  entry[2] = GTK_ENTRY(lookup_widget(widget, "endEntry"));
  repeat[2] = GTK_COMBO_BOX(lookup_widget(widget, "endOccurance"));
  tmpGStr = g_string_sized_new(DEFAULT_GSTRING_SIZE);

  /* Grab start regex */
  tmpStr = makeInterimRegex (type[0], entry[0], repeat[0], REGWIZ_START_TYPE);
  if (tmpStr != NULL) {
    g_string_append(tmpGStr, tmpStr);
    g_free(tmpStr);
  }

  /* Grab middle(s) regex */
  tmpStr = makeInterimRegex (type[1], entry[1], repeat[1], -1);
  if (tmpStr != NULL) {
    gtk_widget_set_sensitive(lookup_widget(widget, "addMidContents"), TRUE);
    g_free(tmpStr);
  } else {
    gtk_widget_set_sensitive(lookup_widget(widget, "addMidContents"), FALSE);
  }
  
  if (gtk_tree_model_get_iter_first   (treemodel, &iter)) {
    do {
      gtk_tree_model_get (treemodel, &iter,
			  REGEX_TYPE_INT_COLUMN, &iType,
			  REGEX_ENTRY_COLUMN, &pEntry,
			  REGEX_REPEAT_INT_COLUMN, &iRepeat,
			  -1);
      tmpStr = makeInterimRegex2 (widget, iType, pEntry, iRepeat, -1);
      if (tmpStr != NULL) {
	g_string_append(tmpGStr, tmpStr);
	g_free(tmpStr);
      }
      g_free(pEntry);
    } while (gtk_tree_model_iter_next(treemodel, &iter));
  }
  
  /* Grab end regex */
  tmpStr = makeInterimRegex (type[2], entry[2], repeat[2], REGWIZ_END_TYPE);
  if (tmpStr != NULL) {
    g_string_append(tmpGStr, tmpStr);
    g_free(tmpStr);
  }
  
  /* If regex valid then allow ok button to be pressed */
  if (tmpGStr->len > 0) {
    gtk_widget_set_sensitive(lookup_widget(widget, "okRegExWizard"), TRUE);
  } else {
    gtk_widget_set_sensitive(lookup_widget(widget, "okRegExWizard"), FALSE);
  }

  /* Write validated regex or clear text on failure */
  tmpStr = g_string_free(tmpGStr, FALSE);
  if (tmpStr != NULL) {
    gtk_entry_set_text(output, tmpStr);
    g_free(tmpStr);
  }
  return;
  gtk_entry_set_text(output, ""); /* Should never reach this.. */
}


/*
 * Internal helper: within regular expression wizard.
 * Given references to each of the widgets, extracts the text in order to generate
 * the regular expression (Majority of work done in makeInterimRegex2).
 * The position entry refers to whether it is the start, mid or end entry.
 * The resulting string is returned to the caller - g_free after use.
 * TODO: Create enum typedef for position so that beginning, middle or end is more explicit.
 * TODO: Choose a better name for this function - see makeInterimRegex2
 */
gchar *makeInterimRegex(GtkComboBox *type, GtkEntry *entry, GtkComboBox *repeatc, gint position)
{
    gint iType = gtk_combo_box_get_active (type);
    const gchar *pEntry = gtk_entry_get_text(entry);
    gint iRepeat = gtk_combo_box_get_active (repeatc);
    
    return makeInterimRegex2(GTK_WIDGET(type), iType, pEntry, iRepeat, position);
}


/*
 * Internal helper: within regular expression wizard.
 * Given the text entries for a particular use entry row, and its position (start, mid, or end)
 * return the resulting regular expression, including necessary brackets and conversions.
 * Returned string must be free'd
 * TODO: Create enum typedef for the type variable, so that it is more explicit.
 * TODO: Create enum typedef for position so that beginning, middle or end is more explicit.
 * TODO: Choose a better name for this function - see makeInterimRegex
 */
gchar *makeInterimRegex2(GtkWidget *widget, gint type, const gchar *pEntry, gint repeatc, gint position)
{
  GString *output;
  gchar *tmpStr;
  gboolean convertInternal = gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(lookup_widget(widget, "convertRegex")));
  
  /* Exit early if empty field detected */
  if ((pEntry[0] == '\0') && 
      ((type >= REGWIZ_THE_CHAR) && (type <= REGWIZ_THE_PHRASE))) {
    return NULL;
  }

  /* Set body */
  switch (type) {
  case REGWIZ_ANY_CHAR:
    output = g_string_new(".");
    break;
    
  case REGWIZ_THE_CHAR:
    output = convertRegex(pEntry, convertInternal);
    break;
            
  case REGWIZ_ANY_ONE_CHAR:
    output = convertRegex2(pEntry, convertInternal);
    g_string_prepend_c(output, '[');
    g_string_append_c(output, ']');
    break;
    
  case REGWIZ_ANY_CHAR_EXCEPT:
    output = convertRegex2(pEntry, convertInternal);
    g_string_prepend(output, "[^");
    g_string_append_c(output, ']');
    break;
    
  case REGWIZ_THE_PHRASE:
    output = convertRegex(pEntry, convertInternal);
    g_string_prepend_c(output, '(');
    g_string_append_c(output, ')');
    break;
    
  case REGWIZ_SPACE:
    output = g_string_new("\\s");
    break;
    
  case REGWIZ_ANY_NUMERIC:
    output = g_string_new("\\d");
    break;
    
  case REGWIZ_ANY_TEXT:
    output = g_string_new("\\w");
    break;
    
  case REGWIZ_DONT_KNOW:
  default:
    return NULL; /* This is just an empty string */
  }
  
  /* Set repeat symbol */
  switch (repeatc) {
  case REGWIZ_REPEAT_ONE_PLUS:
    g_string_append_c(output, '+');
    break;
  case REGWIZ_REPEAT_ZERO_PLUS:
    g_string_append_c(output, '*');
    break;
  case REGWIZ_REPEAT_ZERO_ONE:
    g_string_append_c(output, '?');
    break;
  default:
    /* Do nothing */
    break;
  }
  
  /* Set prefix/affix symbol */
  if (position == REGWIZ_START_TYPE) {
    g_string_prepend_c(output, '^');
  }
  if (position == REGWIZ_END_TYPE) {
    g_string_append_c(output, '$');
  }
  
  /* Free up the GString, but return a gchar array */
  return g_string_free(output, FALSE);
}


/*
 * Internal helper: within regular expression wizard.
 * Given a regular expression entry, convert reserved charactors into their regular expression
 * defined alternatives. For example [ becomes \[, and ^ becomes \^.
 * For the most part, a preceeding backslash is added to the supplied letter
 * This function is only valid for strings not within square brackets.
 * Resulting string is returned (even if identical), and must always be free'd
 * If change is FALSE, then no conversion is done, but malloc'ed string is still returned.
 * TODO: Rename this function to be more specific - see convertRegex2
 */
GString *convertRegex(const gchar *input, gboolean change)
{
  GString *output;
  gint index = 0;

  if (!change) {
    return g_string_new(input);
  }
  
  output = g_string_sized_new(DEFAULT_GSTRING_SIZE);
  do {
    switch (input[index]) {
    case '[':
    case '\\':
    case '^':
    case '$':
    case '.':
    case '|':
    case '?':
    case '*':
    case '+':
    case '(':
    case ')':
      g_string_append_c(output, '\\');
      break;
    default:
      break;
    }
    g_string_append_c(output, input[index]);
    index ++;
  } while (input[index] != '\0');
  
  return output;
}


/*
 * Internal helper: within regular expression wizard.
 * Given a regular expression entry, convert reserved charactors into their regular expression
 * defined alternatives.
 * This function is only valid for strings within square brackets as different rules apply..
 * Resulting string is returned (even if identical), and must always be free'd
 * TODO: Change is not currently used - is it needed?
 * TODO: Rename this function to be more specific - see convertRegex
 */
GString *convertRegex2(const gchar *input, gboolean change)
{
  GString *output = g_string_sized_new(DEFAULT_GSTRING_SIZE); /* Create output */
  gint index = 0;
  gint flags = 0; /* Using enumerations defined in header */

  do {
    switch (input[index]) {
    case ']':
      flags |= INC_RIGHT_BRKT;
      break;
    case '^':
      flags |= INC_CARET;
      break;
    case '-':
      flags |= INC_MINUS;
      break;
    default:
      g_string_append_c(output, input[index]);
      break;
    }
    index++;
  } while (input[index] != '\0');
  
  /* Deal with start/end special cases */
  if (flags & INC_CARET) {
    g_string_append(output, "^");
  }
  if (flags & INC_MINUS) {
    g_string_append(output, "-");
  }
  if (flags & INC_RIGHT_BRKT) {
    g_string_prepend(output, "]");
  }
  
  /* Return gchar, but free g_string */
  return output;
}


/*
 * Callbacks helper: within regular expression wizard.
 * When mid-regular expression add button is pressed, append the entry into the
 * table view directly below the entry boxes. Each entry, and its order, correspond
 * to a new part to the regular expression.
 */
void appendTableRow(GtkWidget *widget, gint num, ...) // gchar *entries[])
{
    gint i;
    va_list ap; /* define 'ap' as a pointer to the input strings */ 
    gint Inc=0;
    va_start(ap, num);
    GtkTreeIter iter;
    gchar *readString;
    GtkListStore *store = GTK_LIST_STORE(gtk_tree_view_get_model(GTK_TREE_VIEW(widget)));
 
    gtk_list_store_append (GTK_LIST_STORE(store), &iter);

    /* Loop throgh input strings, adding them to the current iter */
    do
    {
      gtk_list_store_set (store, &iter,
                          Inc, va_arg(ap, gchar *),
                          -1);
    } while (++Inc < num);
    va_end(ap);
}
