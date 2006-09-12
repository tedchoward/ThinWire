/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;

import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;

/**
 * @author Joshua J. Gertzen
 */
public class DropDown<T extends Component> extends AbstractMaskEditorComponent {
    public static final String PROPERTY_EDIT_ALLOWED = "editAllowed";
    public static final String PROPERTY_VIEW = "view";
    public static final String PROPERTY_COMPONENT = "component";
    
    public static interface View<T extends Component> {
        DropDown<T> getDropDown();
        Object getValue();
        void setValue(Object value);
    }   
    

    //TODO: DropDown should have a second style for defining what the Button looks like.
    //      Also, the DropDown's Button should inherit from the Button.class style so that all
    //      Buttons look the same by default.
    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        s.getBackground().setColor(Color.WINDOW);
        Border b = s.getBorder();
        b.setSize(2);
        b.setType(Border.Type.INSET);
        b.setColor(Color.THREEDFACE);        
        setDefaultStyle(DropDown.class, s);
    }    
    
    private boolean editAllowed = true;
    private DropDown.View<T> view;
    private T comp;
    
    DropDown(DropDown.View<T> view, T comp) {
        if (view == null) throw new IllegalArgumentException("view == null");
        if (comp == null) throw new IllegalArgumentException("comp == null");
        setView(view);
        setComponent(comp);
    }    
        
    public void setText(String text) {
        formattedText = text = text == null ? "" : text;
        getView().setValue(text);
        super.setText(getUnformattedText(text));
    }        
        
    public View<T> getView() {
        return view;
    }
    
    public void setView(View<T> view) {
        if (view == null) throw new IllegalArgumentException("view == null");
        View<T> oldView = this.view;
        this.view = view;
        firePropertyChange(this, PROPERTY_VIEW, oldView, view);
    }
    
    /**
     * Returns the component that is displayed upon clicking the drop down button.
     * @return the component that is displayed upon clicking the drop down button.
     */
    public T getComponent() {
        return comp;
    }
    
    /**
     * Assigns the component that is displayed upon clicking the drop down button.
     * @param comp the component to display upon clicking the drop down button.
     */
    public void setComponent(T comp) {
        if (comp == null) throw new IllegalArgumentException("comp == null");
        if (comp.getParent() != null) throw new IllegalStateException("comp.getParent() != null");
        T oldComp = getComponent();
        if (oldComp != null) ((AbstractComponent)oldComp).setParent(null);        
        this.comp = comp;
        ((AbstractComponent)comp).setParent(this);
        firePropertyChange(this, PROPERTY_COMPONENT, oldComp, comp);
    }
    
    /**
     * Returns whether you can type in the text field.
     * @return true if the field is editable (Default = true)
     */
    public boolean isEditAllowed() {
        return editAllowed;
    }

    /**
     * Toggles a boolean variable that determines if the text field will be active (editable).
     * @param editAllowed If true the field will be editable. (Default = true)
     */
    public void setEditAllowed(boolean editAllowed) {
        boolean oldEditAllowed = this.editAllowed;
        this.editAllowed = editAllowed;
        firePropertyChange(this, PROPERTY_EDIT_ALLOWED, oldEditAllowed, editAllowed);
    }    
}
