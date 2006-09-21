/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.style;

/**
 * @author Joshua J. Gertzen
 */
public class FX {
    public static final String PROPERTY_FX_POSITION_CHANGE = "fxPositionChange";
    public static final String PROPERTY_FX_SIZE_CHANGE = "fxSizeChange";
    public static final String PROPERTY_FX_VISIBLE_CHANGE = "fxVisibleChange";
    
    public enum Type {
        NONE, SMOOTH;
        
        public String toString() {
            return this.name().toLowerCase();
        }
    }
    
    private Style parent;
    private Type positionChange;
    private Type sizeChange;
    private Type visibleChange;
    
    FX(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getFX());        
    }    
        
    public void copy(FX fx) {
        if (fx == null) throw new IllegalArgumentException("fx == null");
        setPositionChange(fx.getPositionChange());
        setSizeChange(fx.getSizeChange());
        setVisibleChange(fx.getVisibleChange());
    }
        
    public Style getParent() {
        return parent;
    }
    
    public Type getPositionChange() {
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        return positionChange;
    }
    
    public void setPositionChange(Type positionChange) {
        if (positionChange == null && parent.defaultStyle != null) positionChange = parent.defaultStyle.getFX().getPositionChange();
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        Type oldPositionChange = this.positionChange;
        this.positionChange = positionChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_POSITION_CHANGE, oldPositionChange, this.positionChange);        
    }
    
    public Type getSizeChange() {
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        return sizeChange;
    }
       
    public void setSizeChange(Type sizeChange) {
        if (sizeChange == null && parent.defaultStyle != null) sizeChange = parent.defaultStyle.getFX().getSizeChange();
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        Type oldSizeChange = this.sizeChange;
        this.sizeChange = sizeChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_SIZE_CHANGE, oldSizeChange, this.sizeChange);
    }

    public Type getVisibleChange() {
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        return visibleChange;
    }

    public void setVisibleChange(Type visibleChange) {
        if (visibleChange == null && parent.defaultStyle != null) visibleChange = parent.defaultStyle.getFX().getVisibleChange();
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        Type oldVisibleChange = this.visibleChange;
        this.visibleChange = visibleChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_VISIBLE_CHANGE, oldVisibleChange, this.visibleChange);
    }
}
