/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
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
public class RadioButton extends AbstractTextComponent<RadioButton> implements CheckedComponent {
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
    	
    	public final void addActionListener(String action, ActionListener listener) {
    	    for (RadioButton rb : l) {
                rb.addActionListener(action, listener);
            }    		
   		
    	}
    	
    	public final void addActionListener(String[] actions, ActionListener listener) {
    	    for (RadioButton rb : l) {
                rb.addActionListener(actions, listener);
            }    		
    	}
    	
    	public final void removeActionListener(ActionListener listener) {
    	    if (listener == null) return;

    	    for (RadioButton rb : l) {
                rb.removeActionListener(listener);
            }    		
    	}
    	
    	public final void fireAction(ActionEvent ev) {    		
    	    for (RadioButton rb : l) {
                rb.fireAction(ev);
            }    		
    	}
    	
    	public final void fireAction(String action) {
    	    for (RadioButton rb : l) {
                rb.fireAction(action);
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
	}

    public static final String PROPERTY_GROUP = "group";
    
	private boolean checked;
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
}
