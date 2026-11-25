package by.bsuir.ritmo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/** Executable domain prototype. Memory storage is NOT the persistence layer of SDD. */
public final class HabitService {
    private static final ZoneId ZONE = ZoneId.of("Europe/Minsk");
    private final Clock clock;
    private final Map<UUID, Habit> habits = new LinkedHashMap<>();

    private static final class Habit {
        final UUID id = UUID.randomUUID();
        final UUID owner;
        String name;
        final Set<LocalDate> dates = new HashSet<>();
        Habit(UUID owner, String name) { this.owner = owner; this.name = name; }
    }
    public record HabitView(UUID id, String name, boolean completedToday, int streak) {}
    public record Snapshot(LocalDate serverDate, List<HabitView> habits, long totalPoints) {
        public Snapshot { habits = List.copyOf(habits); }
    }
    public static final class DomainError extends RuntimeException {
        private final String code;
        public DomainError(String code) { super(code); this.code = code; }
        public String code() { return code; }
    }
    public HabitService(Clock clock) { this.clock = Objects.requireNonNull(clock); }
    private LocalDate today() { return LocalDate.now(clock.withZone(ZONE)); }
    private static UUID requireOwner(UUID owner) {
        if (owner == null) throw new DomainError("UNAUTHENTICATED");
        return owner;
    }
    private Habit owned(UUID owner, UUID id) {
        requireOwner(owner);
        Habit h = habits.get(id);
        if (h == null || !h.owner.equals(owner)) throw new DomainError("NOT_FOUND");
        return h;
    }
    private static String validName(String raw) {
        if (raw == null) throw new DomainError("INVALID_NAME");
        String name = raw.trim();
        int n = name.codePointCount(0, name.length());
        if (n < 1 || n > 80) throw new DomainError("INVALID_NAME");
        return name;
    }
    public synchronized UUID create(UUID owner, String name) {
        Habit h = new Habit(requireOwner(owner), validName(name));
        habits.put(h.id, h);
        return h.id;
    }
    public synchronized void rename(UUID owner, UUID id, String name) {
        Habit h = owned(owner, id);
        h.name = validName(name);
    }
    public synchronized Snapshot setToday(UUID owner, UUID id, LocalDate expectedDate, boolean complete) {
        Habit h = owned(owner, id);
        LocalDate date = today(); // Exactly one Clock snapshot for validation and result.
        if (!date.equals(expectedDate)) throw new DomainError("DAY_CHANGED");
        if (complete) h.dates.add(date); else h.dates.remove(date);
        return snapshotAt(owner, date);
    }
    public synchronized Snapshot delete(UUID owner, UUID id) {
        Habit h = owned(owner, id);
        LocalDate date = today();
        habits.remove(h.id);
        return snapshotAt(owner, date);
    }
    public synchronized Snapshot snapshot(UUID owner) {
        requireOwner(owner);
        return snapshotAt(owner, today());
    }
    private Snapshot snapshotAt(UUID owner, LocalDate date) {
        List<HabitView> result = new ArrayList<>();
        long points = 0;
        for (Habit h : habits.values()) {
            if (!h.owner.equals(owner)) continue;
            result.add(new HabitView(h.id, h.name, h.dates.contains(date), streak(h.dates, date)));
            points += 10L * h.dates.size();
        }
        return new Snapshot(date, result, points);
    }
    static int streak(Set<LocalDate> dates, LocalDate today) {
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
        int count = 0;
        while (dates.contains(cursor)) { count++; cursor = cursor.minusDays(1); }
        return count;
    }
}
