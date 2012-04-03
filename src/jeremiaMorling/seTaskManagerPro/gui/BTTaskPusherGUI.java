/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jeremiaMorling.seTaskManagerPro.gui;

import jeremiaMorling.utils.bluetooth.BTDeviceFinderGUI;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.utils.bluetooth.BTDevice;
import jeremiaMorling.utils.bluetooth.BTDeviceFinder;
import jeremiaMorling.seTaskManagerPro.bluetooth.BTTaskPusher;
import jeremiaMorling.seTaskManagerPro.common.IProcessInfoReceiver;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.utils.managers.DM;

/**
 * 
 *
 * @author Jeremia Mörling
 */
public class BTTaskPusherGUI extends BTDeviceFinderGUI implements IProcessInfoReceiver {
    private Task task;

    public BTTaskPusherGUI( Task task ) {
        super( BTDeviceFinder.OPP_FORMAT_V_CALENDAR_1_0 );
        this.task = task;
    }

    protected void deviceSelected( BTDevice btDevice ) {
        synchronized( this ) {
            DM.newAlert(
                    SETMPLoc.getText( "sendTask.sendingTask.title" ),
                    SETMPLoc.getText( "sendTask.sendingTask.text" ),
                    RM.getImage( RM.WAIT_64 ),
                    MESSAGE_TIMEOUT );
            new BTTaskPusher( this, btDevice, task );
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
                        SETMPLoc.getText( "bluetooth.sendTaskFailed.title" ),
                        SETMPLoc.getText( "bluetooth.sendTaskFailed.text" ),
                        RM.getImage( RM.ERROR_64 ) );
        }
    }
}
