/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

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
