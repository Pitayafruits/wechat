package com.pitayafruits.service;


import com.pitayafruits.pojo.netty.ChatMsg;
import com.pitayafruits.utils.PagedGridResult;

public interface ChatMessageService {

    /**
     * 保存聊天信息
     * @param chatMsg
     */
    public void saveMsg(ChatMsg chatMsg);

    /**
     * 查询聊天信息列表
     * @param senderId
     * @param receiverId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryChatMsgList(String senderId,
                                            String receiverId,
                                            Integer page,
                                            Integer pageSize);

    /**
     * 标记语音聊天信息的签收已读
     * @param msgId
     */
    public void updateMsgSignRead(String msgId);

}
