/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import thinwire.ui.Application;
import thinwire.ui.Component;
import thinwire.ui.RadioButton;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class RadioButtonRenderer extends ComponentRenderer {
    private static final String RADIOBUTTON_CLASS = "tw_RadioButton";
    private static final String SET_CHECKED = "setChecked";
    private static final String SET_GROUP = "setGroup";

    private Application.Local<Map<Integer, List<RadioButtonRenderer>>> groups = new Application.Local<Map<Integer, List<RadioButtonRenderer>>>() {
        public Map<Integer, List<RadioButtonRenderer>> initialValue() {
            return new HashMap<Integer, List<RadioButtonRenderer>>(5);
        }
    };
    
    private int groupId;
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(RADIOBUTTON_CLASS, wr, c, container);
		RadioButton rb = (RadioButton)c;
        addClientSideProperty(RadioButton.PROPERTY_CHECKED);
        addInitProperty(RadioButton.PROPERTY_TEXT, rb.getText());
        addInitProperty(RadioButton.PROPERTY_CHECKED, rb.isChecked());
        setGroup(System.identityHashCode(rb.getGroup()));
        addInitProperty(RadioButton.PROPERTY_GROUP, groupId);
        super.render(wr, c, container);
	}
    
    private List<RadioButtonRenderer> getRenderGroup(Integer groupId) {
        Map<Integer, List<RadioButtonRenderer>> map = groups.get();
        List<RadioButtonRenderer> group = map.get(groupId);
        if (group == null) map.put(groupId, group = new ArrayList<RadioButtonRenderer>(3));
        return group;
    }
    
    private void setGroup(int groupId) {
        if (this.groupId != 0) {
            for (Iterator<RadioButtonRenderer> it = getRenderGroup(this.groupId).iterator(); it.hasNext();) {
                if (it.next() == this) {
                    it.remove();
                    break;
                }
            }
        }
        
        this.groupId = groupId;
        if (groupId != 0) getRenderGroup(groupId).add(this);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;

        if (name.equals(RadioButton.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, pce.getNewValue());
        } else if (name.equals(RadioButton.PROPERTY_CHECKED)) {
            postClientEvent(SET_CHECKED, pce.getNewValue());
        } else if (name.equals(RadioButton.PROPERTY_GROUP)) {
            setGroup(System.identityHashCode(pce.getNewValue()));
            postClientEvent(SET_GROUP, groupId);
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        RadioButton rb = (RadioButton)comp;
        
        if (name.equals(RadioButton.PROPERTY_CHECKED)) {
            RadioButton.Group group = rb.getGroup();
            
            if (group != null) {
                List<RadioButtonRenderer> lst = getRenderGroup(System.identityHashCode(group));
                
                for (RadioButtonRenderer r : lst) {
                    r.setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, true);
                }
                
                rb.setChecked(Boolean.valueOf(value).booleanValue());
                
                for (RadioButtonRenderer r : lst) {
                    r.setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, false);
                }
            } else {
                setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, true);
                rb.setChecked(Boolean.valueOf(value).booleanValue());
                setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, false);
            }
        } else {
            super.componentChange(event);
        }
    }
    
    void destroy() {
        setGroup(0);
        super.destroy();
    }
}
