package me.sablednah.legendquest;

import java.util.logging.Logger;

import me.sablednah.legendquest.classes.Classes;
import me.sablednah.legendquest.cmds.CmdClass;
import me.sablednah.legendquest.cmds.CmdRace;
import me.sablednah.legendquest.cmds.CmdSkill;
import me.sablednah.legendquest.cmds.CmdStats;
import me.sablednah.legendquest.cmds.RootCommand;
import me.sablednah.legendquest.config.DataConfig;
import me.sablednah.legendquest.config.LangConfig;
import me.sablednah.legendquest.config.MainConfig;
import me.sablednah.legendquest.db.DataSync;
import me.sablednah.legendquest.listeners.AbilityControlEvents;
import me.sablednah.legendquest.listeners.DamageEvents;
import me.sablednah.legendquest.listeners.ItemControlEvents;
import me.sablednah.legendquest.listeners.KarmaMonitorEvents;
import me.sablednah.legendquest.listeners.PlayerEvents;
import me.sablednah.legendquest.playercharacters.PCs;
import me.sablednah.legendquest.races.Races;
import me.sablednah.legendquest.skills.SkillPool;
import me.sablednah.legendquest.utils.DebugLog;
import me.sablednah.legendquest.utils.ManaTicker;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public Logger logger;
    public MainConfig configMain;
    public LangConfig configLang;
    public DataConfig configData;
    public SkillPool skills;
    public Races races;
    public Classes classes;
    public DataSync datasync;
    public PCs players;
    public DebugLog debug;

    public static final int MAX_XP = 58245;
    public static final int MAX_LEVEL = 150;

    public void log(final String msg) {
        logger.info(msg);
        debug.info("[serverlog] " + msg);
    }

    public void logSevere(final String msg) {
        logger.severe(msg);
        debug.severe("[serverlog] " + msg);
    }

    public void logWarn(final String msg) {
        logger.warning(msg);
        debug.warning("[serverlog] " + msg);
    }

    @Override
    public void onDisable() {
        debug.closeLog();
        datasync.shutdown();

        log(configLang.shutdown);
    }

    @Override
    public void onEnable() {
        this.logger = getLogger();
        getDataFolder().mkdirs();

        // enable debugger.
        debug = new DebugLog(this);

        // TODO remove this in live setup.
        debug.setDebugMode();

        // load main core settings
        configMain = new MainConfig(this);

        if (configMain.debugMode) {
            debug.setDebugMode();
        }

        // Get localised text from config
        configLang = new LangConfig(this);

        // Grab the constants
        configData = new DataConfig(this);

        // Notify loading has begun...
        log(configLang.startup);

        // load skills
        skills = new SkillPool(this);
        
        // Time to read the Race and class files.
        races = new Races(this);
        classes = new Classes(this);

        // start database data writing task
        datasync = new DataSync(this);

        // now load players
        players = new PCs(this);

        // Lets listen for events shall we?
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerEvents(this), this);
        pm.registerEvents(new DamageEvents(this), this);
        pm.registerEvents(new ItemControlEvents(this), this);
        pm.registerEvents(new AbilityControlEvents(this), this);
        pm.registerEvents(new KarmaMonitorEvents(this), this);

        // setup commands
        getCommand("lq").setExecutor(new RootCommand(this));
        getCommand("race").setExecutor(new CmdRace(this));
        getCommand("class").setExecutor(new CmdClass(this));
        getCommand("stats").setExecutor(new CmdStats(this));
        getCommand("skill").setExecutor(new CmdSkill(this));

        // Mana ticker
        getServer().getScheduler().runTaskTimer(this, new ManaTicker(this), 20, 20);

    }

}
