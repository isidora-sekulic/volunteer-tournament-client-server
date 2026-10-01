package zahtevi;

import java.io.Serializable;

public class ZahtevUloguj implements Serializable {
    private static final long serialVersionUID = 1L;

    private String korisnickoIme;
    private String lozinka;

    public ZahtevUloguj() {
    }

    public ZahtevUloguj(String korisnickoIme, String lozinka) {
        this.korisnickoIme = korisnickoIme;
        this.lozinka = lozinka;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }
}
