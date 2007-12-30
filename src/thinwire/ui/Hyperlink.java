/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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

import java.io.File;

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
public class Hyperlink extends AbstractLabelComponent {    
    private static Application.Local<Integer> targetId = new Application.Local<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };
    
    private static String getNextGeneratedTarget() {
    	String target;
        Integer id = targetId.get();
        targetId.set(id + 1);
        target = "hl" + id;
    	return target;
    }
    
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_VISIBLE_CHROME = "visibleChrome";
    public static final String PROPERTY_RESIZE_ALLOWED = "resizeAllowed";

    public static void openLocation(String location) {
        openLocation(location, null, false, false);
    }

    public static void openLocation(String location, String target) {
    	openLocation(location, null, false, false);
    }
    
    public static void openLocation(String location, String target, boolean visibleChrome) {
    	openLocation(location, null, visibleChrome, false);
    }

    public static void openLocation(String location, String target, boolean visibleChrome, boolean resizeAllowed) {
        if (target == null || target.length() < 1) target = getNextGeneratedTarget();
        location = Application.current().getQualifiedURL(location);
        
        if (location.startsWith(WebApplication.REMOTE_FILE_PREFIX)) {
        	((WebApplication)WebApplication.current()).clientSideMethodCallWaitForReturn("tw_Hyperlink", "openLocation", location, target, visibleChrome, resizeAllowed);
	        Application.current().removeFileFromMap(location);
        } else {
        	((WebApplication)WebApplication.current()).clientSideMethodCall("tw_Hyperlink", "openLocation", location, target, visibleChrome, resizeAllowed);
        }
    }

    private String location = "";
    private String target = "";
    private boolean visibleChrome;
    private boolean resizeAllowed;
        
    public Hyperlink() {
    	this(null, null, null, true, true);
    }       

    public Hyperlink(String text) {
        this(text, Application.validateURL(text) ? text : null, null, true, true);
    }

    public Hyperlink(String text, String location) {
    	this(text, location, null, true, true);
    }
    
    public Hyperlink(String text, String location, String target) {
    	this(text, location, target, true, true);
    }
    
    public Hyperlink(String text, String location, String target, boolean visibleChrome) {
    	this(text, location, target, visibleChrome, true);
    }

    public Hyperlink(String text, String location, String target, boolean visibleChrome, boolean resizeAllowed) {
        setText(text);
        setLocation(location);
        setTarget(target);
        this.visibleChrome = visibleChrome;
        this.resizeAllowed = resizeAllowed;
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

	public void setTarget(String target) {
		String oldTarget = this.target;
		target = target == null ? getNextGeneratedTarget() : target;
		this.target = target;
		firePropertyChange(this, PROPERTY_TARGET, oldTarget, target);
	}

	public String getTarget() {
		return target;
	}

	public void setVisibleChrome(boolean visibleChrome) {
		boolean oldVisibleChrome = this.visibleChrome;
		this.visibleChrome = visibleChrome;
		firePropertyChange(this, PROPERTY_VISIBLE_CHROME, oldVisibleChrome, visibleChrome);
	}

	public boolean isVisibleChrome() {
		return visibleChrome;
	}

	public void setResizeAllowed(boolean resizeAllowed) {
		boolean oldResizeAllowed = this.resizeAllowed;
		this.resizeAllowed = resizeAllowed;
		firePropertyChange(this, PROPERTY_RESIZE_ALLOWED, oldResizeAllowed, resizeAllowed);
	}

	public boolean isResizeAllowed() {
		return resizeAllowed;
	}
}
