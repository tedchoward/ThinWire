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
package thinwire.render.web;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import thinwire.ui.style.Background;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Font;

class RichTextParser {
    
    private static final Pattern BOLD_TAG_REGEX = Pattern.compile("(.*?)<b>(.+)</b>(.*)");
    private static final String BOLD_TAG_REPLACEMENT = "$1<font bold=\"true\">$2</font>$3";
    private static final Pattern ITALIC_TAG_REGEX = Pattern.compile("(.*?)<i>(.+)</i>(.*)");
    private static final String ITALIC_TAG_REPLACEMENT = "$1<font italic=\"true\">$2</font>$3";
    private static final Pattern UNDERLINE_TAG_REGEX = Pattern.compile("(.*?)<u>(.+)</u>(.*)");
    private static final String UNDERLINE_TAG_REPLACEMENT = "$1<font underline=\"true\">$2</font>$3";
    private static final Pattern STRIKE_TAG_REGEX = Pattern.compile("(.*?)<s>(.+)</s>(.*)");
    private static final String STRIKE_TAG_REPLACEMENT = "$1<font strike=\"true\">$2</font>$3";
    
    private final Logger log = Logger.getLogger(RichTextParser.class.getName());
    private final Map<String, Validator> fontAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> borderAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> borderLeftAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> borderRightAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> borderTopAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> borderBottomAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> borderIgnoreAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> anchorAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> imgAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> imgStyleAtts = new HashMap<String, Validator>();
    private final Map<String, Validator> backgroundAtts = new HashMap<String, Validator>();
    
    private StringBuilder sb;
    private Document document;
    
    private class Validator  {
        String jsPropName;
        
        Validator(String jsPropName) {
            this.jsPropName = jsPropName;
        }
        
        Object getValue(String value) {
            return value;
        }
    }
    
    private class EnumValidator extends Validator {
        Class c;
        EnumValidator(Class c, String jsPropName) {
            super(jsPropName);
            this.c = c;
        }
        
        @Override
        public Object getValue(String value) {
            Object objValue = value;
            try {
                Field f = c.getField(value.toUpperCase().replace('-', '_'));                        
                objValue = f.get(null);
            } catch (NoSuchFieldException e2) {
                try {
                    Method m = c.getMethod("valueOf", String.class);
                    if (m.getReturnType() != c) throw new NoSuchMethodException("public static " + c + " valueOf(String value)");
                    objValue = m.invoke(null, objValue);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);                
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } catch (IllegalAccessException e2) {
                throw new RuntimeException(e2);
            }
            return objValue;
        }
    }
    
    private class BooleanValidator extends Validator {
        String trueValueName;
        String falseValueName;
        
        BooleanValidator(String jsPropName, String trueValueName, String falseValueName) {
            super(jsPropName);
            this.trueValueName = trueValueName;
            this.falseValueName = falseValueName;
        }
        
        @Override
        public String getValue(String value) {
            return Boolean.valueOf(value) ? trueValueName : falseValueName;
        }
    }
    
    private class URLValidator extends Validator {
        
        URLValidator(String jsPropName) {
            super(jsPropName);
        }
        
        String getValue(String value, ComponentRenderer cr) {
            return cr.getQualifiedURL(value);
        }

    }
    
    private class TargetValidator extends Validator {
        TargetValidator() {
            super("target");
        }
        
        @Override
        public String getValue(String value) {
            if (value.charAt(0) == '_') throw new RuntimeException("value.charAt(0) == '_'");
            return value;
        }
    }
    
    private class NumberValidator extends Validator {
        int min;
        int max;
        
        NumberValidator(String jsPropName, int min, int max) {
            super(jsPropName);
            this.min = min;
            this.max = max;
        }
        
        @Override
        String getValue(String value) {
            int size = Integer.parseInt(value);
            if (size <= min || size > max) throw new RuntimeException(jsPropName + " <= " + min + " || " + jsPropName + " > " + max);
            return String.valueOf(size) + "px";
        }
    }
    
    RichTextParser() {
        Validator fontFamilyValidator = new EnumValidator(Font.Family.class, "fontFamily");
        fontAtts.put("family", fontFamilyValidator);
        fontAtts.put("face", fontFamilyValidator);
        fontAtts.put("color", new EnumValidator(Color.class, "color"));
        fontAtts.put("size", new NumberValidator("fontSize", 0, 128));
        fontAtts.put("bold", new BooleanValidator("fontWeight", "bold", "normal"));
        fontAtts.put("underline", new BooleanValidator("textDecoration", "underline", "none"));
        fontAtts.put("strike", new BooleanValidator("textDecoration", "line-through", "none"));
        fontAtts.put("italic", new BooleanValidator("fontStyle", "italic", "normal"));
        
        borderAtts.put("type", new EnumValidator(Border.Type.class, "borderStyle"));
        borderAtts.put("size", new NumberValidator("borderWidth", 0, 32));
        borderAtts.put("color", new EnumValidator(Color.class, "borderColor"));
        
        borderLeftAtts.put("type", new EnumValidator(Border.Type.class, "borderLeftStyle"));
        borderLeftAtts.put("size", new NumberValidator("borderLeftWidth", 0, 32));
        borderLeftAtts.put("color", new EnumValidator(Color.class, "borderLeftColor"));
        
        borderRightAtts.put("type", new EnumValidator(Border.Type.class, "borderRightStyle"));
        borderRightAtts.put("size", new NumberValidator("borderRightWidth", 0, 32));
        borderRightAtts.put("color", new EnumValidator(Color.class, "borderRightColor"));
        
        borderTopAtts.put("type", new EnumValidator(Border.Type.class, "borderTopStyle"));
        borderTopAtts.put("size", new NumberValidator("borderTopWidth", 0, 32));
        borderTopAtts.put("color", new EnumValidator(Color.class, "borderTopColor"));
        
        borderBottomAtts.put("type", new EnumValidator(Border.Type.class, "borderBottomStyle"));
        borderBottomAtts.put("size", new NumberValidator("borderBottomWidth", 0, 32));
        borderBottomAtts.put("color", new EnumValidator(Color.class, "borderBottomColor"));
        
        borderIgnoreAtts.put("edge", null);
        
        backgroundAtts.put("color", new EnumValidator(Color.class, "backgroundColor"));
        backgroundAtts.put("position", new EnumValidator(Background.Position.class, "backgroundPosition"));
        backgroundAtts.put("repeat", new EnumValidator(Background.Repeat.class, "backgroundRepeat"));
        backgroundAtts.put("image", new URLValidator("backgroundImage"));
        
        anchorAtts.put("href", new URLValidator("href"));
        anchorAtts.put("target", new TargetValidator());
        
        imgAtts.put("src", new URLValidator("src"));
        
        imgStyleAtts.put("width", new NumberValidator("width", 0, Short.MAX_VALUE));
        imgStyleAtts.put("height", new NumberValidator("height", 0, Short.MAX_VALUE));
    }
    
    public Object parseRichText(Object textValue, ComponentRenderer cr) {
        if (cr instanceof EditorComponentRenderer) return textValue;
        if (textValue == null) return "";
        String richText = textValue.toString();
        
        if (richText.indexOf('<') >= 0) {
            try {
                String tmpText = richText;
                tmpText = BOLD_TAG_REGEX.matcher(tmpText).replaceAll(BOLD_TAG_REPLACEMENT);
                tmpText = ITALIC_TAG_REGEX.matcher(tmpText).replaceAll(ITALIC_TAG_REPLACEMENT);
                tmpText = UNDERLINE_TAG_REGEX.matcher(tmpText).replaceAll(UNDERLINE_TAG_REPLACEMENT);
                tmpText = STRIKE_TAG_REGEX.matcher(tmpText).replaceAll(STRIKE_TAG_REPLACEMENT);
                sb = new StringBuilder();
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = db.parse(new ByteArrayInputStream(("<richText>" + tmpText + "</richText>").getBytes()));
                sb.append("[");
                processNode(document.getFirstChild(), cr);
                sb.setCharAt(sb.length() - 1, ']');
                log.fine("RICH TEXT: " + sb.toString());
                return sb;
            } catch (Exception e) {
                if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "Exception Caught While Parsing Rich Text", e);
                return richText;
            }
        } else {
            return richText;
        }
    }
    
    void processNode(Node curNode, ComponentRenderer cr) {
        if (curNode instanceof Text) {
            sb.append("\"").append(curNode.getNodeValue()).append("\",");
        } else if (curNode instanceof Element) {
            String name = curNode.getNodeName();
            if (name.equals("richText")) {
                if (curNode.hasChildNodes()) {
                    NodeList childNodes = curNode.getChildNodes();
                    for (int i = 0, cnt = childNodes.getLength(); i < cnt; i++) {
                        processNode(childNodes.item(i), cr);
                    }
                }
            } else if (name.equals("font")) {
                sb.append("{t:\"span\",");
                processAttributes(curNode, "s", fontAtts, null, cr);
                processChildren(curNode, cr);
                closeObject();
            } else if (name.equals("border")) {
                sb.append("{t:\"span\",");
                if (curNode.hasAttributes()) {
                    Node edgeNode = curNode.getAttributes().getNamedItem("edge");
                    if (edgeNode != null) {
                        String[] edges = edgeNode.getNodeValue().split(",");
                        boolean first = true;
                        for (String curEdge : edges) {
                            if (curEdge.equals("left")) {
                                processAttributes(curNode, first ? "s" : null, borderLeftAtts, borderIgnoreAtts, cr);
                            } else if (curEdge.equals("right")) {
                                processAttributes(curNode, first ? "s" : null, borderRightAtts, borderIgnoreAtts, cr);
                            } else if (curEdge.equals("top")) {
                                processAttributes(curNode, first ? "s" : null, borderTopAtts, borderIgnoreAtts, cr);
                            } else if (curEdge.equals("bottom")) {
                                processAttributes(curNode, first ? "s" : null, borderBottomAtts, borderIgnoreAtts, cr);
                            } else {
                                throw new RuntimeException("Invalid Border Edge Specified");
                            }
                            first = false;
                        }
                    } else {
                        processAttributes(curNode, "s", borderAtts, null, cr);
                    }
                }
                processChildren(curNode, cr);
                closeObject();
            } else if (name.equals("a")) {
                sb.append("{t:\"a\",");
                if (curNode.hasAttributes()) {
                    Node target = curNode.getAttributes().getNamedItem("target");
                    if (target == null) {
                        target = document.createAttribute("target");
                        target.setNodeValue("a" + System.identityHashCode(cr));
                        curNode.getAttributes().setNamedItem(target);
                    }
                }
                processAttributes(curNode, "a", anchorAtts, null, cr);
                processChildren(curNode, cr);
                closeObject();
            } else if (name.equals("img")) {
                sb.append("{t:\"img\",");
                processAttributes(curNode, "s", imgStyleAtts, imgAtts, cr);
                processAttributes(curNode, "a", imgAtts, imgStyleAtts, cr);
                closeObject();
            } else if (name.equals("background")) {
                sb.append("{t:\"span\",");
                processAttributes(curNode, "s", backgroundAtts, null, cr);
                processChildren(curNode, cr);
                closeObject();
            } else if (name.equals("br") && cr instanceof LabelRenderer) {
                sb.append("{t:\"br\",");
                closeObject();
            } else {
                throw new RuntimeException("Invalid tag: " + name);
            }
        }
        
    }
    
    private void closeObject() {
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.insert(sb.length() - 1, "}");
        } else {
            sb.append("},");
        }
    }
    
    private void processChildren(Node curNode, ComponentRenderer cr) {
        if (curNode.hasChildNodes()) {
            sb.append("c:");
            NodeList childNodes = curNode.getChildNodes();
            
            if (childNodes.getLength() == 1 && childNodes.item(0) instanceof Text) {
                processNode(childNodes.item(0), cr);
            } else {
                sb.append("[");
                for (int i = 0, cnt = childNodes.getLength(); i < cnt; i++) {
                    processNode(childNodes.item(i), cr);
                }
                int sbLength = sb.length();
                if (sb.charAt(sbLength - 1) == ',') {
                    sb.replace(sbLength - 1, sbLength, "]");
                } else {
                    sb.append("]");
                }
            }
        }
    }
  
    private void processAttributes(Node curNode, String attrSet, Map<String, Validator> attMap, Map<String, Validator> ignoreMap, ComponentRenderer cr) {
        if (attrSet != null) {
            sb.append(attrSet).append(":{");
        } else {
            sb.deleteCharAt(sb.lastIndexOf("}"));
        }
        NamedNodeMap attributes = curNode.getAttributes();
        for (int i = 0, cnt = attributes.getLength(); i < cnt; i++) {
            Node attr = (Attr) attributes.item(i);
            String attrName = attr.getNodeName();
            
            if (attMap.containsKey(attrName)) {
                Validator v = attMap.get(attrName);
                Object value;
                if (v instanceof URLValidator) {
                    value = ((URLValidator) v).getValue(attr.getNodeValue(), cr);
                } else {
                    value = v.getValue(attr.getNodeValue());
                }
                sb.append(v.jsPropName).append(":");
                sb.append("\"").append(value).append("\",");
            } else if (ignoreMap == null || !ignoreMap.containsKey(attrName)) {
                throw new RuntimeException("Invalid Attribute Specified: '" + attrName + "' on tag: " + curNode.getNodeName());
            }
        }
        
        closeObject();
    }
}
