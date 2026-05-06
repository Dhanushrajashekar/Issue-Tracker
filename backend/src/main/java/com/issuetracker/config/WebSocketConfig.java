package com.issuetracker.config;

import com.issuetracker.security.JwtService;
import com.issuetracker.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// WebSocket uses STOMP as the messaging protocol (like HTTP but for WebSockets).
// SockJS is a JavaScript library that falls back to HTTP long-polling if WebSockets aren't supported.
//
// How it works:
//   1. Browser connects to /ws via SockJS
//   2. Browser sends STOMP CONNECT frame with JWT in the Authorization header
//   3. Our ChannelInterceptor validates the JWT and sets the session principal
//   4. Browser subscribes to /user/queue/notifications
//   5. Server pushes messages using SimpMessagingTemplate.convertAndSendToUser(email, ...)
//   6. STOMP routes to the right user's connection based on their principal (email)
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired private JwtService jwtService;
    @Autowired private UserDetailsServiceImpl userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The URL the frontend connects to, with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/user" prefix routes messages to a specific user
        // "/topic" would broadcast to all subscribers (not used here)
        registry.enableSimpleBroker("/user");

        // Messages from the browser to the server are prefixed with "/app"
        registry.setApplicationDestinationPrefixes("/app");

        // Required for convertAndSendToUser() to route correctly
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // This interceptor runs when the browser sends a STOMP frame (CONNECT, SUBSCRIBE, etc.)
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Only validate JWT on the initial CONNECT frame
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            String email = jwtService.extractEmail(token);
                            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                            if (jwtService.isTokenValid(token, userDetails)) {
                                // Set the user principal — Spring uses this for convertAndSendToUser()
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                accessor.setUser(auth);
                            }
                        } catch (Exception ignored) {
                            // Invalid token — connection proceeds but no principal is set
                        }
                    }
                }

                return message;
            }
        });
    }
}
