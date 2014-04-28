package me.sablednah.legendquest.skills;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import me.sablednah.legendquest.Main;
import me.sablednah.legendquest.playercharacters.PC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

public class SkillDataStore {

	public String					name;
	public SkillType				type;
	public String					description;
	public String					author;
	public double					version;

	public int						buildup			= 0;
	public int						delay			= 0;
	public int						duration		= 0;
	public int						cooldown		= 0;

	public int						manaCost		= 0;
	public ItemStack				consumes		= null;

	public int						levelRequired	= 0;
	public int						skillPoints		= 0;
	public HashMap<String, Object>	vars			= new HashMap<String, Object>();

	public String					permission;
	public String					startCommand;
	public String					endCommand;

	private Location				lastUseLoc		= null;
	private long					lastUse			= 0;
	private boolean					isCanceled		= false;
	private boolean					isActive		= false;

	private SkillPhase				phase			= SkillPhase.READY;

	public SkillDataStore(ConfigurationSection conf) {
		readConfigInfo(conf);
	}

	public SkillDataStore(SkillInfo defaults) {
		this.name = defaults.name;
		this.version = defaults.version;
		this.type = defaults.type;
		this.author = defaults.author;
		this.description = defaults.description;
		this.buildup = defaults.buildup;
		this.delay = defaults.delay;
		this.duration = defaults.duration;
		this.cooldown = defaults.cooldown;
		this.manaCost = defaults.manaCost;
		this.levelRequired = defaults.levelRequired;
		this.skillPoints = defaults.skillPoints;
		this.consumes = defaults.consumes;
		this.vars = defaults.vars;
	}

	public void readConfigInfo(final ConfigurationSection conf) {
		if (conf != null) {
			// bthis.name = skillInfo.getName();
			if (conf.contains("permission")) {
				this.permission = conf.getString("permission");
			}
			if (conf.contains("command")) {
				this.startCommand = conf.getString("command");
			}
			if (conf.contains("startcommand")) {
				this.startCommand = conf.getString("startcommand");
			}
			if (conf.contains("endcommand")) {
				this.endCommand = conf.getString("endcommand");
			}
			if (conf.contains("buildup")) {
				this.buildup = conf.getInt("buildup");
			}
			if (conf.contains("delay")) {
				this.delay = conf.getInt("delay");
			}
			if (conf.contains("duration")) {
				this.duration = conf.getInt("duration");
			}
			if (conf.contains("cooldown")) {
				this.cooldown = conf.getInt("cooldown");
			}
			if (conf.contains("level")) {
				this.levelRequired = conf.getInt("level");
			}
			if (conf.contains("cost")) {
				this.skillPoints = conf.getInt("cost");
			}
			if (conf.contains("manaCost")) {
				this.manaCost = conf.getInt("manaCost");
			}
			if (conf.contains("consumes")) {
				this.consumes = new ItemStack(Material.getMaterial(conf.getString("consumes")));
			}
			if (conf.contains("vars")) {
				Map<String, Object> tmpvar = conf.getConfigurationSection("vars").getValues(false);
				Iterator<Entry<String, Object>> entries = tmpvar.entrySet().iterator();
				while (entries.hasNext()) {
					Entry<String, Object> entry = entries.next();
					Object data = (Object) entry.getValue();
					System.out.print("Loading " + this.name + " Skill vars: " + entry.getKey() + " - " + data);
					vars.put(entry.getKey(), data);
				}

			}
		}
		System.out.print("skill dataset loaded: " + this.name + "|" + this.skillPoints + "|" + this.levelRequired + "|" + this.delay + "|" + this.duration + "|" + this.cooldown);
	}

	public SkillPhase checkPhase() {
		if (this.type == SkillType.PASSIVE) {
			return SkillPhase.ACTIVE;
		}
		long time = System.currentTimeMillis();
		long timeline = lastUse;

		if (time < timeline) { // last used in future!?!
			return SkillPhase.READY;
		}

		timeline = timeline + buildup;
		if (time < timeline) {// skill is building up
			return SkillPhase.BUILDING;
		}

		timeline = timeline + delay;
		if (time < timeline) {// skill is delayed
			return SkillPhase.DELAYED;
		}

		timeline = timeline + duration;
		if (time < timeline) {// skill is active
			return SkillPhase.ACTIVE;
		}

		timeline = timeline + cooldown;
		if (time < timeline) {// skill is coolingdown
			return SkillPhase.COOLDOWN;
		}

		return SkillPhase.READY;
	}

	public void startperms(Main lq, Player p) {
		if (permission != null && (!permission.isEmpty())) {
			System.out.print("[skill tick] datastore starting skill perm: " + permission);

			if (lq.players.permissions.containsKey(p.getUniqueId().toString() + permission)) {
				p.removeAttachment(lq.players.permissions.get(p.getUniqueId().toString() + permission));
				lq.players.permissions.remove(p.getUniqueId().toString() + permission);
			}
			PermissionAttachment attachment = p.addAttachment(lq, permission, true, (int) lq.configMain.skillTickInterval + 1);
			lq.players.permissions.put(p.getUniqueId().toString() + permission, attachment);
		}
	}

	public boolean start(Main lq, PC activePlayer) {
		Player p = activePlayer.getPlayer();
		// pay the price...
		if (manaCost > 0) {
			if (activePlayer.mana < manaCost) {
				p.sendMessage(lq.configLang.skillLackOfMana);
				isCanceled = true;
				lastUse = 0;
				activePlayer.skillSet.put(name, this);
				return false;
			} else {
				activePlayer.mana = activePlayer.mana - manaCost;
			}
		}
		// TODO put ingrediant use check here.

		// run the start command if any.
		System.out.print("[skill tick] starting skill command: " + startCommand);
		if (startCommand != null && (!startCommand.isEmpty())) {
			System.out.print("[skill tick] datastore starting skill command: " + startCommand);
			lq.getServer().dispatchCommand(p, startCommand);
			Skill skillClass = lq.skills.skillList.get(name);
			if (skillClass != null) {
				// CommandResult result =
				skillClass.onCommand();
			}
		}
		return true;
	}

	public Location getLastUseLoc() {
		return lastUseLoc;
	}

	public void setLastUseLoc(Location lastUseLoc) {
		this.lastUseLoc = lastUseLoc;
	}

	public long getLastUse() {
		return lastUse;
	}

	public void setLastUse(long lastUse) {
		this.lastUse = lastUse;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public SkillPhase getPhase() {
		return phase;
	}

	public void setPhase(SkillPhase phase) {
		this.phase = phase;
	}
}