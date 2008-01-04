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
package thinwire.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import thinwire.ui.Application;

/**
 * Xml Object Document (XOD) generates objects from the XML file, and associates them with a Map.
 * XODs can be used to create instances of plain old java objects (POJO). For example, a Dialog can be
 * created as follows:<p>
 * <h3>Sample File</h3>
 * <img src="doc-files/XOD-1.png"> <p>
 * <h3>Sample File XML</h3>
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;xod&gt;
 *     &lt;include file="classes.xml"/&gt;
 *     &lt;Dialog id="dialog"&gt;
 *         &lt;title&gt;XOD Demo File&lt;/title&gt;
 *         &lt;width&gt;300&lt;/width&gt;
 *         &lt;height&gt;200&lt;/height&gt;
 *         &lt;children&gt;
 *             &lt;TextField id="name"&gt;
 *                 &lt;x&gt;90&lt;/x&gt;
 *                 &lt;y&gt;5&lt;/y&gt;
 *                 &lt;width&gt;80&lt;/width&gt;
 *                 &lt;height&gt;25&lt;/height&gt;
 *             &lt;/TextField&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Name:&lt;/text&gt;
 *                 &lt;labelFor&gt;name&lt;/labelFor&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;5&lt;/x&gt;
 *                 &lt;y&gt;5&lt;/y&gt;
 *                 &lt;width&gt;80&lt;/width&gt;
 *                 &lt;height&gt;25&lt;/height&gt;
 *             &lt;/Label&gt;
 *         &lt;/children&gt;
 *     &lt;/Dialog&gt;
 * &lt;/xod&gt;
 * </pre>
 * <h3>Sample Java Code Using XOD</h3>
 * <pre>
 * XOD xod = new XOD();
 * xod.execute("dialog.xml");  
 * Dialog dialog = (Dialog)xod.getObjectMap().get("dialog");
 * dialog.setVisible(true);
 * </pre>
 *  <h3>Sample Alias XML File</h3>
 * The &lt;alias&gt; tag associates a short name with a class name.  This content doesn't
 * have to be separate.  Instead of using the &lt;include&gt; tag in the demo file displayed
 * above, the demo file could have included it directly.<p>
 * <pre>
 * &lt;xod&gt;
 *     &lt;alias name="Button" class="thinwire.ui.Button"/&gt;
 *     &lt;alias name="CheckBox" class="thinwire.ui.CheckBox"/&gt;
 *     &lt;alias name="Dialog" class="thinwire.ui.Dialog"/&gt;
 *     &lt;alias name="Divider" class="thinwire.ui.Divider"/&gt;
 *     &lt;alias name="DropDownGridBox" class="thinwire.ui.DropDownGridBox"/&gt;
 *     &lt;alias name="Frame" class="thinwire.ui.Frame"/&gt;
 *     &lt;alias name="GridBox" class="thinwire.ui.GridBox"/&gt;
 *     &lt;alias name="Image" class="thinwire.ui.Image"/&gt;
 *     &lt;alias name="Label" class="thinwire.ui.Label"/&gt;
 *     &lt;alias name="Menu" class="thinwire.ui.Menu"/&gt;
 *     &lt;alias name="Panel" class="thinwire.ui.Panel"/&gt;
 *     &lt;alias name="RadioButton" class="thinwire.ui.RadioButton"/&gt;
 *     &lt;alias name="RadioButton.Group" class="thinwire.ui.RadioButton$Group"/&gt;
 *     &lt;alias name="TabFolder" class="thinwire.ui.TabFolder"/&gt;
 *     &lt;alias name="TabSheet" class="thinwire.ui.TabSheet"/&gt;
 *     &lt;alias name="TextArea" class="thinwire.ui.TextArea"/&gt;
 *     &lt;alias name="TextField" class="thinwire.ui.TextField"/&gt;
 * &lt;/xod&gt;
 *</pre>
 *<h3>Notes on the XOD Tags</h3>
 *<h4>&lt;xod&gt;</h4>
 *The &lt;xod&gt; tag is the top level tag.  It occurs once and encloses all other
 *tags.
 *<h4>&lt;include&gt;</h4>
 *The &lt;include&gt; tag allows you to include other XOD XML files within an XOD file.
 *Use it to reduce duplication. E.g. You can use it to include an alias file like the
 *one shown above.
 *<h4>&lt;alias&gt;</h4>
 *Many of the tags in the demo listed above can be thought of as
 *instruction to construct plain old java objects.  E.g. The &lt;Button&gt; tag can be thought of as
 *instruction to build a Button class instance.
 *<p>
 *In general, an XOD file can contain tags of the form
 *<pre>
 *  &lt;com.mypackage.XXXX&gt;
 *</pre>
 *where XXXX is a class, and such a tag can be thought of as an 
 *instruction to create an instance of the XXXX class.<p>
 *But the full class names are long.  To allow for shorter tags in your xod files, you can define an alias: <br>
 *<pre>
 *  &lt;alias name="Button" class="thinwire.ui.Button"/&gt;   
 *</pre>
 *Once you've included this alias in your file, you can construct
 *an object using it:<br>
 *<pre>
 *   &lt;Button id="button_ok"&gt;
 *        &lt;text&gt;OK&lt;/text&gt;
 *        &lt;x&gt;73&lt;/x&gt;
 *        &lt;y&gt;301&lt;/y&gt;
 *        &lt;width&gt;84&lt;/width&gt;
 *        &lt;height&gt;30&lt;/height&gt;
 *   &lt;/Button&gt;
 * </pre>
 *<h4>&lt;ref&gt;</h4>
 *Some objects need to be associated with other objects.
 *E.g. We need a means to indicate that the RadioButtons described in a
 *container are members of the RadioButton.Group.
 *We use the &lt;ref&gt; tag to accomplish this.
 *<p>
 *<h4>Object Class Tags</h4>
 *If there's a class with the name "com.mypackage.XXXX", you can include an
 *&lt;com.mypackage.XXXX&gt; element in your XOD definition.  You can also
 *create an alias for that class and use it in place of the class name. 
 *<p>
 *<h4>Component Property Tags - e.g. &lt;width&gt;, &lt;text&gt;, &lt;x&gt;</h4>
 *To specify properties - e.g. location, size, and text - for the components you wish
 *to build, add property tag elements. In general, if a UI component class has a "setXxxx" method,
 *you can include an &lt;xxxx&gt; property tag.<p>
 *E.g. Since the thinwire.ui.Button class has a setText method,
 *you can specify the text for your button with a &lt;text&gt; element.
 *
 *<pre>
 *   &lt;Button id="button_ok"&gt;
 *        ....
 *        &lt;text&gt;OK&lt;/text&gt;
 *        ....
 *   &lt;/Button&gt;
 * </pre>
 * 
 * <h4>Property Tag Values</h4>
 * The properties of objects have types. E.g.  The text property of the
 * Button class is of type String, and the x property of the Button class is of type
 * int. Other standard types are Integer, double, Double, char, Character, long, Long,
 * boolean, Boolean, float, Float, byte, and Byte.
 * <p>
 * In the case of these standard types, the XOD class will make the appropriate conversion
 * while processing a xod file. E.g. When it comes across
 * <pre>
 *   &lt;Button id="Search_btn"&gt;
 *        .....
 *        &lt;x&gt;73&lt;/x&gt;
 *        .....
 *   &lt;/Button&gt;
 * </pre>
 * the XOD engine will convert "73" to an int and assign the x property of the
 * new Button that int value.
 * <p>
 * In the case of non-literal types, XOD has three special strategies.
 * First, it will check to see if the class type of the property you are assigning
 * to has a 'valueOf' method that returns the appropriate type.  If it does, it
 * will call that method and assign the returned value to the property. 
 * E.g. The Label class has an alignX property of type thinwire.ui.AlignX.
 * This class has a 'valueOf' method.  When XOD processes the above demo file and comes across
 * <pre>
 * &lt;Label&gt;
 *     .....
 *     &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *     .....
 * &lt;/Label&gt;
 * </pre>
 * it calls the static 'valueOf' on AlignX, which returns the appropriate constant AlignX.RIGHT.
 * Second, it will check to see if the class type has a static final field (i.e. constant) that
 * is named the same as the value specified in the property tag.  If it finds a constant, it will
 * be assed to the property. 
 * Third, it looks for an object defined elsewhere in the XOD, an object
 * whose id in the XOD matches the property value. If it finds one, it assigns
 * the object as a value for the property. E.g. When it comes across
 * <pre>
 *  &lt;Label&gt;
 *      .....
 *      &lt;labelFor&gt;name&lt;/labelFor&gt;
 *      .....
 *  &lt;/Label&gt;
 * </pre>
 * then XOD engine discovers that there is a 'name' object defined previously, and it assigns
 * this object to the Label's labelFor property.<p> 
 * <p>
 * <h4>Collection Properties</h4>
 * A component may have a property with a Collection type.  E.g. Dialogs have
 * a getChildren method, and a children property.  The children of a Dialog form
 * a collection.<p>
 * When an XML element represents a property with a Collection type, the content of 
 * the element represents the members of the collection. E.g.  When XOD comes 
 * across 
 * <pre>
 *  &lt;children&gt;
 *      &lt;Label&gt;
 *          ....
 *      &lt;/Label&gt;
 *      &lt;Label&gt;
 *          ....
 *      &lt;/Label&gt;
 *      &lt;Label&gt;
 *          ....
 *      &lt;/Label&gt;
 *      &lt;Divider&gt;
 *          ....
 *      &lt;/Divider&gt;
 *      &lt;Divider&gt;
 *          ....
 *      &lt;/Divider&gt;
 *      &lt;TextField id="name"&gt;
 *          .... 
 *      &lt;/TextField&gt;
 *      ....
 *  &lt;/children&gt;
 * </pre>
 * it constructs Label, Divider, and TextField components and makes these
 * components children of the Dialog.
 *
 * @author Joshua J. Gertzen
 */
public final class XOD {
    private static final Logger log = Logger.getLogger(XOD.class.getName());
    private static final Level LEVEL = Level.FINER;
    
    private Map<String, Object> objectMap;
    private Map<Object, String> idMap;
    private List<Object> rootObjects;
    private List<Object> objects;
    private Map<String, Class> aliases;
    private Map<String, String> properties;
    private Map<Class, Map<String, Object[]>> propertyAliases;
    private List<String> uriStack;
    private boolean processingInclude;
    
    /**
     * Create a new XOD.
     */       
    public XOD() {
        this(null);
    }
    
    /**
     * Create a new XOD.
     * @param uri the name of the XML Object Document file to execute.
     */
    public XOD(String uri) {
        rootObjects = new ArrayList<Object>();
        objectMap = new HashMap<String, Object>();
        objects = new ArrayList<Object>();
        aliases = new HashMap<String, Class>();
        properties = new HashMap<String, String>();
        uriStack = new ArrayList<String>();
        if (uri != null) execute(uri);
    }
    
    public Map<String, Class> getAliasMap() {
        return aliases;
    }
    
    public Map<String, String> getPropertyMap() {
        return properties;
    }
    
    /**
     * The object map contains references to previously defined objects
     * identified by id.  Typically, it will map an ID found in the 
     * XOD to an object.
     * @return the object Map
     */
    public Map<String, Object> getObjectMap() {
        return objectMap;
    }
    
    public List<Object> getObjects() {
        return Collections.unmodifiableList(objects);
    }
    
    public List<Object> getRootObjects() {
        return Collections.unmodifiableList(rootObjects);
    }
        
    /**
     * Gets the id that is associated to the specified object.
     * For this to succeed, the object must be in the object map.
     * @param o the object to retrieve the id for.
     * @return the id associated to the object, or null if the object is not in the object map.
     */
    public String getObjectId(Object o) {
        return getIdMap().get(o);
    }
    
    private Map<Object, String> getIdMap() {
        if (idMap == null) {
            idMap = new HashMap<Object, String>();

            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                idMap.put(entry.getValue(), entry.getKey());
            }
        }
        
        return idMap;
    }
    
    /**
     * Executes a XOD file, adding the created objects to the map and list.
     * @param uri the name of the XML Object Document file.
     */
    public void execute(String uri) {
        processFile(null, uri, 0);
    }
    
    private Object processFile(Object parent, String uri, int level) {
        Object ret = null;
        
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            uri = uri.replace('\\', '/');
            InputStream is = Application.getResourceAsStream(uri);
            if (is == null) throw new IllegalArgumentException("Content for URI was not found:" + uri);
            uriStack.add(uri);
            setUriVariables(uri);
            Document doc = builder.parse(is);
            is.close();
            ret = processBranch(parent, doc.getChildNodes(), level);
        } catch (Exception e) {
            throw new RuntimeException("processing file '" + uri + "'", e);
        } finally {
            uriStack.remove(uriStack.size() - 1);
            if (uriStack.size() > 0) setUriVariables(uriStack.get(uriStack.size() - 1));
        }
        
        return ret;
    }
    
    private void setUriVariables(String uri) {
        int index = uri.lastIndexOf('/');
        if (index == -1) index = 0;
        properties.put("xod.file", uri.substring(index));
        properties.put("xod.path", index == 0 ? "" : uri.substring(0, index));
    }

    private void appendAttributes(Node n) {
        NamedNodeMap nnm = n.getAttributes();
        Document doc = n.getOwnerDocument();
        
        for (int i = 0, cnt = nnm.getLength(); i < cnt; i++) {
            Node attribute = nnm.item(i);
            Node tag = doc.createElement(attribute.getNodeName());
            tag.appendChild(doc.createTextNode(attribute.getNodeValue()));
            n.appendChild(tag);
        }
    }
    
    private Object processBranch(Object parent, NodeList nl, int level) {        
        for (int i = 0, cnt = nl.getLength(); i < cnt; i++) {
            Node n = nl.item(i);
            
            switch (n.getNodeType()) {
            	case Node.COMMENT_NODE:
            	    //Valid, but just ignored
            	    break;
            	
            	case Node.TEXT_NODE:
            	    if (n.getNodeValue().trim().length() != 0)
            	        throw new DOMException(DOMException.NO_DATA_ALLOWED_ERR, "a value for the <" + n.getNodeName() + "> tag is not allowed");
            	    break;
            	
            	case Node.ELEMENT_NODE:
            	    evaluateNode(parent, n, level);
            	    break;
            	    
            	default:
            	    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "node type " + n.getNodeType() + " is not supported");
            }
        }
        
        return null;
    }           
    
    private Object evaluateNode(Object parent, Node n, int level) {
        Object ret = null;
        String name = n.getNodeName();

        if (name.equals("xod")) {
            if (n.getChildNodes().getLength() == 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() == 0");
            if (level != 0 && !processingInclude) throw new DOMException(DOMException.INVALID_STATE_ERR, "level != 0");            
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "xod");
            processBranch(parent, n.getChildNodes(), level + 1);
        } else if (name.equals("property") && parent == null) {
            if (n.getAttributes().getLength() != 2) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 2");
            String key = (String)n.getAttributes().getNamedItem("name").getNodeValue();
            String value = (String)n.getAttributes().getNamedItem("value").getNodeValue();
            value = this.replaceProperties(value);
            properties.put(key, value);
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "property[name:" + key + ",value:" + value + "]");
        } else if (name.equals("alias")) {
            if (n.getAttributes().getLength() != 2) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 2");
            //if (n.getChildNodes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() != 0");
            String aliasName = (String)n.getAttributes().getNamedItem("name").getNodeValue();
            String className = this.replaceProperties((String)n.getAttributes().getNamedItem("class").getNodeValue());
            Class clazz = getClassForName(aliasName, className);
            if (n.hasChildNodes()) processBranch(clazz, n.getChildNodes(), level + 1);            
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "alias[name:" + aliasName + ",class:" + className + "]");
        } else if (name.equals("property") && parent instanceof Class) {
            if (n.getAttributes().getLength() != 2 && n.getAttributes().getLength() != 3) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 2 && n.getAttributes().getLength() != 3");
            String propName = this.replaceProperties((String)n.getAttributes().getNamedItem("name").getNodeValue());
            String aliasName = this.replaceProperties((String)n.getAttributes().getNamedItem("alias").getNodeValue());
            String className = this.replaceProperties((String)n.getAttributes().getNamedItem("class").getNodeValue());
            setPropertyAlias((Class)parent, propName, aliasName, getClassForName(null, className));
        } else if (name.equals("include")) {
            if (n.getAttributes().getLength() != 1) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 1");
            if (n.getChildNodes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() != 0");
            String fileUri = this.replaceProperties((String)n.getAttributes().getNamedItem("file").getNodeValue());

            //TODO This whole section is questionable going forward. Rather than walking up the directory structure, we should just 
            //require that uri's have explicit pathing relative to ${xod.path} or relative from the app folder.
            if (fileUri.indexOf("://") == -1 || fileUri.startsWith("file:///")) {
            	String newUri = fileUri;
                if (newUri.startsWith("file:///")) newUri = newUri.substring(7);
            
	            if (!Application.getRelativeFile(newUri).exists()) {
	                String curFileUri = uriStack.get(0);
	                
	                do {
	                    File parentFolder = Application.getRelativeFile(curFileUri).getParentFile();
	                    
	                    if (parentFolder == null) {
	                        newUri = null;
	                        break;
	                    }
	                    
	                    curFileUri = parentFolder.getAbsolutePath();
	                    newUri = Application.getRelativeFile(curFileUri, newUri).getAbsolutePath();
	                } while (!Application.getRelativeFile(newUri).exists());
	                
	                if (newUri != null) fileUri = newUri;
	            }
            }
            
            processingInclude = true;
            processFile(parent, fileUri, level + 1);
            processingInclude = false;
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "include[file:" + fileUri + "]");
        } else if (name.equals("ref")) {
            if (n.getAttributes().getLength() != 1) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 1");
            if (n.getChildNodes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() != 0");
            String id = (String)n.getAttributes().getNamedItem("id").getNodeValue();
            ret = objectMap.get(id);            
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "ref[id:" + id + "]");
            
            if (parent != null && parent instanceof Collection) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "adding ref child to collection");
                ((Collection)parent).add(ret);
            }
        } else {            
            boolean property = false;
            
            //If there is a parent and the tag name does not contain a period and the first character is lowerCase, then this might be a property
            if (parent != null && name.indexOf('.') == -1 && Character.isLowerCase(name.charAt(0))) {
                //if (n.getAttributes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 0");

                //Check to see if this property has an Alias.
                Object[] propertyAlias = getPropertyAlias(parent.getClass(), name);                
                if (propertyAlias != null)
                    name = (String)propertyAlias[0];
                
                //Search for a set method for the property
                String setter = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                String getter = "get" + setter;
                String istter = "is" + setter;
                setter = "set" + setter;
                
                Method setMethod = null;
                Method getMethod = null;

                for (Method m : parent.getClass().getMethods()) {
                    if (setMethod != null && getMethod != null) break; 
                    String methodName = m.getName();
                    
                    if (methodName.equals(setter)) {
                        setMethod = m;
                    } else if (methodName.equals(getter) || methodName.equals(istter)) {
                        getMethod = m;
                    }
                }
                                
                if (getMethod != null) {
                    if (setMethod == null) {
                        Object subObject = invoke(parent, getMethod, (Object[])null);    
                        appendAttributes(n);
                        processBranch(subObject, n.getChildNodes(), level + 1);                        
                        property = true;
                    } else {
                        Class[] params = setMethod.getParameterTypes();
                                            
                        if (params.length == 1) {                        
                            NodeList propNodes = n.getChildNodes();
                            Node node = null;
                            
                            if (propNodes.getLength() == 1) 
                                node = propNodes.item(0);
                            else {
                                for (int i = propNodes.getLength() - 1; i >= 0; i--) {
                                    Node item = propNodes.item(i);
                                    
                                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                                        node = item;
                                        break;
                                    }
                                }                                                    
                            }
                            
                            if (node != null) {
                                short nodeType = node.getNodeType();
                                Class paramClass = propertyAlias != null ? (Class)propertyAlias[1] : params[0];
                                Object value;
                                
                                //If there is only one property then this is a simple value assignment
                                //Else if there is three then there is a tag being set as the value
                                if (nodeType == Node.TEXT_NODE) {                            
                                    value = getObjectForTypeFromString(paramClass, (String)node.getNodeValue(), true);
                                } else if (nodeType == Node.ELEMENT_NODE) {
                                    value = evaluateNode(null, node, level + 1);
                                    Class valueClass = value.getClass();
                                    
                                    if (!paramClass.isAssignableFrom(valueClass)) {
                                        value = getObjectForTypeFromString(paramClass, value.toString(), false);
                                        if (value == null) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "no conversion from " + valueClass + " to " + paramClass + " is known");
                                    }
                                } else {
                                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "a property tag can only contain a value or a single tag");
                                }
                                
                                if (value == null) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "the property tag's value resolved to null which is not valid");
                                
                                invoke(parent, setMethod, value);                        
                                if (log.isLoggable(LEVEL)) log.log(LEVEL, "set property " + name + " = " + value);
                            }
                            
                            property = true;
                        }
                    }
                }
            }
            
            //If this was determined to not be a property, this must be a class instantiation
            if (!property) {
                Class c = aliases.get(name);
                if (c == null) c = getClassForName(name, name);

                NodeList children = n.getChildNodes();
                
                try {
                    String id = null;
                    NamedNodeMap attrs = n.getAttributes();
                    List<Object[]> nonStatic = null;
                    
                    for (int cnt = attrs.getLength(); --cnt >=0;) {
                        Node attr = attrs.item(cnt);
                        String attrName = attr.getNodeName();
                        String attrValue = attr.getNodeValue();
                        
                        if (attrName.equals("id")) {
                            id = attrValue;
                        } else {
                            String setter = "set" + Character.toUpperCase(attrName.charAt(0)) + attrName.substring(1);
                            boolean found = false;
                            
                            for (Method m : c.getMethods()) {
                                String methodName = m.getName();

                                if (methodName.equals(attrName) || methodName.equals(setter)) {
                                    Class[] args = m.getParameterTypes();
                                    
                                    if (args.length == 1) {
                                        found = true;
                                        Object arg = getObjectForTypeFromString(args[0], attrValue, true);
                                        
                                        if (Modifier.isStatic(m.getModifiers())) {
                                            ret = invoke(null, m, arg);
                                            
                                            if (!c.isInstance(ret)) {
                                                if (log.isLoggable(LEVEL)) log.log(LEVEL, "invoked static id=" + id + ":" + c.getName() + "." + methodName + "('" + attrValue + "')");
                                                ret = null;
                                            } else {
                                                if (log.isLoggable(LEVEL)) log.log(LEVEL, "static factory new id=" + id + ":" + c.getName() + "." + methodName + "('" + attrValue + "')");
                                            }
                                        } else {
                                            if (nonStatic == null) nonStatic = new ArrayList<Object[]>(3);
                                            
                                            if (methodName.equals(attrName)) {
                                                nonStatic.add(0, new Object[]{m, arg});
                                            } else {
                                                nonStatic.add(new Object[]{m, arg});
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (!found) throw new DOMException(DOMException.NOT_FOUND_ERR, "no target method found id=" + id + ":" + c.getName() + "." + attrName + "('" + attrValue + "')");
                        }
                    }

                    if (ret == null) {
                        ret = c.newInstance();
                        if (log.isLoggable(LEVEL)) log.log(LEVEL, "new " + c.getName());
                    }
                    
                    if (nonStatic != null) {
                        for (Object[] callMeth : nonStatic) {
                            invoke(ret, (Method)callMeth[0], callMeth[1]);
                            if (log.isLoggable(LEVEL)) log.log(LEVEL, "invoked id=" + id + ":" + c.getName() + "." + ((Method)callMeth[0]).getName() + "(" + callMeth[1] + ")");
                        }
                    }
                    
                    if (id != null) objectMap.put(id, ret);
                    objects.add(ret);
                    if (level == 1) rootObjects.add(ret);
                    
                    if (parent != null && parent instanceof Collection) {
                        if (log.isLoggable(LEVEL)) log.log(LEVEL, "adding child to collection");
                        ((Collection)parent).add(ret);
                    }
                    
                    processBranch(ret, children, level + 1);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        return ret;
    }

    private static final Pattern REGEX_PROPERTY = Pattern.compile("(.*?)[$][{]([\\w.]+)[}](.*?)", Pattern.DOTALL);
    
    private String replaceProperties(String value) {
        if (log.isLoggable(LEVEL)) log.log(LEVEL, "before replaceProperties:" + value);
        if (value != null && value.indexOf("${") >= 0) {
            StringBuffer sb = new StringBuffer();
            Matcher m = REGEX_PROPERTY.matcher(value);
            
            while (m.find()) {
                String prop = m.group(2);
                String val = properties.get(prop);
                if (val == null) val = "${" + prop + "}";
                m.appendReplacement(sb, m.group(1) + val + m.group(3));
            }
            
            m.appendTail(sb);
            value = sb.toString();
        }
        
        if (log.isLoggable(LEVEL)) log.log(LEVEL, "after replaceProperties:" + value);
        return value;
    }
    
    private Object getObjectForTypeFromString(Class type, String str, boolean performObjectFieldLookup) {
        Object value;
        str = replaceProperties(str);
        
        if (type == String.class) {
            value = str;
    	} else if (type == boolean.class || type == Boolean.class) {
            value = Boolean.valueOf(str);
        } else if (type == int.class || type == Integer.class) {                            
            value = new Integer(Double.valueOf(str).intValue());
        } else if (type == long.class || type == Long.class) {                            
            value = new Long(Double.valueOf(str).longValue());
        } else if (type == short.class || type == Short.class) {
            value = new Short(Double.valueOf(str).shortValue());
        } else if (type == byte.class || type == Byte.class) {
            value = new Byte(Double.valueOf(str).byteValue());
        } else if (type == float.class || type == Float.class) {
            value = new Float(Double.valueOf(str).floatValue());
        } else if (type == double.class || type == Double.class) {
            value = Double.valueOf(str);                                
        } else if (type == char.class || type == Character.class) {                                
            value = new Character(str.charAt(0));
        } else if (performObjectFieldLookup) {
            //See if there is a constant with the name specified
            try {
                Method method = type.getMethod("valueOf", String.class);
                value = method.invoke(null, str);
            } catch (Exception e) {
                String upperStr = str.toUpperCase();
                value = null;
                
                for (Field f : type.getFields()) {
                    if (f.getName().toUpperCase().equals(upperStr)) {
                        try {
                            value = f.get(null);
                        } catch (IllegalAccessException e2) {
                            value = null;
                        }

                        break;
                    }
                }
                
                if (value == null) value = objectMap.get(str);
            }
        } else
            value = null;
        
        return value == null ? str : value;
    }
    
    private Object invoke(Object object, Method method, Object... params) {
        try {             
            if (!method.isAccessible()) method.setAccessible(true);
            return method.invoke(object, params);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("[" + object.getClass().getName() + "." + method.getName() + "]", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("[" + object.getClass().getName() + "." + method.getName() + "]", e);
        }        
    }
    
    private void setPropertyAlias(Class clazz, String propertyName, String aliasName, Class aliasClass) {
        if (propertyAliases == null) propertyAliases = new HashMap<Class, Map<String, Object[]>>(3);
        Map<String, Object[]> map = propertyAliases.get(clazz);        
        if (map == null) propertyAliases.put(clazz, map = new HashMap<String, Object[]>(1));
        map.put(aliasName, new Object[] {propertyName, aliasClass});
    }
    
    private Object[] getPropertyAlias(Class clazz, String propertyName) {
        if (propertyAliases != null) {
            Map<String, Object[]> map = propertyAliases.get(clazz);        
            if (map != null) return map.get(propertyName);
        }
        
        return null;        
    }
        
    private Class getClassForName(String name, String className) {
        try {
        	Class c = Application.getApplicationContextClass(className);
            if (name != null) aliases.put(name, c);
            return c;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }        
    }
}
