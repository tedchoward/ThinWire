/*
 * Created on Jul 14, 2006
  */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public interface SelectionComponent extends Component {
    public static final String PROPERTY_LENGTH = "length";
    public static final String PROPERTY_SELECTION_BEGIN_INDEX = "selectionBeginIndex";
    public static final String PROPERTY_SELECTION_END_INDEX = "selectionEndIndex";    
    public static final String PROPERTY_CURSOR_INDEX = "cursorIndex";
    
    /**
     * Sets the range of the selection<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes 2 <code>PropertyChangeEvent</code>s ( propertyName = PROPERTY_SELECTION_BEGIN_INDEX and propertyName = PROPERTY_SELECTION_END_INDEX ) to be generated.
     * </p>
     * @param selectionBeginIndex the index of the start of the selection
     * @param selectionEndIndex the index of the end of the selection
     * @see #setSelectionBeginIndex(int)
     * @see #setSelectionEndIndex(int)
     * @see #PROPERTY_SELECTION_BEGIN_INDEX
     * @see #PROPERTY_SELECTION_END_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setSelectionRange(int selectionBeginIndex, int selectionEndIndex);
    
    /**
     * Returns the index of the beginning of the selection
     * @return the index at the start of the selection or -1 if there is no selection
     */
    public int getSelectionBeginIndex();
    
    /**
     * Sets the index of the beginning of a selection
     * @param selectionBeginIndex the index of the start of the selection
     */
    public void setSelectionBeginIndex(int selectionBeginIndex);
    
    /**
     * Returns the index of the end of the selection
     * @return the index of the end of the selection or -1 if there is no selection
     */
    public int getSelectionEndIndex();
    
    /**
     * Sets the index of the end of the selection
     * @param selectionEndIndex the index of the end of the selection
     */
    public void setSelectionEndIndex(int selectionEndIndex);
    
    /**
     * Retuns the index of the current cursor position
     * @return the index of the cursor or -1 if there is a selection
     */
    public int getCursorIndex();
    
    /**
     * Sets the index of the cursor position
     * @param index the index of the cursor
     */
    public void setCursorIndex(int index);
    
    /**
     * Returns the length of the selectable area
     * @return the length of the selectable area
     */
    public int getLength();
}
