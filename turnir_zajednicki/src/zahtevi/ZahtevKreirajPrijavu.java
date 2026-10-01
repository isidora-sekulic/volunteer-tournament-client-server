package zahtevi;

import enums.Pozicija;
import enums.Smena;

import java.io.Serializable;
import java.time.LocalDate;

public class ZahtevKreirajPrijavu implements Serializable {
    private static final long serialVersionUID = 1L;

    
    
    private String ime;
    private String prezime;
    private String jmbg;
    private String emailAdresa;

    private LocalDate datumVolontiranja;
    private Smena smena;
    private Pozicija pozicija;

    public ZahtevKreirajPrijavu() {
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() { 
        return prezime; 
    }
    public void setPrezime(String prezime) { 
        this.prezime = prezime; 
    }

    public String getJmbg() { 
        return jmbg;
    }
    public void setJmbg(String jmbg) {
        this.jmbg = jmbg; 
    }

    public String getEmailAdresa() { 
        return emailAdresa; 
    }
    public void setEmailAdresa(String emailAdresa) { 
        this.emailAdresa = emailAdresa;
    }

    public LocalDate getDatumVolontiranja() { 
        return datumVolontiranja; 
    }
    public void setDatumVolontiranja(LocalDate datumVolontiranja) {
        this.datumVolontiranja = datumVolontiranja; 
    }

    public Smena getSmena() {
        return smena;
    }
    public void setSmena(Smena smena) {
        this.smena = smena;
    }

    public Pozicija getPozicija() { 
        return pozicija;
    }
    public void setPozicija(Pozicija pozicija) {
        this.pozicija = pozicija;
    }
}
