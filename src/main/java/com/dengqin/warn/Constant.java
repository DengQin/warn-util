package com.dengqin.warn;

class Constant {

	// 报警接口，将搜集的报警推送到某个系统，由某个系统继续具体的报警发放（需要自己实现）
	static final String WARN_URL = "http://warn.xxx.com/ws/receive/receive.do";

	// 消息发送接口，将报警发送到某个地方，比如发送QQ、邮件等（需要自己实现）
	static final String SEND_MSG_URL = "http://warn.yyy.com/msg/send.do";

	// 报警类型
	static final String WARN_TYPE = "warn";

	// 异常监控类型
	static final String STAT_TYPE = "stat";

}
