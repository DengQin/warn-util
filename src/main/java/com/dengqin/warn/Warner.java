package com.dengqin.warn;

import com.dengqin.warn.BaseUtil.IpUtil;
import com.dengqin.warn.BaseUtil.JsonUtil;
import com.dengqin.warn.BaseUtil.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 警报器
 */
public class Warner {
	private static Logger log = LoggerFactory.getLogger(Warner.class);

	private static String sysCode = "";

	private static String heartbeatCode = "";

	private static String ip = "";

	private static boolean isInit = false;

	private static WarnChecker warnChecker = null;

	/**
	 * 线程心跳监控间隔不能小于该值
	 */
	private static long minThreadPeriod = 120L * 1000;

	private static final byte[] lock = new byte[1];

	/**
	 * 线程时间配置<br>
	 * 线程名，毫秒数
	 */
	private static Map<String, Long> threadConf = new ConcurrentHashMap<String, Long>();

	/**
	 * 线程运行记录<br>
	 * 线程名，当时时间毫秒
	 */
	private static Map<String, Long> threadRunInf = new ConcurrentHashMap<String, Long>();

	/**
	 * 初始化线程配置
	 * 
	 * @param threadName
	 *            线程名称
	 * @param periodMs
	 *            监控周期，毫秒
	 */
	public static void initThreadMonitor(String threadName, long periodMs) {
		// 避免误报
		periodMs = periodMs * 3 / 2;
		periodMs = Math.max(minThreadPeriod, periodMs);
		threadConf.put(threadName, periodMs);
		updateThreadRunInfo(threadName);
	}

	/**
	 * 更新线程运行时间
	 */
	public static void updateThreadRunInfo(String threadName) {
		threadRunInf.put(threadName, System.currentTimeMillis());
	}

	public static void updateThreadRunInfo(String threadName, long curTimeStamp) {
		threadRunInf.put(threadName, curTimeStamp);
	}

	/**
	 * 上报缓存中的警报
	 */
	public static void reportWarns() {
		checkThreadRun();
		BaseUtil.reportWarn(sysCode, heartbeatCode, getWarnJsonStr());
	}

	/**
	 * 检查线程运行状况
	 */
	private static void checkThreadRun() {
		if (threadConf == null && threadConf.size() == 0) {
			return;
		}
		try {
			for (Map.Entry<String, Long> e : threadConf.entrySet()) {
				String name = e.getKey();
				long period = e.getValue();
				Long ts = threadRunInf.get(name);
				if (ts == null) {
					warn(name + "的运行时间为空");
					continue;
				}
				if ((System.currentTimeMillis() - ts.longValue()) > period) {
					warn(name + "可能挂起了");
				}
			}
		} catch (Exception e) {
			warn(e);
		}

	}

	/**
	 * 存放警报,里面有log.error
	 */
	public static void warn(Exception e) {
		warn(e, e.getMessage(), "");
	}

	/**
	 * 存放警报,里面有log.error
	 */
	public static void warn(String message) {
		warn(null, message, "");
	}

	/**
	 * 存放警报,里面有log.error
	 */
	public static void warn(Exception e, String message) {
		warn(e, message, "");
	}

	/**
	 * 存放警报,里面有log.error <br>
	 * receivers:接收者,用,号分隔 <br>
	 */
	public static void warn(String message, String receivers) {
		warn(null, message, receivers);
	}

	/**
	 * 存放警报,里面有log.error <br>
	 * receivers:接收者,用,号分隔 <br>
	 */
	public static void warn(Exception e, String message, String receivers) {
		message = BaseUtil.getMessage(message);
		WarnVo warn = new WarnVo(sysCode, heartbeatCode, message, receivers, Constant.WARN_TYPE, ip);
		if (e == null) {
			log.error(warn.getSignCode() + "-" + message);
		} else {
			log.error(warn.getSignCode() + "-" + message, e);
		}
		if (!isInit) {
			log.error("警告配置还没初始化");
			return;
		}
		WarnPool.putWarnVo(warn);
	}

	/**
	 * 初始化类，自动定时发送警报，每60秒上报警报和心跳
	 * 
	 */
	public static void init(String sysCode, String heartbeatCode) {
		init(sysCode, heartbeatCode, null, 60, 0);
	}

	/**
	 * 初始化类，自动定时发送警报
	 * 
	 * @param period
	 *            警报发送的周期，秒
	 */
	public static void init(String sysCode, String heartbeatCode, int period) {
		init(sysCode, heartbeatCode, null, period, 0);
	}

	/**
	 * 初始化类，自动定时发送警报
	 * 
	 * @param warnChecker
	 *            异步发送时的检查类,可传null
	 * @param period
	 *            警报发送的周期，秒
	 */
	public static void init(String sysCode, String heartbeatCode, WarnChecker warnChecker, int period) {
		init(sysCode, heartbeatCode, warnChecker, period, 0);
	}

	/**
	 * 实时发放，不过滤重复的信息
	 */
	public static void sendMsg(String sysCode, String groupCode, String content) {
		BaseUtil.sendMsg(sysCode, groupCode, content);
	}

	/**
	 * 初始化类，自动定时发送警报
	 * 
	 * @param warnChecker
	 *            异步发送时的检查类,可传null
	 * @param period
	 *            异步发送的周期，秒
	 * @param minThreadPeriod
	 *            线程运行的最小检查时间
	 */
	public static void init(String sysCode, String heartbeatCode, WarnChecker warnChecker, int period,
			long minThreadPeriod) {
		if (StringUtil.isBlank(sysCode)) {
			log.error("报警功能初始化失败,sysCode[" + sysCode + "]");
			return;
		}
		if (isInit) {
			log.info("报警功能已经初始化过了");
			return;
		}
		synchronized (lock) {
			if (isInit) {
				log.info("报警功能已经初始化过了");
				return;
			}
			Warner.sysCode = sysCode;
			Warner.heartbeatCode = heartbeatCode;
			Warner.warnChecker = warnChecker;
			Warner.ip = IpUtil.getLocalIp();
			if (minThreadPeriod > 0) {
				Warner.minThreadPeriod = minThreadPeriod;
			}
			// 启动主动警报线程
			new Thread(new SendWarnThread(period * 1000)).start();
			log.info("报警功能初始化完毕,sysCode[{}],heartbeatCode[{}]", new Object[] { sysCode, heartbeatCode });
			isInit = true;
		}
	}

	/**
	 * 存放异常统计
	 */
	static void sendStat(String message) {
		message = BaseUtil.getMessage(message);
		WarnVo warn = new WarnVo(sysCode, message, ip);
		WarnPool.putWarnVo(warn);
	}

	/**
	 * 获取警报数据
	 */
	private static String getWarnJsonStr() {
		// 检查异常
		doCheck();
		List<WarnVo> list = WarnPool.getWarnVos();
		String message = null;
		if (!isInit) {
			message = "警告配置还没初始化";
			log.error(message);
			list.add(new WarnVo(sysCode, heartbeatCode, message, "", Constant.WARN_TYPE, ip));
		}
		return JsonUtil.toJson(list);
	}

	/**
	 * 检查系统是有异常
	 */
	private static void doCheck() {
		try {
			if (warnChecker != null) {
				warnChecker.check();
			}
		} catch (Exception e) {
			log.error("执行报警检查器出错" + e.getMessage(), e);
		}
	}

}

/**
 * 警报发送线程
 */
class SendWarnThread implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(SendWarnThread.class);

	private long period;

	public SendWarnThread(long period) {
		super();
		this.period = period;
	}

	public void run() {
		log.info("启动定时线程发送报警信息,发送周期" + (int) (period / 1000) + "秒");
		while (true) {
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			Warner.reportWarns();
		}

	}
}