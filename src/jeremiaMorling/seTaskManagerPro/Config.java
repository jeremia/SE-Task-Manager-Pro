/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.content.Registry;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import jeremiaMorling.seTaskManagerPro.pim.TaskList;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.managers.ExceptionManager;
import jeremiaMorling.utils.persistence.PersistenceUtils;

/**
 *
 * @author Jeremia
 */
public class Config {
    private static boolean isBluetoothSupported;
    private static boolean isReminderSupported;
    private static boolean isDueDateTimeSupported;
    private static int summaryMaxLength = Integer.MIN_VALUE;
    private static int noteMaxLength = Integer.MIN_VALUE;
    private static boolean isPIMChangeListenerSupported;
    private static boolean isBTPusherSupported;
    private static boolean isSMSSenderSupported;
    private static boolean isUnifiedSenderSupported;
    private static boolean isSonyEricssonNonSmartphone;
    
    private static final String RS_PHONE_CONSTRAINTS = "phoneConstraints";
    
    private static final String SEJP_PIM_CHANGE_LISTENER = "JP-8.3";
    private static final String SEJP_BT_PUSHER = "JP-8.5.0";
    private static final String SEJP_SMS_SENDER = "JP-8.5.0";
    private static final String BRAND_SONY_ERICSSON = "SonyEricsson";
    
    public static final String TODO_FILE_EXTENSION = ".vcs";
    public static final String TODO_CONTENT_TYPE = "text/plain";
    public static final String TEMP_PATH = "file:///C:/Temp/";
    public static final String TODO_TO_SEND_PATH = TEMP_PATH + "toDoToSend" + TODO_FILE_EXTENSION;
    
    private static final String UNIFIED_FILE_SENDER_ID = "com.sonyericsson.messaging.unified.editor.filesender";
    private static final String SMS_FILE_SENDER_ID = "com.sonyericsson.messaging.sms.editor.filesender";
    public static final String SEND_VIA_BLUETOOTH_ID = "com.sonyericsson.bluetooth.send";
    
    public static final String ACTION_SEND = "send";
    
    public static final String FORMAT_VCALENDAR_1_0 = "VCALENDAR/1.0";
    
    private static final Object CONFIG_SYNCHRONIZER = new Object();
    
    private Config() {}
    
    public static boolean isBluetoothSupported() {
        return isBluetoothSupported;
    }
    
    public static boolean isReminderSupported() {
        return isReminderSupported;
    }
    
    public static boolean isDueDateTimeSupported() {
        return isDueDateTimeSupported;
    }
    
    public static int getSummaryMaxLength() {
        return summaryMaxLength;
    }
    
    public static int getNoteMaxLength() {
        return noteMaxLength;
    }
    
    public static boolean isPIMChangeListenerSupported() {
        return isPIMChangeListenerSupported;
    }
    
    public static boolean isBTPusherSupported() {
        return isBTPusherSupported;
    }
    
    public static boolean isSMSSenderSupported() {
        return isSMSSenderSupported;
    }
    
    public static String getMessageFileSenderId() {
        if( !isSMSSenderSupported )
            return null;
        else if( isUnifiedSenderSupported )
            return UNIFIED_FILE_SENDER_ID;
        else
            return SMS_FILE_SENDER_ID;
    }
    
    public static boolean isSonyEricssonNonSmartPhone() {
        return isSonyEricssonNonSmartphone;
    }
    
    public static void initPhoneConstraints() {
        if( !readPhoneConstraints() ) {
            calculatePhoneConstraints();
            savePhoneConstraints();
        }
    }
    
    private static void calculatePhoneConstraints() {
        isPIMChangeListenerSupported = isCurrentSEJPGreaterOrEqual( SEJP_PIM_CHANGE_LISTENER );
        isBTPusherSupported = isCurrentSEJPGreaterOrEqual( SEJP_BT_PUSHER );
        isSMSSenderSupported = isCurrentSEJPGreaterOrEqual( SEJP_SMS_SENDER );
        if( isSMSSenderSupported )
            isUnifiedSenderSupported = checkUnifiedSender();
        isSonyEricssonNonSmartphone = isSonyEricsson() && !isSmartphone();
        isBluetoothSupported = doesClassExist( "javax.bluetooth.DiscoveryAgent" );
        isReminderSupported = TaskList.isReminderSupported();
        Object[] taskFieldConstraints = TaskList.testFieldConstraints();
        isDueDateTimeSupported = ((Boolean)taskFieldConstraints[0]).booleanValue();
        summaryMaxLength = ((Integer)taskFieldConstraints[1]).intValue();
        noteMaxLength = ((Integer)taskFieldConstraints[2]).intValue();
    }
    
    private static boolean isCurrentSEJPGreaterOrEqual( String minSEJP ) {
        String currentSEJP = System.getProperty("com.sonyericsson.java.platform");
        if( currentSEJP == null )
            return false;
        else
            return currentSEJP.compareTo( minSEJP ) >= 0;
    }
    
    private static boolean checkUnifiedSender() {
        Registry registry = Registry.getRegistry( SETaskManagerPro.class.getName() );
        return (registry.forID( UNIFIED_FILE_SENDER_ID, true ) != null);
    }

    private static boolean doesClassExist( String className ) {
        try {
            Class.forName( className );
            return true;
        } catch( ClassNotFoundException e ) {
            return false;
        }
    }
    
    private static void savePhoneConstraints() {
        try {
            RecordStore rs = RecordStore.openRecordStore( RS_PHONE_CONSTRAINTS, true );
            PersistenceUtils.deleteAllRecords( rs );
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( 1 + 1 + 2*4 );
            DataOutputStream dataOutputStream = new DataOutputStream( outputStream );
            
            dataOutputStream.writeBoolean( isPIMChangeListenerSupported );
            dataOutputStream.writeBoolean( isBTPusherSupported );
            dataOutputStream.writeBoolean( isSMSSenderSupported );
            if( isSMSSenderSupported )
                dataOutputStream.writeBoolean( isUnifiedSenderSupported );
            dataOutputStream.writeBoolean( isSonyEricssonNonSmartphone );
            dataOutputStream.writeBoolean( isBluetoothSupported );
            dataOutputStream.writeBoolean( isReminderSupported );
            dataOutputStream.writeBoolean( isDueDateTimeSupported );
            dataOutputStream.writeInt( summaryMaxLength );
            dataOutputStream.writeInt( noteMaxLength );
            
            dataOutputStream.flush();
            byte[] record = outputStream.toByteArray();
            rs.addRecord( record, 0, record.length );
            
            outputStream.close();
            dataOutputStream.close();
            rs.closeRecordStore();
        } catch( Exception e ) {
            DM.error( e, "Config: savePhoneConstraints()" );
        }
    }
    
    private static boolean readPhoneConstraints() {
        if( !PersistenceUtils.doesRecordStoreExist( RS_PHONE_CONSTRAINTS ) )
            return false;

        try {
            RecordStore rs = RecordStore.openRecordStore( RS_PHONE_CONSTRAINTS, false );
            RecordEnumeration re = rs.enumerateRecords( null, null, false );
            byte[] record = re.nextRecord();
            ByteArrayInputStream inputStream = new ByteArrayInputStream( record );
            DataInputStream dataInputStream = new DataInputStream( inputStream );
            
            isPIMChangeListenerSupported = dataInputStream.readBoolean();
            isBTPusherSupported = dataInputStream.readBoolean();
            isSMSSenderSupported = dataInputStream.readBoolean();
            if( isSMSSenderSupported )
                isUnifiedSenderSupported = dataInputStream.readBoolean();
            isSonyEricssonNonSmartphone = dataInputStream.readBoolean();
            isBluetoothSupported = dataInputStream.readBoolean();
            isReminderSupported = dataInputStream.readBoolean();
            isDueDateTimeSupported = dataInputStream.readBoolean();
            summaryMaxLength = dataInputStream.readInt();
            noteMaxLength = dataInputStream.readInt();
            
            inputStream.close();
            dataInputStream.close();
            re.destroy();
            rs.closeRecordStore();
            return true;
        } catch( Exception e ) {
            DM.error( e, "Config: readPhoneConstraints()" );
            return false;
        }
    }
    
    private static boolean isSonyEricsson() {
        String platform = System.getProperty( "microedition.platform" );
        if( platform == null )
            return false;
        else
            return platform.startsWith( BRAND_SONY_ERICSSON );
    }
    
    private static boolean isSmartphone() {
        return new Config().new InteractionModesReader().isSmartphone();
    }
    
    public static boolean isAttributeSet( int attributes, int attribute ) {
        return ((attributes & attribute) == attribute);
    }
    
    public static String toString( boolean value ) {
        if( value )
            return "True/Yes";
        else
            return "False/No";
    }
    
    private class InteractionModesReader extends CustomItem {
        private InteractionModesReader() {
            super( null );
        }
        
        private boolean isSmartphone() {
            return isAttributeSet( getInteractionModes(), POINTER_PRESS );
        }
        
        protected int getMinContentWidth() { return 0; }
        protected int getMinContentHeight() { return 0; }
        protected int getPrefContentWidth( int height ) { return 0; }
        protected int getPrefContentHeight( int width ) { return 0; }
        protected void paint( Graphics g, int w, int h ) {}
    }
}
