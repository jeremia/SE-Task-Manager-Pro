/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jeremiaMorling.seTaskManagerPro.common;

/**
 *
 * @author Jeremia
 */
public interface IProcessInfoReceiver {
    public static final int MESSAGE_TIMEOUT = 2000;
    
    public void completed( boolean success );
}
