/*
 * Zmanim Java API
 * Copyright (C) 2004-2026 Eliyahu Hershfeld
 *
 * This file includes adapted code from the SPA implementation by Klaus A. Brunner
 * (MIT License). See the root-level SPA source attribution for details.
 */
package com.kosherjava.zmanim.util.spa;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Calculate Julian date for a given point in time.
 */
final class JulianDate {
	private final double julianDate;
	private final double deltaT;

	JulianDate(final ZonedDateTime date) {
		this(date, 0.0);
	}

	JulianDate(final ZonedDateTime date, final double deltaT) {
		this(calcJulianDate(createUtcDateTime(date).toLocalDateTime()), deltaT);
	}

	JulianDate(double julianDate, double deltaT) {
		this.julianDate = julianDate;
		this.deltaT = deltaT;
	}

	double getJulianDate() {
		return julianDate;
	}

	static ZonedDateTime createUtcDateTime(final ZonedDateTime fromDateTime) {
		return fromDateTime.withZoneSameInstant(ZoneOffset.UTC);
	}

	private static double calcJulianDate(LocalDateTime localDateTime) {
		int y = localDateTime.getYear();
		int m = localDateTime.getMonthValue();

		if (m < 3) {
			y = y - 1;
			m = m + 12;
		}

		final double d = localDateTime.getDayOfMonth()
				+ (localDateTime.getHour() + (localDateTime.getMinute() + localDateTime.getSecond() / 60.0) / 60.0) / 24.0;
		final double jd = Math.floor(365.25 * (y + 4716.0)) + Math.floor(30.6001 * (m + 1)) + d - 1524.5;
		final double a = Math.floor(y / 100.0);
		final double b = jd > 2299160.0 ? (2.0 - a + Math.floor(a / 4.0)) : 0.0;

		return jd + b;
	}

	double julianEphemerisDay() {
		return julianDate + deltaT / 86400.0;
	}

	double julianCentury() {
		return (julianDate - 2451545.0) / 36525.0;
	}

	double julianEphemerisCentury() {
		return (julianEphemerisDay() - 2451545.0) / 36525.0;
	}

	double julianEphemerisMillennium() {
		return julianEphemerisCentury() / 10.0;
	}
}
