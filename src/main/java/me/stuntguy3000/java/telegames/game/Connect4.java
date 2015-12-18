package me.stuntguy3000.java.telegames.game;

import me.stuntguy3000.java.telegames.object.Game;
import me.stuntguy3000.java.telegames.object.LobbyMember;
import me.stuntguy3000.java.telegames.util.GameState;
import pro.zackpollard.telegrambot.api.chat.message.send.InputFile;
import pro.zackpollard.telegrambot.api.chat.message.send.SendablePhotoMessage;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;

// @author Luke Anderson | stuntguy3000
public class Connect4 extends Game {
    private GameState gameState;

    public Connect4() {
        setGameInfo("Connect4", "Description");
        setDevModeOnly(true);

        gameState = GameState.WAITING_FOR_PLAYERS;
    }

    @Override
    public void endGame() {

    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {

    }

    @Override
    public boolean playerJoin(LobbyMember lobbyMember) {
        InputFile baseFile = new InputFile(getClass().getResource("/connect4.jpg"));
        getGameLobby().sendMessage(SendablePhotoMessage.builder().photo(baseFile).build());
        return true;
    }

    @Override
    public void playerLeave(String username, int userID) {

    }

    @Override
    public boolean tryStartGame() {
        return true;
    }
}
    