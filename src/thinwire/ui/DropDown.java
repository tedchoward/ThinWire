/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.style.*;
import thinwire.ui.event.*;

/**
 * @author Joshua J. Gertzen
 */
public class DropDown<T extends Component> extends AbstractMaskEditorComponent {
    public static final String PROPERTY_EDIT_ALLOWED = "editAllowed";
    public static final String PROPERTY_VIEW = "view";
    public static final String PROPERTY_COMPONENT = "component";
    
    static final String[] STYLE_PROPERTIES = {Font.PROPERTY_FONT_BOLD, Font.PROPERTY_FONT_COLOR, Font.PROPERTY_FONT_FAMILY,
        Font.PROPERTY_FONT_ITALIC, Font.PROPERTY_FONT_SIZE, Font.PROPERTY_FONT_UNDERLINE, Border.PROPERTY_BORDER_COLOR, Border.PROPERTY_BORDER_TYPE,
        Border.PROPERTY_BORDER_SIZE, Background.PROPERTY_BACKGROUND_COLOR, FX.PROPERTY_FX_POSITION_CHANGE, FX.PROPERTY_FX_SIZE_CHANGE,
        FX.PROPERTY_FX_VISIBLE_CHANGE
    };
    
    static void setStyleValue(Style s, String propertyName, Object o) {
        if (propertyName.equals(Background.PROPERTY_BACKGROUND_COLOR)) {
            s.getBackground().setColor((Color)o);
        } else if (propertyName.equals(Border.PROPERTY_BORDER_COLOR)) {
            s.getBorder().setColor((Color)o);
        } else if (propertyName.equals(Border.PROPERTY_BORDER_TYPE)) {
            s.getBorder().setType((Border.Type)o);
        } else if (propertyName.equals(Border.PROPERTY_BORDER_SIZE)) {
            s.getBorder().setSize((Integer)o); 
        } else if (propertyName.equals(Font.PROPERTY_FONT_FAMILY)) {
            s.getFont().setFamily((Font.Family)o); 
        } else if (propertyName.equals(Font.PROPERTY_FONT_SIZE)) {
            s.getFont().setSize((Integer)o); 
        } else if (propertyName.equals(Font.PROPERTY_FONT_COLOR)) {
            s.getFont().setColor((Color)o); 
        } else if (propertyName.equals(Font.PROPERTY_FONT_BOLD)) {
            s.getFont().setBold((Boolean)o); 
        } else if (propertyName.equals(Font.PROPERTY_FONT_ITALIC)) {
            s.getFont().setItalic((Boolean)o); 
        } else if (propertyName.equals(Font.PROPERTY_FONT_UNDERLINE)) {
            s.getFont().setUnderline((Boolean)o);
        } else if (propertyName.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {
            s.getFX().setPositionChange((FX.Type)o);
        } else if (propertyName.equals(FX.PROPERTY_FX_SIZE_CHANGE)) {
            s.getFX().setSizeChange((FX.Type)o);
        } else if (propertyName.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
            s.getFX().setVisibleChange((FX.Type)o);
        }
    }

    static void copyDropDownStyle(Component parent, Component child, boolean halfBorder) {
        Style cs = child.getStyle();
        Style ps = parent.getStyle();
        Border csb = cs.getBorder();
        Border dcsb = Application.current().getDefaultStyle(child.getClass()).getBorder();
        int borderSize = ps.getBorder().getSize();
        if (halfBorder) borderSize = DropDown.calcDropDownBorderSize(borderSize);
            
        if (csb.getColor().equals(dcsb.getColor())) csb.setColor(Color.WINDOWFRAME);
        if (csb.getType().equals(dcsb.getType())) csb.setType(Border.Type.SOLID);
        if (csb.getSize() == dcsb.getSize()) csb.setSize(borderSize);
        
        cs.getBackground().copy(ps.getBackground(), true);
        cs.getFont().copy(ps.getFont(), true);
        cs.getFX().copy(ps.getFX(), true);        
    }
    
    static int calcDropDownBorderSize(int size) {
        size /= 2;
        if (size < 1) size = 1;
        return size;        
    }    
    
    public static interface View<T extends Component> {
        DropDown<T> getDropDown();
        Object getValue();
        void setValue(Object value);
    }   
    
    private boolean editAllowed = true;
    private DropDown.View<T> view;
    private T comp;
    
    DropDown(DropDown.View<T> view, T comp) {
        if (view == null) throw new IllegalArgumentException("view == null");
        if (comp == null) throw new IllegalArgumentException("comp == null");
        setView(view);
        setComponent(comp);
        
        addPropertyChangeListener(STYLE_PROPERTIES, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                String propertyName = ev.getPropertyName();
                if (propertyName.equals(Border.PROPERTY_BORDER_TYPE) || propertyName.equals(Border.PROPERTY_BORDER_COLOR)) return;
                Style s = getComponent().getStyle();
                Object o = ev.getNewValue();
                if (propertyName.equals(Border.PROPERTY_BORDER_SIZE)) o = calcDropDownBorderSize((Integer)o);
                setStyleValue(s, propertyName, o);
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
        copyDropDownStyle(this, comp, true);
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
