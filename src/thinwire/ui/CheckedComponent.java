/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface CheckedComponent extends TextComponent {
    public static final String PROPERTY_CHECKED = "checked";
    
    /**
     * Return whether the CheckBox is checked.
     * @return whether the box is checked.
     */
    public boolean isChecked();
    
    /**
     * Programmatically sets or clears the checkbox.
     * @param checked true to set this CheckBox, false to clear it.
     */
    public void setChecked(boolean checked);    
}
