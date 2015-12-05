package me.stuntguy3000.java.telegames.object;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// @author Luke Anderson | stuntguy3000
public class BotSettings {
    @Getter
    private Boolean autoUpdater;
    @Getter
    private List<Integer> telegramAdmins;
    @Getter
    private String telegramKey;

    public BotSettings() {
        this.telegramKey = "";
        this.telegramAdmins = new ArrayList<>();
        this.autoUpdater = true;
    }
}
    