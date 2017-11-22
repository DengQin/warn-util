package com.dengqin.warn;

import com.dengqin.warn.BaseUtil.StringUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 自定义实现log4j的输出源<br/>
 * 
 * 调用Log4j输出日志时，调用各个组件的顺序: <br/>
 * 1、日志信息传入 Logger。<br/>
 * 2、将日志信息封装成 LoggingEvent 对象并传入 Appender。 <br/>
 * 3、在 Appender 中调用 Filter 对日志信息进行过滤，调用 Layout 对日志信息进行格式化，然后输出
 */
public class GwarnLog4jAppender extends AppenderSkeleton {

	private String sysCode;

	private String period;

	private boolean isInit = false;

	private static final byte[] lock = new byte[1];

	@Override
	public void close() {
	}

	// 是否需要按格式输出文本
	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (isInit == false) {
			initWarn();
		}
		if (event != null && event.getMessage() != null) {
			if (!Warner.class.getName().equals(event.getLoggerName())) {
				Warner.sendStat(event.getMessage().toString());
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
