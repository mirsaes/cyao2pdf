package com.mirsaes.topdf.converter.live;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

@Service
public class ConverterService {
	private static final Logger logger = LogManager.getLogger(ConverterService.class);

	@Value("${upload.dir}")
	private String uploadDir;

	public boolean textConvert(final String text) {
		ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());

		try {
			String generatedPDFFileName = convertToPDF(bais, ".txt");
			if (!StringUtils.isEmpty(generatedPDFFileName)) {
				// delete file
				FileUtils.safeDeleteFile(generatedPDFFileName);
				return true;
			}
		} catch (UnableToConvertException e) {
			logger.error("failed to convert test string. string=[" + text + "]", e);
		}

		return false;
	}

	public String getVersionInfo() {
		String versionInfo = "";

		try {
			// Process p = new
			// ProcessBuilder().redirectErrorStream(true).directory(workingDir).command("soffice",
			// "--headless", "--version").start();
			ExecReturnData returnData = execProcess(new String[] { "soffice", "--version" });

			versionInfo += returnData.getProcessOutput();

			// 0 is normal
			if (returnData.getReturnCode()!= 0) {
				versionInfo = "unable to get version info";
			}
		} catch (Exception e) {
			logger.warn("unable to get version info", e);
			versionInfo = "unable to get version info";
		}

		return versionInfo;
	}

	class ExecReturnData {
		final String processOutput;
		int returnCode;
		ExecReturnData(String processOutput, int returnCode) {
			this.processOutput = processOutput;
			this.returnCode=returnCode;
		}
		String getProcessOutput()
		{
			return processOutput;
		}
		int getReturnCode()
		{
			return this.returnCode;
		}
	}

	private synchronized ExecReturnData execProcess(String [] commandAndArgs) throws IOException, InterruptedException 
	{
		File workingDir = new File(uploadDir);

		Process p = Runtime.getRuntime().exec(
			commandAndArgs, null, workingDir);
		Scanner scanner = new Scanner(p.getInputStream());
		StringBuilder processOutput = new StringBuilder();

		while (scanner.hasNext())
		{
			final String line = scanner.nextLine();
			logger.debug(line);
			processOutput.append(line);
		}

		int retVal = p.waitFor();
		scanner.close();

		return new ExecReturnData(processOutput.toString(), retVal);
	}

	// can only have one instance of soffice running at a time
	protected int convertLocalFileToPDF(String toConvertFileName) throws IOException,
			InterruptedException
	{
		return execProcess(new String[] { "soffice", "--headless", "--convert-to", "pdf", toConvertFileName }).getReturnCode();
	}

	// attempt to convert a document using ubuntu alias libreoffice -> soffice
	// various performance notes
	// * how many concurrent soffice commands can be running?
	// * can an environment be supplied to libreoffice enabling
	// multiple command lines
	// * very possible this would hit memory constraints with multiple soffice
	// instances
	// * consideration could be given to throttling the amount of simultaneous
	// conversions
	/**
	 * 
	 * @param inputStream
	 * @param srcExtension
	 * @return pdfFileName
	 * @throws UnableToConvertException
	 */
	public String convertToPDF(final InputStream inputStream, final String srcExtension)
			throws UnableToConvertException
	{
		// use a unique identified to save upload stream to disk
		final String uuid = UUID.randomUUID().toString();

		// this could be simplified, something strange with the exec call and
		// the working directory
		final String toConvertFileName = uuid + srcExtension;
		final String toConvertFullFileName = uploadDir + "/" + toConvertFileName;

		// if converted, it will be located here with this name
		final String pdfFileName = uploadDir + "/" + uuid + ".pdf";

		File fileToConvert = null;

		try
		{
			fileToConvert = new File(toConvertFullFileName);

			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileToConvert));
			FileCopyUtils.copy(inputStream, stream);
			stream.close();

			// NOTE: clean this all up and sanity check everything
			// I was working with 3 hours sleep due to newborn, had to kill the
			// time somehow
			// likely should use ProcessBuilder instead of Runtime
			int retVal = convertLocalFileToPDF(toConvertFileName);
			// 0 is normal
			if (retVal != 0)
			{
				throw new UnableToConvertException("UnableToConvert. bad return code. retVal="+retVal);
			}

			// check that file was created..
			File pdfFile = new File(pdfFileName);
			if (!pdfFile.exists()) {
				throw new UnableToConvertException("UnableToConvert. file not generated. pdfFileName="+pdfFileName);
			}

		} catch (Exception e)
		{
			throw new UnableToConvertException("UnableToConvert", e);
		} finally
		{
			// delete the uploaded file
			if (fileToConvert != null)
			{
				try
				{
					fileToConvert.delete();
				} catch (Exception ex)
				{
					logger.warn("unable to delete uploaded file" + uuid, ex);
				}
			}
		}

		return pdfFileName;
	}

	public String getUploadDir()
	{
		return uploadDir;
	}
}
