
package com.ldw.music.utils;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.DecimalFormat;

public class StringHelper {
	public static enum CharType {
		DELIMITER,
		NUM,
		LETTER,
		OTHER,
		CHINESE;
	}


	public static CharType checkType(char c) {
		CharType ct = null;


		if ((c >= 0x4e00) && (c <= 0x9fbb)) {
			ct = CharType.CHINESE;
		}


		else if ((c >= 0xff00) && (c <= 0xffef)) {
			if (((c >= 0xff21) && (c <= 0xff3a))
					|| ((c >= 0xff41) && (c <= 0xff5a))) {
				ct = CharType.LETTER;
			}


			else if ((c >= 0xff10) && (c <= 0xff19)) {
				ct = CharType.NUM;
			}


			else
				ct = CharType.DELIMITER;
		}


		else if ((c >= 0x0021) && (c <= 0x007e)) {
			if ((c >= 0x0030) && (c <= 0x0039)) {
				ct = CharType.NUM;
			}
			else if (((c >= 0x0041) && (c <= 0x005a))
					|| ((c >= 0x0061) && (c <= 0x007a))) {
				ct = CharType.LETTER;
			}

			else
				ct = CharType.DELIMITER;
		}


		else if ((c >= 0x00a1) && (c <= 0x00ff)) {
			if ((c >= 0x00c0) && (c <= 0x00ff)) {
				ct = CharType.LETTER;
			} else
				ct = CharType.DELIMITER;
		} else
			ct = CharType.OTHER;

		return ct;
	}


	public static char getPinyinFirstLetter(char c) {
		String[] pinyin = null;
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		try {

			pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}


		if (pinyin == null) {
			return 0;
		}


		return pinyin[0].charAt(0);
	}

	public static String bytesToMB(long bytes) {
		float size = (float) (bytes * 1.0 / 1024 / 1024);
		String result = null;
		if (bytes >= (1024 * 1024)) {
			result = new DecimalFormat("###.00").format(size) + "MB";
		} else {
			result = new DecimalFormat("0.00").format(size) + "MB";
		}
		return result;
	}


	public static String getPingYin(String inputString) {
		if (TextUtils.isEmpty(inputString)) {
			return "";
		}
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();
		String output = "";

		try {
			for (int i = 0; i < input.length; i++) {
				if (java.lang.Character.toString(input[i]).matches(
						"[\\u4E00-\\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							input[i], format);
					if (temp == null || TextUtils.isEmpty(temp[0])) {
						continue;
					}
					output += temp[0].replaceFirst(temp[0].substring(0, 1),
							temp[0].substring(0, 1).toUpperCase());
				} else
					output += java.lang.Character.toString(input[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
