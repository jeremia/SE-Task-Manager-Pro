/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.SETaskManagerPro;
import jeremiaMorling.utils.fileConnection.FileConnectionUtil;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.displayUtils.displays.TextForm;

/**
 *
 * @author Jeremia
 */
public class AboutGUI extends TextForm {
    private static final String PROP_APP_NAME = "MIDlet-Name";
    private static final String PROP_APP_VERSION = "MIDlet-Version";
    private static final String PROP_APP_VENDOR = "MIDlet-Vendor";
    
    private static final Font HEADER_FONT = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL );
    
    public AboutGUI() {
        super( SETMPLoc.getText( "about" ) );
        try {
            SETaskManagerPro app = SETaskManagerPro.getInstance();
            appendString( app.getAppProperty( PROP_APP_NAME ) + " v" + app.getAppProperty( PROP_APP_VERSION ) );
            appendString( app.getAppProperty( PROP_APP_VENDOR ) + " (c) 2011." );
            newLine();
            appendString( SETMPLoc.getText( "about.attributions" ), HEADER_FONT );
            appendString( FileConnectionUtil.readTextFile( "/texts/Attributions.txt" ) );

            addCommand( new Command( SETMPLoc.getText( "common.back" ), Command.BACK, 1 ) );
        } catch( Exception e ) {
            DM.error( e, "AboutGUI: AboutGUI()" );
        }
    }

    public void commandAction( Command c, Displayable d ) {
        if( c.getCommandType() == Command.BACK )
            DM.back();
        else
            super.commandAction( c, d );
    }
}
