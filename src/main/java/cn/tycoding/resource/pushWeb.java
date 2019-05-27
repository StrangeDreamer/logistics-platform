package cn.tycoding.resource;

import cn.tycoding.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class pushWeb {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 群发消息
     * @param message
     * @return
     */
    @GetMapping(value = "/pushWeb")
    public Map<String, Object> pushVideoListToWeb(String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            WebSocketServer.sendInfo("有新客户呼入,message:" + message);
            result.put("operationResult", true);
        } catch (Exception e) {
            result.put("operationResult", true);
        }
        return result;
    }
}
