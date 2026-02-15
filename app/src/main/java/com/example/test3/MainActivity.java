package com.example.test3;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test3.expenseList.Expense;
import com.example.test3.listTest.UserAccount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends /* ListActivity */  AppCompatActivity {

    /** Реализация с extends AppCompatActivity v2 : */
    /*
    // набор данных, который свяжем со списком
    String[] countries = { "Бразилия", "Аргентина", "Колумбия", "Чили", "Уругвай"};
    */
    /** !Реализация с extends AppCompatActivity v2 : */


    /** Реализация с extends AppCompatActivity v3 : */
    /*
    ArrayList<String> users = new ArrayList<String>();
    ArrayList<String> selectedUsers = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView usersList;
    */
    /** !Реализация с extends AppCompatActivity v3 */


    /** Реализация с extends AppCompatActivity v4 : (UserAccount) */
    /*
    ArrayAdapter<UserAccount> arrayAdapter;
    ListView listView;
    ArrayList<UserAccount> selectedUsers = new ArrayList<UserAccount>();
    */
    /** !Реализация с extends AppCompatActivity v4 */


    /** Реализация с extends AppCompatActivity v4 : (Expense) */
    ArrayAdapter<Expense> arrayAdapter;
    ListView listView;
    ArrayList<Expense> selectedUsers = new ArrayList<Expense>();
    /** !Реализация с extends AppCompatActivity v4 */



    /** Реализация с extends AppCompatActivity v4 : (Expense) */
//    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.expenseList);


        ArrayList<Expense> expenseList = new ArrayList<Expense>() {{
            add(new Expense("Lunch"));
            add(new Expense("Coffee"));
            add(new Expense("Supper"));
        }};


        arrayAdapter = new ArrayAdapter<Expense>(this, android.R.layout.simple_list_item_1 , expenseList);

        listView.setAdapter(arrayAdapter);


        // Обработка установки и снятия отметки в списке :
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


//                CheckBox cb = (CheckBox) v.getTag();
//                cb.setChecked(true);

//                listView.setItemChecked(1, false);


                // Получаем нажатый элемент
                Expense expense = arrayAdapter.getItem(position);

                if(listView.isItemChecked(position)) {
                    selectedUsers.add(expense);
                } else {
                    selectedUsers.remove(expense);
                }


//                // снимаем все ранее установленные отметки (Убрать после написания Multiple реализации)
//                listView.clearChoices();

            }

        });
        // !Обработка установки и снятия отметки в списке

    }


    public void add(View view){

        /** Вычитываем введённые пользователем данные: */
        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
        String expenseName = expenseNameEditText.getText().toString();

        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);
        Double expense = Double.parseDouble(expenseEditText.getText().toString());

        EditText expenseDateEditText = findViewById(R.id.editTextDate);
        String expenseDateTimeString = expenseDateEditText.getText().toString();

        String expenseDescription = null;

        /** Создаём новую запись: */
        if(!expenseName.isEmpty()){

//            Expense newExpense = new Expense(expenseName);
            Expense newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);

            arrayAdapter.add(newExpense);
            cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
//            expenseNameEditText.setText("");
//            expenseEditText.setText("");
//            expenseDateEditText.setText("");
            arrayAdapter.notifyDataSetChanged();

        }

    }


    public void remove(View view){

        // Получаем и удаляем выделенные элементы
        for(int i=0; i< selectedUsers.size();i++){
            arrayAdapter.remove(selectedUsers.get(i));
        }

        // Снимаем все ранее установленные отметки
        listView.clearChoices();

        // Очищаем массив выбраных объектов
        selectedUsers.clear();

        arrayAdapter.notifyDataSetChanged();
    }
//    */


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
    /** !Реализация с extends AppCompatActivity v4 (Expense) */


    /** Реализация с extends ListActivity : */
    /*
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, item + " выбран", Toast.LENGTH_LONG).show();
    }
    */
    /** !Реализация с extends ListActivity */


    /** Реализация с extends AppCompatActivity v2 : */
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // получаем элемент ListView
        ListView countriesList = findViewById(R.id.expenseList);

        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, countries);

        // устанавливаем для списка адаптер
        countriesList.setAdapter(adapter);
    }
    */
    /** !Реализация с extends AppCompatActivity v2 */


    /** Реализация с extends AppCompatActivity v3 : (Рабочая v1) */
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // добавляем начальные элементы
        Collections.addAll(users, "Tom", "Bob", "Sam", "Alice");

        // получаем элемент ListView
        usersList = findViewById(R.id.expenseList);

        // создаем адаптер
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, users);

        // устанавливаем для списка адаптер
        usersList.setAdapter(adapter);

        // обработка установки и снятия отметки в списке
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                // получаем нажатый элемент
                String user = adapter.getItem(position);
                if(usersList.isItemChecked(position)) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }

            }

        });

    }


    public void add(View view){

        EditText userName = findViewById(R.id.editTextNameExpense);
        String user = userName.getText().toString();
        if(!user.isEmpty()){
            adapter.add(user);
            userName.setText("");
            adapter.notifyDataSetChanged();
        }
    }


    public void remove(View view){

        // получаем и удаляем выделенные элементы
        for(int i=0; i< selectedUsers.size();i++){
            adapter.remove(selectedUsers.get(i));
        }

        // снимаем все ранее установленные отметки
        usersList.clearChoices();

        // очищаем массив выбраных объектов
        selectedUsers.clear();

        adapter.notifyDataSetChanged();
    }
    */
    /** !Реализация с extends AppCompatActivity v3 */


    /** Реализация с extends AppCompatActivity v4 : (UserAccount) */
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.expenseList);


        UserAccount tom = new UserAccount("Tom","admin");
        UserAccount jerry = new UserAccount("Jerry","user");
        UserAccount donald = new UserAccount("Donald","guest", false);

//        UserAccount[] users = new UserAccount[]{tom,jerry, donald};
        ArrayList<UserAccount> users = new ArrayList<UserAccount>() {{ add(tom); add(jerry); add(donald); }};


        // android.R.layout.simple_list_item_1 is a constant predefined layout of Android.
        // used to create a ListView with simple ListItem (Only one TextView).

//        ArrayAdapter<UserAccount> arrayAdapter = new ArrayAdapter<UserAccount>(this, android.R.layout.simple_list_item_1 , users);
        arrayAdapter = new ArrayAdapter <UserAccount> (this, android.R.layout.simple_list_item_1 , users);

        listView.setAdapter(arrayAdapter);


        // Обработка установки и снятия отметки в списке :
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

//                CheckBox cb = (CheckBox) v.getTag();
//                cb.setChecked(true);


                // получаем нажатый элемент
                UserAccount user = arrayAdapter.getItem(position);

                if(listView.isItemChecked(position)) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }


//                // снимаем все ранее установленные отметки (Убрать после написания Multiple реализации)
//                listView.clearChoices();

            }

        });
        // !Обработка установки и снятия отметки в списке

    }


    public void add(View view){

        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
        String expenseName = expenseNameEditText.getText().toString();

        if(!expenseName.isEmpty()){

            UserAccount newExpense = new UserAccount(expenseName,"guest");


            arrayAdapter.add(newExpense);
            expenseNameEditText.setText("");
            arrayAdapter.notifyDataSetChanged();


//            arrayAdapter.getItems();
//            ArrayList<String> base = new ArrayList<String>(); for (int i = 0; i < adapter.getCount(); i++) base.add(adapter.getItem(i))
//            arrayAdapter = new ArrayAdapter <UserAccount> (this, android.R.layout.simple_list_item_1 , users);


        }

    }


    public void remove(View view){

        // получаем и удаляем выделенные элементы
        for(int i=0; i< selectedUsers.size();i++){
            arrayAdapter.remove(selectedUsers.get(i));
        }

        // снимаем все ранее установленные отметки
        listView.clearChoices();

        // очищаем массив выбраных объектов
        selectedUsers.clear();

        arrayAdapter.notifyDataSetChanged();
    }
    */
    /** !Реализация с extends AppCompatActivity v4 (UserAccount) */


    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


//        /** Привязка элементов интерфейса
//        editTextName = findViewById(R.id.editTextName);
//        Button btnSave = findViewById(R.id.buttonSave);
//        textViewUsers = findViewById(R.id.textViewUsers);
//
//        /** Создание зависимостей прямо внутри Activity
//        AppDatabaseHelper dbHelper = new AppDatabaseHelper(getApplicationContext());
//        UserRepository repository = new UserRepository(dbHelper);
//        presenter = new UserPresenter(this, repository);
//
//        /** Первичная загрузка данных
//        presenter.loadUsers();
//
//        /** Делегируем действие презентеру
//        btnSave.setOnClickListener(v -&gt;
//        presenter.saveUser(editTextName.getText().toString().trim()));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    */


    /** Методы интерфейса View — вызываются презентером */
//    @Override
//    public void showUsers(List users) {
//        textViewUsers.setText(TextUtils.join("\n", users));
//    }
//
//    @Override
//    public void showError(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void clearInput() {
//        editTextName.setText("");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        presenter.detach(); // освобождаем ресурсы, предотвращаем утечки
//    }


}