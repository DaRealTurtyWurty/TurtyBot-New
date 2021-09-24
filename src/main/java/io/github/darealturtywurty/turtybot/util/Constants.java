package io.github.darealturtywurty.turtybot.util;

import java.awt.Color;
import java.text.DateFormat;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import io.github.darealturtywurty.turtybot.managers.levelling_system.LevellingManager;

public final class Constants {
    public static final long DEFAULT_OWNER_ID = 309776610255437824L;
    public static final long SECOND_TO_MILLI = 1000L;
    public static final long MINUTE_TO_MILLI = 60000L;
    public static final long HOUR_TO_MILLI = (long) 3.6e+6;
    public static final long DAY_TO_MILLI = (long) 8.64e+7;
    public static final long WEEK_TO_MILLI = (long) 6.048e+8;
    public static final long MONTH_TO_MILLI = (long) 2.628e+9;
    public static final long YEAR_TO_MILLI = (long) 3.154e+10;

    public static final String USER_AGENT = "Mozilla/5.0 | TurtyBot#8108 | TurtyWurty#5690";
    public static final Pattern URL_PATTERN = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Config CONFIG = ConfigFactory.load();
    public static final Logger LOGGER = Logger.getGlobal();
    public static final DateFormat DATE_FORMAT = DateFormat.getInstance();
    public static final Random RANDOM = new Random();

    public static final LevellingManager LEVELLING_MANAGER = new LevellingManager();

    public static final String STRAWPOLL_URL = "https://strawpoll.com/api/poll";
    public static final String R6STATS_URL = "https://api2.r6stats.com/public-api/";
    public static final String BEAN_DUMPY_URL = "https://images-ext-2.discordapp.net/external/hFKX8jvrVZ_MobmGXB5IHiWCCejb8T-Q-so6T414k50/https/media.discordapp.net/attachments/855162784924434442/859517109725954048/dumpy.gif";

    private Constants() {
    }

    public static final class ColorConstants {
        public static final Color BROWN = new Color(102, 51, 0);
        public static final Color DARK_BLUE = new Color(0, 0, 204);
        public static final Color DARK_BROWN = new Color(51, 0, 0);
        public static final Color DARK_GREEN = new Color(0, 153, 0);
        public static final Color DARK_RED = new Color(204, 0, 0);
        public static final Color DARK_YELLOW = new Color(255, 204, 0);
        public static final Color GOLD = new Color(255, 204, 51);
        public static final Color LIGHT_BLUE = new Color(51, 153, 255);
        public static final Color LIGHT_BROWN = new Color(153, 102, 0);
        public static final Color LIGHT_GREEN = new Color(0, 255, 51);
        public static final Color LIGHT_ORANGE = new Color(255, 153, 0);
        public static final Color LIGHT_RED = new Color(255, 51, 51);
        public static final Color LIGHT_YELLOW = new Color(255, 255, 153);
        public static final Color PURPLE = new Color(102, 0, 153);
    }
}
