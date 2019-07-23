package cn.tycoding.resource;

import cn.tycoding.websocket.WebSocketServer;
import cn.tycoding.websocket.WebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController

public class SendMsg {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WebSocketTest webSocketTest;

    public SendMsg(WebSocketTest webSocketTest) {
        this.webSocketTest = webSocketTest;
    }

    /**
     * 群发消息
     * @param message
     * @return
     */
    @GetMapping(value = "/qunfa")
    public Map<String, Object> qunFa(String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            WebSocketServer.sendInfo("有新客户呼入,message:" + message);
            result.put("operationResult", true);
        } catch (Exception e) {
            result.put("operationResult", false);
        }
        return result;
    }

    @GetMapping(value = "/qunfa2")
    public Map<String, Object> qunFa2(String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            logger.info("使用websockettest群发消息");
            webSocketTest.sendAll(message);
            result.put("群发是否成功", true);
        } catch (Exception e) {
            result.put("群发是否成功", false);
        }
        return result;
    }

    /**
     * websocket的单发方法
     * @param toUser
     * @param msg
     * @return
     */

    @GetMapping(value = "/danfa")
    public Map<String,Object> danFa(String toUser,String msg){
        Map<String,Object> res=new HashMap<>();
        try {
            webSocketTest.sendToUser2(toUser,msg);
            logger.info("WebSocketTest  单发消息"+msg+"给"+toUser);
            res.put("单发是否成功",true);
        }catch (Exception e){
            res.put("单发是否成功",false);
        }
        return res;
    }
}
