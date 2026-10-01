package server;

import baza.UpitiKorisnik;
import baza.UpitiPrijava;
import enums.Pozicija;
import enums.Smena;
import model.Korisnik;
import model.PrijavaVolontiranja;
import transfer.ServerskiOdgovor;
import zahtevi.ZahtevKreirajPrijavu;
import zahtevi.ZahtevIzmenaPrijave;
import zahtevi.ZahtevUloguj;
import util.PravilaDatuma;
import util.UtilStatusPrijave;
import validacija.Validatori;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ServerKontroler {

    private UpitiKorisnik korisnikUpit;
    private UpitiPrijava prijavaUpit;

   
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ServerKontroler(UpitiKorisnik korisnikUpit, UpitiPrijava prijavaUpit) {
        this.korisnikUpit = korisnikUpit;
        this.prijavaUpit = prijavaUpit;
    }

    public ServerskiOdgovor getWelcomeOdgovor() {
        List<String> opcije = new ArrayList<>();
        opcije.add("1) Registracija");
        opcije.add("2) Prijava (login)");
        opcije.add("3) Prijava za volontiranje");
        opcije.add("4) Pregled mojih prijava");
        opcije.add("5) Otkazivanje prijave");
        opcije.add("6) Izmena prijave");
        opcije.add("7) Odjava (logout)");
        opcije.add("0) Izlaz");

        ServerskiOdgovor o = ServerskiOdgovor.uspeh("Uspesno povezivanje na Turnir2026 server. Opcije su u polju 'podaci'.");
        o.setPodaci(opcije);
        return o;
    }

    public ServerskiOdgovor registruj(Korisnik k) {
        if (k == null) return ServerskiOdgovor.greska("Nedostaju podaci.");

        if (Validatori.jePrazno(k.getKorisnickoIme()) || Validatori.jePrazno(k.getLozinka())
                || Validatori.jePrazno(k.getIme()) || Validatori.jePrazno(k.getPrezime())
                || Validatori.jePrazno(k.getJmbg()) || Validatori.jePrazno(k.getEmailAdresa())) {
            return ServerskiOdgovor.greska("Sva polja su obavezna.");
        }
        if (!Validatori.jeValidanJmbg(k.getJmbg())) {
            return ServerskiOdgovor.greska("JMBG mora imati tacno 13 cifara.");
        }
        if (!Validatori.jeValidanEmail(k.getEmailAdresa())) {
            return ServerskiOdgovor.greska("Neispravan format e-mail adrese.");
        }

        String korisnickoIme = k.getKorisnickoIme().trim();
        k.setKorisnickoIme(korisnickoIme);
        k.setEmailAdresa(normalizujEmail(k.getEmailAdresa()));

        try {
            
                Korisnik postojeci = korisnikUpit.nadjiPoKorisnickomImenu(korisnickoIme);
                if (postojeci != null) {
                    return ServerskiOdgovor.greska("Korisnicko ime je zauzeto.");
                }
                korisnikUpit.sacuvaj(k);
            
            return ServerskiOdgovor.uspeh("Registracija uspesna. Sada mozete da se prijavite (login).");
        } catch (Exception e) {
            return ServerskiOdgovor.greska("Greska pri radu sa bazom: " + e.getMessage());
        }
    }

    public Korisnik autentifikuj(ZahtevUloguj req) {
        if (req == null) return null;
        if (Validatori.jePrazno(req.getKorisnickoIme()) || Validatori.jePrazno(req.getLozinka())) return null;

        try {
            Korisnik k = korisnikUpit.nadjiPoKorisnickomImenu(req.getKorisnickoIme().trim());
            if (k == null) return null;
            if (!req.getLozinka().equals(k.getLozinka())) return null;
            return k;
        } catch (Exception e) {
            return null;
        }
    }

    public ServerskiOdgovor kreirajPrijavu(ZahtevKreirajPrijavu req, Korisnik uSesiji) {
        if (req == null) return ServerskiOdgovor.greska("Nedostaju podaci.");

        String ime;
        String prezime;
        String jmbg;
        String email;
        String korisnickoIme = null;

        if (uSesiji != null) {
            ime = uSesiji.getIme();
            prezime = uSesiji.getPrezime();
            jmbg = uSesiji.getJmbg();
            email = uSesiji.getEmailAdresa();
            korisnickoIme = uSesiji.getKorisnickoIme();
        } else {
            ime = req.getIme();
            prezime = req.getPrezime();
            jmbg = req.getJmbg();
            email = req.getEmailAdresa();
        }

        if (Validatori.jePrazno(ime) || Validatori.jePrazno(prezime)
                || Validatori.jePrazno(jmbg) || Validatori.jePrazno(email)) {
            return ServerskiOdgovor.greska("Ime, prezime, JMBG i e-mail su obavezni.");
        }
        if (!Validatori.jeValidanJmbg(jmbg)) {
            return ServerskiOdgovor.greska("JMBG mora imati tacno 13 cifara.");
        }
        if (!Validatori.jeValidanEmail(email)) {
            return ServerskiOdgovor.greska("Neispravan format e-mail adrese.");
        }

        LocalDate datum = req.getDatumVolontiranja();
        Smena smena = req.getSmena();
        Pozicija pozicija = req.getPozicija();

        if (datum == null || smena == null || pozicija == null) {
            return ServerskiOdgovor.greska("Datum, smena i pozicija su obavezni.");
        }
        if (!PravilaDatuma.ValidanDatumVolontiranja(datum, LocalDate.now())) {
            return ServerskiOdgovor.greska("Datum volontiranja mora biti od sutra do "
                    + PravilaDatuma.AO2026_FINALNI_DATUM.format(DATE_FMT) + ".");
        }

        String identitet = kljucIdentiteta(jmbg, email);

        PrijavaVolontiranja p = new PrijavaVolontiranja();
        p.setId(UUID.randomUUID());
        p.setKorisnickoIme(korisnickoIme);
        p.setIme(ime.trim());
        p.setPrezime(prezime.trim());
        p.setJmbg(jmbg.trim());
        p.setEmailAdresa(normalizujEmail(email));
        p.setDatumVolontiranja(datum);
        p.setSmena(smena);
        p.setPozicija(pozicija);
        p.setKreirano(LocalDateTime.now());
        p.setOtkazano(false);

        try {
            
                List<PrijavaVolontiranja> sve = prijavaUpit.ucitajSve();

               
                for (int i = 0; i < sve.size(); i++) {
                    PrijavaVolontiranja a = sve.get(i);
                    if (a == null) continue;

                    if (!a.isOtkazano()) {
                        String idA = kljucIdentiteta(a.getJmbg(), a.getEmailAdresa());
                        if (identitet.equals(idA)) {
                            if (datum.equals(a.getDatumVolontiranja())) {
                                return ServerskiOdgovor.greska("Vec imate prijavu za taj datum. Jedna prijava po danu.");
                            }
                        }
                    }
                }

                
                List<Pozicija> dozvoljene = izracunajDozvoljenePozicije(sve, identitet);
                if (dozvoljene.size() >= 2 && !listaSadrziPoziciju(dozvoljene, pozicija)) {
                    return ServerskiOdgovor.greska("Mozete birati samo prve dve razlicite pozicije koje ste do sada naveli: " + dozvoljene);
                }

                //Limit smena
                long vecernje = 0;
                long popodnevne = 0;

                for (int i = 0; i < sve.size(); i++) {
                    PrijavaVolontiranja a = sve.get(i);
                    if (a == null) continue;
                    if (a.isOtkazano()) continue;

                    String idA = kljucIdentiteta(a.getJmbg(), a.getEmailAdresa());
                    if (!identitet.equals(idA)) continue;

                    if (a.getSmena() == Smena.VECERNJA) vecernje++;
                    if (a.getSmena() == Smena.POPODNEVNA) popodnevne++;
                }

                if (smena == Smena.VECERNJA && vecernje >= 3) {
                    return ServerskiOdgovor.greska("Prekoracen limit: maksimalno 3 vecernje smene po korisniku.");
                }
                if (smena == Smena.POPODNEVNA && popodnevne >= 5) {
                    return ServerskiOdgovor.greska("Prekoracen limit: maksimalno 5 popodnevnih smena po korisniku.");
                }

                prijavaUpit.sacuvaj(p);
            
        } catch (Exception e) {
            return ServerskiOdgovor.greska("Greska pri radu sa bazom: " + e.getMessage());
        }

        ServerskiOdgovor o = ServerskiOdgovor.uspeh("Prijava je sacuvana. Dobijate fajl sa podacima prijave.");
        o.setPodaci(p);

        String fajl = PotvrdaFajl(p);
        o.setPrilogBajtovi(fajl.getBytes(StandardCharsets.UTF_8));
        o.setPrilogNazivFajla(NazivPotvrde(p));
        return o;
    }

    public ServerskiOdgovor listaMojihPrijava(String jmbg, String email, Korisnik uSesiji) {
        if (uSesiji != null) {
            jmbg = uSesiji.getJmbg();
            email = uSesiji.getEmailAdresa();
        }

        if (Validatori.jePrazno(jmbg) || Validatori.jePrazno(email)) {
            return ServerskiOdgovor.greska("Morate uneti JMBG i e-mail.");
        }
        if (!Validatori.jeValidanJmbg(jmbg) || !Validatori.jeValidanEmail(email)) {
            return ServerskiOdgovor.greska("Neispravan JMBG ili e-mail.");
        }

        String identitet = kljucIdentiteta(jmbg, email);

        try {
            List<PrijavaVolontiranja> sve = prijavaUpit.ucitajSve();
            List<PrijavaVolontiranja> moje = new ArrayList<>();

            for (int i = 0; i < sve.size(); i++) {
                PrijavaVolontiranja a = sve.get(i);
                if (a == null) continue;

                String idA = kljucIdentiteta(a.getJmbg(), a.getEmailAdresa());
                if (identitet.equals(idA)) {
                    moje.add(a);
                }
            }

            
            for (int i = 0; i < moje.size() - 1; i++) {
                for (int j = i + 1; j < moje.size(); j++) {
                    PrijavaVolontiranja p1 = moje.get(i);
                    PrijavaVolontiranja p2 = moje.get(j);

                    LocalDate d1 = p1.getDatumVolontiranja();
                    LocalDate d2 = p2.getDatumVolontiranja();

                    boolean menjaj = false;

                    if (d1 != null && d2 != null) {
                        if (d1.isAfter(d2)) menjaj = true;
                        else if (d1.equals(d2)) {
                            String s1 = (p1.getSmena() == null) ? "" : p1.getSmena().name();
                            String s2 = (p2.getSmena() == null) ? "" : p2.getSmena().name();
                            if (s1.compareTo(s2) > 0) menjaj = true;
                        }
                    }

                    if (menjaj) {
                        moje.set(i, p2);
                        moje.set(j, p1);
                    }
                }
            }

            ServerskiOdgovor o = ServerskiOdgovor.uspeh("Pronadjeno prijava: " + moje.size());
            o.setPodaci(moje);
            return o;

        } catch (Exception e) {
            return ServerskiOdgovor.greska("Greska pri radu sa bazom: " + e.getMessage());
        }
    }

    public ServerskiOdgovor otkaziPrijavu(UUID idPrijave, String jmbg, String email, Korisnik uSesiji) {
        if (uSesiji != null) {
            jmbg = uSesiji.getJmbg();
            email = uSesiji.getEmailAdresa();
        }
        if (idPrijave == null) return ServerskiOdgovor.greska("Nedostaje ID prijave.");

        if (uSesiji == null) {
            if (Validatori.jePrazno(jmbg) || Validatori.jePrazno(email)) {
                return ServerskiOdgovor.greska("Morate uneti JMBG i e-mail (preko opcije 'Pregled mojih prijava') ili se ulogovati.");
            }
            if (!Validatori.jeValidanJmbg(jmbg) || !Validatori.jeValidanEmail(email)) {
                return ServerskiOdgovor.greska("Neispravan JMBG ili e-mail.");
            }
        }

        String identitet = kljucIdentiteta(jmbg, email);

        try {
            
                List<PrijavaVolontiranja> sve = prijavaUpit.ucitajSve();
                PrijavaVolontiranja target = null;

                for (int i = 0; i < sve.size(); i++) {
                    PrijavaVolontiranja p = sve.get(i);
                    if (p == null) continue;

                    if (idPrijave.equals(p.getId())) {
                        String idP = kljucIdentiteta(p.getJmbg(), p.getEmailAdresa());
                        if (identitet.equals(idP)) {
                            target = p;
                            break;
                        }
                    }
                }

                if (target == null) return ServerskiOdgovor.greska("Prijava nije pronadjena (proverite ID).");
                if (target.isOtkazano()) return ServerskiOdgovor.greska("Prijava je vec otkazana.");

                prijavaUpit.postaviOtkazano(idPrijave, true);
                return ServerskiOdgovor.uspeh("Prijava je uspesno otkazana.");
            
        } catch (Exception e) {
            return ServerskiOdgovor.greska("Greska pri radu sa bazom: " + e.getMessage());
        }
    }

    public ServerskiOdgovor izmeniPrijavu(ZahtevIzmenaPrijave req, String jmbg, String email, Korisnik uSesiji) {
        if (uSesiji != null) {
            jmbg = uSesiji.getJmbg();
            email = uSesiji.getEmailAdresa();
        }
        if (req == null || req.getIdPrijave() == null) return ServerskiOdgovor.greska("Nedostaje ID prijave.");

        if (req.getNoviDatumVolontiranja() == null || req.getNovaSmena() == null || req.getNovaPozicija() == null) {
            return ServerskiOdgovor.greska("Morate uneti novi datum, smenu i poziciju.");
        }

        if (uSesiji == null) {
            if (Validatori.jePrazno(jmbg) || Validatori.jePrazno(email)) {
                return ServerskiOdgovor.greska("Morate uneti JMBG i e-mail (preko opcije 'Pregled mojih prijava') ili se ulogovati.");
            }
            if (!Validatori.jeValidanJmbg(jmbg) || !Validatori.jeValidanEmail(email)) {
                return ServerskiOdgovor.greska("Neispravan JMBG ili e-mail.");
            }
        }

        String identitet = kljucIdentiteta(jmbg, email);

        LocalDate noviDatum = req.getNoviDatumVolontiranja();
        Smena novaSmena = req.getNovaSmena();
        Pozicija novaPozicija = req.getNovaPozicija();

        if (!PravilaDatuma.ValidanDatumVolontiranja(noviDatum, LocalDate.now())) {
            return ServerskiOdgovor.greska("Datum volontiranja mora biti od sutra do "
                    + PravilaDatuma.AO2026_FINALNI_DATUM.format(DATE_FMT) + ".");
        }

        try {
            
                List<PrijavaVolontiranja> sve = prijavaUpit.ucitajSve();

                PrijavaVolontiranja target = null;

                
                for (int i = 0; i < sve.size(); i++) {
                    PrijavaVolontiranja p = sve.get(i);
                    if (p == null) continue;

                    if (req.getIdPrijave().equals(p.getId())) {
                        String idP = kljucIdentiteta(p.getJmbg(), p.getEmailAdresa());
                        if (identitet.equals(idP)) {
                            target = p;
                            break;
                        }
                    }
                }

                if (target == null) return ServerskiOdgovor.greska("Prijava nije pronadjena (proverite ID).");
                if (target.isOtkazano()) return ServerskiOdgovor.greska("Ne mozete menjati otkazanu prijavu.");

                if (UtilStatusPrijave.daLiJeZakljucano(target, LocalDateTime.now())) {
                    return ServerskiOdgovor.greska("Prijava je zakljucana (manje od 24h do smene). Izmena nije dozvoljena.");
                }

                
                for (int i = 0; i < sve.size(); i++) {
                    PrijavaVolontiranja a = sve.get(i);
                    if (a == null) continue;
                    if (a.isOtkazano()) continue;

                    if (req.getIdPrijave().equals(a.getId())) continue;

                    String idA = kljucIdentiteta(a.getJmbg(), a.getEmailAdresa());
                    if (identitet.equals(idA)) {
                        if (noviDatum.equals(a.getDatumVolontiranja())) {
                            return ServerskiOdgovor.greska("Vec imate prijavu za taj datum. Jedna prijava po danu.");
                        }
                    }
                }

                
                List<Pozicija> dozvoljene = izracunajDozvoljenePozicije(sve, identitet);
                if (dozvoljene.size() >= 2 && !listaSadrziPoziciju(dozvoljene, novaPozicija)) {
                    return ServerskiOdgovor.greska("Mozete birati samo prve dve razlicite pozicije koje ste do sada naveli: " + dozvoljene);
                }

                
                long vecernje = 0;
                long popodnevne = 0;

                for (int i = 0; i < sve.size(); i++) {
                    PrijavaVolontiranja a = sve.get(i);
                    if (a == null) continue;
                    if (a.isOtkazano()) continue;

                    if (req.getIdPrijave().equals(a.getId())) continue;

                    String idA = kljucIdentiteta(a.getJmbg(), a.getEmailAdresa());
                    if (!identitet.equals(idA)) continue;

                    if (a.getSmena() == Smena.VECERNJA) vecernje++;
                    if (a.getSmena() == Smena.POPODNEVNA) popodnevne++;
                }

                if (novaSmena == Smena.VECERNJA && vecernje >= 3) {
                    return ServerskiOdgovor.greska("Prekoracen limit: maksimalno 3 vecernje smene po korisniku.");
                }
                if (novaSmena == Smena.POPODNEVNA && popodnevne >= 5) {
                    return ServerskiOdgovor.greska("Prekoracen limit: maksimalno 5 popodnevnih smena po korisniku.");
                }

                boolean ok = prijavaUpit.izmeni(req.getIdPrijave(), noviDatum, novaSmena, novaPozicija);
                if (!ok) return ServerskiOdgovor.greska("Ne mogu da izmenim prijavu (nije pronadjena).");

                // ucitaj izmenjenu prijavu
                List<PrijavaVolontiranja> posle = prijavaUpit.ucitajSve();
                PrijavaVolontiranja izmenjena = null;
                for (int i = 0; i < posle.size(); i++) {
                    PrijavaVolontiranja pp = posle.get(i);
                    if (pp == null) continue;
                    if (req.getIdPrijave().equals(pp.getId())) {
                        izmenjena = pp;
                        break;
                    }
                }

                ServerskiOdgovor o = ServerskiOdgovor.uspeh("Prijava je uspesno izmenjena.");
                o.setPodaci(izmenjena);
                return o;
            
        } catch (Exception e) {
            return ServerskiOdgovor.greska("Greska pri radu sa bazom: " + e.getMessage());
        }
    }

    

    private List<Pozicija> izracunajDozvoljenePozicije(List<PrijavaVolontiranja> sve, String identitet) {
        
        List<PrijavaVolontiranja> moje = new ArrayList<>();
        for (int i = 0; i < sve.size(); i++) {
            PrijavaVolontiranja a = sve.get(i);
            if (a == null) continue;
            String idA = kljucIdentiteta(a.getJmbg(), a.getEmailAdresa());
            if (identitet.equals(idA)) {
                moje.add(a);
            }
        }

        
        for (int i = 0; i < moje.size() - 1; i++) {
            for (int j = i + 1; j < moje.size(); j++) {
                PrijavaVolontiranja p1 = moje.get(i);
                PrijavaVolontiranja p2 = moje.get(j);

                LocalDateTime t1 = p1.getKreirano();
                LocalDateTime t2 = p2.getKreirano();

                boolean menjaj = false;

                if (t1 == null && t2 != null) menjaj = false;
                else if (t1 != null && t2 == null) menjaj = true;
                else if (t1 != null && t2 != null) {
                    if (t1.isAfter(t2)) menjaj = true;
                }

                if (menjaj) {
                    moje.set(i, p2);
                    moje.set(j, p1);
                }
            }
        }

        // uzmi prve 2 razlicite pozicije
        List<Pozicija> rez = new ArrayList<>();
        for (int i = 0; i < moje.size(); i++) {
            PrijavaVolontiranja p = moje.get(i);
            if (p.getPozicija() == null) continue;

            if (!listaSadrziPoziciju(rez, p.getPozicija())) {
                rez.add(p.getPozicija());
                if (rez.size() == 2) break;
            }
        }
        return rez;
    }

    private boolean listaSadrziPoziciju(List<Pozicija> lista, Pozicija poz) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i) == poz) return true;
        }
        return false;
    }

    private String kljucIdentiteta(String jmbg, String email) {
        String j = (jmbg == null) ? "" : jmbg.trim();
        String e = (email == null) ? "" : normalizujEmail(email);
        return j + "|" + e;
    }

    private String normalizujEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String NazivPotvrde(PrijavaVolontiranja p) {
        return "potvrda_prijave_" + p.getId() + ".txt";
    }

    private String PotvrdaFajl(PrijavaVolontiranja p) {
        StringBuilder sb = new StringBuilder();
        sb.append("POTVRDA PRIJAVE ZA VOLONTIRANJE (TURNIR 2026)\n");
        sb.append("-------------------------------------------\n");
        sb.append("ID prijave: ").append(p.getId()).append("\n");
        sb.append("Ime i prezime: ").append(p.getIme()).append(" ").append(p.getPrezime()).append("\n");
        sb.append("JMBG: ").append(p.getJmbg()).append("\n");
        sb.append("Email: ").append(p.getEmailAdresa()).append("\n");
        sb.append("Datum volontiranja: ").append(p.getDatumVolontiranja().format(DATE_FMT)).append("\n");
        sb.append("Smena: ").append(p.getSmena()).append("\n");
        sb.append("Pozicija: ").append(p.getPozicija()).append("\n");
        sb.append("Kreirano: ").append(p.getKreirano().format(DATETIME_FMT)).append("\n");
        return sb.toString();
    }
}
