package ru.itis.queuing_system;

public class Main {

    public static void main(String[] args) {
        var csvReader = new CsvReader();
        var dates = csvReader.readCsv();
        var task1 = new FirstTask();
        System.out.println("========== Первое задание ===========");
        task1.avgAndDispersion(dates);
        var task2 = new SecondTask();
        System.out.println("========== Второе задание ===========");
        task2.avgAndDispersion(dates);
        var task3 = new ThirdTask();
        System.out.println("========== Третье задание ===========");
        task3.avgAndDispersion(dates);
        var task4 = new FourthTask();
        System.out.println("========== Четвертое задание ===========");
        task4.avgAndDispersion(dates);
        var task5 = new FifthTask();
        System.out.println("========== Пятое задание ===========");
        task5.avgAndDispersion(dates);
        var task6 = new SixthTask();
        System.out.println("========== Шестое задание ===========");
        task6.avgAndDispersion(dates);
    }
}
