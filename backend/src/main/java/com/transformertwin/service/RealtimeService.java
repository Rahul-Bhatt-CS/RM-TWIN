package com.transformertwin.service;

import com.transformertwin.api.dto.ApiDtos.LiveEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        return emitter;
    }

    public void publish(LiveEvent event) {
        for (SseEmitter emitter : emitters) {
            try { emitter.send(SseEmitter.event().name("twin-update").data(event)); }
            catch (IOException ex) { emitters.remove(emitter); }
        }
    }
}
