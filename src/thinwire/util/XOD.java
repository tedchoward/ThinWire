package thinwire.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import thinwire.ui.Application;
import thinwire.ui.style.Style;

/**
 * Xml Object Document (XOD) generates objects from the XML file, and associates them with a Map.
 * XODs can be used to generate screens from screen layouts.<p>
 * <h3>Sample Screen</h3>
 * <img src="doc-files/XOD-1.png"> <p>
 * <h3>Sample Screen Layout XML</h3>
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;xod&gt;
 *     &lt;include file="TestScripts/aliasuiclass.xml"/&gt;
 *     &lt;Dialog id="APPID"&gt;
 *         &lt;title&gt;Deal Search Screen&lt;/title&gt;
 *         &lt;width&gt;574&lt;/width&gt;
 *         &lt;height&gt;333&lt;/height&gt;
 *         &lt;children&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Search Results...&lt;/text&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;14&lt;/x&gt;
 *                 &lt;y&gt;122&lt;/y&gt;
 *                 &lt;width&gt;119&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Search...&lt;/text&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;14&lt;/x&gt;
 *                 &lt;y&gt;53&lt;/y&gt;
 *                 &lt;width&gt;63&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Go Directly To...&lt;/text&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;14&lt;/x&gt;
 *                 &lt;y&gt;4&lt;/y&gt;
 *                 &lt;width&gt;119&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;Divider&gt;
 *                 &lt;x&gt;7&lt;/x&gt;
 *                 &lt;y&gt;46&lt;/y&gt;
 *                 &lt;width&gt;574&lt;/width&gt;
 *                 &lt;height&gt;5&lt;/height&gt;
 *             &lt;/Divider&gt;
 *             &lt;Divider&gt;
 *                 &lt;x&gt;7&lt;/x&gt;
 *                 &lt;y&gt;115&lt;/y&gt;
 *                 &lt;width&gt;574&lt;/width&gt;
 *                 &lt;height&gt;5&lt;/height&gt;
 *             &lt;/Divider&gt;
 *             &lt;TextField id="WRK_GOTO_DEAL"&gt;
 *                 &lt;editMask&gt;999999999&lt;/editMask&gt;
 *                 &lt;x&gt;112&lt;/x&gt;
 *                 &lt;y&gt;24&lt;/y&gt;
 *                 &lt;width&gt;63&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/TextField&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Deal Number:&lt;/text&gt;
 *                 &lt;labelFor&gt;WRK_GOTO_DEAL&lt;/labelFor&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;21&lt;/x&gt;
 *                 &lt;y&gt;24&lt;/y&gt;
 *                 &lt;width&gt;84&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Primary:&lt;/text&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;300&lt;/x&gt;
 *                 &lt;y&gt;24&lt;/y&gt;
 *                 &lt;width&gt;56&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;RadioButton id="rb_Y"&gt;
 *                 &lt;value&gt;Y&lt;/value&gt;
 *                 &lt;text&gt;Yes&lt;/text&gt;
 *                 &lt;x&gt;375&lt;/x&gt;
 *                 &lt;y&gt;24&lt;/y&gt;
 *                 &lt;width&gt;35&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/RadioButton&gt;
 *             &lt;RadioButton id="rb_N"&gt;
 *                 &lt;value&gt;N&lt;/value&gt;
 *                 &lt;text&gt;No&lt;/text&gt;
 *                 &lt;x&gt;437&lt;/x&gt;
 *                 &lt;y&gt;24&lt;/y&gt;
 *                 &lt;width&gt;35&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/RadioButton&gt;                        
 *             &lt;DropDownGridBox id="WRK_SEARCH_TYPE"&gt;
 *                 &lt;editAllowed&gt;false&lt;/editAllowed&gt;
 *                 &lt;x&gt;112&lt;/x&gt;
 *                 &lt;y&gt;73&lt;/y&gt;
 *                 &lt;width&gt;378&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/DropDownGridBox&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Type:&lt;/text&gt;
 *                 &lt;labelFor&gt;WRK_SEARCH_TYPE&lt;/labelFor&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;70&lt;/x&gt;
 *                 &lt;y&gt;73&lt;/y&gt;
 *                 &lt;width&gt;35&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;TextField id="WRK_SEARCH_VALUE"&gt;
 *                 &lt;editMask&gt;xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&lt;/editMask&gt;
 *                 &lt;x&gt;112&lt;/x&gt;
 *                 &lt;y&gt;93&lt;/y&gt;
 *                 &lt;width&gt;350&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/TextField&gt;
 *             &lt;Label&gt;
 *                 &lt;text&gt;Value:&lt;/text&gt;
 *                 &lt;labelFor&gt;WRK_SEARCH_VALUE&lt;/labelFor&gt;
 *                 &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *                 &lt;x&gt;63&lt;/x&gt;
 *                 &lt;y&gt;93&lt;/y&gt;
 *                 &lt;width&gt;42&lt;/width&gt;
 *                 &lt;height&gt;20&lt;/height&gt;
 *             &lt;/Label&gt;
 *             &lt;GridBox id="WRK_RESULTS"&gt;
 *                 &lt;x&gt;7&lt;/x&gt;
 *                 &lt;y&gt;142&lt;/y&gt;
 *                 &lt;width&gt;553&lt;/width&gt;
 *                 &lt;height&gt;154&lt;/height&gt;
 *             &lt;/GridBox&gt;
 *             &lt;Button id="OK_btn"&gt;
 *                 &lt;image&gt;OK&lt;/image&gt;
 *                 &lt;text&gt;OK&lt;/text&gt;
 *                 &lt;x&gt;14&lt;/x&gt;
 *                 &lt;y&gt;301&lt;/y&gt;
 *                 &lt;width&gt;56&lt;/width&gt;
 *                 &lt;height&gt;30&lt;/height&gt;
 *             &lt;/Button&gt;
 *             &lt;Button id="Search_btn"&gt;
 *                 &lt;image&gt;DETAIL&lt;/image&gt;
 *                 &lt;text&gt;Search&lt;/text&gt;
 *                 &lt;x&gt;73&lt;/x&gt;
 *                 &lt;y&gt;301&lt;/y&gt;
 *                 &lt;width&gt;84&lt;/width&gt;
 *                 &lt;height&gt;30&lt;/height&gt;
 *             &lt;/Button&gt;
 *             &lt;Button id="Cancel_btn"&gt;
 *                 &lt;image&gt;CANCEL&lt;/image&gt;
 *                 &lt;text&gt;Cancel&lt;/text&gt;
 *                 &lt;x&gt;160&lt;/x&gt;
 *                 &lt;y&gt;301&lt;/y&gt;
 *                 &lt;width&gt;84&lt;/width&gt;
 *                 &lt;height&gt;30&lt;/height&gt;
 *             &lt;/Button&gt;
 *         &lt;/children&gt;
 *     &lt;/Dialog&gt;
 *     &lt;RadioButton.Group id="rb_group"&gt;
 *         &lt;ref id="rb_Y" /&gt;
 *         &lt;ref id="rb_N" /&gt;
 *     &lt;/RadioButton.Group&gt;
 * &lt;/xod&gt;
 * </pre>
 * <h3>Sample NextGen Script</h3>
 * <pre>
 * importPackage(Packages.com.customcreditsystems.ng.tools);
 * importPackage(Packages.com.customcreditsystems.ng.ui);
 * importPackage(Packages.java.util);
 * 
 * 
 * var app = Application.current();
 * var map = new HashMap();
 * app.setProperty("Resource.map", map);
 * with (map) {
 *     put("OK", "resources/ngLF/ok.png");
 *     put("CANCEL", "resources/ngLF/cancel.png");
 *     put("DETAIL", "resources/ngLF/detail.png");
 * }
 * 
 * var xod =  new XOD();
 * xod.execute('TestScripts/screentest.xml');  
 * var screen = new Object();
 * JS.fromMap(xod.getObjectMap(), screen);
 * 
 * screen.APPID.visible = true;
 * </pre>
 *  <h3>Sample Alias XML File</h3>
 * The &lt;alias&gt; tag associates an alias with a class name.  This content doesn't
 * have to be separate.  Instead of using the &lt;include&gt; tag in the Screen Layout XML file displayed
 * above, the Screen Layout file could have included it directly.<p>
 * <pre>
 * &lt;xod&gt;
 *     &lt;alias name="Button" class="com.customcreditsystems.ng.ui.Button"/&gt;
 *     &lt;alias name="CheckBox" class="com.customcreditsystems.ng.ui.CheckBox"/&gt;
 *     &lt;alias name="Dialog" class="com.customcreditsystems.ng.ui.Dialog"/&gt;
 *     &lt;alias name="Divider" class="com.customcreditsystems.ng.ui.Divider"/&gt;
 *     &lt;alias name="DropDownGridBox" class="com.customcreditsystems.ng.ui.DropDownGridBox"/&gt;
 *     &lt;alias name="Frame" class="com.customcreditsystems.ng.ui.Frame"/&gt;
 *     &lt;alias name="GridBox" class="com.customcreditsystems.ng.ui.GridBox"/&gt;
 *     &lt;alias name="Image" class="com.customcreditsystems.ng.ui.Image"/&gt;
 *     &lt;alias name="Label" class="com.customcreditsystems.ng.ui.Label"/&gt;
 *     &lt;alias name="Menu" class="com.customcreditsystems.ng.ui.Menu"/&gt;
 *     &lt;alias name="Panel" class="com.customcreditsystems.ng.ui.Panel"/&gt;
 *     &lt;alias name="RadioButton" class="com.customcreditsystems.ng.ui.RadioButton"/&gt;
 *     &lt;alias name="RadioButton.Group" class="com.customcreditsystems.ng.ui.RadioButton$Group"/&gt;
 *     &lt;alias name="TabFolder" class="com.customcreditsystems.ng.ui.TabFolder"/&gt;
 *     &lt;alias name="TabSheet" class="com.customcreditsystems.ng.ui.TabSheet"/&gt;
 *     &lt;alias name="TextArea" class="com.customcreditsystems.ng.ui.TextArea"/&gt;
 *     &lt;alias name="TextField" class="com.customcreditsystems.ng.ui.TextField"/&gt;
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
 *Many of the tags in the Screen Layout XML listed above can be thought of as
 *instruction to build UI components.  E.g. The &lt;Button&gt; tag can be thought of as
 *instruction to build a Button component.
 *<p>
 *In general, an XOD file can contain tags of the form
 *<pre>
 *  &lt;com.customcreditsystems.ng.ui.XXXX&gt;
 *</pre>
 *where XXXX is a UI Component class, and such a tag can be thought of as an 
 *instruction to create an instance of the XXXX class.<p>
 *But the full class names are long.  To allow for shorter tags in your Screen Layout XML, you can define an alias: <br>
 *<pre>
 *  &lt;alias name="Button" class="com.customcreditsystems.ng.ui.Button"/&gt;   
 *</pre>
 *Once you've included this alias in your Screen Layout XML, you can construct
 *a button using it:<br>
 *<pre>
 *   &lt;Button id="Search_btn"&gt;
 *        &lt;image&gt;DETAIL&lt;/image&gt;
 *        &lt;text&gt;Search&lt;/text&gt;
 *        &lt;x&gt;73&lt;/x&gt;
 *        &lt;y&gt;301&lt;/y&gt;
 *        &lt;width&gt;84&lt;/width&gt;
 *        &lt;height&gt;30&lt;/height&gt;
 *   &lt;/Button&gt;
 * </pre>
 *<h4>&lt;ref&gt;</h4>
 *Some UI Components need to be associated with other UI Components.
 *E.g. We need a means to indicate that the RadioButtons described in the
 *Screen Layout XML are members of the RadioButton Group named "rb_group".
 *We use the &lt;ref&gt; tag to accomplish this.
 *<p>
 *<h4>UI Class Tags - e.g. &lt;Button&gt;, &lt;Label&gt;, &lt;Divider&gt;</h4>
 *If there's a UI Component class with the name "com.customcreditsystems.ng.ui.XXXX", you can include an
 *&lt;com.customcreditsystems.ng.ui.XXXX&gt; element in your Screen Layout XML.  You can also
 *create an alias for that class and use it in place of the class name. 
 *<p>
 *<h4>Component Property Tags - e.g. &lt;width&gt;, &lt;text&gt;, &lt;x&gt;</h4>
 *To specify properties - e.g. location, size, and text - for the components you wish
 *to build, add property tag elements. In general, if a UI component class has a "setXxxx" method,
 *your Screen Layout XML can include an &lt;xxxx&gt; property tag.<p>
 *E.g. Since the com.customcreditsystems.ng.ui.Button class has a setText method,
 *you can specify the text for your button with a &lt;text&gt; element.
 *
 *<pre>
 *   &lt;Button id="Search_btn"&gt;
 *        ....
 *        &lt;text&gt;Search&lt;/text&gt;
 *        ....
 *   &lt;/Button&gt;
 * </pre>
 * 
 * <h4>Property Tag Values</h4>
 * The properties of UI component classes have types. E.g.  The text property of the
 * Button class is of type String, and the x property of the Button class is of type
 * int. Other standard types are Integer, double, Double, char, Character, long, Long,
 * boolean, Boolean, float, Float, byte, and Byte.
 * <p>
 * In the case of these standard types, the XOD class will make the appropriate conversion
 * while processing a screen layout. E.g. When it comes across
 * <pre>
 *   &lt;Button id="Search_btn"&gt;
 *        .....
 *        &lt;x&gt;73&lt;/x&gt;
 *        .....
 *   &lt;/Button&gt;
 * </pre>
 * the XOD class will convert "73" to an int and assign the x property of the
 * new Button that int value.
 * <p>
 * In the case of NextGen custom types, however, XOD has two special strategies.
 * First, it looks for an object defined elsewhere in the screen layout, an object
 * whose id in the screen layout matches the property value. If it finds one, it assigns
 * the object as a value for the property. E.g. When it comes across
 * <pre>
 *  &lt;Label&gt;
 *      .....
 *      &lt;labelFor&gt;WRK_GOTO_DEAL&lt;/labelFor&gt;
 *      .....
 *  &lt;/Label&gt;
 * </pre>
 * then XOD discovers that there is a WRK_GOTO_DEAL object defined elsewhere, and it assigns
 * this object to the Label's labelFor property.<p> 
 * Second, if XOD doesn't find the value defined elsewhere in the screen layout, it 
 * will look for a constant on the custom type class. 
 * E.g. The Label class has an alignX property of type com.customcreditsystems.ng.ui.AlignX.
 * This class has a constant named RIGHT.  When XOD processes the above screen layout and comes across
 * <pre>
 * &lt;Label&gt;
 *     .....
 *     &lt;alignX&gt;RIGHT&lt;/alignX&gt;
 *     .....
 * &lt;/Label&gt;
 * </pre>
 * it finds a constant named RIGHT on the AlignX class, and this is what it
 * assigns to the alignX property of the new Label. 
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
 *      &lt;TextField id="WRK_GOTO_DEAL"&gt;
 *          .... 
 *      &lt;/TextField&gt;
 *      ....
 *  &lt;/children&gt;
 * </pre>
 * it constructs Label, Divider, and TextField components and makes these
 * components children of the Dialog.
 *
 * @author jjg
 */
public final class XOD {
    private static final Logger log = Logger.getLogger(XOD.class.getName());   

    public static void main(String args[]) {
        java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
        handler.setLevel(Level.FINEST);
        log.addHandler(handler);
        log.setLevel(Level.FINEST);
        XOD xod = new XOD();
        xod.execute("class:///thinwire.ui.Application/resources/DefaultStyle.xml");
        Style s = (Style)xod.getObjectMap().get("thinwire.ui.Button");
        System.out.println("fx.positionChange=" + s.getFX().getPositionChange());
        System.out.println("aliasMap.size=" + xod.getAliasMap().size());
        System.out.println("objectMap.size=" + xod.getObjectMap().size());
    }
    
    private static File getRelativeFile(String uri) {
        Application app = Application.current();
        return app == null ? new File(uri) : app.getRelativeFile(uri);
    }    

    private static File getRelativeFile(String parent, String child) {
        Application app = Application.current();
        return app == null ? new File(parent, child) : app.getRelativeFile(parent, child);
    }    
    
    private Map<String, Object> objectMap;
    private Map<Object, String> idMap;
    private List<Object> objects;
    private Map<String, Class> aliases;
    private Map<Class, Map<String, Object[]>> propertyAliases;
    private boolean processingInclude;
    private String topFileUri;
       
    /**
     * Create a new XOD.
     */
    public XOD() {
        objectMap = new HashMap<String, Object>();
        objects = new ArrayList<Object>();
        aliases = new HashMap<String, Class>();
    }
    
	/**
	 * The alias Map is typically used to map abbreviated class names to full class names.
     * E.g. the alias Map may map "Button" to "com.customcreditsystems.ng.ui.Button".
	 * @return the alias Map
	 */
    public Map<String, Class> getAliasMap() {
        return aliases;
    }
    
    /**
     * The object map contains references to previously defined objects
     * identified by id.  Typically, it will map an ID found in the 
     * Screen Layout XML to a UI Component.
     * @return the object Map
     */
    public Map<String, Object> getObjectMap() {
        return objectMap;
    }
    
    public List<Object> getObjects() {
        return Collections.unmodifiableList(objects);
    }
        
    /**
     * Gets the id that is associated to the specified object.
     * For this to succeed, the object must be in the object map.
     * @param o the object to retrieve the id for.
     * @return the id associated to the object, or null if the object is not in the object map.
     */
    public String getObjectId(Object o) {
        if (idMap == null) {
            idMap = new HashMap<Object, String>();

            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                idMap.put(entry.getValue(), entry.getKey());
            }
        }
        
        return idMap.get(o);
    }
    
    /**
     * Executes the XML file to build the object tree.
     * Typically used to construct a screen from a screen layout XML file.
     * @param uri the name of the xml file.
     */
    public void execute(String uri) {
        this.topFileUri = uri;
        processFile(null, uri, 0);
    }

    private InputStream getResourceAsStream(String uri) {
        try {
            InputStream is;
            
            //"class:///thinwire.ui.layout.SplitLayout/resources/Image.png"                
            if (uri.startsWith("class:///")) {                    
                int endIndex = uri.indexOf('/', 9);
                String className = uri.substring(9, endIndex);
                String resource = uri.substring(endIndex + 1);
                Class clazz = Class.forName(className);
                is = clazz.getResourceAsStream(resource);
                if (is == null) throw new FileNotFoundException(uri);            
            } else {
                Application app = Application.current();
                is = new FileInputStream(app == null ? new File(uri) : app.getRelativeFile(uri));
            }
    
            return is;
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
            throw (RuntimeException)e;
        }
    }
    
    private Object processFile(Object parent, String uri, int level) {
        Object ret = null;
        
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            BufferedInputStream is = new BufferedInputStream(getResourceAsStream(uri));                   
            Document doc = builder.parse(is);
            is.close();            
            ret = processBranch(parent, doc.getChildNodes(), level);
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
            throw (RuntimeException)e;
        }
        
        return ret;
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
            if (log.isLoggable(Level.FINEST)) log.finest("xod");
            processBranch(parent, n.getChildNodes(), level + 1);
        } else if (name.equals("alias")) {
            if (n.getAttributes().getLength() != 2) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 2");
            //if (n.getChildNodes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() != 0");
            String aliasName = (String)n.getAttributes().getNamedItem("name").getNodeValue();
            String className = (String)n.getAttributes().getNamedItem("class").getNodeValue();
            Class clazz = getClassForName(aliasName, className);
            if (n.hasChildNodes()) processBranch(clazz, n.getChildNodes(), level + 1);            
            if (log.isLoggable(Level.FINEST)) log.finest("alias[name:" + aliasName + ",class:" + className + "]");
        } else if (name.equals("property") && parent instanceof Class) {
            if (n.getAttributes().getLength() != 2 && n.getAttributes().getLength() != 3) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 2 && n.getAttributes().getLength() != 3");
            String propName = (String)n.getAttributes().getNamedItem("name").getNodeValue();
            String aliasName = (String)n.getAttributes().getNamedItem("alias").getNodeValue();
            String className = (String)n.getAttributes().getNamedItem("class").getNodeValue();
            setPropertyAlias((Class)parent, propName, aliasName, getClassForName(null, className));
        } else if (name.equals("include")) {
            if (n.getAttributes().getLength() != 1) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 1");
            if (n.getChildNodes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() != 0");
            String fileUri = (String)n.getAttributes().getNamedItem("file").getNodeValue();
                        
            if (!getRelativeFile(fileUri).exists()) {
                String curFileUri = topFileUri;
                String newUri = fileUri;
                
                do {
                    File parentFolder = getRelativeFile(curFileUri).getParentFile();
                    
                    if (parentFolder == null) {
                        newUri = null;
                        break;
                    }
                    
                    curFileUri = parentFolder.getAbsolutePath();
                    newUri = getRelativeFile(curFileUri, fileUri).getAbsolutePath();
                } while (!getRelativeFile(newUri).exists());
                
                if (newUri != null) fileUri = newUri;
            }
            
            processingInclude = true;
            processFile(parent, fileUri, level + 1);
            processingInclude = false;
            if (log.isLoggable(Level.FINEST)) log.finest("include[file:" + fileUri + "]");
        } else if (name.equals("ref")) {
            if (n.getAttributes().getLength() != 1) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getAttributes().getLength() != 1");
            if (n.getChildNodes().getLength() != 0) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, name + ":n.getChildNodes().getLength() != 0");
            String id = (String)n.getAttributes().getNamedItem("id").getNodeValue();
            ret = objectMap.get(id);            
            if (log.isLoggable(Level.FINEST)) log.finest("ref[id:" + id + "]");
            
            if (parent != null && parent instanceof Collection) {
                if (log.isLoggable(Level.FINEST)) log.finest("adding ref child to collection");
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
                        m.setAccessible(true);
                    } else if (methodName.equals(getter) || methodName.equals(istter)) {
                        getMethod = m;
                        m.setAccessible(true);
                    }
                }
                                
                if (getMethod != null) {
                    if (setMethod == null) {
                        Object subObject = invoke(parent, getMethod, null);    
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
                            if (log.isLoggable(Level.FINEST)) log.finest("set property " + name + " = " + value);
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
                            for (Method m : c.getMethods()) {
                                if (m.getName().equals(attrName)) {
                                    Class[] args = m.getParameterTypes();
                                    
                                    if (args.length == 1) {
                                        Object arg = getObjectForTypeFromString(args[0], attrValue, true);
                                        
                                        if (Modifier.isStatic(m.getModifiers())) {
                                            ret = invoke(null, m, arg);
                                            if (!c.isInstance(ret)) {
                                                ret = null;
                                            } else {
                                                if (log.isLoggable(Level.FINEST)) log.finest("new " + c.getName() + ".valueOf('" + attrValue + "')");
                                            }
                                        } else {
                                            if (nonStatic == null) nonStatic = new ArrayList<Object[]>(3);
                                            nonStatic.add(new Object[]{m, arg});
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (ret == null) {
                        ret = c.newInstance();
                        if (log.isLoggable(Level.FINEST)) log.finest("new " + c.getName());
                    }
                    
                    if (nonStatic != null) {
                        for (Object[] callMeth : nonStatic) {
                            invoke(ret, (Method)callMeth[0], callMeth[1]);
                            if (log.isLoggable(Level.FINEST)) log.finest("calling simple method:" + c.getName() + "." + ((Method)callMeth[0]).getName() + "(" + callMeth[1] + ")");
                        }
                    }

                    for (int cnt = attrs.getLength(); --cnt >=0;) {
                        Node attr = attrs.item(cnt);
                        String attrName = attr.getNodeName();
                        String attrValue = attr.getNodeValue();
                        
                        if (attrName.equals("id")) {
                            id = attrValue;
                        } else {
                            for (Method m : c.getMethods()) {
                                if (m.getName().equals(attrName)) {
                                    Class[] args = m.getParameterTypes();
                                    
                                    if (args.length == 1) {
                                        invoke(ret, m, getObjectForTypeFromString(args[0], attrValue, true));
                                    }
                                }
                            }
                        }
                    }
                    
                    if (id != null) objectMap.put(id, ret);
                    objects.add(ret);
                    
                    if (parent != null && parent instanceof Collection) {
                        if (log.isLoggable(Level.FINEST)) log.finest("adding child to collection");
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
    
    private Object getObjectForTypeFromString(Class type, String str, boolean performObjectFieldLookup) {
        Object value;
        
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
            Class c = Class.forName(className);
            if (name != null) aliases.put(name, c);
            return c;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }        
    }
}
