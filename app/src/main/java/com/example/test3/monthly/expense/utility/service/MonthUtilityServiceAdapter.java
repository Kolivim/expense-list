package com.example.test3.monthly.expense.utility.service;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNING;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.expenseList.ExpenseAdapter;
import com.example.test3.expenseList.ExpenseDetailWithDeleteActivity;
import com.example.test3.meter.Meter;
import com.example.test3.monthly.expense.planning.MonthlyExpensePlanningDto;
import com.example.test3.monthly.expense.planning.UniversalDepositsActivity;
import com.example.test3.monthly.expense.utility.service.meter.reading.MeterDetailActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class MonthUtilityServiceAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List< MonthUtilityServiceDto /* MonthlyExpensePlanningDto */ > groups;
    private LayoutInflater inflater;

    /** Для подсветки заголовка у выбранного месяца */
    private int selectedGroupPosition = -1;

    /*
    private ExpenseService expenseService;
    private DepositService depositService;
    private MeterReadingService meterReadingService;

    public MonthExpenseExpandableAdapter(Context context, List<MonthlyExpensePlanningDto> groups,
                                         ExpenseService expenseService,
                                         DepositService depositService,
                                         MeterReadingService meterReadingService) {
        this.context = context;
        this.groups = groups;
        this.inflater = LayoutInflater.from(context);
        this.expenseService = expenseService;
        this.depositService = depositService;
        this.meterReadingService = meterReadingService;
    }
    */

    private OnAddExpenseClickListener addExpenseListener;


    public interface OnAddExpenseClickListener {
        void onAddExpense(MonthUtilityServiceDto dto);
    }


    public void setOnAddExpenseClickListener(OnAddExpenseClickListener listener) {
        this.addExpenseListener = listener;
    }


    private OnGroupClickListener groupClickListener;


    public interface OnGroupClickListener {
        void onGroupClick(int groupPosition);
    }


    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.groupClickListener = listener;
    }


    public MonthUtilityServiceAdapter(Context context, List< MonthUtilityServiceDto /* MonthlyExpensePlanningDto */ > groups) {
        this.context = context;
        this.groups = groups;
        this.inflater = LayoutInflater.from(context);
    }


    public void setSelectedGroupPosition(int position) {
        this.selectedGroupPosition = position;
        notifyDataSetChanged();                                                                     /** Перерисовка */
    }


    @Override
    public int getGroupCount() {
//        return groups.size();
        return 1;
    }


    @Override
    public int getChildrenCount(int groupPosition) {

        /*
        return 1; // один составной дочерний элемент на месяц
        */


        List<Expense> children = groups.get(groupPosition).getExpenseList();
        return children == null ? 1 : children.size() + 1;
//        return groups.get(groupPosition).getExpenseList().size() + 1;


        /*
        List<Expense> children = groups.get(groupPosition).getExpenseList();
        return children == null ? 0 : children.size();
        */
    }


    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
//        return groups.get(groupPosition).getExpenseList().get(childPosition);

//        return groups.get(groupPosition);

        MonthUtilityServiceDto dto = groups.get(groupPosition);
        int expensesCount = dto.getExpenseList().size();
        if (childPosition < expensesCount) {
            return dto.getExpenseList().get(childPosition);
        } else {
            // возвращаем сам DTO для показаний
            return dto;
        }

    }


    @Override
    public long getGroupId(int groupPosition) {
        return groups.get(groupPosition).getMonth().getId();
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {

//        return groups.get(groupPosition).getExpenseList().get(childPosition).getId();

//        return groups.get(groupPosition).getMonth().getId();

        MonthUtilityServiceDto dto = groups.get(groupPosition);
        int expensesCount = dto.getExpenseList().size();
        if (childPosition < expensesCount) {
            return dto.getExpenseList().get(childPosition).getId();
        } else {
            // уникальный отрицательный ID для показаний
            return -dto.getMonth().getId();
        }

    }


    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.d("getGroupView", "startMethod");

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group_month, parent, false);
        }

        MonthUtilityServiceDto dto = groups.get(groupPosition);

        TextView textViewMonthName = convertView.findViewById(R.id.textViewMonthName);
        TextView textViewExpenseStats = convertView.findViewById(R.id.textViewExpenseStats);
        TextView textViewDepositStats = convertView.findViewById(R.id.textViewDepositStats);
        TextView textViewBalance = convertView.findViewById(R.id.textViewBalance);

        textViewMonthName.setText(dto.getMonth().getMonthYear());

        /*
        String stats = String.format("Расходы: %.2f руб. (%d записей | %d платежей)",
                dto.getTotalExpenseAmount(),
                dto.getExpensesCount(),
                dto.getPaymentsCount());
        */
        String expenseStats = context.getString(R.string.total_month_expense_amount,
                dto.getTotalExpenseAmount(), dto.getExpensesCount(), dto.getPaymentsCount());
        textViewExpenseStats.setText( /* stats */ expenseStats);
        /***/


        /** Устанавливаем статистику по взносам : */
        /*
        String depositStats = String.format("Внесено: %.2f руб. (%d шт.)",
                dto.getTotalDepositAmount(),
                dto.getDepositsCount());
        */
//                dto.getDepositsPayments);                                                         /** Не реализован сбор статистики по Deposit Payments */
        String depositStats = context.getString(R.string.total_month_deposit_amount, dto.getTotalDepositAmount());
        textViewDepositStats.setText(depositStats);
        /***/


//        /** Устанавливаем balance : */
//        String balance = String.format("Итог: %.2f руб.", dto.getBalance());
//        String balance = context.getString(R.string.balance_2, dto.getBalance());
//        textViewBalance.setText(balance);
        /***/


        // Также можно установить цвет текста для баланса
        /** Устанавливает фон для выделения */
        if (groupPosition == selectedGroupPosition) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }


        /** Кнопка добавить Expense для месяца */
        Button buttonAddExpense = convertView.findViewById(R.id.buttonAddExpense);
        buttonAddExpense.setOnClickListener(v -> {
            if (addExpenseListener != null) {
                addExpenseListener.onAddExpense(groups.get(groupPosition));
            }
        });


        convertView.setOnClickListener(v -> {
            if (groupClickListener != null) {
                groupClickListener.onGroupClick(groupPosition);
            }
        });


        Log.d("getGroupView", "endMethod");
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        Log.d("getChildView", "startMethod");

        MonthUtilityServiceDto dto = groups.get(groupPosition);
        int expensesCount = dto.getExpenseList().size();

        if (childPosition < expensesCount) {

            /** ---------- ОТОБРАЖЕНИЕ РАСХОДА ---------- */
            Expense expense = (Expense) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_child_expense, parent, false);
            }

            /** 1 : */
            TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
            TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);
            TextView textViewExpenseAmount = convertView.findViewById(R.id.textViewExpenseAmount);

            String expenseText = expense.getName();
            if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
                expenseText += " (" + expense.getDescription() + ")";
            }


            textViewInfo.setText(expenseText);
            textViewDate.setText(expense.getDateTimeString());
            textViewExpenseAmount.setText(context.getString(R.string.amount, expense.getExpenseListTotalAmount()));


            /** Устанавливает цвет : */
            if (expense.getRowColor() != null && expense.getRowColor() != -1) {
                textViewInfo.setTextColor(expense.getRowColor());
            } else {
                textViewInfo.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            }
            /** !Устанавливает цвет */


            View depositContainer = convertView.findViewById(R.id.depositContainer);
            TextView textViewDepositAmount = convertView.findViewById(R.id.textViewDepositAmount);

            TextView textViewBalance = convertView.findViewById(R.id.textViewBalance);


            /** Управляет видимостью контейнера с Deposit и поля Balance : */
            if (expense.getDepositList() == null || expense.getDepositList().isEmpty()) {

                depositContainer.setVisibility(View.GONE);
                textViewBalance.setVisibility(View.GONE);

            } else {

                depositContainer.setVisibility(View.VISIBLE);
                textViewBalance.setVisibility(View.VISIBLE);

                double depositTotalAmount = expense.getDepositListTotalAmount();
                textViewDepositAmount.setText(context.getString(R.string.total_deposit_amount, depositTotalAmount));

                double balance = expense.getBalance();   /* expense.getExpenseListTotalAmount() - expense.getDepositListTotalAmount(); */
                textViewBalance.setText(context.getString(R.string.balance, balance));

            }
            /***/


            /** Вызывает Activity для редактирования Expense */
            convertView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ExpenseDetailActivity.class);
//            Intent intent = new Intent(context, ExpenseDeleteActivity.class);
                Intent intent = new Intent(context, ExpenseDetailWithDeleteActivity.class);
                intent.putExtra("expense_id", expense.getId());
                context.startActivity(intent);
            });


            /** Вызывает Activity для редактирования списка Deposit, относящихся к Expense */
            Button depositButton = convertView.findViewById(R.id.deposit);
            depositButton.setOnClickListener(v -> {
                Log.d("depositButton.setOnClickListener", "pushDepositButton");



//            if (depositClickListener != null) {
//                depositClickListener.onDepositClick(expense);

                Intent intent = new Intent(context, UniversalDepositsActivity.class);
                intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_ID, expense.getId());
                intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_TYPE, UniversalDepositsActivity.TYPE_EXPENSE);
                intent.putExtra(UniversalDepositsActivity.EXTRA_TITLE, "Взносы: " + expense.getName());
                intent.putExtra(UniversalDepositsActivity.EXTRA_DEPOSIT_TYPE_ID, TYPE_DEPOSIT_MONTH_PLANNING);
                context.startActivity(intent);

//            }

            });
            /** !1 */

            return convertView;

        } else {

            // ---------- ОТОБРАЖЕНИЕ ПОКАЗАНИЙ СЧЁТЧИКОВ ----------
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_child_meter_readings, parent, false);
            }

//            RecyclerView readingsList = convertView.findViewById(R.id.listMeterReadings);
            ListView readingsList = convertView.findViewById(R.id.listMeterReadings);
//            TextView readingsTitle = convertView.findViewById(R.id.readingsTitle);

            List<Meter> meters = dto.getMeterList();
            if (meters != null && !meters.isEmpty()) {

                ArrayAdapter<Meter> adapter = new ArrayAdapter<Meter>(context, android.R.layout.simple_list_item_1, meters) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parentView /*parent*/ ) {
                        if (convertView == null) {
                            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parentView /*parent*/, false);
                        }
                        TextView text = convertView.findViewById(android.R.id.text1);
                        Meter meter = getItem(position);
                        text.setText(meter.getName() + ": " + meter.getValue() + " ед.");
                        return convertView;
                    }
                };
                readingsList.setAdapter(adapter);
                readingsList.setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.readingsTitle).setVisibility(View.VISIBLE);


                /** Обновляет список Meter : */
//                /*
                readingsList.setAdapter(adapter);
                readingsList.post(() -> {
                    int totalHeight = 0;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        View listItem = adapter.getView(i, null, readingsList);
                        listItem.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                        totalHeight += listItem.getMeasuredHeight();
                    }
                    ViewGroup.LayoutParams params = readingsList.getLayoutParams();
                    params.height = totalHeight;
                    readingsList.setLayoutParams(params);
                });
//                */
                /** !Обновляет список Meter */

                readingsList.setOnItemClickListener((parentR, view, position, id) -> {
                    Meter meter = (Meter) parentR.getItemAtPosition(position);
                    Intent intent = new Intent(context, MeterDetailActivity.class);
                    intent.putExtra(MeterDetailActivity.EXTRA_METER_ID, meter.getId());
                    context.startActivity(intent);
                });


            } else {
                readingsList.setVisibility(View.GONE);
                convertView.findViewById(R.id.readingsTitle).setVisibility(View.GONE);
            }

//            if (dto.getMeterReadingList() != null && !dto.getMeterReadingList().isEmpty()) {
//                MeterReadingAdapter adapter = new MeterReadingAdapter(context, dto.getMeterReadingList());
//                readingsList.setAdapter(adapter);
//                readingsList.setVisibility(View.VISIBLE);
//                convertView.findViewById(R.id.readingsTitle).setVisibility(View.VISIBLE);
//            } else {
//                readingsList.setVisibility(View.GONE);
//                convertView.findViewById(R.id.readingsTitle).setVisibility(View.GONE);
//            }


            Button addButton = convertView.findViewById(R.id.buttonAddMeterReading);
            addButton.setOnClickListener(v -> {

                Log.d("getChildView", "Нажата кнопка добавить показания");

                if (addMeterReadingListener != null) {

                    /* Прокидывается в Активити в метод adapter.setOnAddMeterReadingListener(this::showAddMeterReadingDialog); */
                    addMeterReadingListener.onAddMeterReading(dto);
                    /* showAddMeterReadingDialog(dto); */ /** Вынесено в Активити MonthUtilityServiceActivity */

                }

            });


            return convertView;
        }



        /******************************************************************************************/



        /*
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_child_month_utility_service_detail, parent, false);
        }

        MonthlyExpensePlanningDto dto = groups.get(groupPosition);


        // Список расходов
        ListView expensesList = convertView.findViewById(R.id.listExpenses);
        ExpenseInMonthAdapter expenseAdapter = new ExpenseInMonthAdapter(context, dto.getExpenseList());
        expensesList.setAdapter(expenseAdapter);


        // Список взносов (если есть)
//        ListView depositsList = convertView.findViewById(R.id.listDeposits);
//        if (dto.getDepositList() != null && !dto.getDepositList().isEmpty()) {
//            DepositAdapter depositAdapter = new DepositAdapter(context, dto.getDepositList(), depositService, null);
//            depositsList.setAdapter(depositAdapter);
//            depositsList.setVisibility(View.VISIBLE);
//            convertView.findViewById(R.id.depositsTitle).setVisibility(View.VISIBLE);
//        } else {
//            depositsList.setVisibility(View.GONE);
//            convertView.findViewById(R.id.depositsTitle).setVisibility(View.GONE);
//        }

        // Список показаний
//        ListView readingsList = convertView.findViewById(R.id.listMeterReadings);
//        if (dto.getMeterReadingList() != null && !dto.getMeterReadingList().isEmpty()) {
//            MeterReadingAdapter readingAdapter = new MeterReadingAdapter(context, dto.getMeterReadingList());
//            readingsList.setAdapter(readingAdapter);
//            readingsList.setVisibility(View.VISIBLE);
//            convertView.findViewById(R.id.readingsTitle).setVisibility(View.VISIBLE);
//        } else {
//            readingsList.setVisibility(View.GONE);
//            convertView.findViewById(R.id.readingsTitle).setVisibility(View.GONE);
//        }

        return convertView;
        */
    }
//
//
    public View getChildViewIsh(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        Log.d("getChildView", "startMethod");

//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.list_child_month_detail, parent, false);
//        }
//
//        MonthlyExpensePlanningDto dto = groups.get(groupPosition);
//
//        // Список расходов
//        ListView expensesList = convertView.findViewById(R.id.listExpenses);
//        ExpenseAdapter expenseAdapter = new ExpenseAdapter(context, dto.getExpenseList(), expenseService);
//        expensesList.setAdapter(expenseAdapter);
//
//        // Список взносов
//        ListView depositsList = convertView.findViewById(R.id.listDeposits);
//        DepositAdapter depositAdapter = new DepositAdapter(context, dto.getDepositList(), depositService, null);
//        depositsList.setAdapter(depositAdapter);
//
//        return convertView;


        // Контейнер для показаний
//        LinearLayout meterReadingsContainer = convertView.findViewById(R.id.meterReadingsContainer);
//        TextView noReadingsText = convertView.findViewById(R.id.noReadingsText);
//        ListView readingsListView = convertView.findViewById(R.id.readingsListView);
//
//        meterReadingService.getReadingsForExpense(expense.getId(), readings -> {
//            if (readings == null || readings.isEmpty()) {
//                meterReadingsContainer.setVisibility(View.GONE);
//            } else {
//                meterReadingsContainer.setVisibility(View.VISIBLE);
//                MeterReadingAdapter adapter = new MeterReadingAdapter(context, readings);
//                readingsListView.setAdapter(adapter);
//            }
//        });



        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_child_expense, parent, false);
        }


        Expense expense = (Expense) getChild(groupPosition, childPosition);

        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
        TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);
        TextView textViewExpenseAmount = convertView.findViewById(R.id.textViewExpenseAmount);

        String expenseText = expense.getName();
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            expenseText += " (" + expense.getDescription() + ")";
        }
        /*
        if (expense.getExpenseList() != null && !expense.getExpenseList().isEmpty()) {
            expenseText += "\nСумма: " + String.format("%.2f", expense.getExpenseListTotalAmount()) +
                    " руб. | Платежей: " + expense.getExpenseList().size();
        } else {
            expenseText += "\nНет платежей";
        }
        */


        textViewInfo.setText(expenseText);
        textViewDate.setText(expense.getDateTimeString());
        textViewExpenseAmount.setText(context.getString(R.string.amount, expense.getExpenseListTotalAmount()));


        /** Устанавливает цвет : */
        if (expense.getRowColor() != null && expense.getRowColor() != -1) {
            textViewInfo.setTextColor(expense.getRowColor());
        } else {
            textViewInfo.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }
        /** !Устанавливает цвет */


        View depositContainer = convertView.findViewById(R.id.depositContainer);
//        TextView textViewDepositName = convertView.findViewById(R.id.textViewDepositName);
//        TextView textViewDepositDate = convertView.findViewById(R.id.textViewDepositDate);
        TextView textViewDepositAmount = convertView.findViewById(R.id.textViewDepositAmount);

        TextView textViewBalance = convertView.findViewById(R.id.textViewBalance);


        /** Управляет видимостью контейнера с Deposit и поля Balance : */
        if (expense.getDepositList() == null || expense.getDepositList().isEmpty()) {

            depositContainer.setVisibility(View.GONE);
            textViewBalance.setVisibility(View.GONE);

        } else {

            depositContainer.setVisibility(View.VISIBLE);
            textViewBalance.setVisibility(View.VISIBLE);

            double depositTotalAmount = expense.getDepositListTotalAmount();
            textViewDepositAmount.setText(context.getString(R.string.total_deposit_amount, depositTotalAmount));

            double balance = expense.getBalance();   /* expense.getExpenseListTotalAmount() - expense.getDepositListTotalAmount(); */
            textViewBalance.setText(context.getString(R.string.balance, balance));


            /*
            textViewDepositInfo
            textViewDepositAmount
            textViewDepositDate
            textViewBalance

                    double amount = deposit.getTotalAmount();                                                   //  String amountStr = String.format("%.2f руб.", deposit.getTotalAmount());
                    String dateStr = deposit.getDateTime() != null ? deposit.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yy")) : "без даты";
                    String desc = deposit.getDescription() != null && !deposit.getDescription().isEmpty() ? " (" + deposit.getDescription() + ")" : "";
                    int paymentsCount = deposit.getPayments() == null ? 0 : deposit.getPayments().size();

                    textViewDepositName.setText(deposit.getName() + desc);
                    textViewDepositDate.setText(dateStr);
                    textViewDepositAmount.setText(context.getString(R.string.amount, amount));                  //  textViewDepositAmount.setText(amountStr);
                    textViewPaymentsCount.setText(context.getString(R.string.payments_count, paymentsCount));   //  textViewPaymentsCount.setText(deposit.getPayments() == null ? "0" : String.valueOf(deposit.getPayments().size()));
            */

        }
        /***/


        /** Вызывает Activity для редактирования Expense */
        convertView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ExpenseDetailActivity.class);
//            Intent intent = new Intent(context, ExpenseDeleteActivity.class);
            Intent intent = new Intent(context, ExpenseDetailWithDeleteActivity.class);
            intent.putExtra("expense_id", expense.getId());
            context.startActivity(intent);
        });


        /** Вызывает Activity для редактирования списка Deposit, относящихся к Expense */
        Button depositButton = convertView.findViewById(R.id.deposit);
        depositButton.setOnClickListener(v -> {
            Log.d("depositButton.setOnClickListener", "pushDepositButton");



//            if (depositClickListener != null) {
//                depositClickListener.onDepositClick(expense);

            Intent intent = new Intent(context, UniversalDepositsActivity.class);
            intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_ID, expense.getId());
            intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_TYPE, UniversalDepositsActivity.TYPE_EXPENSE);
            intent.putExtra(UniversalDepositsActivity.EXTRA_TITLE, "Взносы: " + expense.getName());
            intent.putExtra(UniversalDepositsActivity.EXTRA_DEPOSIT_TYPE_ID, TYPE_DEPOSIT_MONTH_PLANNING);
            context.startActivity(intent);

//            }

        });


        Log.d("getChildView", "endMethod");
        return convertView;
    }


    /** Обработчик нажатий кнопки Взносы : */
//    public interface OnDepositClickListener {
//        void onDepositClick(Expense expense);
//    }
//
//    private OnDepositClickListener depositClickListener;
//
//    public void setOnDepositClickListener(OnDepositClickListener listener) {
//        this.depositClickListener = listener;
//    }
    /** !Обработчик нажатий кнопки Взносы */


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;                                                                                /** дочерние элементы кликабельны */
    }


    /** Для списка показаний : */
    public interface OnAddMeterReadingListener {
        void onAddMeterReading(MonthUtilityServiceDto dto);
    }


    private OnAddMeterReadingListener addMeterReadingListener;


    public void setOnAddMeterReadingListener(OnAddMeterReadingListener listener) {
        this.addMeterReadingListener = listener;
    }


    /** Вынесено в Активити MonthUtilityServiceActivity */
    /*
    private void showAddMeterReadingDialog(MonthlyExpensePlanningDto dto) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавить показание для " + dto.getMonth().getMonthYear());

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_meter_reading, null);
        EditText editDate = view.findViewById(R.id.editReadingDate);
        EditText editCurrent = view.findViewById(R.id.editCurrentValue);
        EditText editPrevious = view.findViewById(R.id.editPreviousValue);
        EditText editConsumption = view.findViewById(R.id.editConsumption);
        EditText editDescription = view.findViewById(R.id.editDescription);

        // Предустановим текущую дату
        editDate.setText(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy")));

        builder.setView(view);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            // Валидация
            String dateStr = editDate.getText().toString().trim();
            String currentStr = editCurrent.getText().toString().trim();
            String previousStr = editPrevious.getText().toString().trim();
            String consumptionStr = editConsumption.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (dateStr.isEmpty()) {
                Toast.makeText(context, "Введите дату", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentStr.isEmpty()) {
                Toast.makeText(context, "Введите текущее показание", Toast.LENGTH_SHORT).show();
                return;
            }

            double currentValue;
            try {
                currentValue = Double.parseDouble(currentStr);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Некорректное текущее показание", Toast.LENGTH_SHORT).show();
                return;
            }

            Double previousValue = null;
            if (!previousStr.isEmpty()) {
                try {
                    previousValue = Double.parseDouble(previousStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Некорректное предыдущее показание", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Double consumption = null;
            if (!consumptionStr.isEmpty()) {
                try {
                    consumption = Double.parseDouble(consumptionStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Некорректное потребление", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (previousValue != null) {
                // Автоматически рассчитаем потребление
                consumption = currentValue - previousValue;
            } else {
                Toast.makeText(context, "Укажите потребление или предыдущее показание", Toast.LENGTH_SHORT).show();
                return;
            }

            ZonedDateTime readingDate = parseDate(dateStr);
            if (readingDate == null) {
                Toast.makeText(context, "Некорректная дата (дд.мм.гг)", Toast.LENGTH_SHORT).show();
                return;
            }


//            // Создаём MeterReading
//            MeterReading reading = new MeterReading();
//            reading.setExpenseId(dto.getMonth().getId()); // привязываем к месяцу
//            reading.setReadingDate(Date.from(readingDate.toInstant()));
//            reading.setCurrentValue(currentValue);
//            reading.setPreviousValue(previousValue);
//            reading.setConsumption(consumption);
//            reading.setDescription(description.isEmpty() ? null : description);
//            // photoUri пока не заполняем
//
//            // Сохраняем через сервис
//            meterReadingService.addMeterReading(reading, new MeterReadingService.Callback<Long>() {
//                @Override
//                public void onResult(Long id) {
//                    if (id != -1) {
//                        Toast.makeText(MonthUtilityServiceActivity.this, "Показание добавлено", Toast.LENGTH_SHORT).show();
//                        loadData(); // перезагрузить данные
//                    } else {
//                        Toast.makeText(MonthUtilityServiceActivity.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//            });


        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }


    private ZonedDateTime parseDate(String dateStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
            Date date = formatter.parse(dateStr);
            return date.toInstant().atZone(ZoneId.systemDefault());
        } catch (ParseException e) {
            return null;
        }
    }
    */
    /** !Для списка показаний */


    private class MeterRecyclerAdapter extends RecyclerView.Adapter<MeterRecyclerAdapter.ViewHolder> {
        private List<Meter> meters;

        public MeterRecyclerAdapter(List<Meter> meters) {
            this.meters = meters;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Meter meter = meters.get(position);
            holder.textView.setText(meter.getName() + ": " + meter.getValue() + " ед.");
        }

        @Override
        public int getItemCount() {
            return meters.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }

}