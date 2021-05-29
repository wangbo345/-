package com.changgou.search.listener;

import com.changgou.search.service.EsManagerService;
import com.netflix.discovery.converters.Auto;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/25
 * @description :
 */
@Component
@RabbitListener(queues = "search_delete_queue")
public class SpuDeleteListener {
    @Autowired
    private EsManagerService esManagerService;
    @RabbitHandler
    public void deleteSpuFromES(String id) {
        esManagerService.deleteSpuFromES(id);
    }
}
