package com.example.test3;

import android.database.sqlite.SQLiteException;
import android.provider.Contacts;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserPresenter  implements UserContract.Presenter {

    private static final String TAG = "UserPresenter";

    private final UserRepository repository;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private volatile UserContract.View view;


    public UserPresenter(UserContract.View view, UserRepository repository) {
        this.view = view;
        this.repository = repository;
    }


    @Override
    public void attach(UserContract.View view) {
        this.view = view;
    }


    @Override
    public void detach() {
        view = null;
        io.shutdownNow();
    }


    @Override
    public void saveUser(String name) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
//            Ui.run(
//                    () -&gt; {
//                        if (view != null) view.showError("Введите имя");
//                        }
//                    );
            return;
        }

//        io.execute(
//                () -&gt; {
//                            try {
//                                repository.saveUser(n);
//                                List users = repository.getAllUsers();
//                                Ui.run(() -&gt; {
//                                    if (view != null) {
//                                        view.clearInput();
//                                        view.showUsers(users);
//                                    }
//                                });
//                            } catch (SQLiteException e) {
//                                Log.e(TAG, "Ошибка базы данных", e);
//                                Ui.run(() -&gt; { if (view != null) view.showError("Не удалось сохранить данные"); });
//                            } catch (Exception e) {
//                                Log.e(TAG, "Неожиданная ошибка", e);
//                                Ui.run(() -&gt; { if (view != null) view.showError("Произошла ошибка. Попробуйте позже."); });
//                            }
//
//                        }
//                );

    }


    @Override
    public void loadUsers() {
//        io.execute(
//                () -&gt; {
//                                try {
//                                    List users = repository.getAllUsers();
//                                    Ui.run(() -&gt; { if (view != null) view.showUsers(users); });
//                                } catch (SQLiteException e) {
//                                    Log.e(TAG, "Ошибка базы данных", e);
//                                    Ui.run(() -&gt; { if (view != null) view.showError("Не удалось загрузить список"); });
//                                } catch (Exception e) {
//                                    Log.e(TAG, "Неожиданная ошибка", e);
//                                    Ui.run(() -&gt; { if (view != null) view.showError("Произошла ошибка. Попробуйте позже."); });
//                                }
//                }
//        );
    }


}

