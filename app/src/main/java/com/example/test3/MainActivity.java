package com.example.test3;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends /* ListActivity */  AppCompatActivity {

    /** Реализация с extends AppCompatActivity v2 : */
    // набор данных, который свяжем со списком
    String[] countries = { "Бразилия", "Аргентина", "Колумбия", "Чили", "Уругвай"};
    /** !Реализация с extends AppCompatActivity v2 : */


    /** Реализация с extends AppCompatActivity v3 : */
    ArrayList<String> users = new ArrayList<String>();
    ArrayList<String> selectedUsers = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView usersList;
    /** !Реализация с extends AppCompatActivity v3 */


//    private EditText editTextName;
//    private TextView textViewUsers;
//    private UserContract.Presenter presenter;


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


    /** Реализация с extends AppCompatActivity v3 : */
//    /*
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
//    */
    /** !Реализация с extends AppCompatActivity v3 */


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