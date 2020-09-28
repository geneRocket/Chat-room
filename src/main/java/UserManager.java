import javax.print.DocFlavor;
import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    ConcurrentHashMap<String, SelectionKey> online_users = new ConcurrentHashMap<>(16);
    ConcurrentHashMap<SelectionKey, String> record = new ConcurrentHashMap<>(16);

    boolean add_user(String user_name,SelectionKey key){
        if(online_users.contains(user_name))
            return false;
        record.put(key,user_name);
        online_users.put(user_name,key);
        return true;
    }

    void remove_user(String user_name){
        SelectionKey key=online_users.get(user_name);
        record.remove(key);
        online_users.remove(user_name);

    }

    void remove_user(SelectionKey key){
        String user_name=record.get(key);
        online_users.remove(user_name);
        record.remove(key);

    }


}
