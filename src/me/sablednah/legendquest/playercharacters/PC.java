package me.sablednah.legendquest.playercharacters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import me.sablednah.legendquest.Main;
import me.sablednah.legendquest.classes.ClassType;
import me.sablednah.legendquest.db.HealthStore;
import me.sablednah.legendquest.experience.ExperienceSource;
import me.sablednah.legendquest.experience.SetExp;
import me.sablednah.legendquest.races.Race;
import me.sablednah.legendquest.skills.SkillDataStore;
import me.sablednah.legendquest.skills.SkillPhase;
import me.sablednah.legendquest.mechanics.Difficulty;
import me.sablednah.legendquest.mechanics.Mechanics;
import me.sablednah.legendquest.mechanics.Attribute;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PC {

	public class DelayedCheck implements Runnable {
		public void run() {
			healthCheck();
		}
	}

	public class DelayedInvCheck implements Runnable {
		public void run() {
			checkInv();
		}
	}

	public class DelayedXPSave implements Runnable {
		public void run() {
			saveXP();
		}
	}

	public UUID								uuid;
	public Main								lq;
	public String							charname;
	public String							player;

	public Race								race;
	public boolean							raceChanged;
	public ClassType						mainClass;
	public ClassType						subClass;

	public HashMap<String, Integer>			xpEarnt			= new HashMap<String, Integer>();
	public double							maxHP;
	public int								currentXP;

	public double							health;
	public int								mana;
	public long								karma;

	public int								statStr;
	public int								statDex;
	public int								statInt;
	public int								statWis;
	public int								statCon;
	public int								statChr;

	public HashMap<String, SkillDataStore>	skillSet		= null;
	public Map<String, Boolean>				skillsSelected;
	public HashMap<String, Integer>			skillsPurchased	= new HashMap<String, Integer>();
	public HashMap<Material, String>		skillLinkings	= new HashMap<Material, String>();

	// private boolean skillsEnabled = true;

	/**
	 * Create PC by name
	 * 
	 * @deprecated use {@link PC(Main, UUID)} instead.
	 */

	@Deprecated
	public PC(Main plugin, String pName) {
		this(plugin, plugin.getServer().getPlayer(pName).getUniqueId());
	}

	/**
	 * Create PC by UUID
	 **/
	public PC(Main plugin, UUID uuid) {
		String pName;
		pName = plugin.getServer().getOfflinePlayer(uuid).getName();
		this.lq = plugin;
		this.uuid = uuid;
		this.player = pName;
		this.charname = pName;
		this.mainClass = this.lq.classes.defaultClass;
		this.race = this.lq.races.defaultRace;
		this.raceChanged = false;
		this.subClass = null;
		this.maxHP = 20;
		this.health = 20;
		this.mana = getMaxMana();
		this.currentXP = 0;
		if (!lq.configMain.randomStats) {
			statStr = statDex = statInt = statWis = statCon = statChr = 12;
		} else {
			int[] statline = { 16, 14, 13, 12, 11, 10 };
			Random r = new Random(pName.hashCode());
			for (int i = 0; i < statline.length; i++) {
				int position = i + r.nextInt(statline.length - i);
				int temp = statline[i];
				statline[i] = statline[position];
				statline[position] = temp;
			}
			statStr = statline[0];
			statDex = statline[1];
			statInt = statline[2];
			statWis = statline[3];
			statCon = statline[4];
			statChr = statline[5];
		}
		skillSet = getUniqueSkills(true);
		checkSkills();
		scheduleCheckInv();
		scheduleHealthCheck();

	}

	public boolean allowedArmour(Material id) {
		Boolean valid = false;
		if (id == null) {
			valid = true;
			lq.debug.fine("Naked is valid armour");
		}
		if (mainClass.allowedArmour.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid armour for class: " + mainClass.name);
		}
		if (race.allowedArmour.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid armour for race: " + race.name);
		}
		if (subClass != null && subClass.allowedArmour.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid armour for sub-class: " + subClass.name);
		}
		if (mainClass.dissallowedArmour.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid armour for class: " + mainClass.name);
		}
		if (race.dissallowedArmour.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid armour for race: " + race.name);
		}
		if (subClass != null && subClass.dissallowedArmour.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid armour for sub-class: " + subClass.name);
		}
		return valid;
	}

	public boolean allowedTool(Material id) {
		Boolean valid = false;

		if (id == null) {
			valid = true;
			lq.debug.fine("Air/fist is valid tool");
		}
		if (mainClass.allowedTools.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid tool for class: " + mainClass.name);
		}
		if (race.allowedTools.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid tool for race: " + race.name);
		}
		if (subClass != null && subClass.allowedTools.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid tool for sub-class: " + subClass.name);
		}
		if (mainClass.dissallowedTools.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid tool for class: " + mainClass.name);
		}
		if (race.dissallowedTools.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid tool for race: " + race.name);
		}
		if (subClass != null && subClass.dissallowedTools.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid tool for sub-class: " + subClass.name);
		}
		return valid;
	}

	public boolean allowedWeapon(Material id) {
		Boolean valid = false;

		if (id == null) {
			valid = true;
			lq.debug.fine("Air/Fist is valid weapon");
		}
		if (mainClass.allowedWeapons.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid weapon for class: " + mainClass.name);
		}
		if (race.allowedWeapons.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid weapon for race: " + race.name);
		}
		if (subClass != null && subClass.allowedWeapons.contains(id)) {
			valid = true;
			lq.debug.fine(id.toString() + " is valid weapon for sub-class: " + subClass.name);
		}
		if (mainClass.dissallowedWeapons.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid weapon for class: " + mainClass.name);
		}
		if (race.dissallowedWeapons.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid weapon for race: " + race.name);
		}
		if (subClass != null && subClass.dissallowedWeapons.contains(id)) {
			valid = false;
			lq.debug.fine(id.toString() + " is Invalid weapon for sub-class: " + subClass.name);
		}
		return valid;
	}

	@SuppressWarnings("deprecation")
	public void checkInv() {
		Player p = lq.getServer().getPlayer(uuid);
		if (p != null && p.isOnline()) {
			if (!lq.validWorld(p.getWorld().getName())) {
				return;
			}

			PlayerInventory i = p.getInventory();

			ItemStack helm = i.getHelmet();
			ItemStack chest = i.getChestplate();
			ItemStack legs = i.getLeggings();
			ItemStack boots = i.getBoots();

			if (helm != null && !(allowedArmour(helm.getType()))) {
				p.sendMessage(lq.configLang.cantEquipArmour);
				lq.debug.fine("Removed helmet " + (helm.getType().toString()) + " from " + p.getName() + ".");
				p.getWorld().dropItemNaturally(p.getLocation(), helm);
				i.setHelmet(null);
			}
			if (chest != null && !(allowedArmour(chest.getType()))) {
				p.sendMessage(lq.configLang.cantEquipArmour);
				lq.debug.fine("Removed chestplate " + (chest.getType().toString()) + " from " + p.getName() + ".");
				p.getWorld().dropItemNaturally(p.getLocation(), chest);
				i.setChestplate(null);
			}
			if (legs != null && !(allowedArmour(legs.getType()))) {
				p.sendMessage(lq.configLang.cantEquipArmour);
				lq.debug.fine("Removed leggings " + (legs.getType().toString()) + " from " + p.getName() + ".");
				p.getWorld().dropItemNaturally(p.getLocation(), legs);
				i.setLeggings(null);
			}
			if (boots != null && !(allowedArmour(boots.getType()))) {
				p.sendMessage(lq.configLang.cantEquipArmour);
				lq.debug.fine("Removed boots " + (boots.getType().toString()) + " from " + p.getName() + ".");
				p.getWorld().dropItemNaturally(p.getLocation(), boots);
				i.setBoots(null);
			}
			p.updateInventory();
		}
	}

	// Couldn't resist...
	public Double getMaxHeadroom() {
		return race.size;
	}

	public double getMaxHealth() {
		int hp, level, con;
		double result, perlevel;
		con = getAttributeModifier(Attribute.CON);
		hp = race.baseHealth + con;
		if (hp < 1) {
			hp = 1;
		}
		level = SetExp.getLevelOfXpAmount(currentXP);
		if (subClass != null) {
			perlevel = Math.max(mainClass.healthPerLevel, subClass.healthPerLevel);
		} else {
			perlevel = mainClass.healthPerLevel;
		}
		double conBonus = ((con * 10) + 100) / 100.00D; // percent per level bonus of +/-50%
		perlevel *= conBonus;
		double base = hp;
		if (lq.configMain.attributesModifyBaseStats) {
			base *= conBonus;
		}
		result = (base + (level * perlevel));

		result = (Math.round(result * 10.0) / 10.0);

		this.maxHP = result;

		return this.maxHP;
	}

	public int getMaxMana() {
		double result = 0;
		int mana, level, bonus, wis;
		double perlevel;
		wis = getAttributeModifier(Attribute.WIS);
		mana = race.baseMana;

		level = SetExp.getLevelOfXpAmount(currentXP);
		if (subClass != null) {
			perlevel = Math.max(mainClass.healthPerLevel, subClass.healthPerLevel);
			bonus = Math.max(mainClass.manaBonus, subClass.manaBonus);
		} else {
			perlevel = mainClass.healthPerLevel;
			bonus = mainClass.manaBonus;
		}
		double wisBonus = ((wis * 10) + 100) / 100.00D; // percent per level bonus of +/-50%
		perlevel *= wisBonus;
		double base = mana + bonus;
		if (lq.configMain.attributesModifyBaseStats) {
			base *= wisBonus;
		}
		result = (base + (level * perlevel));

		return (int) result;
	}

	public int getMaxSkillPointsLeft() {
		int sp, level, intel;
		double result, perlevel;
		intel = getAttributeModifier(Attribute.INT);
		sp = race.skillPoints;
		sp += mainClass.skillPoints;
		level = SetExp.getExpAtLevel(currentXP);
		if (subClass != null) {
			perlevel = Math.max(mainClass.skillPointsPerLevel, subClass.skillPointsPerLevel);
		} else {
			perlevel = mainClass.healthPerLevel;
		}
		perlevel += race.skillPointsPerLevel;
		double intBonus = ((intel * 10) + 100) / 100.00D; // percent per level bonus of +/-50%
		perlevel *= intBonus;

		double base = sp;
		if (lq.configMain.attributesModifyBaseStats) {
			base *= intBonus;
		}
		result = (base + (level * (perlevel)));

		return (int) result;
	}

	public float getSpeed() {
		float sp;
		sp = race.baseSpeed;
		sp += mainClass.speedMod;
		return sp;
	}
	
	public double getXPMod(ExperienceSource es) {
		double xp;
		switch (es) {
			case KILL:
				if (subClass != null) {
					xp = race.xpAdjustKill + mainClass.xpAdjustKill + subClass.xpAdjustKill;
				} else {
					xp = race.xpAdjustKill + mainClass.xpAdjustKill;
				}
				break;
			case MINE:
				if (subClass != null) {
					xp = race.xpAdjustMine + mainClass.xpAdjustMine + subClass.xpAdjustMine;
				} else {
					xp = race.xpAdjustMine + mainClass.xpAdjustMine;
				}
				break;
			case SMELT:
				if (subClass != null) {
					xp = race.xpAdjustSmelt + mainClass.xpAdjustSmelt + subClass.xpAdjustSmelt;
				} else {
					xp = race.xpAdjustSmelt + mainClass.xpAdjustSmelt;
				}
				break;
			default:
				xp = 0;
		}
		return xp;
	}

	public int getSkillPointsLeft() {
		return getMaxSkillPointsLeft() - getSkillPointsSpent();
	}

	public int getSkillPointsSpent() {
		int result = 0;
		for (Entry<String, Integer> cost : skillsPurchased.entrySet()) {
			String lKey = cost.getKey().toLowerCase();
			if (lKey.startsWith(mainClass.name.toLowerCase()) || lKey.startsWith(race.name.toLowerCase()) || (subClass != null && lKey.startsWith(subClass.name.toLowerCase()))) {
				result += cost.getValue();
			}
		}
		return result;
	}

	/**
	 * @return the statChr
	 */
	public int getStatChr() {
		if (lq.configMain.disableStats) { return 10; }
		int stat;
		stat = statChr;
		if (race != null) {
			stat += race.statChr;
		}
		if (mainClass != null) {
			if (subClass != null) {
				int classboost = 0;
				if (mainClass.statChr > -1 && subClass.statChr > -1) {
					// both positive (ok 0+)statChr
					classboost = Math.max(mainClass.statChr, subClass.statStr);
				} else if (mainClass.statChr < 1 && subClass.statChr < 1) {
					// both negative (ok 0+)
					classboost = Math.max(mainClass.statChr, subClass.statChr);
				} else {
					classboost = mainClass.statChr + subClass.statChr;
				}
				stat += classboost;
			} else {
				stat += mainClass.statChr;
			}
		}
		return stat;
	}

	/**
	 * @return the statCon
	 */
	public int getStatCon() {
		if (lq.configMain.disableStats) { return 10; }
		int stat;
		stat = statCon;
		if (race != null) {
			stat += race.statCon;
		}
		if (mainClass != null) {
			if (subClass != null) {
				int classboost = 0;
				if (mainClass.statCon > -1 && subClass.statCon > -1) {
					// both positive (ok 0+)
					classboost = Math.max(mainClass.statCon, subClass.statCon);
				} else if (mainClass.statCon < 1 && subClass.statCon < 1) {
					// both negative (ok 0+)
					classboost = Math.max(mainClass.statCon, subClass.statCon);
				} else {
					classboost = mainClass.statCon + subClass.statCon;
				}
				stat += classboost;
			} else {
				stat += mainClass.statCon;
			}
		}
		return stat;
	}

	/**
	 * @return the statDex
	 */
	public int getStatDex() {
		if (lq.configMain.disableStats) { return 10; }
		int stat;
		stat = statDex;
		if (race != null) {
			stat += race.statDex;
		}
		if (mainClass != null) {
			if (subClass != null) {
				int classboost = 0;
				if (mainClass.statDex > -1 && subClass.statDex > -1) {
					// both positive (ok 0+)
					classboost = Math.max(mainClass.statDex, subClass.statDex);
				} else if (mainClass.statDex < 1 && subClass.statDex < 1) {
					// both negative (ok 0+)
					classboost = Math.max(mainClass.statDex, subClass.statDex);
				} else {
					classboost = mainClass.statDex + subClass.statDex;
				}
				stat += classboost;
			} else {
				stat += mainClass.statDex;
			}
		}
		return stat;
	}

	/**
	 * @return the statInt
	 */
	public int getStatInt() {
		if (lq.configMain.disableStats) { return 10; }
		int stat;
		stat = statInt;
		if (race != null) {
			stat += race.statInt;
		}
		if (mainClass != null) {
			if (subClass != null) {
				int classboost = 0;
				if (mainClass.statInt > -1 && subClass.statInt > -1) {
					// both positive (ok 0+)
					classboost = Math.max(mainClass.statInt, subClass.statInt);
				} else if (mainClass.statInt < 1 && subClass.statInt < 1) {
					// both negative (ok 0+)
					classboost = Math.max(mainClass.statInt, subClass.statInt);
				} else {
					classboost = mainClass.statInt + subClass.statInt;
				}
				stat += classboost;
			} else {
				stat += mainClass.statInt;
			}
		}
		return stat;
	}

	/**
	 * @return the statStr
	 */
	public int getStatStr() {
		if (lq.configMain.disableStats) { return 10; }
		int stat;
		stat = statStr;
		if (race != null) {
			stat += race.statStr;
		}
		if (mainClass != null) {
			if (subClass != null) {
				int classboost = 0;
				if (mainClass.statStr > -1 && subClass.statStr > -1) {
					// both positive (ok 0+)
					classboost = Math.max(mainClass.statStr, subClass.statStr);
				} else if (mainClass.statStr < 1 && subClass.statStr < 1) {
					// both negative (ok 0+)
					classboost = Math.max(mainClass.statStr, subClass.statStr);
				} else {
					classboost = mainClass.statStr + subClass.statStr;
				}
				stat += classboost;
			} else {
				stat += mainClass.statStr;
			}
		}
		return stat;
	}

	/**
	 * @return the statWis
	 */
	public int getStatWis() {
		if (lq.configMain.disableStats) { return 10; }
		int stat;
		stat = statWis;
		if (race != null) {
			stat += race.statWis;
		}
		if (mainClass != null) {
			if (subClass != null) {
				int classboost = 0;
				if (mainClass.statWis > -1 && subClass.statWis > -1) {
					// both positive (ok 0+)
					classboost = Math.max(mainClass.statWis, subClass.statWis);
				} else if (mainClass.statWis < 1 && subClass.statWis < 1) {
					// both negative (ok 0+)
					classboost = Math.max(mainClass.statWis, subClass.statWis);
				} else {
					classboost = mainClass.statWis + subClass.statWis;
				}
				stat += classboost;
			} else {
				stat += mainClass.statWis;
			}
		}
		return stat;
	}

	public void checkSkills() {
		HashMap<String, SkillDataStore> potentialSkills = getUniqueSkills();
		Map<String, Boolean> activeSkills = new HashMap<String, Boolean>();
		for (Entry<String, SkillDataStore> s : potentialSkills.entrySet()) {
			if (isValidSkill(s.getKey())) {
				activeSkills.put(s.getValue().name, true);
			}
		}
		skillsSelected = activeSkills;
	}

	public boolean isValidSkill(String skill) {
		int level = SetExp.getLevelOfXpAmount(currentXP);
		HashMap<String, SkillDataStore> potentialSkills = getUniqueSkills();
		if (potentialSkills.containsKey(skill)) {
			SkillDataStore s = potentialSkills.get(skill);
			boolean valid = false;
			if (s.levelRequired <= level && s.skillPoints < 1) {
				valid = true;
			}
			if ((skillsPurchased.containsKey(mainClass.name + "|" + s.name) || skillsPurchased.containsKey(race.name + "|" + s.name) || (subClass != null && skillsPurchased.containsKey(subClass.name + "|" + s.name))) && (s.levelRequired <= level)) {
				valid = true;
			}
			if (!valid) {
				return false;
			}
			valid = checkSkillDeps(skill);
			if (valid) {
				return true;
			}
		}
		return false;
	}

	public boolean checkSkillDeps(String skill) {
		boolean valid = false;
		HashMap<String, SkillDataStore> potentialSkills = getUniqueSkills();
		if (potentialSkills.containsKey(skill)) {
			SkillDataStore s = potentialSkills.get(skill);
			if (s.requiresOne != null && !s.requiresOne.isEmpty()) {
				for (String skillname : s.requiresOne) {
					if (isValidSkill(skillname)) {
						valid = true;
					}
				}
			} else {
				valid = true;
			}

			if (valid) {
				if (s.requires != null && !s.requires.isEmpty()) {
					for (String skillname : s.requires) {
						if (!isValidSkill(skillname)) {
							valid = false;
						}
					}
				}
			}

			if ((s.requires == null || s.requires.isEmpty())) {
				if (s.requiresOne == null || s.requiresOne.isEmpty()) {
					return true;
				}
			}
		}
		return valid;
	}

	public HashMap<String, SkillDataStore> getUniqueSkills() {
		return getUniqueSkills(false);
	}

	public HashMap<String, SkillDataStore> getUniqueSkills(boolean rescan) {
		// use cached set if present unless told otherwise
		if (!rescan && this.skillSet != null && !skillSet.isEmpty()) {
			return skillSet;
		}

		Set<SkillDataStore> set = new HashSet<SkillDataStore>();
		set.addAll(race.availableSkills);
		set.addAll(race.outsourcedSkills);
		if (subClass != null) {
			set.addAll(subClass.availableSkills);
			set.addAll(subClass.outsourcedSkills);
		}
		set.addAll(mainClass.availableSkills);
		set.addAll(mainClass.outsourcedSkills);
		List<SkillDataStore> uniques = new ArrayList<SkillDataStore>();
		uniques.addAll(set);
		return makeMap(uniques);
	}

	public HashMap<String, SkillDataStore> makeMap(List<SkillDataStore> in) {
		HashMap<String, SkillDataStore> out = new HashMap<String, SkillDataStore>();
		for (SkillDataStore item : in) {
			out.put(item.name, item);
		}
		return out;
	}

	public String getSkillsource(String skillName) {
		String source = null;
		for (SkillDataStore s : race.availableSkills) {
			if (s.name.equalsIgnoreCase(skillName)) {
				return race.name;
			}
		}
		for (SkillDataStore s : race.outsourcedSkills) {
			if (s.name.equalsIgnoreCase(skillName)) {
				return race.name;
			}
		}

		for (SkillDataStore s : mainClass.availableSkills) {
			if (s.name.equalsIgnoreCase(skillName)) {
				return mainClass.name;
			}
		}
		for (SkillDataStore s : mainClass.outsourcedSkills) {
			if (s.name.equalsIgnoreCase(skillName)) {
				return mainClass.name;
			}
		}

		if (subClass != null) {
			for (SkillDataStore s : subClass.availableSkills) {
				if (s.name.equalsIgnoreCase(skillName)) {
					return subClass.name;
				}
			}
			for (SkillDataStore s : subClass.outsourcedSkills) {
				if (s.name.equalsIgnoreCase(skillName)) {
					return subClass.name;
				}
			}
		}

		return source;
	}

	public boolean hasMastered(String className) {
		lq.logger.info("className (" + className + ")...");
		if (xpEarnt.containsKey(className.toLowerCase())) {
			lq.logger.info("className (" + className + "): " + xpEarnt.get(className.toLowerCase()));
			if (xpEarnt.get(className.toLowerCase()) >= lq.configMain.max_xp) {
				return true;
			}
		}
		return false;
	}

	public void healthCheck() {
		Player p = Bukkit.getServer().getPlayer(uuid);
		if (p != null) {

			if (!lq.validWorld(p.getWorld().getName())) {
				if (lq.configMain.manageHealthNonLqWorlds) {
					HealthStore hs = lq.datasync.getAltHealthStore(p.getUniqueId());
					if (hs == null || hs.getMaxhealth() < 1) {
						double hp = p.getHealth();
						if (hp > 20.0D) {
							hp = 20.0D;
							p.setHealth(hp);
							p.setMaxHealth(20.0D);
							p.setHealthScale(20.0D);
						}
					} else {
						p.setHealth(hs.getHealth());
						p.setMaxHealth(hs.getMaxhealth());
						p.setHealthScale(20.0D);
					}
				}
				return;
			}

			getMaxHealth();

			this.health = p.getHealth();

			if (this.health > 0.0D) {
				if (this.health > this.maxHP) {
					this.health = this.maxHP;
				}

				p.setHealth(Math.min(this.health, p.getMaxHealth()));

				p.setMaxHealth(this.maxHP);
				p.setHealth(this.health);
				double scale = this.maxHP;
				if (scale > 40.0D) {
					scale = 40.0D;
				}
				p.setHealthScale(scale);
				p.setHealthScaled(true);
				if (lq.configMain.debugMode) {
					lq.debug.fine("SHC - HP: " + p.getHealth() + " | pHP: " + this.health + " | p.max: " + p.getMaxHealth() + " | pc.max: " + this.maxHP);
				}
			}
		}
	}

	public boolean manaGain() {
		double gain;
		gain = race.manaPerSecond;
		if (subClass != null) {
			gain += (Math.max(mainClass.manaPerSecond, subClass.manaPerSecond));
		} else {
			gain += mainClass.manaPerSecond;
		}
		return manaGain(gain);
	}

	public boolean manaGain(double gain) {
		double manaNow = this.mana;
		this.mana += Math.round(gain);
		if (this.mana > getMaxMana()) {
			this.mana = getMaxMana();
		}
		return (manaNow != this.mana);
	}

	public void manaLoss(int loss) {
		this.mana -= loss;
		if (this.mana < 0) {
			this.mana = 0;
		}
	}

	public void scheduleCheckInv() {
		Bukkit.getServer().getScheduler().runTaskLater(lq, new DelayedInvCheck(), 2L);
	}

	public void scheduleHealthCheck() {
		Bukkit.getServer().getScheduler().runTaskLater(lq, new DelayedCheck(), 2L);
	}
	
	public void scheduleXPSave() {
		Bukkit.getServer().getScheduler().runTaskLater(lq, new DelayedXPSave(), 2L);
	}

	public void setXP(int newXP) {
		xpEarnt.put(mainClass.name.toLowerCase(), newXP);
		if (subClass != null) {
			xpEarnt.put(subClass.name.toLowerCase(), newXP);
		}

		// lq.debug.fine("newXP:"+newXP);

		currentXP = newXP;

		// lq.debug.fine("currentXP:"+currentXP);

		Player p = Bukkit.getServer().getPlayer(uuid);
		if (p != null) {
			SetExp.setTotalExperience(p, newXP);
		}
	}
	
	public void saveXP() {
		currentXP = SetExp.getTotalExperience(getPlayer());
		xpEarnt.put(mainClass.name.toLowerCase(), SetExp.getTotalExperience(getPlayer()));
		if (subClass != null) {
			xpEarnt.put(subClass.name.toLowerCase(), SetExp.getTotalExperience(getPlayer()));
		}
		
	}

	public void giveXP(int XPGain) {
		PlayerExpChangeEvent event = new PlayerExpChangeEvent(getPlayer(), XPGain);
		Bukkit.getPluginManager().callEvent(event);
		XPGain = event.getAmount();
		this.setXP(currentXP + XPGain);
	}

	public boolean canCraft() {
		if (race.allowCrafting || mainClass.allowCrafting) {
			return true;
		}
		if (subClass != null && subClass.allowCrafting) {
			return true;
		}
		return false;
	}

	public boolean canSmelt() {
		if (race.allowSmelting || mainClass.allowSmelting) {
			return true;
		}
		if (subClass != null && subClass.allowSmelting) {
			return true;
		}
		return false;
	}

	public boolean canBrew() {
		if (race.allowBrewing || mainClass.allowBrewing) {
			return true;
		}
		if (subClass != null && subClass.allowBrewing) {
			return true;
		}
		return false;
	}

	public boolean canEnchant() {
		if (race.allowEnchating || mainClass.allowEnchating) {
			return true;
		}
		if (subClass != null && subClass.allowEnchating) {
			return true;
		}
		return false;
	}

	public boolean canRepair() {
		if (race.allowRepairing || mainClass.allowRepairing) {
			return true;
		}
		if (subClass != null && subClass.allowRepairing) {
			return true;
		}
		return false;
	}

	public boolean validSkill(String name) {
		if (!lq.validWorld(getPlayer().getWorld().getName())) {
			return false;
		}

		checkSkills();
		if (skillsSelected != null && name != null) {
			Boolean hasSkill = skillsSelected.get(name);
			if (hasSkill != null) {
				return hasSkill;
			}
		}
		return false;
	}

	public void useSkill(String name) {
		if (validSkill(name)) {
			SkillPhase phase = getSkillPhase(name);
			Player p = getPlayer();
			if (phase == SkillPhase.READY) {
				SkillDataStore skill = skillSet.get(name);
				skill.setLastUse(System.currentTimeMillis());
				if (p != null && p.isOnline()) {
					skill.setLastUseLoc(p.getLocation().clone());
				}
				if (skill.delay < 1 && skill.buildup < 1) {
					skill.startperms(lq, p);
					skill.start(lq, this);
				}
			} else if (phase == SkillPhase.COOLDOWN) {
				if (p != null && p.isOnline()) {
					p.sendMessage(name + " " + lq.configLang.skillCooldown);
				}
			} else if (phase == SkillPhase.DELAYED) {
				if (p != null && p.isOnline()) {
					p.sendMessage(name + " " + lq.configLang.skillDelayed);
				}
			} else if (phase == SkillPhase.BUILDING) {
				if (p != null && p.isOnline()) {
					p.sendMessage(name + " " + lq.configLang.skillBuilding);
				}
			} else if (phase == SkillPhase.ACTIVE) {
				if (p != null && p.isOnline()) {
					p.sendMessage(name + " " + lq.configLang.skillActive);
				}
			}
		} else {
			Player p = getPlayer();
			p.sendMessage(lq.configLang.skillInvalid + name);
		}
	}

	public SkillPhase getSkillPhase(String name) {
		if (skillSet.containsKey(name)) {
			if (validSkill(name)) {
				return skillSet.get(name).checkPhase();
			}
		}
		return null;
	}

	public int getStat(Attribute attr) {
		if (lq.configMain.disableStats) { return 10; }
		switch (attr) {
			case STR:
				return getStatStr();
			case DEX:
				return getStatDex();
			case CON:
				return getStatCon();
			case WIS:
				return getStatWis();
			case INT:
				return getStatInt();
			case CHR:
				return getStatChr();
			default:
				throw new IllegalStateException();
		}
	}

	public int getAttributeModifier(Attribute attr) {
		return Mechanics.getPlayersAttributeModifier(this, attr);
	}

	public int skillTest(int dif, Attribute attr) {
		return Mechanics.skillTest(dif, attr, this);
	}

	public int skillTest(Difficulty dif, Attribute attr, PC pc) {
		return Mechanics.skillTest(dif, attr, this);
	}

	public boolean skillTestB(int dif, Attribute attr, PC pc) {
		return Mechanics.skillTestB(dif, attr, this);
	}

	public boolean skillTestB(Difficulty dif, Attribute attr, PC pc) {
		return Mechanics.skillTestB(dif, attr, this);
	}

	public double logOfBase(int base, long num) {
		return Math.log(num) / Math.log(base);
	}

	public String karmaName() {
		return karmaName(this.karma);
	}

	public String karmaName(long number) {
		int x = (int) logOfBase(lq.configMain.karmaScale, Math.abs(number));
		String karma = null;
		// x is index
		if (number > 0) {
			if (x > lq.configLang.karmaPositiveItems.size() - 1) {
				x = lq.configLang.karmaPositiveItems.size() - 1;
			}
			karma = lq.configLang.karmaPositiveItems.get(x);
		} else if (number < 0) {
			if (x > lq.configLang.karmaNegativeItems.size() - 1) {
				x = lq.configLang.karmaNegativeItems.size() - 1;
			}
			karma = lq.configLang.karmaNegativeItems.get(x);
		} else {
			karma = lq.configLang.karmaPositiveItems.get(0);
		}

		return karma;
	}

	public Player getPlayer() {
		return lq.getServer().getPlayer(uuid);
	}

	public boolean payMana(int cost) {
		if (mana < cost) {
			return false;
		} else {
			mana = mana - cost;
			return true;
		}
	}

	public boolean payItem(ItemStack item) {
		int amount = item.getAmount();
		Player p = getPlayer();
		if (p == null) {
			return false;
		}
		if (!p.isOnline()) {
			return false;
		}
		PlayerInventory inv = p.getInventory();
		Material payment = item.getType();
		if (!inv.contains(payment)) {
			return false;
		} else {
			for (ItemStack i : inv.getContents()) {
				if (i.getType().equals(payment)) {
					if (i.getAmount() == amount) {
						inv.remove(i);
						return true;
					} else if (i.getAmount() > amount) {
						ItemStack replacement = new ItemStack(i.getType(), i.getAmount() - amount);
						inv.setItem(inv.first(i), replacement);
						return true;
					}
				}
			}
		}
		return false;
	}

	// skillLinkings
	public boolean hasLink(Material m) {
		return (skillLinkings.containsKey(m));
	}

	public String getLink(Material m) {
		return (skillLinkings.get(m));
	}

	public String addLink(Material m, String s) {
		return skillLinkings.put(m, s);
	}

	public String RemoveLink(Material m) {
		return skillLinkings.remove(m);
	}

	public boolean buySkill(String skillname) {
		SkillDataStore skill = getSkillData(skillname);
		if (skill != null) {
			getPlayer().sendMessage("Skill " + skillname + " found");
			int cost = skill.skillPoints;
			if (getSkillPointsLeft() >= cost) {
				if (checkSkillDeps(skillname)) {
					String classname = this.getSkillsource(skillname);
					skillsPurchased.put(classname + "|" + skillname, cost);
					return true;
				} else {
					if (skill.requires != null && !skill.requires.isEmpty()) {
						getPlayer().sendMessage(lq.configLang.skillRequires + skill.requires.toString());
					}
					if (skill.requiresOne != null && !skill.requiresOne.isEmpty()) {
						getPlayer().sendMessage(lq.configLang.skillRequiresOne + skill.requiresOne.toString());
					}
					return false;
				}
			} else {
				getPlayer().sendMessage(lq.configLang.skillPointsMissing + "'" + skillname + "': " + getSkillPointsLeft() + " (" + getSkillPointsSpent() + "/" + getMaxSkillPointsLeft() + ")");
				return false;
			}
		} else {
			getPlayer().sendMessage("Skill " + skillname + " not found");
			return false;
		}
	}

	public SkillDataStore getSkillData(String name) {
		SkillDataStore s = skillSet.get(name);
		return s;
	}

	public void changeClass(ClassType cl, Boolean sub) {
		if (cl == null) {
			lq.debug.fine(lq.configLang.classInvalid);
			return;
		}

		Player p = getPlayer();

		final int xpNow = SetExp.getTotalExperience(p);

		int newxp = 0;
		if (p.getLevel() > 1 && xpNow < lq.configMain.max_xp) {
			lq.debug.fine("Level is: " + p.getLevel());
			if ((!sub && this.mainClass != lq.classes.defaultClass) || (sub && this.subClass != null)) {
				lq.debug.fine("resetting " + p.getName() + " XP: " + p.getTotalExperience() + " - " + ((int) (p.getTotalExperience() * (lq.configMain.percentXpKeepClassChange / 100))));
				// reset XP
				newxp = (int) (xpNow * (lq.configMain.percentXpKeepClassChange / 100));
				this.setXP(newxp);
				lq.players.savePlayer(this);
			}
		}

		String oldClassname;
		if (sub) {
			oldClassname = this.subClass.name.toLowerCase();
			this.subClass = cl;
		} else {
			oldClassname = this.mainClass.name.toLowerCase();
			this.mainClass = cl;
		}
		int newclassxp = 0;
		if (this.xpEarnt.containsKey(cl.name.toLowerCase())) {
			newclassxp = this.xpEarnt.get(cl.name.toLowerCase());
		} else {
			newclassxp = newxp;
		}

		// if mastered class - save this xp and check if target class is mastered.
		if (xpNow > lq.configMain.max_xp) {
			this.xpEarnt.put(oldClassname, xpNow);

			if (newclassxp > lq.configMain.max_xp) {
				this.setXP(newclassxp);
			} else {
				this.setXP(0);
			}
		} else {
			// old class was not masteted - xp loss if any was done above.
			this.setXP(newclassxp);
		}

		lq.players.addPlayer(uuid, this);
		lq.players.savePlayer(this);
		this.scheduleHealthCheck();
		lq.players.scheduleUpdate(uuid);
		this.checkInv();
		this.skillSet = this.getUniqueSkills(true);
		lq.debug.fine(lq.configLang.classChanged + ": " + cl.name + " - " + p.getName());
	}

	public void changeRace(Race r) {
		this.race = r;
		this.raceChanged = true;
		lq.players.addPlayer(uuid, this);
		lq.players.savePlayer(uuid);
		this.scheduleHealthCheck();
		this.checkInv();
		this.skillSet = this.getUniqueSkills(true);
	}

	public void damage(double dmg) {
		damage(dmg, null);
	}

	public void damage(double dmg, Entity source) {
		getPlayer().damage(dmg, source);
		this.scheduleHealthCheck();
	}

	public void heal(double health) {
		heal(health, null);
	}

	public void heal(double health, Entity source) {
		Player p = getPlayer();
		double h = p.getHealth();
		h = h + health;
		if (h > p.getMaxHealth()) {
			h = p.getMaxHealth();
		}
		p.setHealth(h);
		this.health = h;
		this.scheduleHealthCheck();
	}

	public void setStatStr(int statStr) {
		this.statStr = statStr;
	}

	public void setStatDex(int statDex) {
		this.statDex = statDex;
	}

	public void setStatInt(int statInt) {
		this.statInt = statInt;
	}

	public void setStatWis(int statWis) {
		this.statWis = statWis;
	}

	public void setStatCon(int statCon) {
		this.statCon = statCon;
	}

	public void setStatChr(int statChr) {
		this.statChr = statChr;
	}
}
