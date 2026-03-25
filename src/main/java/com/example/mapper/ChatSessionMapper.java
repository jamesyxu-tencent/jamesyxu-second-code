package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话Mapper接口
 * 继承BaseMapper，自动获得CRUD方法
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 查询所有会话，按最后消息时间倒序
     */
    @Select("SELECT * FROM chat_session where is_delete = 0 ORDER BY last_message_time DESC")
    List<ChatSession> selectAllOrderByLastMessageTime();

    /**
     * 查询最近10个会话
     */
    @Select("SELECT * FROM chat_session where is_delete = 0 ORDER BY last_message_time DESC LIMIT 10")
    List<ChatSession> selectTop10();

}