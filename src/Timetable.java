import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Timetable {
    public Map<Integer, Map<LocalDateTime, Set<String>>> days;

    public Timetable() {
        days = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            days.put(i, new HashMap<>());
        }
    }

    public void addTimeRange(int dayIndex, LocalDateTime date, String timeRange) {
        days.get(dayIndex).computeIfAbsent(date, k -> new HashSet<>()).add(timeRange);
    }

    public Set<String> getTimeRanges(int dayIndex, LocalDateTime date) {
        return days.get(dayIndex).getOrDefault(date, new HashSet<>());
    }
}
