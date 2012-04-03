package jeremiaMorling.seTaskManagerPro.gui;

import java.util.Date;
import javax.microedition.lcdui.Alert;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Spacer;

import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.seTaskManagerPro.pim.TaskList;
import jeremiaMorling.utils.displayUtils.displays.AlertDialog;
import jeremiaMorling.utils.displayUtils.items.BooleanSettingViewer;
import jeremiaMorling.utils.displayUtils.items.ChoiceItem;
import jeremiaMorling.utils.displayUtils.items.ChoiceSettingViewer;
import jeremiaMorling.utils.displayUtils.items.CustomDateSettingViewer;
import jeremiaMorling.utils.displayUtils.items.IDateSettingViewer;
import jeremiaMorling.utils.displayUtils.items.IEditListener;
import jeremiaMorling.utils.displayUtils.items.NativeDateSettingViewer;
import jeremiaMorling.utils.displayUtils.items.TextSettingViewer;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.managers.IFocusable;
import jeremiaMorling.utils.vector.IntVector;

/**
 * 
 *
 * @author Jeremia M�rling
 */
public class EditTaskGUI extends Form implements IFocusable, CommandListener, IEditListener {

    private Task task;
    private TaskList taskList;
    private TextSettingViewer summary;
    private ChoiceSettingViewer priority;
    private CategorySettingViewer categories;
    private IDateSettingViewer reminder;
    private IDateSettingViewer dueDate;
    private TextSettingViewer note;
    private BooleanSettingViewer completed;
    
    private boolean creatingNewTask = false;
    private int editingStep;
    private static final int EDITING_SUMMARY = 0;
    private static final int EDITING_PRIORITY = 1;
    private static final int EDITING_CATEGORIES = 2;
    private static final int EDITING_REMINDER = 3;
    private static final int EDITING_DUE_DATE = 4;
    private static final int EDITING_NOTE = 5;
    
    //private boolean changeMade = false;
    
    private static EditTaskGUI instance;
    
    private static final int SPACE = 5;
    
    private static final String DEFAULT_SUMMARY = "";
    private static final int DEFAULT_PRIORITY = Task.PRIO_NORMAL;
    private static final String DEFAULT_NOTE = "";
    private static final Date DEFAULT_REMINDER = null;
    private static final Date DEFAULT_DUE_DATE = null;
    private static final boolean DEFAULT_COMPLETED = false;
    //private static final IntVector DEFAULT_ASSIGNED_CATEGORIES = null;
    
    public static EditTaskGUI show( Task task, TaskList taskList ) {
        
        if( instance == null )
            instance = new EditTaskGUI( task, taskList );
        else {
            instance.taskList = taskList;
            instance.editTask( task );
        }
        
        DM.add( instance );
        //instance.changeMade = false;
        if( task != null ) {
            instance.creatingNewTask = false;
            instance.summary.setFocus();
        }
        else {
            instance.creatingNewTask = true;
            instance.editingStep = EDITING_SUMMARY;
            instance.summary.edit();
        }
        
        return instance;
    }

    private EditTaskGUI( Task task, TaskList taskList ) {
        super( SETMPLoc.getText( "editTask" ) );
        
        this.task = task;
        this.taskList = taskList;
        
        if( task != null ) {
            buildGUI(
                    task.getSummary(),
                    task.getPriority(),
                    task.getNote(),
                    task.getReminder(),
                    task.getDueDate(),
                    task.isCompleted(),
                    task.getAssignedCategories() );
        } else {
            buildGUI(
                    DEFAULT_SUMMARY,
                    DEFAULT_PRIORITY,
                    DEFAULT_NOTE,
                    DEFAULT_REMINDER,
                    DEFAULT_DUE_DATE,
                    DEFAULT_COMPLETED,
                    getDefaultAssignedCategories() );
        }

        addCommand( new Command( SETMPLoc.getText( "editTask.save" ), Command.OK, 2 ) );
        addCommand( new Command( SETMPLoc.getText( "common.cancel" ), Command.CANCEL, 2 ) );
        setCommandListener( this );
    }

    public void editTask( Task task ) {
        this.task = task;
        if( task != null )
            setValues(
                    task.getSummary(),
                    task.getPriority(),
                    task.getNote(),
                    task.getReminder(),
                    task.getDueDate(),
                    task.isCompleted(),
                    task.getAssignedCategories()
                    );
        else {
            
            setValues(
                    DEFAULT_SUMMARY,
                    DEFAULT_PRIORITY,
                    DEFAULT_NOTE,
                    DEFAULT_REMINDER,
                    DEFAULT_DUE_DATE,
                    DEFAULT_COMPLETED,
                    getDefaultAssignedCategories()
                    );
        }
    }
    
    private IntVector getDefaultAssignedCategories() {
        if( taskList.getMode() == CategoryChooserGUI.CATEGORY ) {
            IntVector assignedCategories = new IntVector( 1 );
            assignedCategories.addElement( taskList.getCategory().getId() );
            return assignedCategories;
        }
        else
            return null;
    }
    
    /*private boolean getDefaultCompleted() {
        if( taskList.getMode() == CategoryChooserGUI.COMPLETED )
            return true;
        else
            return false;
    }*/

    private void buildGUI( String summaryValue, int priorityValue, String noteValue, Date reminderDateValue, Date dueDateValue, boolean completedValue, IntVector assignedCategories ) {
        summary = addTextSettingViewer( "task.summary", summaryValue, RM.getImage( RM.SUMMARY_16 ), Config.getSummaryMaxLength(), true );
        addSpace();
        ChoiceItem[] priorites = new ChoiceItem[]{
            new ChoiceItem( SETMPLoc.getText( "prio.high" ), RM.getImage( RM.PRIO_HIGH_16 ), RM.getImage( RM.PRIO_HIGH_24 ) ),
            new ChoiceItem( SETMPLoc.getText( "prio.normal" ), RM.getImage( RM.PRIO_NORMAL_16 ), RM.getImage( RM.PRIO_NORMAL_24 ) ),
            new ChoiceItem( SETMPLoc.getText( "prio.low" ), RM.getImage( RM.PRIO_LOW_16 ), RM.getImage( RM.PRIO_LOW_24 ) )
        };
        priority = addChoiceSettingViewer( "task.priority", priorites, getPriorityIndex( priorityValue ), true );
        addSpace();
        categories = addCategorySettingViewer( "task.categories", assignedCategories, RM.getImage( RM.CATEGORY_16 ), true );addSpace();
        addSpace();
        if( Config.isReminderSupported() ) {
            reminder = addDateSettingViewer( "task.reminder", reminderDateValue, RM.getImage( RM.REMINDER_16 ), true, true );
            addSpace();
        }
        dueDate = addDateSettingViewer( "task.dueDate", dueDateValue, RM.getImage( RM.DUE_DATE_16 ), Config.isDueDateTimeSupported(), true );
        addSpace();
        note = addTextSettingViewer( "task.note", noteValue, RM.getImage( RM.NOTE_16 ), Config.getNoteMaxLength(), true );
        addSpace();
        completed = addBooleanSettingViewer( "task.completed", completedValue, RM.getImage( RM.TASK_NOT_COMPLETED_16 ), RM.getImage( RM.TASK_COMPLETED_16 ), true );
    }
    
        private void setValues(
            String summaryValue,
            int priorityValue,
            String noteValue,
            Date reminderValue,
            Date dueDateValue,
            boolean completedValue,
            IntVector assignedCategories ) {
        summary.setValue( summaryValue );
        priority.setSelectedIndex( getPriorityIndex( priorityValue ) );
        note.setValue( noteValue );
        if( Config.isReminderSupported() )
            reminder.setDate( reminderValue );
        dueDate.setDate( dueDateValue );
        completed.setValue( completedValue );
        categories.setAssignedCategories( assignedCategories );
    }

    private void addSpace() {
        append( new Spacer( getWidth(), SPACE ) );
    }

    private TextSettingViewer addTextSettingViewer( String localizedLabel, String value, Image icon, int maxSize, boolean enabled ) {
        TextSettingViewer textSettingViewer = new TextSettingViewer( this, SETMPLoc.getText( localizedLabel ), value, icon, maxSize, enabled );
        textSettingViewer.setEditListener( this );
        return textSettingViewer;
    }

    private ChoiceSettingViewer addChoiceSettingViewer( String localizedLabel, ChoiceItem choices[], int selectedIndex, boolean enabled ) {
        ChoiceSettingViewer choiceSettingViewer = new ChoiceSettingViewer( this, SETMPLoc.getText( localizedLabel ), choices, selectedIndex, enabled );
        choiceSettingViewer.setEditListener( this );
        return choiceSettingViewer;
    }

    private CategorySettingViewer addCategorySettingViewer( String localizedLabel, IntVector assignedCategories, Image icon, boolean enabled ) {
        CategorySettingViewer categorySettingViewer = new CategorySettingViewer( this, SETMPLoc.getText( localizedLabel ), assignedCategories, icon, enabled );
        categorySettingViewer.setEditListener( this );
        return categorySettingViewer;
    }

    private IDateSettingViewer addDateSettingViewer( String localizedLabel, Date date, Image icon, boolean dateTimeSupported, boolean enabled ) {
        IDateSettingViewer dateSettingViewer;
        if( !Config.isSonyEricssonNonSmartPhone() )
            dateSettingViewer = new NativeDateSettingViewer( this, SETMPLoc.getText( localizedLabel ), date, dateTimeSupported );
        else {
            dateSettingViewer = new CustomDateSettingViewer( this, SETMPLoc.getText( localizedLabel ), date, icon, dateTimeSupported, enabled );
            ((CustomDateSettingViewer)dateSettingViewer).setEditListener( this );
        }
        return dateSettingViewer;
    }

    private BooleanSettingViewer addBooleanSettingViewer( String localizedLabel, boolean value, Image noIcon, Image yesIcon, boolean enabled ) {
        BooleanSettingViewer booleanSettingViewer = new BooleanSettingViewer( this, SETMPLoc.getText( localizedLabel ), value, noIcon, yesIcon, enabled );
        return booleanSettingViewer;
    }

    public static int getPriorityIndex( int value ) {
        return value - 1;
    }

    public static int getPriorityValue( int index ) {
        return index + 1;
    }

    public void focusGained( boolean refreshNeeded ) {
        /*if( refreshNeeded )
            changeMade = true;*/
        
        switch( editingStep ) {
            case EDITING_SUMMARY:
                if( creatingNewTask && !refreshNeeded )
                    DM.back();
                else if( summary.getValue().equals( "" ) ) {
                    summary.edit();
                    DM.newAlert( SETMPLoc.getText( "editTask.validationError" ), SETMPLoc.getText( "editTask.summaryMandatory" ), RM.getImage( RM.ERROR_64 ), Alert.FOREVER );
                } else if( creatingNewTask ) {
                    editingStep = EDITING_PRIORITY;
                    priority.edit();
                } else if( refreshNeeded ) {
                    priority.setFocus();
                }
                break;
            case EDITING_PRIORITY:
                if( creatingNewTask ) {
                    if( refreshNeeded ) {
                        categories.setFocus();
                        creatingNewTask = false;
                    } else {
                        editingStep = EDITING_SUMMARY;
                        summary.edit();
                    }
                } else if( refreshNeeded )
                    categories.setFocus();
                break;
            case EDITING_CATEGORIES:
                if( refreshNeeded ) {
                    if( Config.isReminderSupported() )
                        reminder.setFocus();
                    else
                        dueDate.setFocus();
                }
                break;
            case EDITING_REMINDER:
                if( refreshNeeded )
                    dueDate.setFocus();
                break;
            case EDITING_DUE_DATE:
                if( refreshNeeded )
                    note.setFocus();
                break;
            case EDITING_NOTE:
                if( refreshNeeded )
                    completed.setFocus();
                break;
        }
        
    }

    public void commandAction( Command c, Displayable d ) {
        int commandType = c.getCommandType();

        switch( commandType ) {
            case Command.OK:
                save();
                break;
            case Command.CANCEL:
                if( !changesMade() )
                    DM.back( false );
                else
                    DM.add( new CancelWarning(), true );
                break;
            case Command.ITEM:
                if( c instanceof NativeDateSettingViewer.DateRemoverCommand ) {
                    int id = ((NativeDateSettingViewer.DateRemoverCommand)c).getId();
                    if( id == reminder.getId() )
                        reminder.setDate( null );
                    else if( id == dueDate.getId() )
                        dueDate.setDate( null );
                }
                break;
        }
    }
    
        private void save() {
        try {
            if( task == null )
                task = taskList.createNewTask();
            task.setSummary( summary.getValue() );
            task.setNote( note.getValue() );
            task.setPriority( getPriorityValue( priority.getSelectedIndex() ) );
            if( Config.isReminderSupported() )
                task.setReminder( reminder.getDate() );
            task.setDueDate( dueDate.getDate() );
            task.setCompleted( completed.getValue() );
            task.save();
            task.setAssignedCategories( categories.getAssignedCategories() );
            task.setSelected( true );
            TaskList.needsResorting();
            DM.back( true );
            return;
        } catch( Exception e ) {
            DM.error( e, "EditTaskGUI: commandAction()" );
        }
    }
    
    private boolean changesMade() {
        if( !summary.getValue().equals( task.getSummary() ) )
            return true;
        else if( getPriorityValue( priority.getSelectedIndex() ) != task.getPriority() )
            return true;
        else if( !equals( categories.getAssignedCategories(), task.getAssignedCategories() ) )
            return true;
        else if( !equals( reminder.getDate(), task.getReminder() ) )
            return true;
        else if( !equals( dueDate.getDate(), task.getDueDate() ) )
            return true;
        else if( !note.getValue().equals( task.getNote() ) )
            return true;
        else if( completed.getValue() != task.isCompleted() )
            return true;
        else
            return false;
    }
    
    private static boolean equals( IntVector vector1, IntVector vector2 ) {
        if( vector1 == null && vector2 == null )
            return true;
        else if( vector1 == null || vector2 == null )
            return false;
        else
            return vector1.equals( vector2 );
    }
    
    private static boolean equals( Date date1, Date date2 ) {
        if( date1 == null && date2 == null )
            return true;
        else if( date1 == null || date2 == null )
            return false;
        else
            return date1.equals( date2 );
    }

    public void editing( int id ) {
        if( id == summary.getId() )
            editingStep = EDITING_SUMMARY;
        else if( id == priority.getId() )
            editingStep = EDITING_PRIORITY;
        else if( id == categories.getId() )
            editingStep = EDITING_CATEGORIES;
        else if( id == note.getId() )
            editingStep = EDITING_NOTE;
        else if( Config.isSonyEricssonNonSmartPhone() ) {
            if( Config.isReminderSupported() && id == ((CustomDateSettingViewer)reminder).getId() )
                editingStep = EDITING_REMINDER;
            else if( id == ((CustomDateSettingViewer)dueDate).getId() )
                editingStep = EDITING_DUE_DATE;
        }
    }
    
    private class CancelWarning extends AlertDialog implements CommandListener {
        private CancelWarning() {
            super(
                    SETMPLoc.getText( "editTask.cancelWarning.title" ),
                    SETMPLoc.getText( "editTask.cancelWarning.text" ),
                    RM.getImage( RM.WARNING_64 ) );
            
            addCommand( new Command( SETMPLoc.getText( "common.yes" ), Command.OK, 0 ) );
            addCommand( new Command( SETMPLoc.getText( "common.no" ), Command.CANCEL, 1 ) );
            setCommandListener( this );
        }

        public void commandAction( Command c, Displayable d ) {
            switch( c.getCommandType() ) {
                case Command.OK:
                    save();
                    break;
                case Command.CANCEL:
                    DM.back( false );
                    break;
            }
        }
    }
}
