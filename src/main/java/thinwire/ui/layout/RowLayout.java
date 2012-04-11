package thinwire.ui.layout;

import java.util.HashSet;
import java.util.Set;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.layout.Layout;

public class RowLayout implements Layout {
	Container<Component> container = null;
	Orientation orientation = Orientation.VERTICAL;

	public static enum Orientation {
		VERTICAL, HORIZONTAL
	} 

	int margin = 0;
	int spacing = 0;
	
	
	public RowLayout(){
		
	}

	public RowLayout(Orientation orientation){
		this.orientation=orientation;
	}
	
	public void apply() {

		int x = getMargin()+getSpacing();
		int y = getMargin()+getSpacing();
		int rowHeight = 0;
		Set<Component> components = new HashSet<Component>();
		for (Component comp : container.getChildren()) {
			rowHeight=comp.getHeight()>rowHeight?comp.getHeight():rowHeight;
			comp.setX(x);
			comp.setY(y);
			x+=comp.getWidth()+getSpacing();
			if(getOrientation() == Orientation.VERTICAL)
			{
				comp.setWidth(comp.getWidth()>container.getInnerWidth()?comp.getWidth():container.getInnerWidth());
			}
			if (getOrientation() == Orientation.VERTICAL
					|| x + comp.getWidth() > container.getInnerWidth()) {
				
				for (Component comp2 : components) {
					comp2.setHeight(rowHeight);
					comp2.setY(y);
				}
				y += rowHeight+getSpacing();
				x = getMargin()+getSpacing();
			}
		}
	}

	public Container<Component> getContainer() {
		return container;
	}

	public int getMargin() {
		return margin;
	}

	public int getSpacing() {
		return spacing;
	}

	public boolean isAutoApply() {
		return true;
	}

	public void setAutoApply(boolean arg0) {
	}

	public void setContainer(Container<Component> arg0) {
		this.container = arg0;
	}

	public void setMargin(int arg0) {
		this.margin = arg0;
	}

	public void setSpacing(int arg0) {
		this.spacing = arg0;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

}
