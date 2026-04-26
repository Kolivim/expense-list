package com.example.test3.deposit;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.R;
import com.example.test3.deposit.Deposit;
import com.example.test3.service.DepositService;
import com.example.test3.util.UtilService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_REFUND_PLANNING;

public class DepositAddActivity extends AppCompatActivity {

    public static final String EXTRA_PARENT_ID = "parent_id";
    public static final String EXTRA_DEFAULT_NAME = "default_name";
    public static final String EXTRA_DEFAULT_DATE = "default_date";

    private EditText editName, editAmount, editDate, editDescription;
    private Button buttonSave, buttonCancel;
    private DepositService depositService;
    private long parentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_actual_deposit);

        depositService = new DepositService(this);

        parentId = getIntent().getLongExtra(EXTRA_PARENT_ID, -1);
        if (parentId == -1) {
            Toast.makeText(this, "Ошибка: не указан родитель", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName = findViewById(R.id.editTextDepositName);
        editAmount = findViewById(R.id.editTextDepositAmount);
        editDate = findViewById(R.id.editTextDepositDate);
        editDescription = findViewById(R.id.editTextDepositDescription);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        /** Предзаполнение : */
        String defaultName = getIntent().getStringExtra(EXTRA_DEFAULT_NAME);
        if (!TextUtils.isEmpty(defaultName)) {
            editName.setText(defaultName);
        }
        String defaultDate = getIntent().getStringExtra(EXTRA_DEFAULT_DATE);
        if (!TextUtils.isEmpty(defaultDate)) {
            editDate.setText(defaultDate);
        } else {
            editDate.setText(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy")));
        }

        buttonSave.setOnClickListener(v -> saveDeposit());
        buttonCancel.setOnClickListener(v -> finish());
    }


    private void saveDeposit() {
        String name = editName.getText().toString().trim();
        String amountStr = editAmount.getText().toString().trim();
        String dateStr = editDate.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

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

        ZonedDateTime date = UtilService.parseDate(dateStr);
        if (date == null) {
            Toast.makeText(this, "Некорректная дата", Toast.LENGTH_SHORT).show();
            return;
        }

        Deposit deposit = new Deposit(name, TYPE_DEPOSIT_MONTH_REFUND_PLANNING, date, parentId, amount);

        if (!description.isEmpty()) deposit.setDescription(description);

        long id = depositService.insertDeposit(deposit);
        if (id != -1) {
            Toast.makeText(this, "Взнос добавлен", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
        }

    }


}
