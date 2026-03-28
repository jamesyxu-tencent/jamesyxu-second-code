package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ChatFeedback;
import com.example.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface ChatFeedbackMapper extends BaseMapper<ChatFeedback> {

    List<ChatMessage> selectFavoriteMessages();

}