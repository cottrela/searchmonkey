#ifndef SYSTEMIO_H
#define SYSTEMIO_H
/*
 * File: systemio.c header
 * Description: This file deals with CSV file creation, and importing/exporting criteria
 */

/*
 * System callback constants. The actual executable file list is stored within search.c - namely G_EXE_LIST
 */
#define G_EXE_LIST_MAX_WIDTH 3 /* Maximum number of executables for each syscallType */
typedef enum {
  BROWSER_LIST = 0,
  TEXTEDITOR_LIST,
  FILEEXPLORER_LIST,
  G_EXE_LIST_MAX_DEPTH
} syscallType;
#define PROGRESS_BAR_NUDGE_VALUE (1 / (gdouble)(G_EXE_LIST_MAX_WIDTH * G_EXE_LIST_MAX_DEPTH)) 

typedef struct { /* Internal structure for */
  GtkWidget *parent;
  gchar *retStr[G_EXE_LIST_MAX_WIDTH][G_EXE_LIST_MAX_DEPTH]; /* Used to store discovered file names */
  gint i; /* loop counter A */
  gint j; /* loop counter B */
  guint gid; /* GSource ID */
} userExeData;

/* CSV related functions */
gchar *resultsToCsvString(GtkWidget *widget);
gchar *quoteString(const gchar *delimiter, const gchar *string);
void saveResults(GtkWidget *widget);

/* Check CSV configuration */
void checkCsvEntry(GtkEntry *entry);
void on_errorMsg_response (GtkDialog *dialog, gint arg1, gpointer user_data);

/* Allow import/export of data to a file for regular expressions */
void importCriteria(GtkWidget *widget);
void exportCriteria(GtkWidget *widget); 


/* System call functions */
//void findExecutables2(GtkWidget *parent, GtkWidget *dialog);
void g_free_exeData(gpointer user_data);
gboolean getExeData(gpointer user_data);
//void *findExecutables(void *args);
gboolean SMsyscall (const gchar *address, syscallType type);
void openUrlLinkFunc (GtkAboutDialog *about, const gchar *link, gpointer data);
gchar *replaceAttributeString(gchar *rawString, const gchar *replacement);
void spawnNewSearchmonkey(void);

/* File I/O and clipboard commands */
gboolean mkFullDir(gchar *folderName, gint mode);
void deleteFile(GtkWidget *widget);
void copyFile(GtkWidget *widget);

/* GLIB wrapper functions/replacements */
gboolean g_file_get_contents2 (const gchar *filename, gchar **contents,  gsize *length, GError **error);
gboolean g_file_set_contents2 (const gchar *filename, const gchar *contents, gssize length, GError **error);
void set_internal_error(GError **error, const gint err_no);
gchar *comboBoxReadCleanFolderName(GtkComboBox *combobox);
gchar **strvdup(gchar **strv);

#endif /* CSVLIBRARY_H */
