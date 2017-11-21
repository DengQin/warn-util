package com.dengqin.warn;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class BaseUtil {

	private static Logger log = LoggerFactory.getLogger(BaseUtil.class);

	/**
	 * 警报上报（需要根据具体的接口变动）
	 * 
	 * @param sysCode
	 *            系统代号
	 * @param heartbeatCode
	 *            心跳代号
	 * @param content
	 *            内容
	 */
	public static void reportWarn(String sysCode, String heartbeatCode, String content) {
		Map<String, String> params = new HashMap<String, String>(4, 1);
		params.put("sysCode", sysCode);
		params.put("heartbeatCode", heartbeatCode);
		params.put("content", content);
		// 重试3次
		for (int i = 0; i < 3; i++) {
			try {
				String s = NetUtil.postURL(Constant.WARN_URL, params);
				if (!"1".equals(StringUtil.trim(s))) {
					log.error("发送警报信息出错！接口返回:" + s);
				}
				return;
			} catch (Exception e) {
				log.error("发送警报信息出错！异常信息" + e.getMessage(), e);
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 警报消息发送（需要根据具体的接口和场景变动，比如发QQ消息、邮件等，根据接口来实现）
	 * 
	 * @param sysCode
	 *            系统代号
	 * @param groupCode
	 * @param content
	 *            内容
	 */
	public static void sendMsg(String sysCode, String groupCode, String content) {
		Map<String, String> params = new HashMap<String, String>(4, 1);
		params.put("sysCode", sysCode);
		params.put("groupCode", groupCode);
		params.put("content", content);
		try {
			String s = NetUtil.postURL(Constant.SEND_MSG_URL, params);
			if (!"1".equals(StringUtil.trim(s))) {
				log.error("发送警报信息出错！接口返回:" + s);
			}
			return;
		} catch (Exception e) {
			log.error("发送YY群信息出错！异常信息" + e.getMessage(), e);
		}
	}

	/** 获取签名号 */
	public static String getSignCode() {
		return StringUtil.getTimeString() + StringUtil.getRandomString(3);
	}

	/** 截取长消息 */
	public static String getMessage(String message) {
		if (message == null) {
			return "";
		}
		int length = message.length();
		if (length <= 200) {
			return message;
		}
		return message.substring(0, 100) + "......" + message.substring(length - 100, length);
	}

	public static String getSimpleMessage(String message) {
		if (message == null) {
			return "";
		}
		char[] arr = message.toCharArray();
		boolean found = false;
		StringBuffer string = new StringBuffer();
		for (char c : arr) {
			if (c == '<') {
				found = true;
				continue;
			}
			if (c == '>') {
				found = false;
				continue;
			}
			if (!found) {
				string.append(c);
			}
		}
		return string.toString();
	}

	protected static class JsonUtil {
		private static Logger log = LoggerFactory.getLogger(JsonUtil.class);

		private static ObjectMapper mapper = new ObjectMapper(); // can reuse,
																	// share

		public static String toJson(Object obj) {
			if (obj == null) {
				return null;
			}
			try {
				String str = mapper.writeValueAsString(obj);
				return str;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}

		public static <T> T toObject(String content, Class<T> valueType) {
			if (StringUtil.isBlank(content)) {
				return null;
			}
			try {
				return mapper.readValue(content, valueType);
			} catch (Exception e) {
				log.error(e.getMessage() + ",content[" + content + "]转换出错", e);
				throw new RuntimeException(e);
			}
		}

		/**
		 * json转List
		 * 
		 * @param content
		 *            json数据
		 * @param valueType
		 *            泛型数据类型
		 * @return
		 */
		public static <T> List<T> toListObject(String content, Class<T> valueType) {
			try {
				return mapper.readValue(content,
						mapper.getTypeFactory().constructParametricType(List.class, valueType));
			} catch (Exception e) {
				log.error(e.getMessage() + ",content[" + content + "]转换出错", e);
				throw new RuntimeException(e);
			}
		}

		public static Map<?, ?> toMap(String content) {
			if (StringUtil.isBlank(content)) {
				return null;
			}
			try {
				Map<?, ?> map = mapper.readValue(content, Map.class);
				return map;
			} catch (Exception e) {
				log.error(e.getMessage() + ",content[" + content + "]转换出错", e);
				throw new RuntimeException(e);
			}
		}

	}

	protected static class EncryptUtil {

		public static String getMD5(String str) {
			return encode(str, "MD5");
		}

		public static String getSHA1(String str) {
			return encode(str, "SHA-1");
		}

		public static String getLittleMD5(String str) {
			String estr = encode(str, "MD5");
			return estr.substring(0, 20);
		}

		public static String getLittleSHA1(String str) {
			String estr = encode(str, "SHA-1");
			return estr.substring(0, 20);
		}

		private static String encode(String str, String type) {
			try {
				MessageDigest alga = MessageDigest.getInstance(type);
				alga.update(str.getBytes("UTF-8"));
				byte[] digesta = alga.digest();
				return byte2hex(digesta);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		public static String byte2hex(byte[] b) {
			String hs = "";
			String stmp = "";
			for (int n = 0; n < b.length; n++) {
				stmp = (Integer.toHexString(b[n] & 0XFF));
				if (stmp.length() == 1)
					hs = hs + "0" + stmp;
				else
					hs = hs + stmp;
			}
			return hs.toUpperCase();
		}

	}

	protected static class NetUtil {

		private static final Logger log = LoggerFactory.getLogger(NetUtil.class);

		public static String postURL(String url, Map<String, String> params) {
			PrintWriter out = null;
			BufferedReader in = null;
			StringBuilder sb = new StringBuilder(256);
			String result = "";
			try {
				int i = 0;
				for (Map.Entry<String, String> entry : params.entrySet()) {
					if (i != 0) {
						sb.append('&');
					}
					sb.append(URLEncoder.encode(entry.getKey(), "utf-8"));
					sb.append("=");
					sb.append(URLEncoder.encode(entry.getValue(), "utf-8"));
					i++;
				}
				String content = sb.toString();
				URL realUrl = new URL(url);
				// 打开和URL之间的连接
				URLConnection conn = realUrl.openConnection();
				// for (Map.Entry<String, String> entry : heads.entrySet()) {
				// conn.setRequestProperty(entry.getKey(), entry.getValue());
				// }
				// 发送POST请求必须设置如下两行
				conn.setDoOutput(true);
				conn.setDoInput(true);
				// 获取URLConnection对象对应的输出流
				out = new PrintWriter(conn.getOutputStream());
				// 发送请求参数
				out.print(content);
				// flush输出流的缓冲
				out.flush();
				// 定义BufferedReader输入流来读取URL的响应
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				String line;
				while ((line = in.readLine()) != null) {
					result += line;
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				IOUtil.closeQuietly(out);
				IOUtil.closeQuietly(in);
			}
			return result;
		}

	}

	protected static class IOUtil {
		public static void closeQuietly(InputStream input) {
			closeQuietly((Closeable) input);
		}

		public static void closeQuietly(OutputStream output) {
			closeQuietly((Closeable) output);
		}

		public static void closeQuietly(Closeable closeable) {
			try {
				if (closeable != null) {
					closeable.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}

	protected static class IpUtil {
		public static String getLocalIp() {
			Enumeration<NetworkInterface> allNetInterfaces = null;
			try {
				allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {
				return "";
			}
			InetAddress inetAddress = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = allNetInterfaces.nextElement();
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					inetAddress = addresses.nextElement();
					if (inetAddress != null && inetAddress instanceof Inet4Address) {
						String ip = inetAddress.getHostAddress();
						if ("127.0.0.1".equals(StringUtil.trim(ip))) {
							continue;
						}
						if ("localhost".equals(inetAddress.getHostName())) {
							continue;
						}
						return ip;
					}
				}
			}
			return "";
		}
	}

	protected static class StringUtil {
		private static final String RANDOM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		/**
		 * 获得currentTimeMillis的压缩字符串
		 */
		public static String getTimeString() {
			return getTimeString(System.currentTimeMillis());
		}

		/**
		 * 获得currentTimeMillis的压缩字符串
		 */
		public static String getTimeString(long time) {
			char[] arr = RANDOM_CHARS.toCharArray();
			StringBuilder str = new StringBuilder();
			int length = arr.length;
			while (time != 0) {
				str.append(arr[(int) (time % length)]);
				time = time / length;
			}
			return str.toString();
		}

		/**
		 * 返回压缩字符串对应的时间
		 */
		public static long getTime(String compressorStr) {
			char[] arr = compressorStr.toCharArray();
			long result = 0;
			int length = RANDOM_CHARS.length();
			for (int i = arr.length - 1; i >= 0; i--) {
				char c = arr[i];
				int index = RANDOM_CHARS.indexOf(c);
				result = length * result + index;
			}
			return result;
		}

		public static String trim(String str) {
			if (str == null) {
				return "";
			}
			return str.trim();
		}

		public static int parseInt(String str, int defaultValue) {
			try {
				return Integer.parseInt(str);
			} catch (Exception e) {
				return defaultValue;
			}
		}

		/**
		 * 获得一个随机的字符串
		 * 
		 */
		public static String getRandomString(int len) {
			StringBuilder buf = new StringBuilder(len + 1);

			Random rand = new Random();
			for (int i = 0; i < len; i++) {
				buf.append(RANDOM_CHARS.charAt(rand.nextInt(RANDOM_CHARS.length())));
			}
			return buf.toString();
		}

		/**
		 * 检查空字符串，或者" "字符串
		 * 
		 * @param str
		 * @return
		 */
		public static boolean isBlank(String str) {
			int strLen;
			if (str == null || (strLen = str.length()) == 0) {
				return true;
			}
			for (int i = 0; i < strLen; i++) {
				if ((Character.isWhitespace(str.charAt(i)) == false)) {
					return false;
				}
			}
			return true;
		}

		public static boolean isNotBlank(String str) {
			int strLen;
			if (str == null || (strLen = str.length()) == 0) {
				return false;
			}
			for (int i = 0; i < strLen; i++) {
				if ((Character.isWhitespace(str.charAt(i)) == false)) {
					return true;
				}
			}
			return false;
		}

	}

}
