package com.kosherjava.zmanim.util.spa;

import static org.junit.Assert.fail;

import org.junit.Test;

public class SolarPositionTest {

	@Test
	public void rejectsSillyAzimuth() {
		try {
			new SolarPosition(-0.1, 90);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
		try {
			new SolarPosition(360.1, 90);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void rejectsSillyZenithAngle() {
		try {
			new SolarPosition(90, -0.1);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
		try {
			new SolarPosition(90, 180.1);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}
}
