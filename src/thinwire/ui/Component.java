/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.event.KeyPressListener;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.Style;

/**
 * <code>Component</code> is the foundation of all visual objects in the framework. A visual object is one that
 * offers a user interface that can be displayed and interacted with by a user.  The common
 * capabilities of all visual objects are defined by this object.  This includes methods for setting
 * and getting common spatial and focus properites as well as support for adding event listeners
 * that receive notification of property and keypress state changes.
 * <p>
 * Since Component is an interface, you do not actually create an instance of it directly, instead you create an instance of
 * one of its sub-classes, such as <code>Button</code>, <code>TextField</code> or <code>Label</code>.
 * </p>
 * <p>
 * <b>Keyboard Navigation:</b><br>
 * <table border="1">
 *     <tr><td>KEY</td><td>RESPONSE</td><td>NOTE</td></tr>
 *     <tr><td>Tab</td><td>Transitions to the next focus capable <code>Component</code></td><td>See {@link #setFocus(boolean)} for details.</td></tr>
 *     <tr><td>Shift-Tab</td><td>Transitions to the prior focus capable <code>Component</code></td><td>See {@link #setFocus(boolean)} for details.</td></tr>
 * </table> 
 * </p>
 * @author Joshua J. Gertzen
 */
public interface Component {
    /**
     * Contains the formal property name for the 'X' coordinate of the component.
     * @see #setX(int)
     * @see #getX()
     */    
    public static final String PROPERTY_X = "x";
    
    /**
     * Contains the formal property name for the 'Y' coordinate of the component.
     * @see #setY(int)
     * @see #getY()
     */
    public static final String PROPERTY_Y = "y";
    
    /**
     * Contains the formal property name for the width of the component.
     * @see #setWidth(int)
     * @see #getWidth()
     */
    public static final String PROPERTY_WIDTH = "width";
    
    /**
     * Contains the formal property name for the height of the component.
     * @see #setHeight(int)
     * @see #getHeight()
     */
    public static final String PROPERTY_HEIGHT = "height";
    
    /**
     * Contains the formal property name for the visible state of the component.
     * @see #setVisible(boolean)
     * @see #isVisible()
     */
    public static final String PROPERTY_VISIBLE = "visible";
    
    /**
     * Contains the formal property name for the enabled state of the component.
     * @see #setEnabled(boolean)
     * @see #isEnabled()
     */
    public static final String PROPERTY_ENABLED = "enabled";
    
    /**
     * Contains the formal property name for the focus capability of the component.
     * @see #setFocusCapable(boolean)
     * @see #isFocusCapable()
     */
    public static final String PROPERTY_FOCUS_CAPABLE = "focusCapable";
    
    /**
     * Contains the formal property name for the focus state of the component.
     * @see #setFocus(boolean)
     * @see #isFocus()
     */
    public static final String PROPERTY_FOCUS = "focus";
    
    /**
     * Contains the formal property name for the user object of the component.
     * @see #setUserObject(Object)
     * @see #getUserObject()
     */
    public static final String PROPERTY_USER_OBJECT = "userObject";

    /**
     * Adds a <code>PropertyChangeListener</code> that will be notified when the specified property changes. Adding a property
     * listener to a component allows your code to react to a state change within the component. <br>
     * <b>Example:</b>
     * 
     * <pre>
     * final TextField tf = new TextField();
     * tf.setEnabled(false);
     * 
     * CheckBox cb = new CheckBox(&quot;Check me to enable the TextField.&quot;);
     * cb.addPropertyChangeListener(CheckBox.PROPERTY_CHECKED, new PropertyChangeListener() {
     *     public void propertyChange(PropertyChangeEvent pce) {
     *         if (pce.getNewValue() == Boolean.TRUE) {
     *             tf.setEnabled(true);
     *         } else {
     *             tf.setEnabled(false);
     *         }
     *     }
     * });
     * </pre>
     * 
     * @param propertyName the name of the property that the listener will receive change events for.
     * @param listener the listener that will receive <code>PropertyChangeEvent</code> objects upon the property changing.
     * @throws IllegalArgumentException if <code>listener</code> or <code>propertyName</code> is null or if
     *         <code>propertyName</code> is an empty string.
     * @see thinwire.ui.event.PropertyChangeListener
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Adds a <code>PropertyChangeListener</code> to the component that will be notified when any of the specified properties
     * change. This method is equivalent to calling {@link #addPropertyChangeListener(String, PropertyChangeListener)} once
     * for each property you want to listen to.
     * @param propertyNames a string array of property names that the listener will receive change events for.
     * @param listener the listerner that will receive <code>PropertyChangeEvent</code> objects anytime one of the specified
     *        propertyNames of this component change.
     * @throws IllegalArgumentException if <code>listener</code>, <code>propertyNames</code> or any property name is the array
     *         is null or if any property name is an empty string.
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     * @see thinwire.ui.event.PropertyChangeListener
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void addPropertyChangeListener(String[] propertyNames, PropertyChangeListener listener);

    /**
     * Removes the specified <code>PropertyChangeListener</code> from the component. If the listener was added for multiple
     * properties, it will be removed for all of them. NOTE: An exception is NOT thrown if you attempt to remove a listener that
     * does not exist on this component.
     * @param listener the listener to remove from the component.
     * @throws IllegalArgumentException if <code>listener</code> is null.
     * @see thinwire.ui.event.PropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Adds a <code>KeyPressListener</code> that will be notified when the specified key press combination occurs.
     * <p>
     * For a description and list of valid <code>keyPressCombo</code> strings, see the documentation for
     * {@link thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)}.
     * </p>
     * <p>
     * Establishing keyboard shortcuts for certain features can be a highly effective way to improve the efficiency of your
     * application. If your application has a <code>Menu</code>, then typically the best way to establish such shortcuts is to
     * simply set the <code>keyPressCombo</code> property for each <code>Menu.Item</code>. Second to that, using this method to
     * establish shortcuts on the <code>Frame</code> or a <code>Dialog</code> will have a similar wide reaching effect.
     * Occasionally, based on the requirements of your application, you may also use this method to establish shortcuts that are
     * only valid when a given component has focus.
     * </p>
     * <b>Details:</b>
     * <p>
     * When a user presses a key and/or combination, the event bubbles up the component hierarchy from the component that currently
     * has focus and is absorbed by the first <code>Component</code> that has a listener asking to be notified of that event.
     * Therefore, if both a <code>Component</code> and a <code>Container</code> up the hierarchy are listening for the same
     * event, only the <code>Component</code> will receive notification. There is currently no way to cause the event to continue
     * bubbling.
     * </p>
     * <p>
     * Additionally, the keyboard navigation of each <code>Component</code> cannot be overridden and you cannot receive
     * notification of such events. As an example, establishing a <code>KeyPressListener</code> for "Space" key on the
     * <code>CheckBox</code>, will have no effect because the "Space" key toggles the checked state of that component.
     * </p>
     * <p>
     * NOTE ON WEBBROWSERS: If no <code>Component</code> is listening for a given key press, then the default behavior that the
     * browser has associated with that key press will occur. Additionally, certain key press events in certain browsers cannot be
     * entirely circumvented. In such a case, both the action defined by a listener and the browser's default behavior will occur.
     * An example of this is the F1 key in Internet Explorer. If you establish a listener for the F1 key, the IE help file will open
     * in addition to whatever action you may have defined.
     * </p>
     * <b>Example:</b>
     * 
     * <pre>
     * Application.current().getFrame().addKeyPressListener(&quot;Ctrl-Alt-M&quot;, new KeyPressListener() {
     *     public void keyPress(KeyPressEvent kpe) {
     *         MessageBox.confirm(&quot;You pressed the following key combination: &quot; + kpe.getKeyPressCombo());
     *     }
     * });
     * </pre>
     * 
     * @param keyPressCombo a key press combo in any dash separated format supported by
     *        {@link thinwire.ui.event.KeyPressEvent#normalizeKeyPressCombo(String)}.
     * @param listener the listener that will receive <code>KeyPressEvent</code> objects upon the key press occurring.
     * @throws IllegalArgumentException if <code>listener</code> or <code>keyPressCombo</code> is null, or if
     *         <code>keyPressCombo</code> is an empty string, or if <code>keyPressCombo</code> represents an invalid key combo.
     * @see thinwire.ui.event.KeyPressListener
     * @see thinwire.ui.event.KeyPressEvent
     * @see thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)
     * @see thinwire.ui.event.KeyPressEvent#normalizeKeyPressCombo(String)
     */
    void addKeyPressListener(String keyPressCombo, KeyPressListener listener);

    /**
     * Adds a <code>KeyPressListener</code> that will be notified when any of the specified key press combinations occur.
     * <p>
     * For a description and list of valid <code>keyPressCombo</code> strings, see the documentation for
     * {@link thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)}.
     * </p>
     * <p>
     * See {@link #addKeyPressListener(String, KeyPressListener)} for a full semantic description.
     * </p> 
     * @param keyPressCombos a string array of key press combos, each in any dash separated format supported by
     *        {@link thinwire.ui.event.KeyPressEvent#normalizeKeyPressCombo(String)}.
     * @param listener the listener that will receive <code>KeyPressEvent</code> objects when any of the key presses occur.
     * @throws IllegalArgumentException if <code>listener</code> or any key press combo in <code>keyPressCombos</code> is null,
     *         or if any key press combo in <code>keyPressCombos</code> is an empty string, or if any key press combo in
     *         <code>keyPressCombos</code> represents an invalid key combo.
     * @see #addKeyPressListener(String, KeyPressListener)
     * @see thinwire.ui.event.KeyPressListener
     * @see thinwire.ui.event.KeyPressEvent
     * @see thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)
     * @see thinwire.ui.event.KeyPressEvent#normalizeKeyPressCombo(String)
     */
    void addKeyPressListener(String[] keyPressCombos, KeyPressListener listener);

    /**
     * Removes the specified <code>KeyPressListener</code> from the component. If the listener was added for multiple
     * key press combinations, it will be removed for all of them. NOTE: An exception is NOT thrown if you attempt to remove a listener that
     * does not exist on this component.
     * @param listener the listener to remove from the component.
     * @throws IllegalArgumentException if <code>listener</code> is null.
     * @see thinwire.ui.event.KeyPressListener
     */
    void removeKeyPressListener(KeyPressListener listener);

    /**
     * Allows you to programmatically trigger a key press combination. Passing this method a valid key press combination will result
     * in a <code>KeyPressEvent</code> being generated. As a result, all <code>KeyPressListener</code>'s that are registered on
     * the specified <code>keyPressCombo</code> will be notified. <br>
     * <p>
     * For a description and list of valid <code>keyPressCombo</code> strings, see the documentation for
     * {@link thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)}.
     * </p>
     * <b>Details:</b>
     * <p>
     * A <code>KeyPressEvent</code> that is generated programmatically via this mechansim may, under some circumstances, have a
     * slightly different behavior than one generated by user activity. The reason for this is that the event is only propagated
     * within the framework itself and does not actually occur in the client. In general, this should never be an issue because the
     * desired response to a keypress will be expressly defined by a given <code>KeyPressListener</code> and therefore there would be
     * no dependence on any such side-effect. However, an example of one such
     * difference, is in terms of a browser's default behavior for a specific key press combination. If you use this mechanism
     * to trigger an F1 keypress, the browser's default behavior (typically bringing up a help window), will not occur.
     * </p>
     * @param keyPressCombo a key press combo in any dash separated format supported by
     *        {@link thinwire.ui.event.KeyPressEvent#normalizeKeyPressCombo(String)}.
     * @throws IllegalArgumentException if <code>keyPressCombo</code> is null, or if <code>keyPressCombo</code> is an empty
     *         string, or if <code>keyPressCombo</code> represents an invalid key combo.
     * @see thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)
     * @see thinwire.ui.event.KeyPressEvent#normalizeKeyPressCombo(String)
     */
    void fireKeyPress(String keyPressCombo);

    /**
     * Returns the parent <code>Object</code> of this <code>Component</code>. If you specifically need the parent
     * <code>Container</code> of this <code>Component</code> use {@link #getContainer()} instead.
     * <br>
     * <b>Details:</b>
     * <p>
     * Under the majority of situations, the returned value is either a <code>Container</code> or <code>null</code> since a
     * <code>Component</code> will either be a child of a <code>Container</code> or not attached to any object. However, in some
     * cases the parent of the <code>Component</code> may be another <code>Component</code>, or a completely different kind of
     * <code>Object</code>. For example, in the case of the <code>DropDownGridBox</code>, there is an actual
     * <code>GridBox</code> that is a child of the drop down. Therefore, the parent of that <code>GridBox</code> would be the
     * <code>DropDownGridBox</code>. Another situation exists when you use a multi-tiered <code>GridBox</code>, meaning a
     * <code>GridBox</code> that has one or more "pop-up" child <code>GridBox</code>'s. Under that scenario, the parent of the
     * child's <code>GridBox</code> is actually an instance of <code>GridBox.Row</code> and the parent of the row is the
     * <code>GridBox</code>.
     * </p>
     * @return the parent <code>Object</code> of this <code>Component</code>, or <code>null</code> if no parent exists.
     * @see #getContainer()
     */
    Object getParent();

    /**
     * Returns the parent <code>Container</code> of this <code>Component</code>. Unlike <code>getParent()</code>, this
     * method guarantees that if a non-null value is returned, it will be a <code>Contaienr</code>.
     * @return the parent <code>Container</code> of this <code>Component</code>, or <code>null</code> if no parent exists.
     * @throws IllegalStateException if in the process of walking up the parent hierarchy, an unrecognized parent type is found.
     * @see #getParent()
     */
    Container getContainer();

    /**
     * Returns the <code>Label</code> assigned to this <code>Component</code>. This property is part of a two-way relationship
     * that is established by the {@link thinwire.ui.Label#setLabelFor(Component)} property. There is no <code>setLabel</code>
     * method, instead use {@link thinwire.ui.Label#setLabelFor(Component)}.
     * @return the <code>Label</code> assigned to this <code>Component</code>.
     */
    Label getLabel();

    /**
     * Returns the user defined <code>Object</code> for this <code>Component</code>.
     * <br>
     * <b>Default:</b> null
     * @return the user defined <code>Object</code> for this <code>Component</code>, or null if no value has been specified.
     * @see #setUserObject(Object)
     */
    Object getUserObject();

    /**
     * Assigns a user defined <code>Object</code> to this <code>Component</code>. This property has no direct effect
     * on the state of the <code>Component</code>.  Instead, it provides a general purpose storage
     * mechanism to the developer that allows any kind of data to be associated to this <code>Component</code>.
     * For complex applications, alternate methods of associating state to a <code>Component</code> will likely
     * serve your design more thoroughly.  However, there are a number of cases where this flexibility could be useful
     * and therefore the framework supports the concept.
     * <br>
     * <b>Default:</b> null
     * <p>
     * Refer to the documenation on {@link Application#addGlobalPropertyChangeListener(String, PropertyChangeListener)} for an
     * example of a potential use of this property.
     * </p>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_USER_OBJECT ) to be generated.
     * </p>
     * @param userObject an <code>Object</code> of any type that is to be associated with this <code>Component</code>.
     * @see #getUserObject()
     * @see Application#addGlobalPropertyChangeListener(String, PropertyChangeListener)
     * @see #PROPERTY_USER_OBJECT
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setUserObject(Object userObject);

    /**
     * Returns whether this <code>Component</code> is enabled and therefore supports user interaction.
     * <br>
     * <b>Default:</b> true
     * @return true if the <code>Component</code> supports user interaction, false otherwise.
     * @see #setEnabled(boolean)
     */
    boolean isEnabled();

    /**
     * Assigns whether this <code>Component</code> is enabled and therefore supports user interaction.
     * The form of user iteraction this property controls, depends on the specific kind of <code>Component</code>
     * itself.  However, in general, all keyboard interaction and mouse interaction is disabled by
     * setting this property to false.
     * <br>
     * <b>Default:</b> true
     * <br>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_ENABLED ) to be generated.
     * </p>
     * @param enabled true to allow user interaction, false to disallow it.
     * @see #isEnabled()
     * @see #PROPERTY_ENABLED
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setEnabled(boolean enabled);

    /**
     * Returns whether this <code>Component</code> supports gaining focus. <br>
     * <b>Default:</b> true, except for <code>Divider</code>, <code>Image</code> and <code>Label</code>.
     * @return true if this <code>Component</code> supports gaining focus, false otherwise.
     * @see #setFocusCapable(boolean)
     * @see #setFocus(boolean)
     */
    boolean isFocusCapable();

    /**
     * Assigns whether this <code>Component</code> supports gaining focus. <br>
     * <b>Default:</b> true, except for <code>Divider</code>, <code>Image</code> and <code>Label</code>. <br>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_FOCUS_CAPABLE ) to be generated.
     * </p>
     * @param focusCapable true to allow this component to receive focus, false to disallow it.
     * @see #isFocusCapable()
     * @see #PROPERTY_FOCUS_CAPABLE
     * @see #setFocus(boolean)
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setFocusCapable(boolean focusCapable);

    /**
     * Returns whether this <code>Component</code> has the input focus. If this is a <code>Container</code>, then this method
     * will return true if a child <code>Component</code> has the focus. In such a case, you can use the
     * {@link thinwire.ui.Container#getChildWithFocus()} method to get a reference to that child. Similarly, if you want to find the
     * child <code>Component</code> that has the focus anywhere in the current <code>Frame</code> or <code>Dialog</code>, you
     * can use the {@link thinwire.ui.Container#getComponentWithFocus()} method.
     * <p>
     * <b>Default:</b> false. However, at rendering time, if no component in the window has focus, the first focus capable
     * component is given focus.
     * </p> 
     * <p>
     * See the {@link #setFocus(boolean)} method for a full description of focus details. 
     * </p>
     * @return true if this <code>Component</code> has the input focus, false otherwise.
     * @see #setFocus(boolean)
     * @see thinwire.ui.Container#getComponentWithFocus()
     * @see thinwire.ui.Container#getChildWithFocus()
     */
    boolean isFocus();

    /**
     * Assigns whether this <code>Component</code> has the input focus.  When this <code>Component</code>
     * has the input focus, it will receive all keyboard events generated by the user.  Therefore,
     * if this <code>Component</code> supports text editing and it has focus, the user can type a value
     * into it's field.  Additionally, any keyboard navigation supported by this <code>Component</code>
     * or keyboard shortcuts added by a developer become available upon gaining focus.  Conversely, when this <code>Component</code> no longer has
     * focus, it will receive no keyboard events.  
     * <p>
     * <b>Default:</b> false. However, at rendering time, if no component in the window has focus, the first focus capable
     * component is given focus.
     * </p> 
     * <b>Details:</b>
     * <p>
     * The simplest of all cases, is when this <code>Component</code> has not yet been added to a <code>Container</code>.
     * In that scenario, the focus property is simply set to true and no other effect occurs.  Later, when this
     * <code>Component</code> is added to a <code>Container</code> it will be given the focus according to the guidelines
     * that follow.
     * </p>
     * <p>
     * As a general rule,
     * only a single <code>Component</code> can have the focus per <code>Frame</code> or <code>Dialog</code>
     * container hierarchy.  In terms of the user interface, only a single <code>Component</code> will actually
     * have the focus regardless of whether a <code>Dialog</code> and the <code>Frame</code> have components with focus.
     * In such a case, the actual focus is determined based on which window is currently active.
     * </p>
     * <p>
     * Since only one <code>Component</code> per window can have focus, giving this <code>Component</code> focus
     * will cause the prior <code>Component</code> of the window to lose focus.  In the most common case, both this
     * <code>Component</code> and the <code>Component</code> losing focus will be siblings in the same <code>Container</code>.
     * In that case, the focus property of the <code>Component</code> losing focus is simply set to false whereas the 
     * focus property of this <code>Component</code> is set to true. 
     * </p>
     * <p>
     * More complex scenarios arise when the <code>Component</code> losing focus and this <code>Component</code> are
     * not siblings in the same <code>Container</code>.  In those cases, the order in which focus is lost and gained
     * occurs as follows:
     * </p>
     * <ul>
     *    <li>1. The highest level shared parent <code>Container</code> between both the <code>Component</code> losing focus
     *    and this <code>Component</code> is found. This shared parent and any <code>Container</code> above it in the hierarchy will be left alone.</li> 
     *    <li>2. The focus property is set to <code>false</code> for each <code>Container</code> in the hierarchy that contains the <code>Component</code> losing focus, as well as the
     *    component itself. This is done in top down order, so that the top most <code>Container</code> loses focus first, followed 
     *    by every container between it and the <code>Component</code> losing focus next, and with the component itself losing focus last.</li> 
     *    <li>3. The focus property is set to <coded>true<code> for each <code>Container</code> in the hierarchy that contains this <code>Component</code>, as well as the
     *    component itself. This is done in top down order, so that the top most <code>Container</code> gains focus first, followed 
     *    by every container between it and the <code>Component</code> gaining focus next, and with this component gaining focus last.</li> 
     * </ul>
     * <p>
     * The final case to be aware of is if you directly set this <code>Component</code>'s focus to false.  In that case,
     * the same loss of focus rules outlined above apply.  There is simply no gaining of focus that occurs by any component.
     * Therefore you cause the window to have no <code>Component</code> with focus, with the except of the parent <code>Container</code>
     * of this <code>Component</code>.
     * </p>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_FOCUS ) to be generated. Additionally,
     * similar event generation may occur for other components according to the details outlined above. 
     * </p>
     * @param focus true to give this <code>Component</code> and it's parent containers focus, false otherwise.
     * @see #isFocus()
     * @see thinwire.ui.Container#getComponentWithFocus()
     * @see thinwire.ui.Container#getChildWithFocus()
     * @throws IllegalStateException if this <code>Component</code> is not focus capable
     * @throws UnsupportedOperationException if the parent of this <code>Component</code> is not null and is not a <code>Container</code>
     */
    void setFocus(boolean focus);

    /**
     * Returns a <code>Style</code> object representing this <code>Component</code>'s current style settings. NOTE: This method
     * will never return null.
     * @return a <code>Style</code> object representing this <code>Component</code>'s current style settings.
     * @see thinwire.ui.style.Style
     */
    Style getStyle();

    /**
     * Returns the X coordinate of this <code>Component</code>.
     * @return the X coordinate (in pixels) of this <code>Component</code>
     * @see #setX(int)
     */
    int getX();

    /**
     * Assigns the specified X coordinate to this <code>Component</code>.<br>
     * <b>Default:</b> 0
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_X ) to be generated.
     * </p>
     * @param x the x coordinate (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the x value is < -32768 or >= 32767
     * @see #getX()
     * @see #setPosition(int, int)
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_X
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setX(int x);

    /**
     * Returns the Y coordinate of this <code>Component</code>.
     * @return the Y coordinate (in pixels) of this <code>Component</code>
     * @see #setY(int)
     */
    int getY();

    /**
     * Assigns the specified Y coordinate to this <code>Component</code>.<br>
     * <b>Default:</b> 0
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_Y ) to be generated.
     * </p>
     * @param y the y coordinate (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the y value is < -32768 or >= 32767
     * @see #getY()
     * @see #setPosition(int, int)
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_Y
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setY(int y);

    /**
     * Assigns the specified X and Y coordinates to this <code>Component</code> atomically, in one operation.
     * Aside from the convienence provided by this method, it also guarantees that both of the provided
     * X and Y coordinates are legal values before the values are committed.  The primary benefit of this
     * is that no <code>PropertyChangeEvent</code>'s will be generated until both values have been set.
     * <b>Events:</b>
     * <p>
     * This method may generate <code>PropertyChangeEvent</code>'s. See the documenation of <code>setX</code> and <code>setY</code> for more details.
     * </p>
     * @param x the x coordinate (in pixels) to assign to this <code>Component</code>
     * @param y the y coordinate (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the x or y value is < -32768 or >= 32767
     * @see #setX
     * @see #setY
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_X
     * @see #PROPERTY_Y
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setPosition(int x, int y);

    /**
     * Returns the width of this <code>Component</code>.
     * @return the width (in pixels) of this <code>Component</code>
     * @see #setWidth(int)
     */
    int getWidth();

    /**
     * Assigns the specified width to this <code>Component</code>.<br>
     * <b>Default:</b> 0<br>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_WIDTH ) to be generated.
     * </p>
     * @param width the width (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the width value is < 0 or >= 32767
     * @see #getWidth()
     * @see #setSize(int, int)
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_WIDTH
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setWidth(int width);

    /**
     * Returns the height of this <code>Component</code>.
     * @return the height (in pixels) of this <code>Component</code>
     * @see #setHeight(int)
     */
    int getHeight();

    /**
     * Assigns the specified height to this <code>Component</code>.<br>
     * <b>Default:</b> 0<br>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_HEIGHT ) to be generated.
     * </p>
     * @param height the height (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the height value is < 0 or >= 32767
     * @see #getHeight()
     * @see #setSize(int, int)
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_HEIGHT
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setHeight(int height);

    /**
     * Assigns the specified width and height to this <code>Component</code> atomically, in one operation.
     * Aside from the convienence provided by this method, it also guarantees that both of the provided
     * width and height are legal values before the values are committed.  The primary benefit of this
     * is that no <code>PropertyChangeEvent</code>'s will be generated until both values have been set.
     * <b>Events:</b>
     * <p>
     * This method may generate <code>PropertyChangeEvent</code>'s. See the documenation of <code>setWidth</code> and <code>setHeight</code> for more details.
     * </p>
     * @param width the width (in pixels) to assign to this <code>Component</code>
     * @param height the height (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the width or height value is < 0 or >= 32767
     * @see #setWidth
     * @see #setHeight
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_WIDTH
     * @see #PROPERTY_HEIGHT
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setSize(int width, int height);

    /**
     * Assigns the specified width, height, X and Y values to this <code>Component</code> atomically, in one operation.
     * Aside from the convienence provided by this method, it also guarantees that all of the provided
     * values are legal before they are committed.  The primary benefit of this
     * is that no <code>PropertyChangeEvent</code>'s will be generated until all values have been set.
     * <b>Events:</b>
     * <p>
     * This method may generate <code>PropertyChangeEvent</code>'s. See the documenation of <code>setX</code>, <code>setY</code>, <code>setWidth</code> and <code>setHeight</code> for more details.
     * </p>
     * @param x the x coordinate (in pixels) to assign to this <code>Component</code>
     * @param y the y coordinate (in pixels) to assign to this <code>Component</code>
     * @param width the width (in pixels) to assign to this <code>Component</code>
     * @param height the height (in pixels) to assign to this <code>Component</code>
     * @throws IllegalArgumentException if the width or height value is < 0 or >= 32767, or if the x or y value is < -32768 or >= 32767
     * @see #setX
     * @see #setY
     * @see #setWidth
     * @see #setHeight
     * @see #setBounds(int, int, int, int)
     * @see #PROPERTY_X
     * @see #PROPERTY_Y
     * @see #PROPERTY_WIDTH
     * @see #PROPERTY_HEIGHT
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setBounds(int x, int y, int width, int height);

    /**
     * Returns a boolean value indicating whether this <code>Component</code> may be displayed in a window. See
     * the documentation of {@link #setVisible(boolean)} for further details about this property.<br>
     * <b>Default:</b> true, except for the <code>Dialog</code> and <code>Frame</code> containers.
     * @return true if this <code>Component</code> may be displayed, fasle otherwise
     * @see #setVisible(boolean)
     */
    boolean isVisible();

    /**
     * Assigns a boolean value indicating whether this <code>Component</code> may be displayed in a window. <br>
     * <b>Default:</b> true, except for the <code>Dialog</code> and <code>Frame</code> containers. <br>
     * <b>Details:</b>
     * <p>
     * This <code>Component</code> will not actually be displayed unless it is visible and added to a <code>Container</code>
     * hierarchy in which all of the containers are also visible and the top-level <code>Container</code> is a visible
     * <code>Frame</code> or <code>Dialog</code>. Once a <code>Component</code> has been displayed, toggling this property
     * results in a light-weight operation that simply hides/shows this <code>Component</code>. This may sound trivial, but the
     * difference is important when you need to maximize the performance of your application. For instance, it is a faster to toggle
     * the visibility of components then it is to add/remove the components from a displayed <code>Container</code>. This is
     * because the first time a <code>Component</code> is displayed, the entire state must be rendered. In contrast, when you
     * toggle visibility, the <code>Component</code> remains in memory in a fully rendered form, it is just not visible to the
     * user.
     * </p>
     * <b>Events:</b>
     * <p>
     * If the prior value and new value differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_VISIBLE ) to be generated.
     * </p>
     * @param visible true to indicate this <code>Component</code> may be displayed, false otherwise
     * @see #isVisible()
     * @see #PROPERTY_VISIBLE
     * @see thinwire.ui.Container#getChildren()
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setVisible(boolean visible);
}