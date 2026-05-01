package com.kosherjava.zmanim.util.spa;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

final class SpaTestData {
	private SpaTestData() {
	}

	static List<String> readDataLines(String relativePath) {
		String resourcePath = "spa-testdata/" + relativePath;
		try (InputStream stream = SpaTestData.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (stream == null) {
				throw new RuntimeException("Missing test data resource: " + resourcePath);
			}
			List<String> raw = new ArrayList<String>();
			try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
				while (scanner.hasNextLine()) {
					raw.add(scanner.nextLine());
				}
			}
			List<String> out = new ArrayList<String>();
			for (String line : raw) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				out.add(trimmed);
			}
			return out;
		} catch (IOException e) {
			throw new RuntimeException("Failed reading test data resource: " + resourcePath, e);
		}
	}
}
