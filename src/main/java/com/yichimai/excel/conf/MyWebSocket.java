package com.yichimai.excel.conf;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

//https://www.jianshu.com/p/e6f50e5b270e
@Component
public class MyWebSocket implements WebSocketHandler {

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
//    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();
    
    private static WebSocketSession CURRENT_SESSION ;
    
    public static void sendMsg(String msg) throws IOException {
    	CURRENT_SESSION.sendMessage(new TextMessage(msg));
    }
    

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("websocket connected");
		CURRENT_SESSION = session;
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

    /**
     * 群发自定义消息
     * */
//    public static void sendInfo(String message) throws IOException {
//        for (MyWebSocket item : webSocketSet) {
//            try {
//                item.sendMessage(message);
//            } catch (IOException e) {
//                continue;
//            }
//        } 
//    }

}
