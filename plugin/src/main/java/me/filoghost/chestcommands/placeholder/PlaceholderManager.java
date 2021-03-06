/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.placeholder;

import me.filoghost.chestcommands.api.PlaceholderReplacer;
import me.filoghost.chestcommands.hook.PlaceholderAPIHook;
import me.filoghost.chestcommands.placeholder.scanner.PlaceholderMatch;
import me.filoghost.chestcommands.placeholder.scanner.PlaceholderScanner;
import me.filoghost.commons.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderManager {

	private static final List<StaticPlaceholder> staticPlaceholders = new ArrayList<>();
	private static final PlaceholderRegistry relativePlaceholderRegistry = new PlaceholderRegistry();
	static {
		for (DefaultPlaceholder placeholder : DefaultPlaceholder.values()) {
			relativePlaceholderRegistry.registerInternalPlaceholder(placeholder.getIdentifier(), placeholder.getReplacer());
		}
	}

	private static final PlaceholderCache placeholderCache = new PlaceholderCache();

	public static boolean hasRelativePlaceholders(List<String> list) {
		for (String element : list) {
			if (hasRelativePlaceholders(element)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasRelativePlaceholders(String text) {
		if (new PlaceholderScanner(text).anyMatch(PlaceholderManager::isValidPlaceholder)) {
			return true;
		}

		if (PlaceholderAPIHook.INSTANCE.isEnabled() && PlaceholderAPIHook.hasPlaceholders(text)) {
			return true;
		}

		return false;
	}

	public static String replaceRelativePlaceholders(String text, Player player) {
		text = new PlaceholderScanner(text).replace(match -> getReplacement(match, player));

		if (PlaceholderAPIHook.INSTANCE.isEnabled()) {
			text = PlaceholderAPIHook.setPlaceholders(text, player);
		}

		return text;
	}

	private static boolean isValidPlaceholder(PlaceholderMatch placeholderMatch) {
		return relativePlaceholderRegistry.getPlaceholderReplacer(placeholderMatch) != null;
	}

	private static String getReplacement(PlaceholderMatch placeholderMatch, Player player) {
		PlaceholderReplacer placeholderReplacer = relativePlaceholderRegistry.getPlaceholderReplacer(placeholderMatch);

		if (placeholderReplacer == null) {
			return null; // Placeholder not found
		}

		return placeholderCache.computeIfAbsent(placeholderMatch, player, () -> {
			return placeholderReplacer.getReplacement(player, placeholderMatch.getArgument());
		});
	}

	public static void setStaticPlaceholders(List<StaticPlaceholder> staticPlaceholders) {
		PlaceholderManager.staticPlaceholders.clear();
		PlaceholderManager.staticPlaceholders.addAll(staticPlaceholders);
	}

	public static boolean hasStaticPlaceholders(List<String> list) {
		for (String element : list) {
			if (hasStaticPlaceholders(element)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasStaticPlaceholders(String text) {
		for (StaticPlaceholder staticPlaceholder : staticPlaceholders) {
			if (text.contains(staticPlaceholder.getIdentifier())) {
				return true;
			}
		}
		return false;
	}

	public static String replaceStaticPlaceholders(String text) {
		for (StaticPlaceholder staticPlaceholder : staticPlaceholders) {
			text = text.replace(staticPlaceholder.getIdentifier(), staticPlaceholder.getReplacement());
		}
		return text;
	}

	public static void registerPluginPlaceholder(Plugin plugin, String identifier, PlaceholderReplacer placeholderReplacer) {
		Preconditions.notNull(plugin, "plugin");
		Preconditions.notNull(identifier, "identifier");
		Preconditions.checkArgument(1 <= identifier.length() && identifier.length() <= 30, "identifier length must be between 1 and 30");
		Preconditions.checkArgument(identifier.matches("[a-zA-Z0-9_]+"), "identifier must contain only letters, numbers and underscores");
		Preconditions.notNull(placeholderReplacer, "placeholderReplacer");

		relativePlaceholderRegistry.registerExternalPlaceholder(plugin, identifier, placeholderReplacer);
	}

	public static void onTick() {
		placeholderCache.onTick();
	}

}
