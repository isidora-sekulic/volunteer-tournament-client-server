package zahtevi;

import java.io.Serializable;
import java.util.UUID;

public class ZahtevOtkazivanjePrijave implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID idPrijave;

    public ZahtevOtkazivanjePrijave() {
    }

    public ZahtevOtkazivanjePrijave(UUID idPrijave) {
        this.idPrijave = idPrijave;
    }

    public UUID getIdPrijave() {
        return idPrijave;
    }

    public void setIdPrijave(UUID idPrijave) {
        this.idPrijave = idPrijave;
    }
}
