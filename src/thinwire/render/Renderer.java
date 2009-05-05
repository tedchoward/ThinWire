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
package thinwire.render;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import thinwire.ui.Component;
import thinwire.ui.event.PropertyChangeListener;

/**
 * @author Joshua J. Gertzen
 */
public interface Renderer extends PropertyChangeListener {
	
	public static final class RendererManager{
		static private HashMap<String, RendererManager> platformRendererManagers=new HashMap<String, RendererManager>();
		HashMap<Class<Component>, Class<thinwire.render.ComponentRenderer>> renderers=new HashMap<Class<Component>, Class<thinwire.render.ComponentRenderer>>();
		private RendererManager(){
			//private constructor to ensure factory
		}
		@SuppressWarnings("unchecked")
		private void load(URL propertiesURL)
		{
			try {
				Properties props=new Properties();
				props.loadFromXML(propertiesURL.openStream());
				for(Entry<Object, Object> entry:props.entrySet())
				{
					try {
						renderers.put((Class<Component>)Class.forName((String)entry.getKey()), (Class<ComponentRenderer>)Class.forName((String)entry.getValue()));
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public Class<thinwire.render.ComponentRenderer> getRenderer(Class forClass){
			return renderers.get(forClass);
		}
		public static RendererManager getInstance(String platform){
			if(platform==null){
				return null;
			}
			if(platformRendererManagers.get(platform)==null)
			{
				RendererManager manager=new RendererManager();
				platformRendererManagers.put(platform, manager);
				try {
					for(Enumeration<URL> urlEnum=Thread.currentThread().getContextClassLoader().getResources("thinwire/config/"+platform+".xml");urlEnum.hasMoreElements();)
					{
						manager.load(urlEnum.nextElement());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return platformRendererManagers.get(platform);
		}
	}
    void eventSubTypeListenerInit(Class<? extends EventListener> clazz, Set<Object> subTypes);
    void eventSubTypeListenerAdded(Class<? extends EventListener> clazz, Object subType);
    void eventSubTypeListenerRemoved(Class<? extends EventListener> clazz, Object subType);
}
