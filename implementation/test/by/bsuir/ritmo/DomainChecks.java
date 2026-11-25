package by.bsuir.ritmo;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class DomainChecks {
    private static int checks;
    private static void check(boolean ok) {
        checks++;
        if (!ok) throw new AssertionError("Check " + checks);
    }
    private static void error(String code, Runnable task) {
        try { task.run(); throw new AssertionError("Expected " + code); }
        catch (HabitService.DomainError e) { check(e.code().equals(code)); }
    }
    public static void main(String[] args) throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 16);
        HabitService s = new HabitService(Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC));
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        UUID h = s.create(a, "  Читать  ");
        check(s.snapshot(a).habits().getFirst().name().equals("Читать"));
        error("INVALID_NAME", () -> s.create(a, " "));
        error("INVALID_NAME", () -> s.create(a, "x".repeat(81)));
        UUID emoji = s.create(a, "😀".repeat(80));
        check(s.snapshot(a).habits().size() == 2);
        s.delete(a, emoji);
        error("NOT_FOUND", () -> s.setToday(b, h, date, true));
        error("UNAUTHENTICATED", () -> s.snapshot(null));
        error("DAY_CHANGED", () -> s.setToday(a, h, date.minusDays(1), true));
        check(s.snapshot(a).totalPoints() == 0);
        check(s.setToday(a, h, date, true).totalPoints() == 10);
        check(s.setToday(a, h, date, true).totalPoints() == 10);
        s.rename(a, h, "Книга");
        check(s.snapshot(a).habits().getFirst().streak() == 1);
        check(s.setToday(a, h, date, false).totalPoints() == 0);
        check(s.setToday(a, h, date, false).totalPoints() == 0);
        check(HabitService.streak(Set.of(date, date.minusDays(1)), date) == 2);
        check(HabitService.streak(Set.of(date.minusDays(1), date.minusDays(2)), date) == 2);
        check(HabitService.streak(Set.of(date.minusDays(2)), date) == 0);
        Set<LocalDate> seven = new HashSet<>();
        for (int i=0;i<7;i++) seven.add(date.minusDays(i));
        check(HabitService.streak(seven, date) == 7);
        seven.remove(date);
        check(HabitService.streak(seven, date) == 6);
        // Concurrent duplicates must not accumulate points.
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            List<Callable<Void>> work = new ArrayList<>();
            for (int i=0;i<100;i++) work.add(() -> { s.setToday(a, h, date, true); return null; });
            for (Future<Void> f : pool.invokeAll(work)) f.get();
        }
        check(s.snapshot(a).totalPoints() == 10);
        check(s.delete(a, h).totalPoints() == 0);
        error("NOT_FOUND", () -> s.setToday(a, h, date, true));
        // UTC 21:00 is the next calendar day in Minsk.
        HabitService midnight = new HabitService(Clock.fixed(Instant.parse("2026-11-16T21:00:00Z"), ZoneOffset.UTC));
        UUID next = midnight.create(a, "Тест");
        error("DAY_CHANGED", () -> midnight.setToday(a, next, date, true));
        check(midnight.setToday(a, next, date.plusDays(1), true).serverDate().equals(date.plusDays(1)));
        System.out.println("PASS: " + checks + " domain checks; concurrent PUT x100");
    }
}
