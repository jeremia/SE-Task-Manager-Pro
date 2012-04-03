/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jeremiaMorling.seTaskManagerPro.bluetooth;

import jeremiaMorling.seTaskManagerPro.common.IProcessInfoReceiver;
import jeremiaMorling.utils.bluetooth.BTDevice;
import java.io.OutputStream;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.pim.PIM;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.pim.Task;

/**
 * 
 *
 * @author Jeremia Mörling
 */
public class BTTaskPusher implements Runnable {
    private Thread processThread;
    private BTDevice btDevice;
    private Task task;
    private IProcessInfoReceiver processInfoReceiver;

    // this is the connection object to be used for
    //  bluetooth i/o
    Connection connection = null;

    public BTTaskPusher( IProcessInfoReceiver processInfoReceiver, BTDevice btDevice, Task task ) {
        this.processInfoReceiver = processInfoReceiver;
        this.btDevice = btDevice;
        this.task = task;
        processThread = new Thread( this );
        processThread.start();
    }

    public void run() {
        try{
            connection = Connector.open( btDevice.getConnectionURL() );
            // connection obtained

            // now, let's create a session and a headerset objects
            ClientSession cs = (ClientSession)connection;
            HeaderSet hs = cs.createHeaderSet();

            // now let's send the connect header
            cs.connect( hs );
            hs.setHeader( HeaderSet.NAME, task.getSummary() + Config.TODO_FILE_EXTENSION );
            hs.setHeader( HeaderSet.TYPE, Config.TODO_CONTENT_TYPE );

            Operation putOperation = cs.put( hs );

            OutputStream outputStream = putOperation.openOutputStream();
            PIM.getInstance().toSerialFormat( task.getToDo(), outputStream, null, Config.FORMAT_VCALENDAR_1_0 );
            // task push complete

            outputStream.close();
            putOperation.close();

            cs.disconnect(null);

            connection.close();
            processInfoReceiver.completed( true );
        } catch (Exception e){
            processInfoReceiver.completed( false );
        }
    }
}
