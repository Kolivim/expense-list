package com.example.test3.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.test3.expenseList.Expense;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class FileExportUtil  {

    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Экспортирует список расходов в текстовый файл
     * @param context контекст приложения
     * @param expenseList список расходов для экспорта
     * @return true если экспорт успешен, false в противном случае
     */
    public static boolean exportExpensesToTxt(Context context, ArrayList<Expense> expenseList) {

        /** Проверяем доступность внешнего хранилища */
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "Внешнее хранилище недоступно", Toast.LENGTH_SHORT).show();
            return false;
        }

        /** Создаём имя файла с текущей датой и временем */
        String fileName = "expenses_" +
                java.time.ZonedDateTime.now().format(FILE_NAME_FORMAT) +
                ".txt";

        /** Получаем директорию для загрузок (общедоступная папка) */
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        /** Для Android 10 и выше рекомендуется использовать другой подход
            Но для простоты оставляем так */

        /** Создаём файл */
        File file = new File(downloadsDir, fileName);

        try {

            writeExpensesToFile(file, expenseList);

            /** Показываем сообщение об успехе */
            Toast.makeText(context,
                    "Файл сохранён: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "Ошибка при сохранении файла: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    /** Проверяет, доступно ли внешнее хранилище для записи */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /** Записывает данные о расходах в файл */
    private static void writeExpensesToFile(File file, ArrayList<Expense> expenseList) throws IOException {

        try (FileWriter writer = new FileWriter(file)) {

            /** Заголовок файла */
            writer.write("ОТЧЁТ ПО РАСХОДАМ\n");
            writer.write("Дата создания: " +
                    java.time.ZonedDateTime.now().format(Util.dateFormatterInsert) + "\n");
            writer.write("========================================\n\n");

            /** Проверяем, есть ли данные */
            if (expenseList == null || expenseList.isEmpty()) {
                writer.write("Нет данных о расходах.\n");
                return;
            }

            double totalAll = 0.0;
            int expenseCount = 0;
            int paymentCount = 0;


            /** Записываем каждую запись расхода */
            for (Expense expense : expenseList) {
                expenseCount++;

                writer.write("Запись #" + expenseCount + "\n");
                writer.write("Дата: " + expense.getDateTimeString() + "\n");
                writer.write("Название: " + expense.getName() + "\n");

                if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
                    writer.write("Описание: " + expense.getDescription() + "\n");
                }

                writer.write("Платежи:\n");

                if (expense.getExpenseList() != null && !expense.getExpenseList().isEmpty()) {
                    int paymentIndex = 1;
                    for (Double payment : expense.getExpenseList()) {
                        writer.write(String.format("  %d. %.2f руб.\n", paymentIndex++, payment));
                        paymentCount++;
                    }

                    double expenseTotal = expense.getExpenseListTotalAmount();
                    writer.write(String.format("  Итого по записи: %.2f руб.\n", expenseTotal));
                    totalAll += expenseTotal;

                } else {
                    writer.write("  Нет платежей\n");
                }

                writer.write("----------------------------------------\n\n");
            }


            /** Итоговая статистика */
            writer.write("========================================\n");
            writer.write("ИТОГОВАЯ СТАТИСТИКА:\n");
            writer.write(String.format("Всего записей: %d\n", expenseCount));
            writer.write(String.format("Всего платежей: %d\n", paymentCount));
            writer.write(String.format("Общая сумма: %.2f руб.\n", totalAll));
            writer.write("========================================\n");
        }

    }


    /**
     * Альтернативный метод для сохранения в приватную директорию приложения
     * (не требует разрешений, но файл будет виден только данному приложению)
     */
    public static boolean exportToPrivateStorage(Context context, ArrayList<Expense> expenseList) {

        String fileName = "expenses_" +
                java.time.ZonedDateTime.now().format(FILE_NAME_FORMAT) +
                ".txt";

        /** Получаем приватную директорию приложения */
        File privateDir = context.getExternalFilesDir(null);

        if (privateDir == null) {
            privateDir = context.getFilesDir(); /** Внутренняя память */
        }

        File file = new File(privateDir, fileName);

        try {
            writeExpensesToFile(file, expenseList);

            Toast.makeText(context,
                    "Файл сохранён в приватной директории: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "Ошибка при сохранении файла: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    /** Экспорт в JSON: */
    /**
     * Экспортирует список расходов в JSON файл
     * @param context контекст приложения
     * @param expenseList список расходов для экспорта
     * @return true если экспорт успешен, false в противном случае
     */
    public static boolean exportExpensesToJson(Context context, ArrayList<Expense> expenseList) {

        /** Проверяем доступность внешнего хранилища */
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "Внешнее хранилище недоступно", Toast.LENGTH_SHORT).show();
            return false;
        }

        /** Создаём имя файла с текущей датой и временем */
        String fileName = "expenses_" +
                java.time.ZonedDateTime.now().format(FILE_NAME_FORMAT) +
                ".json";

        /** Получаем директорию для загрузок */
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        try {

            /** Записываем данные в JSON файл */
            writeExpensesToJsonFile(file, expenseList);

            Toast.makeText(context,
                    "JSON файл сохранён: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "Ошибка при сохранении JSON файла: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    /** Записывает данные о расходах в JSON файл */
    private static void writeExpensesToJsonFile(File file, ArrayList<Expense> expenseList) throws IOException {

        try (FileWriter writer = new FileWriter(file)) {

            /** Начинаем JSON массив */
            writer.write("[\n");

            if (expenseList != null && !expenseList.isEmpty()) {

                for (int i = 0; i < expenseList.size(); i++) {
                    Expense expense = expenseList.get(i);


                    /** Начинаем объект расхода */
                    writer.write("  {\n");
                    writer.write("    \"id\": " + (expense.getId() != null ? expense.getId() : "null") + ",\n");
                    writer.write("    \"name\": \"" + escapeJson(expense.getName()) + "\",\n");

                    if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
                        writer.write("    \"description\": \"" + escapeJson(expense.getDescription()) + "\",\n");
                    } else {
                        writer.write("    \"description\": null,\n");
                    }

                    writer.write("    \"dateTime\": \"" + expense.getDateTime().format(Util.dateFormatterInsert) + "\",\n");
                    writer.write("    \"isDeleted\": " + expense.isDeleted() + ",\n");

                    if (expense.getRowColor() != null) {
                        writer.write("    \"rowColor\": " + expense.getRowColor() + ",\n");
                    } else {
                        writer.write("    \"rowColor\": null,\n");
                    }


                    /** Записываем платежи */
                    writer.write("    \"payments\": [\n");

                    if (expense.getExpenseList() != null && !expense.getExpenseList().isEmpty()) {
                        for (int j = 0; j < expense.getExpenseList().size(); j++) {
                            Double payment = expense.getExpenseList().get(j);
                            writer.write("      " + payment);
                            if (j < expense.getExpenseList().size() - 1) {
                                writer.write(",");
                            }
                            writer.write("\n");
                        }
                    }


                    writer.write("    ],\n");

                    /** Добавляем итоговую сумму для удобства */
                    writer.write("    \"totalAmount\": " + expense.getExpenseListTotalAmount() + "\n");

                    /** Закрываем объект расхода */
                    writer.write("  }");

                    /** Добавляем запятую, если это не последний элемент */
                    if (i < expenseList.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }
            }

            /** Закрываем JSON массив */
            writer.write("]\n");

        }
    }


    /** Экранирует специальные символы для JSON */
    private static String escapeJson(String s) {
        if (s == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }


    /** Альтернативный метод для сохранения JSON в приватную директорию */
    public static boolean exportJsonToPrivateStorage(Context context, ArrayList<Expense> expenseList) {

        String fileName = "expenses_" +
                java.time.ZonedDateTime.now().format(FILE_NAME_FORMAT) +
                ".json";

        File privateDir = context.getExternalFilesDir(null);

        if (privateDir == null) {
            privateDir = context.getFilesDir();
        }

        File file = new File(privateDir, fileName);

        try {
            writeExpensesToJsonFile(file, expenseList);

            Toast.makeText(context,
                    "JSON файл сохранён в приватной директории: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "Ошибка при сохранении JSON файла: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /** !Экспорт в JSON */

}
