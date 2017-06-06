package com.mirsaes.topdf.converter.live;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils
{

	public static void safeDeleteFile(final String fileName)
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
			// we actually want to keep it open
			// it might be expensive to keep open/close
			// and sometimes this throws exceptions
			// fs.close();
		}
	}
}
