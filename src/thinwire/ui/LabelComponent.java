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

/**
 * @author Joshua J. Gertzen
 */
public interface LabelComponent extends AlignTextComponent {
    public static final String PROPERTY_LABEL_FOR = "labelFor";
    public static final String PROPERTY_WRAP_TEXT = "wrapText";
    
    /**
     * Returns the component that this label is associated with.
     * 
     * @return the Component associated with this Label.
     */
    public Component getLabelFor();
    
    /**
     * This method links a label with an onscreen component.
     * 
     * @param labelFor the component to link with the label
     */
    public void setLabelFor(Component labelFor);

    /**
     * Returns true if this <code>LabelComponent</code> automatically wraps it's text when it's text exceeds it's width.
     * @return true if this <code>LabelComponent</code> automatically wraps it's text when it's text exceeds it's width.
     */
    public boolean isWrapText();
    
    /**
     * Sets whether this <code>LabelComponent</code> automatically wraps it's text when it's text exceeds it's width.
     * @param wrapText true to have this <code>LabelComponent</code> automatically wrap it's text, false otherwise.
     */
	public void setWrapText(boolean wrapText);
}
