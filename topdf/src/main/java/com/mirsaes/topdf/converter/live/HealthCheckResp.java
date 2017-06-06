package com.mirsaes.topdf.converter.live;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) class HealthCheckResp
{
	private String versionInfo;
	private Boolean convertSuccess;
	private Long convertTimeMs;

	public String getVersionInfo()
	{
		return versionInfo;
	}

	public void setVersionInfo(String versionInfo)
	{
		this.versionInfo = versionInfo;
	}

	public Boolean isConvertSuccess()
	{
		return convertSuccess;
	}

	public void setConvertSuccess(boolean convertSuccess)
	{
		this.convertSuccess = convertSuccess;
	}

	public Long getConvertTimeMs()
	{
		return convertTimeMs;
	}

	public void setConvertTimeMs(long convertTimeMs)
	{
		this.convertTimeMs = convertTimeMs;
	}

}