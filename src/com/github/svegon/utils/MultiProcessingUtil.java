/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public final class MultiProcessingUtil {
	private MultiProcessingUtil() {
		throw new AssertionError();
	}
	
	public static ProcessBuilder makeProcess(Class<?> mainClass, String... args) {
		ArrayList<String> cmd = new ArrayList<>();
		cmd.add(getJavaDir().toString());
		cmd.add("-cp");
		cmd.add(getClasspath().toString());
		cmd.add(mainClass.getName());
		cmd.addAll(Arrays.asList(args));
		
		return new ProcessBuilder(cmd);
	}
	
	public static Process startProcess(Class<?> mainClass, String... args) throws IOException {
		return makeProcess(mainClass, args).inheritIO().start();
	}
	
	public static Process startProcessWithIO(Class<?> mainClass, String... args) throws IOException {
		return makeProcess(mainClass, args).start();
	}
	
	private static Path getJavaDir() {
		return Paths.get(System.getProperty("java.home"), "bin", "java");
	}
	
	private static Path getClasspath() {
		try {
			return Paths.get(MultiProcessingUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
