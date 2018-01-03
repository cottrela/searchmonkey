#ifndef SEARCH_H
#define SEARCH_H

#include <gtk/gtk.h>
#include <regex.h>
#include <sys/types.h>

/*
 * External global variable declarations
 */
extern GtkWidget *mainWindowApp; /* Holds pointer to the main searchmonkey GUI. Declared in main.c */

/*
 * Global/miscillaneous constants
 */
#define MAX_FILENAME_STRING 512 /* String maximum for things like the statusbar, and other fixed length buffers - minimise usage */
#define TREEVIEW_HEADER_PIXEL_SIZE_Y 25 /* Treeview header size in pixels - on_treeview1_button_press_event() */
#define SEARCHMONKEY_CONFIG "config.ini" /* Configuration name for searchmonkey options - main()*/
#ifndef G_FILENAME_ENCODING
#define G_FILENAME_ENCODING "UTF-8" /* If not set by operating system, force to UTF-8 */
#endif /* G_FILENAME_ENCODING */

/*
 * Progress bar constants - used to control speed, when it occurs, and sleep times
 */
#define MIN_STEP_VAL (1 / (G_EXE_LIST_MAX_DEPTH * (G_EXE_LIST_MAX_WIDTH + 1))) /* Minimum amount to move progress bar by when finding executables */
#define TOTAL_PBAR_JUMP_COUNT 6 /* Number of progress bar steps to reach 100% within function findExecutables() */
#define PBAR_SHOW_IF_MIN 100 /* Mininimum file list size before progress bar is shown*/
#define PHASE_ONE_PBAR_PULSE_MULTILPIER 8 /* Where true speed is 0.5/MULTIPLIER seconds between each pulse */
#define SPIN_BAR_USLEEP_TIME 100000 /* Micro sleep used to help allow the spin bar to be updated */

/*
 * Global Object Variable Names - use g_object_get_data to retrieve, and set to store.
 */
#define MASTER_SEARCH_DATA "search1" /* Storage for the Phase One data inside the g_object */
#define MASTER_SEARCH_CONTROL "control1" /* Storage for the Phase One data inside the g_object */
#define MASTER_STATUSBAR_DATA "status1" /* Storage for the Phase One data inside the g_object */

/*
 * seachmonkey macros to simplify pointer access, and getting the CONTEXT ID for statusbar
 * TODO: Consider converting these to functions instead
 */
#define GET_LAST_PTR(x) g_ptr_array_index(x, (x->len - 1)) /* Helper macro to simplify getting the last pointer from array */
#define DEL_LAST_PTR(x) g_ptr_array_remove_index_fast(x, (x->len -1))  /* Helper macro to simplify removing the nth item from pointer list */

/*
 * Status bar structures and macros
 * TODO: Convert macro to function
 */
#define STATUSBAR_CONTEXT_ID(x) (gtk_statusbar_get_context_id(x, MASTER_STATUSBAR_DATA)) /* Helper macro to get the status bar context id */
typedef struct
{
  gchar constantString[MAX_FILENAME_STRING + 1]; /* Constant for use with fixed info status messages */
  GStringChunk *statusbarChunk; /* Mass storage for push/pop operations performed when tree searching the file system */
} statusbarData;

/*
 * Search results data and control structures
 */
typedef struct { /* Structure to store each match result before being displayed */
  gsize matchIndex; /* first lineMatch index */
  gsize matchCount; /* Number of lineMatch items */
  gchar *pFullName; /* pointer to the full name */
  gchar *pFileName; /* pointer to the file name */
  gchar *pLocation; /* pointer to the location */
  gchar *pMDate; /* pointer to the modified date */
  gchar *pFileSize; /* pointer to the file size */
  gchar *pFileType; /* pointer to the file size */
  long int mDate; /* modified date as an integer */
  unsigned int fileSize; /* filesize as an integer */
} textMatch;
typedef struct { /* Structure to store each line within the file that has a matching result */
  gsize lineNum;
  gsize lineCount; /* Number of lines within this match */
  gsize lineCountBefore; /* Number of displayed lines up to the line where matching text is found */
  gsize lineCountAfter; /* Number of displayed lines after the line where matching text is found */
  gchar *pLine; /* Copy of the line (NULL terminated) */
  gsize offsetStart; /* Match offset start */
  gsize offsetEnd; /* Match offset end */
  gsize lineLen; /* Size of pLine */
  guint invMatchIndex; /* inverse pointer to textMatch array */
} lineMatch;
typedef struct { /* Structure to store all results together */
  GPtrArray *pLocationArray; /* pointer to actual location chunk */
  GPtrArray *textMatchArray; /* array of text match arrays */
  GPtrArray *fileNameArray; /* array of file names */
  GPtrArray *fullNameArray; /* array of full file names */
  GPtrArray *lineMatchArray; /* array of lineMatch objects */
  GStringChunk *locationChunk; /* Mass storage for all of the directory locations */
  GStringChunk *fileSizeChunk; /* Mass storage for all of the file sizes */
  GStringChunk *mDateChunk; /* Mass storage for all of the modified dates */
  GStringChunk *fileTypeChunk; /* Mass storage for all of the file types */
  GStringChunk *textMatchChunk; /* Mass storage for all of the matched lines */
  GtkListStore *store; /* Pointer to the data store */ 
  GtkTreeIter *iter; /* Malloc Iter */
} searchData;
enum searchFlagsEnum  { /* Enumeration of all of the search options set by the user configurations */
  SEARCH_SUB_DIRECTORIES = 1, /* If set, allows sub-directories to be entered when searching files */
  SEARCH_CASE_SENSITIVE = (1<<1), /* If set makes the search case sensitive */
  SEARCH_HIDDEN_FILES = (1<<2), /* If set lets hidden files (starting with .) be found by phase one search */
  SEARCH_SKIP_LINK_FILES = (1<<3), /* If set does not follow symbolic links.*/
//  SEARCH_EXTENDED_REGEX = (1<<4),
  SEARCH_TEXT_CONTEXT = (1<<5), /* If set, performs phase two searching on file content */
  SEARCH_INVERT_FILES = (1<<6), /* If set, inverts the file name search i.e. NOT (*.txt) */
  SEARCH_MORETHAN_SET = (1<<7), /* If set, checks that file size is greater than user entry */
  SEARCH_LESSTHAN_SET = (1<<8), /* If set, checks that file size is smaller than user entry */
  SEARCH_AFTER_SET = (1<<9), /* If set, checks that file was modified after user entry */
  SEARCH_BEFORE_SET = (1<<10), /* If set, checks that file was modified before user entry */
  DEPTH_RESTRICT_SET = (1<<11), /* If set, restrict the maximum recursion depth for folders */
  LIMIT_RESULTS_SET = (1<<12), /* If set, limits the number of file matches shown */
  SEARCH_EXTRA_LINES = (1<<13), /* If set, allows some/more lines of results to be shown for every hit */
  SEARCH_LIMIT_CONTENT_SHOWN = (1<<14) /* If set, limits the number of content lines shown */
};

typedef struct { /* Control structure for search keeps copy of all user settings, and pointers to widgets */
  gchar *startingFolder; /* Pointer to the starting location */
  gchar *fileSearchRegEx; /* Regular expression pointer for the file search */
  gboolean fileSearchIsRegEx; /*If TRUE then fileSearchRegEx is a regex, else a glob pattern */
  gchar *textSearchRegEx; /* Regular expression pointer for the text search */
  guint fileSearchFlags; /* Flags for the file regexp */
  guint textSearchFlags; /* Flags for the text regexp */
  gint folderDepth; /* restrict depth to which folders should be recursed */
  gint limitResults; /* Limits number of results */
  gint numExtraLines; /* Number of extra lines to show beside each match */
  gint limitContentResults; /* Limits number of content matches within each file */
  GtkWidget *widget; /* Pointer to the main window widget */
  time_t after; /* Copy of the modified after time */
  time_t before; /* Copy of the modified before time */
  off_t moreThan; /* Copy of the more than size in bytes */
  off_t lessThan; /* Copy of the less than size in bytes */
  enum searchFlagsEnum flags; /* Search criteria flags */
  gboolean cancelSearch; /* If TRUE stops the current search ASAP */
} searchControl;
enum  { /* Enumeration for all of the file name results table columns */
  FILENAME_COLUMN = 0,
  LOCATION_COLUMN,
  SIZE_COLUMN,
  TYPE_COLUMN,
  MODIFIED_COLUMN, 
  MATCHES_COUNT_STRING_COLUMN,
  FULL_FILENAME_COLUMN,
  INT_SIZE_COLUMN,
  INT_MODIFIED_COLUMN,
  MATCHES_COUNT_COLUMN,
  MATCH_INDEX_COLUMN,
  N_COLUMNS /* Total number of columns in results table */
};

/* Main search entry points (not threaded) */
void start_search_thread(GtkWidget *widget);
void stop_search_thread(GtkWidget *widget);

/* Main search POSIX thread */
void *walkDirectories(void *args);
glong phaseOneSearch(searchControl *mSearchControl, searchData *mSearchData, statusbarData *status);
glong phaseTwoSearch(searchControl *mSearchControl, searchData *mSearchData, statusbarData *status);
gboolean symLinkReplace(gchar **pFullFileName, gchar **pFileName);
gboolean getAllMatches(searchData *mSearchData, gchar *contents, gsize length, regex_t *search);
void dereferenceAbsolutes(searchData *mSearchData,gchar *contents, gsize length, gint numLines);
void getLength(searchData *mSearchData, textMatch *newMatch);
void getFileType(searchData *mSearchData, textMatch *newMatch);
void getModified(searchData *mSearchData, textMatch *newMatch);
void getFileSize(searchData *mSearchData, textMatch *newMatch);
void updateStatusFilesFound(const gsize matchCount, statusbarData *status, searchControl *mSearchControl);
gboolean statMatchPhase(const gchar *tmpFullFileName, searchControl *mSearchControl);
void displayMatch(searchControl *mSearchControl, searchData *mSearchData);
void displayQuickMatch(searchControl *mSearchControl, searchData *mSearchData);
void setTimeFromDate(struct tm *tptr, GDate *date);

/* Constructors and destructors for search data (not threaded) */
void createSearchData(GObject *object, const gchar *dataName);
void destroySearchData(gpointer data);
void createSearchControl(GObject *object, const gchar *dataName);
void destroySearchControl(gpointer data);
void createStatusbarData(GObject *object, const gchar *dataName);
void destroyStatusbarData(gpointer data);
void ptr_array_free_cb(gpointer data, gpointer user_data);


#endif /* SEARCH_H */
