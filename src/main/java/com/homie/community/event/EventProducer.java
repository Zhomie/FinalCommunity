package com.homie.community.event;

import com.alibaba.fastjson.JSONObject;
import com.homie.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
       //生产者需要调用kafkaTemplate去发消息
        @Autowired
        private KafkaTemplate kafkaTemplate;

        //处理事件的方法
        public void fireEvent(Event event){
                //发布事件到指定的主题
                kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));

        }

}
