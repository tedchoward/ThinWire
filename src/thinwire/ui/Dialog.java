/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * A Dialog is a window with a title that is usually associated to a Frame.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Dialog-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Dialog Test&quot;);
 * dlg.setBounds(25, 25, 600, 400);
 * 
 * final TextField tBox = new TextField();
 * tBox.setBounds(25, 25, 150, 20);
 * dlg.getChildren().add(tBox);
 * 
 * final Button btn1 = new Button(&quot;Add Text&quot;);
 * btn1.setBounds(200, 20, 100, 30);
 * dlg.getChildren().add(btn1);
 * 
 * final Button btn2 = new Button(&quot;Numeric Mask&quot;);
 * btn2.setBounds(310, 20, 100, 30);
 * dlg.getChildren().add(btn2);
 * 
 * final Button btn3 = new Button(&quot;Right Align&quot;);
 * btn3.setBounds(420, 20, 100, 30);
 * dlg.getChildren().add(btn3);
 * 
 * ActionListener clickListener = new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         Button btn = (Button) e.getSource();
 * 
 *         if (btn == btn1) {
 *             tBox.setText(tBox.getText() + &quot;913JQP-&quot;);
 *         } else if (btn == btn2) {
 *             tBox.setEditMask(&quot;#########&quot;);
 *         } else if (btn == btn3) {
 *             tBox.setAlignX(AlignX.RIGHT);
 *         }
 *     }
 * };
 * 
 * btn1.addActionListener(Button.ACTION_CLICK, clickListener);
 * btn2.addActionListener(Button.ACTION_CLICK, clickListener);
 * btn3.addActionListener(Button.ACTION_CLICK, clickListener);
 * 
 * Divider dv = new Divider();
 * dv.setBounds(25, 70, 550, 5);
 * dlg.getChildren().add(dv);
 * 
 * final TextArea ta = new TextArea();
 * ta.setBounds(25, 100, 350, 200);
 * dlg.getChildren().add(ta);
 * 
 * Button btn4 = new Button(&quot;Add Text&quot;);
 * btn4.setBounds(420, 100, 100, 30);
 * dlg.getChildren().add(btn4);
 * btn4.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         ta.setText(ta.getText() + &quot;913JQP-&quot;);
 *     }
 * });
 * 
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * <p>
 * @author Joshua J. Gertzen
 */
public class Dialog extends AbstractWindow {
    public static final String PROPERTY_RESIZE_ALLOWED = "resizeAllowed";
    
    private static final int TITLE_BAR_HEIGHT = 18 + 1;
        
    private boolean resizeAllowed;
    
    /**
     * Constructs a new Dialog with no title. 
     */
    public Dialog() {
        this(null);
    }
    
    /**
     * Constructs a new Dialog with the specified title.
     * @param title the title to display.
     */
    public Dialog(String title) {        
        setTitle(title);
        setWidth(320);
        setHeight(240);        
    }
        
    public boolean isResizeAllowed() {
        return resizeAllowed;
    }

    public void setResizeAllowed(boolean resizeAllowed) {
        boolean oldResizeAllowed = this.resizeAllowed;
        this.resizeAllowed = resizeAllowed;
        firePropertyChange(this, PROPERTY_RESIZE_ALLOWED, oldResizeAllowed, this.resizeAllowed);
    }
    
    public int getInnerHeight() {        
        return getHeight() - CALC_BORDER_PADDING_SUB - TITLE_BAR_HEIGHT - (getMenu() == null ? 0 : MENU_BAR_HEIGHT);
    }
    
    public int getInnerWidth() {        
        return getWidth() - CALC_BORDER_PADDING_SUB;
    }        
}
