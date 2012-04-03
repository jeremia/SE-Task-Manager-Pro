/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jeremiaMorling.seTaskManagerPro.pim;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.utils.displayUtils.items.ChoiceItem;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.vector.IClonable;
import jeremiaMorling.utils.vector.ISerializable;

/**
 * 
 *
 * @author Jeremia Mörling
 */
public class Category extends ChoiceItem implements ISerializable  {
    private int id = -1;
    
    private static final Image CATEGORY_16;
    private static final Image CATEGORY_24;
    
    public static final int NAME_MAX_LENGTH = 100;
    
    static {
        CATEGORY_16 = RM.getImage( RM.CATEGORY_16 );
        CATEGORY_24 = RM.getImage( RM.CATEGORY_24 );
    }
    
    public Category() {
        setSmallIcon( CATEGORY_16 );
        setIcon( CATEGORY_24 );
    }

    Category( int id ) {
        this.id = id;
    }

    public Category( String name ) {
        this();
        setName( name );
    }

    public Category( int id, String name ) {
        this( name );
        this.id = id;
    }
    
    private void setName( String name ) {
        setText( name );
    }
    
    public String getName() {
        return getText();
    }

    public void setId( int id ) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public IClonable clone() {
        Category clone = new Category( getText() );
        clone.id = id;

        return clone;
    }

    public boolean equals( Object o ) {
        if( !(o instanceof Category) )
            return false;

        Category c = (Category)o;
        return id == c.id;
    }

    public void toSerialFormat( DataOutputStream dataOutputStream ) {
        try {
            dataOutputStream.writeUTF( getName() );
        } catch( Exception e ) {
            DM.error( e, "Category: toSerialFormat()" );
        }
    }

    public ISerializable fromSerialFormat( DataInputStream dataInputStream ) {
        try {
            setName( dataInputStream.readUTF() );
        } catch( Exception e ) {
            DM.error( e, "Category: fromSerialFormat()" );
        }
        return this;
    }
    
    public void changeName( String name ) {
        setName( name );
        try {
            RecordStore rs = RecordStore.openRecordStore( CategoryList.RS_CATEGORIES, false );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream( outputStream );
            toSerialFormat( dataOutputStream );
            dataOutputStream.flush();
            byte[] record = outputStream.toByteArray();
            rs.setRecord( id, record, 0, record.length );
            outputStream.close();
            dataOutputStream.close();
            rs.closeRecordStore();
        } catch( Exception e ) {
            DM.error( e, "Category: changeName()" );
        }
    }
}
