/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.commons.config.mapped.converter;

import me.filoghost.commons.config.ConfigValueType;

import java.lang.reflect.Type;

public class StringConverter implements Converter {

	@Override
	public ConfigValueType<?> getConfigValueType(Type[] fieldGenericTypes) {
		return ConfigValueType.STRING;
	}

	@Override
	public boolean matches(Class<?> type) {
		return type == String.class;
	}

}
