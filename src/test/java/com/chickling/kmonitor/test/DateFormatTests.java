package com.chickling.kmonitor.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月6日
 * @description
 *
 */
public class DateFormatTests {

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sFormatForRangeto = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		System.out.println(sFormatForRangeto.parse("2017-07-06 17:08"));
	}
}
