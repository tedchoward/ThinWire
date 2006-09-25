/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.Arrays;
import java.util.EventObject;

/**
 * @author Joshua J. Gertzen
 */
public class KeyPressEvent extends EventObject {    
    private static final String[] specialKeyNames; 
    static {
        specialKeyNames = new String[] {
            "Dash", "=", "`", "[", "]", "\\", ";", "'", 
            ",", ".", "/", "PageUp", "PageDown",
            "End", "Home", "ArrowLeft", "ArrowRight", "ArrowDown",
            "ArrowUp", "Space", "Enter", "Esc",  "Tab", "BackSpace",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7",
            "F8", "F9", "F10", "F11", "F12", 
            "Del", "Ins", "Pause", "ScrollLock", "NumLock",
            "Num0", "Num1", "Num2", "Num3", "Num4", "Num5",
            "Num6", "Num7", "Num8", "Num9", "Num*", "Num+",
            "NumDash", "Num/", "Num."
        };
        
        Arrays.sort(specialKeyNames, String.CASE_INSENSITIVE_ORDER);
    }
    
    private static String getKeyName(String key) {
        if (key.length() == 1 && key.matches("[A-Z0-9]")) return key;
        
        if (key.length() == 1 && key.matches("[a-z]")) {
            return key.toUpperCase();
        } else {
            if (key.indexOf('_') > 0) key = key.replaceFirst("[_]", "");
            int index = Arrays.binarySearch(specialKeyNames, key, String.CASE_INSENSITIVE_ORDER);
            
            if (index >= 0) {
                return specialKeyNames[index];
            } else {
                throw new IllegalArgumentException("Invalid key format:" + key);
            }
        }
    }
        
    /**
     * Combines the individual elements of a key press combination into a normalized string. The first part of the returned string
     * contains the following key modifier string:
     * <ul>
     * <li>If <code>ctrl</code> is true, "Ctrl-" is the first part of the string.</li>
     * <li>If <code>alt</code> is true, then "Alt-" is the first part of the string.</li>
     * <li>If <code>shift</code> is true, then "Shift-" is the first part of the string.</li>
     * <li>If any combination of <code>ctrl</code>, <code>alt</code> and <code>shift</code> is true, then the first part of
     * the string will be a combination "Ctrl-", "Alt-" and "Shift-", in that order. Ex. If <code>ctrl</code> and
     * <code>shift</code> are true, but <code>alt</code> is false, the first part of the string will be "Ctrl-Shift-".
     * <li>If neither <code>ctrl</code>, <code>alt</code> or <code>shift</code> is true, then an empty value is the first
     * part of the string.</li>
     * </ul>
     * The last part of the string is a result of the value specified by the <code>key</code> argument. However, it is normalized
     * into one of the following values: <br>
     * <p>
     * "Dash", "=", "`", "[", "]", "\\", ";", "'", ",", ".", "/", "PageUp", "PageDown", "End", "Home", "ArrowLeft", "ArrowRight",
     * "ArrowDown", "ArrowUp", "Space", "Enter", "Esc", "Tab", "BackSpace", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9",
     * "F10", "F11", "F12", "Del", "Ins", "Pause", "ScrollLock", "NumLock", "Num0", "Num1", "Num2", "Num3", "Num4", "Num5", "Num6",
     * "Num7", "Num8", "Num9", "Num*", "Num+", "NumDash", "Num/", "Num."
     * </p>
     * @param ctrl if true, the returned string will contain "Ctrl-" per the rules above.
     * @param alt if true, the returned string will contain "Alt-" per the rules above.
     * @param shift if true, the returned string will contain "Shift-" per the rules above.
     * @param key returned as part of the string, only in a normalized form per the rules above.
     * @return a key press combo string representing the elements specified per the rules above.
     */
    public static String encodeKeyPressCombo(boolean ctrl, boolean alt, boolean shift, String key) {
        StringBuilder sb = new StringBuilder();
        if (ctrl) sb.append("Ctrl-");
        if (alt) sb.append("Alt-");
        if (shift) sb.append("Shift-");
        sb.append(getKeyName(key));
        return sb.toString();        
    }
    
    /**
     * Parses the specified <code>keyPressCombo</code> and return's it in a normalized form. This method will accept a string that
     * is of any case and that has it's key modifier(s) in any order. The sole restriction is that the elements of the key press
     * combination must be separated by a dash '-'. The purpose of this method is to provide a way of generating a properly formed
     * key press combination strings (i.e. normalized). The normalized string form returned by this method is defined by the
     * <code>encodeKeyPressCombo</code> method above.
     * @param keyPressCombo a key press combo in any dash separated format.
     * @return a normalized form of keyPressCombo, per the defintion in the encodeKeyPressCombo method.
     * @see #encodeKeyPressCombo(boolean, boolean, boolean, String)
     */    
    public static String normalizeKeyPressCombo(String keyPressCombo) {
        if (keyPressCombo == null || keyPressCombo.length() == 0) new IllegalArgumentException("keyPressCombo == null || keyPressCombo.length() == 0");
        boolean ctrl = false;
        boolean alt = false;
        boolean shift = false;
        String key = "";
        String[] parts = keyPressCombo.toUpperCase().split("-");
        if (parts.length > 4) throw new IllegalArgumentException("Invalid key combo:" + keyPressCombo);
        
        for (String part : parts) {
            if (part.equals("CTRL")) {
                ctrl = true;
            } else if (part.equals("ALT")) {
                alt = true;
            } else if (part.equals("SHIFT")) {
                shift = true;
            } else {
                key = getKeyName(part);
                break;
            }
        }
        
        return encodeKeyPressCombo(ctrl, alt, shift, key);
    }
        
    private boolean ctrl;
    private boolean alt;
    private boolean shift;
    private String key;
    private String keyPressCombo;
    
    public KeyPressEvent(Object source, String keyPressCombo) {
        super(source);
        this.keyPressCombo = keyPressCombo = normalizeKeyPressCombo(keyPressCombo);
        ctrl = keyPressCombo.indexOf("Ctrl-") >= 0;
        alt = keyPressCombo.indexOf("Alt-") >= 0;
        shift = keyPressCombo.indexOf("Shift-") >= 0;
        key = keyPressCombo.substring(keyPressCombo.lastIndexOf('-') + 1);
    }
    
    public boolean isCtrl() {
        return ctrl;
    }
    
    public boolean isAlt() {
        return alt;
    }
    
    public boolean isShift() {
        return shift;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getKeyPressCombo() {
        return keyPressCombo;
    }
}
