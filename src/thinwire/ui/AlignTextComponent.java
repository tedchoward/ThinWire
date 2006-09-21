/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface AlignTextComponent extends TextComponent {
    public static final String PROPERTY_ALIGN_X = "alignX";

    /**
     * Get this label's text justification
     * @return this label's text justification
     */
    public AlignX getAlignX();    
    
    /**
     * Sets the text justification of the label (left, right, or center).
     * @param alignX (Default = AlignX.LEFT)
     * @throws IllegalArgumentException if alignX is null 
     */
    public void setAlignX(AlignX alignX);
}
