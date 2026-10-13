package ru.edu.pr05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SelfCheck {
    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate END = LocalDate.of(2026, 10, 5);
    private static int passed = 0;
    private static int count = 0;

    public static void main(String[] args) {
        checkCreate();
        checkSubmitApprove();
        checkReject();
        checkCancelComplete();
        checkService();
        checkVariant();
        System.out.println("Unit: " + passed + "/" + count + " passed");
        // свой файл: java -cp out ru.edu.pr05.SelfCheck data/my.csv
        runFile(args.length > 0 ? args[0] : "data/sample.csv");
    }

    private static void checkCreate() {
        // корректная заявка создается в статусе DRAFT
        check("create draft", newTrip().getStatus() == TripStatus.DRAFT);
        // граница дат: начало и конец в один день можно, конец раньше начала нельзя
        check("same day", tryCreate("T", END, END));
        check("end before start", !tryCreate("T", END, START));
        // пустой id и пустые даты
        check("empty id", !tryCreate(" ", START, END));
        check("null date", !tryCreate("T", null, END));
    }

    private static void checkSubmitApprove() {
        // отправить без обязательных данных нельзя
        BusinessTrip trip = new BusinessTrip("T-1", START, END);
        check("submit without details", !tryAction(trip, "SUBMIT"));
        trip.fillDetails(new Employee("E1", "Иванов Иван"), "Казань");
        trip.submit();
        check("submit", trip.getStatus() == TripStatus.SUBMITTED);
        // повторно отправить и менять данные после отправки нельзя
        check("submit twice", !tryAction(trip, "SUBMIT"));
        check("fill after submit", !tryFill(trip));
        trip.approve();
        check("approve", trip.getStatus() == TripStatus.APPROVED);
        // согласовать черновик нельзя
        check("approve draft", !tryAction(newTrip(), "APPROVE"));
    }

    private static void checkReject() {
        int min = BusinessTrip.MIN_REASON_LENGTH;
        // причина ровно минимальной длины подходит, на 1 символ короче нет
        BusinessTrip trip = submittedTrip();
        check("reject short reason", !tryAction(trip, "REJECT:" + "я".repeat(min - 1)));
        check("status kept", trip.getStatus() == TripStatus.SUBMITTED);
        trip.reject("я".repeat(min));
        check("reject min reason", trip.getStatus() == TripStatus.REJECTED && trip.getRejectionReason().length() == min);
        // пустая причина и отклонение черновика
        check("reject empty", !tryAction(submittedTrip(), "REJECT:"));
        check("reject draft", !tryAction(newTrip(), "REJECT:Нет денег в бюджете отдела на поездку"));
    }

    private static void checkCancelComplete() {
        // отмена из DRAFT, SUBMITTED и APPROVED
        check("cancel draft", tryAction(newTrip(), "CANCEL"));
        check("cancel submitted", tryAction(submittedTrip(), "CANCEL"));
        BusinessTrip approved = submittedTrip();
        approved.approve();
        // завершить в последний день нельзя, на следующий день можно
        check("complete on end day", !tryAction(approved, "COMPLETE:" + END));
        approved.complete(END.plusDays(1));
        check("complete next day", approved.getStatus() == TripStatus.COMPLETED);
        // после COMPLETED отменить нельзя, отклоненную тоже
        check("cancel completed", !tryAction(approved, "CANCEL"));
        BusinessTrip rejected = submittedTrip();
        rejected.reject("Поездка не согласована с руководителем");
        check("cancel rejected", !tryAction(rejected, "CANCEL"));
        check("complete submitted", !tryAction(submittedTrip(), "COMPLETE:" + END.plusDays(1)));
    }

    private static void checkService() {
        BusinessTripService service = new BusinessTripService();
        service.create("A", START, END);
        BusinessTrip b = service.create("B", START, END);
        b.fillDetails(new Employee("E1", "Иванов Иван"), "Казань");
        b.submit();
        check("findByStatus", service.findByStatus(TripStatus.DRAFT).size() == 1 && service.findByStatus(TripStatus.SUBMITTED).size() == 1);
        check("duplicate id", !tryServiceCreate(service, "A"));
    }

    private static void checkVariant() {
        // вариант 4: 24 символа мало, 25 достаточно
        check("variant 24 chars", !tryAction(submittedTrip(), "REJECT:" + "а".repeat(24)));
        check("variant 25 chars", tryAction(submittedTrip(), "REJECT:" + "а".repeat(25)));
        // причина на 20 символов раньше подходила, теперь нет
        check("variant old reason", !tryAction(submittedTrip(), "REJECT:Нет денег на поездку"));
        // пробелы по краям не считаются
        check("variant spaces", !tryAction(submittedTrip(), "REJECT:   " + "а".repeat(24) + "   "));
    }

    // ---------- вспомогательные методы ----------

    private static BusinessTrip newTrip() {
        return new BusinessTrip("T", START, END);
    }

    private static BusinessTrip submittedTrip() {
        BusinessTrip trip = newTrip();
        trip.fillDetails(new Employee("E1", "Иванов Иван"), "Казань");
        trip.submit();
        return trip;
    }

    // true - действие выполнилось без исключения
    private static boolean tryAction(BusinessTrip trip, String action) {
        try {
            apply(trip, action);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return false;
        }
    }

    private static boolean tryCreate(String id, LocalDate start, LocalDate end) {
        try {
            new BusinessTrip(id, start, end);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean tryFill(BusinessTrip trip) {
        try {
            trip.fillDetails(new Employee("E2", "Петров Петр"), "Сочи");
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return false;
        }
    }

    private static boolean tryServiceCreate(BusinessTripService service, String id) {
        try {
            service.create(id, START, END);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void check(String name, boolean ok) {
        count++;
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            System.out.println("FAIL " + name);
        }
    }

    // ---------- заявки из файла id;start;end;action ----------

    private static void runFile(String fileName) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println("Не удалось прочитать " + fileName);
            return;
        }
        System.out.println();
        System.out.println("Заявки из файла " + fileName + ":");
        BusinessTripService service = new BusinessTripService();
        for (int i = 1; i < lines.size(); i++) {   // первая строка - заголовок
            if (!lines.get(i).isEmpty()) {
                System.out.println(processLine(service, lines.get(i)));
            }
        }
    }

    // действия идут через >, у REJECT и COMPLETE параметр после двоеточия
    private static String processLine(BusinessTripService service, String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            return line + " -> ошибка: нужно 4 значения через ;";
        }
        BusinessTrip trip;
        try {
            trip = service.create(parts[0], LocalDate.parse(parts[1]), LocalDate.parse(parts[2]));
            // в файле нет сотрудника и города, для проверки переходов подставляются тестовые
            trip.fillDetails(new Employee("E1", "Тестовый сотрудник"), "Москва");
        } catch (DateTimeParseException e) {
            return parts[0] + " -> ошибка создания: неверная дата, нужен формат ГГГГ-ММ-ДД";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return parts[0] + " -> ошибка создания: " + e.getMessage();
        }
        for (String action : parts[3].split(">")) {
            try {
                apply(trip, action);
            } catch (DateTimeParseException e) {
                return parts[0] + " -> " + trip.getStatus() + ", шаг " + action + " отклонен: неверная дата";
            } catch (IllegalArgumentException | IllegalStateException e) {
                return parts[0] + " -> " + trip.getStatus() + ", шаг " + action + " отклонен: " + e.getMessage();
            }
        }
        return parts[0] + " -> " + trip.getStatus();
    }

    private static void apply(BusinessTrip trip, String action) {
        String name = action.contains(":") ? action.substring(0, action.indexOf(':')) : action;
        String param = action.contains(":") ? action.substring(action.indexOf(':') + 1) : "";
        switch (name) {
            case "SUBMIT" -> trip.submit();
            case "APPROVE" -> trip.approve();
            case "REJECT" -> trip.reject(param);
            case "CANCEL" -> trip.cancel();
            case "COMPLETE" -> trip.complete(LocalDate.parse(param));
            default -> throw new IllegalArgumentException("неизвестное действие " + name);
        }
    }
}
