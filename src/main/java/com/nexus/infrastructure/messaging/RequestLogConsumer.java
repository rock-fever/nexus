package com.nexus.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nexus.domain.model.RequestLog;
import com.nexus.domain.repository.IRequestLogRepository;

@Component
public class RequestLogConsumer {

    private final IRequestLogRepository requestLogRepository;

    public RequestLogConsumer(IRequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @KafkaListener(topics = "request.logged", groupId = "nexus-group")
    public void onRequestLogged(RequestLoggedEvent event) {
        requestLogRepository.save(new RequestLog(
                event.tenantId(),
                event.routeId(),
                event.method(),
                event.path(),
                event.statusCode(),
                event.latencyMs(),
                event.timestamp()));
    }
}
