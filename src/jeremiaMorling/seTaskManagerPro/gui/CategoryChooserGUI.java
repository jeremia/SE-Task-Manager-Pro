/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import jeremiaMorling.seTaskManagerPro.Config;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.seTaskManagerPro.RM;
import jeremiaMorling.seTaskManagerPro.SETaskManagerPro;
import jeremiaMorling.seTaskManagerPro.pim.Category;
import jeremiaMorling.seTaskManagerPro.pim.CategoryList;
import jeremiaMorling.seTaskManagerPro.pim.TaskList;
import jeremiaMorling.utils.displayUtils.displays.SortableList;
import jeremiaMorling.utils.displayUtils.displays.TextEditor;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.utils.managers.IFocusable;
import jeremiaMorling.utils.vector.ListItem;

/**
 *
 * @author Jeremia
 */
public class CategoryChooserGUI extends SortableList implements CommandListener, IFocusable {
    private SETaskManagerPro seTaskManagerPro;
    
    public static final int ALL_ACTIVE = 0;
    public static final int COMPLETED = 1;
    public static final int SEARCH = 2;
    public static final int UNCATEGORIZED = 3;
    public static final int CATEGORY = -1;
    
    private static final int PRIO_NEW_CATEGORY = 1;
    private static final int PRIO_RENAME = 2;
    private static final int PRIO_DELETE_CATEGORY = 3;
    private static final int PRIO_REFRESH = 4;
    private static final int PRIO_ABOUT = 5;
    
    public CategoryChooserGUI( SETaskManagerPro seTaskManagerPro ) {
        super( SETMPLoc.getText( "categoryChooser.chooseCategory" ), IMPLICIT );
        this.seTaskManagerPro = seTaskManagerPro;
        
        appendTopItem( new ListItem( SETMPLoc.getText( "categoryChooser.allActive" ), RM.getImage( RM.PRIO_NORMAL_24 ) ) );
        appendTopItem( new ListItem( SETMPLoc.getText( "categoryChooser.completed" ), RM.getImage( RM.TASK_COMPLETED_24 ) ) );
        appendTopItem( new ListItem( SETMPLoc.getText( "categoryChooser.searchTask" ), RM.getImage( RM.SEARCH_24 ) ) );
        appendTopItem( new ListItem( SETMPLoc.getText( "categoryChooser.uncategorized" ), RM.getImage( RM.CATEGORY_24 ) ) );
        
        //refreshCategories();
        setItemsAndRefresh( CategoryList.getCategories() );
        
        setSelectCommand( new Command( SETMPLoc.getText( "common.select" ), Command.OK, 0 ) );
        addCommand( new Command( SETMPLoc.getText( "categoryChooser.newCategory" ), Command.ITEM, PRIO_NEW_CATEGORY ) );
        addCommand( new Command( SETMPLoc.getText( "categoryChooser.renameCategory" ), Command.ITEM, PRIO_RENAME ) );
        addCommand( new Command( SETMPLoc.getText( "categoryChooser.deleteCategory" ), Command.ITEM, PRIO_DELETE_CATEGORY ) );
        if( !Config.isPIMChangeListenerSupported() )
            addCommand( new Command( SETMPLoc.getText( "categoryChooser.refresh"), Command.SCREEN, PRIO_REFRESH ) );
        addCommand( new Command( SETMPLoc.getText( "about" ), Command.HELP, PRIO_ABOUT ) );
        addCommand( new Command( SETMPLoc.getText( "categoryChooser.minimize" ), Command.STOP, 0 ) );
        addCommand( new Command( SETMPLoc.getText( "categoryChooser.exit" ), Command.EXIT, 3 ) );
        setCommandListener( this );
    }

    public void commandAction( Command c, Displayable d ) {
        //refreshSelectedCategory();
        switch( c.getCommandType() ) {
            case Command.OK:
                if( isSelectedItemTopItem() ) {
                    int mode = getSelectedIndex();
                    if( mode != SEARCH ) {
                        TaskListGUI.show( mode );
                    } else {
                        DM.add( new SearchTaskGUI() );
                    }
                } else {
                    TaskListGUI.show( getSelectedCategory() );
                }
                break;
            case Command.STOP:
                seTaskManagerPro.minimize();
                break;
            case Command.ITEM:
                switch( c.getPriority() ) {
                    case PRIO_NEW_CATEGORY:
                        DM.add( new NewCategoryGUI() );
                        break;
                    case PRIO_RENAME:
                        if( !isSelectedItemTopItem() )
                            DM.add( new RenameCategoryGUI() );
                        break;
                    case PRIO_DELETE_CATEGORY:
                        if( !(isSelectedItemTopItem()) )
                            DM.add( new DeleteCategoryDialog() );
                        break;
                }
                break;
            case Command.SCREEN:
                TaskList.refreshBaseTaskList();
                break;
            case Command.HELP:
                DM.add( new AboutGUI() );
                break;
            case Command.EXIT:
                seTaskManagerPro.exit();
                break;
        }
    }
    
    public Category getSelectedCategory() {
        return (Category)getSelectedItem();
    }

    public void focusGained( boolean refreshNeeded ) {
        if( refreshNeeded ) {
            TaskList.sortBaseTasks();
            if( CategoryList.refreshNeeded() )
                refresh();
        }
    }
    
    private class NewCategoryGUI extends TextEditor {
        private NewCategoryGUI() {
            super( SETMPLoc.getText( "categoryChooser.newCategory" ), 100 );
        }

        public void receiveString( String string ) {
            if( string == null || string.equals( "" ) ) {
                DM.newAlert(
                        SETMPLoc.getText( "categoryChooser.emptyName.title" ),
                        SETMPLoc.getText( "categoryChooser.emptyName.new.text" ),
                        RM.getImage( RM.ERROR_64 ) );
                return;
            }
            
            Category newCategory = new Category( string );
            newCategory.setSelected( true );
            try {
                CategoryList.addCategory( newCategory );
            } catch( Exception e ) {
                DM.error( e, "CategoryChooserGUI: receiveString(), CategoryList.addCategory()" );
            }
            refresh();
            
            DM.back();
        }
    }
    
    private class RenameCategoryGUI extends TextEditor {
        private RenameCategoryGUI() {
            super( SETMPLoc.getText( "categoryChooser.renameCategory" ), getSelectedCategory().getName(), 100 );
        }

        public void receiveString( String string ) {
            if( string == null || string.equals( "" ) ) {
                DM.newAlert(
                        SETMPLoc.getText( "categoryChooser.emptyName.title" ),
                        SETMPLoc.getText( "categoryChooser.emptyName.rename.text" ),
                        RM.getImage( RM.ERROR_64 ) );
                return;
            }

            Category selectedCategory = getSelectedCategory();
            selectedCategory.changeName( string );
            selectedCategory.setSelected( true );
            sort();
            
            DM.back();
        }
    }
    
    private class DeleteCategoryDialog extends Alert implements CommandListener {
        private DeleteCategoryDialog() {
            super(
                    SETMPLoc.getText( "categoryChooser.deleteCategory.title" ),
                    getSelectedCategory().getName() + SETMPLoc.getText( "common.deleteQuestion" ),
                    RM.getImage( RM.WARNING_64 ),
                    null );
            
            addCommand( new Command( SETMPLoc.getText( "common.yes" ), Command.OK, 0 ) );
            addCommand( new Command( SETMPLoc.getText( "common.no" ), Command.CANCEL, 1 ) );
            setCommandListener( this );
        }

        public void commandAction( Command c, Displayable d ) {
            switch( c.getCommandType() ) {
                case Command.OK:
                    CategoryList.deleteCategory( getSelectedCategory() );
                    refresh();
                case Command.CANCEL:
                    DM.back();
                    break;
            }
        }
    }
}
