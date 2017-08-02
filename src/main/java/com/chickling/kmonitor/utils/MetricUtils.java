package com.chickling.kmonitor.utils;

import java.math.BigDecimal;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class MetricUtils {
	private static char[] UNIT = { 'k', 'm', 'b' };

	public static String rateFormat(Double rate, int interation) {
		if (rate < 100) {
			return new BigDecimal(rate).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
		} else {
			double value = (rate.longValue() / 100) / 10.0;
			boolean isRound = (value * 10) % 10 == 0;
			if (value < 1000) {
				if (value > 99.9 || isRound || (!isRound && value > 9.99)) {
					return new Double(value).intValue() * 10 / 10 + "" + UNIT[interation];
				} else {
					return value + "" + UNIT[interation];
				}
			} else {
				return rateFormat(value, interation + 1);
			}
		}
	}

	public static String sizeFormat(Double bytes) {
		int unit = 1024;
		if (bytes < unit) {
			return new BigDecimal(bytes).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " B";
		} else {
			int exp = new Double((Math.log(bytes) / Math.log(unit))).intValue();
			char pre = "KMGTPE".charAt(exp - 1);
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
	}
}
