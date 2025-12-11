package com.application.requiemproject.ui.search

object HelpData {
    fun getInitialList(): List<HelpItem> {
        return listOf(
            HelpItem(question = "Приложение вылетает?", solution = "Очистите кэш в настройках."),
            HelpItem(question = "Как включить захват?", solution = "Нажмите кнопку 'Начать' на главном экране."),
            HelpItem(question = "Как сменить язык?", solution = "Используйте выпадающие списки на главном экране."),
            HelpItem(question = "Где мои настройки?", solution = "Нажмите на иконку профиля -> Настройки."),
            HelpItem(question = "Оплата не проходит", solution = "Проверьте интернет-соединение и баланс карты."),
            HelpItem(question = "Отец пидор", solution = "Мама шлюха")
        )
    }
}