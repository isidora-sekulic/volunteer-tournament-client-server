package zahtevi;

import enums.Pozicija;
import enums.Smena;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class ZahtevIzmenaPrijave implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID idPrijave;
    private LocalDate noviDatumVolontiranja;
    private Smena novaSmena;
    private Pozicija novaPozicija;

    public ZahtevIzmenaPrijave() {
    }

    public UUID getIdPrijave() { 
        return idPrijave;
    }
    public void setIdPrijave(UUID idPrijave) { 
        this.idPrijave = idPrijave; 
    }

    public LocalDate getNoviDatumVolontiranja() { 
        return noviDatumVolontiranja;
    }
    public void setNoviDatumVolontiranja(LocalDate noviDatumVolontiranja) { 
        this.noviDatumVolontiranja = noviDatumVolontiranja; 
    }

    public Smena getNovaSmena() { 
        return novaSmena;
    }
    public void setNovaSmena(Smena novaSmena) {
        this.novaSmena = novaSmena;
    }

    public Pozicija getNovaPozicija() { 
        return novaPozicija; 
    }
    public void setNovaPozicija(Pozicija novaPozicija) { 
        this.novaPozicija = novaPozicija;
    }
}
