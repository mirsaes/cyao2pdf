package com.mirsaes.topdf.converter.live;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/live")
class LiveConverterController
{
	private static final Logger logger = Logger.getLogger(LiveConverterController.class);

	@Value("${upload.dir}")
	private String uploadDir;

	@Value("${security.enabled:false}")
	private Boolean securityEnabled;

	@RequestMapping(path = "test", method = RequestMethod.GET, produces = "text/html")
	@ResponseBody()
	public String test()
	{
		return "tested. upload dir=" + uploadDir + ", security=" + securityEnabled;
	}

	@RequestMapping(path = "topdf", method = RequestMethod.POST, produces = "application/pdf")
	public ResponseEntity<InputStreamResource> topdf(@RequestParam("name") String name,
			@RequestParam("file") MultipartFile file)
	{
		// TODO: validate inputs exhaustively (name, file)
		// must have an extension, otherwise conversion will fail
		final String srcExtension = getExtension(name);
		if (file.isEmpty() || StringUtils.isEmpty(srcExtension))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		long fileSizeBytes=file.getSize();
		long startTimeNs = System.nanoTime();
		boolean isError = false;
		
		final String pdfFileName;
		try
		{
			pdfFileName = convertToPDF(file.getInputStream(), srcExtension);
		} catch (Exception ex)
		{
			isError = true;
			logger.warn("unable to convert file", ex);
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} finally {
			logger.info("convertTimeMs=" + (System.nanoTime() - startTimeNs) / 1000 + ",srcExtension=" + srcExtension
					+ ", fileSizeBytes=" + fileSizeBytes + ",isError="+isError);
		}

		// supply a stream back, but delete the generated file on close
		try
		{
			// delete on close is "best effort", should have background timer deleting older files..
			InputStreamResource isr = new InputStreamResource(new DeleteOnCloseFileInputStream(pdfFileName));
			return new ResponseEntity<>(isr, HttpStatus.CREATED);
		} catch (FileNotFoundException ex)
		{
			logger.warn("unable to convert file", ex);
			return new ResponseEntity<>(HttpStatus.GONE);
		} finally {
			
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
	private String convertToPDF(final InputStream inputStream, final String srcExtension)
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
			File workingDir = new File(uploadDir);
			Process p = Runtime.getRuntime().exec(
					new String[] { "soffice", "--headless", "--convert-to", "pdf", toConvertFileName }, null,
					workingDir);

			// TODO: if process "hangs", should likely kill the process
			Scanner scanner = new Scanner(p.getInputStream());
			while (scanner.hasNext())
			{
				logger.debug(scanner.nextLine());
			}

			int retVal = p.waitFor();
			scanner.close();
			// 0 is normal
			if (retVal != 0)
			{
				throw new UnableToConvertException("UnableToConvert");
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

	// return the extension of the file (anything after the last ".")
	private String getExtension(final String name)
	{
		if (name != null)
		{
			int lastDotIdx = name.lastIndexOf('.');
			if (lastDotIdx <= 0)
				return null;

			return name.substring(lastDotIdx);
		}

		return null;
	}
}