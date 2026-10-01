package server;

import model.Korisnik;
import enums.Operacija;
import transfer.ServerskiOdgovor;
import enums.StatusOdgovora;
import transfer.KlijentskiZahtev;
import zahtevi.ZahtevIzmenaPrijave;
import zahtevi.ZahtevKreirajPrijavu;
import zahtevi.ZahtevListaPrijava;
import zahtevi.ZahtevOtkazivanjePrijave;
import zahtevi.ZahtevUloguj;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ObradaKlijentskogZahteva extends Thread {

    private final Socket socket;
    private final ServerKontroler kontroler;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Korisnik korisnikUSesiji;
    private String poslednjiJmbg;
    private String poslednjiEmail;

    public ObradaKlijentskogZahteva(Socket socket, ServerKontroler kontroler) {
        this.socket = socket;
        this.kontroler = kontroler;
        setName("ObradaKlijenta-" + socket.getPort());
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            posalji(kontroler.getWelcomeOdgovor());

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof KlijentskiZahtev)) {
                    posalji(new ServerskiOdgovor(StatusOdgovora.ERROR, "Neispravan zahtev."));
                    continue;
                }
                KlijentskiZahtev zahtev = (KlijentskiZahtev) obj;

                ServerskiOdgovor odgovor;
                try {
                    odgovor = obradi(zahtev);
                } catch (Exception e) {
                    odgovor = new ServerskiOdgovor(StatusOdgovora.ERROR, "Greska na serveru: " + e.getMessage());
                }

                posalji(odgovor);

                if (zahtev.getOperacija() == Operacija.EXIT) {
                    break;
                }
            }
        } catch (EOFException eof) {
            System.out.println("[SERVER] Klijent prekinuo vezu: " + socket.getInetAddress() + ":" + socket.getPort());
        } catch (Exception e) {
            System.err.println("[SERVER] Greska u radu sa klijentom: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignore) {}
        }
    }

    private ServerskiOdgovor obradi(KlijentskiZahtev zahtev) {
        if (zahtev.getOperacija() == null) {
            return new ServerskiOdgovor(StatusOdgovora.ERROR, "Nedostaje operacija.");
        }

        switch (zahtev.getOperacija()) {
            case CONNECT:
                return kontroler.getWelcomeOdgovor();

            case REGISTER:
                return kontroler.registruj((Korisnik) zahtev.getPayload());

            case LOGIN: {
                ZahtevUloguj lp = (ZahtevUloguj) zahtev.getPayload();
                Korisnik k = kontroler.autentifikuj(lp);
                if (k == null) {
                    return new ServerskiOdgovor(StatusOdgovora.ERROR, "Pogresno korisnicko ime ili lozinka.");
                }
                korisnikUSesiji = k;

                
                Korisnik bezLozinke = new Korisnik();
                bezLozinke.setKorisnickoIme(k.getKorisnickoIme());
                bezLozinke.setIme(k.getIme());
                bezLozinke.setPrezime(k.getPrezime());
                bezLozinke.setJmbg(k.getJmbg());
                bezLozinke.setEmailAdresa(k.getEmailAdresa());

                ServerskiOdgovor o = new ServerskiOdgovor(StatusOdgovora.OK, "Uspesno ste prijavljeni.");
                o.setPodaci(bezLozinke);
                return o;
            }

            case LOGOUT:
                korisnikUSesiji = null;
                return new ServerskiOdgovor(StatusOdgovora.OK, "Odjavljeni ste.");

            case CREATE_APPLICATION: {
                ZahtevKreirajPrijavu z = (ZahtevKreirajPrijavu) zahtev.getPayload();
                if (korisnikUSesiji == null && z != null) {
                    poslednjiJmbg = z.getJmbg();
                    poslednjiEmail = z.getEmailAdresa();
                }
                return kontroler.kreirajPrijavu(z, korisnikUSesiji);
            }

            case LIST_MY_APPLICATIONS: {
                ZahtevListaPrijava z = (ZahtevListaPrijava) zahtev.getPayload();
                if (korisnikUSesiji == null && z != null) {
                    poslednjiJmbg = z.getJmbg();
                    poslednjiEmail = z.getEmailAdresa();
                }
                return kontroler.listaMojihPrijava(
                        z == null ? null : z.getJmbg(),
                        z == null ? null : z.getEmailAdresa(),
                        korisnikUSesiji
                );
            }

            case CANCEL_APPLICATION: {
                ZahtevOtkazivanjePrijave z = (ZahtevOtkazivanjePrijave) zahtev.getPayload();
                return kontroler.otkaziPrijavu(
                        z == null ? null : z.getIdPrijave(),
                        korisnikUSesiji == null ? poslednjiJmbg : null,
                        korisnikUSesiji == null ? poslednjiEmail : null,
                        korisnikUSesiji
                );
            }

            case UPDATE_APPLICATION: {
                ZahtevIzmenaPrijave z = (ZahtevIzmenaPrijave) zahtev.getPayload();
                return kontroler.izmeniPrijavu(
                        z,
                        korisnikUSesiji == null ? poslednjiJmbg : null,
                        korisnikUSesiji == null ? poslednjiEmail : null,
                        korisnikUSesiji
                );
            }

            case EXIT:
                return new ServerskiOdgovor(StatusOdgovora.OK, "Dovidjenja!");

            default:
                return new ServerskiOdgovor(StatusOdgovora.ERROR, "Nepoznata operacija: " + zahtev.getOperacija());
        }
    }

    private void posalji(ServerskiOdgovor odgovor) {
        try {
            out.writeObject(odgovor);
            out.flush();
        } catch (Exception e) {
            System.err.println("[SERVER] Ne mogu da posaljem odgovor: " + e.getMessage());
        }
    }
}
