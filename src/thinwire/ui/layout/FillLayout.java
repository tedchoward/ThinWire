package thinwire.ui.layout;

import java.util.List;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.layout.Layout;

public class FillLayout implements Layout {

	Container<Component> container=null;
	public void apply() {
	if(container!=null)
	{
		Rectangle bounds=new Rectangle(0,0,container.getInnerWidth(),container.getInnerHeight());
		bounds.x=bounds.y=margin;
		bounds.width=bounds.width-(2*margin);
		bounds.height=bounds.height-(2*margin);
		for(Component comp:(List<Component>)container.getChildren())
		{
			comp.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}
	}

	public Container<Component> getContainer() {
		// TODO Auto-generated method stub
		return container;
	}

	public int getMargin() {
		// TODO Auto-generated method stub
		return margin;
	}
	int spacing=0;
	public int getSpacing() {
		// TODO Auto-generated method stub
		return spacing;
	}
	boolean autoApply=false;
	public boolean isAutoApply() {
		// TODO Auto-generated method stub
		return autoApply;
	}

	public void setAutoApply(boolean arg0) {
		autoApply=arg0;

	}

	public void setContainer(Container<Component> arg0) {
		container=arg0;

	}
int margin=0;
	public void setMargin(int arg0) {
		if(arg0>-1)
		{
			margin=arg0;
		}

	}

	public void setSpacing(int arg0) {
		spacing=arg0;

	}

}
