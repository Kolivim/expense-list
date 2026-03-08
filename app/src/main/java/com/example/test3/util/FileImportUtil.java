package com.example.test3.util;

import android.content.Context;
import android.widget.Toast;

import com.example.test3.expenseList.Expense;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class FileImportUtil {


    /**
     * Читает расходы из JSON файла
     * @param file JSON файл для импорта
     * @return список расходов
     * @throws IOException если ошибка чтения файла
     * @throws JSONException если ошибка парсинга JSON
     */
    public static ArrayList<Expense> importExpensesFromJson(File file) throws IOException, JSONException {

        ArrayList<Expense> expenseList = new ArrayList<>();


        /** Читаем весь файл в строку */
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        }

        /** Парсим JSON */
        JSONArray jsonArray = new JSONArray(jsonString.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);

            Long id = jsonObj.has("id") && !jsonObj.isNull("id") ? jsonObj.getLong("id") : null;
            String name = jsonObj.getString("name");
            String description = jsonObj.has("description") && !jsonObj.isNull("description") ?
                    jsonObj.getString("description") : null;

            String dateTimeStr = jsonObj.getString("dateTime");
            ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeStr, Util.dateFormatterInsert);

            boolean isDeleted = jsonObj.has("isDeleted") && jsonObj.getBoolean("isDeleted");
            Integer rowColor = jsonObj.has("rowColor") && !jsonObj.isNull("rowColor") ?
                    jsonObj.getInt("rowColor") : null;

            Expense expense = new Expense(id, name, description, dateTime, isDeleted, rowColor);

            /** Читаем платежи */
            if (jsonObj.has("payments")) {
                JSONArray paymentsArray = jsonObj.getJSONArray("payments");
                for (int j = 0; j < paymentsArray.length(); j++) {
                    expense.addPayment(paymentsArray.getDouble(j));
                }
            }

            expenseList.add(expense);
        }

        return expenseList;
    }


    /**
     * Импортирует расходы из JSON файла и сохраняет в базу данных
     * @param context контекст приложения
     * @param file JSON файл для импорта
     * @param expenseService сервис для работы с БД
     * @return количество импортированных записей
     */
    public static int importAndSaveToDatabase(
            Context context,
            File file,
            com.example.test3.service.ExpenseService expenseService) {

        try {

            ArrayList<Expense> importedExpenses = importExpensesFromJson(file);

            int successCount = 0;
            for (Expense expense : importedExpenses) {

                /** Сбрасываем ID, чтобы создались новые записи в БД */
                expense.setId(null);

                /** Вставляем в базу данных */
                if (expenseService.insertExpense(expense)) {
                    successCount++;
                }

            }

            Toast.makeText(context,
                    "Импортировано записей: " + successCount + " из " + importedExpenses.size(),
                    Toast.LENGTH_LONG).show();

            return successCount;

        } catch (IOException | JSONException e) {

            e.printStackTrace();
            Toast.makeText(context,
                    "Ошибка при импорте: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();

            return 0;

        }

    }


}
