/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.placeholder;

import org.bukkit.entity.Player;

public class PlaceholderString {

	private final String originalString;
	private final String stringWithStaticPlaceholders;
	private final boolean hasDynamicPlaceholders;

	public static PlaceholderString of(String string) {
		if (string != null) {
			return new PlaceholderString(string);
		} else {
			return null;
		}
	}
	
	private PlaceholderString(String originalString) {
		this.originalString = originalString;
		this.stringWithStaticPlaceholders = PlaceholderManager.replaceStaticPlaceholders(originalString);
		this.hasDynamicPlaceholders = PlaceholderManager.hasRelativePlaceholders(stringWithStaticPlaceholders);
	}
	
	public String getValue(Player player) {
		if (hasDynamicPlaceholders) {
			return PlaceholderManager.replaceRelativePlaceholders(stringWithStaticPlaceholders, player);
		} else {
			return stringWithStaticPlaceholders;
		}
	}

	public String getOriginalValue() {
		return originalString;
	}

	public boolean hasDynamicPlaceholders() {
		return hasDynamicPlaceholders;
	}

}
