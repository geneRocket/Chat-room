import lombok.*;

@Getter
@Setter
@ToString
public class MessageHeader {
    String sender;
    MessageType Type;
    long timestamp;
}
