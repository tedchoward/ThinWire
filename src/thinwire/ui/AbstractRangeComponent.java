package thinwire.ui;

abstract class AbstractRangeComponent extends AbstractComponent implements RangeComponent {
    private int length;
    private int currentIndex;
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void setCurrentIndex(int currentIndex) {
        if (currentIndex < 0 || currentIndex >= length) throw new IllegalArgumentException("currentIndex < 0 || currentIndex >= length");
        int oldIndex = this.currentIndex;
        this.currentIndex = currentIndex;
        firePropertyChange(this, PROPERTY_CURRENT_INDEX, oldIndex, this.currentIndex);
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        if (length < 1) throw new IllegalArgumentException("length < 1");
        int oldLength = this.length;
        this.length = length;
        firePropertyChange(this, PROPERTY_LENGTH, oldLength, this.length);
    }
}
