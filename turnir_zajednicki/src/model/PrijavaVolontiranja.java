package model;

import enums.Pozicija;
import enums.Smena;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public class PrijavaVolontiranja implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String korisnickoIme; // null ako je anoniman korisnik

    private String ime;
    private String prezime;
    private String jmbg;
    private String emailAdresa;

    private LocalDate datumVolontiranja;
    private Smena smena;
    private Pozicija pozicija;

    private LocalDateTime kreirano;
    private boolean otkazano;

    public PrijavaVolontiranja() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
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

    public LocalDateTime getKreirano() {
        return kreirano;
    }

    public void setKreirano(LocalDateTime kreirano) {
        this.kreirano = kreirano;
    }

    public boolean isOtkazano() {
        return otkazano;
    }

    public void setOtkazano(boolean otkazano) {
        this.otkazano = otkazano;
    }

    public LocalDateTime getDatumVremePocetkaSmene() {
        if (datumVolontiranja == null || smena == null) {
            return null;
        }
        return LocalDateTime.of(datumVolontiranja, smena.getVremePocetka());
    }

    @Override
    public String toString() {
        return "PrijavaVolontiranja{" +
                "id=" + id +
                ", korisnickoIme='" + korisnickoIme + '\'' +
                ", ime='" + ime + '\'' +
                ", prezime='" + prezime + '\'' +
                ", jmbg='" + jmbg + '\'' +
                ", emailAdresa='" + emailAdresa + '\'' +
                ", datumVolontiranja=" + datumVolontiranja +
                ", smena=" + smena +
                ", pozicija=" + pozicija +
                ", kreirano=" + kreirano +
                ", otkazano=" + otkazano +
                '}';
    }
}
