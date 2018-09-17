package com.mirsaes.topdf.converter.live;

public class PerformanceTimer
{
	protected long startTimeMs;
	protected long deltaTimeMs = 0;
	protected boolean isRunning = false;

	public void reset()
	{
		deltaTimeMs = 0;
	}

	public void start()
	{
		startTimeMs = System.currentTimeMillis();
		isRunning = true;
	}

	public long read()
	{
		long deltaMs = (isRunning?(System.currentTimeMillis() - startTimeMs):0);
		return deltaTimeMs + deltaMs;
	}

	public long stop()
	{
		if (isRunning)
		{
			deltaTimeMs += System.currentTimeMillis() - startTimeMs;
		}

		isRunning = false;
		return deltaTimeMs;
	}
}
