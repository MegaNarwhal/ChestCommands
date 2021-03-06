/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.legacy;

import com.google.common.collect.ImmutableList;
import me.filoghost.chestcommands.config.ConfigManager;
import me.filoghost.chestcommands.legacy.upgrade.Upgrade;
import me.filoghost.chestcommands.legacy.upgrade.UpgradeTask;
import me.filoghost.chestcommands.legacy.upgrade.UpgradeTaskException;
import me.filoghost.chestcommands.legacy.v4_0.v4_0_LangUpgradeTask;
import me.filoghost.chestcommands.legacy.v4_0.v4_0_MenuNodeRenameUpgradeTask;
import me.filoghost.chestcommands.legacy.v4_0.v4_0_MenuReformatUpgradeTask;
import me.filoghost.chestcommands.legacy.v4_0.v4_0_PlaceholdersUpgradeTask;
import me.filoghost.chestcommands.legacy.v4_0.v4_0_SettingsUpgradeTask;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.commons.collection.CollectionUtils;
import me.filoghost.commons.config.ConfigLoader;
import me.filoghost.commons.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class UpgradeList {

	/*
	 * Note: order of declaration determines order of execution
	 */
	private static final ImmutableList<Upgrade> orderedUpgrades = ImmutableList.of(
			multiTaskUpgrade("v4.0-menus-rename", (configManager) -> {
				return createMenuTasks(configManager, v4_0_MenuNodeRenameUpgradeTask::new);
			}),

			// Reformat after nodes have already been renamed
			multiTaskUpgrade("v4.0-menus-reformat", (configManager) -> {
				String legacyCommandSeparator = readLegacyCommandSeparator(configManager);
				return createMenuTasks(configManager,
						file -> new v4_0_MenuReformatUpgradeTask(configManager, file, legacyCommandSeparator));
			}),

			// Upgrade config after reading the command separator for menus
			singleTaskUpgrade("v4.0-config", v4_0_SettingsUpgradeTask::new),
			singleTaskUpgrade("v4.0-placeholders", v4_0_PlaceholdersUpgradeTask::new),
			singleTaskUpgrade("v4.0-lang", v4_0_LangUpgradeTask::new)
	);

	private static Upgrade singleTaskUpgrade(String id, Upgrade.SingleTaskSupplier upgradeTaskSupplier) {
		return new Upgrade(id, configManager -> {
			return Collections.singletonList(upgradeTaskSupplier.getTask(configManager));
		});
	}

	private static Upgrade multiTaskUpgrade(String id, Upgrade.MultiTaskSupplier upgradeTasksSupplier) {
		return new Upgrade(id, upgradeTasksSupplier);
	}

	private static List<UpgradeTask> createMenuTasks(ConfigManager configManager, Function<Path, UpgradeTask> menuTaskSupplier) throws UpgradeTaskException {
		List<Path> menuFiles = getMenuFiles(configManager);
		return CollectionUtils.transform(menuFiles, menuTaskSupplier);
	}

	private static List<Path> getMenuFiles(ConfigManager configManager) throws UpgradeTaskException {
		try {
			return configManager.getMenuFiles();
		} catch (IOException e) {
			throw new UpgradeTaskException(Errors.Upgrade.menuListIOException, e);
		}
	}

	private static String readLegacyCommandSeparator(ConfigManager configManager) {
		ConfigLoader settingsConfigLoader = configManager.getConfigLoader("config.yml");

		if (!settingsConfigLoader.fileExists()) {
			return null;
		}

		try {
			return settingsConfigLoader.load().getString("multiple-commands-separator");
		} catch (Throwable t) {
			Log.warning("Failed to load \"" + settingsConfigLoader.getFile() + "\", assuming default command separator \";\".");
			return null;
		}
	}

	public static ImmutableList<Upgrade> getOrderedUpgrades() {
		return orderedUpgrades;
	}
}
