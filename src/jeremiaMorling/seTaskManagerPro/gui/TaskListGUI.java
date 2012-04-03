package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.rms.RecordStoreException;
import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.pim.PIMException;

import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.pim.Category;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.seTaskManagerPro.pim.TaskList;
import jeremiaMorling.utils.displayUtils.displays.AlertDialog;
import jeremiaMorling.utils.displayUtils.displays.AlertDisplay;
import jeremiaMorling.utils.vector.ListItem;
import jeremiaMorling.utils.displayUtils.displays.SortableList;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.managers.IFocusable;

/**
 * 
 *
 * @author Jeremia M�rling
 */
public class TaskListGUI extends SortableList implements IFocusable, CommandListener {
    //private Task selectedTask;
    private int selectedIndex;
    
    private static TaskListGUI instance;
    
    static final int CMDPRIO_MARK_AS_COMPLETED = 2;
    static final int CMDPRIO_EDIT = 3;
    static final int CMDPRIO_SEND_TASK = 4;
    static final int CMDPRIO_SEND_AS_SMS = 5;
    static final int CMDPRIO_NEW_TASK = 6;
    static final int CMDPRIO_DELETE = 7;
    static final int CMDPRIO_ADD_TASKS = 8;
    
    private final Command ADD_TASKS_COMMAND = new Command( SETMPLoc.getText( "taskList.addTasks" ), Command.SCREEN, CMDPRIO_ADD_TASKS );
    
    public static TaskListGUI show( int mode ) {
        return show( mode, null, null );
    }
    
    public static TaskListGUI show( Category category ) {
        return show( CategoryChooserGUI.CATEGORY, category, null );
    }
    
    public static TaskListGUI show( String searchString ) {
        return show( CategoryChooserGUI.SEARCH, null, searchString );
    }
    
    private static TaskListGUI show( int mode, Category category, String searchString ) {
        if( instance == null )
            instance = new TaskListGUI();
        
        switch( mode ) {
            case CategoryChooserGUI.ALL_ACTIVE:
                instance.setTitle( SETMPLoc.getText( "categoryChooser.allActive" ) );
                break;
            case CategoryChooserGUI.COMPLETED:
                instance.setTitle( SETMPLoc.getText( "categoryChooser.completed" ) );
                break;
            case CategoryChooserGUI.SEARCH:
                instance.setTitle( SETMPLoc.getText( "taskList.searchResult" ) );
                break;
            case CategoryChooserGUI.UNCATEGORIZED:
                instance.setTitle( SETMPLoc.getText( "categoryChooser.uncategorized" ) );
                break;
            case CategoryChooserGUI.CATEGORY:
                instance.setTitle( category.getName() );
                break;
        }
        
        if( mode == CategoryChooserGUI.SEARCH )
            instance.setHideTopItems( true );
        else
            instance.setHideTopItems( false );
        
        instance.removeCommand( instance.ADD_TASKS_COMMAND );
        if( mode == CategoryChooserGUI.CATEGORY )
            instance.addCommand( instance.ADD_TASKS_COMMAND );
        
        DM.add( instance );
        TaskList taskList = TaskList.getTaskList( mode, category, searchString, instance );
        if( taskList == null )
            DM.add( new AlertDisplay( SETMPLoc.getText( "taskList.waitingForTasks.title" ), SETMPLoc.getText( "taskList.waitingForTasks.text" ), RM.getImage( RM.WAIT_64 ) ) );
        else
            instance.setItemsAndRefresh( taskList );
        
        return instance;
    }

    private TaskListGUI() {
        super( SETMPLoc.getText( "taskList.tasks" ), List.IMPLICIT );
        
        appendTopItem( new ListItem( SETMPLoc.getText( "taskList.newTask" ), RM.getImage( RM.NEW_TASK_24 ) ) );

        setSelectCommand( new Command( SETMPLoc.getText( "taskList.showTask" ), Command.OK, 0 ) );
        addCommand( new Command( SETMPLoc.getText( "common.back" ), Command.BACK, 1 ) );
        addCommand( new Command( SETMPLoc.getText( "taskList.markAsCompleted" ), Command.ITEM, CMDPRIO_MARK_AS_COMPLETED ) );
        addCommand( new Command( SETMPLoc.getText( "common.edit" ), Command.ITEM, CMDPRIO_EDIT ) );
        if( Config.isBluetoothSupported() )
            addCommand( new Command( SETMPLoc.getText( "sendTask" ), Command.ITEM, CMDPRIO_SEND_TASK ) );
        else
            addCommand( new Command( SETMPLoc.getText( "taskList.sendAsSMS" ), Command.ITEM, CMDPRIO_SEND_AS_SMS ) );
        addCommand( new Command( SETMPLoc.getText( "taskList.newTask" ), Command.ITEM, CMDPRIO_NEW_TASK ) );
        addCommand( new Command( SETMPLoc.getText( "common.delete" ), Command.ITEM, CMDPRIO_DELETE ) );
        
        setCommandListener( this );
    }
    
    /*public void tasksFinishedLoading( TaskList taskList ) {
        setItemsAndRefresh( taskList );
        DM.back();
    }*/

    public void focusGained( boolean refreshNeeded ) {
        if ( refreshNeeded ) {
            sort();
            //TaskList currentTaskList = (TaskList)getItems();
            //setItemsAndRefresh( TaskList.getTaskList( currentTaskList.getMode(), currentTaskList.getCategory(), this ) );
            
            if ( /*selectedTask == null &&*/ selectedIndex != -1 ) {
                setSelectedIndex( selectedIndex, true );
            }
        }

        /*if( selectedTask != null ) {
            selectedTask.setSelected( false );
            selectedTask = null;
        }*/
        
        selectedIndex = -1;
    }

    /*void setSelectedTask( Task selectedTask ) {
        selectedTask.setSelected( true );
        this.selectedTask = selectedTask;
    }*/

    public void commandAction( Command c, Displayable d ) {
        synchronized( this ) {
            try {
                int commandType = c.getCommandType();
                int cmdPrio = c.getPriority();

                switch ( commandType ) {
                    case Command.OK:
                        if ( !isSelectedItemTopItem() ) {
                            showTask();
                        } else {
                            editTask();
                        }
                        break;
                    case Command.BACK:
                        DM.back( true );
                        break;
                    case Command.ITEM:
                        switch ( cmdPrio ) {
                            case CMDPRIO_MARK_AS_COMPLETED:
                                markAsCompleted();
                                break;
                            case CMDPRIO_EDIT:
                                editTask();
                                break;
                            case CMDPRIO_SEND_TASK:
                                sendTask();
                                break;
                            case CMDPRIO_SEND_AS_SMS:
                                sendAsSMS();
                                break;
                            case CMDPRIO_NEW_TASK:
                                newTask();
                                break;
                            case CMDPRIO_DELETE:
                                deleteTask();
                                break;
                        }
                        break;
                    case Command.SCREEN:
                        DM.add( new AddTasksToCategoryGUI( this ) );
                        break;
                }
            } catch ( Exception e ) {
                DM.error( e, "TaskListGUI: commandAction()" );
            }
        }
    }

    private void showTask() throws IOException {
        //refreshSelectionInfo( false );
        ShowTaskGUI.show( getSelectedTask(), this );
        //DM.add( showTaskGUI );
    }

    void markAsCompleted() throws PIMException {
        //refreshSelectionInfo( false );
        Task selectedTask = getSelectedTask();
        if ( selectedTask != null ) {
            selectedTask.setCompleted( !selectedTask.isCompleted() );
            selectedTask.save();
            selectedTask = null;
            selectedIndex = getSelectedIndex() - 1;
            focusGained( true );
            TaskList.needsResorting();
        }
    }

    void editTask() throws IOException, IllegalArgumentException, RecordStoreException {
        //refreshSelectionInfo( true );
        Task selectedTask = getSelectedTask();
        EditTaskGUI.show( selectedTask, (TaskList)getItems() );
        /*if( selectedTask == null )
            editToDoGUI.editSummary();*/
    }
    
    public Task getSelectedTask() {
        return (Task)getSelectedItem();
    }

    void sendTask() {
        //refreshSelectionInfo( false );
        Task selectedTask = getSelectedTask();
        if( selectedTask != null ) {
            DM.add( new SendTaskGUI( selectedTask ) );
        }
    }
    
    void sendAsSMS() {
        Task selectedTask = getSelectedTask();
        if( selectedTask != null ) {
            try {
                DM.add( new SMSTaskSenderGUI( selectedTask ) );
            } catch( Exception e ) {
                DM.error( e, "TaskListGUI: sendTaskAsSMS" );
            }
        }
    }

    void newTask() throws IllegalArgumentException, IOException, RecordStoreException {
        EditTaskGUI editToDoGUI = EditTaskGUI.show( null, (TaskList)getItems() );
        //DM.add( editToDoGUI );
        //editToDoGUI.editSummary();
    }

    void deleteTask() throws IOException {
        if ( !(isSelectedItemTopItem()) ) {
            DM.add( new DeleteTaskDialog() );
        }
    }
    
    public TaskList getTaskList() {
        return (TaskList)getItems();
    }

    private class DeleteTaskDialog extends AlertDialog implements CommandListener {
        public DeleteTaskDialog() throws IOException {
            super(
                    SETMPLoc.getText( "taskList.deleteTask.title" ),
                    getSelectedTask().getSummary() + SETMPLoc.getText( "common.deleteQuestion" ),
                    RM.getImage( RM.WARNING_64 ) );
            addCommand( new Command( SETMPLoc.getText( "common.yes" ), Command.OK, 0 ) );
            addCommand( new Command( SETMPLoc.getText( "common.no" ), Command.CANCEL, 0 ) );
            setCommandListener( this );
        }

        public void commandAction( Command c, Displayable d ) {
            try {
                switch ( c.getCommandType() ) {
                    case Command.OK:
                        deleteTask();
                        break;
                    case Command.CANCEL:
                        DM.back();
                        break;
                }
            } catch ( Exception e ) {
                DM.error( e, "TaskListGUI, DeleteDialog: commandAction()" );
            }
        }

        private void deleteTask() throws PIMException {
            TaskList.deleteTask( getSelectedTask() );
            deleteSelectedItem();
            /*int selectedIndex = getSelectedIndex();
            delete( selectedIndex );
            int refreshEndIndex = Math.min( selectedIndex+6, size() );
            for( int i=selectedIndex+1; i<refreshEndIndex; i++ )
                refresh( i );*/
            DM.back( false );
        }
    }
}
