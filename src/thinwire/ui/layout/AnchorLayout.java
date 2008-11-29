/**
 * 
 */
package thinwire.ui.layout;


import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.layout.AbstractLayout;
import thinwire.ui.layout.Layout;
import thinwire.ui.layout.anchorlayout.AnchorLimit;

/**
 * @author tsmith6
 * 
 */
public class AnchorLayout extends AbstractLayout implements Layout
{
	Container<Component> comp = null;
	boolean auto = true;
	LayoutListener list = null;

	public AnchorLayout()
	{
		super();
	}

	public AnchorLayout(Container<Component> parent)
	{
		this();
		this.setContainer(parent);
	}

	public void apply()
	{
		// NOTE: We don't care about the x and y. And in the case of a Frame, it
		// would cause problems.
		Rectangle bounds = new Rectangle(0, 0, comp.getInnerWidth(), comp
				.getInnerHeight());

		for (Component child : comp.getChildren())
		{
			Object lim = child.getLimit();
			if (lim != null && lim instanceof AnchorLimit)
			{
				Rectangle oldBounds = new Rectangle(child.getX(), child.getY(),
						child.getWidth(), child.getHeight());
				Rectangle newBounds = new Rectangle(oldBounds);
				AnchorLimit limit = (AnchorLimit) lim;
				if (limit.getTop() != null)
				{
					newBounds.setY(limit.getTop().getOffset(bounds.height));
				}
				if (limit.getBottom() != null)
				{
					if (limit.getTop() != null)
					{
						newBounds.setHeight(limit.getBottom().getOffset(
								bounds.getHeight())
								- newBounds.getY());
					}
					else
					{
						newBounds.setY(limit.getBottom().getOffset(
								bounds.getHeight())
								- newBounds.getHeight());
					}
				}
				if (limit.getLeft() != null)
				{
					newBounds
							.setX(limit.getLeft().getOffset(bounds.getWidth()));
				}
				if (limit.getRight() != null)
				{
					if (limit.getLeft() != null)
					{
						newBounds.setWidth(limit.getRight().getOffset(
								bounds.getWidth())
								- newBounds.getX());
					}
					else
					{
						newBounds.setX(limit.getRight().getOffset(
								bounds.getWidth())
								- newBounds.getWidth());
					}
				}
				child.setBounds(newBounds.getX(), newBounds.getY(), newBounds
						.getWidth(), newBounds.getHeight());
			}
		}

		// comp.getChildren()
	}

	public Container<Component> getContainer()
	{
		// TODO Auto-generated method stub
		return comp;
	}

	public int getMargin()
	{
		// never any margin
		return 0;
	}

	public int getSpacing()
	{
		// never any spacing
		return 0;
	}

	public boolean isAutoApply()
	{
		// TODO Auto-generated method stub
		return auto;
	} 

	public void setAutoApply(boolean arg0)
	{
		this.auto = arg0;

	}

	public void setContainer(Container<Component> arg0)
	{
		if (list != null)
		{
			list.uninstall();
		}
		this.comp = arg0;
		list = new LayoutListener(arg0);

	}

	public void setMargin(int arg0)
	{
		// do nothing... margins mean nothing to this layout

	}

	public void setSpacing(int arg0)
	{
		// do nothing... spacing means nothing to this layout

	}

}
