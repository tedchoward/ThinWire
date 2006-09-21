/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface TextComponent extends Component {
    public static final String PROPERTY_TEXT = "text";        
    
    /**
     * This method will return the text in the field as a String.
     * @return the text in this Field
     */
    public String getText();
    
    /**
     * This method places the text in the String passed into the text field.
     * @param text the text to place in this TextField
     */
    public void setText(String text);
}