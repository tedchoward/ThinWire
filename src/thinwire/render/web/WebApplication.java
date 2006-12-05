/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import thinwire.ui.layout.SplitLayout;
import thinwire.ui.style.*;
import thinwire.util.Grid;
import thinwire.util.ImageInfo;
import thinwire.util.XOD;

/**
 * @author Joshua J. Gertzen
 */
public final class WebApplication extends Application {
    private static final String SHUTDOWN_INSTANCE = "tw_shutdownInstance";
    private static final String CLASS_NAME = WebApplication.class.getName();
    static final Logger log = Logger.getLogger(CLASS_NAME);
    private static final String PACKAGE_NAME = WebApplication.class.getPackage().getName();
    private static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\");
    private static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    private static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n");
    private static final String EOL = System.getProperty("line.separator");
    private static final long MINUTE = 60 * 1000;
    private static final long INSTANCE_TIMEOUT = 10 * MINUTE;
    private static final long INSTANCE_KEEP_ALIVE_CYCLE = INSTANCE_TIMEOUT - MINUTE;
    private static final long INSTANCE_MONITOR_THREAD_CYCLE = INSTANCE_TIMEOUT / 2;
    private static final Set<WebApplication> allApps = new HashSet<WebApplication>();
    private static Thread instanceMonitorThread;
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
        "Startup.js",
        "FileUploadPage.html",
        "treeEmpty.png",
        "treeExpand.png",
        "treeExpandBottom.png",
        "treeExpandBottomTop.png",
        "treeExpandTop.png",
        "treeLeaf.png",
        "treeLeafBottom.png",
        "treeLeafBottomTop.png",
        "treeLeafTop.png",
        "treeStraight.png",
        "treeUnexpand.png",
        "treeUnexpandBottom.png",
        "treeUnexpandBottomTop.png",
        "treeUnexpandTop.png",
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
            String twPrefix = "ThinWire_v" + Application.getPlatformVersionInfo().get("productVersion");
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
        
    private static final Runnable instanceMonitorRunnable = new Runnable() {
        public void run() {
            log.fine("Starting instance monitoring thread");
            WebApplication[] apps;
            
            synchronized (allApps) {
                apps = allApps.toArray(new WebApplication[allApps.size()]);
            }

            do {
                try {
                    Thread.sleep(INSTANCE_MONITOR_THREAD_CYCLE);
                } catch (Exception e) {
                    throw new RuntimeException(e);                    
                }
                
                for (WebApplication twapp : apps) {
                    long currentTime = System.currentTimeMillis();
                    
                    //If more than two minutes have passed shutdown app
                    if (currentTime - twapp.lastClientRequestTime.longValue() > INSTANCE_TIMEOUT) {
                        log.fine("Shutting down application instance " + twapp.id + " due to inactivity");
                        twapp.queueWebComponentEvent(new WebComponentEvent(WebApplication.APPEVENT_ID, WebApplication.APPEVENT_SHUTDOWN, null));
                        
                        try {
                            //Attempt to join thread for 10 seconds, if this fails, forcefully kill the thread.
                            if (twapp.appThread != null) {                                
                                twapp.appThread.join(10000);
                                
                                if (twapp.appThread.isAlive()) {
                                    log.fine("Forcefully stopping application instance " + twapp.id + ", thread did not respond to join");
                                    twapp.appThread.stop();
                                    
                                    synchronized (allApps) {
                                        allApps.remove(twapp);
                                    }
                                    
                                    twapp.appThread = null;
                                }                                
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
                synchronized (allApps) {
                    apps = allApps.toArray(new WebApplication[allApps.size()]);
                }
            } while (apps.length > 0);
            
            log.fine("Finishing instance monitoring thread");
        }
    };
    
    private final String id;

    Map<String, Color> systemColors;

    private StringBuilder sbClientEvents;
    private List<WebComponentEvent> eventQueue;
    private Map<String, Timer> timerMap;
    private Map<String, Class<ComponentRenderer>> nameToRenderer;
    private Map<Window, WindowRenderer> windowToRenderer;
    private Map<Integer, WebComponentListener> webComponentListeners;
    private Set<String> clientSideIncludes;
    private Map<Component, Object> renderCallbacks;
    Map<Style, String> styleToStyleClass;
    private String[] syncCallResponse = new String[1];
    private boolean threadCaptured;
    private boolean threadWaiting;
    private boolean processClientEvents;
    private int nextCompId;
    private int captureCount;
    private Long lastClientRequestTime;
    private AppThread appThread;
    
    FileInfo[] fileList = new FileInfo[1];
	
	//Stress Test Variables.
	private UserActionListener userActionListener;    
	private boolean playBackOn = false;
	private long playBackStart = -1;
	private long playBackDuration = -1;
	private long recordDuration = -1;
	private boolean playBackEventReceived = false;
	//end Stress Test.

    static public final Integer APPEVENT_ID = new Integer(Integer.MAX_VALUE);
    static public final String APPEVENT_SHUTDOWN = "SHUTDOWN";
    static final String APPEVENT_FILEUPLOAD_COMPLETE = "FILEUPLOAD_COMPLETE";
    static final String APPEVENT_RUN_TIMER = "RUN_TIMER";

    private static class ClientSideScriptException extends RuntimeException {
        private ClientSideScriptException(String message) {
            super(message);
        }
    }
    
    private static class Timer {
        private Runnable task;
        private long timeout;
        private boolean repeat;
    }

    private static final String DEFAULT_STYLE_SHEET = "class:///" + Application.class.getName() + "/resources/DefaultStyle.xml";
    
    WebApplication(final WebServlet servlet, final HttpSession httpSession, final String mainClass, String styleSheet, final String[] args) {
        nameToRenderer = new HashMap<String, Class<ComponentRenderer>>();
        windowToRenderer = new HashMap<Window, WindowRenderer>();
        eventQueue = new ArrayList<WebComponentEvent>();
        timerMap = new HashMap<String, Timer>();
        sbClientEvents = new StringBuilder(4096);
        sbClientEvents.append('[');
        webComponentListeners = new HashMap<Integer, WebComponentListener>();        
        id = httpSession.getId();
        setBaseFolder(servlet.getServletContext().getRealPath(""));

        setWebComponentListener(APPEVENT_ID, new WebComponentListener() {
            public void componentChange(WebComponentEvent event) {
                String name = event.getName();

                if (APPEVENT_SHUTDOWN.equals(name)) {
                    getFrame().setVisible(false);
                } else if (APPEVENT_RUN_TIMER.equals(name)) {
                    String timerId = (String)event.getValue();
                    Timer timer = timerMap.get(timerId);
                    if (timer != null) {
                        timer.task.run();
                        
                        if (timer.repeat) {
                            resetTimerTask(timer.task);                            
                        } else {
                            removeTimerTask(timer.task);
                        }
                    }
                }
            }
        });
        
        appThread = new AppThread(this, id) {
            public void run() {
                try {
                    //set the frame to visible and then wait for the 
                    //frame size information and any other initial state
                    //events to return from the client.  The process those
                    //events and call the entry point for the application. 
                    synchronized (eventQueue) {
                        getFrame().setVisible(true);
                        
                        do {
                            eventQueue.wait();
                            WebComponentEvent event = eventQueue.remove(0);
                            
                            if (event != null) {
                                WebComponentListener wcl = getWebComponentListener((Integer) event.getSource());
                                if (wcl != null) wcl.componentChange(event);
                            }                            
                        } while (eventQueue.size() > 0);
                    }
                    
                    try {
                        Class clazz = Class.forName(mainClass);
                        clazz.getMethod("main", new Class[] { String[].class }).invoke(clazz, new Object[] { args });
                    } catch (Exception e) {
                        if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
                        throw (RuntimeException)e;
                    }                    
                    
                    //If the main method terminates but the frame is still visible, then wait for it to be closed.
                    while (getFrame().isVisible()) {
                        getFrame().setWaitForWindow(true);
                    }                                       
                                        
                    log.entering(WebApplication.class.getName(), "exit");
                    releaseThread(); // ?? The hideWindow does this, doesn't it?

                    // Call the client-side shutdown instance
                    clientSideFunctionCall(SHUTDOWN_INSTANCE, 
                            "The application instance has shutdown. Press F5 to restart the application or close the browser to end your session.");

                    if (userActionListener != null){
                    	userActionListener.stop();
                    }                    

                    // Set the execution thread to null,
                    // so that this application instance can get
                    // garbage collected.
                    
                    synchronized (allApps) {
                        allApps.remove(WebApplication.this);
                    }
                    
                    WebApplication.this.appThread = null;
                    
                    if (httpSession.getAttribute("instance") == WebApplication.this) httpSession.setAttribute("instance", null);                    
                    log.exiting(WebApplication.class.getName(), "exit");                    
                } catch (Exception e) {
                    reportException(null, e);
                }
            }
        };
        
        synchronized (allApps) {
            allApps.add(this);
        }
        
        //Guarantee communication back and forth to the client every 60 seconds.  This causes an update
        //to the lastClientRequestTime and therefore prevents the app instance from being shutdown.
        //TODO: Make this feature and it's time configurable.
        this.addTimerTask(new Runnable() {
            public void run() {
                lastClientRequestTime = new Long(System.currentTimeMillis());
            }
        }, INSTANCE_KEEP_ALIVE_CYCLE, true);

        lastClientRequestTime = new Long(System.currentTimeMillis());
        
        synchronized (instanceMonitorRunnable) {
            if (instanceMonitorThread == null || !instanceMonitorThread.isAlive()) {
                instanceMonitorThread = new Thread(instanceMonitorRunnable, "ThinWire Instance Monitor Thread");
                instanceMonitorThread.setPriority(Thread.MIN_PRIORITY);
                instanceMonitorThread.start();
            }
        }

        try {
            XOD styleDef = new XOD();
            String sheet = styleSheet == null ? DEFAULT_STYLE_SHEET : styleSheet;
            if (!sheet.endsWith(".xml")) sheet += "/Style.xml";
            styleDef.execute(sheet);
            loadStyleSheet(styleDef);
            systemColors = getSystemColors();
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            
            for (Map.Entry<String, Color> e : systemColors.entrySet()) {
                sb.append(e.getKey()).append(":\"").append(e.getValue()).append("\",");
            }
            
            sb.setCharAt(sb.length() - 1, '}');
            clientSideMethodCall("tw_Component", "setSystemColors", sb);

            sb.setLength(0);
            sb.append('{');
            
            for (Map.Entry<String, String> e : getSystemImages().entrySet()) {
                String value = e.getValue();
                
                if (!value.matches("^\\w{3,}://.*")) {
                    if (value != null && !value.matches("^\\w?:?[\\\\|/].*")) value = getBaseFolder() + File.separator + value;   
                    value = new File(value).getCanonicalPath();
                }
                
                value = RemoteFileMap.INSTANCE.add(value, null, Application.getResourceBytes(value));
                sb.append(e.getKey()).append(":\"").append("%SYSROOT%").append(value).append("\",");
            }
            
            sb.setCharAt(sb.length() - 1, '}');
            clientSideMethodCall("tw_Component", "setSystemImages", sb);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
        
        appThread.start();
    }
    
    Integer getNextComponentId() {
        nextCompId = nextCompId == Integer.MAX_VALUE ? 1 : nextCompId + 1;
        return new Integer(nextCompId);
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
                if (color.isSystemColor()) color = systemColors.get(color.toString());
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
    
    ComponentRenderer getRenderer(Component comp) {
        Class compClass = comp.getClass();        
        String className = compClass.getName();
        Class<ComponentRenderer> renderClazz = nameToRenderer.get(className);
        
        if (renderClazz == null) {
            Style defaultStyle = getDefaultStyle(compClass);
            String compClassName = className;
            List<Class> lst = new ArrayList<Class>();
            lst.add(compClass);
            
            while (lst.size() > 0) {
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
            }                
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
    
    public void invokeAfterRendered(Component comp, RenderStateListener r) {
        Integer id = getComponentId(comp);

        if (id == null) {
            if (renderCallbacks == null) renderCallbacks = new WeakHashMap<Component, Object>();            
            Object o = renderCallbacks.get(comp);            

            if (o instanceof RenderStateListener) {
                List l = new ArrayList<RenderStateListener>(3);
                l.add(o);
                l.add(r);
                renderCallbacks.put(comp, l);
            } else {
                if (o == null) renderCallbacks.put(comp, o = new ArrayList<RenderStateListener>(3));                
                ((List)o).add(r);                
            }
        } else {        
            r.renderStateChange(new RenderStateEvent(comp, id));
        }
    }
    
    void flushRenderCallbacks(Component comp, Integer id) {
        if (renderCallbacks == null) return;
        Object o = renderCallbacks.remove(comp);        
        if (o == null) return;
        
        RenderStateEvent ev = new RenderStateEvent(comp, id);
        
        if (o instanceof RenderStateListener) {
            ((RenderStateListener)o).renderStateChange(ev);
        } else {        
            for (RenderStateListener r : ((List<RenderStateListener>)o)) {
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

                if (sb.length() >= 1000) {
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
                        synchronized (eventQueue) {
                            if (!threadWaiting) {
                                sbClientEvents.insert(1, "{m:\"sendGetEvents\",n:tw_em,a:[]},");
                            }
                        }

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

    public void queueWebComponentEvent(WebComponentEvent wce) {
        synchronized (eventQueue) {
            eventQueue.add(wce);
            eventQueue.notify();
        }
    }

    protected void captureThread() {
        int currentCaptureCount = ++captureCount;
        log.fine("increase captureCount:" + captureCount);
        threadCaptured = true;

        while (threadCaptured) {
            WebComponentEvent event;

            synchronized (eventQueue) {
                if (eventQueue.size() > 0) {
                    event = eventQueue.remove(0);
                    if (this.userActionListener != null){
                 	   this.notifyUserActionReceived(event);
                    }
                    if (this.playBackOn && !this.playBackEventReceived){
                    	this.playBackEventReceived = true;
                    	this.playBackStart = new Date().getTime();
                    }
                } else {
                    try {
                        threadWaiting = true;
                        eventQueue.wait();
                        threadWaiting = false;
                    } catch (InterruptedException e) {
                        log.log(Level.SEVERE, null, e);
                    }

                    event = null;
                }
            }

            if (event != null) {
                if (this.playBackOn){
                	this.flushClientEvents();
                	if (WebApplication.APPEVENT_SHUTDOWN.equals(event.getName())){
                		this.setPlayBackOn(false);
                	}
                }            	
                WebComponentListener wcl = getWebComponentListener((Integer) event.getSource());
                if (wcl != null) wcl.componentChange(event);
                if (currentCaptureCount == captureCount) threadCaptured = true;
            }
        }
    }

    protected void releaseThread() {
        threadCaptured = false;
        captureCount--;
        log.fine("decrease captureCount:" + captureCount);
    }

    protected void showWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.get(w);
        if (wr != null) throw new IllegalStateException("A window cannot be set to visible while it is already visible");
        windowToRenderer.put(w, wr = (WindowRenderer) getRenderer(w));
        wr.ai = this;
        
        if (wr instanceof FrameRenderer) {
            styleToStyleClass = new HashMap<Style, String>();
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
        }
        
        wr.render(wr, w, w instanceof Dialog ? windowToRenderer.get(getFrame()) : null);
        log.fine("Showing window with id:" + wr.id);
    }

    public void checkForUnwaitFrame() {
        if (getFrame().isWaitForWindow()) {
            boolean unwaitFrame = true;

            for (Window w : windowToRenderer.keySet()) {
                if (getFrame() != w && w.isWaitForWindow()) {
                    unwaitFrame = false;
                    break;
                }
            }

            if (unwaitFrame) getFrame().setWaitForWindow(false);
        }
    }

    protected void hideWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.remove(w);
        if (wr == null) throw new IllegalStateException("Cannot close a window that has not been set to visible");
        log.fine("Closing window with id:" + wr.id);
        wr.destroy();
        checkForUnwaitFrame();
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
        log.log(Level.FINER, "finalizing app " + this.id);
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
