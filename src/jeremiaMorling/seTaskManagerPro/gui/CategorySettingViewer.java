/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeremiaMorling.seTaskManagerPro.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import jeremiaMorling.seTaskManagerPro.SETMPLoc;
import jeremiaMorling.utils.displayUtils.displays.SortableList;
import jeremiaMorling.utils.displayUtils.items.IStringReceiver;
import jeremiaMorling.utils.displayUtils.items.SettingViewer;
import jeremiaMorling.utils.displayUtils.displays.TextEditor;
import jeremiaMorling.utils.managers.DM;
import jeremiaMorling.seTaskManagerPro.pim.Category;
import jeremiaMorling.seTaskManagerPro.pim.CategoryList;
import jeremiaMorling.utils.vector.IntVector;

/**
 *
 *
 * @author Jeremia Mörling
 */
public class CategorySettingViewer extends SettingViewer {

    private CategoryList categories;

    public CategorySettingViewer( Form form, String label, IntVector assignedCategories, Image icon, boolean enabled ) {
        super( form, label, SETMPLoc.getText( "common.edit" ), enabled );
        setAssignedCategories( assignedCategories );
        setIcon( icon );
    }

    private CategoryList getVectorForViewer( IntVector assignedCategories ) {
        CategoryList vectorForViewer = (CategoryList) CategoryList.getCategories().clone();

        if ( assignedCategories == null ) {
            return vectorForViewer;
        }

        for ( int i = 0; i < assignedCategories.size(); i++ ) {
            vectorForViewer.getCategoryById( assignedCategories.getInt( i ) ).setSelected( true );
        }

        return vectorForViewer;
    }

    public IntVector getAssignedCategories() {
        IntVector assignedCategories = new IntVector(1);

        for ( int i = 0; i < categories.size(); i++ ) {
            Category category = categories.getCategory( i );
            if ( category.isSelected() ) {
                assignedCategories.addElement( category.getId() );
            }
        }

        return assignedCategories;
    }
    
    public void setAssignedCategories( IntVector assignedCategories ) {
        categories = getVectorForViewer( assignedCategories );
        updateSelectedChoices();
    }

    private void updateSelectedChoices() {
        StringBuffer text = new StringBuffer();

        for ( int i = 0; i < categories.size(); i++ ) {
            Category category = categories.getCategory( i );
            if ( category.isSelected() ) {
                if ( text.length() > 0 ) {
                    text.append( ", " );
                }
                text.append( category.getText() );
            }
        }

        setValue( text.toString() );
    }
    
    public void edit() {
        DM.add( new CategorySettingEditor() );
    }

    private class CategorySettingEditor extends SortableList implements CommandListener, IStringReceiver {

        public CategorySettingEditor() {
            super( getLabel(), MULTIPLE );

            setItemsAndRefresh( categories );

            addCommand( new Command( SETMPLoc.getText( "common.done" ), Command.OK, 1 ) );
            addCommand( new Command( SETMPLoc.getText( "common.cancel" ), Command.CANCEL, 2 ) );
            addCommand( new Command( SETMPLoc.getText( "categoryChooser.newCategory" ), Command.ITEM, 2 ) );
            setCommandListener( this );
        }

        public void commandAction( Command c, Displayable d ) {
            try {
                int commandType = c.getCommandType();
                switch ( commandType ) {
                    case Command.OK:
                        for ( int i = 0; i < size(); i++ ) {
                            categories.getCategory( i ).setSelected( isSelected( i ) );
                        }
                        updateSelectedChoices();
                        DM.back( true );
                        break;
                    case Command.CANCEL:
                        DM.back();
                        break;
                    case Command.ITEM:
                        DM.add( new TextEditor( SETMPLoc.getText( "categoryChooser.newCategory" ), "", Category.NAME_MAX_LENGTH, this ) );
                        break;
                }
            } catch ( Exception e ) {
                DM.error( e, "CategorySettingViewer, CategorySettingEditor: commandAction()" );
            }
        }

        public void receiveString( String string ) {
            try {
                Category newCategory = new Category( string );
                newCategory.setSelected( true );
                appendWithoutRefresh( newCategory );
                sort();
                CategoryList.addCategory( newCategory );
            } catch ( Exception e ) {
                DM.error( e, "CategorySettingViewer, CategorySettingEditor: receiveString()" );
            }
        }
    }
}
