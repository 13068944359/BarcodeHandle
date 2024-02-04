package com.yichimai.excel.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

	@Autowired
	private MyWebSocket webSocket;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//		registry.addHandler() // todo  [2]
//       .addInterceptors() // todo  [3]
//        .setAllowedOrigins("*"); // 解决跨域问题 [4]
		
		registry.addHandler(webSocket, "/ws").addInterceptors().setAllowedOrigins("*");
		
	}

}