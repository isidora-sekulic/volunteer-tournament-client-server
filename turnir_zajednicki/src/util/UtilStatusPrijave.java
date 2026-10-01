package util;

import model.PrijavaVolontiranja;

import java.time.Duration;
import java.time.LocalDateTime;


public class UtilStatusPrijave {
    
    private UtilStatusPrijave() {
    }

    public static boolean daLiJeZakljucano(PrijavaVolontiranja p, LocalDateTime sada) {
        if (p == null || sada == null) return false;
        LocalDateTime pocetak = p.getDatumVremePocetkaSmene();
        if (pocetak == null) return false;
        long sati = Duration.between(sada, pocetak).toHours();
        return sati < 24;
    }
}
