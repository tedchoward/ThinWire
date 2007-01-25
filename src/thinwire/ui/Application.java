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

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ExceptionEvent;
import thinwire.ui.event.ExceptionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.*;
import thinwire.util.XOD;

/**
 * The Application class represents an instance of a ThinWire application.  Methods in this class are
 *  those that directly affect the system and it's environment.
 * @author Joshua J. Gertzen
 */
public abstract class Application {
	private static final Logger log = Logger.getLogger(Application.class.getName());
    private static final String DEFAULT_STYLE_SHEET = "class:///" + Application.class.getName() + "/resources/DefaultStyle.zip";
    
    private static final Map<String, String> versionInfo;
    static {
        Properties props = new Properties();
        Map<String, String> vi = new HashMap<String, String>();
        
        try {
            props.load(Application.class.getResourceAsStream("resources/versionInfo.properties"));
            
            for (Map.Entry<Object, Object> e : props.entrySet()) {
                String key = e.getKey().toString();
                key = key.substring(key.indexOf('.') + 1);
                vi.put(key, e.getValue().toString());
            }
            
            versionInfo = Collections.unmodifiableMap(vi);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }            
    
    protected static abstract class AppThread extends Thread {
        protected Application app;
        protected String id;
        
        public AppThread(Application app, String id) {
            super("ThinWire AppThread-" + id);
            this.app = app;
            this.id = id;
        }
    }
    
    private Map<Local, Object> appLocal = new WeakHashMap<Local, Object>();
    
    public static class Local<T> {
        public void set(T value) {
            Map<Local, Object> map = Application.current().appLocal;
            
            synchronized (map) {
                map.put(this, value);
            }
        }
        
        public T get() {
            Map<Local, Object> map = Application.current().appLocal;

            synchronized (map) {
                T value = (T)map.get(this);
                if (value == null && !map.containsKey(this)) map.put(this, value = initialValue());
                return value;
            }
        }
        
        protected T initialValue() {
            return null;
        }
    }
        
    /**
     * Returns the current version info details for the platform.
     * The returned Map is read-only and contains the following keys:
     * companyName
     * companyAddress1
     * companyAddress2
     * companyCity
     * companyState
     * companyZip
     * companyPhone
     * companyWebsite
     * productDescription
     * productVersion
     * internalName
     * legalCopyright
     * originalFilename
     * @return the current version info details for the platform.
     */
    public static Map<String, String> getPlatformVersionInfo() {
        return versionInfo;
    }    
    
    /**
     * Displays a version detail dialog.
     * @param args
     */
    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream("resources/licenseHeader.txt")));        
        String line = r.readLine();
        
        while (line != null) {
            sb.append(line).append('\n');
            line = r.readLine();
        }

        sb.append("\nContents of the Application.getPlatformVersionInfo() map:\n\n");
        
        for (Map.Entry<String, String> e : getPlatformVersionInfo().entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append('\n');
        }

        final java.awt.Frame frame = new java.awt.Frame("ThinWire(R) RIA Ajax Framework v" + getPlatformVersionInfo().get("productVersion"));
        final java.awt.TextArea ta = new java.awt.TextArea(sb.toString());
        ta.setEditable(false);
        frame.add(ta);
        frame.setSize(640, 480);
        frame.setVisible(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent ev) {
                frame.dispose();
            }            
        });        
    }
    
    /**
     * @return the current instance of the application, or null if called from a thread other than the UI thread.
     */
	public static Application current() {
        Thread t = Thread.currentThread();
        
        if (t instanceof AppThread) {
            return ((AppThread)t).app;
        } else {
            return null;
        }
	}
    
    /**
     * Returns an InputStream representing the specified resource.
     * The following URL's are supported:
     * <ul>
     *  <li>Any valid resource filename. If Application.current() returns non-null,
     *   which is the typical scenario, then relative paths are interpreted as 
     *   relative to the application folder. {@link #getRelativeFile}</li>
     *  <li>class:///com.mypackage.MyClass/resource.ext</li>
     *  <li>http://www.mycompany.com/resource.ext</li>
     * </ul>
     * 
     * @return an InputStream representing the specified resource or null if the resource was not found.
     */
    public static InputStream getResourceAsStream(String uri) {
        InputStream is = null;

        if (uri != null && uri.trim().length() > 0) {
            try {
                String innerFile = null;
                int index = uri.indexOf(".zip");
                
                if (index > 0 && index + 5 < uri.length()) {
                    innerFile = uri.substring(index + 5);
                    uri = uri.substring(0, index + 4);
                }
                
                //"class:///thinwire.ui.layout.SplitLayout/resources/Image.png"
                if (uri.startsWith("class:///")) {
                    int endIndex = uri.indexOf('/', 9);
                    String className = uri.substring(9, endIndex);
                    String resource = uri.substring(endIndex + 1);
                    Class clazz = Class.forName(className);
                    is = clazz.getResourceAsStream(resource);
                } else if (uri.startsWith("http://")) {
                    URL remoteImageURL = new URL(uri);
                    URLConnection remoteImageConnection = remoteImageURL.openConnection();
                    is = remoteImageConnection.getInputStream();
                } else {
                    Application app = Application.current();
                    File file = app == null ? new File(uri) : app.getRelativeFile(uri);
                    if (file.exists()) is = new FileInputStream(file);
                }
                
                if (is != null) {
                    is = new BufferedInputStream(is);
                    
                    if (innerFile != null) {
                        innerFile = innerFile.replace('\\', '/');
                        ZipInputStream zip = new ZipInputStream(is);
                        ZipEntry entry;
                        
                        while ((entry = zip.getNextEntry()) != null) {
                            if (!entry.isDirectory() && entry.getName().equals(innerFile)) {
                                byte[] bytes = new byte[(int)entry.getSize()];
                                int pos = 0, cnt;

                                while ((cnt = zip.read(bytes, pos, bytes.length - pos)) > 0) {
                                    pos += cnt;
                                }
                                
                                is = new ByteArrayInputStream(bytes);
                                break;
                            }
                            
                            zip.closeEntry();
                        }
                                                
                        zip.close();
                    }
                }
            } catch (Exception e) {
                if (e instanceof RuntimeException) throw (RuntimeException)e;
            }
       }
       
       return is;
    }
    
    public static byte[] getResourceBytes(String uri) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeResourceToStream(uri, baos);
        return baos.toByteArray();
    }

    public static void writeResourceToStream(String uri, OutputStream os) {
        if (os == null) throw new IllegalArgumentException("os == null");
        InputStream is = getResourceAsStream(uri);
        if (is == null) throw new IllegalArgumentException("Content for URI was not found:" + uri);
        byte[] bytes = new byte[128];
        int size;
        
        try {
            while ((size = is.read(bytes)) != -1)
                os.write(bytes, 0, size);
            
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ExceptionListener> exceptionListeners;
    private EventListenerImpl<PropertyChangeListener> gpcei;
    private WeakReference<Component> priorFocus;
    private Frame frame;
    
    //#IFDEF V1_1_COMPAT    
    private Map<String, String> fileMap;
    //#ENDIF
    private Map<String, Color> systemColors;
    private Map<String, String> systemImages;
    private Map<Class<? extends Component>, Style> compTypeToStyle;
    
    protected Application() {
        exceptionListeners = new ArrayList<ExceptionListener>();
        gpcei = new EventListenerImpl<PropertyChangeListener>(null, PropertyChangeListener.class);        
    }   
    
    /**
     * Adds a <code>PropertyChangeListener</code> that will be notified when the specified property of any new component changes. To
     * further clarify, a global property change listener will receive an event notification for any component that is created after
     * this global property change listener is added, for which the specified property changes.  Therefore, establishing a global
     * property change listener is the same as addding a {@link Component#addPropertyChangeListener(String, PropertyChangeListener)}
     * call after the creation of every new component. 
     * <p>
     * As a general rule, you should avoid using this feature because it can cause a large volume of events to be generated. This is
     * especially true if you listen to a frequently updated property, such as <code>PROPERTY_TEXT</code>. However, there are
     * cases where this can be quite useful. One such case is when you use the 'userObject' property to store a boolean state that
     * indicates whether a value is required for the component. In such a case, you can establish a global property change listener
     * for 'userObject' and then based on the property being set to <code>true</code> you could update the background color so it
     * is apparent to the user that a value is required.
     * </p>
     * <b>Example:</b>
     * 
     * <pre>
     * Component.addGlobalPropertyChangeListener(Component.PROPERTY_USER_OBJECT, new PropertyChangeListener() {
     *     public void propertyChange(PropertyChangeEvent pce) {
     *         Component comp = (Component) pce.getSource();
     * 
     *         if (comp.getUserObject() == Boolean.TRUE) {
     *             comp.getStyle().getBackground().setColor(Color.PALEGOLDENROD);
     *         } else {
     *             comp.getStyle().getBackground().setColor(null); //restore the default background color
     *         }
     *     }
     * });
     * 
     * TextField tf = new TextField();
     * tf.setUserObject(Boolean.TRUE); //Causes background color to be set to Color.PALEGOLDENROD
     * tf.setUserObject(Boolean.FALSE); //Causes background color to be set to the default.
     * </pre>
     * 
     * @param propertyName the name of the property that the listener will receive change events for.
     * @param listener the listener that will receive <code>PropertyChangeEvent</code> objects upon the property of any new component changing.
     * @throws IllegalArgumentException if <code>listener</code> or <code>propertyName</code> is null or if
     *         <code>propertyName</code> is an empty string.
     * @see Component#addPropertyChangeListener(String, PropertyChangeListener)
     * @see thinwire.ui.event.PropertyChangeListener
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void addGlobalPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        gpcei.addListener(propertyName, listener);
    }
    
    /**
     * Adds a <code>PropertyChangeListener</code> to the component that will be notified when any of the specified properties of
     * any new component changes. This method is equivalent to calling
     * {@link #addGlobalPropertyChangeListener(String, PropertyChangeListener)} once for each property you want to listen to.
     * @param propertyNames a string array of property names that the listener will receive change events for.
     * @param listener the listerner that will receive <code>PropertyChangeEvent</code> objects anytime one of the specified
     *        propertyNames of any new component change.
     * @throws IllegalArgumentException if <code>listener</code>, <code>propertyNames</code> or any property name is the array is null or if
     *         any property name is an empty string.
     * @see #addGlobalPropertyChangeListener(String, PropertyChangeListener)
     * @see Component#addPropertyChangeListener(String[], PropertyChangeListener)
     * @see thinwire.ui.event.PropertyChangeListener
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void addGlobalPropertyChangeListener(String[] propertyNames, PropertyChangeListener listener) {
        gpcei.addListener(propertyNames, listener);        
    }    
    
    /**
     * Removes the <code>PropertyChangeListener</code> from the list of global listeners that are added to all 
     * new <code>Component</code>'s.  To further clarify, removing a global property change listener will NOT
     * remove the <code>listener</code> from <code>Component</code>'s that have already been created.
     * @param listener the listener to remove from the notification list.
     * @throws IllegalArgumentException if <code>listener</code> is null.
     * @see thinwire.ui.event.PropertyChangeListener
     */       
    public void removeGlobalPropertyChangeListener(PropertyChangeListener listener) {
        gpcei.removeListener(listener);
    }
    
    EventListenerImpl<PropertyChangeListener> getGloalPropertyChangeListenerImpl() {
        return gpcei;
    }
    
    /**
     * 
     * @param listener
     */
    public void addExceptionListener(ExceptionListener listener) {
        exceptionListeners.add(listener);
    }
    
    /**
     * 
     * @param listener
     */
    public void removeExceptionListener(ExceptionListener listener) {
        exceptionListeners.remove(listener);
    }
    
    /**
     * 
     * @param source
     * @param exception
     */
    public void reportException(Object source, Throwable exception) {
        ExceptionEvent ee = new ExceptionEvent(source, exception);
        
        for (ExceptionListener el : exceptionListeners.toArray(new ExceptionListener[exceptionListeners.size()])) {
            el.exceptionOccurred(ee);
            if (ee.isStopPropagation()) break;
        }
        
        if (!ee.isSuppressLogging()) log.log(Level.SEVERE, null, exception);        
        
        if (!ee.isCanceled()) {
            final int msgSize = 300;
            final int gap = 5;
            final Dialog d = new Dialog("System Exception");
            String image = "WARNING";                       
            Image img = null;
            
            if (getRelativeFile(image).exists()) {
                img = new Image(image);
                img.setBounds(gap, gap, 40, 40);
                d.getChildren().add(img);
            }

            final TextArea taUserMessage = new TextArea(ee.getDefaultMessage());
            taUserMessage.setBounds(img == null ? gap : img.getX() + img.getWidth() + gap, gap, msgSize, 60);
            taUserMessage.setEnabled(false);
            d.getChildren().add(taUserMessage);
            
            final Button bDetail = new Button("Show Details >>");
            bDetail.setSize(90, 25);
            bDetail.setPosition(taUserMessage.getX() + taUserMessage.getWidth() - bDetail.getWidth(), taUserMessage.getY() + taUserMessage.getHeight() + gap);
            d.getChildren().add(bDetail);
            
            final TextArea taDetail = new TextArea(ee.getStackTraceText());

            final Button bOk = new Button();
            bOk.setBounds(bDetail.getX() - gap - bDetail.getWidth(), bDetail.getY(), bDetail.getWidth(), bDetail.getHeight());
            bOk.setText("OK");
            d.getChildren().add(bOk);
            bOk.addActionListener(Button.ACTION_CLICK, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    d.setVisible(false);
                }
            });
                        
            taDetail.setBounds(taUserMessage.getX(), bOk.getY() + bOk.getHeight() + gap, msgSize * 2, msgSize);
            taDetail.setVisible(false);
            d.getChildren().add(taDetail);            
                        
            int x = (getFrame().getInnerWidth() / 2) - ((d.getWidth() + msgSize) / 2);
            int y = (getFrame().getInnerHeight() / 2) - ((d.getHeight() + msgSize) / 2);
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            d.setPosition(x, y);
            
            final int normalWidth = 6 + taUserMessage.getX() + taUserMessage.getWidth() + gap;
            final int normalHeight = 25 + bDetail.getY() + bDetail.getHeight() + gap;            
            d.setSize(normalWidth, normalHeight);
            final int[] detailSize = new int[4];
            detailSize[0] = normalWidth + msgSize;
            detailSize[1] = normalHeight + msgSize + gap;
            detailSize[2] = taDetail.getWidth();
            detailSize[3] = taDetail.getHeight();
            
            final PropertyChangeListener dialogSizePCL = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    int diff = ((Integer)pce.getNewValue()) - ((Integer)pce.getOldValue());
                    
                    if (pce.getPropertyName().equals(Dialog.PROPERTY_WIDTH)) {
                        diff = taDetail.getWidth() + diff;
                        if (diff < 10) diff = 10;
                        taDetail.setWidth(diff);
                    } else {                        
                        diff = taDetail.getHeight() + diff;
                        if (diff < 10) diff = 10;
                        taDetail.setHeight(diff);
                    }
                }
            };            
            
            bDetail.addActionListener(Button.ACTION_CLICK, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (bDetail.getText().equals("Show Details >>")) {
                        bDetail.setText("<< Hide Details");
                        d.setResizeAllowed(true);
                        d.setSize(detailSize[0], detailSize[1]);
                        taDetail.setSize(detailSize[2], detailSize[3]);
                        taDetail.setVisible(true);
                        d.addPropertyChangeListener(new String[]{Dialog.PROPERTY_WIDTH, Dialog.PROPERTY_HEIGHT}, dialogSizePCL);
                    } else if (bDetail.getText().equals("<< Hide Details")) {
                        d.removePropertyChangeListener(dialogSizePCL);
                        bDetail.setText("Show Details >>");
                        d.setResizeAllowed(false);
                        d.setSize(normalWidth, normalHeight);
                        taDetail.setVisible(false);
                    }
                }
            });
            
            d.setVisible(true);
        }
    }
    
    /**
     * Gets the <code>Frame</code> that represents the primary application window.
     * @return the <code>Frame</code> that represents the primary application window.
     */
    public Frame getFrame() {
        if (frame == null) frame = new Frame();
        return frame;
    }
    
    /**
     * Returns the <code>Component</code> that previously had focus in the Application. The <code>Component</code> that
     * previously had focus is the last <code>Component</code> in ANY window that held the focus. This is different from the
     * behavior of the focus property in which their is one <code>Component</code> with focus PER window.
     * @return the <code>Component</code> that previously had focus in the Application, or null if no <code>Component</code> has
     *         had prior focus.
     * @see Component#isFocus()
     * @see Component#setFocus(boolean)
     */
    public Component getPriorFocus() {
        return priorFocus.get();
    }
        
    protected void loadStyleSheet(String styleSheet) {
        XOD props = new XOD();
        String sheet = styleSheet == null ? DEFAULT_STYLE_SHEET : styleSheet;
        if (!sheet.endsWith(".xml")) sheet += "/Style.xml";
        props.execute(getSystemFile(sheet));
        
        compTypeToStyle = new HashMap<Class<? extends Component>, Style>();
        systemColors = new HashMap<String, Color>();
        systemImages = new HashMap<String, String>();

        for (Map.Entry<String, Object> e : props.getObjectMap().entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            
            if (value instanceof Color) {
                Color color = Color.valueOf(name); //Just verify that the specified systemColor is valid;
                if (!color.isSystemColor()) throw new UnsupportedOperationException("You can only override system colors in the style sheet: " + name);
                systemColors.put(color.toString(), (Color)value);
            } else if (value instanceof Style) {
                if (name.startsWith("default")) {
                    if (name.equals("default")) compTypeToStyle.put(null, (Style)value);
                } else if (name.matches(".*?[A-Z].*?")){
                    if (Character.isUpperCase(name.charAt(0)) && name.indexOf('.') == -1) name = "thinwire.ui." + name;
                    
                    try {
                        Class clazz = Class.forName(name);
                        if (clazz.getMethod("getStyle") != null) compTypeToStyle.put((Class<? extends Component>)clazz, (Style)value);
                    } catch (Exception ex) { /*purposely fall through*/ }
                }
            } else {
                throw new UnsupportedOperationException("Unsupported object type in style sheet:" + value.getClass());
            }
        }

        for (Color c : Color.values()) {
            if (c.isSystemColor()) {
                String name = c.toString();
                if (systemColors.get(name) == null) systemColors.put(name, c);
            }
        }
        
        for (Map.Entry<String, String> e : props.getPropertyMap().entrySet()) {
            String key = e.getKey();
            
            if (key.startsWith("image.")) {
                key = key.substring(6).replace('.', '_').toUpperCase();
                systemImages.put(key, e.getValue());
            }
        }
    }
    
    protected String getSystemFile(String value) {        
        if (!value.matches("^\\w{3,}://.*")) {
            if (value != null && !value.matches("^\\w?:?[\\\\|/].*")) value = getBaseFolder() + File.separator + value;
            
            try {
                value = new File(value).getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        return value;
    }
    
    protected Map<Class<? extends Component>, Style> getDefaultStyles() {
        return compTypeToStyle;
    }
    
    protected Map<String, Color> getSystemColors() {
        return systemColors;
    }

    protected Map<String, String> getSystemImages() {
        return systemImages;
    }

    public Style getDefaultStyle(Class<? extends Component> clazz) {
        if (clazz == null) throw new IllegalArgumentException("clazz == null");
        Style style = compTypeToStyle.get(clazz);
        
        if (style == null) {
            List<Class<? extends Component>> lst = new ArrayList<Class<? extends Component>>();
            lst.add(clazz);
            
            do {                                        
                Class<? extends Component> findClazz = lst.remove(0);
                style = compTypeToStyle.get(findClazz);
                
                if (style == null) {
                    Class sc = findClazz.getSuperclass();
                    if (Component.class.isAssignableFrom(sc)) lst.add(sc);
                    
                    for (Class i : findClazz.getInterfaces()) {
                        if (Component.class.isAssignableFrom(i)) lst.add(i);
                    }
                } else {
                    break;
                }
            } while (lst.size() > 0);

            if (style == null) style = compTypeToStyle.get(null);
            compTypeToStyle.put(clazz, style);
        }

        return style;
    }
    
    void setPriorFocus(Component comp) {
        priorFocus = new WeakReference<Component>(comp);    
    }
            
	/**
	 * @return the base folder of the system
	 */
	public abstract String getBaseFolder();
    
    //#IFDEF V1_1_COMPAT    
    /**
     * @return
     * @deprecated there is no replacement for this method, instead use String constant variables.
     */
    public Map<String, String> getFileMap() {
        return fileMap;
    }
    
    /**
     * All keys in the file map must be upper case
     * @param fileMap
     * @deprecated there is no replacement for this method, instead use String constant variables.
     */
    public void setFileMap(Map<String, String> fileMap) {
        this.fileMap = fileMap;
    }

    //#ENDIF
    /**
     * 
     * @param pathname
     * @return
     */
    public File getRelativeFile(String pathname) {
        return new File(appendBaseFolder(pathname));
    }
    
    /**
     * 
     * @param parent
     * @param child
     * @return
     */
    public File getRelativeFile(String parent, String child) {
        return new File(appendBaseFolder(parent), child);
    }
    
    private String appendBaseFolder(String s) {
        String ret = null;

        if (s != null && !s.matches("^\\w?:?[\\\\|/].*")) {
            //#IFDEF V1_1_COMPAT
            //Limit the keys in the map to UPPER-CASE letters or underscores.
            //This lowers the chance of someone using this resourceMap to override a valid
            //file path.  i.e.  map.put("C:\\WORK\\CUSTOMERFILE.DOC", "C:\\TEMP\\HACK.TXT")
            //would make new Resource("C:\\WORK\\CUSTOMERFILE.DOC") return a file reference
            //to "C:\\TEMP\\HACK.TXT" instead of "C:\\WORK\\CUSTOMERFILE.DOC".
            if (s.matches("^[A-Z_]+$")) {            
                if (fileMap != null) {
                    String m = fileMap.get(s);
                
                    if (m != null) {
                        if (m.matches("^\\w?:?[\\\\|/].*"))
                            ret = m;
                        else
                            ret = getBaseFolder() + File.separator + m;
                    }
                }
            }
            //#ENDIF
            if (ret == null) ret = getBaseFolder() + File.separator + s;
        }
        
        ret = (ret == null ? s : ret);
        return ret;
    }
        
    protected Object setPackagePrivateMember(String memberName, Component comp, Object value) {
        if (memberName.equals("renderer")) {
            ((AbstractComponent)comp).setRenderer((Renderer)value);
        } else if (memberName.equals("innerWidth")) {
            ((Frame)comp).setInnerWidth((Integer)value);
        } else if (memberName.equals("innerHeight")) {
            ((Frame)comp).setInnerHeight((Integer)value);
        } else if (memberName.equals("frameSize")) {
            Integer[] size = (Integer[])value;
            ((Frame)comp).sizeChanged(size[0], size[1]);
        } else if (memberName.equals("initDDGBView")) {
            DropDownGridBox.DefaultView v = new DropDownGridBox.DefaultView();
            v.init(null, (GridBox) value);
            return v;
        }
        return null;
    }
    
    protected abstract void captureThread();
    protected abstract void releaseThread();
    protected abstract void showWindow(Window w);
    protected abstract void hideWindow(Window w);
    protected abstract FileChooser.FileInfo getFileInfo();
    
    /**
     * 
     * @param task
     * @param milliseconds
     */
    public abstract void addTimerTask(Runnable task, long milliseconds);
    
    /**
     * 
     * @param task
     * @param milliseconds
     * @param repeat
     */
    public abstract void addTimerTask(Runnable task, long milliseconds, boolean repeat);
    
    /**
     * 
     * @param task
     */
    public abstract void resetTimerTask(Runnable task);
    
    /**
     * 
     * @param task
     */
    public abstract void removeTimerTask(Runnable task);    
}
