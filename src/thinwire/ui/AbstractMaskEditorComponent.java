/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractMaskEditorComponent extends AbstractEditorComponent implements MaskEditorComponent {
    private static final String MASK_CHARS = "9#MdyAaXxhmp";

    private String editMask = "";
    String formattedText = "";
    private boolean formatText = true;
    
    public String getEditMask() {
        return editMask;
    }
    
    public void setEditMask(String editMask) {
        String oldEditMask = this.editMask;
        editMask = editMask == null ? "" : editMask;
        this.editMask = editMask;
        firePropertyChange(this, PROPERTY_EDIT_MASK, oldEditMask, editMask);
    }

    public boolean isFormatText() {
        return formatText;
    }
    
    public void setFormatText(boolean formatText) {
        boolean oldFormatText = this.formatText;        
        this.formatText = formatText;        

        //This modifies the text without firing a propertyChange which is what
        //you want in this case, because changing whether a value is formatted is not
        //technically chaning the value itself.
        if (formatText) {
            setTextDirect(formattedText);
        } else {
            setTextDirect(getUnformattedText(getText()));
        }
        
        firePropertyChange(this, PROPERTY_FORMAT_TEXT, oldFormatText, formatText);
    }    

    String getUnformattedText(String text) {
        if (!formatText && text.length() > 0) {
            StringBuilder sb = new StringBuilder();
            
            if (editMask.indexOf('#') >= 0) {
                boolean hasDot = false;
                if (text.charAt(0) == '-') sb.append('-');
                
                for (int i = 0, len = text.length(); i < len; i++) {
                    char c = text.charAt(i);
                    
                    if ("0123456789".indexOf(c) >= 0) {
                        sb.append(c);                    
                    } else if (c == '.' && !hasDot) {
                        sb.append(c);
                        hasDot = true;
                    }
                }
            } else {                        
                for (int i = editMask.length(); --i >= 0;) {
                    char c = editMask.charAt(i);
                    if (MASK_CHARS.indexOf(c) == -1) sb.append(c);
                }
                
                String formatChars = sb.toString();
                sb.setLength(0);
                
                for (int i = 0, len = text.length(); i < len; i++) {
                    char c = text.charAt(i);                
                    if (formatChars.indexOf(c) == -1) sb.append(c);
                }
            }
            
            text = sb.toString();
        }
        
        return text;
    }
}
