package enums;

import java.time.LocalTime;


public enum Smena {
    PREPODNEVNA(LocalTime.of(8, 0)),
    POPODNEVNA(LocalTime.of(14, 0)),
    VECERNJA(LocalTime.of(20, 0));

    private final LocalTime vremePocetka;

    Smena(LocalTime vremePocetka) {
        this.vremePocetka = vremePocetka;
    }

    public LocalTime getVremePocetka() {
        return vremePocetka;
    }
}
