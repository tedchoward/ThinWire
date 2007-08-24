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
package thinwire.ui.layout;

import java.util.List;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.web.WebApplication;
import thinwire.ui.*;
import thinwire.ui.event.*;
import thinwire.ui.style.Style;

/**
 * SplitLayout divides a Container into two sections placing one component on
 * each side and provides a dragable divider allowing the user to adjust the
 * split. The split can be either vertical (splitting the Components left and
 * right) or horizontal (splitting the Components top and bottom).
 * 
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/SplitLayout-1.png"> <br>
 * 
 * <pre>
 * Panel p = new Panel();
 * p.getChildren().add(new Button(&quot;Top&quot;));
 * p.getChildren().add(new Button(&quot;Bottom&quot;));
 * p.setLayout(new SplitLayout(.5));
 * </pre>
 * 
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class SplitLayout extends AbstractLayout {
	public enum Maximize {
		NONE, FIRST, SECOND
	};

	private WebApplication app;

	private double split;

	private boolean splitVertical;

	private Maximize maximize;

	private Label spacer;

	private boolean layoutInProgress;

	/**
	 * Constructs a horizontal SplitLayout with the first Component having the
	 * specified height, 0px margin and 4px spacing.
	 * 
	 * @param split
	 */
	public SplitLayout(double split) {
		this(split, false, 0, 4);
	}

	/**
	 * Constructs a SplitLayout with the first Component having the specified
	 * size, 0px margin and 4px spacing
	 * 
	 * @param split
	 * @param splitVertical
	 */
	public SplitLayout(double split, boolean splitVertical) {
		this(split, splitVertical, 0, 4);
	}

	/**
	 * Constructs a SplitLayout with the first Component having the specified
	 * size, specified margin and 4px spacing
	 * 
	 * @param split
	 * @param splitVertical
	 * @param margin
	 */
	public SplitLayout(double split, boolean splitVertical, int margin) {
		this(split, splitVertical, margin, 4);
	}

	/**
	 * Constructs a SplitLayout with the first Component having the specified
	 * size, specified margin and specified spacing
	 * 
	 * @param split
	 * @param splitVertical
	 * @param margin
	 * @param spacing
	 */
	public SplitLayout(double split, boolean splitVertical, int margin,
			int spacing) {
		app = (WebApplication) Application.current();
		spacer = new Label();
		spacer.addPropertyChangeListener(new String[] { Component.PROPERTY_X,
				Component.PROPERTY_Y }, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				if (!layoutInProgress && SplitLayout.this.container != null) {
					double value = (Integer) ev.getNewValue()
							- SplitLayout.this.margin;

					if (SplitLayout.this.split < 1) {
						int contValue = ev.getPropertyName().equals(
								Component.PROPERTY_X) ? SplitLayout.this.container
								.getInnerWidth()
								: SplitLayout.this.container.getInnerHeight();
						contValue -= SplitLayout.this.spacing
								+ SplitLayout.this.margin * 2;
						value = Math.floor(value / contValue * 1000 + .5) / 1000;
					}

					SplitLayout.this.setSplit(value);
				}
			}
		});

		setSplit(split);
		setSplitVertical(splitVertical);
		setMargin(margin);
		setSpacing(spacing);
		setMaximize(null);
		setAutoApply(true);
	}

	private RenderStateListener spacerListener = new RenderStateListener() {
		public void renderStateChange(RenderStateEvent ev) {
			app.clientSideMethodCall("tw_SplitLayout", "newInstance", ev
					.getId(), margin);
		}
	};

	/**
	 * Associates the specified Container to this layout. (NOTE: This method
	 * should only be called from Container.setLayout())
	 */
	public void setContainer(Container<Component> container) {
		if (this.container != null) {
			app.removeRenderStateListener(spacer, spacerListener);
			this.container.getChildren().remove(spacer);
		}

		if (container != null) {
			app.addRenderStateListener(spacer, spacerListener);
			spacer.setVisible(false);
			container.getChildren().add(spacer);
		}

		super.setContainer(container);
	}

	/**
	 * 
	 * @return the size of the first Component in the Container
	 */
	public double getSplit() {
		return split;
	}

	/**
	 * Set the size of the first Component in the Container
	 * 
	 * @param split
	 */
	public void setSplit(double split) {
		this.split = split;
		if (autoLayout)
			apply();
	}

	/**
	 * 
	 * @return true if the split is vertical (Components are left and right)
	 */
	public boolean isSplitVertical() {
		return splitVertical;
	}

	/**
	 * Sets whether the split is vertical (Components are left and right) or
	 * horizontal (Components are top and bottom)
	 * 
	 * @param splitVertical
	 */
	public void setSplitVertical(boolean splitVertical) {
		this.splitVertical = splitVertical;

		if (splitVertical) {
			this.spacer.setSize(4, 8);
		} else {
			this.spacer.setSize(8, 4);
		}

		if (autoLayout)
			apply();
	}

	@Override
	public void setMargin(int margin) {
		super.setMargin(margin);
		Integer id = app.getComponentId(spacer);
		if (id != null)
			app.clientSideMethodCall("tw_SplitLayout", "setMargin", id, margin);
	}

	/**
	 * 
	 * @return the Stle object for the spacer
	 */
	public Style getSpacerStyle() {
		return spacer.getStyle();
	}

	/**
	 * 
	 * @return true if one of the components is currently maximized
	 */
	public boolean isMaximized() {
		return maximize != Maximize.NONE;
	}

	/**
	 * Returns an enum constant telling whether the first, second or neither
	 * component is currently maximized.
	 * 
	 * @return
	 */
	public Maximize getMaximize() {
		return maximize;
	}

	/**
	 * Set either the first or second component to be maximized. Passing
	 * Maximize.NONE or null will restore the layout to the last state.
	 * 
	 * @param maximize
	 */
	public void setMaximize(Maximize maximize) {
		if (maximize == null)
			maximize = Maximize.NONE;
		this.maximize = maximize;
		if (autoLayout)
			apply();
	}

	protected void update() {
		if (container == null)
			return;
		int innerHeight = container.getInnerHeight();
		int innerWidth = container.getInnerWidth();
		if (innerHeight < 10 || innerWidth < 10)
			return;
		layoutInProgress = true;
		int firstSize = (splitVertical ? innerWidth : innerHeight) - margin * 2;
		int spacing = this.spacing;
		int secondSize;

		if (maximize == Maximize.NONE) {
			firstSize -= spacing;
			secondSize = firstSize;

			if (split >= 1) {
				firstSize = (int) Math.floor(split);
			} else {
				firstSize *= split;
			}

			secondSize -= firstSize;
		} else {
			spacing = 0;

			if (maximize == Maximize.FIRST) {
				secondSize = 0;
			} else {
				secondSize = firstSize;
				firstSize = 0;
			}
		}

		List<Component> children = container.getChildren();

		for (int i = children.size(), cnt = 0; --i >= 0;) {
			Component c = children.get(i);

			if (c == spacer) {
				if (maximize == Maximize.NONE) {
					if (splitVertical) {
						c.setBounds(firstSize + margin, margin, spacing,
								innerHeight - (margin * 2));
					} else {
						c.setBounds(margin, firstSize + margin, innerWidth
								- (margin * 2), spacing);
					}

					c.setVisible(true);
				} else {
					c.setVisible(false);
				}
			} else if (cnt == 0) {
				if (maximize == Maximize.FIRST) {
					c.setVisible(false);
				} else {
					if (splitVertical) {
						c.setBounds(firstSize + spacing + margin, margin,
								secondSize, innerHeight - (margin * 2));
					} else {
						c.setBounds(margin, firstSize + spacing + margin,
								innerWidth - (margin * 2), secondSize);
					}

					c.setVisible(true);
				}

				cnt++;
			} else if (cnt == 1) {
				if (maximize == Maximize.SECOND) {
					c.setVisible(false);
				} else {
					if (splitVertical) {
						c.setBounds(margin, margin, firstSize, innerHeight
								- (margin * 2));
					} else {
						c.setBounds(margin, margin, innerWidth - (margin * 2),
								firstSize);
					}

					c.setVisible(true);
				}

				cnt++;
			} else {
				c.setVisible(false);
			}
		}

		layoutInProgress = false;
	}
}
