package server;

import server.ServerKontroler;
import server.ObradaKlijentskogZahteva;
import baza.KonekcijaBaze;
import baza.UpitiKorisnik;
import baza.UpitiPrijava;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        
        int port = 9000;
        
        KonekcijaBaze kb = new KonekcijaBaze();
        UpitiKorisnik korisnikUpit = new UpitiKorisnik(kb);
        UpitiPrijava prijavaUpit = new UpitiPrijava(kb);

        ServerKontroler kontroler = new ServerKontroler(korisnikUpit, prijavaUpit);

        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server pokrenut na portu " + port);
            while (true) {
                Socket client = ss.accept();
                System.out.println("Novi klijent: " + client.getInetAddress() + ":" + client.getPort());
                new ObradaKlijentskogZahteva(client, kontroler).start();
            }
        } catch (Exception e) {
            System.err.println("Greska " + e.getMessage());
            e.printStackTrace();
        }
    }
}
