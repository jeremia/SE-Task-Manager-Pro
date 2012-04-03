/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import java.io.IOException;
import javax.microedition.rms.RecordStoreException;
import jeremiaMorling.seTaskManagerPro.common.IProcessInfoReceiver;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.seTaskManagerPro.sms.SMSTaskSender;
import jeremiaMorling.utils.displayUtils.displays.AlertDisplay;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.sms.gui.SMSReceiverGUI;

/**
 *
 * @author Jeremia
 */
public class SMSTaskSenderGUI extends SMSReceiverGUI implements IProcessInfoReceiver {
    private Task task;
    
    public SMSTaskSenderGUI( Task task ) throws RecordStoreException, IOException {
        super();

        this.task = task;
    }
    
    protected void sendSMS( String phoneNumber ) {
        try {
            DM.add( new AlertDisplay(
                        SETMPLoc.getText( "sendTask.sendingTaskTitle" ),
                        SETMPLoc.getText( "sendTask.sendingTaskText" ),
                        RM.getImage( RM.WAIT_64 ) ) );
                new SMSTaskSender( this, phoneNumber, task );
        } catch( Exception e ) {
            DM.error( e, "SMSTaskSenderGUI: sendSMS()" );
        }
    }

    public void completed( boolean success ) {
        synchronized(this) {
            DM.back( 1 );
            if( success )
                DM.newAlert(
                        SETMPLoc.getText( "sendTask.sendTaskSuccessful.title" ),
                        SETMPLoc.getText( "sendTask.sendTaskSuccessful.text" ),
                        RM.getImage( RM.SUCCESS_64 ),
                        MESSAGE_TIMEOUT );
            if ( !success )
                DM.newAlert(
                        SETMPLoc.getText( "sms.sendTaskFailed.title" ),
                        SETMPLoc.getText( "sms.sendTaskFailed.text" ),
                        RM.getImage( RM.ERROR_64 ) );
        }
    }
}
