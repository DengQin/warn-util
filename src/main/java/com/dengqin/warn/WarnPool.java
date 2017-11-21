package com.dengqin.warn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 报警池
 */
class WarnPool {

	private static Map<String, WarnVo> warnVoMap = new HashMap<String, WarnVo>();

	private static final int poolSize = 1000;

	private static final byte[] lock = new byte[1];

	/**
	 * 存储报警
	 */
	public static void putWarnVo(WarnVo vo) {
		String key = BaseUtil.getSimpleMessage(vo.getMessage()) + vo.getType();
		int size = warnVoMap.size();
		if (size < poolSize) {
			synchronized (lock) {
				WarnVo t = warnVoMap.get(key);
				if (t == null) {
					warnVoMap.put(key, vo);
				} else {
					vo.setHappenTimes(t.getHappenTimes() + 1);
					warnVoMap.put(key, vo);
				}
			}
		} else if (size == poolSize) {
			String message = "警报池满了！";
			vo.setMessage(message);
			synchronized (lock) {
				warnVoMap.put(message, vo);
			}
		}
	}

	/**
	 * 取出报警并清空报警池
	 */
	public static List<WarnVo> getWarnVos() {
		Map<String, WarnVo> curMap = null;
		synchronized (lock) {
			curMap = warnVoMap;
			warnVoMap = new HashMap<String, WarnVo>();
		}
		List<WarnVo> list = new LinkedList<WarnVo>();
		for (Map.Entry<String, WarnVo> entry : curMap.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}

}
