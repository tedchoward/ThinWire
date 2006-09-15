/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ExceptionEvent;
import thinwire.ui.event.ExceptionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

/**
 * The Application class represents an instance of a ThinWire application.  Methods in this class are
 *  those that directly affect the system and it's environment.
 * @author Joshua J. Gertzen
 */
public abstract class Application {
	private static final Logger log = Logger.getLogger(Application.class.getName());	
	private static final Map<Thread, Application> instanceForThread = Collections.synchronizedMap(new HashMap<Thread, Application>());
    
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
    public static void main(String[] args) {        
        StringBuffer sb = new StringBuffer();
        
        for (Map.Entry<String, String> e : getPlatformVersionInfo().entrySet()) {
            sb.append(e.getKey() + "=" + e.getValue() + "\n");
        }
                
        final java.awt.Frame frame = new java.awt.Frame(Application.class.getName() + ".getPlatformVersionInfo()");
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
		return (Application)instanceForThread.get(Thread.currentThread());
	}
    
    private String baseFolder;
    private Thread executionThread;
    private List<ExceptionListener> exceptionListeners;
    private EventListenerImpl<PropertyChangeListener> gpcei;
    private WeakReference<Component> priorFocus;
    private Frame frame;
    private Map<String, String> fileMap;    
    
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
	
	protected final void setExecutionThread(Thread executionThread) {
        if (this.executionThread != null)
        instanceForThread.remove(this.executionThread);        
        this.executionThread = executionThread;        
        if (executionThread != null) instanceForThread.put(executionThread, this);
	}
    
	protected final Thread getExecutionThread() {
	    return executionThread;
	}
    
    protected static final Application[] getApplications() {
        synchronized (instanceForThread) {
            Collection<Application> coll = instanceForThread.values();
            Application[] apps = coll.toArray(new Application[coll.size()]);
            return apps;
        }
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
