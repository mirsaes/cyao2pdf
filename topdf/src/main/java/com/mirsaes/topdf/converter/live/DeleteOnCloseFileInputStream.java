package com.mirsaes.topdf.converter.live;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class DeleteOnCloseFileInputStream extends FileInputStream
{
	private final String fileName;

	public DeleteOnCloseFileInputStream(final String fileName) throws FileNotFoundException
	{
		super(fileName);
		this.fileName = fileName;
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			super.close();
		} finally
		{
			FileUtils.safeDeleteFile(fileName);
		}
	}
}
