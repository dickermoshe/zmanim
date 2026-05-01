/*
 * Zmanim Java API
 * Copyright (C) 2004-2026 Eliyahu Hershfeld
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 */
package com.kosherjava.zmanim.util;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.kosherjava.zmanim.util.spa.DeltaT;
import com.kosherjava.zmanim.util.spa.SPA;
import com.kosherjava.zmanim.util.spa.SPA.SunriseResult;
import com.kosherjava.zmanim.util.spa.SolarPosition;

/**
 * Implementation of the NREL Solar Position Algorithm (SPA) for sunrise, sunset, transit, and sun position calculations.
 * This implementation is based on an internal fold-in of the MIT-licensed Java SPA implementation by Klaus Brunner.
 */
public class SPACalculator extends AstronomicalCalculator {

	/**
	 * Default constructor of the SPACalculator.
	 */
	public SPACalculator() {
		super();
	}

	@Override
	public String getCalculatorName() {
		return "NREL Solar Position Algorithm";
	}

	@Override
	public double getUTCSunrise(LocalDate localDate, GeoLocation geoLocation, double zenith, boolean adjustForElevation) {
		double adjustedZenith = adjustZenith(zenith, adjustForElevation ? geoLocation.getElevation() : 0);
		double elevationAngle = 90 - adjustedZenith;
		SunriseResult result = SPA.calculateSunriseTransitSet(getDayStart(localDate, geoLocation), geoLocation.getLatitude(),
				geoLocation.getLongitude(), resolveDeltaT(localDate), elevationAngle);
		if (result instanceof SunriseResult.RegularDay) {
			return toUTCDouble(((SunriseResult.RegularDay) result).getSunrise());
		}
		return Double.NaN;
	}

	@Override
	public double getUTCSunset(LocalDate localDate, GeoLocation geoLocation, double zenith, boolean adjustForElevation) {
		double adjustedZenith = adjustZenith(zenith, adjustForElevation ? geoLocation.getElevation() : 0);
		double elevationAngle = 90 - adjustedZenith;
		SunriseResult result = SPA.calculateSunriseTransitSet(getDayStart(localDate, geoLocation), geoLocation.getLatitude(),
				geoLocation.getLongitude(), resolveDeltaT(localDate), elevationAngle);
		if (result instanceof SunriseResult.RegularDay) {
			return toUTCDouble(((SunriseResult.RegularDay) result).getSunset());
		}
		return Double.NaN;
	}

	@Override
	public double getUTCNoon(LocalDate localDate, GeoLocation geoLocation) {
		SunriseResult result = SPA.calculateSunriseTransitSet(getDayStart(localDate, geoLocation), geoLocation.getLatitude(),
				geoLocation.getLongitude(), resolveDeltaT(localDate));
		return toUTCDouble(result.getTransit());
	}

	@Override
	public double getUTCMidnight(LocalDate localDate, GeoLocation geoLocation) {
		ZonedDateTime midnight = SPA.calculateSolarMidnight(getDayStart(localDate, geoLocation), geoLocation.getLatitude(),
				geoLocation.getLongitude(), resolveDeltaT(localDate));
		return toUTCDouble(midnight);
	}

	@Override
	public double getSolarElevation(ZonedDateTime zonedDateTime, GeoLocation geoLocation) {
		SolarPosition position = SPA.calculateSolarPosition(zonedDateTime, geoLocation.getLatitude(), geoLocation.getLongitude(),
				geoLocation.getElevation(), resolveDeltaT(zonedDateTime.toLocalDate()));
		return 90 - position.getZenithAngle();
	}

	@Override
	public double getSolarAzimuth(ZonedDateTime zonedDateTime, GeoLocation geoLocation) {
		SolarPosition position = SPA.calculateSolarPosition(zonedDateTime, geoLocation.getLatitude(), geoLocation.getLongitude(),
				geoLocation.getElevation(), resolveDeltaT(zonedDateTime.toLocalDate()));
		return position.getAzimuth();
	}

	private static ZonedDateTime getDayStart(LocalDate localDate, GeoLocation geoLocation) {
		return localDate.atStartOfDay(geoLocation.getZoneId());
	}

	private static double resolveDeltaT(LocalDate localDate) {
		return DeltaT.estimate(localDate);
	}

	private static double toUTCDouble(ZonedDateTime dateTime) {
		ZonedDateTime utc = dateTime.withZoneSameInstant(ZoneOffset.UTC);
		double value = utc.getHour()
				+ (utc.getMinute() + (utc.getSecond() + utc.getNano() / 1_000_000_000.0) / 60.0) / 60.0;
		return value >= 0 ? value % 24 : value % 24 + 24;
	}
}
