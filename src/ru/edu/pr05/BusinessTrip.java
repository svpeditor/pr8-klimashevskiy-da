package ru.edu.pr05;

import java.time.LocalDate;

public class BusinessTrip {
    // вариант 4: причина отклонения не короче 25 символов (было 10)
    public static final int MIN_REASON_LENGTH = 25;

    private final String id;
    private final LocalDate start;
    private final LocalDate end;
    private Employee employee;
    private String destination;
    private TripStatus status = TripStatus.DRAFT;
    private String rejectionReason;

    public BusinessTrip(String id, LocalDate start, LocalDate end) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Командировка: пустой id");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Командировка " + id + ": не указаны даты");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Командировка " + id + ": дата окончания " + end + " раньше даты начала " + start);
        }
        this.id = id;
        this.start = start;
        this.end = end;
    }

    // заполнить обязательные данные можно только в черновике
    public void fillDetails(Employee employee, String destination) {
        checkStatus("заполнить данные", TripStatus.DRAFT);
        if (employee == null || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Командировка " + id + ": нужны сотрудник и место назначения");
        }
        this.employee = employee;
        this.destination = destination;
    }

    public void submit() {
        checkStatus("отправить", TripStatus.DRAFT);
        if (employee == null || destination == null) {
            throw new IllegalStateException("Командировка " + id + ": не заполнены сотрудник и место назначения");
        }
        status = TripStatus.SUBMITTED;
    }

    public void approve() {
        checkStatus("согласовать", TripStatus.SUBMITTED);
        status = TripStatus.APPROVED;
    }

    public void reject(String reason) {
        checkStatus("отклонить", TripStatus.SUBMITTED);
        if (reason == null || reason.strip().length() < MIN_REASON_LENGTH) {
            throw new IllegalArgumentException("Причина отклонения должна быть не короче " + MIN_REASON_LENGTH + " символов");
        }
        rejectionReason = reason.strip();
        status = TripStatus.REJECTED;
    }

    public void cancel() {
        checkStatus("отменить", TripStatus.DRAFT, TripStatus.SUBMITTED, TripStatus.APPROVED);
        status = TripStatus.CANCELLED;
    }

    // завершить можно только после даты окончания командировки
    public void complete(LocalDate today) {
        checkStatus("завершить", TripStatus.APPROVED);
        if (today == null || !today.isAfter(end)) {
            throw new IllegalStateException("Командировка " + id + ": завершить можно только после " + end);
        }
        status = TripStatus.COMPLETED;
    }

    // переход разрешен, только если текущий статус есть в списке allowed
    private void checkStatus(String action, TripStatus... allowed) {
        for (TripStatus s : allowed) {
            if (status == s) {
                return;
            }
        }
        throw new IllegalStateException("Командировка " + id + ": нельзя " + action + " из статуса " + status);
    }

    public String getId() {
        return id;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getDestination() {
        return destination;
    }

    public TripStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
