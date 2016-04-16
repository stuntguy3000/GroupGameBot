package me.stuntguy3000.java.telegames.game;

import me.stuntguy3000.java.telegames.hook.TelegramHook;
import me.stuntguy3000.java.telegames.object.game.Game;
import me.stuntguy3000.java.telegames.object.game.GameState;
import me.stuntguy3000.java.telegames.object.user.TelegramUser;
import me.stuntguy3000.java.telegames.util.string.Lang;
import me.stuntguy3000.java.telegames.util.string.StringUtil;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardHide;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardMarkup;
import pro.zackpollard.telegrambot.api.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hangman extends Game {

    private Character censoredChar = '-';
    private List<Character> censoredWord = new ArrayList<>();
    private List<Character> guesses = new ArrayList<>();
    private int guessesLeft = 9;
    private List<String> predefinedWords = new ArrayList<>();
    private int roundsLeft;
    private TelegramUser selector; //cringe
    private String word;

    public Hangman() {
        setGameInfo(Lang.GAME_HANGMAN_NAME, Lang.GAME_HANGMAN_DESCRIPTION);
        setMinPlayers(2);
        setGameState(GameState.WAITING_FOR_PLAYERS);
    }

    private SendableTextMessage.SendableTextMessageBuilder createChooserKeyboard() {
        ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();


        replyKeyboardMarkupBuilder.addRow(new ArrayList<>(Collections.singletonList(Lang.GAME_HANGMAN_KEYBOARD_RANDOM)));

        replyKeyboardMarkupBuilder.resize(true);
        replyKeyboardMarkupBuilder.oneTime(true);
        replyKeyboardMarkupBuilder.selective(false);

        return SendableTextMessage.builder().replyMarkup(replyKeyboardMarkupBuilder.build());
    }

    private String getCensoredWord() {
        StringBuilder word = new StringBuilder();

        for (char letter : censoredWord) {
            word.append(letter);
        }

        return word.toString();
    }

    private String getGuessedLetters() {
        StringBuilder stringBuilder = new StringBuilder();

        for (char guess : guesses) {
            stringBuilder.append(guess).append(" ");
        }

        return stringBuilder.toString();
    }

    private boolean guessLetter(char letter) {
        boolean guessed = false;
        int index = 0;

        letter = Character.toLowerCase(letter);
        for (char wordCharacter : word.toCharArray()) {
            if (wordCharacter == letter) {
                censoredWord.remove(index);
                censoredWord.add(index, letter);
                guessed = true;
            }
            index++;
        }

        return guessed;
    }

    private boolean isAlphaCharactersOnly(String message) {
        char[] chars = message.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    private boolean isPlayer(TelegramUser telegramUser) {
        for (TelegramUser player : getActivePlayers()) {
            if (telegramUser.getUserID() == player.getUserID()) {
                return true;
            }
        }

        return false;
    }

    private void loadWords() {
        InputStream in = getClass().getResourceAsStream("/hangmanwords.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && line.matches("[A-z][A-z]+")) {
                    predefinedWords.add(line.toLowerCase().replace(" ", ""));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.shuffle(predefinedWords);
    }

    public void nextRound() {
        if (roundsLeft > 0) {
            selector = getActivePlayers().get(roundsLeft % getActivePlayers().size());
            word = null;
            censoredWord.clear();
            guesses.clear();
            guessesLeft = 9;
            getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_SELECTING, StringUtil.markdownSafe(selector.getUsername()))).parseMode(ParseMode.MARKDOWN).build());
            TelegramBot.getChat(selector.getUserID()).sendMessage(createChooserKeyboard().message(Lang.GAME_HANGMAN_SELECTING_ASK).build(), TelegramHook.getBot());
            roundsLeft--;
        } else {
            getGameLobby().stopGame();
        }
    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {
        if (event.getChat().getType() == ChatType.PRIVATE) {
            User sender = event.getMessage().getSender();
            String message = event.getContent().getContent();
            TelegramUser telegramUser = getGameLobby().getTelegramUser(sender.getUsername());

            if (isPlayer(telegramUser)) {
                if (word != null && message.length() == 1 && sender.getId() != selector.getUserID()) {
                    if (isAlphaCharactersOnly(message)) {
                        char letter = message.toCharArray()[0];
                        getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_GUESS_LETTER, StringUtil.markdownSafe(sender.getUsername()), letter)).parseMode(ParseMode.MARKDOWN).build());
                        if (guesses.contains(Character.toUpperCase(letter)) || guesses.contains(Character.toLowerCase(letter))) {
                            getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_GUESS_ALREADY_GUESSED, guessesLeft, getCensoredWord(), getGuessedLetters())).parseMode(ParseMode.MARKDOWN).build());
                        } else {
                            boolean guessedCorrectly = guessLetter(letter);

                            if (wordCompleted()) {
                                getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_GUESS_WORD, word)).parseMode(ParseMode.MARKDOWN).build());
                                nextRound();
                            } else {
                                if (guessedCorrectly) {
                                    guesses.add(letter);
                                    getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_GUESS_CORRECT, guessesLeft, getCensoredWord())).parseMode(ParseMode.MARKDOWN).build());
                                } else {
                                    --guessesLeft;
                                    if (guessesLeft > 0) {
                                        guesses.add(letter);
                                        getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_GUESS_INCORRECT, guessesLeft, getCensoredWord(), getGuessedLetters())).parseMode(ParseMode.MARKDOWN).build());
                                    } else {
                                        getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_GUESS_LOSE, word)).parseMode(ParseMode.MARKDOWN).build());
                                        nextRound();
                                        return;
                                    }
                                }
                            }
                        }
                    } else {
                        TelegramBot.getChat(selector.getUserID()).sendMessage(SendableTextMessage.builder().message(Lang.ERROR_ALPHA_ONLY).parseMode(ParseMode.MARKDOWN).build(), TelegramHook.getBot());
                    }
                    return;
                } else {
                    if (sender.getId() == selector.getUserID() && word == null) {
                        if (message.equals(Lang.GAME_HANGMAN_KEYBOARD_RANDOM)) {
                            message = predefinedWords.remove(0);
                            TelegramBot.getChat(selector.getUserID()).sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_RANDOM_CHOSEN, message)).parseMode(ParseMode.MARKDOWN).build(), TelegramHook.getBot());
                        }

                        if (isAlphaCharactersOnly(message)) {
                            if (message.length() >= 3) {
                                word = message.toLowerCase();

                                for (int i = 0; i < word.length(); i++) {
                                    censoredWord.add(i, censoredChar);
                                }

                                getGameLobby().sendMessage(SendableTextMessage.builder().message(String.format(Lang.GAME_HANGMAN_WORD_CHOSEN, getCensoredWord())).parseMode(ParseMode.MARKDOWN).replyMarkup(ReplyKeyboardHide.builder().build()).build());
                            } else {
                                TelegramBot.getChat(selector.getUserID()).sendMessage(SendableTextMessage.builder().message(Lang.ERROR_TOO_SHORT_3).parseMode(ParseMode.MARKDOWN).build(), TelegramHook.getBot());
                            }
                        } else {
                            TelegramBot.getChat(selector.getUserID()).sendMessage(SendableTextMessage.builder().message(Lang.ERROR_ALPHA_ONLY).parseMode(ParseMode.MARKDOWN).build(), TelegramHook.getBot());
                        }
                        return;
                    }
                }

                getGameLobby().userChat(telegramUser, message);
            }
        }
    }

    @Override
    public void startGame() {
        setGameState(GameState.INGAME);
        roundsLeft = getActivePlayers().size() * 3;

        loadWords();
        nextRound();
    }

    public boolean wordCompleted() {
        for (char wordCharacter : censoredWord) {
            if (wordCharacter == censoredChar) {
                return false;
            }
        }
        return true;
    }
}