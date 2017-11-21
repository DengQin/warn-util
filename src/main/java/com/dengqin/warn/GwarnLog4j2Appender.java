package com.dengqin.warn;

import com.dengqin.warn.BaseUtil.StringUtil;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

/**
 * 自定义实现log4j2的输出源
 */
@Plugin(name = "Gwarn", category = "Core", elementType = "appender", printObject = true)
public final class GwarnLog4j2Appender extends AbstractAppender {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3650550892005283720L;

	private static final byte[] lock = new byte[1];

	protected GwarnLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout, String sysCode,
			String period, final boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
		this.sysCode = sysCode;
		this.period = period;
	}

	private String sysCode;

	private String period;

	private boolean isInit = false;

	@Override
	public void append(LogEvent event) {
		if (isInit == false) {
			initWarn();
		}
		if (event != null && event.getMessage() != null) {
			if (!Warner.class.getName().equals(event.getLoggerName())) {
				Warner.sendStat(event.getMessage().getFormat());
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

	@PluginFactory
	public static GwarnLog4j2Appender createAppender(@PluginAttribute("name") String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter, @PluginAttribute("sysCode") String sysCode,
			@PluginAttribute("period") String period) {
		if (name == null) {
			LOGGER.error("No name provided for MyCustomAppenderImpl");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new GwarnLog4j2Appender(name, filter, layout, sysCode, period, true);
	}

}
