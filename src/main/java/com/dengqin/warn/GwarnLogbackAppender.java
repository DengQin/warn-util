package com.dengqin.warn;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.dengqin.warn.BaseUtil.StringUtil;

/**
 * 自定义实现logback的输出源
 */
public class GwarnLogbackAppender extends AppenderBase<ILoggingEvent> {

	private String sysCode;

	private String period;

	private boolean isInit = false;

	private static final byte[] lock = new byte[1];

	@Override
	protected void append(ILoggingEvent eventObject) {
		if (isInit == false) {
			initWarn();
		}
		if (eventObject != null && eventObject.getMessage() != null) {
			if (!Warner.class.getName().equals(eventObject.getLoggerName())) {
				Warner.sendStat(eventObject.getMessage());
			}
		}
	}

	private void initWarn() {
		if (StringUtil.isNotBlank(sysCode)) {
			synchronized (lock) {
				if (isInit) {
					return;
				}
				Warner.init(sysCode, "", StringUtil.parseInt(period, 60));
				isInit = true;
			}
		} else {
			isInit = true;
		}
	}

	public void setSysCode(String sysCode) {
		this.sysCode = sysCode;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

}
