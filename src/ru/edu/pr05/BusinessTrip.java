package ru.edu.pr05;
import java.time.*;
public class BusinessTrip {
    private final String id; private final LocalDate start; private final LocalDate end; private TripStatus status=TripStatus.DRAFT; private String rejectionReason;
    public BusinessTrip(String id, LocalDate start, LocalDate end) {
        // TODO 1  Проверить даты и id
        this.id=id; this.start=start; this.end=end;
    }
    public void submit() { /* TODO 2 */ }
    public void approve() { /* TODO 3 */ }
    public void reject(String reason) { /* TODO 4 */ }
    public void cancel() { /* TODO 5 */ }
    public void complete(LocalDate today) { /* TODO 6 */ }
    public TripStatus getStatus() { return status; } public String getRejectionReason() { return rejectionReason; }
}
