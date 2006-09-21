/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.layout;

import java.util.*;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;

/**
 * @author Joshua J. Gertzen
 */
public final class VisibleLayout implements Layout {
    private static final Comparator<Region> REGION_COMPARATOR = new Comparator<Region>() {
        public int compare(Region r1, Region r2) {
            if (r1.start < r2.start) {
                return -1;
            } else if (r1.start == r2.start) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    private static class Region {
        private int start;
        private int end;
    }
    
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pce) {
            if (autoLayout) apply();
        }
    };
    
    private ItemChangeListener icl = new ItemChangeListener() {
        public void itemChange(ItemChangeEvent ice) {
            ItemChangeEvent.Type type = ice.getType();
            Component comp = (Component)ice.getNewValue();
            
            if (type == Type.REMOVE || type == Type.SET) {
                comp.setY(compToY.remove(comp));
                comp.removePropertyChangeListener(pcl);
            }
            
            if (type == Type.ADD || type == Type.SET) {                
                compToY.put(comp, comp.getY());
                comp.addPropertyChangeListener(Component.PROPERTY_VISIBLE, pcl);
            }
            
            pcl.propertyChange(null);
        }
    };
    
    private boolean autoLayout = true;
    private Container<Component> container;
    private List<Region> regions;
    private List<Region> visibleRegions;
    private Map<Component, Integer> compToY;

    public void setAutoLayout(boolean autoLayout) {
        this.autoLayout = autoLayout;
        if (autoLayout) apply();
    }
    
    public boolean isAutoLayout() {
        return autoLayout;
    }
    
    public Container<Component> getContainer() {
        return container;
    }   
    
    public void setContainer(Container<Component> container) {
        if (this.container != null) {
            for (Component comp : this.container.getChildren()) {
                comp.setY(compToY.remove(comp));
                comp.removePropertyChangeListener(pcl);
            }
            
            this.container.removeItemChangeListener(icl);
        }
        
        this.container = container;

        if (container == null) {        
            regions = new ArrayList<Region>(1);
            visibleRegions = new ArrayList<Region>(1);            
            compToY = new HashMap<Component, Integer>(1);
        } else {
            int size = (int)(container.getChildren().size() * 1.2);
            regions = new ArrayList<Region>((int)(size * .40));
            visibleRegions = new ArrayList<Region>((int)(size * .60));            
            compToY = new HashMap<Component, Integer>(size / 2);            
            
            for (Component comp : container.getChildren()) {
                compToY.put(comp, comp.getY());
                comp.addPropertyChangeListener(Component.PROPERTY_VISIBLE, pcl);
            }
            
            this.container.addItemChangeListener(icl);
        }
        
        if (autoLayout) apply();
    }    

    public void apply() {
        if (container == null) return;
        regions.clear();
        visibleRegions.clear();        
        
        for (Component comp : container.getChildren()) {
            int start = compToY.get(comp);
            int end = start + comp.getHeight() - 1;
            addRegion(start, end, comp.isVisible());
        }
        
        for (Iterator<Region> it = regions.iterator(); it.hasNext();) {
            Region r = it.next();
            
            for (Region r2 : regions.toArray(new Region[regions.size()])) {
                if (r2 != r) {
                    if (r.start <= r2.start && r.end >= r2.end) {
                        //Consumes or equals prior region
                        r2.start = r.start;
                        r2.end = r.end;
                        it.remove();
                        break;
                    } else if ((r.end - r2.start) >= 0 && r.start <= r2.start) {
                        //Overlaps to the top, therefore expand region to consume both areas
                        r2.start = r.start;
                        it.remove();
                        break;
                    } else if ((r2.end - r.start) >= 0 && r.end >= r2.end) {
                        //Overlaps to the bottom, therefore expand region to consume both areas
                        r2.end = r.end;
                        it.remove();
                        break;
                    }
                }
            }            
        }

        for (Region vr : visibleRegions) {
            for (Iterator<Region> rit = regions.iterator(); rit.hasNext();) {
                Region r = rit.next();
    
                if ((vr.start >= r.start && vr.start <= r.end) || (vr.end >= r.start && vr.end <= r.end)) {
                    rit.remove();
                }
            }
        }                
        
        Collections.sort(regions, REGION_COMPARATOR);
        
        for (Component comp : container.getChildren()) {
            int offset = 0;
            int y = compToY.get(comp);
            
            for (Region r : regions) {
                if (y >= r.end) offset += r.end - r.start + 1;
            }
            
            comp.setY(y - offset);
        }
    }
    
    private void addRegion(int start, int end, boolean visible) {
        List<Region> lr = visible ? visibleRegions : regions;
        
        for (Region r : lr) {
            if (start == r.start && end == r.end) return;
        }
        
        Region r = new Region();
        r.start = start;
        r.end = end;                
        lr.add(r);
    }
    
    protected void finalize() {
        setContainer(null);
    }
}
