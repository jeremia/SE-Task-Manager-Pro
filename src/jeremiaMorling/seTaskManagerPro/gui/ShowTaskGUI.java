package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.utils.displayUtils.displays.MessageBox;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.displayUtils.time.DateTimeFormatter;
import jeremiaMorling.utils.vector.IntVector;

public class ShowTaskGUI extends MessageBox /*implements Runnable*/ {

    private TaskListGUI taskListGUI;
    private static final Font SUMMARY_FONT = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL );
    private static final Font DEFAULT_FONT = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL );
    
    private Image prioHighIcon;
    private Image prioNormalIcon;
    private Image prioLowIcon;
    private Image categoryIcon;
    private Image taskNotCompletedIcon;
    private Image taskCompletedIcon;
    private Image reminderIcon;
    
    private static ShowTaskGUI instance;
    
    public static ShowTaskGUI show( Task task, TaskListGUI taskListGUI ) {
        if( instance == null )
            instance = new ShowTaskGUI( task, taskListGUI );
        else {
            instance.showTask( task );
            instance.repaint();
        }
        
        DM.add( instance );
        
        return instance;
    }

    private ShowTaskGUI( Task task, TaskListGUI taskListGUI ) {
        super( task.getSummary() );

        this.taskListGUI = taskListGUI;
        
        prioHighIcon = RM.getImage( RM.PRIO_HIGH_16 );
        prioNormalIcon = RM.getImage( RM.PRIO_NORMAL_16 );
        prioLowIcon = RM.getImage( RM.PRIO_LOW_16 );
        categoryIcon = RM.getImage( RM.CATEGORY_16 );
        taskNotCompletedIcon = RM.getImage( RM.TASK_NOT_COMPLETED_16 );
        taskCompletedIcon = RM.getImage( RM.TASK_COMPLETED_16 );
        reminderIcon = RM.getImage( RM.REMINDER_16 );
        
        showTask( task );

        addCommand( new Command( SETMPLoc.getText( "common.back" ), Command.BACK, 1 ) );
        
        addCommand( new Command( SETMPLoc.getText( "taskList.markAsCompleted" ), Command.ITEM, TaskListGUI.CMDPRIO_MARK_AS_COMPLETED ) );
        addCommand( new Command( SETMPLoc.getText( "common.edit" ), Command.ITEM, TaskListGUI.CMDPRIO_EDIT ) );
        if( Config.isBluetoothSupported() )
            addCommand( new Command( SETMPLoc.getText( "sendTask" ), Command.ITEM, TaskListGUI.CMDPRIO_SEND_TASK ) );
        else
            addCommand( new Command( SETMPLoc.getText( "taskList.sendAsSMS" ), Command.ITEM, TaskListGUI.CMDPRIO_SEND_AS_SMS ) );
        addCommand( new Command( SETMPLoc.getText( "common.delete" ), Command.ITEM, TaskListGUI.CMDPRIO_DELETE ) );
        setCommandListener( this );
    }
    
    private void showTask( Task task ) {
        removeMessageItems();
        setTitle( task.getSummary() );
        
        addMessageItem( task.getSummary(), SUMMARY_FONT );

        if ( !task.getNote().equals( "" ) ) {
            addMessageItem( task.getNote(), DEFAULT_FONT );
        }

        if ( task.getPriority() == Task.PRIO_HIGH ) {
            addMessageItem( prioHighIcon, SETMPLoc.getText( "showTask.highPriority" ), DEFAULT_FONT );
        } else if ( task.getPriority() == Task.PRIO_LOW ) {
            addMessageItem( prioLowIcon, SETMPLoc.getText( "showTask.lowPriority" ), DEFAULT_FONT );
        } else {
            addMessageItem( prioNormalIcon, SETMPLoc.getText( "showTask.normalPriority" ), DEFAULT_FONT );
        }

        IntVector assignedCategories = task.getAssignedCategories();
        if( assignedCategories != null && assignedCategories.size() > 0 ) {
            addMessageItem( categoryIcon, task.getAssignedCategoriesAsString(), DEFAULT_FONT );
        }

        if ( !task.isCompleted() ) {
            addMessageItem( taskNotCompletedIcon, SETMPLoc.getText( "showTask.notCompleted" ), DEFAULT_FONT );
        } else {
            addMessageItem( taskCompletedIcon, SETMPLoc.getText( "showTask.completed" ), DEFAULT_FONT );
        }

        if ( task.getReminder() != null ) {
            addMessageItem( reminderIcon, DateTimeFormatter.formatShortDateTime( task.getReminder() ), DEFAULT_FONT );
        }

        if ( task.getDueDate() != null ) {
            if( Config.isDueDateTimeSupported() )
                addMessageItem( SETMPLoc.getText( "showTask.dueDate" ) + " " + DateTimeFormatter.formatShortDateTime( task.getDueDate() ), DEFAULT_FONT );
            else
                addMessageItem( SETMPLoc.getText( "showTask.dueDate" ) + " " + DateTimeFormatter.formatShortDate( task.getDueDate() ), DEFAULT_FONT );
        }

        if ( task.getCompletionDate() != null ) {
            addMessageItem( SETMPLoc.getText( "task.completionDate" ) + " " + DateTimeFormatter.formatShortDateTime( task.getCompletionDate() ), DEFAULT_FONT );
        }

        addMessageItem( SETMPLoc.getText( "task.lastEditDate" ) + " " + DateTimeFormatter.formatShortDateTime( task.getLastEditDate() ), DEFAULT_FONT );
    }

    public void commandAction( Command c, Displayable d ) {
        try {
            int commandType = c.getCommandType();
            int cmdPrio = c.getPriority();

            switch ( commandType ) {
                case Command.BACK:
                    DM.back();
                    break;
                case Command.ITEM:
                    DM.back();
                    switch ( cmdPrio ) {
                        case TaskListGUI.CMDPRIO_MARK_AS_COMPLETED:
                            taskListGUI.markAsCompleted();
                            break;
                        case TaskListGUI.CMDPRIO_EDIT:
                            taskListGUI.editTask();
                            break;
                        case TaskListGUI.CMDPRIO_SEND_TASK:
                            taskListGUI.sendTask();
                            break;
                        case TaskListGUI.CMDPRIO_SEND_AS_SMS:
                            taskListGUI.sendAsSMS();
                            break;
                        case TaskListGUI.CMDPRIO_DELETE:
                            taskListGUI.deleteTask();
                            break;
                    }
                    break;
                default:
                    super.commandAction( c, d );
            }
        } catch ( Exception e ) {
            DM.error( e, "ShowTaskGUI: commandAction()" );
        }
    }
}
