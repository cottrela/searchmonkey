#include <gtk/gtk.h>


void
on_window1_destroy                     (GtkObject       *object,
                                        gpointer         user_data);

void
on_menubar1_realize                    (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_menubar1_unrealize                  (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_open_criteria1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_criteria1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_results1_activate              (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_print1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_print_preview1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_print_setup1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_quit1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_word_wrap1_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_set_font1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_set_highligting_colour1_activate    (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_cl_ear_history1_activate            (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_delete1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_copy2_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_toolbar2_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_status_bar1_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_file_name1_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_location1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_size1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_type1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_modified1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_search1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_playButton_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_stopButton_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_configuration1_activate             (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_test1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_reg_expression1_activate            (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_1_search1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_contents1_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_support1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_about1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_playButton_clicked                  (GtkButton       *button,
                                        gpointer         user_data);

void
on_stopButton_clicked                  (GtkButton       *button,
                                        gpointer         user_data);

void
on_Question_clicked                    (GtkButton       *button,
                                        gpointer         user_data);

void
on_searchNotebook_realize              (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_searchNotebook_unrealize            (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_containingText_changed              (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_folderSelector_clicked              (GtkButton       *button,
                                        gpointer         user_data);

void
on_lessThanCheck_toggled               (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_beforeCheck_toggled                 (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_moreThanCheck_toggled               (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_afterCheck_toggled                  (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_expertUserCheck_toggled             (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_treeview1_realize                   (GtkWidget       *widget,
                                        gpointer         user_data);

gboolean
on_treeview1_button_press_event        (GtkWidget       *widget,
                                        GdkEventButton  *event,
                                        gpointer         user_data);

gboolean
on_treeview1_popup_menu                (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_textview1_realize                   (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_textview1_unrealize                 (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_statusbar1_realize                  (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_open1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_copy3_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_delete2_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_explore1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_cancel1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_testRegExDialog_response            (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data);

void
on_SampleTextView_realize              (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_expWizard_response                  (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data);

void
on_expWizard_close                     (GtkDialog       *dialog,
                                        gpointer         user_data);

void
on_startType_changed                   (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_startEntry_changed                  (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_startOccurance_changed              (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_midType_changed                     (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_midEntry_changed                    (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_midOccurance_changed                (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_endType_changed                     (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_endEntry_changed                    (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_endOccurance_changed                (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_regExpWizard1_clicked               (GtkButton       *button,
                                        gpointer         user_data);

void
on_regExpWizard2_clicked               (GtkButton       *button,
                                        gpointer         user_data);

void
on_convertRegex_toggled                (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_addMidContents_clicked              (GtkButton       *button,
                                        gpointer         user_data);

void
on_modifiedSelectedContents_clicked    (GtkButton       *button,
                                        gpointer         user_data);

void
on_deleteSelectedContents_clicked      (GtkButton       *button,
                                        gpointer         user_data);

void
on_midTreeView_realize                 (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_updateSelectedContents_clicked      (GtkButton       *button,
                                        gpointer         user_data);

void
on_midSelection_changed                (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_expWizard_realize                   (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_expWizard_unrealize                 (GtkWidget       *widget,
                                        gpointer         user_data);


void
on_midTreeView_drag_end                (GtkWidget       *widget,
                                        GdkDragContext  *drag_context,
                                        gpointer         user_data);

void
on_notebook2_switch_page               (GtkNotebook     *notebook,
                                        GtkNotebookPage *page,
                                        guint            page_num,
                                        gpointer         user_data);

void
on_configDialog_realize                (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_treeview1_unrealize                 (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_autoFindExe_clicked                 (GtkButton       *button,
                                        gpointer         user_data);

void
on_online_release_notes1_activate      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_configResetAll_clicked              (GtkButton       *button,
                                        gpointer         user_data);

void
on_configSaveNow_clicked               (GtkButton       *button,
                                        gpointer         user_data);

void
on_forums1_activate                    (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_matches1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_window1_unrealize                   (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_window1_realize                     (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_autoAdjustColumnWidth_toggled       (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

gboolean
on_configResultEOL_focus_out_event     (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

gboolean
on_configResultEOF_focus_out_event     (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

gboolean
on_configResultDelimiter_focus_out_event
                                        (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

void
on_newInstance1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_horizontal_results1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_vertical_results1_activate          (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_importCriteria_clicked              (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_exportCriteria_clicked              (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_saveResults_clicked                 (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_printResults_clicked                (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_newInstance2_clicked                (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_playButton2_clicked                 (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_stopButton2_clicked                 (GtkToolButton   *toolbutton,
                                        gpointer         user_data);

void
on_aboutSearchmonkey_response          (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data);

void
on_autosize_columns_activate           (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_edit_file1_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_open_folder1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_containingText_changed              (GtkComboBox     *combobox,
                                        gpointer         user_data);

void
on_searchNotebook_switch_page          (GtkNotebook     *notebook,
                                        GtkNotebookPage *page,
                                        guint            page_num,
                                        gpointer         user_data);

void
on_basic_mode1_toggled                 (GtkCheckMenuItem *checkmenuitem,
                                        gpointer         user_data);

void
on_expert_mode1_toggled                (GtkCheckMenuItem *checkmenuitem,
                                        gpointer         user_data);

void
on_folderDepthCheck_toggled            (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_searchSubfoldersCheck_toggled       (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_dosExpressionRadioFile_activate     (GtkButton       *button,
                                        gpointer         user_data);

void
on_regularExpressionRadioFile_activate (GtkButton       *button,
                                        gpointer         user_data);

void
on_dosExpressionRadioFile_clicked      (GtkButton       *button,
                                        gpointer         user_data);

void
on_regularExpressionRadioFile_clicked  (GtkButton       *button,
                                        gpointer         user_data);

void
on_afterCalenderBtn_clicked            (GtkButton       *button,
                                        gpointer         user_data);

void
on_beforeCalendatBtn_clicked           (GtkButton       *button,
                                        gpointer         user_data);

gboolean
on_regexp_focus_out_event              (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

gboolean
on_regexp_focus_out_event              (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

gboolean
on_regexp_focus_out_event              (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

gboolean
on_fileName_focus                      (GtkWidget       *widget,
                                        GtkDirectionType  direction,
                                        gpointer         user_data);

gboolean
on_regexp2_focus_out_event             (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

void
on_limitResultsCheckResults_toggled    (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_showLinesCheckResults_toggled       (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

gboolean
on_searchNotebook_focus_out_event      (GtkWidget       *widget,
                                        GdkEventFocus   *event,
                                        gpointer         user_data);

void
on_limitContentsCheckResults_toggled   (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_autoComplete_response               (GtkDialog       *dialog,
                                        gint             response_id,
                                        gpointer         user_data);

