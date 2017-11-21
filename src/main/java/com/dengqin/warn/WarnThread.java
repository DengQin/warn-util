package com.dengqin.warn;

/**
 * 报警线程
 */
public abstract class WarnThread extends Thread {

	public WarnThread() {
		this(true);
	}

	public WarnThread(boolean initThread) {
		super();
		if (initThread) {
			// 初始化线程监控配置
			Warner.initThreadMonitor(getThreadName(), getPeriod());
		}
	}

	@Override
	public void run() {
		// 更新运行时间
		long startTimeStamp = System.currentTimeMillis();
		try {
			Warner.updateThreadRunInfo(getThreadName(), startTimeStamp);
		} catch (Exception e) {
			Warner.warn(e, getThreadName() + "更新运行时间出错" + e.getMessage());
		}
		// 执行任务
		try {
			doRun(startTimeStamp);
		} catch (Exception e) {
			Warner.warn(e, getThreadName() + "出错" + e.getMessage());
		}
	}

	/**
	 * 更新心跳时间
	 */
	public void updateHeartbeat(long startTimeStamp) {
		if (needUpdateHeartbeat(startTimeStamp)) {
			Warner.updateThreadRunInfo(getThreadName());
		}
	}

	/**
	 * 是否需要更新心跳时间，for循环的任务，执行时间可能超过运行周期，这会产生误报，所以需要判断是否超了周期，跳出循环
	 */
	public boolean needUpdateHeartbeat(long startTimeStamp) {
		long nowTs = System.currentTimeMillis();
		long period = getPeriod();
		if (nowTs - startTimeStamp >= period) {
			// 超出运行周期
			return true;
		}
		return false;
	}

	/**
	 * 线程的名称
	 */
	public String getThreadName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 使用中需要实现的方法
	 */
	public abstract void doRun(long startTimeStamp);

	/**
	 * 线程运行的周期，毫秒
	 */
	public abstract long getPeriod();
}
