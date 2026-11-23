package com.transformertwin.service;
import org.springframework.stereotype.Service; import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException; import java.util.concurrent.*;
@Service
public class RealtimeService {
 private final ConcurrentMap<Long,CopyOnWriteArrayList<SseEmitter>> clients=new ConcurrentHashMap<>();
 public SseEmitter subscribe(Long id){SseEmitter e=new SseEmitter(0L);clients.computeIfAbsent(id,k->new CopyOnWriteArrayList<>()).add(e);e.onCompletion(()->remove(id,e));e.onTimeout(()->remove(id,e));e.onError(x->remove(id,e));return e;}
 public void publish(Long id,Object payload){var list=clients.get(id);if(list==null)return;for(SseEmitter e:list){try{e.send(SseEmitter.event().name("dashboard").data(payload));}catch(IOException ex){remove(id,e);}}}
 private void remove(Long id,SseEmitter e){var l=clients.get(id);if(l!=null){l.remove(e);if(l.isEmpty())clients.remove(id,l);}}
}
