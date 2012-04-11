/**
 * 
 */
package thinwire.ui.layout.anchorlayout;

/**
 * @author tsmith6
 * 
 */
public class AnchorLimit
{
	Anchor left, right, top, bottom = null;

	public AnchorLimit()
	{
		// default constructor
	}

	public AnchorLimit(Anchor left, Anchor right, Anchor top, Anchor bottom)
	{
		super();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public Anchor getBottom()
	{
		return bottom;
	}

	public Anchor getLeft()
	{
		return left;
	}

	public Anchor getRight()
	{
		return right;
	}

	public Anchor getTop()
	{
		return top;
	}

	public void setBottom(Anchor bottom)
	{
		this.bottom = bottom;
	}

	public void setLeft(Anchor left)
	{
		this.left = left;
	}

	public void setRight(Anchor right)
	{
		this.right = right;
	}

	public void setTop(Anchor top)
	{
		this.top = top;
	}
}
