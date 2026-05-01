/*
 * Zmanim Java API
 * Copyright (C) 2004-2026 Eliyahu Hershfeld
 *
 * This file includes adapted code from the SPA implementation by Klaus A. Brunner
 * (MIT License). See the root-level SPA source attribution for details.
 */
package com.kosherjava.zmanim.util.spa;

import java.util.Objects;

/**
 * Result type for an azimuth/zenith angle pair of values.
 */
public final class SolarPosition {
	private final double azimuth;
	private final double zenithAngle;

	public SolarPosition(double azimuth, double zenithAngle) {
		if (azimuth < 0 || azimuth > 360) {
			throw new IllegalArgumentException(String.format("illegal value %.3f for azimuth", azimuth));
		}
		if (zenithAngle < 0 || zenithAngle > 180) {
			throw new IllegalArgumentException(String.format("illegal value %.3f for zenithAngle", zenithAngle));
		}
		this.azimuth = azimuth;
		this.zenithAngle = zenithAngle;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public double getZenithAngle() {
		return zenithAngle;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SolarPosition)) {
			return false;
		}
		SolarPosition that = (SolarPosition) o;
		return Double.doubleToLongBits(azimuth) == Double.doubleToLongBits(that.azimuth)
				&& Double.doubleToLongBits(zenithAngle) == Double.doubleToLongBits(that.zenithAngle);
	}

	@Override
	public int hashCode() {
		return Objects.hash(azimuth, zenithAngle);
	}

	@Override
	public String toString() {
		return "SolarPosition{" + "azimuth=" + azimuth + ", zenithAngle=" + zenithAngle + '}';
	}
}
