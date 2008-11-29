package thinwire.ui;

import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.layout.AnchorLayout;
import thinwire.ui.layout.Layout;
import thinwire.ui.layout.anchorlayout.AnchorLimit;
import thinwire.ui.layout.anchorlayout.PositionalAnchor;

public class ScrollPane extends Panel {
	public static enum ScrollType {
		Vertical, Horizontal, Both, Neither
	}

	private Panel viewPort;
	private Panel contentsPane;
	private Slider verticalScroll;
	private Slider horizontalScroll;
	private ScrollType scroll = ScrollType.Neither;
	private int scrollbarSize = 15;

	public ScrollPane() {
		viewPort = new Panel();
		contentsPane = new Panel();
		verticalScroll = new Slider();
		horizontalScroll = new Slider();
		scroll = ScrollType.Neither;
		viewPort.getChildren().add(contentsPane);
		viewPort.setVisible(true);
		contentsPane.setVisible(true);
		super.getChildren().add(viewPort);
		super.getChildren().add(horizontalScroll);
		super.getChildren().add(verticalScroll);

		
		
		horizontalScroll.addPropertyChangeListener(
				Slider.PROPERTY_CURRENT_INDEX, new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent ev) {
						processScroll();
					}
				});
		verticalScroll.addPropertyChangeListener(Slider.PROPERTY_CURRENT_INDEX,
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent ev) {
						processScroll();
					}
				});

		addPropertyChangeListener(new String[] { "width", "height" },
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent ev) {
						validate();
					}
				});

	}

	private void processScroll(){
		contentsPane
		.setY(1+((verticalScroll.getLength() - verticalScroll.getCurrentIndex())
				* -1));
		contentsPane
		.setX((horizontalScroll.getCurrentIndex())
				* -1);
		
	}
	
	boolean vertical = false;
	boolean horizontal = false;

	private void updatePositions() {
		// switch(scroll){
		// case Both:{
		if (vertical && horizontal && scroll == ScrollType.Both) {
			viewPort.setBounds(0, 0, super.getInnerWidth() - scrollbarSize,
					super.getInnerHeight() - scrollbarSize);
			horizontalScroll.setBounds(0, getInnerHeight() - scrollbarSize,
					getInnerWidth() - scrollbarSize, scrollbarSize);
			verticalScroll.setBounds(getInnerWidth() - scrollbarSize, 0,
					scrollbarSize, getInnerHeight() - scrollbarSize);
			horizontalScroll.setVisible(true);
			verticalScroll.setVisible(true);
			return;
		}
		// case Horizontal:{
		if ((scroll == ScrollType.Both || scroll == ScrollType.Horizontal)
				&& horizontal) {
			viewPort.setBounds(0, 0, super.getInnerWidth(), super
					.getInnerHeight()
					- scrollbarSize);
			horizontalScroll.setBounds(0, getInnerHeight() - scrollbarSize,
					getInnerWidth(), scrollbarSize);
			// verticalScroll.setBounds(getInnerWidth()-scrollbarSize, 0,
			// scrollbarSize, getInnerHeight()- scrollbarSize);
			horizontalScroll.setVisible(true);
			verticalScroll.setVisible(false);
			return;
		}
		// case Vertical:{
		if ((scroll == ScrollType.Both || scroll == ScrollType.Vertical)
				&& vertical) {
			viewPort.setBounds(0, 0, super.getInnerWidth() - scrollbarSize,
					super.getInnerHeight());
			// horizontalScroll.setBounds(0,getInnerHeight()-scrollbarSize,
			// getInnerWidth()-scrollbarSize,scrollbarSize);
			verticalScroll.setBounds(getInnerWidth() - scrollbarSize, 0,
					scrollbarSize, getInnerHeight());
			horizontalScroll.setVisible(false);
			verticalScroll.setVisible(true);
			return;
		}
		// case Neither:{
		// if(scroll==ScrollType.Neither)
		{
			viewPort.setBounds(0, 0, super.getInnerWidth(), super
					.getInnerHeight());
			// horizontalScroll.setBounds(0,getInnerHeight()-scrollbarSize,
			// getInnerWidth()-scrollbarSize,scrollbarSize);
			// verticalScroll.setBounds(getInnerWidth()-scrollbarSize, 0,
			// scrollbarSize, getInnerHeight()- scrollbarSize);
			horizontalScroll.setVisible(false);
			verticalScroll.setVisible(false);
			return;
		}

		// }

	}

	public ScrollType getScroll() {
		return scroll;
	}

	public void setScroll(ScrollType scroll) {
		this.scroll = scroll;
	}
	public int getMaxVerticalScroll(){
		return verticalScroll.getLength();
	}
	public int getMaxHorizontalScroll(){
		return horizontalScroll.getLength();
	}
	public int getVerticalScroll(){
		return verticalScroll.getCurrentIndex();
	}
	public int getHorizontalScroll(){
		return horizontalScroll.getCurrentIndex();
	}
	public void setVerticalScroll(int pos) throws IndexOutOfBoundsException{
		if(pos<0||pos>verticalScroll.getLength()){
			throw new IndexOutOfBoundsException();
		}
		verticalScroll.setCurrentIndex(pos);
	}
	public void setHorizontalScroll(int pos) throws IndexOutOfBoundsException
	{
		if(pos<0||pos>verticalScroll.getLength()){
			throw new IndexOutOfBoundsException();
		}
		horizontalScroll.setCurrentIndex(pos);
	}
	
	
	
	
	public void validate() {
		int width = 0;
		int height = 0;	
		
		if (contentsPane.getLayout() != null) {
			contentsPane.getLayout().apply();
		}
		for (Component child : contentsPane.getChildren()) {
			int w = child.getX() + child.getWidth();
			int h = child.getY() + child.getHeight();
			width = w > width ? w : width;
			height = h > height ? h : height;
		}
		int hd = height - viewPort.getHeight();
		int wd = width - viewPort.getWidth();
		contentsPane.setWidth(width > viewPort.getWidth() ? width : viewPort
				.getWidth());
		contentsPane.setHeight(height > viewPort.getHeight() ? height
				: viewPort.getHeight());
		wd = wd > 0 ? wd : 0;
		hd = hd > 0 ? hd : 0;
		horizontal = wd > 0;
		vertical = hd > 0;
		if (horizontalScroll.getCurrentIndex() > wd) {
			horizontalScroll.setCurrentIndex(wd);
		}
		if (verticalScroll.getCurrentIndex() > hd) {
			verticalScroll.setCurrentIndex(hd);
		}
		horizontalScroll.setLength(wd > 0 ? wd : 1);
		verticalScroll.setLength(hd > 0 ? hd : 1);
		updatePositions();
		
		processScroll();
	}

	// @Override
	// public List<Component> getChildren() {
	// return new ArrayList<Component>(super.getChildren());
	// }

	public Panel getContents() {
		return contentsPane;
	}

	public int getScrollbarSize() {
		return scrollbarSize;
	}

	public void setScrollbarSize(int scrollbarSize) {
		this.scrollbarSize = scrollbarSize;
		updatePositions();
	}

	@Override
	public Layout getLayout() {
		return null;
	}

	@Override
	public void setLayout(Layout layout) {
		//
	}

}
