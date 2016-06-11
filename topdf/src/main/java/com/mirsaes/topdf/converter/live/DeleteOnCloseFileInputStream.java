package com.mirsaes.topdf.converter.live;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

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
			final FileSystem fs = FileSystems.getDefault();
			final Path pdfFilePath = fs.getPath(fileName);

			try
			{
				Files.delete(pdfFilePath);
			} catch (Exception ex)
			{
				// do nothing
			} finally
			{
				fs.close();
			}
		}
	}
}
