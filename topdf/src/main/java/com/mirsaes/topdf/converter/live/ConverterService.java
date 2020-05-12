package com.mirsaes.topdf.converter.live;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	@Value("${convertusers.enabled:false}")
	private boolean convertUsersEnabled;

	@Value("${convertusers.count:4}")
	private int convertUsersCount;

	@Value("${convertusers.username.prefix:cyao2pdf}")
	private String convertUsersNamePrefix;

	private static final int MAX_CONVERT_USERS = 8;

	public boolean textConvert(final String text) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());

		try {
			final String generatedPDFFileName = convertToPDF(bais, ".txt");
			if (!StringUtils.isEmpty(generatedPDFFileName)) {
				// delete file
				FileUtils.safeDeleteFile(generatedPDFFileName);
				return true;
			}
		} catch (final UnableToConvertException e) {
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
			final ExecReturnData returnData = syncExecProcess(new String[] { "soffice", "--version" });

			versionInfo += returnData.getProcessOutput();

			// 0 is normal
			if (returnData.getReturnCode() != 0) {
				versionInfo = "unable to get version info";
			}
		} catch (final Exception e) {
			logger.warn("unable to get version info", e);
			versionInfo = "unable to get version info";
		}

		return versionInfo;
	}

	class ExecReturnData {
		final String processOutput;
		int returnCode;

		ExecReturnData(final String processOutput, final int returnCode) {
			this.processOutput = processOutput;
			this.returnCode = returnCode;
		}

		String getProcessOutput() {
			return processOutput;
		}

		int getReturnCode() {
			return this.returnCode;
		}
	}

	private ExecReturnData execProcess(final String[] commandAndArgs) throws IOException, InterruptedException {
		final File workingDir = new File(uploadDir);

		final Process p = Runtime.getRuntime().exec(commandAndArgs, null, workingDir);
		final Scanner scanner = new Scanner(p.getInputStream());
		final StringBuilder processOutput = new StringBuilder();

		while (scanner.hasNext()) {
			final String line = scanner.nextLine();
			logger.debug(line);
			processOutput.append(line);
		}

		final int retVal = p.waitFor();
		scanner.close();

		return new ExecReturnData(processOutput.toString(), retVal);
	}

	private synchronized ExecReturnData syncExecProcess(final String[] commandAndArgs)
			throws IOException, InterruptedException {
		return execProcess(commandAndArgs);
	}

	private static Lock[] activeConversions = new Lock[MAX_CONVERT_USERS];

	static {
		for (int i = 0; i < MAX_CONVERT_USERS; i++) {
			activeConversions[i] = new ReentrantLock();
		}
	}

	private ExecReturnData tryLockExec(final String toConvertFileName, final long lockWaitMs)
			throws InterruptedException, IOException {
		for (int i = 0; i < MAX_CONVERT_USERS && i < convertUsersCount; i++) {

			final Lock lock = activeConversions[i];
			final boolean isAcquired = lock.tryLock(lockWaitMs, TimeUnit.MILLISECONDS);
			if (isAcquired) {
				try {
					final String userName = convertUsersNamePrefix + Integer.toString(i + 1);
					logger.info("trying to convert. user={},toConvertFileName={}", userName, toConvertFileName);
					return execProcess(new String[] { "sh", "convert.sh", userName, toConvertFileName });
				} finally {
					lock.unlock();
				}
			}
		}
		return null;
	}

	// can only have one instance of soffice running at a time
	protected int convertLocalFileToPDF(final String toConvertFileName) throws IOException, InterruptedException 
	{
		if (!convertUsersEnabled)
		{
			return syncExecProcess(new String[] { "soffice", "--headless", "--convert-to", "pdf", toConvertFileName })
					.getReturnCode();
		}

		// try lock convert
		long lockWaitMs = 5;
		final long lockTryMaxWaitMs = 50;
		final long lockTryIncMs = 5;
		final long lockStartMs = System.currentTimeMillis();
		long lastLogMs = System.currentTimeMillis();

		while (true) {
			final ExecReturnData execReturnData = tryLockExec(toConvertFileName, lockWaitMs);
			if (execReturnData != null) {
				return execReturnData.getReturnCode();
			}

			final long currentTimeMillis = System.currentTimeMillis();
			final long elapsedTimeMs = (currentTimeMillis - lockStartMs);
			if (elapsedTimeMs > 5000 && (currentTimeMillis - lastLogMs) > 5000) {
				lastLogMs = currentTimeMillis;
				logger.warn("waiting for available converter. toConvertFileName={},elapsedTimeMs={}", toConvertFileName, elapsedTimeMs);
			}

			if (lockWaitMs < lockTryMaxWaitMs) {
				lockWaitMs += lockTryIncMs;
			}
		}
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
			throws UnableToConvertException {
		// use a unique identified to save upload stream to disk
		final String uuid = UUID.randomUUID().toString();

		// this could be simplified, something strange with the exec call and
		// the working directory
		final String toConvertFileName = uuid + srcExtension;
		final String toConvertFullFileName = uploadDir + "/" + toConvertFileName;

		// if converted, it will be located here with this name
		final String pdfFileName = uploadDir + "/" + uuid + ".pdf";

		File fileToConvert = null;

		try {
			fileToConvert = new File(toConvertFullFileName);

			final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileToConvert));
			FileCopyUtils.copy(inputStream, stream);
			stream.close();

			// NOTE: clean this all up and sanity check everything
			// I was working with 3 hours sleep due to newborn, had to kill the
			// time somehow
			// likely should use ProcessBuilder instead of Runtime
			final int retVal = convertLocalFileToPDF(toConvertFileName);
			// 0 is normal
			if (retVal != 0) {
				throw new UnableToConvertException("UnableToConvert. bad return code. retVal=" + retVal);
			}

			// check that file was created..
			final File pdfFile = new File(pdfFileName);
			if (!pdfFile.exists()) {
				throw new UnableToConvertException("UnableToConvert. file not generated. pdfFileName=" + pdfFileName);
			}

		} catch (final Exception e) {
			throw new UnableToConvertException("UnableToConvert", e);
		} finally {
			// delete the uploaded file
			if (fileToConvert != null) {
				try {
					fileToConvert.delete();
				} catch (final Exception ex)
				{
					logger.warn("unable to delete uploaded file: {}", uuid, ex);
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
