package me.sablednah.legendquest.cmds;

import java.text.DecimalFormat;
import java.util.Map;

import me.sablednah.legendquest.Main;
import me.sablednah.legendquest.experience.SetExp;
import me.sablednah.legendquest.mechanics.Attribute;
import me.sablednah.legendquest.playercharacters.PC;
import me.sablednah.legendquest.utils.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdStats extends CommandTemplate implements CommandExecutor {

	public Main	lq;

	public CmdStats(final Main p) {
		this.lq = p;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		// get the enum for this command
		final Cmds cmd = Cmds.valueOf("STATS");

		if (!validateCmd(lq, cmd, sender, args)) {
			return true;
		}

		final boolean isPlayer = (sender instanceof Player);
		String targetName = null;

		if (args.length > 0) {
			targetName = args[0];
		} else {
			if (isPlayer) {
				if (!lq.validWorld(((Player) sender).getWorld().getName())) {
					((Player) sender).sendMessage(lq.configLang.invalidWorld);
					return true;
				}
				targetName = sender.getName();
			} else {
				sender.sendMessage(cmd.toString() + ": " + lq.configLang.invalidArgumentsCommand);
				return true;
			}
		}

		PC pc = null;
		if (targetName != null) {
			pc = lq.players.getPC(Utils.getPlayerUUID(targetName));
		}
		if (pc != null) {
			sender.sendMessage(lq.configLang.playerStats);
			sender.sendMessage(lq.configLang.playerName + ": " + pc.charname + " (" + targetName + ")");
			if (!lq.configMain.disableStats) {
				String mod = "";
				String output = "";
				if (pc.getAttributeModifier(Attribute.STR) >= 0) {
					mod = "+" + pc.getAttributeModifier(Attribute.STR);
				} else {
					mod = "" + pc.getAttributeModifier(Attribute.STR);
				}
				if (lq.configMain.verboseStats) {
					sender.sendMessage(lq.configLang.statSTR + ": " + pc.getStatStr() + " (" + mod + ")");
				} else {
					output += lq.configLang.statSTR + ": " + pc.getStatStr() + " (" + mod + ") ";
				}

				if (pc.getAttributeModifier(Attribute.DEX) >= 0) {
					mod = "+" + pc.getAttributeModifier(Attribute.DEX);
				} else {
					mod = "" + pc.getAttributeModifier(Attribute.DEX);
				}
				if (lq.configMain.verboseStats) {
					sender.sendMessage(lq.configLang.statDEX + ": " + pc.getStatDex() + " (" + mod + ")");
				} else {
					output += lq.configLang.statDEX + ": " + pc.getStatDex() + " (" + mod + ") ";
				}

				if (pc.getAttributeModifier(Attribute.INT) >= 0) {
					mod = "+" + pc.getAttributeModifier(Attribute.INT);
				} else {
					mod = "" + pc.getAttributeModifier(Attribute.INT);
				}
				if (lq.configMain.verboseStats) {
					sender.sendMessage(lq.configLang.statINT + ": " + pc.getStatInt() + " (" + mod + ")");
				} else {
					output += lq.configLang.statINT + ": " + pc.getStatInt() + " (" + mod + ") ";
				}

				if (pc.getAttributeModifier(Attribute.WIS) >= 0) {
					mod = "+" + pc.getAttributeModifier(Attribute.WIS);
				} else {
					mod = "" + pc.getAttributeModifier(Attribute.WIS);
				}
				if (lq.configMain.verboseStats) {
					sender.sendMessage(lq.configLang.statWIS + ": " + pc.getStatWis() + " (" + mod + ")");
				} else {
					output += lq.configLang.statWIS + ": " + pc.getStatWis() + " (" + mod + ") ";
				}

				if (pc.getAttributeModifier(Attribute.CON) >= 0) {
					mod = "+" + pc.getAttributeModifier(Attribute.CON);
				} else {
					mod = "" + pc.getAttributeModifier(Attribute.CON);
				}
				if (lq.configMain.verboseStats) {
					sender.sendMessage(lq.configLang.statCON + ": " + pc.getStatCon() + " (" + mod + ")");
				} else {
					output += lq.configLang.statCON + ": " + pc.getStatCon() + " (" + mod + ") ";
				}

				if (pc.getAttributeModifier(Attribute.CHR) >= 0) {
					mod = "+" + pc.getAttributeModifier(Attribute.CHR);
				} else {
					mod = "" + pc.getAttributeModifier(Attribute.CHR);
				}
				if (lq.configMain.verboseStats) {
					sender.sendMessage(lq.configLang.statCHR + ": " + pc.getStatChr() + " (" + mod + ")");
				} else {
					output += lq.configLang.statCHR + ": " + pc.getStatChr() + " (" + mod + ")";
				}
				if (!output.isEmpty()) {
					sender.sendMessage(output);
				}
			}
			if (lq.races.getRaces().size()>1) {
				sender.sendMessage(lq.configLang.statRace + ": " + pc.race.name);
			}
			sender.sendMessage(lq.configLang.statClass + ": " + pc.mainClass.name);

			sender.sendMessage("--------------------");
			sender.sendMessage(lq.configLang.statKarma + ": " + pc.karmaName() + " (" + pc.karma + ")");
			sender.sendMessage("--------------------");

			DecimalFormat df = new DecimalFormat("#.00");
			sender.sendMessage(Utils.barGraph(pc.health, pc.maxHP, 20, lq.configLang.statHealth, (" " + df.format(pc.health) + " / " + df.format(pc.maxHP))));
			sender.sendMessage(Utils.barGraph(pc.mana, pc.getMaxMana(), 20, lq.configLang.statMana, (" " + pc.mana + " / " + pc.getMaxMana())));
			sender.sendMessage("--------------------");

			for (final Map.Entry<String, Integer> entry : pc.xpEarnt.entrySet()) {
				sender.sendMessage("XP: " + entry.getKey().toLowerCase() + ": " + lq.configLang.statLevelShort + " " + SetExp.getLevelOfXpAmount(entry.getValue()) + " (" + entry.getValue() + ")");
			}

			sender.sendMessage("--------------------");
			sender.sendMessage(lq.configLang.skillPoints + ": " + pc.getSkillPointsLeft() + " (" + pc.getSkillPointsSpent() + "/" + pc.getMaxSkillPointsLeft() + ")");

			return true;
		} else {
			sender.sendMessage(lq.configLang.characterNotFound + targetName);
			return true;
		}
	}
}
