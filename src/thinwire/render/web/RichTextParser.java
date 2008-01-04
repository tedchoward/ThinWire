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
package thinwire.render.web;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import thinwire.ui.Application;
import thinwire.ui.style.Background;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Font;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
class RichTextParser extends DefaultHandler {
    private static final Logger log = Logger.getLogger(RichTextParser.class.getName());
    private static final Level LEVEL = Level.FINER;
    
    static final String STYLE_FONT_FAMILY = "ff";
    static final String STYLE_COLOR = "fc";
    static final String STYLE_FONT_SIZE = "fs";
    static final String STYLE_FONT_WEIGHT = "fw";
    static final String STYLE_TEXT_DECORATION = "fd";
    static final String STYLE_FONT_STYLE = "ft";
    static final String STYLE_BORDER_STYLE = "dt";
    static final String STYLE_BORDER_WIDTH = "dw";
    static final String STYLE_BORDER_COLOR = "dc";
    static final String STYLE_BORDER_IMAGE = "di";
    static final String STYLE_BACKGROUND_COLOR = "bc";
    static final String STYLE_BACKGROUND_POSITION = "bp";
    static final String STYLE_BACKGROUND_REPEAT = "br";
    static final String STYLE_BACKGROUND_IMAGE = "bi";
    
    private static final String ATTR_HREF = "r";
    private static final String ATTR_TARGET = "t";
    private static final String ATTR_SRC = "s";
    private static final String ATTR_WIDTH = "w";
    private static final String ATTR_HEIGHT = "h";
    
    private static final Map<String, Tag> TAGS;
    private static final Pattern TAG_REGEX;
    static {
    	Map<String, Tag> tags = new HashMap<String, Tag>();
        Map<String, Validator> map = new HashMap<String, Validator>();
        map.put("family", new EnumValidator(Font.Family.class, STYLE_FONT_FAMILY));
        map.put("face", new EnumValidator(Font.Family.class, STYLE_FONT_FAMILY));
        map.put("color", new EnumValidator(Color.class, STYLE_COLOR));
        map.put("size", new NumberValidator(STYLE_FONT_SIZE, 0, 128, "px"));
        map.put("bold", new BooleanValidator(STYLE_FONT_WEIGHT, "bold", "normal"));
        map.put("underline", new BooleanValidator(STYLE_TEXT_DECORATION, "underline", "none"));
        map.put("strike", new BooleanValidator(STYLE_TEXT_DECORATION, "line-through", "none"));
        map.put("italic", new BooleanValidator(STYLE_FONT_STYLE, "italic", "normal"));
        tags.put("font", new Tag(map));
    	tags.put("b", new Tag(map, "bold", "true"));
    	tags.put("i", new Tag(map, "italic", "true"));
    	tags.put("u", new Tag(map, "underline", "true"));
    	tags.put("s", new Tag(map, "strike", "true"));
    	
        map = new HashMap<String, Validator>();
        map.put("edge", null);
        tags.put("border", new Tag(newBorderSet("")));
        tags.put("border left", new Tag(newBorderSet("Left"), map));
        tags.put("border right", new Tag(newBorderSet("Right"), map));
        tags.put("border top", new Tag(newBorderSet("Top"), map));
        tags.put("border bottom", new Tag(newBorderSet("Bottom"), map));
        
        map = new HashMap<String, Validator>();
        map.put("color", new EnumValidator(Color.class, STYLE_BACKGROUND_COLOR));
        map.put("position", new EnumValidator(Background.Position.class, STYLE_BACKGROUND_POSITION));
        map.put("repeat", new EnumValidator(Background.Repeat.class, STYLE_BACKGROUND_REPEAT));
        map.put("image", new URLValidator(STYLE_BACKGROUND_IMAGE));
        tags.put("background", new Tag(map));

        map = new HashMap<String, Validator>();
        map.put("href", new URLValidator(ATTR_HREF));
        map.put("target", new TargetValidator(ATTR_TARGET));
        tags.put("a", new Tag("a", map, true));
        
        map = new HashMap<String, Validator>();
        map.put("src", new URLValidator(ATTR_SRC));
        map.put("width", new NumberValidator(ATTR_WIDTH, 0, Short.MAX_VALUE, null));
        map.put("height", new NumberValidator(ATTR_HEIGHT, 0, Short.MAX_VALUE, null));
        tags.put("img", new Tag("img", map, false));
        
        tags.put("br", new Tag("br", null, false));
        
        StringBuilder sb = new StringBuilder();
        sb.append(".*<(?:");
        
        for (String s : tags.keySet()) {
        	sb.append(s).append('|');
        }
        
        sb.setCharAt(sb.length() - 1, ')');
        sb.append(".*");
        TAGS = tags;
        TAG_REGEX = Pattern.compile(sb.toString());
    }
    
    private static Map<String, Validator> newBorderSet(String edge) {
    	Map<String, Validator> map = new HashMap<String, Validator>();
        String side = edge.length() == 0 ? "" : String.valueOf(edge.charAt(0));
        map.put("type", new EnumValidator(Border.Type.class, STYLE_BORDER_STYLE + side));
        map.put("size", new NumberValidator(STYLE_BORDER_WIDTH + side, 0, 32, "px"));
        map.put("color", new EnumValidator(Color.class, STYLE_BORDER_COLOR + side));
        return Collections.unmodifiableMap(map);
    }
    
    private static class Tag {
    	String name = "span";
    	Map<String, Validator> attrMap;
    	Map<String, Validator> attrIgnoreMap;
    	String specificAttrName;
    	String specificAttrValue;
    	boolean children = true;
    	
    	Tag(String name, Map<String, Validator> attrMap, boolean children) {
    		this.name = name;
    		this.attrMap = attrMap;
    		this.children = children;
    	}

    	Tag(Map<String, Validator> attrMap) {
    		this.attrMap = attrMap;
    	}

    	Tag(Map<String, Validator> attrMap, Map<String, Validator> attrIgnoreMap) {
    		this.attrMap = attrMap;
    		this.attrIgnoreMap = attrIgnoreMap;
    	}
    	
    	Tag(Map<String, Validator> attrMap, String specificAttrName, String specificAttrValue) {
    		this.attrMap = attrMap;
    		this.specificAttrName = specificAttrName;
    		this.specificAttrValue = specificAttrValue;
    	}
    }    

	private static class Depth {
		List<Integer[]> tracker = new ArrayList<Integer[]>();
		int size;
		
		void reset() {
			tracker.clear();
			size = 0;
		}
		
		int add() {
			if (size > 0) tracker.get(size - 1)[0]++;
			tracker.add(new Integer[]{0,0});
			++size;
			return size;
		}
		
		void remove() {
			tracker.remove(--size);
		}
		
		int children() {
			return tracker.get(size - 1)[0];
		}
		
		void setMark(int mark) {
			tracker.get(size - 1)[1] = mark;			
		}
		
		int getMark() {
			return tracker.get(size - 1)[1];
		}
		
		public String toString() {
			return "Depth{size:" + size + ",children:" + children() + ",mark:" + getMark() + "}";
		}
	}

	private static class Validator  {
        String jsPropName;
        
        Validator(String jsPropName) {
            this.jsPropName = jsPropName;
        }
        
        String getValue(String value) {
            return value;
        }
    }
    
    private static class EnumValidator extends Validator {
        Class c;
        EnumValidator(Class c, String jsPropName) {
            super(jsPropName);
            this.c = c;
        }
        
        @Override
        public String getValue(String value) {
            String objValue = value;
            
            try {
                Field f = c.getField(value.toUpperCase().replace('-', '_'));                        
                objValue = f.get(null).toString();
            } catch (NoSuchFieldException e2) {
                try {
                    Method m = c.getMethod("valueOf", String.class);
                    if (m.getReturnType() != c) throw new NoSuchMethodException("public static " + c + " valueOf(String value)");
                    objValue = m.invoke(null, objValue).toString();
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
    
    private static class BooleanValidator extends Validator {
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
    
    private static class URLValidator extends Validator {
        URLValidator(String jsPropName) {
            super(jsPropName);
        }
        
        String getValue(RichTextParser parser, String value) {
        	return parser.addResourceRef(value);
        }
    }
    
    private static class TargetValidator extends Validator {
        TargetValidator(String jsPropName) {
            super(jsPropName);
        }
        
        @Override
        public String getValue(String value) {
            if (value.charAt(0) == '_') throw new RuntimeException("value.charAt(0) == '_'");
            return value;
        }
    }
    
    private static class NumberValidator extends Validator {
        int min;
        int max;
        String units;
        
        NumberValidator(String jsPropName, int min, int max, String units) {
            super(jsPropName);
            this.min = min;
            this.max = max;
            this.units = units;
        }
        
        @Override
        String getValue(String value) {
            int size = Integer.parseInt(value);
            if (size <= min || size > max) throw new RuntimeException(jsPropName + " <= " + min + " || " + jsPropName + " > " + max);
            return units == null ? String.valueOf(size) : size + units;
        }
    }
    
    private static final Application.Local<SAXParser> INSTANCE = new Application.Local<SAXParser>() {
    	public SAXParser initialValue() {
        	try {
        		return SAXParserFactory.newInstance().newSAXParser();
        	} catch (Exception e) {
        		if (e instanceof RuntimeException) throw (RuntimeException)e;
        		throw new RuntimeException(e);
        	}
    	}
    };
    
    static boolean isRichText(String text) {
    	return text.indexOf('<') >= 0 && text.indexOf('>') > 0 & TAG_REGEX.matcher(text).matches();
    }
    
    private SAXParser parser;
    private Depth depth;
    private ComponentRenderer renderer;
    private StringBuilder sb;
    private List<String> resources;
    
    RichTextParser(ComponentRenderer cr) {
    	parser = INSTANCE.get();
    	depth = new Depth();
    	renderer = cr;
    }
    
    Object parse(String richText) {        
        try {
        	reset();
            sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<richText>").append(richText).append("</richText>");
            richText = sb.toString();
            sb.setLength(0);
        	depth.reset();
        	parser.parse(new ByteArrayInputStream(richText.getBytes("utf-8")), this);
        	if (log.isLoggable(LEVEL)) log.log(LEVEL, "RICH TEXT: " + sb.toString());
            return sb;
        } catch (Exception e) {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "unable to parse rich text:" + richText, e);
        	return richText;
        }
    }
    
    private String addResourceRef(String uri) {
    	if (resources == null) resources = new ArrayList<String>();
    	resources.add(uri);
    	return renderer.wr.ai.addResourceMapping(uri);
    }
    
    void reset() {
    	if (resources != null) {
    		for (String uri : resources) {
    			renderer.wr.ai.removeResourceMapping(uri);
    		}
    		
    		resources.clear();
    	}
    }

    @Override
	public void startDocument() throws SAXException {
		sb.append("[");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
		int attrLen = attr.getLength();
		int depthSize = depth.add();
		if (log.isLoggable(LEVEL)) log.log(LEVEL, "depth=" + depth + ",uri=" + uri + ",localName=" + localName + ",qName=" + qName + ",attributes.getLength()=" + attrLen);

		if (qName.equals("richText")) {
			if (depthSize > 1) throw new SAXException("duplicate top-level element <" + qName + ">");
		} else {
			if (depthSize == 1) throw new SAXException("top-level element <richText> not found");
			Tag tag = TAGS.get(qName);
			if (tag == null) throw new SAXException("invalid start of element <" + qName + ">");
			
			if (tag.specificAttrName != null) {
				if (attrLen != 0) throw new SAXException("attributes specified for <" + qName + ">");
				attr = new AttributesImpl();
				((AttributesImpl)attr).addAttribute("", tag.specificAttrName, tag.specificAttrName, "CDATA", tag.specificAttrValue);
				attrLen++;
			}
			
			if (attrLen == 0 && tag.attrMap != null) throw new SAXException("no attributes specified for <" + qName + ">");
			if (attrLen != 0 && tag.attrMap == null) throw new SAXException("attributes specified for <" + qName + ">");
			
			if (qName.equals("border") && attr.getValue("edge") != null) {
				if (attrLen < 2) throw new SAXException("no attributes specified for <" + qName + " edge='" + attr.getValue("edge") + "'>");
				sb.append("{t:\"").append(tag.name).append("\",s:{");
                String[] edges = attr.getValue("edge").split(",");
            
                for (String s : edges) {
                	tag = TAGS.get(qName + " " + s);
                	if (tag == null) throw new SAXException("invalid <border edge=''> attribute '" + s + "' specified");
                	writeAttributes(attr, tag.attrMap, tag.attrIgnoreMap);
                }

                //Close object
                int len = sb.length();
            	sb.setCharAt(len - 1, '}');
            	sb.append(',');
                depth.setMark(len + 1);
			} else {
				if (qName.equals("a") && attr.getValue("target") == null) {
	            	if (!(attr instanceof AttributesImpl)) attr = new AttributesImpl(attr);
	            	((AttributesImpl)attr).addAttribute("", "target", "target", "CDATA", "a" + System.identityHashCode(renderer));
				}
				
				sb.append("{t:\"").append(tag.name).append("\",").append(tag.name.equals("span") ? "s" : "a").append(":{");
				if (tag.attrMap != null) writeAttributes(attr, tag.attrMap, null);
		    	int index = sb.length() - 1;

		    	if (sb.charAt(index) == ',') {
		    		sb.setCharAt(index, '}');
		    		sb.append(',');
		        } else {
		            sb.append("},");
		        }

				depth.setMark(sb.length());
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		depth.add();
		if (log.isLoggable(LEVEL)) log.log(LEVEL, "depth=" + depth + ",characters=" + new String(ch, start, length));
		sb.append("\"").append(ch, start, length).append("\",");
		depth.remove();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {		
		if (log.isLoggable(LEVEL)) log.log(LEVEL, "depth=" + depth + ",uri=" + uri + ",localName=" + localName + ",qName=" + qName);

		if (qName.equals("richText")) {
			//if (depth.size > 1) throw new SAXException("duplicate top-level element <" + qName + ">");
		} else {
			Tag tag = TAGS.get(qName);			
			if (tag == null) throw new SAXException("invalid end element </" + qName + ">");
			int children = depth.children();
			
			if (tag.children) {
				//XXX is it an error if there were no children?
				if (children > 0) {			
					if (children > 1) {
						sb.insert(depth.getMark(), "c:[");
						sb.setCharAt(sb.length() - 1, ']');
					} else {
						sb.insert(depth.getMark(), "c:");
					}
				} else {
					//XXX Remove entire entry? Or is this an error?
				}
			} else if (children > 0) {
				throw new SAXException("no children allowed for <" + qName + "> tag");
			}
			
	    	int index = sb.length() - 1;

	    	if (sb.charAt(index) == ',') {
	    		sb.setCharAt(index, '}');
	    		sb.append(',');
	        } else {
	            sb.append("},");
	        }
		}
		
		depth.remove();
	}

	@Override
	public void endDocument() throws SAXException {
        sb.setCharAt(sb.length() - 1, ']');
	}

	private void writeAttributes(Attributes attributes, Map<String, Validator> attMap, Map<String, Validator> ignoreMap) throws SAXException {
        for (int i = 0, cnt = attributes.getLength(); i < cnt; i++) {
            String attrName = attributes.getQName(i);
            
            if (attMap.containsKey(attrName)) {
            	Validator v = attMap.get(attrName);
            	String value = attributes.getValue(i);
            	
                if (v instanceof URLValidator) {
                    value = ((URLValidator) v).getValue(this, value);
                } else {
                    value = v.getValue(value);
                }
                
                sb.append(v.jsPropName).append(":");
                sb.append("\"").append(value).append("\",");
            } else if (ignoreMap == null || !ignoreMap.containsKey(attrName)) {
                throw new SAXException("invalid attribute '" + attrName + "' specified");
            }
        }
	}
}
