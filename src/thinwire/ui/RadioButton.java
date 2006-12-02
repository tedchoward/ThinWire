/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.PropertyChangeListener;

/**
 * A RadioButton is a screen element that usually appears in groups. Radio buttons can be either checked or cleared, but only one
 * radio button per group may be checked.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/RadioButton-1.png"> <br>
 * 
 * <pre>
 * final RadioButton rb1 = new RadioButton(&quot;Yes&quot;);
 * rb1.setBounds(20, 20, 70, 30);
 * final RadioButton rb2 = new RadioButton(&quot;No&quot;);
 * rb2.setBounds(100, 20, 70, 30);
 * 
 * RadioButton.Group rbg = new RadioButton.Group();
 * rbg.add(rb1);
 * rbg.add(rb2);
 * rbg.addPropertyChangeListener(RadioButton.PROPERTY_CHECKED, new PropertyChangeListener() {
 *     public void propertyChange(PropertyChangeEvent pce) {
 *         RadioButton rb = (RadioButton) pce.getSource();
 * 
 *         if (rb == rb1) {
 *             rb.setText(rb.isChecked() ? &quot;[YES]&quot; : &quot;Yes&quot;);
 *         } else {
 *             rb.setText(rb.isChecked() ? &quot;[NO]&quot; : &quot;No&quot;);
 *         }
 *     }
 * });
 * 
 * Frame f = Application.current().getFrame();
 * f.getChildren().add(rb1);
 * f.getChildren().add(rb2);
 * </pre>
 * 
 * </p>
 * <p>
 * <b>Keyboard Navigation:</b><br>
 * <table border="1">
 * <tr>
 * <td>KEY</td>
 * <td>RESPONSE</td>
 * <td>NOTE</td>
 * </tr>
 * <tr>
 * <td>Space</td>
 * <td>Fires PropertyChangeEvent( propertyName = RadioButton.PROPERTY_CHECKED )</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table>
 * </p>
 * @author Joshua J. Gertzen
 */
public class RadioButton extends AbstractTextComponent implements CheckedComponent {
	public static class Group extends AbstractList<RadioButton> {
	    private RadioButton checked;
	    private List<RadioButton> l = new ArrayList<RadioButton>(3);
        
	    public void add(int index, RadioButton o) {
            RadioButton rb = (RadioButton)o;            
		    if (rb.getGroup() != null) throw new IllegalStateException("rb.getGroup() != null");		               
            l.add(index, rb);
            modCount++;
            rb.setGroup(this);
            if (rb.isChecked()) rb.setChecked(true); //Cause the checked state of the button to get in sync with the group.
	    }
	    
        public RadioButton get(int index) {
            return l.get(index);
        }

        public RadioButton set(int index, RadioButton o) {
            RadioButton rb = (RadioButton)o;            
		    if (rb.getGroup() != null) throw new IllegalStateException("rb.getGroup() != null");
            RadioButton ret = (RadioButton)l.set(index, rb);
            ret.setGroup(null);
            if (checked == ret) checked = null;		    
            rb.setGroup(this);
            if (rb.isChecked()) rb.setChecked(true); //Cause the checked state of the button to get in sync with the group.
            return ret;
        }        
        
        public RadioButton remove(int index) {
            modCount++;
            RadioButton ret = (RadioButton)l.remove(index);
            ret.setGroup(null);
            if (checked == ret) checked = null;
            return ret;
        }
        
        public int size() {
            return l.size();            
        }
        //#IFDEF V1_1_COMPAT
        
    	/**
    	 * Adds a PropertyChangeListener to each radio button component in the group.
    	 * @param listener
         * @throws IllegalStateException if compat mode is not on
    	 * @deprecated for performance reasons, this form as been deprecated.  Use the named property form instead.
    	 */
    	public final void addPropertyChangeListener(PropertyChangeListener listener) {
            if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use addPropertyChangeListener(propertyName, listener) instead.");        
    	    if (listener == null) return;
            
            for (RadioButton rb : l) {
                rb.addPropertyChangeListener(listener);
            }
    	}
        //#ENDIF
    	
        /**
         * Adds a PropertyChangeListener to each radio button component in the group.
         * @param propertyName
         * @param listener
         */
        public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            for (RadioButton rb : l) {
                rb.addPropertyChangeListener(propertyName, listener);
            }            
        }
        
        /**
         * Adds a PropertyChangeListener to each radio button component in the group.
         * @param propertyNames
         * @param listener
         */
        public final void addPropertyChangeListener(String[] propertyNames, PropertyChangeListener listener) {
            for (RadioButton rb : l) {
                rb.addPropertyChangeListener(propertyNames, listener);
            }            
        }
        
    	/**
    	 * Removes a preexisting PropertyChangeListener from each component in the group.
    	 * @param listener the listener to remove
    	 */
    	public final void removePropertyChangeListener(PropertyChangeListener listener) {
    	    if (listener == null) return;
            
            for (RadioButton rb : l) {
                rb.removePropertyChangeListener(listener);
            }
    	}        

        public boolean isEnabled() {
            boolean enabled = true;
            
            for (RadioButton rb : l) {
                if (!rb.isEnabled()) {
                    enabled = false;
                    break;                    
                }
            }
            
            return enabled;
        }
        
	    public void setEnabled(boolean enabled) {
            for (RadioButton rb : l) {            
                rb.setEnabled(enabled);
            }
	    }
        
        public boolean isVisible() {
            boolean visible = true;
            
            for (RadioButton rb : l) {
                if (!rb.isVisible()) {
                    visible = false;
                    break;
                }
            }            
            
            return visible;
        }
        
        public void setVisible(boolean visible) {
            for (RadioButton rb : l) {
                rb.setVisible(visible);
            }
        }
        
        public Object getUserObject() {
            Object obj = null;
            
            for (RadioButton rb : l) {
                obj = rb.getUserObject();
                if (obj != null) break;
            }
            
            return obj;
        }
        
        public void setUserObject(Object userObject) {
            for (RadioButton rb : l) {
                rb.setUserObject(userObject);
            }
        }
        
        /**
         * 
         * @return
         */
	    public RadioButton getChecked() {
	        return checked;
	    }
	    
        /**
         * 
         * @param rb
         */
	    public void setChecked(RadioButton rb) {
	        rb.setChecked(true);
	    }
        //#IFDEF V1_1_COMPAT
	    
        /**
         * Returns getChecked().getValue().
         * @return getChecked().getValue();
         * @deprecated although their is no direct equivalent, a similar result could be accomplished with getChecked().getUserObject().
         */
	    public Object getCheckedValue() {                        
            if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on. There is no direct equivalent, use another method to track values like this.");
	        return checked == null ? "" : checked.getValue();
	    }

        /**
         * Sets the checked state of the RadioButton that has a 'value' property equal to checkedValue.
         * @param checkedValue
         * @throws IllegalStateException if compat mode is not on
         * @deprecated there is no direct equivalent, use another method to track values like this.
         */
	    public void setCheckedValue(Object checkedValue) {
            if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on. There is no direct equivalent, use another method to track values like this.");
            
            if(checkedValue == null || (checkedValue instanceof String && ((String)checkedValue).trim().length() == 0)) {
                for (RadioButton rb : l) {
                    if (rb.isChecked()) {
                        rb.setChecked(false);
                        break;
                    }
                }
            }
            else {
                for (RadioButton rb : l) {
    	            if (rb.getValue().equals(checkedValue)) {
    	                rb.setChecked(true);
    	                break;
    	            }
                }
            }
	    }
        //#ENDIF
	}

    public static final String PROPERTY_GROUP = "group";
    
	private boolean checked;
    //#IFDEF V1_1_COMPAT
	private Object value = "";
    //#ENDIF
	private Group group;	

	/**
	 * Constructs a RadioButton with no text.
	 */
	public RadioButton() {
	    
	}
	
	/**
	 * Constructs a RadioButton with the specified text.
	 * @param text the text
	 */
	public RadioButton(String text) {
	    setText(text);
	}
    
    /**
     * Constructs a new RadioButton with the specified text and initial checked state.
     * @param text the text to display on the right side of the RadioButton.
     * @param checked the initial checked state
     */
    public RadioButton(String text, boolean checked) {
        setText(text);
        setChecked(checked);
    }  
    
    /**
     * Constructs a new RadioButton with the specified text, false checked state and group.
     * @param group the RadioButton.Group for the RadioButton
     * @param text the text to display on the right side of the RadioButton.
     * @throws IllegalArgumentException if <code>group</code> is null
     */
    public RadioButton(RadioButton.Group group, String text) {
        this(group, text, false);
    }    
    
    /**
     * Constructs a new RadioButton with the specified text, initial checked state and group.
     * @param group the RadioButton.Group for the RadioButton
     * @param text the text to display on the right side of the RadioButton.
     * @param checked the initial checked state
     * @throws IllegalArgumentException if <code>group</code> is null
     */
    public RadioButton(RadioButton.Group group, String text, boolean checked) {
        if (group == null) throw new IllegalArgumentException("group == null");
        setText(text);
        setChecked(checked);
        group.add(this);
    }    

    /**
     * Constructs a new RadioButton with the specified text, false checked state and group.
     * @param sibling a RadioButton that has a group this RadioButton should become a member of.  
     * @param text the text to display on the right side of the RadioButton.
     * @throws IllegalArgumentException if <code>sibling</code> is null
     */
    public RadioButton(RadioButton sibling, String text) {
        this(sibling, text, false);
    }
    
    /**
     * Constructs a new RadioButton with the specified text, initial checked state and group.
     * NOTE: If the specified <code>sibling</code> is not a member of a <code>RadioButton.Group</code>
     *       then a RadioButton.Group will be constructed and both the <code>sibling</code> and <code>this</code>
     *       RadioButton will be added to it.
     * @param sibling a RadioButton that has a group this RadioButton should become a member of.  
     * @param text the text to display on the right side of the RadioButton.
     * @param checked the initial checked state
     * @throws IllegalArgumentException if <code>sibling</code> is null
     */
    public RadioButton(RadioButton sibling, String text, boolean checked) {
        if (sibling == null) throw new IllegalArgumentException("sibling == null");
        setText(text);
        setChecked(checked);
        RadioButton.Group group = sibling.getGroup();
        
        if (group == null) {
            group = new RadioButton.Group();
            group.add(sibling);
        }
        
        group.add(this);
    }    
    
	/**
	 * Returns the Group that this radio button is a part of.
	 * @return a radio button Group
	 */
	public Group getGroup() {
	    return group;
	}
	
	private void setGroup(Group group) {
        Group oldGroup = this.group;
	    this.group = group;
        firePropertyChange(this, PROPERTY_GROUP, oldGroup, group);
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
	    boolean oldChecked = this.checked;
		this.checked = checked;
		
		if (group != null) {		    
		    if (checked) {
                for (RadioButton rb : group.l) {
			        if (rb != this) rb.setChecked(false);
			    }
			    
			    group.checked = this;
		    } else if (group.checked == this)
		        group.checked = null;
		}
		
		firePropertyChange(this, PROPERTY_CHECKED, oldChecked, checked);		
	}
    //#IFDEF V1_1_COMPAT

    /**
     * Returns a user defined <code>Object</code> assigned to this <code>RadioButton</code>.
     * @return a value associated to this RadioButton
     * @throws IllegalStateException if compat mode is not on
     * @deprecated Component.setUserObject is the replacement 
     * @see Component#setUserObject(Object)
     */
	public Object getValue() {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getUserObject() instead; RadioButton no longer has a value dedicated to itself");
		return value;
	}
	
    /**
     * Assigns a user defined <code>Object</code> to this <code>RadioButton</code>.
     * @param value a value to associate to this RadioButton
     * @throws IllegalStateException if compat mode is not on
     * @deprecated Component.setUserObject is the replacement 
     * @see Component#setUserObject(Object)
     */
	public void setValue(Object value) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getUserObject() instead; RadioButton no longer has a value dedicated to itself");
	    Object oldValue = this.value;
	    value = value == null ? "" : value;
		this.value = value;
		firePropertyChange(this, "value", oldValue, value);		
	}
    //#ENDIF
}
