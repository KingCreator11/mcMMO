package com.gmail.nossr50.util.experience;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.CoreSkills;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.player.PlayerLevelUtils;
import com.gmail.nossr50.util.text.StringUtils;
import com.neetgames.mcmmo.player.OnlineMMOPlayer;
import com.neetgames.mcmmo.skill.RootSkill;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A visual representation of a players skill level progress for a PrimarySkillType
 */
public class ExperienceBarWrapper {

    private final @NotNull PrimarySkillType primarySkillType; //Primary Skill
    private @NotNull BossBar bossBar;
    protected final @NotNull McMMOPlayer mmoPlayer;
    private int lastLevelUpdated;

    /*
     * This is stored to help optimize updating the title
     */
    protected String niceSkillName;
    protected String title;

    public ExperienceBarWrapper(@NotNull PrimarySkillType primarySkillType, @NotNull McMMOPlayer mmoPlayer) {
        this.mmoPlayer = mmoPlayer;
        this.primarySkillType = primarySkillType;
        title = "";
        lastLevelUpdated = 0;

        //These vars are stored to help reduce operations involving strings
        niceSkillName = StringUtils.getCapitalized(primarySkillType.toString());

        //Create the bar
        initBar();
    }

    private void initBar() {
        title = getTitleTemplate();
        createBossBar();
    }

    public void updateTitle() {
        title = getTitleTemplate();
        bossBar.setTitle(title);
    }

    private String getTitleTemplate() {
        //If they are using extra details

        if(ExperienceConfig.getInstance().isEarlyGameBoostEnabled() && PlayerLevelUtils.qualifiesForEarlyGameBoost(mmoPlayer, primarySkillType)) {
                return LocaleLoader.getString("XPBar.Template.EarlyGameBoost");
        } else if(ExperienceConfig.getInstance().getAddExtraDetails())
            return LocaleLoader.getString("XPBar.Complex.Template", LocaleLoader.getString("XPBar."+niceSkillName, getLevel()), getCurrentXP(), getMaxXP(), getPowerLevel(), getPercentageOfLevel());

        return LocaleLoader.getString("XPBar."+niceSkillName, getLevel(), getCurrentXP(), getMaxXP(), getPowerLevel(), getPercentageOfLevel());
    }

    private int getLevel() {
        return mmoPlayer.getSkillLevel(primarySkillType);
    }
    private int getCurrentXP() {
        return mmoPlayer.getSkillExperience(primarySkillType);
    }
    private int getMaxXP() {
        return mmoPlayer.getExperienceToNextLevel(primarySkillType);
    }
    private int getPowerLevel() { return mmoPlayer.getPowerLevel(); }
    private int getPercentageOfLevel() { return (int) (mmoPlayer.getProgressInCurrentSkillLevel(primarySkillType) * 100); }

    public String getTitle() {
        return bossBar.getTitle();
    }

    public void setTitle(String s) {
        bossBar.setTitle(s);
    }

    public BarColor getColor() {
        return bossBar.getColor();
    }

    public void setColor(BarColor barColor) {
        bossBar.setColor(barColor);
    }

    public BarStyle getStyle() {
        return bossBar.getStyle();
    }

    public void setStyle(BarStyle barStyle) {
        bossBar.setStyle(barStyle);
    }

    public void setProgress(double v) {
        //Clamp Values
        if(v < 0)
            bossBar.setProgress(0.0D);

        else if(v > 1)
            bossBar.setProgress(1.0D);
        else
            bossBar.setProgress(v);

        //Check player level
        if(ExperienceConfig.getInstance().isEarlyGameBoostEnabled() && PlayerLevelUtils.qualifiesForEarlyGameBoost(mmoPlayer, primarySkillType)) {
           setColor(BarColor.YELLOW);
        } else {
            setColor(ExperienceConfig.getInstance().getExperienceBarColor(primarySkillType));
        }

        //Every time progress updates we need to check for a title update
        if(getLevel() != lastLevelUpdated || ExperienceConfig.getInstance().getDoExperienceBarsAlwaysUpdateTitle())
        {
            updateTitle();
            lastLevelUpdated = getLevel();
        }
    }

    public double getProgress() {
        return bossBar.getProgress();
    }

    public List<Player> getPlayers() {
        return bossBar.getPlayers();
    }

    public boolean isVisible() {
        return bossBar.isVisible();
    }

    public void hideExperienceBar()
    {
        bossBar.setVisible(false);
    }

    public void showExperienceBar()
    {
        bossBar.setVisible(true);
    }

    /*public NamespacedKey getKey()
    {
        return bossBar
    }*/

    private void createBossBar()
    {
        bossBar = Bukkit.getServer().createBossBar(title, ExperienceConfig.getInstance().getExperienceBarColor(primarySkillType), ExperienceConfig.getInstance().getExperienceBarStyle(primarySkillType));
        bossBar.addPlayer(mmoPlayer.getPlayer());
    }
}
