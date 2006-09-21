/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface EditorComponent extends AlignTextComponent, SelectionComponent {
    public static final String PROPERTY_MAX_LENGTH = "maxLength";    
        
    /**
     * Sets the editor max length.
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength);

    /**
     * Gets the TextField's max length.
     * @return Returns the maxLength.
     */
    public int getMaxLength();    
}