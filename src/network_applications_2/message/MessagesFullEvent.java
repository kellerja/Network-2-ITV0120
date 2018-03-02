package network_applications_2.message;

import java.util.List;

public interface MessagesFullEvent {
    void propagateMessages(List<Message> messages);
}
