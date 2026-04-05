package com.bntu.user.socket;

import com.bntu.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketServer implements ApplicationRunner {

    @Value("${tcp.port}")
    private int port;

    private final ProfileService profileService;
    private final AtomicInteger clientCount = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void run(ApplicationArguments args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            log.info("/\\ User сервер запущен на порту {}", port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serverSocket.accept();
                int count = clientCount.incrementAndGet();
                log.info("()_() Активных клиентов: {}", count);

                executor.submit(() -> {
                    try {
                        new ClientHandler(client, profileService).run();
                    } finally {
                        clientCount.decrementAndGet();
                    }
                });
            }
        }
    }
}