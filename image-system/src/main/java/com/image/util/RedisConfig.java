package com.image.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig
 *
 * @Author litianwei
 * @Date 2024/1/25
 **/
@Configuration
public class RedisConfig {

    // 这个redisTemplate好像有点问题，建议直接使用StringRedisTemplate
    // @Bean
    // public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
    //     RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    //     redisTemplate.setConnectionFactory(lettuceConnectionFactory);
    //
    //     // 使用Jackson2JsonRedisSerialize 替换默认序列化
    //     Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
    //
    //     ObjectMapper objectMapper = new ObjectMapper();
    //     objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    //     objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    //
    //     jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
    //
    //     // 设置value的序列化规则和 key的序列化规则
    //     redisTemplate.setKeySerializer(new StringRedisSerializer());
    //     //jackson2JsonRedisSerializer就是JSON序列号规则，
    //     redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
    //
    //     // Hash类型 key序列器
    //     redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    //     // Hash类型 value序列器
    //     redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    //
    //     redisTemplate.afterPropertiesSet();
    //     return redisTemplate;
    // }

}
