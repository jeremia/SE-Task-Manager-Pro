/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.utils.displayUtils.displays.TextEditor;
import jeremiaMorling.utils.managers.DM;

/**
 *
 * @author Jeremia
 */
public class SearchTaskGUI extends TextEditor {
    public SearchTaskGUI() {
        super( SETMPLoc.getText( "categoryChooser.searchTask" ), 100 );
    }

    public void receiveString( String string ) {
        DM.pop();
        TaskListGUI.show( string );
    }
}
