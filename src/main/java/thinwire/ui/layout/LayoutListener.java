/**
 * 
 */
package thinwire.ui.layout;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

/**
 * @author tsmith6
 * 
 */
public class LayoutListener implements PropertyChangeListener
{
	@SuppressWarnings("unchecked")
	thinwire.ui.Container container = null;

	public LayoutListener(thinwire.ui.Container comp)
	{
		this.container = comp;
		comp.addPropertyChangeListener("width", this);
		comp.addPropertyChangeListener("height", this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see thinwire.ui.event.PropertyChangeListener#propertyChange(thinwire.ui.event.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0)
	{
		if(container==null||container.getLayout()==null)return;
		container.getLayout().apply();
	}

	public void uninstall(){
		try {this.container.removePropertyChangeListener(this);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
