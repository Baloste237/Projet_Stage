package com.example.backend.scan.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncExecutorService {

    @Async("scanTaskExecutor")
    public void execute(Runnable task) {
        task.run();
    }
}
