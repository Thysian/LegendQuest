package me.sablednah.legendquest.config;

import java.util.ArrayList;

import me.sablednah.legendquest.Main;
import me.sablednah.legendquest.experience.SetExp;

public class MainConfig extends Config {

	public boolean				debugMode					= false;

	public boolean				useMySQL					= false;
	public String				sqlUsername					= "username";
	public String				sqlPassword					= "password";
	public String				sqlHostname					= "localhost";
	public int					sqlPort						= 3306;
	public String				sqlDatabase					= "legendquest";

	public boolean				logSQL						= true;
	public String				logLevel					= "ALL";

	public ArrayList<String>	worlds						= new ArrayList<String>();
	public boolean				randomStats					= true;

	public boolean				useScoreBoard				= true;

	public double				percentXpKeepClassChange	= 10.00D;
	public double				percentXpLossRespawn		= 10.00D;
	public int					max_level					= 150;
	public int					max_xp						= 58245;
	public boolean				XPnotify					= true;
	public double				scaleXP						= 100.00D;

	public boolean				useSkillTestForCombat		= true;
	public boolean				verboseCombat				= true;
	public boolean				useSizeForCombat			= true;

	public int					karmaDamagePlayer			= -10;
	public int					karmaDamageVillager			= -5;
	public int					karmaDamagePet				= -2;
	public int					karmaDamageAnimal			= -1;
	public int					karmaDamageMonster			= 2;
	public int					karmaDamageSlime			= 1;

	public int					karmaKillPlayer				= -1000;
	public int					karmaKillVillager			= -500;
	public int					karmaKillPet				= -200;
	public int					karmaKillAnimal				= -50;
	public int					karmaKillMonster			= 200;
	public int					karmaKillSlime				= 100;
	public int					karmaScale					= 500;

	public double				skillBuildupMoveAllowed		= 2.0D;
	public long					skillTickInterval			= 10L;

	public boolean				manageHealthNonLqWorlds		= true;

	public boolean				chatUsePrefix				= true;
	public boolean				chatProcessPrefix			= true;
	public String				chatPrefix					= "[{race}|{class} ({lvl})] {current}";

	public boolean				attributesModifyBaseStats	= false;
	public boolean				disableStats				= false;
	public boolean				verboseStats				= true;

	@SuppressWarnings("unchecked")
	public MainConfig(final Main p) {
		super(p, "config.yml");

		this.debugMode = this.getConfigItem("debugMode", this.debugMode);

		this.useMySQL = this.getConfigItem("useMySQL", this.useMySQL);
		this.sqlUsername = this.getConfigItem("sqlUsername", this.sqlUsername);
		this.sqlPassword = this.getConfigItem("sqlPassword", this.sqlPassword);
		this.sqlHostname = this.getConfigItem("sqlHostname", this.sqlHostname);
		this.sqlPort = this.getConfigItem("sqlPort", this.sqlPort);
		this.sqlDatabase = this.getConfigItem("sqlDatabase", this.sqlDatabase);
		this.logSQL = this.getConfigItem("logSQL", this.logSQL);
		this.logLevel = this.getConfigItem("logLevel", this.logLevel);

		this.worlds = (ArrayList<String>) this.getConfigItem("worlds", this.worlds);

		this.useScoreBoard = this.getConfigItem("useScoreBoard", this.useScoreBoard);

		this.randomStats = this.getConfigItem("randomStats", this.randomStats);
		this.percentXpKeepClassChange = this.getConfigItem("percentXpKeepClassChange", this.percentXpKeepClassChange);
		this.percentXpLossRespawn = this.getConfigItem("percentXpLossRespawn", this.percentXpLossRespawn);
		this.max_level = this.getConfigItem("max_level", this.max_level);
		this.max_xp = SetExp.getExpToLevel(this.max_level);
		this.XPnotify = this.getConfigItem("XPnotify", this.XPnotify);
		this.scaleXP = this.getConfigItem("scaleXP", this.scaleXP);

		this.useSkillTestForCombat = this.getConfigItem("useSkillTestForCombat", this.useSkillTestForCombat);
		this.useSizeForCombat = this.getConfigItem("useSizeForCombat", this.useSizeForCombat);
		this.verboseCombat = this.getConfigItem("verboseCombat", this.verboseCombat);

		this.karmaDamagePlayer = this.getConfigItem("karmaDamagePlayer", this.karmaDamagePlayer);
		this.karmaDamageVillager = this.getConfigItem("karmaDamageVillager", this.karmaDamageVillager);
		this.karmaDamagePet = this.getConfigItem("karmaDamagePet", this.karmaDamagePet);
		this.karmaDamageAnimal = this.getConfigItem("karmaDamageAnimal", this.karmaDamageAnimal);
		this.karmaDamageMonster = this.getConfigItem("karmaDamageMonster", this.karmaDamageMonster);
		this.karmaDamageSlime = this.getConfigItem("karmaDamageSlime", this.karmaDamageSlime);

		this.karmaKillPlayer = this.getConfigItem("karmaKillPlayer", this.karmaKillPlayer);
		this.karmaKillVillager = this.getConfigItem("karmaKillVillager", this.karmaKillVillager);
		this.karmaKillPet = this.getConfigItem("karmaKillPet", this.karmaKillPet);
		this.karmaKillAnimal = this.getConfigItem("karmaKillAnimal", this.karmaKillAnimal);
		this.karmaKillMonster = this.getConfigItem("karmaKillMonster", this.karmaKillMonster);
		this.karmaKillSlime = this.getConfigItem("karmaKilleSlime", this.karmaKillSlime);

		this.karmaScale = this.getConfigItem("karmaScale", this.karmaScale);

		this.skillBuildupMoveAllowed = this.getConfigItem("skillBuildupMoveAllowed", this.skillBuildupMoveAllowed);
		this.skillTickInterval = (long) this.getConfigItem("skillTickInterval", this.skillTickInterval);

		this.chatUsePrefix = this.getConfigItem("chatUsePrefix", this.chatUsePrefix);
		this.chatProcessPrefix = this.getConfigItem("chatProcessPrefix", this.chatProcessPrefix);
		this.chatPrefix = this.getConfigItem("chatPrefix", this.chatPrefix);

		this.attributesModifyBaseStats = this.getConfigItem("attributesModifyBaseStats", this.attributesModifyBaseStats);
		this.disableStats = this.getConfigItem("disableStats", this.disableStats);
		this.verboseStats = this.getConfigItem("verboseStats", this.verboseStats);
		
		
	}
}
