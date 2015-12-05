package me.stuntguy3000.java.telegames.object;

import lombok.Getter;
import me.stuntguy3000.java.telegames.Telegames;
import me.stuntguy3000.java.telegames.hook.TelegramHook;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableMessage;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;

public abstract class Command {
    @Getter
    private final String description;
    @Getter
    private final Telegames instance;
    @Getter
    private final String name;

    public Command(Telegames instance, String name, String description) {
        this.instance = instance;
        this.name = name;
        this.description = description;

        instance.getCommandHandler().registerCommand(this);
    }

    public String createBotFatherString() {
        return String.format("%s - %s", name, description);
    }

    public abstract void processCommand(CommandMessageReceivedEvent event);

    public void respond(Chat chat, String message) {
        chat.sendMessage(message, TelegramHook.getBot());
    }

    public void respond(Chat c, String s, Object... format) {
        String msg = String.format(s, format);
        respond(c, msg);
    }

    public void respond(Chat chat, SendableMessage message) {
        chat.sendMessage(message, TelegramHook.getBot());
    }
}
