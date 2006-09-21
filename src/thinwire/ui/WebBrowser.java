/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;

/**
 * A <code>WebBrowser</code> inserts a web browser object in your application.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/WebBrowser-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;WebBrowser Test&quot;);
 * dlg.setBounds(20, 20, 640, 480);
 * 
 * WebBrowser wb = new WebBrowser();
 * wb.setBounds(25, 25, 590, 430);
 * wb.setLocation(&quot;http://www.thinwire.com&quot;);
 * dlg.getChildren().add(wb);
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
// TODO Nice to haves on the client side Iframe - Back/Forward/Stop/Refresh.
// i.e. Browser like functionality.
public final class WebBrowser extends AbstractComponent {
    public static final String PROPERTY_LOCATION = "location";
    
    //NOTE: Not sure how many of these settings actually have an effect since we use an IFRAME to render the WebBrowser
    //control.  For instance the background color will mean nothing.
    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        s.getBackground().setColor(Color.WINDOW);
        Border b = s.getBorder();
        b.setSize(2);
        b.setType(Border.Type.INSET);
        b.setColor(Color.THREEDFACE);
        setDefaultStyle(Tree.class, s);
    }    
    
    private String location = "";
 
    public WebBrowser() {}
    
    public WebBrowser(String location) {
        this.setLocation(location);
    }
    
    public void setLocation(String location) {
        String oldLocation = this.location;
        location = location == null ? "" : location;
        this.location = location;
        firePropertyChange(this, PROPERTY_LOCATION, oldLocation, location);        
    }
    
    public String getLocation() {
        return this.location;
    }
}
