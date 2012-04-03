/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.bluetooth.NativeBTTaskPusher;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.seTaskManagerPro.sms.NativeSMSTaskSender;
import jeremiaMorling.utils.managers.DM;

/**
 *
 * @author Jeremia
 */
public class SendTaskGUI extends List implements CommandListener {
    private Task task;
    
    private static final int INDEX_VIA_BLUETOOTH = 0;
    private static final int INDEX_AS_SMS = 1;
    
    public SendTaskGUI( Task task ) {
        super( SETMPLoc.getText( "sendTask" ), IMPLICIT );
        this.task = task;
        
        append( SETMPLoc.getText( "sendTask.viaBluetooth" ), RM.getImage( RM.BLUETOOTH_24 ) );
        append( SETMPLoc.getText( "sendTask.asSMS" ), RM.getImage( RM.SMS_24 ) );
        
        setSelectCommand( new Command( SETMPLoc.getText( "common.select" ), Command.OK, 0 ) );
        addCommand( new Command( SETMPLoc.getText( "common.cancel" ), Command.CANCEL, 1 ) );
        setCommandListener( this );
    }

    public void commandAction( Command c, Displayable d ) {
        switch( c.getCommandType() ) {
            case Command.OK:
                if( getSelectedIndex() == INDEX_VIA_BLUETOOTH ) {
                    if( Config.isBTPusherSupported() )
                        new NativeBTTaskPusher( task );
                    else
                        DM.add( new BTTaskPusherGUI( task ), true );
                }
                else {
                    if( Config.isSMSSenderSupported() )
                        new NativeSMSTaskSender( task );
                    else {
                        try {
                            DM.add( new SMSTaskSenderGUI( task ), true );
                        } catch( Exception e ) {
                            DM.error( e, "SendTaskGUI: commandAction(), DM.add( new SMSTaskSenderGUI( task ), true )" );
                        }
                    }
                }
                break;
            case Command.CANCEL:
                DM.back();
                break;
        }
    }
}
