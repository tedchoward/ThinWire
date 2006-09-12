/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
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
