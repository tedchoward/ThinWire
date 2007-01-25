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
package thinwire.render.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpSession;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.ui.*;
import thinwire.ui.FileChooser.FileInfo;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.layout.SplitLayout;
import thinwire.ui.style.*;
import thinwire.util.Grid;
import thinwire.util.ImageInfo;
import thinwire.util.XOD;

/**
 * @author Joshua J. Gertzen
 */
public final class WebApplication extends Application {
    private static final String CLASS_NAME = WebApplication.class.getName();
    private static final String PACKAGE_NAME = WebApplication.class.getPackage().getName();
    private static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\");
    private static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    private static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n");
    private static final String EOL = System.getProperty("line.separator");

    static final Logger log = Logger.getLogger(CLASS_NAME);
    
    private static String[] BUILT_IN_RESOURCES = {
        "Main.js",
        "Class.js",
        "HttpRequest.js",
        "Component.js",
        "Animation.js",
        "BaseBrowserLink.js",
        "BaseCheckRadio.js",
        "BaseContainer.js",
        "BaseRange.js",
        "BaseText.js",
        "BorderImage.js",
        "Button.js",
        "CheckBox.js",
        "Container.js",
        "DateBox.js",
        "Dialog.js",
        "Divider.js",
        "DragHandler.js",
        "DragAndDropHandler.js",
        "DropDown.js",
        "EventManager.js",
        "Frame.js",
        "GridBox.js",
        "Hyperlink.js",
        "Image.js",
        "KeyboardManager.js",
        "Label.js",
        "Menu.js",
        "ProgressBar.js",
        "RadioButton.js",
        "Slider.js",
        "TabFolder.js",
        "TabSheet.js",
        "TextArea.js",
        "TextField.js",
        "Tree.js",
        "WebBrowser.js",
        "class:///" + SplitLayout.class.getName() + "/resources/SplitLayout.js",
        "FileChooser.js",
        "Startup.js",
        "FileUploadPage.html",
    };
    
    static final byte[] MAIN_PAGE;
    
    static {
        String classURL = "class:///" + CLASS_NAME + "/resources/";
        
        for (String res : BUILT_IN_RESOURCES) {
            if (!res.endsWith(".js")) {
                if (!res.startsWith("class:///")) res = classURL + res;
                RemoteFileMap.INSTANCE.add(res, null, Application.getResourceBytes(res));
            }
        }

        try {
            String twLib = loadJSLibrary(classURL);
            //Store the MainPage.html after replacing the JS lib name
            MAIN_PAGE = new String(WebApplication.getResourceBytes(classURL + "MainPage.html"))
                .replaceAll("[$][{]ThinWire[.]js[}]", twLib).getBytes();
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
            throw (RuntimeException)e;
        }
    }
    
    private static String loadJSLibrary(String resURL) {
        try {
            //Write out the library JS
            String twPrefix = Application.getPlatformVersionInfo().get("productVersion");
            log.info("Loading ThinWire(R) RIA Ajax Framework v" + twPrefix);
            twPrefix = "ThinWire_v" + twPrefix;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream os = new GZIPOutputStream(baos);
            
            for (String res : BUILT_IN_RESOURCES) {
                if (res.endsWith(".js")) {
                    if (!res.startsWith("class:///")) res = resURL + res;
                    WebApplication.writeResourceToStream(res, os);
                }
            }

            os.close();
            return RemoteFileMap.INSTANCE.add(null, twPrefix + ".js", baos.toByteArray());
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
            throw (RuntimeException)e;
        }
    }
    
    private static class Timer {
        private Runnable task;
        private long timeout;
        private boolean repeat;
    }
    
    static class EventProcessor extends AppThread {
        private List<WebComponentEvent> queue = new LinkedList<WebComponentEvent>();
        private boolean active;
        private int captureCount;
        private boolean threadCaptured;
        
        EventProcessor(WebApplication app) {
            super(app, app.httpSession.getId());
        }

        public void run() {
            if (log.isLoggable(Level.FINE)) log.fine("entering thread#" + getId() + ":" + getName());
            active = true;
            
            try {
                while (true) {
                    processEvent();
                }
            } catch (InterruptedException e) { /* purposefully do nothing */ }
            
            if (log.isLoggable(Level.FINE)) log.fine("exiting thread#" + getId() + ": " + getName());
        }
        
        public void capture() {
            int currentCaptureCount = ++captureCount;
            if (log.isLoggable(Level.FINE)) log.fine("capture thread#" + getId() + ":" + getName() + " captureCount:" + captureCount);
            threadCaptured = true;

            while (threadCaptured) {
                try {
                    processEvent();
                    if (currentCaptureCount == captureCount) threadCaptured = true;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        public void release() {
            threadCaptured = false;
            captureCount--;
            if (log.isLoggable(Level.FINE)) log.fine("release thread#" + getId() + ":" + getName() + " captureCount:" + captureCount);
        }
        
        public boolean isActive() {
            synchronized (queue) {
                return active;
            }
        }

        public void queue(WebComponentEvent wce) {
            synchronized (queue) {
                if (log.isLoggable(Level.FINEST)) log.finest("queue user action event:" + wce);
                queue.add(wce);
                queue.notify();
            }
        }
        
        private void processEvent() throws InterruptedException {
            WebApplication wapp = (WebApplication)app;
            WebComponentEvent event;

            synchronized (queue) {
                if (queue.size() > 0) {
                    event = queue.remove(0);
                    if (log.isLoggable(Level.FINEST)) log.finest("process user action event:" + event);
                    if (wapp.userActionListener != null) wapp.notifyUserActionReceived(event);

                    if (wapp.playBackOn && !wapp.playBackEventReceived) {
                        wapp.playBackEventReceived = true;
                        wapp.playBackStart = new Date().getTime();
                    }
                } else {
                    active = false;
                    queue.wait();
                    active = true;
                    event = null;
                }
            }

            if (event != null) {
                if (wapp.playBackOn) {
                    wapp.flushClientEvents();
                    if (ApplicationEventManager.SHUTDOWN.equals(event.getName())) wapp.setPlayBackOn(false);
                }
                
                try {
                    WebComponentListener wcl = wapp.getWebComponentListener((Integer) event.getSource());
                    if (wcl != null) wcl.componentChange(event);
                } catch (Exception e) {
                    app.reportException(null, e);
                }
            }
        }
    }
    
    static class ApplicationEventManager implements WebComponentListener {
        static final Integer ID = new Integer(Integer.MAX_VALUE);
        private static final String SHUTDOWN_INSTANCE = "tw_shutdownInstance";
        private static final String INIT = "INIT";
        private static final String STARTUP = "STARTUP";
        private static final String SHUTDOWN = "SHUTDOWN";
        private static final String RUN_TIMER = "RUN_TIMER";
        
        static final class StartupInfo {
            String mainClass;
            String[] args;
            
            StartupInfo(String mainClass, String[] args) {
                this.mainClass = mainClass;
                this.args = args;
            }
        }
        
        static final WebComponentEvent newRunTimerEvent(String timerId) {
            return new WebComponentEvent(ID, RUN_TIMER, timerId);
        }

        static final WebComponentEvent newInitEvent() {
            return new WebComponentEvent(ID, INIT, null);
        }
        
        static final WebComponentEvent newStartEvent(WebApplication app) {
            StartupInfo info = app.startupInfo;
            app.startupInfo = null;
            return new WebComponentEvent(ID, STARTUP, info);
        }
        
        static final WebComponentEvent newShutdownEvent() {
            return new WebComponentEvent(ID, SHUTDOWN, null);
        }
        
        private WebApplication app;
        
        ApplicationEventManager(WebApplication app) {
            this.app = app;
        }

        public void componentChange(WebComponentEvent event) {
            String name = event.getName();

            if (INIT.equals(name)) {
                app.sendStyleInitInfo();
                Frame f = app.getFrame();
                f.setVisible(true);

                //When the frame is set to non-visible, fire a shutdown event
                f.addPropertyChangeListener(Frame.PROPERTY_VISIBLE, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent pce) {
                        if (pce.getNewValue() == Boolean.FALSE) app.eventProcessor.queue(ApplicationEventManager.newShutdownEvent());
                    }
                });
            } else if (STARTUP.equals(name)) {
                StartupInfo info = (StartupInfo)event.getValue();

                try {
                    Class clazz = Class.forName(info.mainClass);
                    clazz.getMethod("main", new Class[] { String[].class }).invoke(clazz, new Object[] { info.args });
                } catch (Exception e) {
                    if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
                    throw (RuntimeException)e;
                }                    
            } else if (SHUTDOWN.equals(name)) {
                if (app.getFrame().isVisible()) {
                    app.getFrame().setVisible(false);
                } else {
                    log.entering(CLASS_NAME, "exit");
                    app.eventProcessor.interrupt();
                    //app.releaseThread(); // ?? The hideWindow does this, doesn't it?

                    // Call the client-side shutdown instance
                    app.clientSideFunctionCall(SHUTDOWN_INSTANCE, 
                            "The application instance has shutdown. Press F5 to restart the application or close the browser to end your session.");

                    if (app.userActionListener != null) app.userActionListener.stop();

                    // Set the execution thread to null,
                    // so that this application instance can get
                    // garbage collected.
                    app.eventProcessor = null;
                    
                    if (app.httpSession.getAttribute("instance") == app) app.httpSession.setAttribute("instance", null);                    
                    log.exiting(CLASS_NAME, "exit");                    
                }
            } else if (RUN_TIMER.equals(name)) {
                String timerId = (String)event.getValue();
                Timer timer = app.timerMap.get(timerId);
                if (timer != null) {
                    timer.task.run();
                    
                    if (timer.repeat) {
                        app.resetTimerTask(timer.task);                            
                    } else {
                        app.removeTimerTask(timer.task);
                    }
                }
            }
        }
    }
    
    private HttpSession httpSession;
    private String baseFolder;
    private String styleSheet;
    private StringBuilder sbClientEvents;
    private Map<String, Timer> timerMap;
    private Map<String, Class<ComponentRenderer>> nameToRenderer;
    private Map<Window, WindowRenderer> windowToRenderer;
    private Map<Integer, WebComponentListener> webComponentListeners;
    private Set<String> clientSideIncludes;
    private Map<Component, Object> renderStateListeners;
    private String[] syncCallResponse = new String[1];
    private boolean processClientEvents;
    private int nextCompId;
    
    //Stress Test Variables.
    private UserActionListener userActionListener;    
    private boolean playBackOn = false;
    private long playBackStart = -1;
    private long playBackDuration = -1;
    private long recordDuration = -1;
    private boolean playBackEventReceived = false;
    //end Stress Test.
    
    EventProcessor eventProcessor;
    ApplicationEventManager.StartupInfo startupInfo;
    Map<Style, String> styleToStyleClass = new HashMap<Style, String>();
    FileInfo[] fileList = new FileInfo[1];
    
    WebApplication(HttpSession httpSession, String baseFolder, String mainClass, String styleSheet, String[] args) {
        this.httpSession = httpSession;
        this.baseFolder = baseFolder;
        this.styleSheet = styleSheet;
        nameToRenderer = new HashMap<String, Class<ComponentRenderer>>();
        windowToRenderer = new HashMap<Window, WindowRenderer>();
        timerMap = new HashMap<String, Timer>();
        sbClientEvents = new StringBuilder(4096);
        sbClientEvents.append('[');
        webComponentListeners = new HashMap<Integer, WebComponentListener>();
        
        setWebComponentListener(ApplicationEventManager.ID, new ApplicationEventManager(this));
        eventProcessor = new EventProcessor(this);
        startupInfo = new ApplicationEventManager.StartupInfo(mainClass, args);
        eventProcessor.queue(ApplicationEventManager.newInitEvent());
        eventProcessor.start();
    }
    
    void sendStyleInitInfo() {
        try {
            loadStyleSheet(styleSheet);
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            
            for (Map.Entry<String, Color> e : getSystemColors().entrySet()) {
                sb.append(e.getKey()).append(":\"").append(e.getValue()).append("\",");
            }
            
            sb.setCharAt(sb.length() - 1, '}');
            clientSideMethodCall("tw_Component", "setSystemColors", sb);

            sb.setLength(0);
            sb.append('{');
            
            for (Map.Entry<String, String> e : getSystemImages().entrySet()) {
                String value = getSystemFile(e.getValue());
                value = RemoteFileMap.INSTANCE.add(value, null, Application.getResourceBytes(value));
                sb.append(e.getKey()).append(":\"").append("%SYSROOT%").append(value).append("\",");
            }
            
            sb.setCharAt(sb.length() - 1, '}');
            clientSideMethodCall("tw_Component", "setSystemImages", sb);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }
    
    Color getSystemColor(String name) {
        return getSystemColors().get(name);
    }
    
    StringBuilder getStyleValue(ComponentRenderer cr, StringBuilder sb, String propertyName, Object value) {
        if (propertyName.equals(Border.PROPERTY_BORDER_SIZE)) {
            sb.append("borderWidth").append(":\"").append(value).append("px");
        } else if (propertyName.equals(Border.PROPERTY_BORDER_TYPE)) {
            sb.append("borderStyle").append(":\"").append(value);
        } else if (propertyName.equals(Font.PROPERTY_FONT_COLOR)) {
            sb.append("color").append(":\"").append(value);
        } else if (propertyName.equals(Font.PROPERTY_FONT_UNDERLINE)) {
            sb.append("textDecoration").append(":\"").append(value == Boolean.TRUE ? "underline" : "none");
        } else if (propertyName.equals(Font.PROPERTY_FONT_ITALIC)) {
            sb.append("fontStyle").append(":\"").append(value == Boolean.TRUE ? "italic" : "normal");
        } else if (propertyName.equals(Font.PROPERTY_FONT_BOLD)) {
            sb.append("fontWeight").append(":\"").append(value == Boolean.TRUE ? "bold" : "normal");
        } else if (propertyName.equals(Font.PROPERTY_FONT_SIZE)) {
            sb.append(propertyName).append(":\"").append(value).append("pt");
        } else if (propertyName.equals(Background.PROPERTY_BACKGROUND_IMAGE)) {
            sb.append(propertyName).append(":\"").append(cr.getQualifiedURL((String)value));
        } else {
            sb.append(propertyName).append(":\"");
            
            if (value instanceof Color) {
                Color color = (Color)value;
                if (color.isSystemColor()) color = getSystemColors().get(color.toString());
                sb.append(color.toRGBString());
            } else if (value instanceof Background.Repeat) {
                switch ((Background.Repeat)value) {
                    case BOTH: sb.append("repeat"); break;
                    case X: sb.append("repeat-x"); break;
                    case Y: sb.append("repeat-y"); break;
                    default: sb.append("no-repeat"); break;
                }
            } else if (value instanceof ImageInfo) {
                ImageInfo ii = (ImageInfo)value;
                String name = ii.getName();
                
                if (name.length() > 0) {
                    sb.append(cr.getQualifiedURL(name));
    
                    if (propertyName.equals(Border.PROPERTY_BORDER_IMAGE)) {
                        sb.append(',').append(ii.getWidth()).append(',').append(ii.getHeight());
                    }
                }
            } else {
                sb.append(value);
            }
        }
        
        sb.append("\",");
        return sb;
    }
    
    StringBuilder getStyleValues(ComponentRenderer cr, StringBuilder sb, Style s, Style ds) {
        Background background = s.getBackground();
        Color backgroundColor = background.getColor();
        String backgroundImage = background.getImage();
        Background.Repeat backgroundRepeat = background.getRepeat();
        Background.Position backgroundPosition = background.getPosition();
        
        Border border = s.getBorder();
        Border.Type borderType = border.getType();
        Color borderColor = border.getColor();
        Integer borderSize = border.getSize();
        String borderImage = border.getImage();
        
        Font font = s.getFont();
        Font.Family fontFamily = font.getFamily();
        Double fontSize = font.getSize();
        Color fontColor = font.getColor();
        Boolean fontBold = font.isBold();
        Boolean fontItalic = font.isItalic();
        Boolean fontUnderline = font.isUnderline();
        Boolean fontStrike = font.isStrike();
        
        if (ds != null) {
            background = ds.getBackground();
            if (backgroundColor.equals(background.getColor())) backgroundColor = null;
            if (backgroundImage.equals(background.getImage())) backgroundImage = null;
            if (backgroundRepeat.equals(background.getRepeat())) backgroundRepeat = null;
            if (backgroundPosition.equals(background.getPosition())) backgroundPosition = null;
            
            border = ds.getBorder();
            if (borderType.equals(border.getType())) borderType = null;
            if (borderColor.equals(border.getColor())) borderColor = null;
            if (borderSize.equals(border.getSize())) borderSize = null;
            if (borderImage.equals(border.getImage())) borderImage = null;
            
            font = ds.getFont();
            if (fontFamily.equals(font.getFamily()))
            if (fontSize.equals(font.getSize())) fontSize = null;
            if (fontColor.equals(font.getColor())) fontColor = null;
            if (fontBold.equals(font.isBold())) fontBold = null;
            if (fontItalic.equals(font.isItalic())) fontItalic = null;
            if (fontStrike.equals(font.isStrike())) fontStrike = null;
        }
        
        sb.append("{");
        
        if (backgroundColor != null) getStyleValue(cr, sb, Background.PROPERTY_BACKGROUND_COLOR, backgroundColor);
        if (backgroundImage != null) getStyleValue(cr, sb, Background.PROPERTY_BACKGROUND_IMAGE, backgroundImage);
        if (backgroundRepeat != null) getStyleValue(cr, sb, Background.PROPERTY_BACKGROUND_REPEAT, backgroundRepeat);
        if (backgroundPosition != null) getStyleValue(cr, sb, Background.PROPERTY_BACKGROUND_POSITION, backgroundPosition);
        
        if (borderType != null && borderType != Border.Type.IMAGE) {
            if (borderType == Border.Type.NONE) {
                borderType = Border.Type.SOLID;
                borderColor = backgroundColor;
            }

            getStyleValue(cr, sb, "borderType", borderType);
        }
        
        if (borderImage != null) getStyleValue(cr, sb, Border.PROPERTY_BORDER_IMAGE, s.getBorder().getImageInfo());
        if (borderColor != null) getStyleValue(cr, sb, Border.PROPERTY_BORDER_COLOR, borderColor);
        if (borderSize != null) getStyleValue(cr, sb, Border.PROPERTY_BORDER_SIZE, borderSize);

        if (fontFamily != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_FAMILY, fontFamily);
        if (fontSize != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_SIZE, fontSize);
        if (fontColor != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_COLOR, fontColor);
        if (fontBold != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_BOLD, fontBold); 
        if (fontItalic != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_ITALIC, fontItalic); 
        if (fontUnderline != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_UNDERLINE, fontUnderline); 
        if (fontStrike != null) getStyleValue(cr, sb, Font.PROPERTY_FONT_STRIKE, fontStrike); 
        
        if (sb.length() > 1) {
            sb.setCharAt(sb.length() - 1, '}');
        } else {
            sb.setLength(0);
        }
        
        return sb;
    }
    
    Integer getNextComponentId() {
        nextCompId = nextCompId == Integer.MAX_VALUE ? 1 : nextCompId + 1;
        return new Integer(nextCompId);
    }

    ComponentRenderer getRenderer(Component comp) {
        Class compClass = comp.getClass();        
        String className = compClass.getName();
        Class<ComponentRenderer> renderClazz = nameToRenderer.get(className);
        
        if (renderClazz == null) {
            Style defaultStyle = getDefaultStyle(compClass);
            String compClassName = className;
            List<Class> lst = new ArrayList<Class>();
            lst.add(compClass);
            
            do {
                compClass = lst.remove(0);
                className = compClass.getName();
                String qualClassName = PACKAGE_NAME + '.' + className.substring(className.lastIndexOf('.') + 1) + "Renderer";
                
                try {
                    renderClazz = (Class)Class.forName(qualClassName);
                    nameToRenderer.put(compClassName, renderClazz);
                    break;
                } catch (ClassNotFoundException e) {
                    //We'll continue trying until no classes in the hierarchy are left.
                }
                
                Class sc = compClass.getSuperclass();
                if (Component.class.isAssignableFrom(sc)) lst.add(sc);
                
                for (Class i : compClass.getInterfaces()) {
                    if (Component.class.isAssignableFrom(i)) lst.add(i);
                }
            } while (lst.size() > 0);
        }
        
        if (renderClazz != null) {
            try {
                return (ComponentRenderer)renderClazz.newInstance();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("illegal access while trying to access " + renderClazz.getName(), e);
            } catch (InstantiationException e) {
                throw new RuntimeException(renderClazz.getName() + " could not be instantiated, using default renderer instead", e);
            }
        }
                
        throw new RuntimeException("Renderer for component class '" + comp.getClass() + "' not found");        
    }

    void notifySyncCallResponse(String response) {
        if (response == null) response = "";

        synchronized (syncCallResponse) {
            syncCallResponse[0] = response;
            syncCallResponse.notify();
        }
    }
    
    public void clientSideIncludeFile(String localName) {
        if (clientSideIncludes == null) {            
            clientSideIncludes = new HashSet<String>(3);
        } else if (clientSideIncludes.contains(localName)) {
            return;
        }
        
        if (localName == null || localName.trim().length() == 0) throw new IllegalArgumentException("localName == null || localName.trim().length() == 0");
        if (!localName.startsWith("class:///")) localName = this.getRelativeFile(localName).getAbsolutePath();
        String remoteName = RemoteFileMap.INSTANCE.add(localName);
        clientSideFunctionCallWaitForReturn("tw_include", remoteName);
        clientSideIncludes.add(localName);
    }
    
    public void clientSideFunctionCall(String functionName, Object... args) {
        clientSideCallImpl(false, null, functionName, args);
    }

    public String clientSideFunctionCallWaitForReturn(String functionName, Object... args) {
        return clientSideCallImpl(true, null, functionName, args);
    }
    
    public void clientSideMethodCall(String objectName, String methodName, Object... args) {
        clientSideCallImpl(false, objectName, methodName, args);
    }
    
    public String clientSideMethodCallWaitForReturn(String objectName, String methodName, Object... args) {
        return clientSideCallImpl(true, objectName, methodName, args);
    }

    public void clientSideMethodCall(Integer componentId, String methodName, Object... args) {
        clientSideCallImpl(false, componentId, methodName, args);   
    }
    
    public String clientSideMethodCallWaitForReturn(Integer componentId, String methodName, Object... args) {
        return clientSideCallImpl(true, componentId, methodName, args);   
    }
    
    public void addRenderStateListener(Component comp, RenderStateListener r) {
        Integer id = getComponentId(comp);

        if (id == null) {
            if (renderStateListeners == null) renderStateListeners = new WeakHashMap<Component, Object>();            
            Object o = renderStateListeners.get(comp);            

            if (o instanceof RenderStateListener) {
                if (o != r) {
                    Set<RenderStateListener> l = new HashSet<RenderStateListener>(3);
                    l.add((RenderStateListener)o);
                    l.add(r);
                    renderStateListeners.put(comp, l);
                }
            } else if (o instanceof Set) {
                ((Set)o).add(r);
            } else {
                renderStateListeners.put(comp, r);
            }
        } else {        
            r.renderStateChange(new RenderStateEvent(comp, id));
        }
    }
    
    public void removeRenderStateListener(Component comp, RenderStateListener r) {
        if (renderStateListeners != null) {
            Object o = renderStateListeners.get(comp);            

            if (o instanceof RenderStateListener) {
                if (o == r) renderStateListeners.remove(comp);
            } else if (o instanceof List) {
                ((List)o).remove(r);
            }
        }
    }
    
    void flushRenderCallbacks(Component comp, Integer id) {
        if (renderStateListeners == null) return;
        Object o = renderStateListeners.get(comp);        
        if (o == null) return;
        
        RenderStateEvent ev = new RenderStateEvent(comp, id);
        
        if (o instanceof RenderStateListener) {
            ((RenderStateListener)o).renderStateChange(ev);
        } else {        
            for (RenderStateListener r : ((Set<RenderStateListener>)o)) {
                r.renderStateChange(ev);
            }
        }
    }
    
    static void encodeObject(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof Integer) {
            sb.append(String.valueOf(((Integer) o).intValue()));
        } else if (o instanceof Number) {
            sb.append(String.valueOf(((Number) o).doubleValue()));
        } else if (o instanceof Boolean) {
            sb.append(String.valueOf(((Boolean) o).booleanValue()));
        } else if (o instanceof StringBuilder) {
            sb.append(o.toString());
        } else {
            String s = o.toString();
            s = REGEX_DOUBLE_SLASH.matcher(s).replaceAll("\\\\\\\\");
            s = REGEX_DOUBLE_QUOTE.matcher(s).replaceAll("\\\\\"");
            s = REGEX_CRLF.matcher(s).replaceAll("\\\\r\\\\n");
            sb.append('\"').append(s).append('\"');
        }        
    }
    
    private String clientSideCallImpl(boolean sync, Object objectId, String name, Object[] args) {
        StringBuilder sb = sbClientEvents;
        String ret = null;

        synchronized (sb) {
            processClientEvents = true;
            sb.append("{m:\"").append(name).append('\"');            
            
            if (objectId != null) {
                if (objectId instanceof Integer) {
                    sb.append(",i:").append(objectId);
                } else {
                    sb.append(",n:").append((String)objectId);
                }
            }

            if (args != null && args.length > 0) {
                sb.append(",a:[");

                for (int i = 0, cnt = args.length; i < cnt; i++) {
                    encodeObject(sb, args[i]);
                    sb.append(',');
                }

                sb.setCharAt(sb.length() - 1, ']');
            } else
                sb.append(",a:[]");

            if (sync) {
                sb.append(",s:1},");
                processClientEvents = true;
                sb.notify();
            } else {
                sb.append("},");

                if (sb.length() >= 1024) {
                    /*
                    //Slow things down if the buffer gets this big.
                    if (sb.length() >= 32768) {
                        int count = 50;
                        
                        while (--count >= 0 && sb.length() >= 1024) {
                            try {
                                sb.wait(100);
                            } catch (InterruptedException e) { }
                        }
                    }
                    */
                    processClientEvents = true;
                    sb.notify();
                } else {
                    processClientEvents = false;
                }

                ret = null;
            }
        }

        if (sync) {
            synchronized (syncCallResponse) {
                if (syncCallResponse[0] == null) {
                    try {
                        long beforeWait = System.currentTimeMillis();
                        syncCallResponse.wait(120000);
                        
                        if (syncCallResponse[0] == null) {
                            long afterWait = System.currentTimeMillis();
                            StringBuilder sbscr = new StringBuilder();
                            sbscr.append("sendClientEvent did not respond within 120 seconds, methodName=");
                            sbscr.append(name);
                            sbscr.append(",timeBeforeWait=");
                            sbscr.append(beforeWait);
                            sbscr.append(",timeAfterWait=");
                            sbscr.append(afterWait);
                            sbscr.append(",argCount=");
                            sbscr.append(args.length);
                            sbscr.append(",argValues=");
                            
                            for (Object arg : args) {                                
                                sbscr.append('"').append(arg.toString()).append("\",");
                            }
                                                        
                            throw new IllegalStateException(sbscr.toString());
                        }
                    } catch (InterruptedException e) {
                        log.log(Level.SEVERE, null, e);
                    }
                }

                ret = syncCallResponse[0];
                syncCallResponse[0] = null;
            }
        }

        return ret;
    }
        
    
    public Integer getComponentId(Component comp) {
        Object w = comp;
        
        while (w != null && !(w instanceof Window)) {
            if (w instanceof Grid.Row) {
                w = ((Grid.Row)w).getParent();
            } else {
                w  = ((Component)w).getParent();
            }
        }
        
        if (w != null) {
            WindowRenderer wr = windowToRenderer.get(w);
            return wr == null ? null : wr.getComponentId(comp);
        } else {
            return null;
        }
    }
    
    public Component getComponentFromId(Integer id) {
        return ((ComponentRenderer)getWebComponentListener(id)).comp;
    }

    String getClientEvents() {
        String s = null;

        synchronized (sbClientEvents) {
            try {
                while (true) {
                    if (processClientEvents) {
                        if (eventProcessor.isActive()) sbClientEvents.insert(1, "{m:\"sendGetEvents\",n:tw_em,a:[]},");
                        int length = sbClientEvents.length();

                        if (length > 1) {
                            sbClientEvents.setCharAt(length - 1, ']');
                            s = sbClientEvents.substring(0, length);
                            sbClientEvents.setLength(0);
                            sbClientEvents.append('[');
                        }

                        processClientEvents = false;
                        break;
                    } else {
                        processClientEvents = true;
                        sbClientEvents.wait(100);
                    }
                }
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, null, e);
            }
        }

        return s;
    }

    void setWebComponentListener(Integer compId, WebComponentListener listener) {
        synchronized (webComponentListeners) {
            if (listener == null)
                webComponentListeners.remove(compId);
            else
                webComponentListeners.put(compId, listener);
        }
    }

    WebComponentListener getWebComponentListener(Integer compId) {
        synchronized (webComponentListeners) {
            return webComponentListeners.get(compId);
        }
    }

    public String getBaseFolder() {
        return baseFolder;
    }
    
    protected void captureThread() {
        eventProcessor.capture();
    }

    protected void releaseThread() {
        eventProcessor.release();
    }

    protected void showWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.get(w);
        if (wr != null) throw new IllegalStateException("A window cannot be set to visible while it is already visible");
        windowToRenderer.put(w, wr = (WindowRenderer) getRenderer(w));
        wr.ai = this;
        
        if (wr instanceof DialogRenderer) {
            wr.render(wr, w, windowToRenderer.get(getFrame()));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            
            for (Map.Entry<Class<? extends Component>, Style> e : getDefaultStyles().entrySet()) {
                Class<? extends Component> clazz = e.getKey();
                
                if (clazz != null) {
                    String styleClass = ComponentRenderer.getSimpleClassName(clazz);
                    Style style = e.getValue();
                    styleToStyleClass.put(style, styleClass);
                    sb.append(styleClass).append(":");
                    getStyleValues(wr, sb, style, null);
                    sb.append(',');
                }
            }
            
            sb.setCharAt(sb.length() - 1, '}');
            clientSideMethodCall("tw_Component", "setDefaultStyles", sb);
            wr.render(wr, w, null);
        }
    }

    protected void hideWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.remove(w);
        if (wr == null) throw new IllegalStateException("Cannot close a window that has not been set to visible");
        if (log.isLoggable(Level.FINE)) log.fine("closing window with id:" + wr.id);
        wr.destroy();
    }

    WindowRenderer getWindowRenderer(Window w) {
        return (WindowRenderer) windowToRenderer.get(w);
    }
    
    protected FileInfo getFileInfo() {
        synchronized(fileList) {
            while (fileList[0] == null) {
                try {
                    fileList.wait();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            FileChooser.FileInfo fileInfo = fileList[0];
            fileList[0] = null;
            return fileInfo;
        }
    }

    public void addTimerTask(Runnable task, long timeout) {
        addTimerTask(task, timeout, false);
    }

    public void addTimerTask(Runnable task, long timeout, boolean repeat) {
        String timerId = String.valueOf(System.identityHashCode(task));
        
        if (timerMap.containsKey(timerId)) {
            resetTimerTask(task);
        } else {
            Timer timer = new Timer();
            timer.task = task;
            timer.timeout = timeout;
            timer.repeat = repeat;
            timerMap.put(timerId, timer);
            clientSideFunctionCall("tw_addTimerTask", timerId, timeout);
        }
    }

    public void resetTimerTask(Runnable task) {
        String timerId = String.valueOf(System.identityHashCode(task));
        Timer timer = timerMap.get(timerId);        
        if (timer != null) clientSideFunctionCall("tw_addTimerTask", timerId, timer.timeout);
    }

    public void removeTimerTask(Runnable task) {
        String timerId = String.valueOf(System.identityHashCode(task));
        clientSideFunctionCall("tw_removeTimerTask", timerId);
        timerMap.remove(timerId);
    }

    protected Object setPackagePrivateMember(String memberName, Component comp, Object value) {
        return super.setPackagePrivateMember(memberName, comp, value);
    }    

    public void setUserActionListener(UserActionListener listener) {
		this.userActionListener = listener;
	}

	private void notifyUserActionReceived(WebComponentEvent evt) {
		UserActionEvent uae = new UserActionEvent(evt);
		this.userActionListener.actionReceived(uae);
	}
    
    protected void finalize() {
        log.log(Level.FINER, "finalizing app " + this.httpSession.getId());
    }

	public void setPlayBackOn(boolean playBackOn){
		this.playBackOn = playBackOn;
		if (!this.playBackOn){
			this.endPlayBack();
		}
	}
	
	private void flushClientEvents() {
		synchronized (sbClientEvents) {
			sbClientEvents.setLength(0);
			sbClientEvents.append('[');
		}
	}
	
	private void endPlayBack(){
		log.entering("ThinWireApplication", "endPlayBack");
		this.playBackDuration = new Date().getTime() - this.playBackStart;
        StringBuilder sb = new StringBuilder(EOL + EOL);
		sb.append(Thread.currentThread().getName()
				+ " Playback Statistics" + EOL);
		sb.append("-----------------------------------------------------" + EOL);
		sb.append("Duration of recording session:  " + this.recordDuration + EOL);
		sb.append(" Duration of playback session:  " + this.playBackDuration + EOL);
		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(2);
		df.setMinimumIntegerDigits(1);
		double drecord = new Long(this.recordDuration).doubleValue();
		double dplay = new Long(this.playBackDuration).doubleValue();
		double pctChange = (((dplay/drecord) - 1) * 100);
		sb.append("                     % change:  " + df.format(pctChange)  + EOL + EOL);
		log.info(sb.toString());	
		log.exiting("ThinWireApplication", "endPlayBack");
	}

	public void setRecordDuration(long recordDuration) {
		this.recordDuration = recordDuration;
	}
	
}
