package lib.message;

import java.net.InetAddress;

public class NackMessage implements Message {
    private InetAddress senderId;
    private InetAddress targetId;
    private int requestedMessage;

    public NackMessage(InetAddress senderId, InetAddress targetId, int requestedMessage) {
        this.senderId = senderId;
        this.targetId = targetId;
        this.requestedMessage = requestedMessage;
    }

    public char getType() { return 'N'; }

    public InetAddress getSenderId() {
        return senderId;
    }

    public int getRequestedMessage() {
        return requestedMessage;
    }

    public InetAddress getTargetId() {
        return targetId;
    }
}