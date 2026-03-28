package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询会话的所有消息（按时间正序）
     */
    List<ChatMessage> selectBySessionIdOrderByTime(@Param("sessionId") String sessionId);

    /**
     * 查询会话最近N条消息
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} and is_delete = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> selectRecentMessages(@Param("sessionId") String sessionId,
                                           @Param("limit") int limit);

    /**
     * 统计会话的消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} and is_delete = 0")
    long countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计会话中用户消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND role = 'user' and is_delete = 0")
    long countUserMessagesBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计会话中AI消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND role = 'assistant' and is_delete = 0")
    long countAssistantMessagesBySessionId(@Param("sessionId") String sessionId);
}