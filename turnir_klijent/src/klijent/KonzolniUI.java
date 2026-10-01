package klijent;

import komunikacija.KonekcijaKlijenta;
import enums.Operacija;
import enums.Pozicija;
import enums.Smena;
import model.Korisnik;
import model.PrijavaVolontiranja;
import zahtevi.ZahtevIzmenaPrijave;
import zahtevi.ZahtevKreirajPrijavu;
import zahtevi.ZahtevListaPrijava;
import zahtevi.ZahtevOtkazivanjePrijave;
import zahtevi.ZahtevUloguj;
import transfer.KlijentskiZahtev;
import transfer.ServerskiOdgovor;
import util.PravilaDatuma;
import util.UtilStatusPrijave;
import validacija.Validatori;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import komunikacija.KonekcijaKlijenta;

public class KonzolniUI {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    
    private final KonekcijaKlijenta komunikacija;

    private final Scanner sc = new Scanner(System.in);
    private Korisnik ulogovan;

    public KonzolniUI() {
        
        this.komunikacija = KonekcijaKlijenta.getInstance();
    }

    public void pokreni() throws Exception {
        
        ServerskiOdgovor pocetni = komunikacija.primiOdgovor();

        System.out.println("\n=== TURNIR 2026 KLIJENT ===");
        if (pocetni != null) {
            System.out.println(pocetni.getPoruka());
        } else {
            System.out.println("Nije stigao pocetni odgovor sa servera.");
        }

        while (true) {
            prikaziMeni();
            String izbor = ucitaj("Izaberite opciju: ");

            if ("0".equals(izbor)) {
                bezbedanIzlaz();
                break;
            }

            switch (izbor) {
                case "1": registracija(); break;
                case "2": prijava(); break;
                case "3": prijavaZaVolontiranje(); break;
                case "4": pregledPrijava(); break;
                case "5": otkazivanjePrijave(); break;
                case "6": izmenaPrijave(); break;
                case "7": odjava(); break;
                default: System.out.println("Nepoznata opcija.");
            }
        }
    }

    private void prikaziMeni() {
        System.out.println("\n------------------------------");
        if (ulogovan == null) {
            System.out.println("(Niste prijavljeni)");
            System.out.println("1) Registracija");
            System.out.println("2) Login");
            System.out.println("3) Prijava za volontiranje (anonimno)");
            System.out.println("4) Pregled mojih prijava (JMBG + e-mail)");
            System.out.println("5) Otkazivanje prijave");
            System.out.println("6) Izmena prijave");
            System.out.println("0) Izlaz");
        } else {
            System.out.println("(Prijavljeni ste kao: " + ulogovan.getKorisnickoIme() + ")");
            System.out.println("3) Prijava za volontiranje");
            System.out.println("4) Pregled mojih prijava");
            System.out.println("5) Otkazivanje prijave");
            System.out.println("6) Izmena prijave");
            System.out.println("7) Logout");
            System.out.println("0) Izlaz");
        }
    }

   

    private void registracija() throws Exception {
        if (ulogovan != null) { System.out.println("Vec ste prijavljeni. Prvo uradite logout."); return; }

        System.out.println("\n--- REGISTRACIJA ---");
        Korisnik k = new Korisnik();
        k.setKorisnickoIme(ucitajNePrazno("Korisnicko ime: "));
        k.setLozinka(ucitajNePrazno("Lozinka: "));
        k.setIme(ucitajNePrazno("Ime: "));
        k.setPrezime(ucitajNePrazno("Prezime: "));
        k.setJmbg(ucitajValidanJmbg());
        k.setEmailAdresa(ucitajValidanEmail());

        ServerskiOdgovor o = izvrsi(Operacija.REGISTER, k);
        ispisiOdgovor(o);
    }

    private void prijava() throws Exception {
        if (ulogovan != null) { System.out.println("Vec ste prijavljeni."); return; }

        System.out.println("\n--- LOGIN ---");
        String korisnickoIme = ucitajNePrazno("Korisnicko ime: ");
        String lozinka = ucitajNePrazno("Lozinka: ");

        ServerskiOdgovor o = izvrsi(Operacija.LOGIN, new ZahtevUloguj(korisnickoIme, lozinka));
        ispisiOdgovor(o);

        if (o != null && o.isUspeh() && o.getPodaci() instanceof Korisnik) {
            ulogovan = (Korisnik) o.getPodaci();
        }
    }

    private void odjava() throws Exception {
        if (ulogovan == null) { System.out.println("Niste prijavljeni."); return; }

        ServerskiOdgovor o = izvrsi(Operacija.LOGOUT, null);
        ispisiOdgovor(o);

        if (o != null && o.isUspeh()) ulogovan = null;
    }

    private void prijavaZaVolontiranje() throws Exception {
        System.out.println("\n--- PRIJAVA ZA VOLONTIRANJE ---");

        ZahtevKreirajPrijavu z = new ZahtevKreirajPrijavu();
        if (ulogovan == null) {
            z.setIme(ucitajNePrazno("Ime: "));
            z.setPrezime(ucitajNePrazno("Prezime: "));
            z.setJmbg(ucitajValidanJmbg());
            z.setEmailAdresa(ucitajValidanEmail());
        } else {
            System.out.println("Ulogovani korisnik - licni podaci se uzimaju sa servera.");
        }

        z.setDatumVolontiranja(ucitajValidanDatumVolontiranja());
        z.setSmena(izaberiSmenu());
        z.setPozicija(izaberiPoziciju());

        ServerskiOdgovor o = izvrsi(Operacija.CREATE_APPLICATION, z);
        ispisiOdgovor(o);

        if (o != null && o.isUspeh()) {
            sacuvajPrilogAkoPostoji(o);
        }
    }

    private List<PrijavaVolontiranja> pregledPrijava() throws Exception {
        System.out.println("\n--- PREGLED PRIJAVA ---");

        ZahtevListaPrijava z;
        if (ulogovan == null) {
            z = new ZahtevListaPrijava(ucitajValidanJmbg(), ucitajValidanEmail());
        } else {
            z = new ZahtevListaPrijava(); 
        }

        ServerskiOdgovor o = izvrsi(Operacija.LIST_MY_APPLICATIONS, z);
        ispisiOdgovor(o);

        if (o == null || !o.isUspeh() || !(o.getPodaci() instanceof List)) return null;

        
        List<PrijavaVolontiranja> lista = (List<PrijavaVolontiranja>) o.getPodaci();

        if (lista.isEmpty()) {
            System.out.println("Nema prijava.");
            return lista;
        }

        System.out.println("\nID | Datum | Smena | Pozicija | Status");
        System.out.println("---------------------------------------------------------------");

        LocalDateTime sada = LocalDateTime.now();
        for (PrijavaVolontiranja p : lista) {
            String status = statusTekst(p, sada);
            String datum = p.getDatumVolontiranja() == null ? "" : p.getDatumVolontiranja().format(DATE_FMT);
            System.out.println(p.getId() + " | " + datum + " | " + p.getSmena() + " | " + p.getPozicija() + " | " + status);
        }

        return lista;
    }

    private void otkazivanjePrijave() throws Exception {
        System.out.println("\n--- OTKAZIVANJE PRIJAVE ---");
        List<PrijavaVolontiranja> lista = pregledPrijava();
        if (lista == null || lista.isEmpty()) return;

        UUID id = ucitajUUID("Unesite ID prijave za otkazivanje: ");
        ServerskiOdgovor o = izvrsi(Operacija.CANCEL_APPLICATION, new ZahtevOtkazivanjePrijave(id));
        ispisiOdgovor(o);
    }

    private void izmenaPrijave() throws Exception {
        System.out.println("\n--- IZMENA PRIJAVE ---");
        List<PrijavaVolontiranja> lista = pregledPrijava();
        if (lista == null || lista.isEmpty()) return;

        UUID id = ucitajUUID("Unesite ID prijave za izmenu: ");

        ZahtevIzmenaPrijave z = new ZahtevIzmenaPrijave();
        z.setIdPrijave(id);
        z.setNoviDatumVolontiranja(ucitajValidanDatumVolontiranja());
        z.setNovaSmena(izaberiSmenu());
        z.setNovaPozicija(izaberiPoziciju());

        ServerskiOdgovor o = izvrsi(Operacija.UPDATE_APPLICATION, z);
        ispisiOdgovor(o);
    }

    private void bezbedanIzlaz() throws Exception {
        ServerskiOdgovor o = izvrsi(Operacija.EXIT, null);
        
        if (o != null) System.out.println(o.getPoruka());
        System.out.println("Dovidjenja!");
    }

    

    private ServerskiOdgovor izvrsi(Operacija op, Object payload) throws Exception {
        KlijentskiZahtev kz = new KlijentskiZahtev(op, payload); 
        komunikacija.posaljiZahtev(kz);
        return komunikacija.primiOdgovor();
    }

    private void ispisiOdgovor(ServerskiOdgovor o) {
        if (o == null) {
            System.out.println("Nema odgovora sa servera (null).");
            return;
        }
        System.out.println(o.getPoruka());
    }

    //pomocne validacije

    private void sacuvajPrilogAkoPostoji(ServerskiOdgovor o) {
        if (o.getPrilogBajtovi() == null || o.getPrilogNazivFajla() == null) return;
        try {
            Path p = Paths.get(o.getPrilogNazivFajla());
            Files.write(p, o.getPrilogBajtovi());
            System.out.println("Sacuvan prilog: " + p.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Ne mogu da sacuvam prilog: " + e.getMessage());
        }
    }

    private String statusTekst(PrijavaVolontiranja p, LocalDateTime sada) {
        if (p.isOtkazano()) return "OTKAZANO";
        if (UtilStatusPrijave.daLiJeZakljucano(p, sada)) return "ZAKLJUCANO";
        return "AKTIVNO";
    }

    private String ucitaj(String poruka) {
        System.out.print(poruka);
        return sc.nextLine();
    }

    private String ucitajNePrazno(String poruka) {
        while (true) {
            String s = ucitaj(poruka);
            if (!Validatori.jePrazno(s)) return s.trim();
            System.out.println("Polje je obavezno.");
        }
    }

    private String ucitajValidanJmbg() {
        while (true) {
            String j = ucitajNePrazno("JMBG (13 cifara): ");
            if (Validatori.jeValidanJmbg(j)) return j.trim();
            System.out.println("Neispravan JMBG.");
        }
    }

    private String ucitajValidanEmail() {
        while (true) {
            String e = ucitajNePrazno("E-mail: ");
            if (Validatori.jeValidanEmail(e)) return e.trim();
            System.out.println("Neispravan e-mail.");
        }
    }

    private LocalDate ucitajValidanDatumVolontiranja() {
        while (true) {
            String s = ucitajNePrazno("Datum volontiranja (dd.MM.yyyy): ");
            try {
                LocalDate d = LocalDate.parse(s, DATE_FMT);
                if (PravilaDatuma.ValidanDatumVolontiranja(d, LocalDate.now())) return d;
                System.out.println("Datum mora biti od sutra do " + PravilaDatuma.AO2026_FINALNI_DATUM.format(DATE_FMT) + ".");
            } catch (Exception e) {
                System.out.println("Neispravan format datuma.");
            }
        }
    }

    private Smena izaberiSmenu() {
        System.out.println("Izaberite smenu:");
        Smena[] vrednosti = Smena.values();
        for (int i = 0; i < vrednosti.length; i++) {
            System.out.println((i + 1) + ") " + vrednosti[i]);
        }
        while (true) {
            String s = ucitaj("Opcija: ");
            try {
                int idx = Integer.parseInt(s) - 1;
                if (idx >= 0 && idx < vrednosti.length) return vrednosti[idx];
            } catch (Exception ignore) {}
            System.out.println("Neispravan izbor.");
        }
    }

    private Pozicija izaberiPoziciju() {
        System.out.println("Izaberite poziciju:");
        Pozicija[] vrednosti = Pozicija.values();
        for (int i = 0; i < vrednosti.length; i++) {
            System.out.println((i + 1) + ") " + vrednosti[i]);
        }
        while (true) {
            String s = ucitaj("Opcija: ");
            try {
                int idx = Integer.parseInt(s) - 1;
                if (idx >= 0 && idx < vrednosti.length) return vrednosti[idx];
            } catch (Exception ignore) {}
            System.out.println("Neispravan izbor.");
        }
    }

    private UUID ucitajUUID(String poruka) {
        while (true) {
            String s = ucitajNePrazno(poruka);
            try { return UUID.fromString(s.trim()); }
            catch (Exception e) { System.out.println("Neispravan UUID."); }
        }
    }
}
