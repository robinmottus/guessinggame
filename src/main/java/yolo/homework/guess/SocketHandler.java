package yolo.homework.guess;

import com.google.gson.Gson;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    static Map<String, Double> winnings = new HashMap<>();

    static Map<String, PlayerGuess> playerGuesses = new HashMap<>();

    static int randomNumber;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
        Map<String, String> value = new Gson().fromJson(message.getPayload(), Map.class);

        String name = value.get("name");
        Optional<Integer> number = validateInt(value, session);
        Optional<Double> amount = validateDouble(value, session);

        if (number.isPresent() && amount.isPresent()) {

        PlayerGuess playerGuess = new PlayerGuess();
        playerGuess.setName(name);
        playerGuess.setNumber(number.get());
        playerGuess.setAmount(amount.get());

        session.sendMessage(new TextMessage("Hey " + value.get("name") + ", your guess was " + value.get("number") + ", and you bet " + value.get("amount") + "EUR!"));
        playerGuesses.put(session.getId(), playerGuess);
        }
    }

    private Optional<Integer> validateInt(Map<String, String> value, WebSocketSession session) throws IOException {
        try {
            int number = Integer.parseInt(value.get("number"));
            if (number < 1 || number > 10) {
                session.sendMessage(new TextMessage("Validation exception! Please enter a number from 1-10!"));
            }
            return Optional.of(number);
        } catch (NumberFormatException e) {
            session.sendMessage(new TextMessage("Validation exception! Please enter a number from 1-10!"));
            return Optional.empty();
        }
    }

    private Optional<Double> validateDouble(Map<String, String> value, WebSocketSession session) throws IOException {
        try {
            return Optional.of(Double.parseDouble(value.get("amount")));
        } catch (NumberFormatException e) {
            session.sendMessage(new TextMessage("Validation exception! Please enter an amount!"));
            return Optional.empty();
        }
    }

    private void checkForWin(int randomNumber) {
        try {
            for (WebSocketSession session : sessions) {
                if (playerGuesses.containsKey(session.getId())) {
                    PlayerGuess playerGuess = playerGuesses.get(session.getId());
                    if (playerGuess.getNumber() == randomNumber) {
                        double wonAmount = playerGuess.getNumber() * 9.9;
                        session.sendMessage(new TextMessage("Congratulations, you have won " + wonAmount + "EUR!"));
                        if (winnings.containsKey(playerGuess.getName())) {
                            winnings.put(playerGuess.getName(), winnings.get(playerGuess.getName()) + wonAmount);
                        } else {
                            winnings.put(playerGuess.getName(), wonAmount);
                        }
                    } else {
                        session.sendMessage(new TextMessage("Sorry, you lost! The number was " + randomNumber + "."));
                    }
                    playerGuesses.remove(session.getId());
                }

                if (!winnings.isEmpty()) {
                    session.sendMessage(new TextMessage("The winners thus far: " + winnings));
                }
            }
            startGame();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startTimer()  {
        startGame();
    }

    private void startGame() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                randomNumber = generateRandomNumber();
                checkForWin(randomNumber);
            }
        };

        Timer timer = new Timer("Timer");
        long delay = 10000L;

        timer.schedule(task, delay);
    }

    private int generateRandomNumber() {
        int range = 10;
        return (int) (Math.random() * range) + 1;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }
}
