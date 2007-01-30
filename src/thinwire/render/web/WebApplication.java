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
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.Writer;
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
    private static final Logger log = Logger.getLogger(CLASS_NAME);
    
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
    
    public static Application current() {
        Thread t = Thread.currentThread();
        return t instanceof EventProcessor ? ((EventProcessor)t).app : null;
    }
    
    static class Timer {
        Runnable task;
        private long timeout;
        boolean repeat;
    }
    
    private String baseFolder;
    private String styleSheet;
    private Map<String, Class<ComponentRenderer>> nameToRenderer;
    private Map<Window, WindowRenderer> windowToRenderer;
    private Map<Integer, WebComponentListener> webComponentListeners;
    private Set<String> clientSideIncludes;
    private Map<Component, Object> renderStateListeners;
    private int nextCompId;
    private EventProcessor eventProcessor;
    
    HttpSession httpSession;
    Map<String, Timer> timerMap;
    WebComponentEvent startupEvent;
    Map<Style, String> styleToStyleClass = new HashMap<Style, String>();
    FileInfo[] fileList = new FileInfo[1];

    //Stress Test Variables.
    UserActionListener userActionListener;    
    boolean playBackOn = false;
    long playBackStart = -1;
    private long playBackDuration = -1;
    private long recordDuration = -1;
    boolean playBackEventReceived = false;
    //end Stress Test.
    
    WebApplication(HttpSession httpSession, String baseFolder, String mainClass, String styleSheet, String[] args) throws IOException {
        this.httpSession = httpSession;
        this.baseFolder = baseFolder;
        this.styleSheet = styleSheet;
        nameToRenderer = new HashMap<String, Class<ComponentRenderer>>();
        windowToRenderer = new HashMap<Window, WindowRenderer>();
        timerMap = new HashMap<String, Timer>();
        webComponentListeners = new HashMap<Integer, WebComponentListener>();
        
        setWebComponentListener(ApplicationEventListener.ID, new ApplicationEventListener(this));
        eventProcessor = new EventProcessor(this);
        eventProcessor.start();
        startupEvent = ApplicationEventListener.newStartEvent(mainClass, args);
        eventProcessor.handleRequest(ApplicationEventListener.newInitEvent(), null);
    }
    
    void shutdown(Writer w) throws IOException {
        log.log(Level.FINER, "Initiating Application instance SHUTDOWN");
        eventProcessor.handleRequest(ApplicationEventListener.newShutdownEvent(), w);

        // Set the execution thread to null,
        // so that this application instance can get
        // garbage collected.
        eventProcessor = null;
    }
    
    void processActionEvents(Reader r, PrintWriter w) throws IOException {
        int count = eventProcessor.handleRequest(r, w);
        
        if (startupEvent != null && count == 0) {
            WebComponentEvent startupEvent = this.startupEvent;
            this.startupEvent = null;
            eventProcessor.handleRequest(startupEvent, w);
        }
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
    
    private String clientSideCallImpl(boolean sync, Object objectId, String name, Object[] args) {
        if (eventProcessor == null) throw new IllegalStateException("No event processor allocated to this application. This is likely caused by making UI calls from a non-UI thread");
        return eventProcessor.postUpdateEvent(sync, objectId, name, args);
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
    
    static String stringValueOf(Object o) {
        String ret;
        
        if (o == null) {
            ret = "null";
        } else if (o instanceof Integer) {
            ret = String.valueOf(((Integer) o).intValue());
        } else if (o instanceof Number) {
            ret = String.valueOf(((Number) o).doubleValue());
        } else if (o instanceof Boolean) {
            ret = String.valueOf(((Boolean) o).booleanValue());
        } else if (o instanceof StringBuilder) {
            ret = o.toString();
        } else {
            ret = o.toString();
            ret = REGEX_DOUBLE_SLASH.matcher(ret).replaceAll("\\\\\\\\");
            ret = REGEX_DOUBLE_QUOTE.matcher(ret).replaceAll("\\\\\"");
            ret = REGEX_CRLF.matcher(ret).replaceAll("\\\\r\\\\n");
            ret = '"' + ret + '"';
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

	void notifyUserActionReceived(WebComponentEvent evt) {
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
