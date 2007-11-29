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
public interface MaskEditorComponent extends EditorComponent {
    public static final String PROPERTY_EDIT_MASK = "editMask";
    public static final String PROPERTY_FORMAT_TEXT = "formatText";
    
    /**
     * Get the edit mask for this Component.
     * @return the edit mask
     */
    public String getEditMask();

    /**
     * This method accepts an edit mask as a String and applies it to the text field. 
     * 
     * Allowed mask chars are:
     * - "A" alphabetic characters, converted automatically to uppercase (regex: [A-Z ]|[\u00c0-\u00d6]|[\u00d8-\u00de]). 
	 * - "a" alphabetic characters (regex: [A-Za-z ]|[\u00c0-\u00d6]|[\u00d8-\u00f6]|[\u00f8-\u00ff]). 
	 * - "X" all typographic characters, with alphabetic characters converted automatically to uppercase (regex: [\u0020-`]|[{-~]|[\u00a1-\u00de]|\u00f7). 
	 * - "x" all typographic characters (regex: [ -~]|[\u00a1-\u00ff]). 
	 * - "9" numbers. Values 0-9. 
     * - "#" numbers. Values 0-9, special behavior for monetary amounts or decimal values.
     * ---- "." is treated as decimal sign and "-" on the first position as negative sign. 
	 * - "M" month. Use "MM" for values 01-12. 
	 * - "d" day. Use "dd" for values 01-31, depending on month. 
	 * - "y" year. Use "yyyy" for values 1800-2200 and "yy" for values 00-99. 
	 * - "h" hour. Use "hh" for values 00-23. 
	 * - "m" minute. Use "mm" for values 00-59.
	 * - "p" AM/PM identifier, limits the "hh" mask to 01-12 when present, uppercase and lowercase allowed.
	 *  
	 * All other characters are handled as literals and are not substituted. 
	 * The edit mask is processed on the client side without server calls. 
	 * LOCALIZATION NOTES:
	 * - No support yet for a decimal sign other than ".", such as ","
	 * - No support yet for a decimal separator other than ",", such as "."
	 * - Placing a '+' character at the beginning of a phone editMask does not work correctly.
	 * - Date masking (MM/dd/yyyy) doesn't work 100% right when reordering it to (dd/MM/yyyy). 
     * - Nothing outside of UTF-8 latin characters are validated for 'x', 'X', 'a' or 'A'.
     * - Nothing outside of UTF-8 latin alphabetic characters are auto uppercased for 'X' and 'A'. 
	 * 
	 * @param editMask the edit mask string that should be applied to this editor.
     */
    public void setEditMask(String editMask);

    /**
     * Determines whether the text returned by getText() is formatted.
     * @return true if the text is formatted, false otherwise.  Default is true.
     */
    public boolean isFormatText();
    
    /**
     * Sets whether the text returned by getText() is formatted.
     * If an editMask is specified that contains with format charcters, such as ###,###,###.## and
     * this property is set to false, then the value returned by getText will not contain the the
     * commas from the editMask.  i.e. If the value in the field is 123,456.78 then 123456.78 would
     * be returned.  Whereas, if this property is set to true then 123,456.78 would be returned.
     * Another example would be with a mask of MM/dd/yyyy, value of 11/21/1978.  With this set to false
     * you would get the value 11211978 by calling getText(), with it set to true, you'd get 11/21/1978.
     * @param formatText true if you want the text formattted, false otherwise.  Default is true.
     */
    public void setFormatText(boolean formatText);
}