/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.placeholder;

import com.google.common.collect.ImmutableList;
import me.filoghost.commons.Preconditions;
import me.filoghost.commons.collection.CollectionUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderStringList {

	private final ImmutableList<String> originalList;
	private final ImmutableList<String> listWithStaticPlaceholders;
	private final ImmutableList<PlaceholderString> placeholderStringList;
	private final boolean hasDynamicPlaceholders;
	
	public PlaceholderStringList(List<String> list) {
		Preconditions.notNull(list, "list");
		this.originalList = ImmutableList.copyOf(list);

		// Replace static placeholders only once, if present
		if (PlaceholderManager.hasStaticPlaceholders(originalList)) {
			this.listWithStaticPlaceholders = CollectionUtils.transformImmutable(originalList, PlaceholderManager::replaceStaticPlaceholders);
		} else {
			this.listWithStaticPlaceholders = originalList;
		}

		this.hasDynamicPlaceholders = PlaceholderManager.hasRelativePlaceholders(listWithStaticPlaceholders);
		if (hasDynamicPlaceholders) {
			this.placeholderStringList = CollectionUtils.transformImmutable(listWithStaticPlaceholders, PlaceholderString::of);
		} else {
			this.placeholderStringList = null;
		}
	}
	
	public ImmutableList<String> getOriginalValue() {
		return originalList;
	}
	
	public ImmutableList<String> getValue(Player player) {
		if (hasDynamicPlaceholders) {
			return CollectionUtils.transformImmutable(placeholderStringList, element -> element.getValue(player));
		} else {
			return listWithStaticPlaceholders;
		}
	}
	
	public boolean hasDynamicPlaceholders() {
		return hasDynamicPlaceholders;
	}

}
