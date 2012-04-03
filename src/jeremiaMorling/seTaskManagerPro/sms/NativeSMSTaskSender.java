/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.sms;

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
public class NativeSMSTaskSender extends NativeTaskSender {
    public NativeSMSTaskSender( Task task ) {
        super(
                task,
                SETMPLoc.getText( "sms.sendTaskFailed.title" ),
                SETMPLoc.getText( "sms.sendTaskFailed.text" ) );
    }

    protected void sendTask() throws Exception {
        Invocation sendAsMessage = new Invocation(
                Config.TODO_TO_SEND_PATH,
                Config.TODO_CONTENT_TYPE,
                Config.getMessageFileSenderId(),
                false,
                Config.ACTION_SEND
                );
        Registry registry = Registry.getRegistry( SETaskManagerPro.class.getName() );
        registry.invoke( sendAsMessage );
    }
}
