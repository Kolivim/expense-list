package com.example.test3.service;

import static com.example.test3.dao.ExpenseSQLite.METER_ID;
import static com.example.test3.dao.ExpenseSQLite.METER_MONTH_ID;
import static com.example.test3.dao.ExpenseSQLite.METER_NAME;
import static com.example.test3.dao.ExpenseSQLite.METER_VALUE;
import static com.example.test3.dao.ExpenseSQLite.TABLE_METER;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.expenseList.Expense;
import com.example.test3.meter.Meter;
import com.example.test3.util.Util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MeterService {

    private static final String TAG = "MeterService";

    private ExpenseSQLite dbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    private ExpenseService expenseService;
    private DepositService depositService;


    public MeterService(Context context) {
        this.dbHelper = new ExpenseSQLite(context);
        this.dbRead = dbHelper.getReadableDatabase();
        this.dbWrite = dbHelper.getWritableDatabase();
    }


    /** Вставляет показание счётчика, для месяца, в БД */
    public long insertMeter(String name, double currentValue, Long monthId) {
        Log.d(TAG, "insertMeter() startMethod");

        ContentValues cv = new ContentValues();
        cv.put(ExpenseSQLite.METER_MONTH_ID, monthId);
        cv.put(METER_NAME, name);
        cv.put(METER_VALUE, currentValue);

        Log.d(TAG, "insertMeter: name=" + name +
                ", currentValue=" + currentValue +
                ", monthId=" + monthId);

        return dbWrite.insert(TABLE_METER, null, cv);
    }


    /** Вставляет показание счётчика, для месяца, в БД */
    public boolean removeMeter(Long meterId) {
        Log.d(TAG, "insertMeter() startMethod, meterId: " + meterId);

        long result = 0;

        try {

//            dbWrite.beginTransaction();

            result = dbWrite.delete(TABLE_METER, METER_ID + " = ?",
                    new String[]{String.valueOf(meterId.toString())});

//            dbWrite.setTransactionSuccessful();
            Log.d("ExpenseService", "Удалено записей: " + result);

        } catch (Exception e) {
            Log.e("ExpenseService", "Ошибка при удалении Показаний c meterId: ".concat(meterId.toString()), e);
        } finally {
//            dbWrite.endTransaction();
        }

        return result != -1;
    }


    public List<Meter> getMeterList(Long monthId) {
        Log.d(TAG, " getMeterList() startMethod, monthId: " + monthId);

        ArrayList<Meter> meterList = new ArrayList<>();

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + TABLE_METER + " " +
                        " where " + METER_MONTH_ID + " = " + monthId.toString() + " " +
                        " order by " + METER_ID + " desc;",
                null
        );

        while(cursor.moveToNext()){

            Long meterId = cursor.getLong(0);
            Long pullMonthId = cursor.getLong(1);
            String meterName = cursor.getString(2);
            Double meterValue = cursor.getDouble(3);

            Meter meter = new Meter(meterId, pullMonthId, meterName, meterValue);
            meterList.add(meter);

        }

        cursor.close();

        return meterList;

    }


    public Meter getMeterById(long meterId) {
        Log.d(TAG, "getMeterById() startMethod, meterId: " + meterId);

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + TABLE_METER + " WHERE " + METER_ID + " = ?",
                new String[]{String.valueOf(meterId)}
        );

        Meter meter = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            long monthId = cursor.getLong(1);
            String name = cursor.getString(2);
            double value = cursor.getDouble(3);
            meter = new Meter(id, monthId, name, value);
        }

        cursor.close();

        Log.d(TAG, "getMeterById() endMethod, к возврату meter: " + meter);
        return meter;
    }


    public boolean updateMeter(Meter meter) {
        Log.d(TAG, "updateMeter() startMethod, meter: " + meter);
        ContentValues cv = new ContentValues();
        cv.put(METER_NAME, meter.getName());
        cv.put(METER_VALUE, meter.getValue());
        int result = dbWrite.update(TABLE_METER, cv, METER_ID + " = ?",
                new String[]{String.valueOf(meter.getId())});
        return result > 0;
    }


}
