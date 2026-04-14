package com.example.test3.monthly.expense.utility.service.meter.reading;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.test3.R;
import com.example.test3.meter.Meter;
import com.example.test3.service.MeterService;

public class MeterDetailActivity extends AppCompatActivity {

    public static final String EXTRA_METER_ID = "meter_id";

    private TextView textViewName, textViewValue;
    private Button buttonDelete, buttonUpdate, buttonBack;
    private MeterService meterService;
    private Meter currentMeter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_detail);

        long meterId = getIntent().getLongExtra(EXTRA_METER_ID, -1);
        if (meterId == -1) {
            Toast.makeText(this, "Ошибка: не указан ID показания", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        meterService = new MeterService(this);
        currentMeter = meterService.getMeterById(meterId);
        if (currentMeter == null) {
            Toast.makeText(this, "Показание не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewName = findViewById(R.id.textViewMeterName);
        textViewValue = findViewById(R.id.textViewMeterValue);
        buttonDelete = findViewById(R.id.buttonDeleteMeter);
        buttonUpdate = findViewById(R.id.buttonUpdateMeter);
        buttonBack = findViewById(R.id.buttonBack);

        textViewName.setText(currentMeter.getName());
        textViewValue.setText("Текущее показание: " + currentMeter.getValue());

        buttonDelete.setOnClickListener(v -> confirmDelete());
        buttonUpdate.setOnClickListener(v -> updateMeter());
        buttonBack.setOnClickListener(v -> finish());
    }


    private void confirmDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Удалить показание")
                .setMessage("Удалить показание \"" + currentMeter.getName() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    if (meterService.removeMeter(currentMeter.getId())) {
                        Toast.makeText(this, "Показание удалено", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null)
                .show();

    }


    private void updateMeter() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить показание");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_meter, null);
        EditText editName = view.findViewById(R.id.editMeterName);
        EditText editValue = view.findViewById(R.id.editMeterValue);

        editName.setText(currentMeter.getName());
        editValue.setText(String.valueOf(currentMeter.getValue()));

        builder.setView(view);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newName = editName.getText().toString().trim();
            String newValueStr = editValue.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            double newValue;
            try {
                newValue = Double.parseDouble(newValueStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректное значение", Toast.LENGTH_SHORT).show();
                return;
            }

            currentMeter.setName(newName);
            currentMeter.setValue(newValue);

            if (meterService.updateMeter(currentMeter)) {
                Toast.makeText(this, "Показание обновлено", Toast.LENGTH_SHORT).show();
                textViewName.setText(currentMeter.getName());
                textViewValue.setText("Текущее показание: " + currentMeter.getValue() + " ед.");
            } else {
                Toast.makeText(this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();

    }


}