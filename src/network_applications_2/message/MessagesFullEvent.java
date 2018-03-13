package network_applications_2.message;

import java.util.Set;

public interface MessagesFullEvent {
    void propagateMessages(Set<Message> messages);
}
