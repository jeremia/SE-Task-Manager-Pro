package jeremiaMorling.seTaskManagerPro.pim;

import com.sonyericsson.pim.PIMChangeEvent;
import com.sonyericsson.pim.PIMChangeList;
import com.sonyericsson.pim.PIMChangeListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.gui.CategoryChooserGUI;
import jeremiaMorling.utils.displayUtils.displays.SortableList;
import jeremiaMorling.utils.vector.ListVector;
import jeremiaMorling.utils.managers.DM;

/**
 * 
 *
 * @author Jeremia M�rling
 */
public class TaskList extends ListVector {
    private int mode;
    private Category category;
    private String searchString;
    
    private static final Object TASKS_SYNCHRONIZER = new Object();
    private static TaskList tasks;
    private static ToDoList toDoList;
    private static boolean areTasksLoaded = false;
    private static boolean waitingForTasksToLoad = false;
    private static boolean needsResorting = false;
    
    private static String lastChangedUID;
    
    private static SortableList sortableList;
    private static int modeForReturningTaskList;
    private static Category categoryForReturningTaskList;
    private static String searchStringForReturingTaskList;
    
    public static TaskList getTaskList( int mode, Category category, String searchString, SortableList sortableList ) {
        TaskList.sortableList = sortableList;
        TaskList.modeForReturningTaskList = mode;
        TaskList.categoryForReturningTaskList = category;
        TaskList.searchStringForReturingTaskList = searchString;
       
        if( !areTasksLoaded ) {
            waitingForTasksToLoad = true;
            return null;
        }

        synchronized( TASKS_SYNCHRONIZER ) {
            return new TaskList( mode, category, searchString );
        }
    }
    
    public static void loadTasks() {
        Thread tasksLoader = new Thread() {
            public void run() {
                refreshBaseTaskList();
            }
        };
        tasksLoader.start();
    }
    
    /*private static void createBaseTaskListAndRefresh() {
        //waitingForTasksToLoad = true;
        if( !createBaseTaskList() )
            refreshSelectedTaskList();
    }*/
    
    public static void refreshBaseTaskList() {
        synchronized( TASKS_SYNCHRONIZER ) {
            try {
                areTasksLoaded = false;
                tasks = new TaskList();
                reAddTasksToBaseTaskList();
                readAssignedCategories();
                areTasksLoaded = true;
                refreshSelectedTaskList();
                if( waitingForTasksToLoad ) {
                    DM.back();
                    waitingForTasksToLoad = false;
                }
                
            } catch( Exception e ) {
                DM.error( e, "TaskList, TasksLoader: run()" );
            }
        }
    }
    
    private static void refreshSelectedTaskList() {
        if( sortableList != null )
            sortableList.setItemsAndRefreshSameSelection( getTaskList( modeForReturningTaskList, categoryForReturningTaskList, searchStringForReturingTaskList, sortableList ) );
    }
    
    public static void needsResorting() {
        needsResorting = true;
    }
    
    public static void createToDoList() throws PIMException {
        toDoList = (ToDoList)PIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.READ_WRITE );
    }

    private TaskList() throws RecordStoreException, IOException, PIMException {
        if( Config.isPIMChangeListenerSupported() )
            ((PIMChangeList)toDoList).setPIMChangeListener( new PIMEventListener() );
    }
    
    private TaskList( int mode, Category category, String searchString ) {
        this.mode = mode;
        this.category = category;
        this.searchString = searchString;
        
        refresh();
        //sort();
    }
    
    public int getMode() {
        return mode;
    }
    
    public Category getCategory() {
        return category;
    }
    
    private void addAllActiveTasks() {
        for( int i=0; i<tasks.size(); i++ ) {
            Task task = tasks.getTask( i );
            if( !task.isCompleted() )
                addElement( task );
            else
                break;
        }
    }
    
    private void addCompletedTasks() {
        if( tasks.size() == 0 || !tasks.getTask( tasks.size()-1 ).isCompleted() )
            return;
        
        int firstCompletedTaskIndex = tasks.size()-1;
        for( int i=tasks.size()-2; i>=0; i-- ) {
            if( !tasks.getTask( i ).isCompleted() ) {
                firstCompletedTaskIndex = i+1;
                break;
            }
        }
        
        for( int i=firstCompletedTaskIndex; i<tasks.size(); i++ ) {
            addElement( tasks.getTask( i ) );
        }
    }
    
    private void addTasksFromSearch( String searchString ) {
        String lowerCaseSearchString = searchString.toLowerCase();
        for( int i=0; i<tasks.size(); i++ ) {
            Task task = tasks.getTask( i );
            if( task.getLowerCaseText().indexOf( lowerCaseSearchString ) != -1 ||
                    task.getNote().toLowerCase().indexOf( lowerCaseSearchString ) != -1 )
                addElement( task );
        }
    }
    
    private void addUncategorizedTasks() {
        for( int i=0; i<tasks.size(); i++ ) {
            Task task = tasks.getTask( i );
            if( !task.hasAssignedCategories() )
                addElement( task );
        }
    }
    
    private void addTasksWithAssignedCategory( Category category ) {
        for( int i=0; i<tasks.size(); i++ ) {
            Task task = tasks.getTask( i );
            if( task.hasCategoryAssigned( category ) )
                addElement( task );
        }
    }

    private static void reAddTasksToBaseTaskList() throws PIMException, RecordStoreException, IOException {
        tasks.removeAllElements();

        Enumeration toDos = toDoList.items();
        while( toDos.hasMoreElements() ) {
            ToDo toDo = (ToDo)toDos.nextElement();
            tasks.addElement( new Task( toDo ) );
        }
        
        tasks.sort();
        //readAssignedCategories();
    }
    
    public void refresh() {
        removeAllElements();
        switch( mode ) {
            case CategoryChooserGUI.ALL_ACTIVE:
                addAllActiveTasks();
                break;
            case CategoryChooserGUI.COMPLETED:
                addCompletedTasks();
                break;
            case CategoryChooserGUI.SEARCH:
                addTasksFromSearch( searchString );
                break;
            case CategoryChooserGUI.UNCATEGORIZED:
                addUncategorizedTasks();
                break;
            default:
                if( category != null )
                    addTasksWithAssignedCategory( category );
        }
    }

    public Task getTask( int index ) {
        return (Task)elementAt( index );
    }
    
    public static void close() throws PIMException {
    	toDoList.close();
    }
    
    public void addElement( Object element ) {
        if( !(element instanceof Task) )
            throw new IllegalArgumentException( "element has to be of type Task, but was of type " + element.getClass().getName() );
        
        super.addElement( element );
    }
    
    public static void sortBaseTasks() {
        if( !needsResorting )
            return;
        
        areTasksLoaded = false;
        Thread tasksSorterThread = new Thread() {
            public void run() {
                tasks.sort();
                areTasksLoaded = true;
                needsResorting = false;
            }
        };
        tasksSorterThread.start();
    }
    
    public int indexOf( Object elem ) {
        for( int i = 0; i < elementCount; i++ ) {
            if( getTask( i ).equals( elem ) ) {
                return i;
            }
        }

        return -1;
    }
    
    public Task createNewTask() {
    	ToDo toDo = toDoList.createToDo();
    	Task task = new Task( toDo );
        tasks.addElement( task );
        needsResorting();
    	addElement( task );
    	return task;
    }
    
    public static boolean deleteTask( Task task ) throws PIMException {
        lastChangedUID = task.getUID();
    	toDoList.removeToDo( task.getToDo() );
        return tasks.removeElement( task );
    }
    
    static void setLastChangedUID( String lastChangedUID ) {
        TaskList.lastChangedUID = lastChangedUID;
    }
    
    public static void deletingCategory( Category categoryToDelete ) throws RecordStoreException, IOException {
        for( int i=0; i<tasks.size(); i++ )
            tasks.getTask( i ).removeCategoryAssignment( categoryToDelete );
    }

    public Task getTask( String UID ) {
        int index = indexOf( UID );
        if( index != -1 )
            return tasks.getTask( index );
        else
            return null;
    }
    
    private void addedTask( String UID ) {
        try {
            ToDo addedToDo = getAddedOrModifiedToDo( UID );
            Task addedTask = new Task( addedToDo );
            tasks.addElement( addedTask );
            tasks.sort();
            refreshSelectedTaskList();
        } catch( Exception e ) {
            DM.error( e, "TaskList: addedTask(): " + UID );
            //createBaseTaskList();
        }
    }
    
    private void modifiedTask( String UID ) {
        try {
            Task taskToBeModified = getTask( UID );
            ToDo modifiedToDo = getAddedOrModifiedToDo( UID );
            taskToBeModified.setToDo( modifiedToDo );
            sortableList.sort();
            tasks.sort();
        } catch( Exception e ) {
            DM.error( e, "TaskList: modifiedTask(): " + UID );
            //createBaseTaskList();
        }
    }
    
    private ToDo getAddedOrModifiedToDo( String UID ) throws PIMException {
        try {
            Enumeration lastToDos = toDoList.items();
            while( lastToDos.hasMoreElements() ) {
                ToDo toDo = (ToDo)lastToDos.nextElement();
                if( toDo.getString( ToDo.UID, 0 ).equals( UID ) )
                    return toDo;
            }
            
            throw new IllegalStateException( "ToDo with UID " + UID + " does not exist in the ToDoList." );
        } catch( Exception e ) {
            DM.error( e, "TaskList: getNewOrModifiedToDo" );
            return null;
        }
    }
    
    private void deletedTask( String UID ) {
        int index = tasks.indexOf( UID );
        tasks.removeElementAt( index );
        sortableList.delete( UID );
    }
    
    public static boolean isReminderSupported() {
        synchronized( TASKS_SYNCHRONIZER ) {
            return (toDoList.isSupportedField( Task.REMINDER ) &&
                   (toDoList.getFieldDataType( Task.REMINDER ) == PIMItem.DATE));
        }
    }

    /*public static boolean isTaskSendingSupported() {
        PIM pim = PIM.getInstance();
        String[] supportedSerialFormats = pim.supportedSerialFormats( PIM.TODO_LIST );
        for( int i=0; i<supportedSerialFormats.length; i++ ) {
            if( supportedSerialFormats[i].equals( FORMAT_VCALENDAR10 ) )
                return true;
        }

        return false;
    }*/

    private static void readAssignedCategories() throws RecordStoreException, IOException {
        RecordStore rs = RecordStore.openRecordStore( Task.ASSIGNED_CATEGORIES_RS, true );
        RecordEnumeration assignedCategoriesRecord = rs.enumerateRecords( null, null, false );

        while( assignedCategoriesRecord.hasNextElement() ) {
            int recordId = assignedCategoriesRecord.nextRecordId();
            byte[] record = rs.getRecord( recordId );
            ByteArrayInputStream inputStream = new ByteArrayInputStream( record );
            DataInputStream dataInputStream = new DataInputStream( inputStream );

            String uid = dataInputStream.readUTF();
            Task task = tasks.getTask( uid );
            if( task == null ) {
                rs.deleteRecord( recordId );
                continue;
            }

            task.readAssignedCategories( dataInputStream, recordId );

            inputStream.close();
            dataInputStream.close();
        }

        assignedCategoriesRecord.destroy();
        rs.closeRecordStore();
    }

    public static Object[] testFieldConstraints() {
        synchronized( TASKS_SYNCHRONIZER ) {
            try {
                Object[] result = new Object[3];

                final int TEST_MINUTE = 13;
                Calendar inputDate = Calendar.getInstance();
                inputDate.set( Calendar.MINUTE, TEST_MINUTE );

                StringBuffer thousandCharsBuffer = new StringBuffer( 1000 );
                final String TEN_CHARS = "1234567890";
                for( int i = 0; i < 100; i++ ) {
                    thousandCharsBuffer.append( TEN_CHARS );
                }
                final String THOUSAND_CHARS = thousandCharsBuffer.toString();

                Task dummyTask = new Task( toDoList.createToDo() );

                dummyTask.setDueDate( inputDate.getTime() );
                //dummyToDo.addDate( ToDo.DUE, ToDo.ATTR_NONE, inputDate.getTime().getTime() );
                dummyTask.setSummary( THOUSAND_CHARS );
                //dummyToDo.addString( ToDo.SUMMARY, ToDo.ATTR_NONE, THOUSAND_CHARS );
                dummyTask.setNote( THOUSAND_CHARS );
                //dummyToDo.addString( ToDo.NOTE, ToDo.ATTR_NONE, THOUSAND_CHARS );

                dummyTask.save();
                //dummyToDo.commit();
                //setLastChangedUID( dummyToDo.getString( ToDo.UID, 0) );

                Calendar outputDate = Calendar.getInstance();
                outputDate.setTime( dummyTask.getDueDate() );
                if( outputDate.get( Calendar.MINUTE ) == TEST_MINUTE ) {
                    result[0] = new Boolean( true );
                } else {
                    result[0] = new Boolean( false );
                }

                result[1] = new Integer( dummyTask.getSummary().length() );
                result[2] = new Integer( dummyTask.getNote().length() );

                toDoList.removeToDo( dummyTask.getToDo() );

                return result;
            } catch( PIMException ex ) {
                DM.error( ex, "TaskList: testFieldConstraints()" );
                return null;
            }
        }
    }
    
    private class PIMEventListener implements PIMChangeListener {
        public void itemsChanged( PIMChangeEvent pimce ) {
            new ProcessThread( pimce );
        }
        
        private class ProcessThread extends Thread {
            private PIMChangeEvent pimce;
            
            private ProcessThread( PIMChangeEvent pimce ) {
                this.pimce = pimce;
                start();
            }
            
            public void run() {
                synchronized( TASKS_SYNCHRONIZER ) {
                    int itemCount = pimce.countItems();
                    if( itemCount == 1 ) {
                        singleItem();
                    } else {
                        multipleItems();
                    }
                    lastChangedUID = null;
                }
            }

            private void multipleItems() {
                try {
                    refreshBaseTaskList();
                } catch( Exception e ) {
                    DM.error( e, "TaskList, PIMEventListener, ProcessThread: run(), multipleItems" );
                }
            }

            private void singleItem() {
                String UID = pimce.getUID( 0 );
                if( lastChangedUID != null && UID.equals( lastChangedUID ) ) {
                    return;
                }
                switch( pimce.getChangeType( 0 ) ) {
                    case PIMChangeEvent.ADDED:
                        addedTask( UID );
                        break;
                    case PIMChangeEvent.MODIFIED:
                        modifiedTask( UID );
                        break;
                    case PIMChangeEvent.REMOVED:
                        deletedTask( UID );
                        break;
                }
            }
        }
    }
}
