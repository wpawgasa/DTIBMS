/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tdl.messaging;

/**
 *
 * @author Administrator
 */
public class TDLMessage {
    public String profileId;
    public String fromId;
    public String toId;
    public String msgId;
    public byte msgType;
    public byte[] msg;

    public TDLMessage(String profileId,String fromId, String toId, String msgId, 
            byte msgType, byte[] msg) {
        this.fromId = fromId;
        this.toId = toId;
        this.msgId = msgId;
        this.msgType = msgType;
        this.msg = msg;
        this.profileId = profileId;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public String getMsgId() {
        return msgId;
    }

    public byte getMsgType() {
        return msgType;
    }

    public byte[] getMsg() {
        return msg;
    }

    public String getProfileId() {
        return profileId;
    }
    
    
    
}
