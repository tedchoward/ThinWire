/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface MaskEditorComponent extends EditorComponent {
    public static final String PROPERTY_EDIT_MASK = "editMask";
    public static final String PROPERTY_FORMAT_TEXT = "formatText";
    
    /**
     * Get this TextField's edit mask
     * @return the edit mask
     */
    public String getEditMask();

    /**
     * This method accepts an edit mask as a String and applies it to the text field.
     * @param editMask
     */
    public void setEditMask(String editMask);

    /**
     * Determines whether the text returned by getText() is formatted.
     * @return true if the text is formatted, false otherwise.  Default is true.
     */
    public boolean isFormatText();
    
    /**
     * Sets whether the text returned by getText() is formatted.
     * If an editMask is specified that contains with format charcters, such as ###,###,###.## and
     * this property is set to false, then the value returned by getText will not contain the the
     * commas from the editMask.  i.e. If the value in the field is 123,456.78 then 123456.78 would
     * be returned.  Whereas, if this property is set to true then 123,456.78 would be returned.
     * Another example would be with a mask of MM/dd/yyyy, value of 11/21/1978.  With this set to false
     * you would get the value 11211978 by calling getText(), with it set to true, you'd get 11/21/1978.
     * @param formatText true if you want the text formattted, false otherwise.  Default is true.
     */
    public void setFormatText(boolean formatText);
}