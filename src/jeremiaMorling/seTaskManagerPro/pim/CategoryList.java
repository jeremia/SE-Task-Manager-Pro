/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jeremiaMorling.seTaskManagerPro.pim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import jeremiaMorling.utils.displayUtils.items.ChoiceVector;
import jeremiaMorling.utils.managers.DM;

/**
 * 
 *
 * @author Jeremia Mörling
 */
public class CategoryList extends ChoiceVector {
    private static CategoryList categories;
    private static boolean refreshNeeded = false;
    
    public static final String RS_CATEGORIES = "categories";
    
    public void addElement( Object element ) {
        if( !(element instanceof Category) )
            throw new IllegalArgumentException( "element has to be of type Category, but is of type " + element.getClass().getName() );

        super.addElement( element );
    }

    public Category getCategory( int index ) {
        return (Category)elementAt( index );
    }

    public Category getCategoryById( int id ) {
        for( int i=0; i<size(); i++ ) {
            Category c = getCategory( i );
            if( c.getId() == id )
                return c;
        }
        
        return null;
    }
    
    public static Category addCategory( Category category ) throws RecordStoreException, IOException {
        categories.addElement( category );
        categories.sort();
        refreshNeeded = true;

        RecordStore rs = RecordStore.openRecordStore( RS_CATEGORIES, true );

        byte[] nameAsBytes;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream( outputStream );
        category.toSerialFormat( dataOutputStream );
        dataOutputStream.flush();
        nameAsBytes = outputStream.toByteArray();
        int id = rs.addRecord( nameAsBytes, 0, nameAsBytes.length );
        category.setId( id );
        
        outputStream.close();
        dataOutputStream.close();
        rs.closeRecordStore();
        
        return category;
    }
    
    public static void deleteCategory( Category category ) {
        try {
            categories.removeElement( category );
            RecordStore rs = RecordStore.openRecordStore( RS_CATEGORIES, true );
            rs.deleteRecord( category.getId() );
            rs.closeRecordStore();
            
            TaskList.deletingCategory( category );
        } catch ( Exception e ) {}
    }
    
    public static boolean refreshNeeded() {
        boolean result = refreshNeeded;
        refreshNeeded = false;
        return result;
    }
    
    public static CategoryList getCategories() {
        if( categories == null )
            readCategories();
        
        return categories;
    }
    
    private static CategoryList readCategories() {
        try {
            categories = new CategoryList();

            RecordStore rs = RecordStore.openRecordStore( RS_CATEGORIES, true );
            RecordEnumeration categoryRecords = rs.enumerateRecords( null, null, false );

            int id;
            String name;
            ByteArrayInputStream inputStream;
            DataInputStream dataInputStream;
            while( categoryRecords.hasNextElement() ) {
                id = categoryRecords.nextRecordId();
                byte[] nameAsBytes = rs.getRecord( id );
                inputStream = new ByteArrayInputStream( nameAsBytes );
                dataInputStream = new DataInputStream( inputStream );
                name = dataInputStream.readUTF();
                categories.addElement( new Category( id, name ) );
                inputStream.reset();
                inputStream.close();
                dataInputStream.close();
            }

            rs.closeRecordStore();
            categories.sort();
            
            return categories;
        } catch( Exception e ) {
            DM.error( e, "CategoryManager: readCategories()" );
            return null;
        }
    }
}
