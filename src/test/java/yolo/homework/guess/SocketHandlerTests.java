package yolo.homework.guess;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SocketHandlerTests {

    @Test
    public void verifyOpenConnection() throws IOException, InterruptedException {
        WebSocketSession session = mock(WebSocketSession.class);
        TextMessage textMessage = new TextMessage("{\"name\":\"name\", \"number\":\"1\", \"amount\":\"2\"}");

        when(session.isOpen()).thenReturn(true);

        SocketHandler socketHandler = new SocketHandler();
        socketHandler.handleTextMessage(session, textMessage);
        TextMessage answer = new TextMessage("Hey name, your guess was 1, and you bet 2EUR!");

        verify(session, times(1)).sendMessage(answer);
    }

    @Test
    public void throwErrorForInvalidNumber() throws IOException, InterruptedException {
        WebSocketSession session = mock(WebSocketSession.class);
        TextMessage textMessage = new TextMessage("{\"name\":\"name\", \"number\":\"5\", \"amount\":\"five\"}");

        SocketHandler socketHandler = new SocketHandler();
        socketHandler.handleTextMessage(session, textMessage);

        assertThrows(NumberFormatException.class, () -> Integer.parseInt("five"));
    }

    @Test
    public void throwErrorForInvalidAmount() throws IOException, InterruptedException {
        WebSocketSession session = mock(WebSocketSession.class);
        TextMessage textMessage = new TextMessage("{\"name\":\"name\", \"number\":\"five\", \"amount\":\"2\"}");

        SocketHandler socketHandler = new SocketHandler();
        socketHandler.handleTextMessage(session, textMessage);

        assertThrows(NumberFormatException.class, () -> Integer.parseInt("five"));
    }
}
