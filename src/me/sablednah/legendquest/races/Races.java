package me.sablednah.legendquest.races;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import me.sablednah.legendquest.Main;
import me.sablednah.legendquest.skills.SkillDataStore;
import me.sablednah.legendquest.skills.SkillInfo;
import me.sablednah.legendquest.utils.Pair;
import me.sablednah.legendquest.utils.Utils;
import me.sablednah.legendquest.utils.WeightedProbMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Races {

	public Main										lq;
	private final Map<String, Race>					races			= new HashMap<String, Race>();
	private final ArrayList<Pair<Integer, String>>	raceprobability	= new ArrayList<Pair<Integer, String>>();
	public WeightedProbMap<String>					wpmRaces;

	public Race										defaultRace;

	@SuppressWarnings({ "unchecked", "deprecation" })
	public Races(final Main p) {
		this.lq = p;

		// Find the Races folder
		final File raceDir = new File(lq.getDataFolder() + File.separator + "races");
		// notify scanning begun
		lq.log(lq.configLang.raceScan + ": " + raceDir);

		// make it if not found
		if (!raceDir.exists()) {
			lq.debug.info(raceDir + " not found, installing defaults.");
			raceDir.mkdir();
			try {
				if (lq.configMain.debugMode) {
					lq.debug.info("looking for races zip");
				}

				lq.saveResource("races.zip", true);
				File zf = new File(lq.getDataFolder() + File.separator + "races.zip");
				ZipFile zip = new ZipFile(zf);

				Enumeration<? extends ZipEntry> entries = zip.entries();

				ZipEntry entry;
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					if (lq.configMain.debugMode) {
						lq.debug.info("Extracting " + entry.getName());
					}
					Utils.extractFile(zip.getInputStream(entry), new FileOutputStream(raceDir.getPath() + File.separator + entry.getName()));
				}
				zip.close();
				File nf = new File(lq.getDataFolder() + File.separator + "races.zip");
				nf.delete();

			} catch (IOException e) {
				lq.debug.info("Could not extract defaults from races.zip");
				e.printStackTrace();
			}
		}

		final File[] racefiles = raceDir.listFiles();

		for (final File race : racefiles) {
			// only want .yml configs here baby
			if (race.isFile() && race.getName().toLowerCase().endsWith(".yml")) {
				// found a config file - load it in time

				lq.log(lq.configLang.raceScanFound + race.getName());
				final Race r = new Race();
				Boolean validConfig = true;
				YamlConfiguration thisConfig = null;

				// begin parsing config file
				try {
					thisConfig = YamlConfiguration.loadConfiguration(race);

					r.filename = race.getName();
					r.name = thisConfig.getString("name");
					r.frequency = thisConfig.getInt("frequency");

					r.plural = thisConfig.getString("plural");
					r.size = thisConfig.getDouble("size");
					r.defaultRace = thisConfig.getBoolean("default");
					r.statStr = thisConfig.getInt("statmods.str");
					r.statDex = thisConfig.getInt("statmods.dex");
					r.statInt = thisConfig.getInt("statmods.int");
					r.statWis = thisConfig.getInt("statmods.wis");
					r.statCon = thisConfig.getInt("statmods.con");
					r.statChr = thisConfig.getInt("statmods.chr");
					r.baseHealth = thisConfig.getInt("basehealth");
					r.baseSpeed = (float) thisConfig.getDouble("basespeed",0.2F);

					r.allowCrafting = thisConfig.getBoolean("allowCrafting");
					r.allowSmelting = thisConfig.getBoolean("allowSmelting");
					r.allowBrewing = thisConfig.getBoolean("allowBrewing");
					r.allowEnchating = thisConfig.getBoolean("allowEnchating");
					r.allowRepairing = thisConfig.getBoolean("allowRepairing");

					r.baseMana = thisConfig.getInt("baseMana");
					r.manaPerSecond = thisConfig.getDouble("manaPerSecond");

					r.xpAdjustKill = thisConfig.getDouble("xpAdjustKill");
					r.xpAdjustSmelt = thisConfig.getDouble("xpAdjustSmelt");
					r.xpAdjustMine = thisConfig.getDouble("xpAdjustMine");

					r.skillPointsPerLevel = thisConfig.getDouble("skillPointsPerLevel");
					r.skillPoints = thisConfig.getInt("skillPoints");

					r.perm = thisConfig.getString("perm");

					final List<String> groups = (List<String>) thisConfig.getList("groups");
					for (int i = 0; i < groups.size(); i++) {
						groups.set(i, groups.get(i).toLowerCase());
					}
					r.groups = groups;

					// allowed lists
					List<String> stringList;
					List<Material> materialList;
					String keyName;
					stringList = (List<String>) thisConfig.getList("allowedTools");
					materialList = new ArrayList<Material>();
					if (stringList != null) {
						for (int i = 0; i < stringList.size(); i++) {
							keyName = stringList.get(i).toLowerCase();
							if (keyName.equalsIgnoreCase("all") || keyName.equalsIgnoreCase("any")) {
								keyName = "tools";
								//also add in utility list
								materialList.addAll(lq.configData.dataSets.get("utility"));
							}
							if (!keyName.equalsIgnoreCase("none")) {
								if (lq.configData.dataSets.containsKey(keyName)) {
									materialList.addAll(lq.configData.dataSets.get(keyName));
								} else if (Material.matchMaterial(keyName) != null) {
									materialList.add(Material.matchMaterial(keyName));
								} else if (Utils.isParsableToInt(keyName)) {
									materialList.add(Material.getMaterial(Integer.parseInt(keyName)));
								} else {
									lq.debug.error("Allowed tool '" + keyName + "' in " + r.filename + " not understood");
								}
							}
						}
					}
					r.allowedTools = materialList;

					stringList = (List<String>) thisConfig.getList("allowedArmour");
					materialList = new ArrayList<Material>();
					if (stringList != null) {
						for (int i = 0; i < stringList.size(); i++) {
							keyName = stringList.get(i).toLowerCase();
							if (keyName.equalsIgnoreCase("all") || keyName.equalsIgnoreCase("any")) {
								keyName = "armour";
							}
							if (!keyName.equalsIgnoreCase("none")) {
								if (lq.configData.dataSets.containsKey(keyName)) {
									materialList.addAll(lq.configData.dataSets.get(keyName));
								} else if (Material.matchMaterial(keyName) != null) {
									materialList.add(Material.matchMaterial(keyName));
								} else if (Utils.isParsableToInt(keyName)) {
									materialList.add(Material.getMaterial(Integer.parseInt(keyName)));
								} else {
									lq.debug.error("Allowed Armour '" + keyName + "' in " + r.filename + " not understood");
								}
							}
						}
					}
					r.allowedArmour = materialList;

					stringList = (List<String>) thisConfig.getList("allowedWeapons");
					materialList = new ArrayList<Material>();
					if (stringList != null) {
						for (int i = 0; i < stringList.size(); i++) {
							keyName = stringList.get(i).toLowerCase();
							if (keyName.equalsIgnoreCase("all") || keyName.equalsIgnoreCase("any")) {
								keyName = "weapons";
							}
							if (!keyName.equalsIgnoreCase("none")) {
								if (lq.configData.dataSets.containsKey(keyName)) {
									materialList.addAll(lq.configData.dataSets.get(keyName));
								} else if (Material.matchMaterial(keyName) != null) {
									materialList.add(Material.matchMaterial(keyName));
								} else if (Utils.isParsableToInt(keyName)) {
									materialList.add(Material.getMaterial(Integer.parseInt(keyName)));
								} else {
									lq.debug.error("Allowed Weapons '" + keyName + "' in " + r.filename + " not understood");
								}
							}
						}
					}
					r.allowedWeapons = materialList;

					// build disallowed lists

					stringList = (List<String>) thisConfig.getList("dissallowedTools");
					materialList = new ArrayList<Material>();
					if (stringList != null) {
						for (int i = 0; i < stringList.size(); i++) {
							keyName = stringList.get(i).toLowerCase();
							if (keyName.equalsIgnoreCase("all") || keyName.equalsIgnoreCase("any")) {
								keyName = "tools";
								//also add in utility list
								materialList.addAll(lq.configData.dataSets.get("utility"));
							}
							if (!keyName.equalsIgnoreCase("none")) {
								if (lq.configData.dataSets.containsKey(keyName)) {
									materialList.addAll(lq.configData.dataSets.get(keyName));
								} else if (Material.matchMaterial(keyName) != null) {
									materialList.add(Material.matchMaterial(keyName));
								} else if (Utils.isParsableToInt(keyName)) {
									materialList.add(Material.getMaterial(Integer.parseInt(keyName)));
								} else {
									lq.debug.error("Disallowed tool '" + keyName + "' in " + r.filename + " not understood");
								}
							}
						}
					}
					r.dissallowedTools = materialList;

					stringList = (List<String>) thisConfig.getList("dissallowedArmour");
					materialList = new ArrayList<Material>();
					if (stringList != null) {
						for (int i = 0; i < stringList.size(); i++) {
							keyName = stringList.get(i).toLowerCase();
							if (keyName.equalsIgnoreCase("all") || keyName.equalsIgnoreCase("any")) {
								keyName = "armour";
							}
							if (!keyName.equalsIgnoreCase("none")) {
								if (lq.configData.dataSets.containsKey(keyName)) {
									materialList.addAll(lq.configData.dataSets.get(keyName));
								} else if (Material.matchMaterial(keyName) != null) {
									materialList.add(Material.matchMaterial(keyName));
								} else if (Utils.isParsableToInt(keyName)) {
									materialList.add(Material.getMaterial(Integer.parseInt(keyName)));
								} else {
									lq.debug.error("Disallowed Armour '" + keyName + "' in " + r.filename + " not understood");
								}
							}
						}
					}
					r.dissallowedArmour = materialList;

					stringList = (List<String>) thisConfig.getList("dissallowedWeapons");
					materialList = new ArrayList<Material>();
					if (stringList != null) {
						for (int i = 0; i < stringList.size(); i++) {
							keyName = stringList.get(i).toLowerCase();
							if (keyName.equalsIgnoreCase("all") || keyName.equalsIgnoreCase("any")) {
								keyName = "weapons";
							}
							if (!keyName.equalsIgnoreCase("none")) {
								if (lq.configData.dataSets.containsKey(keyName)) {
									materialList.addAll(lq.configData.dataSets.get(keyName));
								} else if (Material.matchMaterial(keyName) != null) {
									materialList.add(Material.matchMaterial(keyName));
								} else if (Utils.isParsableToInt(keyName)) {
									materialList.add(Material.getMaterial(Integer.parseInt(keyName)));
								} else {
									lq.debug.error("Disallowed Weapons '" + keyName + "' in " + r.filename + " not understood");
								}
							}
						}
					}
					r.dissallowedWeapons = materialList;

					// skills
					r.availableSkills = new ArrayList<SkillDataStore>();
					final ConfigurationSection inateSkills = thisConfig.getConfigurationSection("skills");
					if (inateSkills != null) {
						for (final String key : inateSkills.getKeys(false)) {
							String skillName = key.toLowerCase();
							String realSkill = key.toLowerCase();
							ConfigurationSection skillInfo = inateSkills.getConfigurationSection(key);
							if (skillInfo.contains("skillname")) {
								realSkill = skillInfo.getString("skillname").toLowerCase();
								//create a "copy" of the skill under new name
								// this registers duplicate events with different this.name for fetching correct skill settings.
								lq.skills.initSkill(realSkill,skillName);
							}
							if (lq.skills.skillList.containsKey(skillName)) {
								lq.debug.info("Loading skillName: " + skillName+ " as skill " + realSkill);
								SkillInfo si = 	lq.skills.skillDefs.get(realSkill).getSkillInfoClone();
								SkillDataStore skilldata = new SkillDataStore(si);
								skilldata.name=skillName;
								skilldata.readConfigInfo(skillInfo);
								skilldata.name=skillName;
								r.availableSkills.add(skilldata);
							}
						}
					}

					for (SkillDataStore s :r.availableSkills) {
						lq.debug.info("Vars ["+s .name+"] : "+s.vars.toString());
						
					}
					
					// outsourced skills - skills without skill class - using command/on/off and perm nodes to achieve
					// effect.
					r.outsourcedSkills = new ArrayList<SkillDataStore>();
					final ConfigurationSection permSkills = thisConfig.getConfigurationSection("permskills");
					if (permSkills != null) {
						for (String key : permSkills.getKeys(false)) {
							ConfigurationSection skillInfo = permSkills.getConfigurationSection(key);
							lq.debug.info("Loading permskill: " + key);
							SkillInfo si = new SkillInfo("BukkitPlugin", "sablednah", "Bukkit Skill", null, 1, 0, 0, 0, 0, 0, "", 0, 0, null, null, null, null, null, null);
							si.setName(key);
							si.readConfigBasicInfo(skillInfo);
							SkillDataStore skilldata = new SkillDataStore(si);
							skilldata.readConfigInfo(skillInfo);
							r.outsourcedSkills.add(skilldata);
						}
					}

				} catch (final Exception e) {
					validConfig = false;
					lq.log(lq.configLang.raceScanInvalid + race.getName());
					e.printStackTrace();
				}

				if (validConfig) {
					raceprobability.add(new Pair<Integer, String>(r.frequency, r.name));
					races.put(r.name.toLowerCase(), r);
					if (r.defaultRace) {
						defaultRace = r;
					}
				}
			}
		}

		wpmRaces = new WeightedProbMap<String>(raceprobability);

		// notify scanning ended
		lq.log(lq.configLang.raceScanEnd);

		if (defaultRace == null) {
			lq.log(lq.configLang.raceNoDefault);
			// set "default" to first found to stop breaking...
			defaultRace = races.entrySet().iterator().next().getValue();
		}
	}

	public Race getRace(final String raceName) {
		return races.get(raceName.toLowerCase());
	}

	public Map<String, Race> getRaces() {
		return races;
	}

	public boolean groupExists(final String groupname) {
		final Collection<Race> racelist = races.values();
		for (final Race race : racelist) {
			if (race.groups.contains(groupname.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public boolean raceExists(final String racename) {
		return races.containsKey(racename.toLowerCase());
	}

	public String getRandomRace() {
		return wpmRaces.nextElt();
	}
}
