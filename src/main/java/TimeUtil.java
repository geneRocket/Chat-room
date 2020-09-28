import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static String get_date_string(long timestamp){
        Date date=new Date(timestamp);
        return simpleDateFormat.format(date);
    }
}
