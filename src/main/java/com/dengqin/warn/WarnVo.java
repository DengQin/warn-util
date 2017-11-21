package com.dengqin.warn;

/**
 * 警报消息体
 */
class WarnVo {

	/**
	 * 系统代号
	 */
	private String sysCode;

	/**
	 * 心跳代号
	 */
	private String heartbeatCode;

	/**
	 * 信息
	 */
	private String message;

	/**
	 * 标志码
	 */
	private String signCode;

	/**
	 * 接收者,用,号分隔
	 */
	private String receivers;

	/**
	 * 发生次数
	 */
	private int happenTimes;

	/**
	 * 类型：warn警报，stat异常统计
	 */
	private String type;

	private String ip;

	public WarnVo(String sysCode, String heartbeatCode, String message, String receivers, String type, String ip) {
		super();
		this.sysCode = sysCode;
		this.heartbeatCode = heartbeatCode;
		this.message = message;
		this.signCode = BaseUtil.getSignCode();
		this.receivers = receivers;
		this.happenTimes = 1;
		this.type = type;
		this.ip = ip;
	}

	public WarnVo(String sysCode, String message, String ip) {
		super();
		this.sysCode = sysCode;
		this.heartbeatCode = "";
		this.message = message;
		this.signCode = "";
		this.receivers = "";
		this.happenTimes = 1;
		this.type = Constant.STAT_TYPE;
		this.ip = ip;
	}

	public String getSysCode() {
		return sysCode;
	}

	public void setSysCode(String sysCode) {
		this.sysCode = sysCode;
	}

	public String getHeartbeatCode() {
		return heartbeatCode;
	}

	public void setHeartbeatCode(String heartbeatCode) {
		this.heartbeatCode = heartbeatCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSignCode() {
		return signCode;
	}

	public void setSignCode(String signCode) {
		this.signCode = signCode;
	}

	public String getReceivers() {
		return receivers;
	}

	public void setReceivers(String receivers) {
		this.receivers = receivers;
	}

	public int getHappenTimes() {
		return happenTimes;
	}

	public void setHappenTimes(int happenTimes) {
		this.happenTimes = happenTimes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "WarnVo [sysCode=" + sysCode + ", heartbeatCode=" + heartbeatCode + ", message=" + message
				+ ", signCode=" + signCode + ", receivers=" + receivers + ", happenTimes=" + happenTimes + ", type="
				+ type + ", ip=" + ip + "]";
	}

}
