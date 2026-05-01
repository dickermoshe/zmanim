package com.kosherjava.zmanim.util.spa;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

public class DeltaTTest {

	private static LocalDate yearCal(int year) {
		return LocalDate.of(year, 1, 1);
	}

	@Test
	public void testHistoricalValues() {
		assertEquals(27364, DeltaT.estimate(yearCal(-1000)), 2000);
		assertEquals(17190, DeltaT.estimate(yearCal(-400)), 2000);
		assertEquals(14080, DeltaT.estimate(yearCal(-300)), 3);
		assertEquals(12790, DeltaT.estimate(yearCal(-200)), 2);
		assertEquals(7680, DeltaT.estimate(yearCal(300)), 1);
		assertEquals(3810, DeltaT.estimate(yearCal(700)), 3);
		assertEquals(200, DeltaT.estimate(yearCal(1500)), 2);
		assertEquals(44, DeltaT.estimate(yearCal(1657)), 4);
		assertEquals(13.7, DeltaT.estimate(yearCal(1750)), 2);
		assertEquals(7, DeltaT.estimate(yearCal(1850)), 1);
		assertEquals(1.04, DeltaT.estimate(yearCal(1870)), 1);
		assertEquals(-3, DeltaT.estimate(yearCal(1900)), 1);
		assertEquals(10.38, DeltaT.estimate(yearCal(1910)), 1);
		assertEquals(24.02, DeltaT.estimate(yearCal(1930)), 1);
		assertEquals(29, DeltaT.estimate(yearCal(1950)), 1);
	}

	@Test
	public void testUSNODataRecent() {
		List<String> lines = SpaTestData.readDataLines("deltat/deltat.data.txt");
		for (String line : lines) {
			String[] parts = line.split("\\s+");
			LocalDate date = LocalDate.of(
					Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2]));
			double deltaT = Double.parseDouble(parts[3]);
			assertEquals(deltaT, DeltaT.estimate(date), deltaT * 0.05);
		}
	}
}
