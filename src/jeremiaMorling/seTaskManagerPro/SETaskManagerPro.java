package jeremiaMorling.seTaskManagerPro;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.pim.PIMException;
import jeremiaMorling.seTaskManagerPro.gui.CategoryChooserGUI;
import jeremiaMorling.seTaskManagerPro.pim.TaskList;
import jeremiaMorling.utils.displayUtils.displays.SortableList;
import jeremiaMorling.utils.displayUtils.items.CustomDateSettingViewer;
import jeremiaMorling.utils.displayUtils.items.NativeDateSettingViewer;
import jeremiaMorling.utils.managers.DM;

public final class SETaskManagerPro extends MIDlet {
    private static SETaskManagerPro instance;

    public SETaskManagerPro() {
        try {
            instance = this;
            DM.init( this );
        } catch ( Exception e ) {
            DM.error( e, "SETaskManagerPro: SETaskManagerPro()" );
        }
    }
    
    public static SETaskManagerPro getInstance() {
        return instance;
    }

    protected void destroyApp( boolean unconditional ) throws MIDletStateChangeException {
        try {
            TaskList.close();
            notifyDestroyed();
        } catch ( PIMException e ) {
            DM.error( e, "SETaskManagerPro: destroyApp()" );
        }
    }

    public void exit() {
        try {
            destroyApp( true );
        } catch ( Exception e ) {
            DM.error( e, "SETaskManagerPro: exit()" );
        }
    }

    protected void pauseApp() {
        try {
            RM.emptyCache();
            //taskListGUI.closeTaskList();
            //taskListGUI = null;
            DM.minimize();
            notifyPaused();
        } catch ( Exception e ) {
            DM.error( e, "SETaskManagerPro: pauseApp()" );
        }
    }

    public void minimize() {
        pauseApp();
    }

    protected void startApp() throws MIDletStateChangeException {
        try {
            init();
            DM.add( new CategoryChooserGUI( this ) );
        } catch ( Exception e ) {
            DM.error( e, "SETaskManagerPro: startApp()" );
        }
    }

    private void init() throws PIMException {
        TaskList.createToDoList();
        Config.initPhoneConstraints();
        TaskList.loadTasks();
        setConfigForSonyEricssonNonSmartphone();
    }

    private void setConfigForSonyEricssonNonSmartphone() {
        boolean isSonyEricssonNonSmartphone = Config.isSonyEricssonNonSmartPhone();
        SortableList.setSonyEricssonNonSmartphone( isSonyEricssonNonSmartphone );
        if( !isSonyEricssonNonSmartphone )
            NativeDateSettingViewer.setSonyEricssonNonSmartphone( isSonyEricssonNonSmartphone );
        else
            CustomDateSettingViewer.setSonyEricssonNonSmartphone( isSonyEricssonNonSmartphone );
    }
}
