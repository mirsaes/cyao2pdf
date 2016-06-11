package com.mirsaes.topdf.converter.live;

class UnableToConvertException extends Exception
{
	private static final long serialVersionUID = -5682369350553562344L;

	public UnableToConvertException(String msg, Exception e)
	{
		super(msg, e);
	}

	public UnableToConvertException(String msg)
	{
		super(msg);
	}
}
