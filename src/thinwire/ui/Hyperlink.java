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

import thinwire.render.web.WebApplication;

/**
 * A <code>Hyperlink</code> is a screen component that acts like a standard
 * hyperlink.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Hyperlink-1.png"> <br>
 * 
 * <pre>
 * Hyperlink hl = new Hyperlink(&quot;Custom Credit Systems Home Page&quot;,
 *         &quot;http://www.customcreditsystems.com&quot;);
 * hl.setBounds(25, 25, 175, 20);
 * MessageBox.confirm(&quot;&quot;, &quot;Hyperlink Test&quot;, hl, &quot;OK&quot;);
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
 * <td>Enter</td>
 * <td>Fires Action( Action = Hyperlink.ACTION_CLICK )</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public class Hyperlink extends AbstractTextComponent {    
    private static ThreadLocal<Integer> targetId = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };
    
    public static final String PROPERTY_LOCATION = "location";

    public static void openLocation(String location) {
        openLocation(location, null);
    }
    
    public static void openLocation(String location, String target) {
        if (target == null || target.length() < 1) {
            Integer id = targetId.get();
            targetId.set(id + 1);
            target = "olhl" + id;
        }
        
        ((WebApplication)WebApplication.current()).clientSideMethodCall("tw_Hyperlink", "openLocation", location, target);
    }
    
    private String location = "";
        
    public Hyperlink() {}
    
    public Hyperlink(String text) {
        this(text, null);
    }

    public Hyperlink(String text, String location) {
        this.setText(text);
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
