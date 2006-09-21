package thinwire.ui;

public interface RangeComponent extends Component {
    public static final String PROPERTY_LENGTH = "length";
    public static final String PROPERTY_CURRENT_INDEX = "currentIndex";
    
    /**
     * Retuns the current index
     * @return the index 
     * @see #setCurrentIndex(int)
     */
    public int getCurrentIndex();
    
    /**
     * Sets the current index<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_CURRENT_INDEX ) to be generated.
     * </p>
     * @param index the index of the cursor
     * @see #getCurrentIndex()
     * @see #PROPERTY_CURRENT_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setCurrentIndex(int currentIndex);
    
    /**
     * Returns the length of the range
     * @return the length of the range
     * @see #setLength(int)
     */
    public int getLength();
    
    /**
     * Sets the length of the range.  The length is the total number of increments.<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_LENGTH ) to be generated.
     * </p>
     * @param length the number of increments
     * @see #getLength()
     * @see #PROPERTY_LENGTH
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setLength(int length);
}
