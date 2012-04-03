/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.pim;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import jeremiaMorling.utils.pim.PIMWrapper;
import java.io.IOException;
import java.util.Date;
import javax.microedition.lcdui.Image;
import javax.microedition.pim.PIMException;

import javax.microedition.pim.ToDo;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import jeremiaMorling.seTaskManagerPro.RM;

import jeremiaMorling.utils.vector.ComparableString;
import jeremiaMorling.utils.vector.IComparable;
import jeremiaMorling.utils.vector.IntVector;

/**
 * 
 *
 * @author Jeremia M�rling
 */
public class Task extends PIMWrapper {

    private IntVector assignedCategories;
    private int recordId = -1;
    
    private String uid;
    private boolean completed;
    private int priority;
    private Date dueDate;

    public static final int PRIO_HIGH = 1;
    public static final int PRIO_NORMAL = 2;
    public static final int PRIO_LOW = 3;
    
    public static final int REMINDER = 109;
    
    private static final Image PRIO_HIGH_24;
    private static final Image PRIO_NORMAL_24;
    private static final Image PRIO_LOW_24;
    private static final Image TASK_COMPLETED_24;
    
    public static final String ASSIGNED_CATEGORIES_RS = "assignedCategories";
    
    //public static final int SUMMARY_MAX_LENGTH = 200;
    //public static final int NOTE_MAX_LENGTH = 512;
    
    static {
        PRIO_HIGH_24 = RM.getImage( RM.PRIO_HIGH_24, true );
        PRIO_NORMAL_24 = RM.getImage( RM.PRIO_NORMAL_24, true );
        PRIO_LOW_24 = RM.getImage( RM.PRIO_LOW_24, true );
        TASK_COMPLETED_24 = RM.getImage( RM.TASK_COMPLETED_24, true );
    }
    
    protected Task() {}

    public Task( ToDo toDo ) {
        setToDo( toDo );
    }
    
    public void setToDo( ToDo toDo ) {
        pimItem = toDo;
        setText( getString( ToDo.SUMMARY ) );
        uid = getString( ToDo.UID );
        completed = getBoolean( ToDo.COMPLETED );
        priority = getInt( ToDo.PRIORITY );
        dueDate = getDate( ToDo.DUE );
        calculateIcon();
    }

    public ToDo getToDo() {
        return (ToDo)getPIMItem();
    }

    public String getUID() {
        return uid;
    }

    public String getSummary() {
        //return getString( ToDo.SUMMARY );
        return getText();
    }

    public void setSummary( String summary ) {
        setString( ToDo.SUMMARY, summary );
        setText( summary );
    }

    public String getNote() {
        return getString( ToDo.NOTE );
    }

    public void setNote( String note ) {
        setString( ToDo.NOTE, note );
    }

    public int getPriority() {
        //return getInt( ToDo.PRIORITY );
        return priority;
    }

    public void setPriority( int priority ) {
        setInt( ToDo.PRIORITY, priority );
        this.priority = priority;
        calculateIcon();
    }

    public Date getDueDate() {
        //return getDate( ToDo.DUE );
        return dueDate;
    }

    public void setDueDate( Date dueDate ) {
        setDate( ToDo.DUE, dueDate );
        this.dueDate = dueDate;
    }

    public Date getReminder() {
        return getDate( REMINDER );
    }

    public void setReminder( Date reminder ) {
        setDate( REMINDER, reminder );
    }

    public void setAssignedCategories( IntVector assignedCategories ) throws RecordStoreException, IOException {
        this.assignedCategories = assignedCategories;
        persistAssignedCategories();
    }

    private void persistAssignedCategories() throws IOException, RecordStoreException {
        if( assignedCategories == null || assignedCategories.size() == 0 ) {
            if( recordId != -1 )
                deleteAssignedCategories();
        }
        else
            saveAssignedCategories();
    }

    private void deleteAssignedCategories() throws RecordStoreException {
        RecordStore rs = RecordStore.openRecordStore( ASSIGNED_CATEGORIES_RS, true );
        rs.deleteRecord( recordId );
        rs.closeRecordStore();
        recordId = -1;
    }

    private void saveAssignedCategories() throws IOException, RecordStoreException {
        RecordStore rs = RecordStore.openRecordStore( ASSIGNED_CATEGORIES_RS, true );
        byte[] record;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream( outputStream );

        dataOutputStream.writeUTF( getUID() );
        for ( int i = 0; i < assignedCategories.size(); i++ )
            dataOutputStream.writeInt( assignedCategories.getInt( i ) );

        dataOutputStream.flush();
        record = outputStream.toByteArray();
        if ( recordId == -1 ) {
            recordId = rs.addRecord( record, 0, record.length );
        } else
            rs.setRecord( recordId, record, 0, record.length );
        
        outputStream.close();
        dataOutputStream.close();
        rs.closeRecordStore();
    }
    
    public void removeCategoryAssignment( Category category ) throws RecordStoreException, IOException {
        if( assignedCategories == null || category == null )
            return;

        int categoryId = category.getId();
        for( int i=0; i<assignedCategories.size(); i++ ) {
            if( assignedCategories.getInt( i ) == categoryId ) {
                assignedCategories.removeElementAt( i );
                persistAssignedCategories();
                break;
            }
        }
    }
    
    public void addCategoryAssignment( Category category ) throws IOException, RecordStoreException {
        if( assignedCategories == null )
            assignedCategories = new IntVector( 1 );
        
        int categoryId = category.getId();
        if( !assignedCategories.contains( categoryId ) ) {
            assignedCategories.addElement( categoryId );
            persistAssignedCategories();
        }
    }

    public IntVector getAssignedCategories() {
        return assignedCategories;
    }
    
    public String getAssignedCategoriesAsString() {
        if( assignedCategories == null || assignedCategories.size() == 0 )
            return "";

        CategoryList categories = CategoryList.getCategories();
        StringBuffer text = new StringBuffer( categories.getCategoryById( assignedCategories.getInt( 0 ) ).getText() );
        for( int i=1; i<assignedCategories.size(); i++ ) {
            text.append( ", " );
            text.append( categories.getCategoryById( assignedCategories.getInt( i ) ).getText() );
        }

        return text.toString();
    }
    
    public boolean hasCategoryAssigned( Category category ) {
        if( !(hasAssignedCategories()) )
            return false;
        
        int categoryId = category.getId();
        for( int i=0; i<assignedCategories.size(); i++ ) {
            if( assignedCategories.getInt( i ) == categoryId )
                return true;
        }
        
        return false;
    }
    
    public boolean hasAssignedCategories() {
        if( assignedCategories == null || assignedCategories.size() == 0 )
            return false;
        else
            return true;
    }

    public boolean isCompleted() {
        //return getBoolean( ToDo.COMPLETED );
        return completed;
    }

    public void setCompleted( boolean completed ) {
        setBoolean( ToDo.COMPLETED, completed );
        this.completed = completed;
        calculateIcon();
    }

    public Date getCompletionDate() {
        return getDate( ToDo.COMPLETION_DATE );
    }

    public void setCompletionDate( Date completionDate ) {
        setDate( ToDo.COMPLETION_DATE, completionDate );
    }

    public Date getLastEditDate() {
        return getDate( ToDo.REVISION );
    }

    private void setRecordId( int recordId ) {
        this.recordId = recordId;
    }

    public int getRecordId() {
        return recordId;
    }
    
    public boolean equals( Object o ) {
        if( o == null ) {
            return false;
        } else if( o instanceof String ) {
            return getUID().equals( o );
        } else if( o instanceof Task ) {
            Task taskToCompare = (Task) o;
            return getUID().equals( taskToCompare.getUID() );
        } else if( o instanceof ToDo ) {
            ToDo toDoToCompare = (ToDo) o;
            return getUID().equals( toDoToCompare.getString( ToDo.UID, 0 ) );
        } else {
            return false;
        }
    }

    public int compareTo( Object itemToCompare ) throws IllegalArgumentException {
        if( !(itemToCompare instanceof Task) ) {
            throw new IllegalArgumentException( "comparable is not of type PhoneToDo, but of type " + itemToCompare.getClass().getName() );
        }

        Task taskToCompare = (Task) itemToCompare;

        // Compare completion
        boolean thisCompleted = isCompleted();
        boolean compareCompleted = taskToCompare.isCompleted();
        if( thisCompleted && !compareCompleted ) {
            return 1;
        } else if( !thisCompleted && compareCompleted ) {
            return -1;
        }

        // Compare prio
        int thisPrio = getPriority();
        int comparePrio = taskToCompare.getPriority();
        if( thisPrio < comparePrio ) {
            return -1;
        } else if( thisPrio > comparePrio ) {
            return 1;
        }

        // Compare due date
        Date thisDueDate = getDueDate();
        Date compareDueDate = taskToCompare.getDueDate();
        if( thisDueDate != null || compareDueDate != null ) {
            if( thisDueDate == null && compareDueDate != null ) {
                return 1;
            } else if( thisDueDate != null && compareDueDate == null ) {
                return -1;
            } else if( thisDueDate.getTime() < compareDueDate.getTime() ) {
                return -1;
            } else if( thisDueDate.getTime() > compareDueDate.getTime() ) {
                return 1;
            }
        }

        // Compare summary
        int comparison = ComparableString.lowerCaseCompareToSE( getLowerCaseText(), taskToCompare.getLowerCaseText() );
        if( comparison != 0 ) {
            return comparison;
        }

        // Compare note
        comparison = ComparableString.compareToSEIgnoreCase( getNote(), taskToCompare.getNote() );
        return comparison;
    }

    private void calculateIcon() {
        int prio = getPriority();
        if( isCompleted() ) {
            setIcon( TASK_COMPLETED_24 );
        } else if( prio == Task.PRIO_HIGH ) {
            setIcon( PRIO_HIGH_24 );
        } else if( prio == Task.PRIO_LOW ) {
            setIcon( PRIO_LOW_24 );
        } else {
            setIcon( PRIO_NORMAL_24 );
        }
    }
    
    public void readAssignedCategories( DataInputStream dataInputStream, int recordId ) throws IOException {
        setRecordId( recordId );
        assignedCategories = new IntVector( 1 );

        while( dataInputStream.available() > 0 )
            assignedCategories.addElement( dataInputStream.readInt() );
    }
    
    public void save() throws PIMException {
        //TaskList.setLastChangedUID( uid );
        pimItem.commit();
        uid = getString( ToDo.UID );
        TaskList.setLastChangedUID( uid );
    }
}
