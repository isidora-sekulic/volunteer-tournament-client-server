package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class PravilaDatuma {
    
    private PravilaDatuma() {
        
    }

    public static final LocalDate AO2026_FINALNI_DATUM = LocalDate.of(2026, 2, 5);

    public static boolean ValidanDatumVolontiranja(LocalDate datumVolontiranja, LocalDate danas) {
        if (datumVolontiranja == null || danas == null) return false;
        LocalDate sutra = danas.plusDays(1);
        return (!datumVolontiranja.isBefore(sutra)) && (!datumVolontiranja.isAfter(AO2026_FINALNI_DATUM));
    }

    public static String formatiraj(LocalDate d) {
        if (d == null) return "";
        return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
