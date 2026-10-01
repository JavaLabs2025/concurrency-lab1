package org.labs;

/**
 * Все параметры обеда. Задержки в микросекундах:
 * каждое действие длится случайное время из диапазона [min, max].
 */
record Config(
        int programmers,  // сколько программистов за столом
        int waiters,      // сколько официантов
        long food,        // сколько порций на кухне
        int maxLead,      // на сколько порций можно обогнать соседа
        Delay eat,        // сколько длится еда
        Delay think,      // сколько длится разговор о преподавателях
        Delay serve,      // сколько официант несёт тарелку
        Delay backoff) {  // сколько ждёт тот, кто слишком обогнал соседа

    Config {
        if (programmers < 2) {
            throw new IllegalArgumentException("Нужно минимум 2 программиста");
        }
        if (waiters < 1) {
            throw new IllegalArgumentException("Нужен минимум 1 официант");
        }
        if (food < 0) {
            throw new IllegalArgumentException("Еды не может быть меньше нуля");
        }
        if (maxLead < 0) {
            throw new IllegalArgumentException("maxLead не может быть отрицательным");
        }
        if (backoff.minMicros() < 1) {
            // Иначе ожидание соседа превратится в пустой цикл, нагружающий процессор
            throw new IllegalArgumentException("backoff.min должен быть не меньше 1 мкс");
        }
    }

    static Config defaults() {
        return new Config(7, 2, 1_000_000, 2,
                new Delay(50, 150),   // еда
                new Delay(0, 100),    // разговор
                new Delay(20, 80),    // доставка
                new Delay(10, 50));   // уступка соседу
    }

    /** Аргументы: [программисты] [официанты] [еда] [maxLead]. Пропущенные берутся по умолчанию. */
    static Config fromArgs(String[] args) {
        Config d = defaults();
        return new Config(
                args.length > 0 ? Integer.parseInt(args[0]) : d.programmers(),
                args.length > 1 ? Integer.parseInt(args[1]) : d.waiters(),
                args.length > 2 ? Long.parseLong(args[2]) : d.food(),
                args.length > 3 ? Integer.parseInt(args[3]) : d.maxLead(),
                d.eat(), d.think(), d.serve(), d.backoff());
    }
}
