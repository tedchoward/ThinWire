/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Frame is the browser window. It is created by the Application instance and
 * automatically toggled to visible. When visible is set to false, the session
 * is closed.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Frame-1.png"> <br>
 * 
 * <pre>
 * Frame frm = Application.current().getFrame();
 * frm.setTitle(&quot;Frame Test&quot;);
 * 
 * for (int i = 0; i &lt; 5; i++) {
 * 	Dialog dlg = new Dialog();
 * 	dlg.setBounds((i * 100) + 10, 10, 90, 300);
 * 	dlg.setTitle(&quot;Dlg-&quot; + (i + 1) + &quot;-&quot; + dlg.getX() + &quot;-&quot; + dlg.getY());
 * 	Label lb = new Label(&quot;#&quot; + (i + 1));
 * 	lb.setBounds(10, 10, 50, 30);
 * 	dlg.getChildren().add(lb);
 * 	frm.getChildren().add(dlg);
 * }
 * 
 * </pre>
 * 
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class Frame extends AbstractWindow<Frame> {
    private List<Dialog> children = new ArrayList<Dialog>();
    private List<Dialog> roChildren = Collections.unmodifiableList(children);
    private int innerHeight;
    private int innerWidth;
    private boolean allowSizeChange;
    
    //Prevent the creation of any other Frame.
    Frame() {
        
    }
    
    /**
     * Returns a read-only list of all visible child dialogs.
     * @return A read-only list of all visible child dialogs.
     */
    public List<Dialog> getDialogs() {
        return roChildren;
    }
    
    void dialogVisibilityChanged(Dialog d, boolean visible) {
        if (visible) {
            children.add(d);
        } else {
            children.remove(d);
        }
    }
    
    public int getInnerWidth() {
        return innerWidth;
    }
     
    void setInnerWidth(int innerWidth) {
        if (innerWidth < 0 || innerWidth > 65536) innerWidth = 0;
        this.innerWidth = innerWidth;
    }
    
    public int getInnerHeight() {        
        return innerHeight - (getMenu() == null ? 0 : MENU_BAR_HEIGHT);
    }    

    void setInnerHeight(int innerHeight) {
        if (innerHeight < 0 || innerHeight > 65536) innerHeight = 0;
        this.innerHeight = innerHeight;
    }    
    
    void sizeChanged(int width, int height) {
        try {
            allowSizeChange = true;
            if (width < 0) width = 0;
            if (height < 0) height = 0;
            setSize(width, height);
        } finally {
            allowSizeChange = false;
        }
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public int getX() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, true));        
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void setX(int x) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, false));
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public int getY() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, true));        
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void setY(int y) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, false));
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void setWidth(int width) {
        if (allowSizeChange) {
            super.setWidth(width);
        } else {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_WIDTH, false));
        }
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void setHeight(int height) {
        if (allowSizeChange) {
            super.setHeight(height);
        } else {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_HEIGHT, false));
        }
    }
    
    /**
	 * Although tecnhically visible is false for a Window by default, the
	 * Application instance automatically sets visible to true when the app is
	 * started. Setting visible to false will cause the app instance to close
	 * and terminate the session.
	 * 
	 * @see thinwire.ui.Component#setVisible(boolean)
	 */
    public void setVisible(boolean visible) {
        if (isVisible() != visible) {
            if (visible) {
                app.showWindow(this);
            } else {
                List<Dialog> dialogs = getDialogs();
                
                for (Dialog d : dialogs.toArray(new Dialog[dialogs.size()])) {                    
                    d.setVisible(false);
                }
                
                app.hideWindow(this);
            }

            super.setVisible(visible);
        }
    }
}