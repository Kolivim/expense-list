package com.example.test3.monthly.expense.planning;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNING;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.R;
import com.example.test3.deposit.Deposit;
import com.example.test3.deposit.DepositAdapter;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.DepositService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Универсальная Activity для управления взносами (Deposit).
 * Поддерживает два типа родителей:
 * - TYPE_MONTH (для ежемесячного планирования)
 * - TYPE_EXPENSE (для конкретного расхода)
 */
public class UniversalDepositsActivity extends AppCompatActivity {

    private static final String TAG = "UniversalDepositsActivity";

    public static final String EXTRA_PARENT_ID = "parent_id";
    public static final String EXTRA_PARENT_TYPE = "parent_type";
    public static final String EXTRA_TITLE = "title";

    /** Для PlanningDeposit */
    public static final String EXTRA_DEFAULT_NAME = "default_name";
    public static final String EXTRA_DEFAULT_DATE = "default_date";

    public static final int TYPE_MONTH = 1;
    public static final int TYPE_EXPENSE = 2;

    private DepositService depositService;
    private long parentId;
    private int parentType;
    private String activityTitle;

    private List<Deposit> depositList;
    public static final String EXTRA_DEPOSIT_TYPE_ID = "deposit_type_id";
    private long depositTypeId;
    private DepositAdapter adapter;

    private TextView textViewTitle;
    private ListView listViewDeposits;
    private Button buttonAddDeposit, buttonBack;

    /** Поля ввода для новой записи Deposit */
    private TextView textViewDepositName, textViewDepositAmount, textViewDepositDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate startMethod");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universal_deposits);

        parentId = getIntent().getLongExtra(EXTRA_PARENT_ID, -1);
        parentType = getIntent().getIntExtra(EXTRA_PARENT_TYPE, -1);
        activityTitle = getIntent().getStringExtra(EXTRA_TITLE);
        depositTypeId = getIntent().getLongExtra(EXTRA_DEPOSIT_TYPE_ID, -1);

        Log.d(TAG, "onCreate получены parentId: " + parentId + ", parentType: " + parentType +
                ", depositTypeId: " + depositTypeId);

        if (parentId == -1 || parentType == -1) {
            Toast.makeText(this, "Ошибка: не указан родитель", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        depositService = new DepositService(this);

        initViews();
        loadDeposits();

        buttonAddDeposit.setOnClickListener(v -> {
            Log.d(TAG, "onCreate push buttonAddDeposit");
            add(v);                                                                                 /* showAddDepositDialog(); */
        });

        buttonBack.setOnClickListener(v -> finish());

        Log.d(TAG, "onCreate endMethod");
    }


    private void initViews() {
        Log.d(TAG, "initViews startMethod");

        textViewTitle = findViewById(R.id.textViewTitle);
        listViewDeposits = findViewById(R.id.listViewDeposits);
        buttonAddDeposit = findViewById(R.id.buttonAddDeposit);
        buttonBack = findViewById(R.id.buttonBack);

        textViewDepositName = findViewById(R.id.editTextDepositName);
        textViewDepositAmount = findViewById(R.id.editTextDepositAmount);
        textViewDepositDate = findViewById(R.id.editTextDepositDate);

        /** Устанавливает размер подсказок */
        textViewDepositName.setHint(getHint(12, "Название взноса"));
        textViewDepositAmount.setHint(getHint(12, "Сумма"));
        textViewDepositDate.setHint(getHint(12, "dd.mm.yy"));

        if (activityTitle != null && !activityTitle.isEmpty()) {
            textViewTitle.setText(activityTitle);
        } else {
            textViewTitle.setText("Взносы");
        }


        /** Для планирования возвратов : */
        String defaultName = getIntent().getStringExtra(EXTRA_DEFAULT_NAME);
        if (defaultName != null && !defaultName.isEmpty()) textViewDepositName.setText(defaultName);

        String defaultDate = getIntent().getStringExtra(EXTRA_DEFAULT_DATE);
        if (defaultDate != null && !defaultDate.isEmpty()) textViewDepositDate.setText(defaultDate);
        /** !Для планирования возвратов */

        Log.d(TAG, "initViews endMethod");
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume startMethod");

        super.onResume();
        loadDeposits();

        Log.d(TAG, "onResume endMethod");
    }


    private void loadDeposits() {
        Log.d(TAG, "loadDeposits startMethod");

        /** Все взносы, привязанные к parentId (expenseId в таблице Deposit) */
        depositList = depositService.getExpenseDeposits(parentId, depositTypeId);
        if (depositList == null) depositList = new ArrayList<>();

        adapter = new DepositAdapter(this, depositList, depositService, () -> {

            /** Callback после удаления – перезагрузить данные и обновить UI */
            loadDeposits();

        });
//        adapter = new DepositAdapter(depositList);

        listViewDeposits.setAdapter(adapter);

        Log.d(TAG, "loadDeposits endMethod");
    }


    public void add(View view) {
        Log.d(TAG, "add startMethod");

        /** Вычитываем введённые пользователем данные: */
        String name = textViewDepositName.getText().toString().trim();
        String amountStr = textViewDepositAmount.getText().toString().trim();
        String dateStr = textViewDepositDate.getText().toString().trim();
//        String description = editTextDescription.getText().toString().trim();


        if(checkDeposit(name, amountStr, dateStr)) ;
        double amount = Double.parseDouble(amountStr);
        ZonedDateTime dateTime = parseDate(dateStr);
        /*
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
            return;
        }

        ZonedDateTime dateTime = parseDate(dateStr);
        if (dateTime == null) {
            Toast.makeText(this, "Некорректная дата (используйте дд.мм.гг)", Toast.LENGTH_SHORT).show();
            return;
        }
        */


        Deposit deposit = new Deposit(name, depositTypeId, dateTime, parentId, amount);
//        Deposit deposit = new Deposit(name, depositTypeId);
////        if (!description.isEmpty()) deposit.setDescription(description);
//        deposit.setDateTime(dateTime);
//        deposit.addPayment(amount);
//        deposit.setExpenseId(parentId);

        long id = depositService.insertDeposit(deposit);
        if (id != -1) {

            Toast.makeText(this, "Взнос добавлен", Toast.LENGTH_SHORT).show();

            loadDeposits();

            if (parentType == TYPE_EXPENSE) {
                /** Вызвать сервис для пересчёта суммы Deposit родительского Expense */
                // expenseService.updateExpenseTotalAmount(parentId);
            }

        } else {
            Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
        }


        cleanUserInput(textViewDepositName, textViewDepositAmount, textViewDepositDate);

        Log.d(TAG, "add endMethod");
    }


    public boolean checkDeposit(String name, String amountStr, String dateStr) {
        Log.d(TAG, "checkDeposit startMethod, name: " + name + ", amountStr: " + amountStr +
                ", dateStr: " + dateStr );

        boolean isCorrect = true;

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }

        /* м.б. NULL
        ZonedDateTime dateTime = parseDate(dateStr);
        if (dateTime == null) {
            Toast.makeText(this, "Некорректная дата (используйте дд.мм.гг)", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }
        */


        Log.d(TAG, "checkDeposit endMethod, к возврату isCorrect: " + isCorrect  +
                " для name: " + name + ", amountStr: " + amountStr + ", dateStr: " + dateStr );
        return isCorrect;
    }


    private ZonedDateTime parseDate(String dateStr) {
        Log.d(TAG, "parseDate startMethod");

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
            java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr, formatter);
            return localDate.atStartOfDay(ZonedDateTime.now().getZone());
        } catch (Exception e) {
            return null;
        }

    }


    private SpannableString getHint(int size, String nameHint) {
        Log.d(TAG, "getHint startMethod, size: " + size);

        /** Устанавливает размер подсказки через SpannableString */
        SpannableString hint = new SpannableString(nameHint);
        hint.setSpan(new AbsoluteSizeSpan(size, true), 0, hint.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        Log.d(TAG, "getHint endMethod, к возврату " + hint + " для size: " + size);
        return hint;
    }


    public void cleanUserInput(TextView textViewDepositName, TextView textViewDepositAmount,
                               TextView textViewDepositDate) {
        Log.d(TAG, "cleanUserInput startMethod");

        textViewDepositName.setText("");
        textViewDepositAmount.setText("");
        textViewDepositDate.setText("");

        Log.d(TAG, "cleanUserInput endMethod");
    }


    /** Устаревшие реализации : */
    // todo: Для неё есть активити, перенести все в неё
    private void showAddDepositDialog() {
        Log.d(TAG, "showAddDepositDialog startMethod");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить взнос");

        View view = getLayoutInflater().inflate(R.layout.dialog_universal_deposit, null);
        EditText editTextName = view.findViewById(R.id.editTextDepositName);
        EditText editTextAmount = view.findViewById(R.id.editTextDepositAmount);
        EditText editTextDate = view.findViewById(R.id.editTextDepositDate);
        EditText editTextDescription = view.findViewById(R.id.editTextDepositDescription);


        /** Предлагаем текущую дату по умолчанию */
        editTextDate.setText(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy")));

        builder.setView(view);
        builder.setPositiveButton("Добавить", (dialog, which) -> {

            String name = editTextName.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String dateStr = editTextDate.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
                return;
            }

            ZonedDateTime dateTime = parseDate(dateStr);
            if (dateTime == null) {
                Toast.makeText(this, "Некорректная дата (используйте дд.мм.гг)", Toast.LENGTH_SHORT).show();
                return;
            }

            /** Создаёт Deposit */
            Deposit deposit = new Deposit(name, depositTypeId);
            if (!description.isEmpty()) deposit.setDescription(description);
            deposit.setDateTime(dateTime);
            deposit.addPayment(amount);
            deposit.setExpenseId(parentId);

            long id = depositService.insertDeposit(deposit);
            if (id != -1) {
                Toast.makeText(this, "Взнос добавлен", Toast.LENGTH_SHORT).show();

                /** Обновляет список */
                loadDeposits();

//                /** Если родитель — Expense, можно также обновить общую сумму расхода */
//                if (parentType == TYPE_EXPENSE) {
//                    /** вызвать сервис для пересчёта суммы расхода */
//                    // expenseService.updateExpenseTotalAmount(parentId);
//                }

            } else {
                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNegativeButton("Отмена", null);
        builder.show();

        Log.d(TAG, "showAddDepositDialog endMethod");
    }
    /** !Устаревшие реализации */


}