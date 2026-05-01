package com.kosherjava.zmanim.util.spa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class SPABulkProcessingTest {

	private static final double TOLERANCE = 1e-6;

	@Test
	public void timeDependentPartsProduceIdenticalResults() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 6, 21, 12, 0), ZoneId.of("America/Los_Angeles"));
		double latitude = 37.7749;
		double longitude = -122.4194;
		double elevation = 10;
		double deltaT = 69.0;
		double pressure = 1013.25;
		double temperature = 20;

		SolarPosition traditional = SPA.calculateSolarPosition(dateTime, latitude, longitude, elevation, deltaT, pressure, temperature);
		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);
		SolarPosition optimized = SPA.calculateSolarPositionWithTimeDependentParts(latitude, longitude, elevation, pressure, temperature, timeDependent);

		assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
		assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);
	}

	@Test
	public void timeDependentPartsProduceIdenticalResultsWithoutRefraction() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 1, 15, 8, 30), ZoneId.of("Europe/Berlin"));
		double latitude = 52.5200;
		double longitude = 13.4050;
		double elevation = 34;
		double deltaT = 69.2;

		SolarPosition traditional = SPA.calculateSolarPosition(dateTime, latitude, longitude, elevation, deltaT);
		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);
		SolarPosition optimized = SPA.calculateSolarPositionWithTimeDependentParts(latitude, longitude, elevation, timeDependent);

		assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
		assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);
	}

	@Test
	public void bulkProcessingWithRandomCoordinates() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 7, 1, 6, 0), ZoneId.of("Asia/Shanghai"));
		double deltaT = 69.1;
		double pressure = 1000;
		double temperature = 15;

		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);
		Random random = new Random(42);
		for (int i = 0; i < 100; i++) {
			double latitude = random.nextDouble() * 180 - 90;
			double longitude = random.nextDouble() * 360 - 180;
			double elevation = random.nextDouble() * 1000;

			SolarPosition traditional = SPA.calculateSolarPosition(dateTime, latitude, longitude, elevation, deltaT, pressure, temperature);
			SolarPosition optimized = SPA.calculateSolarPositionWithTimeDependentParts(latitude, longitude, elevation, pressure, temperature, timeDependent);

			assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
			assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);
		}
	}

	@Test
	public void bulkProcessingWithFixedTime() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 3, 20, 14, 0), ZoneId.of("UTC"));
		double deltaT = 69.0;
		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);

		double[][] coordinates = {
				{0, 0, 0},
				{51.5074, -0.1278, 10},
				{-33.8688, 151.2093, 5},
				{35.6762, 139.6503, 40},
				{40.7128, -74.0060, 10},
				{-54.8019, -68.3030, 0}
		};

		for (double[] coord : coordinates) {
			SolarPosition traditional = SPA.calculateSolarPosition(dateTime, coord[0], coord[1], coord[2], deltaT);
			SolarPosition optimized = SPA.calculateSolarPositionWithTimeDependentParts(coord[0], coord[1], coord[2], timeDependent);
			assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
			assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);
		}
	}

	@Test
	public void recycleTimeDependentPartsForMultipleCoordinates() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 9, 22, 15, 30), ZoneId.of("Europe/Berlin"));
		double deltaT = 69.0;
		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);

		double[][] coordinates = {
				{52.5200, 13.4050, 34},
				{48.8566, 2.3522, 35},
				{41.9028, 12.4964, 21}
		};
		for (double[] coord : coordinates) {
			SolarPosition traditional = SPA.calculateSolarPosition(dateTime, coord[0], coord[1], coord[2], deltaT);
			SolarPosition optimized = SPA.calculateSolarPositionWithTimeDependentParts(coord[0], coord[1], coord[2], timeDependent);
			assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
			assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);
		}
	}

	@Test
	public void performanceBenefitOfBulkProcessing() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 12, 21, 12, 0), ZoneId.of("UTC"));
		double deltaT = 69.0;

		List<double[]> coordinates = new ArrayList<double[]>();
		for (double lat = -60; lat <= 60; lat += 10) {
			for (double lon = -180; lon <= 170; lon += 10) {
				coordinates.add(new double[] {lat, lon, 0});
			}
		}

		for (int warmup = 0; warmup < 10; warmup++) {
			for (double[] coord : coordinates) {
				SPA.calculateSolarPosition(dateTime, coord[0], coord[1], coord[2], deltaT);
			}
		}

		long startTraditional = System.nanoTime();
		for (double[] coord : coordinates) {
			SPA.calculateSolarPosition(dateTime, coord[0], coord[1], coord[2], deltaT);
		}
		long traditionalTime = System.nanoTime() - startTraditional;

		long startOptimized = System.nanoTime();
		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);
		for (double[] coord : coordinates) {
			SPA.calculateSolarPositionWithTimeDependentParts(coord[0], coord[1], coord[2], timeDependent);
		}
		long optimizedTime = System.nanoTime() - startOptimized;

		assertTrue(optimizedTime < traditionalTime);
	}

	@Test
	public void timeDependentPartsWithExtremeValues() {
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2024, 6, 21, 0, 0), ZoneId.of("UTC"));
		double deltaT = 69.0;
		SPA.SpaTimeDependent timeDependent = SPA.calculateSpaTimeDependentParts(dateTime, deltaT);

		double latitude = 89.9;
		double longitude = 0;
		double elevation = 0;
		SolarPosition traditional = SPA.calculateSolarPosition(dateTime, latitude, longitude, elevation, deltaT);
		SolarPosition optimized = SPA.calculateSolarPositionWithTimeDependentParts(latitude, longitude, elevation, timeDependent);
		assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
		assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);

		latitude = -89.9;
		traditional = SPA.calculateSolarPosition(dateTime, latitude, longitude, elevation, deltaT);
		optimized = SPA.calculateSolarPositionWithTimeDependentParts(latitude, longitude, elevation, timeDependent);
		assertEquals(traditional.getAzimuth(), optimized.getAzimuth(), TOLERANCE);
		assertEquals(traditional.getZenithAngle(), optimized.getZenithAngle(), TOLERANCE);
	}
}
