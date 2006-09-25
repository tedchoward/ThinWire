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
package thinwire.ui.style;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joshua J. Gertzen
 */
public class Color {    
    private static final Map<String, Color> NAMED_COLORS = new HashMap<String, Color>();
    private static final Color[] VALUES;
    
    //Borrowed from SVG 1.0 Color Set
    public static final Color ALICEBLUE = new Color("aliceblue", 240, 248, 255);
    public static final Color ANTIQUEWHITE = new Color("antiquewhite", 250, 235, 215);
    public static final Color AQUA = new Color("aqua", 0, 255, 255);
    public static final Color AQUAMARINE = new Color("aquamarine", 127, 255, 212);
    public static final Color AZURE = new Color("azure", 240, 255, 255);
    public static final Color BEIGE = new Color("beige", 245, 245, 220);
    public static final Color BISQUE = new Color("bisque", 255, 228, 196);
    public static final Color BLACK = new Color("black", 0, 0, 0);
    public static final Color BLANCHEDALMOND = new Color("blanchedalmond", 255, 235, 205);
    public static final Color BLUE = new Color("blue", 0, 0, 255);
    public static final Color BLUEVIOLET = new Color("blueviolet", 138, 43, 226);
    public static final Color BROWN = new Color("brown", 165, 42, 42);
    public static final Color BURLYWOOD = new Color("burlywood", 222, 184, 135);
    public static final Color CADETBLUE = new Color("cadetblue", 95, 158, 160);
    public static final Color CHARTREUSE = new Color("chartreuse", 127, 255, 0);
    public static final Color CHOCOLATE = new Color("chocolate", 210, 105, 30);
    public static final Color CORAL = new Color("coral", 255, 127, 80);
    public static final Color CORNFLOWERBLUE = new Color("cornflowerblue", 100, 149, 237);
    public static final Color CORNSILK = new Color("cornsilk", 255, 248, 220);
    public static final Color CRIMSON = new Color("crimson", 220, 20, 60);
    public static final Color CYAN = new Color("cyan", 0, 255, 255);
    public static final Color DARKBLUE = new Color("darkblue", 0, 0, 139);
    public static final Color DARKCYAN = new Color("darkcyan", 0, 139, 139);
    public static final Color DARKGOLDENROD = new Color("darkgoldenrod", 184, 134, 11);
    public static final Color DARKGRAY = new Color("darkgray", 169, 169, 169);
    public static final Color DARKGREEN = new Color("darkgreen", 0, 100, 0);
    public static final Color DARKGREY = new Color("darkgrey", 169, 169, 169);
    public static final Color DARKKHAKI = new Color("darkkhaki", 189, 183, 107);
    public static final Color DARKMAGENTA = new Color("darkmagenta", 139, 0, 139);
    public static final Color DARKOLIVEGREEN = new Color("darkolivegreen", 85, 107, 47);
    public static final Color DARKORANGE = new Color("darkorange", 255, 140, 0);
    public static final Color DARKORCHID = new Color("darkorchid", 153, 50, 204);
    public static final Color DARKRED = new Color("darkred", 139, 0, 0);
    public static final Color DARKSALMON = new Color("darksalmon", 233, 150, 122);
    public static final Color DARKSEAGREEN = new Color("darkseagreen", 143, 188, 143);
    public static final Color DARKSLATEBLUE = new Color("darkslateblue", 72, 61, 139);
    public static final Color DARKSLATEGRAY = new Color("darkslategray", 47, 79, 79);
    public static final Color DARKSLATEGREY = new Color("darkslategrey", 47, 79, 79);
    public static final Color DARKTURQUOISE = new Color("darkturquoise", 0, 206, 209);
    public static final Color DARKVIOLET = new Color("darkviolet", 148, 0, 211);
    public static final Color DEEPPINK = new Color("deeppink", 255, 20, 147);
    public static final Color DEEPSKYBLUE = new Color("deepskyblue", 0, 191, 255);
    public static final Color DIMGRAY = new Color("dimgray", 105, 105, 105);
    public static final Color DIMGREY = new Color("dimgrey", 105, 105, 105);
    public static final Color DODGERBLUE = new Color("dodgerblue", 30, 144, 255);
    public static final Color FIREBRICK = new Color("firebrick", 178, 34, 34);
    public static final Color FLORALWHITE = new Color("floralwhite", 255, 250, 240);
    public static final Color FORESTGREEN = new Color("forestgreen", 34, 139, 34);
    public static final Color FUCHSIA = new Color("fuchsia", 255, 0, 255);
    public static final Color GAINSBORO = new Color("gainsboro", 220, 220, 220);
    public static final Color GHOSTWHITE = new Color("ghostwhite", 248, 248, 255);
    public static final Color GOLD = new Color("gold", 255, 215, 0);
    public static final Color GOLDENROD = new Color("goldenrod", 218, 165, 32);
    public static final Color GRAY = new Color("gray", 128, 128, 128);
    public static final Color GREY = new Color("grey", 128, 128, 128);
    public static final Color GREEN = new Color("green", 0, 128, 0);
    public static final Color GREENYELLOW = new Color("greenyellow", 173, 255, 47);
    public static final Color HONEYDEW = new Color("honeydew", 240, 255, 240);
    public static final Color HOTPINK = new Color("hotpink", 255, 105, 180);
    public static final Color INDIANRED = new Color("indianred", 205, 92, 92);
    public static final Color INDIGO = new Color("indigo", 75, 0, 130);
    public static final Color IVORY = new Color("ivory", 255, 255, 240);
    public static final Color KHAKI = new Color("khaki", 240, 230, 140);
    public static final Color LAVENDER = new Color("lavender", 230, 230, 250);
    public static final Color LAVENDERBLUSH = new Color("lavenderblush", 255, 240, 245);
    public static final Color LAWNGREEN = new Color("lawngreen", 124, 252, 0);
    public static final Color LEMONCHIFFON = new Color("lemonchiffon", 255, 250, 205);
    public static final Color LIGHTBLUE = new Color("lightblue", 173, 216, 230);
    public static final Color LIGHTCORAL = new Color("lightcoral", 240, 128, 128);
    public static final Color LIGHTCYAN = new Color("lightcyan", 224, 255, 255);
    public static final Color LIGHTGOLDENRODYELLOW = new Color("lightgoldenrodyellow", 250, 250, 210);
    public static final Color LIGHTGRAY = new Color("lightgray", 211, 211, 211);
    public static final Color LIGHTGREEN = new Color("lightgreen", 144, 238, 144);
    public static final Color LIGHTGREY = new Color("lightgrey", 211, 211, 211);            
    public static final Color LIGHTPINK = new Color("lightpink", 255, 182, 193);
    public static final Color LIGHTSALMON = new Color("lightsalmon", 255, 160, 122);
    public static final Color LIGHTSEAGREEN = new Color("lightseagreen", 32, 178, 170);
    public static final Color LIGHTSKYBLUE = new Color("lightskyblue", 135, 206, 250);
    public static final Color LIGHTSLATEGRAY = new Color("lightslategray", 119, 136, 153);
    public static final Color LIGHTSLATEGREY = new Color("lightslategrey", 119, 136, 153);
    public static final Color LIGHTSTEELBLUE = new Color("lightsteelblue", 176, 196, 222);
    public static final Color LIGHTYELLOW = new Color("lightyellow", 255, 255, 224);
    public static final Color LIME = new Color("lime", 0, 255, 0);
    public static final Color LIMEGREEN = new Color("limegreen", 50, 205, 50);
    public static final Color LINEN = new Color("linen", 250, 240, 230);
    public static final Color MAGENTA = new Color("magenta", 255, 0, 255);
    public static final Color MAROON = new Color("maroon", 128, 0, 0);
    public static final Color MEDIUMAQUAMARINE = new Color("mediumaquamarine", 102, 205, 170);
    public static final Color MEDIUMBLUE = new Color("mediumblue", 0, 0, 205);
    public static final Color MEDIUMORCHID = new Color("mediumorchid", 186, 85, 211);
    public static final Color MEDIUMPURPLE = new Color("mediumpurple", 147, 112, 219);
    public static final Color MEDIUMSEAGREEN = new Color("mediumseagreen", 60, 179, 113);
    public static final Color MEDIUMSLATEBLUE = new Color("mediumslateblue", 123, 104, 238);
    public static final Color MEDIUMSPRINGGREEN = new Color("mediumspringgreen", 0, 250, 154);
    public static final Color MEDIUMTURQUOISE = new Color("mediumturquoise", 72, 209, 204);
    public static final Color MEDIUMVIOLETRED = new Color("mediumvioletred", 199, 21, 133);
    public static final Color MIDNIGHTBLUE = new Color("midnightblue", 25, 25, 112);
    public static final Color MINTCREAM = new Color("mintcream", 245, 255, 250);
    public static final Color MISTYROSE = new Color("mistyrose", 255, 228, 225);
    public static final Color MOCCASIN = new Color("moccasin", 255, 228, 181);
    public static final Color NAVAJOWHITE = new Color("navajowhite", 255, 222, 173);
    public static final Color NAVY = new Color("navy", 0, 0, 128);
    public static final Color OLDLACE = new Color("oldlace", 253, 245, 230);
    public static final Color OLIVE = new Color("olive", 128, 128, 0);
    public static final Color OLIVEDRAB = new Color("olivedrab", 107, 142, 35);
    public static final Color ORANGE = new Color("orange", 255, 165, 0);
    public static final Color ORANGERED = new Color("orangered", 255, 69, 0);
    public static final Color ORCHID = new Color("orchid", 218, 112, 214);
    public static final Color PALEGOLDENROD = new Color("palegoldenrod", 238, 232, 170);
    public static final Color PALEGREEN = new Color("palegreen", 152, 251, 152);
    public static final Color PALETURQUOISE = new Color("paleturquoise", 175, 238, 238);
    public static final Color PALEVIOLETRED = new Color("palevioletred", 219, 112, 147);
    public static final Color PAPAYAWHIP = new Color("papayawhip", 255, 239, 213);
    public static final Color PEACHPUFF = new Color("peachpuff", 255, 218, 185);
    public static final Color PERU = new Color("peru", 205, 133, 63);
    public static final Color PINK = new Color("pink", 255, 192, 203);
    public static final Color PLUM = new Color("plum", 221, 160, 221);
    public static final Color POWDERBLUE = new Color("powderblue", 176, 224, 230);
    public static final Color PURPLE = new Color("purple", 128, 0, 128);
    public static final Color RED = new Color("red", 255, 0, 0);
    public static final Color ROSYBROWN = new Color("rosybrown", 188, 143, 143);
    public static final Color ROYALBLUE = new Color("royalblue", 65, 105, 225);
    public static final Color SADDLEBROWN = new Color("saddlebrown", 139, 69, 19);
    public static final Color SALMON = new Color("salmon", 250, 128, 114);
    public static final Color SANDYBROWN = new Color("sandybrown", 244, 164, 96);
    public static final Color SEAGREEN = new Color("seagreen", 46, 139, 87);
    public static final Color SEASHELL = new Color("seashell", 255, 245, 238);
    public static final Color SIENNA = new Color("sienna", 160, 82, 45);
    public static final Color SILVER = new Color("silver", 192, 192, 192);
    public static final Color SKYBLUE = new Color("skyblue", 135, 206, 235);
    public static final Color SLATEBLUE = new Color("slateblue", 106, 90, 205);
    public static final Color SLATEGRAY = new Color("slategray", 112, 128, 144);
    public static final Color SLATEGREY = new Color("slategrey", 112, 128, 144);
    public static final Color SNOW = new Color("snow", 255, 250, 250);
    public static final Color SPRINGGREEN = new Color("springgreen", 0, 255, 127);
    public static final Color STEELBLUE = new Color("steelblue", 70, 130, 180);
    public static final Color TAN = new Color("tan", 210, 180, 140);
    public static final Color TEAL = new Color("teal", 0, 128, 128);
    public static final Color THISTLE = new Color("thistle", 216, 191, 216);
    public static final Color TOMATO = new Color("tomato", 255, 99, 71);
    public static final Color TURQUOISE = new Color("turquoise", 64, 224, 208);
    public static final Color VIOLET = new Color("violet", 238, 130, 238);
    public static final Color WHEAT = new Color("wheat", 245, 222, 179);
    public static final Color WHITE = new Color("white", 255, 255, 255);
    public static final Color WHITESMOKE = new Color("whitesmoke", 245, 245, 245);
    public static final Color YELLOW = new Color("yellow", 255, 255, 0);
    public static final Color YELLOWGREEN = new Color("yellowgreen", 154, 205, 50);
    
    //Borrowed from CSS 2.0 System Color Set
    public static final Color ACTIVEBORDER = new Color("activeborder", -1, -1, -1);
    public static final Color ACTIVECAPTION = new Color("activecaption", -1, -1, -1);
    public static final Color APPWORKSPACE = new Color("appworkspace", -1, -1, -1);
    public static final Color BACKGROUND = new Color("background", -1, -1, -1);
    public static final Color BUTTONFACE = new Color("buttonface", -1, -1, -1);
    public static final Color BUTTONHIGHLIGHT = new Color("buttonhighlight", -1, -1, -1);
    public static final Color BUTTONSHADOW = new Color("buttonshadow", -1, -1, -1);
    public static final Color BUTTONTEXT = new Color("buttontext", -1, -1, -1);
    public static final Color CAPTIONTEXT = new Color("captiontext", -1, -1, -1);
    public static final Color GRAYTEXT = new Color("graytext", -1, -1, -1);
    public static final Color HIGHLIGHT = new Color("highlight", -1, -1, -1);
    public static final Color HIGHLIGHTTEXT = new Color("highlighttext", -1, -1, -1);
    public static final Color INACTIVEBORDER = new Color("inactiveborder", -1, -1, -1);
    public static final Color INACTIVECAPTION = new Color("inactivecaption", -1, -1, -1);
    public static final Color INACTIVECAPTIONTEXT = new Color("inactivecaptiontext", -1, -1, -1);
    public static final Color INFOBACKGROUND = new Color("infobackground", -1, -1, -1);
    public static final Color INFOTEXT = new Color("infotext", -1, -1, -1);
    public static final Color MENU = new Color("menu", -1, -1, -1);
    public static final Color MENUTEXT = new Color("menutext", -1, -1, -1);
    public static final Color SCROLLBAR = new Color("scrollbar", -1, -1, -1);
    public static final Color THREEDDARKSHADOW = new Color("threeddarkshadow", -1, -1, -1);            
    public static final Color THREEDFACE = new Color("threedface", -1, -1, -1);
    public static final Color THREEDHIGHLIGHT = new Color("threedhighlight", -1, -1, -1);
    public static final Color THREEDLIGHTSHADOW = new Color("threedlightshadow", -1, -1, -1);
    public static final Color THREEDSHADOW = new Color("threedshadow", -1, -1, -1);
    public static final Color WINDOW = new Color("window", -1, -1, -1);
    public static final Color WINDOWFRAME = new Color("windowframe", -1, -1, -1);
    public static final Color WINDOWTEXT = new Color("windowtext", -1, -1, -1);
    public static final Color TRANSPARENT = new Color("transparent", -1, -1, -1);        
    static {
        VALUES = NAMED_COLORS.values().toArray(new Color[NAMED_COLORS.size()]);
        Arrays.sort(VALUES, new Comparator<Color>() {
            public int compare(Color c1, Color c2) {
                int o1 = c1.ordinal();
                int o2 = c2.ordinal();
                
                if (o1 == o2) {
                    return 0;
                } else if (o1 > o2) {
                    return 1;
                } else {
                    return -1;
                }
            }            
        });
    }    
    
    private static final Pattern REGEX_RGB = Pattern.compile("rgb[(]\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*[)]"); 
    private static final Pattern REGEX_HEX = Pattern.compile("#([a-zA-Z0-9]{2})([a-zA-Z0-9]{2})([a-zA-Z0-9]{2})"); 
    private static int nextOrdinal = 0;
    
    public static final Color valueOf(String colorId) {
        if (colorId.startsWith("rgb(")) {
            Matcher m = REGEX_RGB.matcher(colorId);

            if (m.find()) {
                int red = Integer.parseInt(m.group(1));
                int green = Integer.parseInt(m.group(2));
                int blue = Integer.parseInt(m.group(3));
                if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255)
                    throw new IllegalArgumentException("red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255 : " + colorId);

                for (Color c : VALUES) {
                    if (c.red == red && c.green == green && c.blue == blue) return c;
                }
                
                return new Color(null, red, green, blue);
            } else {
                throw new IllegalArgumentException("colorId '" + colorId + "' has an unrecognized rgb format");
            }
        } else if (colorId.startsWith("#")) {
            Matcher m = REGEX_HEX.matcher(colorId);

            if (m.find()) {
                int red = Integer.parseInt(m.group(1), 16);
                int green = Integer.parseInt(m.group(2), 16);
                int blue = Integer.parseInt(m.group(3), 16);
                if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255)
                    throw new IllegalArgumentException("red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255 : " + colorId);
                
                for (Color c : VALUES) {
                    if (c.red == red && c.green == green && c.blue == blue) return c;
                }
                
                return new Color(null, red, green, blue);
            } else {
                throw new IllegalArgumentException("colorId '" + colorId + "' has an unrecognized hex format");
            }
        } else {
            Color named = NAMED_COLORS.get(colorId);
            if (named == null) throw new IllegalArgumentException("specified named color is unknown");
            return named;
        }
    }
    
    public static final Color[] values() {
        Color[] values = new Color[VALUES.length];
        System.arraycopy(VALUES, 0, values, 0, VALUES.length);
        return values;
    }
    
    private int ordinal;
    private String name;
    private String rgbString;
    private String hexString;
    private int red;
    private int green;
    private int blue;
    
    private Color(String name, int red, int green, int blue) {                
        if (name != null && name.length() > 0) {
            this.ordinal = nextOrdinal++;            
            
            if (red >=0 && green >= 0 && blue >= 0) {
                this.red = red;
                this.green = green;
                this.blue = blue;
                this.name = name;
                rgbString = "rgb(" + this.red + "," + this.green + "," + this.blue + ")";
                hexString = "#" + Integer.toString(this.red, 16) + Integer.toString(this.green, 16) + Integer.toString(this.blue, 16);                    
            } else {
                this.red = this.green = this.blue = -1;
                this.name = rgbString = hexString = name;
            }
            
            NAMED_COLORS.put(name, this);
        } else if (red >= 0 && green >= 0 && blue >= 0) {
            this.ordinal = -1;
            this.name = "";
            this.red = red;
            this.green = green;
            this.blue = blue;                    
            rgbString = "rgb(" + this.red + "," + this.green + "," + this.blue + ")";
            hexString = "#" + Integer.toString(this.red, 16) + Integer.toString(this.green, 16) + Integer.toString(this.blue, 16);
        } else {
            throw new IllegalArgumentException("either a name, RGB pair or Hex number must be specified");
        }                
    }

    public int getRed() {
        return this.red;
    }
    
    public int getGreen() {
        return this.green;
    }
    
    public int getBlue() {
        return this.blue;
    }
    
    public String name() {
        return name.toUpperCase();
    }
    
    public int ordinal() {        
        return ordinal;
    }
    
    public String toRGBString() {
        return rgbString;
    }

    public String toHexString() {
        return hexString;
    }
    
    public boolean isSystemColor() {
        return red == -1 || green == -1 || blue == -1;
    }
    
    public int hashCode() {
        return rgbString.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Color)) {
            return false;
        } else {
            Color c = (Color)o;
            return this.red == c.red && this.green == c.green && this.blue == c.blue && this.name.equals(c.name);
        }
    }
    
    public String toString() {
        if (name.length() > 0) {
            return name;
        } else {
            return toRGBString();
        }
    }
}