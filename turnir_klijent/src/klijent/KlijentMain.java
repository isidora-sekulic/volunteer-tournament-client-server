package klijent;


public class KlijentMain {

    public static void main(String[] args) {
        try {
            
            KonzolniUI ui = new KonzolniUI();
            ui.pokreni();

        } catch (Exception e) {
            System.err.println("Ne mogu da se povezem na server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

