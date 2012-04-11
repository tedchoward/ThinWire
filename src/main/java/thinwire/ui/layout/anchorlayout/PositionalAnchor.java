/**
 * 
 */
package thinwire.ui.layout.anchorlayout;

/**
 * @author tsmith6
 * 
 */
public class PositionalAnchor implements Anchor
{
	boolean relative = false;
	int position = 0;

	public PositionalAnchor()
	{
		// default constructor
	}

	public PositionalAnchor(boolean relative, int position)
	{
		super();
		this.relative = relative;
		this.position = position;
	}

	public PositionalAnchor(int position)
	{
		this.position = position;
		this.relative = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ts.layouts.anchorlayout.Anchor#getOffset(int)
	 */
	public int getOffset(int size)
	{
		return (relative) ? size - position : position;
	}

	public int getPosition()
	{
		return position;
	}

	public boolean isRelative()
	{
		return relative;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public void setRelative(boolean relative)
	{
		this.relative = relative;
	}

}
