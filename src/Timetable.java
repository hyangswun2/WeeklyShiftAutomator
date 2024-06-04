import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Timetable {
    public Map<LocalDateTime, Set<String>>[] days;

    @SuppressWarnings("unchecked")
    public Timetable() {
        days = new HashMap[7];
        for (int i = 0; i < 7; i++) {
            days[i] = new HashMap<>();
        }
    }
}