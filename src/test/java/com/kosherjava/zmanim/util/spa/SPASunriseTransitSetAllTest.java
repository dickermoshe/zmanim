package com.kosherjava.zmanim.util.spa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class SPASunriseTransitSetAllTest {
	private static void assertWithinSeconds(String expectedIso, ZonedDateTime actual, long seconds) {
		ZonedDateTime expected = ZonedDateTime.parse(expectedIso);
		long diff = Math.abs(Duration.between(expected.toInstant(), actual.toInstant()).getSeconds());
		assertTrue("expected=" + expected + " actual=" + actual + " diff=" + diff, diff <= seconds);
	}

	@Test
	public void acceptsCustomElevationAngles() {
		ZonedDateTime day = ZonedDateTime.parse("2023-06-21T12:00:00Z");
		SPA.SunriseResult custom = SPA.calculateSunriseTransitSet(day, 48.8566, 2.3522, 0.0, -6.5);
		assertTrue(custom != null);
		SPA.SunriseResult enumResult = SPA.calculateSunriseTransitSet(day, 48.8566, 2.3522, 0.0, SPA.Horizon.ASTRONOMICAL_TWILIGHT);
		SPA.SunriseResult doubleResult = SPA.calculateSunriseTransitSet(day, 48.8566, 2.3522, 0.0, -18.0);
		assertEquals(enumResult.getClass(), doubleResult.getClass());
		Map<Double, SPA.SunriseResult> many = SPA.calculateSunriseTransitSet(day, 48.8566, 2.3522, 0.0, -5.0, -10.0, -15.0);
		assertEquals(3, many.size());
	}

	@Test
	public void rejectsInvalidElevationAngles() {
		ZonedDateTime day = ZonedDateTime.parse("2023-06-21T12:00:00Z");
		try {
			SPA.calculateSunriseTransitSet(day, 48.8566, 2.3522, 0.0, -91.0);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
		try {
			SPA.calculateSunriseTransitSet(day, 48.8566, 2.3522, 0.0, 91.0);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void rejectsNullValuesInResultRecords() {
		try {
			new SPA.SunriseResult.AllDay(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
		}
		try {
			new SPA.SunriseResult.AllNight(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testSpaExampleSunriseTransitSet() {
		ZonedDateTime time = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));
		SPA.SunriseResult res = SPA.calculateSunriseTransitSet(time, 39.742476, -105.1786, 67);
		assertEquals(SPA.SunriseResult.RegularDay.class, res.getClass());
		SPA.SunriseResult.RegularDay regular = (SPA.SunriseResult.RegularDay) res;
		assertWithinSeconds("2003-10-17T06:12:43-07:00", regular.getSunrise(), 1);
		assertWithinSeconds("2003-10-17T11:46:05-07:00", regular.getTransit(), 1);
		assertWithinSeconds("2003-10-17T17:20:19-07:00", regular.getSunset(), 1);
	}

	@Test
	public void testAllDayAndNightAndSillyLatLon() {
		ZonedDateTime summer = ZonedDateTime.of(2015, 6, 17, 12, 30, 30, 0, ZoneOffset.ofHours(2));
		assertEquals(SPA.SunriseResult.AllDay.class, SPA.calculateSunriseTransitSet(summer, 70.978056, 25.974722, 0).getClass());
		ZonedDateTime winter = ZonedDateTime.of(2015, 1, 17, 12, 30, 30, 0, ZoneOffset.ofHours(2));
		assertEquals(SPA.SunriseResult.AllNight.class, SPA.calculateSunriseTransitSet(winter, 70.978056, 25.974722, 0).getClass());

		ZonedDateTime time = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));
		try {
			SPA.calculateSunriseTransitSet(time, 139.742476, -105.1786, 67);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testBulkSunriseReferenceValues() {
		for (String line : SpaTestData.readDataLines("sunrise/spa_reference_testdata.csv")) {
			String[] p = line.split(",");
			if (p.length < 6) {
				continue;
			}
			ZonedDateTime dateTime = ZonedDateTime.parse(p[0]);
			double lat = Double.parseDouble(p[1]);
			double lon = Double.parseDouble(p[2]);
			LocalTime sunrise = parseLocalTime(p[3]);
			LocalTime sunset = parseLocalTime(p[5]);
			SPA.SunriseResult res = SPA.calculateSunriseTransitSet(dateTime, lat, lon, 0);
			if (sunrise != null) {
				assertEquals(SPA.SunriseResult.RegularDay.class, res.getClass());
				SPA.SunriseResult.RegularDay regular = (SPA.SunriseResult.RegularDay) res;
				assertWithinSeconds(withLocalTime(dateTime, sunrise), regular.getSunrise(), 1);
				assertWithinSeconds(withLocalTime(dateTime, sunset), regular.getSunset(), 1);
			}
		}
	}

	@Test
	public void testBulkUSNOAndCivilAndHorizons() {
		for (String line : SpaTestData.readDataLines("sunrise/usno_reference_testdata.csv")) {
			String[] p = line.split(",");
			ZonedDateTime dateTime = ZonedDateTime.parse(p[0]);
			double lat = Double.parseDouble(p[1]);
			double lon = Double.parseDouble(p[2]);
			SPA.SunriseResult res = SPA.calculateSunriseTransitSet(dateTime, lat, lon, 0);
			assertTrue(res != null);
		}
		for (String line : SpaTestData.readDataLines("sunrise/usno_reference_testdata_civil.csv")) {
			String[] p = line.split(",");
			ZonedDateTime dateTime = ZonedDateTime.parse(p[0]);
			double lat = Double.parseDouble(p[1]);
			double lon = Double.parseDouble(p[2]);
			SPA.SunriseResult res = SPA.calculateSunriseTransitSet(dateTime, lat, lon, 0, SPA.Horizon.CIVIL_TWILIGHT);
			if (res instanceof SPA.SunriseResult.RegularDay) {
				SolarPosition pos = SPA.calculateSolarPosition(((SPA.SunriseResult.RegularDay) res).getSunrise(), lat, lon, 0, 0);
				assertEquals(96, pos.getZenithAngle(), 0.2);
			}
		}

		ZonedDateTime dateTime = ZonedDateTime.parse("2023-03-01T12:00:00Z");
		Map<SPA.Horizon, SPA.SunriseResult> all = SPA.calculateSunriseTransitSet(dateTime, 60.1547, -1.1494, 69.2, SPA.Horizon.values());
		assertEquals(4, all.size());
	}

	@Test
	public void sunriseNearMidnightAndAntimeridianCases() {
		double latitude = 49.60139790853522;
		double longitude = 171.01752655220554;
		for (int dayOfMonth : new int[] {1, 2, 3}) {
			ZonedDateTime day = ZonedDateTime.parse(String.format("1986-06-%02dT00:00:00+11:00", dayOfMonth));
			double deltaT = DeltaT.estimate(day.toLocalDate());
			SPA.SunriseResult result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT, SPA.Horizon.ASTRONOMICAL_TWILIGHT);
			assertEquals(SPA.SunriseResult.RegularDay.class, result.getClass());
		}

		for (String line : SpaTestData.readDataLines("cities.csv")) {
			if (line.startsWith("name,")) {
				continue;
			}
			String[] p = line.split(",");
			double lat = Double.parseDouble(p[1]);
			double lon = Double.parseDouble(p[2]);
			ZonedDateTime start = ZonedDateTime.of(LocalDate.of(2023, Month.JANUARY, 1), LocalTime.of(12, 0), ZoneOffset.UTC);
			for (int i = 0; i < 20; i++) {
				ZonedDateTime dt = start.plusDays(i * 18L);
				SPA.SunriseResult res = SPA.calculateSunriseTransitSet(dt, lat, lon, 0, SPA.Horizon.CIVIL_TWILIGHT);
				assertTrue(res != null);
			}
		}
	}

	private static LocalTime parseLocalTime(String value) {
		String v = value == null ? "" : value.trim();
		if (v.isEmpty() || "null".equalsIgnoreCase(v)) {
			return null;
		}
		return LocalTime.parse(v);
	}

	private static String withLocalTime(ZonedDateTime baseDateTime, LocalTime time) {
		return ZonedDateTime.of(baseDateTime.toLocalDate(), time, baseDateTime.getOffset()).toString();
	}
}
