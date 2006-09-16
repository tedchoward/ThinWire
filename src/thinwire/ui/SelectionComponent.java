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
    
    public void setSelectionRange(int selectionBeginIndex, int selectionEndIndex);
    public int getSelectionBeginIndex();
    public void setSelectionBeginIndex(int selectionBeginIndex);
    public int getSelectionEndIndex();
    public void setSelectionEndIndex(int selectionEndIndex);
    public int getCursorIndex();
    public void setCursorIndex(int index);
    
    public int getLength();
}
