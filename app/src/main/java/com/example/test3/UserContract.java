package com.example.test3;

import java.util.List;

public interface UserContract {


    interface View {
        void showUsers(List users);             // показать список пользователей
        void showError(String message);         // вывести сообщение об ошибке
        void clearInput();                      // очистить поле ввода
    }


    interface Presenter {
        void attach(View view);                 // привязать View
        void detach();                          // отвязать View, освободить ресурсы
        void saveUser(String name);             // сохранить пользователя
        void loadUsers();                       // загрузить список
    }


}
