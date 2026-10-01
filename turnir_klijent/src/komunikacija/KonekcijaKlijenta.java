package komunikacija;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import transfer.KlijentskiZahtev;
import transfer.ServerskiOdgovor;

public class KonekcijaKlijenta {

    private static KonekcijaKlijenta instance;
    private Socket s;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private KonekcijaKlijenta() {
        try {
            s = new Socket("localhost", 9000);
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KonekcijaKlijenta getInstance() {
        if (instance == null) {
            instance = new KonekcijaKlijenta();
        }
        return instance;
    }

    public void posaljiZahtev(KlijentskiZahtev kz) {
        try {
            oos.writeObject(kz);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerskiOdgovor primiOdgovor() {
        try {
            return (ServerskiOdgovor) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

