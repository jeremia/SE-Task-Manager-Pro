/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.bluetooth;

import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.common.NativeTaskSender;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.SETaskManagerPro;
import jeremiaMorling.seTaskManagerPro.pim.Task;

/**
 *
 * @author Jeremia
 */
public class NativeBTTaskPusher extends NativeTaskSender {
    
    
    public NativeBTTaskPusher( Task task ) {
        super(
                task,
                SETMPLoc.getText( "bluetooth.sendTaskFailed.title" ),
                SETMPLoc.getText( "bluetooth.sendTaskFailed.text" ) );
    }

    protected void sendTask() throws Exception {
        Invocation sendViaBluetooth = new Invocation(
                Config.TODO_TO_SEND_PATH,
                Config.TODO_CONTENT_TYPE,
                Config.SEND_VIA_BLUETOOTH_ID,
                false,
                Config.ACTION_SEND
                );
        Registry registry = Registry.getRegistry( SETaskManagerPro.class.getName() );
        registry.invoke( sendViaBluetooth );
    }
}
