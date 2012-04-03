/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jeremiaMorling.seTaskManagerPro;

import jeremiaMorling.utils.managers.Loc;

/**
 * @author Jeremia
 */
public class SETMPLoc extends Loc {
    private static SETMPLoc instance;
    
    private static SETMPLoc getInstance() {
        if( instance == null )
            instance = new SETMPLoc();
        
        return instance;
    }
    
    private SETMPLoc() {
        super( "/jeremiaMorling/seTaskManagerPro/messages.properties" );
    }
    
    public static String getText( String key ) {
        return getInstance().internalGetText( key );
    }
}