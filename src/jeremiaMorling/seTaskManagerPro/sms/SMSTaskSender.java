/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.pim.PIM;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.common.IProcessInfoReceiver;
import jeremiaMorling.seTaskManagerPro.pim.Task;

/**
 *
 * @author Jeremia
 */
public class SMSTaskSender implements Runnable {
    private Thread processThread;
    private String phoneNumber;
    private Task task;
    private IProcessInfoReceiver processInfoReceiver;

    public SMSTaskSender( IProcessInfoReceiver processInfoReceiver, String phoneNumber, Task task ) throws IOException {
        this.processInfoReceiver = processInfoReceiver;
        this.phoneNumber = phoneNumber;
        this.task = task;
        
        processThread = new Thread( this );
        processThread.start();
    }

    public void run() {
        try {
            MessageConnection con = (MessageConnection)Connector.open( "sms://" + phoneNumber );
            BinaryMessage msg = (BinaryMessage)con.newMessage( MessageConnection.BINARY_MESSAGE );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PIM.getInstance().toSerialFormat( task.getToDo(), outputStream, null, Config.FORMAT_VCALENDAR_1_0 );
            msg.setPayloadData( outputStream.toByteArray() );
            con.send( msg );
            outputStream.close();
            con.close();
            processInfoReceiver.completed( true );
        } catch( Exception e ) {
            processInfoReceiver.completed( false );
        }
    }
}
