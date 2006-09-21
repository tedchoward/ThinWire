/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import java.util.regex.Pattern;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractTextComponent extends AbstractComponent implements TextComponent {
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(?<!\\r)\\n|\\r(?!\\n)");

    private String text = "";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String oldText = this.text;
        setTextDirect(text);
        firePropertyChange(this, PROPERTY_TEXT, oldText, this.text);            
    }
    
    void setTextDirect(String text) {
        if (text == null) {
            this.text = "";
        } else {
            this.text = NEW_LINE_PATTERN.matcher(text).replaceAll("\r\n");
        }
    }
}