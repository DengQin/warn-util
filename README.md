# warn-util
一个报警器工具：只是实现了报警池部分，报警池可以将数据异步输出到其他系统，由其他专门的报警系统来进行警报的管理和统计
即：// 报警接口，将搜集的报警推送到某个系统，由某个系统继续具体的报警发放（需要自己实现）
  	static final String WARN_URL = "http://warn.xxx.com/ws/receive/receive.do";
  	// 消息发送接口，将报警发送到某个地方，比如发送QQ、邮件等（需要自己实现）
  	static final String SEND_MSG_URL = "http://warn.yyy.com/msg/send.do";
  	这2个接口需要自行构造系统实现
  	
    1、作用：
        1.1、可以监控业务和系统的运行情况
        2.1、在日志中加入报警输出源，可以监控上报系统中的异常情况

    2、使用：
        2.1配置
        Warner.init(sysCode, heartbeatCode);
        2.2警报上报
        调用类com.dengqin.warn.Warner的 warn方法
        2.3收集异常数据
        	增加以下apperder
        logback:
        <!--异常收集 -->
        <appender name="gwarn" class="com.dengqin.warn.GwarnLogbackAppender">
        	<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        		<level>ERROR</level>
        	</filter>
        </appender>
        <root>
        	<level value="INFO" />
        	<appender-ref ref="gwarn" />
        </root>

        log4j:
        log4j.rootLogger=INFO,gwarn
        log4j.appender.gwarn=com.dengqin.warn.GwarnLog4jAppender
        log4j.appender.gwarn.Threshold = ERROR

        log4j2:
        <configuration packages="com.dengqin.warn">
        	<appenders>
        		<Gwarn name="gwarn">
        			<ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY" />
        		</Gwarn>
        	</appenders>
        	<loggers>
        		<root level="info">
        			<appender-ref ref="gwarn" />
        		</root>
        	</loggers>
        </configuration>
        ------------------------------------------------------------------------------------------------------
        2.4日志框架配置
        这种方式不能配置心跳监控
        logback：
        <appender name="gwarn" class="com.dengqin.warn.GwarnLogbackAppender">
        	<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        		<level>ERROR</level>
        	</filter>
        	<sysCode> ${sysCode}</sysCode>
        	<period>60</period>
        </appender>
        <root>
        	<level value="INFO" />
        	<appender-ref ref="gwarn" />
        </root>

        log4j：
        log4j.rootLogger=INFO,gwarn
        log4j.appender.gwarn=com.dengqin.warn.GwarnLog4jAppender
        log4j.appender.gwarn.Threshold = ERROR
        log4j.appender.gwarn.sysCode= ${sysCode}
        log4j.appender.gwarn.period=60

        log4j2：
        <configuration status="OFF" packages="com.dengqin.warn">
        	<appenders>
        		<Gwarn name="gwarn" sysCode="${sysCode}" period="60">
        			<ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY" />
        		</Gwarn>
        	</appenders>
        	<loggers>
        		<root level="info">
        			<appender-ref ref="gwarn" />
        		</root>
        	</loggers>
        </configuration>
        ------------------------------------------------------------------------------------------------------
        2.5监控线程是否运行正常
            2.5.1自己new线程或者用quartz 的情况
                使用这种方式要注意线程执行时的for循环操作，要及时更新线程信息。推荐用2.5.2的方法。
                1.初始化：Warner.initThreadMonitor(threadName, periodMs);
                2.每次运行线程：Warner.updateThreadRunInfo(threadName);
            2.5.2使用线程池的情况
                public class AppInitTables extends WarnThread {

                	@Override
                	public void doRun(long startTimeStamp) {
                		//业务处理
                	}

                	@Override
                	public long getPeriod() {
                		return AbstractCMD.THOUSAND * 3600 * 24;
                	}
                }

                main方法，该类继承AbstractCMD:
                WarnThread appInitTables = new AppInitTables(...);
                ScheduledThreadPoolExecutor exector = createScheduledThreadPoolExecutor(8);
                scheduleAtFixedRate(exector, appInitTables);

                AbstractCMD.java:
                public final static long THOUSAND = 1000L;
                static void scheduleAtFixedRate(ScheduledThreadPoolExecutor exector, WarnThread warnThread) {
                	exector.scheduleAtFixedRate(warnThread, THOUSAND, warnThread.getPeriod(), TimeUnit.MILLISECONDS);
                }

                protected static final ScheduledThreadPoolExecutor createScheduledThreadPoolExecutor(int workThreadNum) {
                		final ScheduledThreadPoolExecutor poolExecutor = new ScheduledThreadPoolExecutor(workThreadNum);
                		// 设置有关在此执行程序已 shutdown 的情况下是否继续执行现有定期任务的策略。
                		poolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
                		// 设置有关在此执行程序已 shutdown 的情况下是否继续执行现有延迟任务的策略。
                		poolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
                		return poolExecutor;
                }

            2.5.3 needUpdateHeartbeat方法使用
            该方法作用：判断是否需要中断运行，for循环的任务，执行时间可能超过运行周期，这会产生误报，所以需要判断是否超了周期，跳出循环。
            	@Override
            	public void doRun(long startTimeStamp) {
            		List<OrderQueue> list = orderService.list(10000);
            		for (OrderQueue orderQueue : list) {
            			if (needUpdateHeartbeat (startTimeStamp)) {
            				break;或者 Warner.updateThreadRunInfo(getThreadName());
            			}
            			orderService.move(orderQueue);
            		}
            	}













