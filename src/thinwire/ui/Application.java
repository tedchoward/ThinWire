/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ExceptionEvent;
import thinwire.ui.event.ExceptionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.*;

/**
 * The Application class represents an instance of a ThinWire application.  Methods in this class are
 *  those that directly affect the system and it's environment.
 * @author Joshua J. Gertzen
 */
public abstract class Application {
	private static final Logger log = Logger.getLogger(Application.class.getName());	
    
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
        private Application app;
        
        public AppThread(Application app, String id) {
            super("ThinWire AppThread-" + id);
            this.app = app;
        }
    }
    
    private Map<Local, Object> appLocal = new WeakHashMap<Local, Object>();
    
    public static class Local<T> {
        public void set(T value) {
            Application.current().appLocal.put(this, value);
        }
        
        public T get() {
            Map<Local, Object> map = Application.current().appLocal;
            T value = (T)map.get(this);
            if (value == null && !map.containsKey(this)) map.put(this, value = initialValue());
            return value;
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

        final java.awt.Frame frame = new java.awt.Frame("ThinWire(TM) RIA Ajax Framework v" + getPlatformVersionInfo().get("productVersion"));
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
     * @return the current instance of the application
     */
	public static Application current() {
        Thread t = Thread.currentThread();
        
        if (t instanceof AppThread) {
            return ((AppThread)t).app;
        } else {
            throw new IllegalArgumentException("You may only get the current application instance from a UI thread");
        }
	}
    
    private String baseFolder;
    private List<ExceptionListener> exceptionListeners;
    private EventListenerImpl<PropertyChangeListener> gpcei;
    private WeakReference<Component> priorFocus;
    private Frame frame;
    private Map<String, String> fileMap;    
    private Map<Class<? extends Component>, Style> compTypeToStyle;
    
    protected Application() {
        exceptionListeners = new ArrayList<ExceptionListener>();
        gpcei = new EventListenerImpl<PropertyChangeListener>();        
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
    
    protected void loadStyleSheet(Properties props) {
        try {
            ClassReflector<Background> backgroundReflect = new ClassReflector<Background>(Background.class, "PROPERTY_", "background");
            ClassReflector<Font> fontReflect = new ClassReflector<Font>(Font.class, "PROPERTY_", "font");
            ClassReflector<Border> borderReflect = new ClassReflector<Border>(Border.class, "PROPERTY_", "border");
            ClassReflector<FX> fxReflect = new ClassReflector<FX>(FX.class, "PROPERTY_", "fx");
            
            compTypeToStyle = new HashMap<Class<? extends Component>, Style>();

            Style defaultStyle = new Style();
            compTypeToStyle.put(null, defaultStyle);
            
            for (String key : backgroundReflect.getPropertyNames()) {
                backgroundReflect.setProperty(defaultStyle.getBackground(), key, props.getProperty("default." + key));
            }

            for (String key : fontReflect.getPropertyNames()) {
                fontReflect.setProperty(defaultStyle.getFont(), key, props.getProperty("default." + key));
            }

            for (String key : borderReflect.getPropertyNames()) {
                borderReflect.setProperty(defaultStyle.getBorder(), key, props.getProperty("default." + key));
            }

            for (String key : fxReflect.getPropertyNames()) {
                fxReflect.setProperty(defaultStyle.getFX(), key, props.getProperty("default." + key));
            }
            
            for (Map.Entry<Object, Object> e : props.entrySet()) {
                String key = (String)e.getKey();
                if (key.startsWith("default.")) continue;
                String value = (String)e.getValue();
                int lastIndex = key.lastIndexOf('.');
                String prefix = key.substring(0, lastIndex); 
                key = key.substring(lastIndex + 1);
                Class<? extends Component> clazz = (Class<? extends Component>)Class.forName(prefix);
                Style style = compTypeToStyle.get(clazz);
                if (style == null) compTypeToStyle.put(clazz, style = new Style(defaultStyle));
                
                if (key.startsWith("background")) {
                    backgroundReflect.setProperty(style.getBackground(), key, value);
                } else if (key.startsWith("font")) {
                    fontReflect.setProperty(style.getFont(), key, value);            
                } else if (key.startsWith("border")) {
                    borderReflect.setProperty(style.getBorder(), key, value);            
                } else if (key.startsWith("fx")) {
                    fxReflect.setProperty(style.getFX(), key, value);
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }
    
    public Style getDefaultStyle(Class<? extends Component> clazz) {
        if (clazz == null) throw new IllegalArgumentException("clazz == null");
        Style style = compTypeToStyle.get(clazz);
        
        if (style == null) {
            List<Class> lst = new ArrayList<Class>();
            Class findClazz = clazz; 
            
            do {                                        
                Class sc = findClazz.getSuperclass();
                if (sc != null && Component.class.isAssignableFrom(sc)) lst.add(sc);
                
                for (Class i : findClazz.getInterfaces()) {
                    if (Component.class.isAssignableFrom(i)) lst.add(i);
                }
                
                findClazz = lst.remove(0);
                Style parentStyle = compTypeToStyle.get(findClazz);
                
                if (parentStyle != null) {
                    compTypeToStyle.put(clazz, style = new Style(compTypeToStyle.get(null)));
                    style.copy(parentStyle);
                    break;
                }
            } while (lst.size() > 0);
        }

        if (style == null) compTypeToStyle.put(clazz, style = new Style(compTypeToStyle.get(null)));
        return style;   
    }
    
    void setPriorFocus(Component comp) {
        priorFocus = new WeakReference<Component>(comp);    
    }
            
	/**
	 * @return the base folder of the system
	 */
	public final String getBaseFolder() {
	    return baseFolder;
	}
    
    protected final void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }
        
    /**
     * 
     * @return
     */
    public Map<String, String> getFileMap() {
        return fileMap;
    }
    
    /**
     * All keys in the file map must be upper case
     * @param fileMap
     */
    public void setFileMap(Map<String, String> fileMap) {
        this.fileMap = fileMap;
    }

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
            
            if (ret == null) ret = getBaseFolder() + File.separator + s;
        }
        
        ret = (ret == null ? s : ret);
        return ret;
    }
        
    protected void setPackagePrivateMember(String memberName, Component comp, Object value) {
        if (memberName.equals("renderer")) {
            ((AbstractComponent)comp).setRenderer((Renderer)value);
        } else if (memberName.equals("innerWidth")) {
            ((Frame)comp).setInnerWidth((Integer)value);
        } else if (memberName.equals("innerHeight")) {
            ((Frame)comp).setInnerHeight((Integer)value);
        } else if (memberName.equals("frameSize")) {
            Integer[] size = (Integer[])value;
            ((Frame)comp).sizeChanged(size[0], size[1]);
        }
    }
    
    protected abstract void captureThread();
    protected abstract void releaseThread();
    protected abstract void showWindow(Window w);
    protected abstract void hideWindow(Window w);
    protected abstract List<FileChooser.FileInfo> showFileChooser(boolean showDescription, boolean multiFile);
    
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
