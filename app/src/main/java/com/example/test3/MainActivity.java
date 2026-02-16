package com.example.test3;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.expenseList.Expense;
import com.example.test3.service.ExpanseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<Expense> arrayAdapter;
    ListView listView;
    ArrayList<Expense> selectedUsers = new ArrayList<Expense>();

    ExpanseService expanseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expanseService = new ExpanseService(getBaseContext());

        listView = findViewById(R.id.expenseList);


        ArrayList<Expense> allExpenseListDb = expanseService.getExpanseList();

        arrayAdapter = new ArrayAdapter<Expense>(this, android.R.layout.simple_list_item_1, allExpenseListDb);

        listView.setAdapter(arrayAdapter);


        /** Обработка установки и снятия отметки в списке : */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                /** Получаем нажатый элемент */
                Expense expense = arrayAdapter.getItem(position);

                if(listView.isItemChecked(position)) {
                    selectedUsers.add(expense);
                } else {
                    selectedUsers.remove(expense);
                }

            }

        });

    }


    public void add(View view){

        /** Вычитываем введённые пользователем данные: */
        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
        String expenseName = expenseNameEditText.getText().toString();

        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);

        Double expense = null;
        if(expenseEditText.getText() != null && !expenseEditText.getText().isEmpty())
            expense = Double.parseDouble(expenseEditText.getText().toString());

        EditText expenseDateEditText = findViewById(R.id.editTextDate);
        String expenseDateTimeString = expenseDateEditText.getText().toString();

        String expenseDescription = null;


        /** Создаём новую запись: */
        if(!expenseName.isEmpty()){

            Expense newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);

            expanseService.insertExpense(newExpense);

            updateAdapter();


            cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
            arrayAdapter.notifyDataSetChanged();

        }

    }


    public void remove(View view){

        /** Получаем и удаляем выделенные элементы */
        for(int i = 0; i < selectedUsers.size(); i++) expanseService.removeExpense(selectedUsers.get(i));


        /** Снимаем все ранее установленные отметки */
        listView.clearChoices();

        /** Очищаем массив выбраных объектов */
        selectedUsers.clear();


        updateAdapter();

        arrayAdapter.notifyDataSetChanged();
    }


    public void updateAdapter() {

        ArrayList<Expense> allExpenseList = expanseService.getExpanseList();
        arrayAdapter = new ArrayAdapter<Expense>(this, android.R.layout.simple_list_item_1, allExpenseList);
        listView.setAdapter(arrayAdapter);

    }


    public Expense getNewExpense(String expenseName, Double expense, String expenseDateTimeString, String expenseDescription) {

        if(expenseName != null && !expenseName.isEmpty()) {

            Expense newExpense = new Expense(expenseName);

            if(expense != null && !expense.isNaN()) newExpense.addExpense(expense);

            if(expenseDateTimeString != null && !expenseDateTimeString.isEmpty()) {
                ZonedDateTime expenseZonedDateTime = getZoneDateTime(expenseDateTimeString);
                if(expenseZonedDateTime != null) newExpense.setDateTime(expenseZonedDateTime);
            }

            if(expenseDescription != null && !expenseDescription.isEmpty()) newExpense.setDescription(expenseDescription);

            return newExpense;
        }


        return null;
    }


    public ZonedDateTime getZoneDateTime(String dateString) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");

        Date date = null;
        ZonedDateTime zonedDateTime = null;

        try {

            date = formatter.parse(dateString);

            zonedDateTime = date.toInstant().atZone(ZonedDateTime.now().getZone());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return zonedDateTime;
    }


    public void cleanUserInput(EditText expenseNameEditText, EditText expenseEditText, EditText expenseDateEditText) {

        expenseNameEditText.setText("");
        expenseEditText.setText("");
        expenseDateEditText.setText("");

    }


    @Deprecated
    public void addOld(View view){

        /** Вычитываем введённые пользователем данные: */
        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
        String expenseName = expenseNameEditText.getText().toString();

        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);

        Double expense = null;
        if(expenseEditText.getText() != null && !expenseEditText.getText().isEmpty())
            expense = Double.parseDouble(expenseEditText.getText().toString());

        EditText expenseDateEditText = findViewById(R.id.editTextDate);
        String expenseDateTimeString = expenseDateEditText.getText().toString();

        String expenseDescription = null;

        /** Создаём новую запись: */
        if(!expenseName.isEmpty()){

            Expense newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);

            expanseService.insertExpense(newExpense);

            // Test :
            ArrayList<Expense> testList = expanseService.getExpanseList();
            arrayAdapter = new ArrayAdapter<Expense>(this, android.R.layout.simple_list_item_1 , testList);
            listView.setAdapter(arrayAdapter);
            // !Test

//            arrayAdapter.add(newExpense);
            cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
            arrayAdapter.notifyDataSetChanged();

        }

    }


    @Deprecated
    public void removeOld(View view){

        /** Получаем и удаляем выделенные элементы */
        for(int i = 0; i < selectedUsers.size(); i++) arrayAdapter.remove(selectedUsers.get(i));


        /** Снимаем все ранее установленные отметки */
        listView.clearChoices();

        /** Очищаем массив выбраных объектов */
        selectedUsers.clear();

        arrayAdapter.notifyDataSetChanged();
    }

}