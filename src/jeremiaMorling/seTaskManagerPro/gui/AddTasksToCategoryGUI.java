/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.pim.Category;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.seTaskManagerPro.pim.TaskList;
import jeremiaMorling.utils.displayUtils.displays.SortableList;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.vector.ListVector;

/**
 *
 * @author Jeremia
 */
public class AddTasksToCategoryGUI extends SortableList implements CommandListener {
    //private TaskList taskList;
    private TaskListGUI taskListGUI;
    
    public AddTasksToCategoryGUI( TaskListGUI taskListGUI ) {
        super( taskListGUI.getTaskList().getCategory().getName(), MULTIPLE );
        
        this.taskListGUI = taskListGUI;
        
        Category category = taskListGUI.getTaskList().getCategory();
        TaskList allActiveTasks = TaskList.getTaskList( CategoryChooserGUI.ALL_ACTIVE, null, null, this );
        for( int i=0; i<allActiveTasks.size(); i++ ) {
            Task task = allActiveTasks.getTask( i );
            if( task.hasCategoryAssigned( category ) )
                task.setSelected( true );
        }
        setItems( allActiveTasks );
        refresh();
        
        addCommand( new Command( SETMPLoc.getText( "common.done" ), Command.OK, 1 ) );
        addCommand( new Command( SETMPLoc.getText( "common.cancel" ), Command.CANCEL, 2 ) );
        setCommandListener( this );
    }
    
    public void setItemsAndRefreshSameSelection( ListVector items ) {
        super.setItemsAndRefresh( items );
        taskListGUI.getTaskList().refresh();
        taskListGUI.refresh();
    }

    public void commandAction( Command c, Displayable d ) {
        TaskList taskList = taskListGUI.getTaskList();
        switch( c.getCommandType() ) {
            case Command.OK:
                taskList.removeAllElements();
                TaskList allTasks = (TaskList)getItems();
                Category category = taskList.getCategory();
                for( int i=0; i<size(); i++ ) {
                    Task task = allTasks.getTask( i );
                    //task.setSelected( false );
                    if( isSelected( i ) ) {
                        try {
                            task.addCategoryAssignment( category );
                        } catch( Exception e ) {
                            DM.error( e, "AddTasksToCategoryGUI: commandAction, task.addCategoryAssignment()" );
                        }
                        taskList.addElement( task );
                    }
                    else {
                        taskList.removeElement( task );
                        try {
                            task.removeCategoryAssignment( category );
                        } catch( Exception e ) {
                            DM.error( e, "AddTasksToCategoryGUI: commandAction, task.removeCategoryAssignment()" );
                        }
                    }
                }
                DM.back( true );
                break;
            case Command.CANCEL:
                DM.back();
                break;
        }
    }
}
