/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.ui;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.web.WebApplication;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.Background;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.FX;
import thinwire.ui.style.Font;
import thinwire.ui.style.Style;

/**
 * The generic <code>DropDown</code> component allows you to place an
 * arbitrary component in a <code>DropDown</code>. In order to implement
 * this, the developer must implement a <code>DropDown.View</code> (or extend
 * <code>AbstractView</code>) for the <code>Component</code> they are
 * placing in the <code>DropDown</code>.
 * 
 * @see thinwire.ui.DropDownDateBox
 * @see thinwire.ui.DropDownGridBox
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public class DropDown<T extends Component> extends AbstractMaskEditorComponent {
    public static final String PROPERTY_EDIT_ALLOWED = "editAllowed";
    public static final String PROPERTY_VIEW = "view";
    public static final String PROPERTY_COMPONENT = "component";
    
    private static final String[] STYLE_PROPERTIES = {Font.PROPERTY_FONT_BOLD, Font.PROPERTY_FONT_COLOR, Font.PROPERTY_FONT_FAMILY,
        Font.PROPERTY_FONT_ITALIC, Font.PROPERTY_FONT_SIZE, Font.PROPERTY_FONT_UNDERLINE, Font.PROPERTY_FONT_STRIKE
    };

    static void copyDropDownStyle(Component parent, Component child, boolean copyBackground) {
        Style cs = child.getStyle();
        Style ps = parent.getStyle();
        Border csb = cs.getBorder();
        Border dcsb = Application.getDefaultStyle(child.getClass()).getBorder();
        if (csb.getColor().equals(dcsb.getColor())) csb.setColor(Color.WINDOWFRAME);
        if (csb.getType().equals(dcsb.getType())) csb.setType(Border.Type.SOLID);
        if (csb.getSize() == dcsb.getSize()) csb.setSize(1);
        if (copyBackground) cs.getBackground().copy(ps.getBackground(), true); 
        cs.getFont().copy(ps.getFont(), true);
    }
        
    public static interface View<T extends Component> {
        DropDown<T> getDropDown();
        Object getValue();
        void setValue(Object value);
        int getOptimalWidth();
        int getOptimalHeight();
        void setComponent(T comp);
    }
    
    public static abstract class AbstractView<T extends Component> implements View<T> {
    	
    	protected DropDown<T> dd;
    	protected T ddc;
    	
    	protected void init(DropDown<T> dropDown, T comp) {
    		dd = dropDown;
    		ddc = comp;
            
            if (dd != null) {
                String oldValue = dd.getText();
                if (oldValue.length() > 0) setValue(oldValue);
            }
    	}
    	
    	protected void addCloseComponent(final Component comp) {
            if (dd == null) throw new IllegalStateException("dd == null");
            
            final WebApplication app = (WebApplication) Application.current();
            
            if (app != null) {
	            app.addRenderStateListener(comp, new RenderStateListener() {
	                public void renderStateChange(RenderStateEvent ev) {
	                    app.clientSideMethodCall(app.getComponentId(dd), "addCloseComponent", app.getComponentId(comp));
	                }
	            });
            }
    	}
    	
    	public int getOptimalWidth() {
    		return dd.getWidth();
    	}
        
        public void setComponent(T comp) {
            init(dd, comp);
        }
    	
    }
    
    private boolean editAllowed = true;
    private DropDown.View<T> view;
    private T comp;
    
    protected DropDown(DropDown.View<T> view, T comp) {
        if (view == null) throw new IllegalArgumentException("view == null");
        if (comp == null) throw new IllegalArgumentException("comp == null");
        setView(view);
        setComponent(comp);
        
        addPropertyChangeListener(STYLE_PROPERTIES, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                String propertyName = ev.getPropertyName();
                Style s = getComponent().getStyle();
                Object o = ev.getNewValue();
                s.setProperty(propertyName, o);
            }
        });
        
        addPropertyChangeListener(PROPERTY_WIDTH, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (DropDown.this.comp != null) DropDown.this.comp.setWidth(getView().getOptimalWidth());
            }
        });
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
        copyDropDownStyle(this, comp, false);
        if (getView().getDropDown() != null) getView().setComponent(comp);
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
