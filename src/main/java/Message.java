import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

@Getter
@Setter
@ToString
public class Message {
    MessageHeader messageHeader;
    String body;

    public Message(){
        messageHeader=new MessageHeader();
    }

}
