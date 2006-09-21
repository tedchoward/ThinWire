/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionListener;
import thinwire.ui.style.Style;

/**
 * A <code>Label</code> is the text that appears next to a control on a
 * screen.
 * <p>
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Label-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Label Test&quot;);
 * dlg.setBounds(25, 25, 415, 150);
 * 
 * final Label lbl = new Label(&quot;Initial 1st Label&quot;);
 * lbl.setBounds(25, 25, 150, 30);
 * 
 * Button btn = new Button(&quot;Toggle Text&quot;);
 * btn.setBounds(300, 20, 100, 30);
 * 
 * btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         if (&quot;Initial 1st Label&quot;.equals(lbl.getText())) {
 *             lbl.setText(&quot;The text has now been toggled.&quot;);
 *         } else {
 *             lbl.setText(&quot;Initial 1st Label&quot;);
 *         }
 *     }
 * });
 * 
 * dlg.getChildren().add(lbl);
 * dlg.getChildren().add(btn);
 * dlg.setVisible(true);
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
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class Label extends AbstractTextComponent implements AlignTextComponent, ActionEventComponent {
    public static final String PROPERTY_LABEL_FOR = "labelFor";
    
    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        setDefaultStyle(Label.class, s);
    }
    
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();
    private AlignX alignX = AlignX.LEFT;
    private Component labelFor = null;

    /**
     * Constructs a new Label with no text.
     */
    public Label() {
       this(null); 
    }
    
    /**
     * Constructs a new Label with the specified text.
     * @param text the text to display on the Label.
     */
    public Label(String text) {
        if (text != null) setText(text);
        setFocusCapable(false);
    }
        
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }    
		
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param action the action to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param actions the actions to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    /**
     * Removes an existing actionListener.
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }    
    
    /**
     * Programmatically signals an action which triggers the appropriate listener which calls
     * the desired method.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(action, this);
    }    
    
    /*
     *  (non-Javadoc)
     * @see thinwire.ui.TextAlignComponent#getAlignX()
     */
    public AlignX getAlignX() {
        return alignX;
    }

    /*
     *  (non-Javadoc)
     * @see thinwire.ui.TextAlignComponent#setAlignX(thinwire.ui.AlignX)
     */
    public void setAlignX(AlignX alignX) {
        if (alignX == null) throw new IllegalArgumentException(PROPERTY_ALIGN_X + " == null");
        AlignX oldAlignX = this.alignX;
        this.alignX = alignX;
        firePropertyChange(this, PROPERTY_ALIGN_X, oldAlignX, alignX);
    }    
        
    /**
     * Returns the component that this label is associated with.
     * 
     * @return the Component associated with this Label.
     */
    public Component getLabelFor() {
        return labelFor;
    }

    /**
     * This method links a label with an onscreen component.
     * 
     * @param labelFor the component to link with the label
     */
    public void setLabelFor(Component labelFor) {
        Component oldLabelFor = this.labelFor;
        this.labelFor = labelFor;
        if (labelFor != null) ((AbstractComponent)labelFor).setLabel(this);
        firePropertyChange(this, PROPERTY_LABEL_FOR, oldLabelFor, labelFor);
    }
}