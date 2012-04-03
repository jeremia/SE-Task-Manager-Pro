/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.common;

import jeremiaMorling.seTaskManagerPro.Config;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.PIM;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.pim.Task;
import jeremiaMorling.utils.managers.DM;

/**
 *
 * @author Jeremia
 */
public abstract class NativeTaskSender {
    protected NativeTaskSender( Task task, String errorTitle, String errorText ){
        try {
            saveTaskToFile( task );
            sendTask();
            DM.back();
        } catch( Exception e ) {
            DM.back();
            DM.newAlert(
                    errorTitle,
                    errorText,
                    RM.getImage( RM.ERROR_64 ) );
        }
    }
    
    private void saveTaskToFile( Task task ) throws Exception {
        DataOutputStream os = null;
        FileConnection fconn = null;
        FileConnection tempFolder = null;
        Exception ocurredError = null;
        try {
            tempFolder = (FileConnection) Connector.open( Config.TEMP_PATH, Connector.READ_WRITE );
            if( !tempFolder.exists() )
                tempFolder.mkdir();
            tempFolder.close();
            
            fconn = (FileConnection) Connector.open( Config.TODO_TO_SEND_PATH, Connector.READ_WRITE );
            if( fconn.exists() )
                fconn.delete();
            fconn.create();
            os = fconn.openDataOutputStream();
            
            PIM.getInstance().toSerialFormat( task.getToDo(), os, null, Config.FORMAT_VCALENDAR_1_0 );
        } catch( Exception e ) {
            ocurredError = e;
        } finally {
            try {
                if( null != os ) {
                    os.close();
                }
                if( null != fconn ) {
                    fconn.close();
                }
            } catch( IOException e ) {
            } finally {
                if( ocurredError != null )
                    throw ocurredError;
            }
        }
    }
    
    protected abstract void sendTask() throws Exception;
}
