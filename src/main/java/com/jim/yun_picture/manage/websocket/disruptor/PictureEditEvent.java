package com.jim.yun_picture.manage.websocket.disruptor;

/**
 * @author Jim_Lam
 * @description 图片编辑事件
 */

import com.jim.yun_picture.entity.User;
import com.jim.yun_picture.manage.websocket.model.PictureEditRequestMessage;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class PictureEditEvent {
    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;
}