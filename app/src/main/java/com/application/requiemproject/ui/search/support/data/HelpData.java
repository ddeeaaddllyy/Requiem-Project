package com.application.requiemproject.ui.search.support.data;


import com.application.requiemproject.data.local.entities.HelpItem;

import java.util.ArrayList;
import java.util.List;

public class HelpData {

    private static final List<HelpItem> questions = new ArrayList<>();

    static {
        questions.add(new HelpItem(
                0,
                "Приложение вылетает?",
                "Очистите кэш в настройках телефона: Настройки -> Приложения -> Requiem -> Память."
        ));

        questions.add(new HelpItem(
                1,
                "Как включить захват экрана?",
                "Нажмите на большую кнопку 'Начать' на главном экране и разрешите наложение окон."
        ));

        questions.add(new HelpItem(
                2,
                "Не работает перевод",
                "Проверьте интернет-соединение или баланс мобильного трафика."
        ));

        questions.add(new HelpItem(
                3,
                "Как сменить язык?",
                "В настройках переводчика выберите нужную пару языков (например, Английский -> Русский)."
        ));

        questions.add(new HelpItem(
                4,
                "Где мои скриншоты?",
                "Приложение обрабатывает изображения в оперативной памяти и не сохраняет их в галерею."
        ));

        questions.add(new HelpItem(
                5,
                "Как связаться с поддержкой?",
                "У нас есть отдельный телеграмм-канал в котором вы можете попросить помощи у сообщества или найти бота с reqiuem поддержкой"
        ));

    }

    public static List<HelpItem> getQuestions() {
        return questions;
    }
}
