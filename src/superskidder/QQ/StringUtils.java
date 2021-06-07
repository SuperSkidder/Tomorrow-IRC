package superskidder.QQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import com.alibaba.fastjson.JSONObject;

public class StringUtils {

	public static byte[] readFile(String fileName){
		InputStream in = null;
		try {
			in = new FileInputStream(new File(fileName));
			byte[] bts = new byte[in.available()];
			in.read(bts);
			return bts;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	//URL解码
	public static String getURLDecoderString(String str) {
		String result = "";
		if (null == str) {
			return "";
		}
		try {
			result = java.net.URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("错误数据" + str);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 16进制转换成为string类型字符串
	 * @param s
	 * @return
	 */
	public static String hexStringToString(String s) {
		if (s == null || s.equals("")) {
			return null;
		}
		s = s.replace(" ", "");
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "UTF-8");
			new String();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}


	public static String subStr(String str,String left,String right){
		if(!str.contains(left)){
			return null;
		}
		int a = str.indexOf(left);
		int b = str.length();
		if(!isEmpty(right)){
			int c = str.indexOf(right,a + left.length() + 1);
			if(c != -1){
				b = c;
			}
		}
		return str.substring(a + left.length(), b);
	}

	/**
	 * 判断文本是否没有值
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		if(str == null || "".equals(str.trim())){
			return true;
		}
		return false;
	}


}

