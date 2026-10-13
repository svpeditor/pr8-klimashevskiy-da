# Практическая работа №8. Инкапсуляция, enum и управление состоянием объекта

Климашевский Даниил Александрович, группа ЭФБО-07-25, вариант 4.

Система оформления командировок: заявка проходит статусы DRAFT, SUBMITTED, APPROVED,
REJECTED, CANCELLED и COMPLETED, каждый переход - отдельный метод с проверками.
По варианту 4 причина отклонения должна быть не короче 25 символов.

## Сборка и запуск

Нужна Java 21 или новее.

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr05.SelfCheck
```

Или скриптом: `./run_selfcheck.sh` (macOS/Linux), `run_selfcheck.bat` (Windows).

Сначала выполняются проверки, потом обрабатываются заявки из `data/sample.csv`
(формат `id;start;end;action`). В колонке action можно писать цепочку через `>`,
у REJECT и COMPLETE параметр после двоеточия:
`T-03;2026-10-01;2026-10-05;SUBMIT>APPROVE>COMPLETE:2026-10-06`.
Сотрудника и города в файле нет, для проверки переходов подставляются тестовые.
Можно передать свой файл: `java -cp out ru.edu.pr05.SelfCheck data/my.csv`.

Пакет называется `ru.edu.pr05`, как в исходном проекте.

## Что где лежит

- `src/ru/edu/pr05/BusinessTrip.java` - заявка на командировку и переходы состояний
- `src/ru/edu/pr05/BusinessTripService.java` - создание и поиск заявок
- `src/ru/edu/pr05/TripStatus.java`, `Employee.java` - статусы и сотрудник
- `src/ru/edu/pr05/SelfCheck.java` - проверки и обработка файла
- `diagrams/` - activity-диаграмма, диаграмма классов и диаграмма переходов (dot и png)
- `DECISIONS.md` - вариант, таблица переходов, два решения, граничные проверки, ответы
- `REPORT.docx` - отчет
