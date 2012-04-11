/**
 * 
 */
package thinwire.ui.layout.anchorlayout;

/**
 * @author tsmith6
 * 
 */
public class PercentageAnchor implements Anchor
{
	int percentage = 100;

	public PercentageAnchor()
	{
		// default constructor
	}

	public PercentageAnchor(int percentage)
	{
		super();
		this.percentage = percentage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ts.layouts.anchorlayout.Anchor#getOffset(int)
	 */
	public int getOffset(int size)
	{
		if (percentage == 0)
		{
			return 0;
		}
		else
		{
			float s=new Integer(size).floatValue();
			float p=new Integer(percentage).floatValue();
			return new Float( s*(p/100)).intValue();
		}
	}

	public int getPercentage()
	{
		return percentage;
	}

	public void setPercentage(int percentage) throws IllegalArgumentException
	{
		if (percentage < 0 || percentage > 100)
			throw new IllegalArgumentException(
					"Percentages must be between 0 and 100 " + percentage
							+ " is invalid.");
		this.percentage = percentage;
	}

}
