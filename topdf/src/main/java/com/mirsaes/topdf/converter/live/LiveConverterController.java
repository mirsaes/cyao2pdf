package com.mirsaes.topdf.converter.live;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	private static final Logger logger = LogManager.getLogger(LiveConverterController.class);

	protected final ConverterService converterService;

	@Autowired
	LiveConverterController(ConverterService converterService)
	{
		this.converterService = converterService;
	}

	@Value("${security.enabled:false}")
	private Boolean securityEnabled;

	@RequestMapping(path = "health", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody()
	public Object health(@RequestParam("testConvert") Optional<Boolean> testConvert)
	{
		HealthCheckResp resp = new HealthCheckResp();

		resp.setVersionInfo(converterService.getVersionInfo());

		if (testConvert.isPresent() && testConvert.get())
		{
			PerformanceTimer perfTimer = new PerformanceTimer();
			perfTimer.start();
			boolean convertSuccess = converterService.textConvert("This is a test text to pdf conversion");
			perfTimer.stop();

			resp.setConvertSuccess(convertSuccess);
			resp.setConvertTimeMs(perfTimer.read());
		}

		return resp;
	}

	@RequestMapping(path = "test", method = RequestMethod.GET, produces = "text/html")
	@ResponseBody()
	public String test()
	{
		return "tested. upload dir=" + converterService.getUploadDir() + ", security=" + securityEnabled;
	}

	@RequestMapping(path = "urltopdf", method = RequestMethod.POST, produces = "application/pdf")
	public ResponseEntity<InputStreamResource> urltopdf(@RequestParam("name") String name,
			@RequestParam("file") String file)
	{
		final String srcExtension = getExtension(name);
		if (file.isEmpty() || StringUtils.isEmpty(srcExtension))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		final String pdfFileName;
		boolean isError = false;

		InputStream urlStream = null;

		PerformanceTimer perfTimer = new PerformanceTimer();
		perfTimer.start();

		try
		{
			URL fileURL = new URL(file);
			urlStream = fileURL.openConnection().getInputStream();

			pdfFileName = converterService.convertToPDF(urlStream, srcExtension);
		} catch (Exception ex)
		{
			isError = true;
			logger.warn("unable to convert to pdf.", ex);
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} finally
		{
			if (urlStream != null)
			{
				try
				{
					urlStream.close();
				} catch (Exception ex)
				{

				}
			}
			logger.info("convertTimeMs=" + perfTimer.read() + ",srcExtension=" + srcExtension + ", url=" + file
					+ ",isError=" + isError);

		}

		return sendPdfResponse(pdfFileName);
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

		PerformanceTimer perfTimer = new PerformanceTimer();
		perfTimer.start();

		long fileSizeBytes = file.getSize();

		boolean isError = false;

		final String pdfFileName;

		try
		{
			pdfFileName = converterService.convertToPDF(file.getInputStream(), srcExtension);
		} catch (Exception ex)
		{
			isError = true;
			logger.warn("unable to convert file. name="+name, ex);
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} finally
		{
			logger.info("convertTimeMs=" + perfTimer.read() + ",srcExtension=" + srcExtension + ", fileSizeBytes="
					+ fileSizeBytes  + ",isError=" + isError + ",name=" + name);
		}

		return sendPdfResponse(pdfFileName);
	}

	private ResponseEntity<InputStreamResource> sendPdfResponse(final String pdfFileName)
	{
		// supply a stream back, but delete the generated file on close
		try
		{
			// delete on close is "best effort", should have background timer
			// deleting older files..
			InputStreamResource isr = new InputStreamResource(new DeleteOnCloseFileInputStream(pdfFileName));
			return new ResponseEntity<>(isr, HttpStatus.CREATED);
		} catch (FileNotFoundException ex)
		{
			logger.warn("unable to convert file", ex);
			return new ResponseEntity<>(HttpStatus.GONE);
		} finally
		{

		}
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
