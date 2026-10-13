package ru.edu.pr05;

public record Employee(String id, String fullName) {
    public Employee {
        if (id == null || id.isBlank() || fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Сотрудник: пустой id или ФИО");
        }
    }
}
