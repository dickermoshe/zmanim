package com.kosherjava.zmanim.util.spa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

public class SPATest {

	private static final double TOLERANCE = 0.0001;

	@Test
	public void testSpaExample() {
		ZonedDateTime time = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));
		SolarPosition result = SPA.calculateSolarPosition(time, 39.742476, -105.1786, 1830.14, 67, 820, 11);
		assertEquals(194.340241, result.getAzimuth(), TOLERANCE / 100);
		assertEquals(50.111622, result.getZenithAngle(), TOLERANCE / 100);
	}

	@Test
	public void testSouthernSolstice() {
		ZonedDateTime time = ZonedDateTime.of(2012, 12, 22, 12, 0, 0, 0, ZoneOffset.UTC);
		SolarPosition result = SPA.calculateSolarPosition(time, -41, 0, 100, 0, 1000, 20);
		assertEquals(359.08592, result.getAzimuth(), TOLERANCE);
		assertEquals(17.5658, result.getZenithAngle(), TOLERANCE);

		result = SPA.calculateSolarPosition(time, -3, 0, 100, 0, 1000, 20);
		assertEquals(180.790356, result.getAzimuth(), TOLERANCE);
		assertEquals(20.4285, result.getZenithAngle(), TOLERANCE);
	}

	@Test
	public void testSillyRefractionParameters() {
		ZonedDateTime time = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));
		SolarPosition result = SPA.calculateSolarPosition(time, 39.742476, -105.1786, 1830.14, 67, -2, 1000);
		assertEquals(194.34024, result.getAzimuth(), TOLERANCE);
		assertEquals(50.1279, result.getZenithAngle(), TOLERANCE);

		SolarPosition result2 = SPA.calculateSolarPosition(time, 39.742476, -105.1786, 1830.14, 67);
		assertEquals(result, result2);
	}

	@Test
	public void testSillyLatLon() {
		ZonedDateTime time = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));
		try {
			SPA.calculateSolarPosition(time, 139.742476, -105.1786, 1830.14, 67, 820, 11);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}

		try {
			SPA.calculateSolarPosition(time, 39.742476, -205.1786, 1830.14, 67, 820, 11);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testBulkSpaReferenceValues() {
		List<String> lines = SpaTestData.readDataLines("azimuth_zenith/spa_reference_testdata.csv");
		for (String line : lines) {
			String[] p = line.split(",");
			ZonedDateTime dateTime = ZonedDateTime.parse(p[0]);
			double lat = Double.parseDouble(p[1]);
			double lon = Double.parseDouble(p[2]);
			double refAzimuth = Double.parseDouble(p[3]);
			double refZenith = Double.parseDouble(p[4]);
			SolarPosition res = SPA.calculateSolarPosition(dateTime, lat, lon, 0, 0, 1000, 10);
			assertEquals(refAzimuth, res.getAzimuth(), TOLERANCE / 100);
			assertEquals(refZenith, res.getZenithAngle(), TOLERANCE / 100);
		}
	}
}
