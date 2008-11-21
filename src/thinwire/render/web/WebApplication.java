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

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.Renderer;
import thinwire.ui.*;
import thinwire.ui.FileChooser.FileInfo;
import thinwire.ui.layout.SplitLayout;
import thinwire.ui.style.*;
import thinwire.util.Grid;
import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public final class WebApplication extends Application {
    private static final String CLASS_NAME = WebApplication.class.getName();
    private static final Logger log = Logger.getLogger(CLASS_NAME);
    private static final Level LEVEL = Level.FINER;
    
    public static String WEB_PLATFORM_STRING="web";
    public String getPlatform(){return WEB_PLATFORM_STRING;} 
  
 
    private static ArrayList<String> BUILT_IN_RESOURCES = new ArrayList<String>(Arrays.asList(new String[]{
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
    }));
    
    static final String MAIN_PAGE;
 
    
    static {
        String classURL = "class:///" + CLASS_NAME + "/resources/";
        
        for (String res : BUILT_IN_RESOURCES) {
            if (!res.endsWith(".js")) {
                if (!res.startsWith("class:///")) res = classURL + res;
                RemoteFileMap.SHARED.add(res);
            }
        }

        try {
            String twLib = loadJSLibrary(classURL);
            //Store the MainPage.html after replacing the JS lib name
            MAIN_PAGE = new String(WebApplication.getResourceBytes(classURL + "MainPage.html"))
                .replaceAll("[$][{]ThinWire[.]js[}]", twLib);
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
            return RemoteFileMap.SHARED.add(twPrefix + ".js", baos.toByteArray());
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
            throw (RuntimeException)e;
        }
    }
    
    public static Application current() {
        Thread t = Thread.currentThread();
        return t instanceof EventProcessor ? ((EventProcessor)t).app : null;
    }
    
    static class Timer {
        Runnable task;
        long timeout;
        boolean repeat;
    }
    
    static enum State {INIT, STARTUP, RUNNING, REPAINT, SHUTDOWN, TERMINATED}
    
    public static final String REMOTE_FILE_PREFIX = "%SYSROOT%";
    
    private String baseFolder;
    private String styleSheet;
    private int nextCompId;
    private Map<String, Class<ComponentRenderer>> nameToRenderer;
    private Map<Window, WindowRenderer> windowToRenderer;
    private Map<Integer, WebComponentListener> webComponentListeners;
    private Map<Component, Object> renderStateListeners;
    private EventProcessor proc;
    private ClassLoader classLoader;
    
    State state;
    List<Runnable> timers;
    Map<String, Timer> timerMap;
    RemoteFileMap remoteFileMap;
    WebComponentEvent startupEvent;
    Map<Style, String> styleToStyleClass = new HashMap<Style, String>();
    FileInfo[] fileList = new FileInfo[1];
	protected UserActionListener userActionListener;
    
    WebApplication(String baseFolder, Class mainClass, String styleSheet, String[] args, String initialFrameTitle) throws IOException {
        this.baseFolder = baseFolder;
        this.styleSheet = styleSheet;
        nameToRenderer = new HashMap<String, Class<ComponentRenderer>>();
        windowToRenderer = new HashMap<Window, WindowRenderer>();
        timerMap = new HashMap<String, Timer>();
        timers = new LinkedList<Runnable>();
        webComponentListeners = new HashMap<Integer, WebComponentListener>();
        remoteFileMap = new RemoteFileMap(this);
        classLoader = mainClass.getClassLoader();
     
        setWebComponentListener(ApplicationEventListener.ID, new ApplicationEventListener(this));
        startupEvent = ApplicationEventListener.newStartEvent(mainClass, args, initialFrameTitle);
        state = State.INIT;
    }
    
    protected ClassLoader getClassLoader() {
    	return classLoader;
    }
    
    InputStream getContextResourceAsStream(String uri) {
    	return getResourceAsStream(classLoader, uri);
    }

    //NOTE: Only to be called by ApplicationEventListener's frame visibility PropertyChangeListener.
    void flushEvents() {
        proc.flush();
    }
    
    void repaint() {
    	state = State.REPAINT;
    }
    
    protected void shutdown() {
        if (state == State.TERMINATED) return;
        if (state != State.SHUTDOWN) state = State.SHUTDOWN;
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": initiating application instance shutdown");        
        
        try {
            proc = EventProcessorPool.INSTANCE.getProcessor(this);            
            WebComponentEvent wce = ApplicationEventListener.newShutdownEvent();
            
            try {
                proc.handleRequest(wce, new CharArrayWriter());

                //XXX Loops infinitely if entered while frame is not visible and a dialog is blocking.
                while (proc.isInUse()) {
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": processor returned, probably from flush(), sending null event");
                    proc.handleRequest((WebComponentEvent)null, new CharArrayWriter());                    
                }

                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": shutdown has completed");
            } catch (IOException e) {
                log.log(Level.SEVERE, "handleRequest generated IOException during shutdown", e);
            }
        } finally { 
            if (!proc.isInUse()) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": returning to pool after shutdown");
                EventProcessorPool.INSTANCE.returnToPool(proc);
            } else {
                //This state should only occur if during the shutdown process another 'waitForWindow' Dialog is
                //presented for some reason.
                EventProcessorPool.INSTANCE.removeFromPool(proc);
                if (log.isLoggable(Level.WARNING)) log.log(Level.WARNING, Thread.currentThread().getName() + ": thread still active!");
            }
            
            proc = null;
            if (nameToRenderer != null) nameToRenderer.clear();
            if (windowToRenderer != null) windowToRenderer.clear();
            if (webComponentListeners != null) webComponentListeners.clear();
            if (renderStateListeners != null) renderStateListeners.clear();
            if (timerMap != null) timerMap.clear();
            if (styleToStyleClass != null) styleToStyleClass.clear();
            
            nameToRenderer = null;
            windowToRenderer = null;
            webComponentListeners = null;
            renderStateListeners = null;
            timerMap = null;
            styleToStyleClass = null;
            fileList = null;
            classLoader = null;
            remoteFileMap.destroy();
            remoteFileMap = null;
            super.shutdown(); //Clear Application references
            state = State.TERMINATED;            
        }
    }
        
    void processActionEvents(Reader r, PrintWriter w) throws IOException {
        if (proc != null) throw new IllegalStateException("There is already an EventProcessor allocated to this application!");
        
        try {
            proc = EventProcessorPool.INSTANCE.getProcessor(this);
            proc.handleRequest(r, w);
     
            if (state != State.RUNNING) {
                if (state == State.INIT) {
                    proc.handleRequest(ApplicationEventListener.newInitEvent(), w);
                    state = State.STARTUP;
                } else if (state == State.SHUTDOWN) {
                    shutdown();
                } else if (state == State.STARTUP && (getFrame().getWidth() > 0 || getFrame().getHeight() > 0)) { 
                    WebComponentEvent startupEvent = this.startupEvent;
                    this.startupEvent = null;
                    proc.handleRequest(startupEvent, w);
                    state = State.RUNNING;
                } else if (state == State.REPAINT) {
                    proc.handleRequest(ApplicationEventListener.newRepaintEvent(), w);
                    state = State.RUNNING;
                }
            }
        } finally {
            if (proc != null) {
                EventProcessorPool.INSTANCE.returnToPool(proc);
                proc = null;
            }
        }
    }
    
    void sendDefaultComponentStyles() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        for (Map.Entry<Class<? extends Component>, Style> e : getDefaultStyles().entrySet()) {
            Class<? extends Component> clazz = e.getKey();
            
            if (clazz != null) {
                String styleClass = ComponentRenderer.getSimpleClassName(clazz);
                Style style = e.getValue();
                styleToStyleClass.put(style, styleClass);
                sb.append(styleClass).append(":");
                getStyleValues(sb, style, null);
                sb.append(',');
            }
        }
        
        sb.setCharAt(sb.length() - 1, '}');
        clientSideMethodCall("tw_Component", "setDefaultStyles", sb);
    }
    
    void sendStyleInitInfo() {
        try {
            loadStyleSheet(styleSheet);
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            
            for (Map.Entry<String, Color> e : getSystemColors().entrySet()) {
                sb.append(e.getKey()).append(":\"").append(e.getValue().toHexString()).append("\",");
            }
            
            sb.setCharAt(sb.length() - 1, '}');
            clientSideMethodCall("tw_Component", "setSystemColors", sb);

            sb.setLength(0);
            sb.append('{');
            
            for (Map.Entry<String, String> e : getSystemImages().entrySet()) {
                String value = getSystemFile(e.getValue());
                sb.append(e.getKey()).append(":\"").append(addResourceMapping(value)).append("\",");
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
    
    StringBuilder getStyleValue(StringBuilder sb, String propertyName, Object value) {
        if (propertyName.equals(Border.PROPERTY_BORDER_SIZE)) {
            sb.append(RichTextParser.STYLE_BORDER_WIDTH).append(":\"").append(value).append("px");
        } else if (propertyName.equals(Font.PROPERTY_FONT_SIZE)) {
            sb.append(RichTextParser.STYLE_FONT_SIZE).append(":\"").append(((Number)value).intValue()).append("px");
    	} else if (propertyName.equals(Border.PROPERTY_BORDER_IMAGE)) {
    		sb.append(RichTextParser.STYLE_BORDER_IMAGE).append(":\"");
    		ImageInfo ii = (ImageInfo)value;
            String name = ii.getName();
            
            if (name.length() > 0) {
                sb.append(addResourceMapping(name));
                sb.append(',').append(ii.getWidth()).append(',').append(ii.getHeight());
            }
    	} else {
	        if (propertyName.equals(Border.PROPERTY_BORDER_TYPE)) {
	            sb.append(RichTextParser.STYLE_BORDER_STYLE);
	    	} else if (propertyName.equals(Font.PROPERTY_FONT_FAMILY)) {
	    		sb.append(RichTextParser.STYLE_FONT_FAMILY);
	    	} else if (propertyName.equals(Background.PROPERTY_BACKGROUND_POSITION)) {
	    		sb.append(RichTextParser.STYLE_BACKGROUND_POSITION);
	        } else if (propertyName.equals(Font.PROPERTY_FONT_ITALIC)) {
	            sb.append(RichTextParser.STYLE_FONT_STYLE);
	            value = value == Boolean.TRUE ? "italic" : "normal";
	        } else if (propertyName.equals(Font.PROPERTY_FONT_BOLD)) {
	            sb.append(RichTextParser.STYLE_FONT_WEIGHT);
	            value = value == Boolean.TRUE ? "bold" : "normal";
	        } else if (propertyName.equals(Background.PROPERTY_BACKGROUND_IMAGE)) {
	            sb.append(RichTextParser.STYLE_BACKGROUND_IMAGE);
	            value = addResourceMapping((String)value);
	        } else if (value instanceof Color) {
	        	if (propertyName.equals(Font.PROPERTY_FONT_COLOR)) {
	        		sb.append(RichTextParser.STYLE_COLOR);
	        	} else if (propertyName.equals(Background.PROPERTY_BACKGROUND_COLOR)) {
	        		sb.append(RichTextParser.STYLE_BACKGROUND_COLOR);
	        	} else if (propertyName.equals(Border.PROPERTY_BORDER_COLOR)) {
	        		sb.append(RichTextParser.STYLE_BORDER_COLOR);
	        	} else {
	        		throw new IllegalArgumentException("unknown style property '" + propertyName + "' with value '" + value + "'");
	        	}
	        		
	            Color color = (Color)value;
	            if (color.isSystemColor()) color = getSystemColors().get(color.toString());
	            value = color.toHexString();
	        } else if (propertyName.equals(Font.PROPERTY_FONT_UNDERLINE) || propertyName.equals(Font.PROPERTY_FONT_STRIKE)) {        	
	            sb.append(RichTextParser.STYLE_TEXT_DECORATION);
	            Boolean[] bool = (Boolean[])value;
	            
	        	if (bool[0] == Boolean.TRUE && bool[1] == Boolean.TRUE) {
	        		value = "underline line-through";
	        	} else if (bool[0] == Boolean.TRUE) {
	        		value = "underline";
	        	} else if (bool[1] == Boolean.TRUE) {
	        		value = "line-through";
	        	} else {
	        		value = "none";
	        	}
	    	} else if (propertyName.equals(Background.PROPERTY_BACKGROUND_REPEAT)) {
	    		sb.append(RichTextParser.STYLE_BACKGROUND_REPEAT);

	    		switch ((Background.Repeat)value) {
	                case BOTH: value = "repeat"; break;
	                case X: value = "repeat-x"; break;
	                case Y: value = "repeat-y"; break;
	                default: value = "no-repeat"; break;
	            }
        	} else {
        		throw new IllegalArgumentException("unknown style property '" + propertyName + "' with value '" + value + "'");
        	}
	        
	        sb.append(":\"").append(value);
    	}
        
        sb.append("\",");
        return sb;
    }
    
    StringBuilder getStyleValues(StringBuilder sb, Style s, Style ds) {
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
            if (fontFamily.equals(font.getFamily())) fontFamily = null;
            if (fontSize.equals(font.getSize())) fontSize = null;
            if (fontColor.equals(font.getColor())) fontColor = null;
            if (fontBold.equals(font.isBold())) fontBold = null;
            if (fontItalic.equals(font.isItalic())) fontItalic = null;
            if (fontUnderline.equals(font.isUnderline())) fontUnderline = null;
            if (fontStrike.equals(font.isStrike())) fontStrike = null;
        }
        
        sb.append("{");
        
        if (backgroundColor != null) getStyleValue(sb, Background.PROPERTY_BACKGROUND_COLOR, backgroundColor);
        if (backgroundImage != null) getStyleValue(sb, Background.PROPERTY_BACKGROUND_IMAGE, backgroundImage);
        if (backgroundRepeat != null) getStyleValue(sb, Background.PROPERTY_BACKGROUND_REPEAT, backgroundRepeat);
        if (backgroundPosition != null) getStyleValue(sb, Background.PROPERTY_BACKGROUND_POSITION, backgroundPosition);
        
        if (borderType != null && borderType != Border.Type.IMAGE) {
            if (borderType == Border.Type.NONE) {
                borderType = Border.Type.SOLID;
                borderColor = backgroundColor;
            }

            getStyleValue(sb, "borderType", borderType);
        }
        
        if (borderImage != null) getStyleValue(sb, Border.PROPERTY_BORDER_IMAGE, s.getBorder().getImageInfo());
        if (borderColor != null) getStyleValue(sb, Border.PROPERTY_BORDER_COLOR, borderColor);
        if (borderSize != null) getStyleValue(sb, Border.PROPERTY_BORDER_SIZE, borderSize);
        if (fontFamily != null) getStyleValue(sb, Font.PROPERTY_FONT_FAMILY, fontFamily);
        if (fontSize != null) getStyleValue(sb, Font.PROPERTY_FONT_SIZE, fontSize);
        if (fontColor != null) getStyleValue(sb, Font.PROPERTY_FONT_COLOR, fontColor);
        if (fontBold != null) getStyleValue(sb, Font.PROPERTY_FONT_BOLD, fontBold); 
        if (fontItalic != null) getStyleValue(sb, Font.PROPERTY_FONT_ITALIC, fontItalic); 
        if (fontUnderline != null || fontStrike != null) getStyleValue(sb, Font.PROPERTY_FONT_UNDERLINE, new Boolean[]{fontUnderline, fontStrike}); 
        
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


    //NOTE: Remote file includes will always cause a reinsertion of the code since the file cache
    //      doesn't hold onto a reference of them and it's very possible that remote file content can
    //      change for each request.
    public void clientSideIncludeFile(String uri) {
        if (remoteFileMap.contains(uri)) return;
        String remoteName = addResourceMapping(uri);
        clientSideFunctionCallWaitForReturn("tw_include", remoteName);
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
    
    private String clientSideCallImpl(boolean sync, Object objectId, String name, Object[] args) {
        if (proc == null) throw new IllegalStateException("No event processor allocated to this application. This is likely caused by making UI calls from a non-UI thread");
        return proc.postUpdateEvent(sync, objectId, name, args);
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
        if (proc == null) throw new IllegalStateException("No event processor allocated to this application. This is likely caused by making UI calls from a non-UI thread");
        proc.captureThread();
    }

    protected void releaseThread() {
        if (proc == null) throw new IllegalStateException("No event processor allocated to this application. This is likely caused by making UI calls from a non-UI thread");
        proc.releaseThread();
    }

    protected void showWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.get(w);
        if (wr != null) throw new IllegalStateException("A window cannot be set to visible while it is already visible");
        windowToRenderer.put(w, wr = (WindowRenderer) getRenderer(w));
        wr.ai = this;
        
        if (wr instanceof DialogRenderer) {
            wr.render(wr, w, windowToRenderer.get(getFrame()));
            
            //Force events to be sent to client because dialog show's must be immediate!
            proc.flush();
        } else {
        	sendDefaultComponentStyles();
            wr.render(wr, w, null);
        }
    }

    protected void hideWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.remove(w);
        if (wr == null) throw new IllegalStateException("Cannot close a window that has not been set to visible");
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": closing window with id:" + wr.id);
        wr.destroy();

        //Force events to be sent to client because dialog hide's must be immediate!
        if (wr instanceof DialogRenderer) proc.flush();
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

    protected String addResourceMapping(String uri) {
        if (uri.trim().length() > 0) {
        	File file = null;
        	
            if (uri.startsWith("file") || uri.startsWith("class") || (file = getRelativeFile(uri)).exists()) {
                if (!uri.startsWith("class")) {
                	if (uri.startsWith("file:///")) {
                		file = getRelativeFile(uri.substring(7));
                		if (!file.exists()) return uri;
                	}
                	
                	uri = file.getAbsolutePath();
                }
                
                uri = remoteFileMap.add(uri);
                uri = WebApplication.REMOTE_FILE_PREFIX + uri;
            }
        } else {
            uri = "";
        }

        return uri;
    }
    
    protected void removeResourceMapping(String uri) {
        if (uri.trim().length() > 0) {
            if (uri.startsWith("file") || uri.startsWith("class") || getRelativeFile(uri).exists()) {
                if (!uri.startsWith("class")) {
                	if (uri.startsWith("file:///")) uri = uri.substring(7);
                	uri = getRelativeFile(uri).getAbsolutePath();
                }
                
                remoteFileMap.remove(uri);
            }
        }
    }

    public void addTimerTask(Runnable task, long timeout) {
        addTimerTask(task, timeout, false);
    }

    public void addTimerTask(Runnable task, long timeout, boolean repeat) {
    	
    	if (timeout == 0 && !repeat) {
    		if (!timers.contains(task)) timers.add(task);
    	} else {
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
    }

    public void resetTimerTask(Runnable task) {
    	if (!timers.contains(task)) {
	        String timerId = String.valueOf(System.identityHashCode(task));
	        Timer timer = timerMap.get(timerId);        
	        if (timer != null) clientSideFunctionCall("tw_addTimerTask", timerId, timer.timeout);
    	}
    }

    public void removeTimerTask(Runnable task) {
    	if (!timers.remove(task)) {
	        String timerId = String.valueOf(System.identityHashCode(task));
	        clientSideFunctionCall("tw_removeTimerTask", timerId);
	        timerMap.remove(timerId);
    	}
    }

    protected Object setPackagePrivateMember(String memberName, Component comp, Object value) {
        return super.setPackagePrivateMember(memberName, comp, value);
    }    
    
    protected void finalize() {
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": finalizing app");
    }

    /**One and only one {@link UserActionListener} can be added. 
     */
	public void setUserActionListener(UserActionListener userActionListener) {
		this.userActionListener = userActionListener;
		
	}
}
