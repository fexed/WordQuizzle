package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.WQInterface;
import com.fexed.lprb.wq.WQUtente;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Il server di WordQuizzle
 * @author Federico Matteoni
 */
public class WQServer extends RemoteServer implements WQInterface {
    /**
     * Flag per tenere il server in esecuzione o avviare la procedura di spegnimento
     */
    private boolean running;

    /**
     * Gli utenti registrati
     */
    private HashMap<String, WQUtente> userBase;

    /**
     * Gli utenti attualmente collegati
     */
    private HashMap<String, WQHandler> loggedIn;

    /**
     * La porta di ascolto del server
     */
    private int port;

    /**
     * Timestamp del momento in cui il server è andato online
     */
    private long onlineSince;

    /**
     * Operazione per inserire un nuovo utente.
     * @param nickUtente Il nickname dell'utente da aggiungere
     * @param password La password dell'utente da aggiungere
     * @return 0 se l'aggiunta va a buon fine, -1 se l'utente è già esistente, -2 se la password è vuota
     * @throws RemoteException
     */
    @Override
    public synchronized int registraUtente(String nickUtente, String password) throws RemoteException {
        if (userBase.containsKey(nickUtente.toLowerCase())) { //Verifica se l'utente non è già registrato
            return -1;
        } else if (password.equals("")) { //Verifica che la password non sia vuota
            WQServerController.gui.updateStatsText("Tentativo di registrare l'utente \"" + nickUtente + "\" con password vuota.");
            return -2;
        } else { //Registra il nuovo utente
            WQUtente user = new WQUtente(nickUtente, password);
            userBase.put(nickUtente, user);
            saveServer();
            WQServerController.gui.addRegistered(user);
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" registrato con successo!");
            return 0;
        }
    }

    /**
     * Ritorna l'utente desiderato
     * @param nickUtente L'utente da ottenere
     * @return L'utente
     */
    public WQUtente ottieniUtente(String nickUtente) {
        return userBase.get(nickUtente);
    }

    /**
     * Procedura di login per un utente già registrato
     * @param nickUtente Il nickname dell'utente
     * @param password La password dell'utente
     * @return 0 se il login va a buon fine, -1 se l'utente non esiste, -2 se la password è sbagliata o -3 se
     * l'utente risulta già collegato
     */
    public synchronized int login(String nickUtente, String password, WQHandler handler){
        if (userBase.containsKey(nickUtente.toLowerCase())) { //Se l'utente esiste
            if (userBase.get(nickUtente.toLowerCase()).password.equals(password)) { //Se la password corretta
                if (!loggedIn.containsKey(nickUtente.toLowerCase())) { //Se non è già collegato
                    loggedIn.put(nickUtente, handler);
                    WQServerController.gui.addOnline(nickUtente);
                    WQUtente loggedUser = userBase.get(nickUtente);
                    for (String friend : loggedUser.friends) { //Segnala alla lista amici che l'utente si è collegato
                        try { loggedIn.get(friend).send("answer:" + nickUtente + " si è appena collegato."); }
                        catch (NullPointerException ignored) {} //Solo agli amici attualmente connessi
                    }
                    return 0;
                } else return -3;
            } else return -2;
        } else return -1;
    }

    /**
     * Effettua il logout dell'utente dal servizio
     * @param nickUtente Il nickname dell'utente che si vuole scollegare
     */
    public void logout(String nickUtente){
        loggedIn.remove(nickUtente); //Rimuove l'utente dalla lista degli utenti connessi
        WQServerController.gui.removeOnline(nickUtente);
        WQUtente loggedUser = userBase.get(nickUtente);
        for (String friend : loggedUser.friends) { //Segnala agli amici che l'utente è andato offline
            try { loggedIn.get(friend).send("answer:" + nickUtente + " è andato offline."); }
            catch (NullPointerException ignored) {} //Anche qua solo agli amici attualmente connessi
        }
    }

    /**
     * Visualizza la lista degli utenti attualmente online
     * @return JSON rappresentante la lista degli utenti online ({@code ArrayList<WQUtente>})
     */
    public String mostraOnline(){
        ArrayList<WQUtente> online = new ArrayList<>();
        for (String username : loggedIn.keySet()) {
            online.add(userBase.get(username));
        }
        Gson gson = new Gson();
        return gson.toJson(online);
    }

    /**
     * Usata da un utente per aggiungerne un altro alla propria lista amici. L'operazione aggiunge {@code nickAmico} alla lista amici di {@code nickUtente} e viceversa. Non è necessario che {@code nickAmico} accetti l'amicizia.
     * @param nickUtente L'utente che vuole aggiungere alla propria lista amici
     * @param nickAmico L'utente da aggiungere alla lista amici
     * @return 0 se l'operazione va a buon fine, -1 se {@code nickAmico} non esiste, -2 se l'amicizia è già esistente, -3 se {@code nickUtente} non esiste
     */
    public synchronized int aggiungiAmico(String nickUtente, String nickAmico){
        if (!nickUtente.toLowerCase().equals(nickAmico.toLowerCase())) {
            if (userBase.containsKey(nickUtente)) { //Se l'utente esiste
                if (userBase.containsKey(nickAmico)) { //Se l'amico esiste
                    if (!userBase.get(nickUtente).friends.contains(userBase.get(nickAmico).username)) {
                        //Se non sono già amici
                        userBase.get(nickUtente).friends.add(nickAmico); //Aggiunge l'utente alla lista amici dell'amico
                        userBase.get(nickAmico).friends.add(nickUtente); //Aggiunge l'amico alla lista amici dell'utente
                        if (loggedIn.get(nickAmico) != null) { //Manda la nuova lista amici all'amico se questo è online
                            String json = listaAmici(nickAmico);
                            String str = "friendlist:".concat(json);
                            loggedIn.get(nickAmico).send(str);
                        }
                        saveServer(); //Salva i dati del server
                        return 0;
                    } else return -2; //Gli utenti sono già amici
                } else return -1; //L'amico non esiste
            } else return -3; //L'utente non esiste, non dovrebbe mai accadere
        } else return -4; //Tentativo di aggiungere sé stesso come amico
    }

    /**
     * Usata per visualizzare la propria lista amici
     * @param nickUtente L'utente che vuole visualizzare la propria lista amici
     * @return JSON rappresentante la lista amici ({@code WQUtente[]})
     */
    public String listaAmici(String nickUtente) {
        ArrayList<WQUtente> friendList = new ArrayList<>();
        for (String name : userBase.get(nickUtente).friends) { //Costruisce la lista e ne manda l'array in formato JSON
            friendList.add(userBase.get(name));
        }
        Gson gson = new Gson();
        return gson.toJson(friendList.toArray());
    }

    /**
     * {@code nickUtente} sfida {@code nickAmico} se quest'ultimo appartiene alla lista amici.
     * @param nickUtente
     * @param nickAmico
     */
    public void sfida(String nickUtente, String nickAmico){
        WQServerController.gui.updateStatsText("Sfida da " + nickUtente + " a " + nickAmico + "!");
        if (userBase.containsKey(nickAmico)) { //Se l'utente richiesto esiste
            if (userBase.get(nickUtente).friends.contains(nickAmico)) { //Se sono amici
                if (loggedIn.containsKey(nickAmico)) { //Se l'amico è online
                    WQServerController.gui.updateStatsText("Richiesta mandata a " + nickAmico);
                    //Manda la richiesta di sfida via UDP
                    DatagramSocket dtgSkt = loggedIn.get(nickAmico).challenge(nickUtente, this.port);
                    if (dtgSkt != null) { //Se la richiesta è stata accettata
                        WQServerController.gui.updateStatsText("Che abbia inizio la sfida tra " + nickUtente + " e " + nickAmico + "!");
                        WQHandler sfidanteUtente = loggedIn.get(nickUtente);
                        sfidanteUtente.send("challengeRound:1"); //Segnala l'inizio della sfida all'utente
                        WQHandler sfidanteAmico = loggedIn.get(nickAmico);
                        sfidanteAmico.send("challengeRound:1"); //Segnala l'inizio della sfida all'amico
                        //Utente e amico avranno tempo di prepararsi mentre il server scarica le traduzioni delle parole
                        try {
                            //Lettura del dizionario dal file
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("dizionario")));
                            ArrayList<String> dizionario = new ArrayList<>();
                            String line;
                            while ( (line = bufferedReader.readLine()) != null ) { dizionario.add(line); }
                            bufferedReader.close();

                            //Scelta delle K parole casuali dal dizionario e download delle traduzioni
                            int K = 6; //parole scelte a caso dal dizionario
                            HashMap<String, ArrayList<String>> randomWords = new HashMap<>();
                            Collections.shuffle(dizionario);
                            for (int i = 0; i < K; i ++) {
                                String word = dizionario.get(i); //Parola casuale i dal dizionario

                                //Richiesta GET al server per la traduzione
                                URL url = new URL("https://api.mymemory.translated.net/get?q=" + word + "!&langpair=it|en");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String inputLine;
                                StringBuilder content = new StringBuilder();
                                while ((inputLine = in.readLine()) != null) {
                                    content.append(inputLine);
                                }
                                in.close();

                                //Parsing del JSON ricevuto
                                JsonElement json = new JsonParser().parse(content.toString());
                                JsonArray translationsArray = json.getAsJsonObject().get("matches").getAsJsonArray();
                                ArrayList<String> translations = new ArrayList<>();
                                for (JsonElement match : translationsArray) {
                                    String translation = match.getAsJsonObject().get("translation").getAsString()
                                            .toLowerCase()
                                            .replaceAll("!", "")
                                            .replaceAll("\\.", "")
                                            .replaceAll("-", "");
                                    //Rimozione di caratteri non alfabetici trovati durante i test
                                    translations.add(translation);
                                }
                                randomWords.put(word, translations); //Coppia parola-traduzioni
                                /*System.out.print(word + ":");
                                for (String tr : translations) System.out.print(" " + tr);
                                System.out.println();*/
                                //Le traduzioni vengono memorizzate in lowercase e verranno controllate a meno di
                                //caratteri non alfabetici presenti (ad esempio, una traduzione di "virus" è "VIRUS!")
                            }
                            new Thread(new WQServerChallenge(sfidanteUtente, sfidanteAmico, randomWords, this)).start();
                        } catch (FileNotFoundException ignored) { //Se il server è installato correttamente non accade
                        } catch (IOException ex) {WQServerController.gui.updateStatsText(ex.getMessage());}

                    } else { //La sfida viene rifiutata o scade il timeout
                        WQServerController.gui.updateStatsText("Sfida tra " + nickUtente + " e " + nickAmico + " rifiutata!");
                        WQHandler sfidanteUtente = loggedIn.get(nickUtente);
                        sfidanteUtente.send("challengeRound:-2");
                    }
                } else { //L'amico non è online
                    WQServerController.gui.updateStatsText("Sfida tra " + nickUtente + " e " + nickAmico + " rifiutata!");
                    WQHandler sfidanteUtente = loggedIn.get(nickUtente);
                    sfidanteUtente.send("challengeRound:-2");
                }
            } else { //Gli utenti non sono amici
                WQServerController.gui.updateStatsText("Sfida tra " + nickUtente + " e " + nickAmico + " rifiutata!");
                WQHandler sfidanteUtente = loggedIn.get(nickUtente);
                sfidanteUtente.send("challengeRound:-2");
            }
        } else { //Lo sfidante richiesto non esiste
            WQServerController.gui.updateStatsText("Sfida tra " + nickUtente + " e " + nickAmico + " rifiutata!");
            WQHandler sfidanteUtente = loggedIn.get(nickUtente);
            sfidanteUtente.send("challengeRound:-2");
        }
    }

    /**
     * Conclude la sfida fra gli utenti, calcolando i punteggi finali e mandando agli sfidanti il risultato della sfida
     * @param sfidanteUtente L'utente sfidante
     * @param pointsUtente Il punteggio dell'utente
     * @param sfidanteAmico L'amico sfidato
     * @param pointsAmico Il punteggio dell'amico
     */
    public void fineSfida(WQHandler sfidanteUtente, int pointsUtente, WQHandler sfidanteAmico, int pointsAmico) {
        WQUtente utente = userBase.get(sfidanteUtente.username); //Riferimenti dell'handler
        WQUtente amico = userBase.get(sfidanteAmico.username);
        utente.points += pointsUtente; //Aggiorna subito i punteggi totali degli utenti con i punti totalizzati
        amico.points += pointsAmico;
        if (pointsUtente > pointsAmico) { //L'utente vincitore guadagna Z = 5 punti bonus
            utente.points += 5;
            sfidanteAmico.send("answer:challengeLose " + amico.points); //Segnala il risultato agli utenti
            sfidanteUtente.send("answer:challengeWin " + utente.points);
        }
        else if (pointsAmico > pointsUtente) {
            amico.points += 5;
            sfidanteUtente.send("answer:challengeLose " + utente.points);
            sfidanteAmico.send("answer:challengeWin " + amico.points);
        } else {
            sfidanteUtente.send("answer:challenge " + utente.points);
            sfidanteAmico.send("answer:challenge " + amico.points);
        }
        saveServer(); //Salva i dati del server
    }

    /**
     * Restituisce il punteggio di {@code nickUtente}
     * @param nickUtente L'utente di cui si vuole sapere il punteggio
     * @return Il punteggio di {@code nickUtente}
     */
    public int mostraPunteggio(String nickUtente) {
        return userBase.get(nickUtente).points;
    }

    /**
     * Restituisce un JSON rappresentante la classifica degli utenti amici di {@code nickUtente}.
     * @param nickUtente L'utente che vuole conoscere la classifica
     * @return JSON rappresentante la classifica ({@code ArrayList<WQUtente>})
     */
    public String mostraClassifica(String nickUtente) {
        ArrayList<WQUtente> listaOrdinata = new ArrayList<>();
        for (String name : userBase.get(nickUtente).friends) {
            listaOrdinata.add(userBase.get(name));
        }
        listaOrdinata.add(userBase.get(nickUtente)); //Aggiunge alla lista tutti gli amici e l'utente stesso
        listaOrdinata.sort(new Comparator<WQUtente>() { //Riordina la lista in base al punteggio
            @Override
            public int compare(WQUtente o1, WQUtente o2) {
                return Integer.compare(o2.points, o1.points);
            }
        });
        Gson gson = new Gson();
        return gson.toJson(listaOrdinata);
    }

    /**
     * Salva {@code fileData} sul file {@code fileName}
     * @param filename Il nome del file su cui salvare
     * @param fileData I dati da salvare
     * @return 0 se il salvataggio è andato a buon fine, -1 se il file non può essere creato o è una directory, -2 se c'è IOException
     */
    private int saveToFile(String filename, String fileData) {
        WQServerController.gui.updateStatsText("Salvataggio dati su " + filename);
        try {
            FileOutputStream fileout = new FileOutputStream(new File(filename));
            fileout.write(fileData.getBytes(StandardCharsets.UTF_8));
            return 0;
        } catch (FileNotFoundException ex) {
            return -1;
        } catch (IOException ex) {
            return -2;
        }
    }

    /**
     * Inizializza il WQServer da file
     * @throws RemoteException
     */
    private void loadServer() throws RemoteException {
        //Inizializzazione RMI
        WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, port+1);
        LocateRegistry.createRegistry(port+1);
        Registry r = LocateRegistry.getRegistry(port+1);
        r.rebind("WordQuizzle_530527", stub);
        WQServerController.gui.updateStatsText("Registrazioni aperte su porta " + (port+1));

        try {
            //Lettura della lista degli utenti da file
            FileInputStream userBaseFile = new FileInputStream(new File("userBase"));
            String userBaseJson = "";
            byte[] buff = new byte[512];
            ByteBuffer bBuff;
            int n;
            do {
                n = userBaseFile.read(buff);
                bBuff = ByteBuffer.wrap(buff);
                userBaseJson = userBaseJson.concat(StandardCharsets.UTF_8.decode(bBuff).toString());
            } while (n > -1);
            userBaseFile.close();

            //Parsing del json
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(userBaseJson));
            reader.setLenient(true);
            Type type = new TypeToken<HashMap<String, WQUtente>>(){}.getType();
            userBase = gson.fromJson(reader, type);
            WQServerController.gui.addAllRegistered(userBase.values()); //aggiornamento GUI
        } catch (IOException e) {
            userBase = new HashMap<>();
        }
        loggedIn = new HashMap<>();
    }

    /**
     * Salva su file i dati del server
     */
    public void saveServer() {
        Gson gson = new Gson();
        int n = saveToFile("userBase", gson.toJson(userBase));
        if (n == -1) WQServerController.gui.updateStatsText("Impossibile creare il file \"userBase\"");
        if (n == -2) WQServerController.gui.updateStatsText("IOException durante la scrittura sul file \"userBase\"");
    }

    /**
     * Restituisce un paragrafo di testo con varie info sullo stato attuale del server
     * @return Le info
     */
    public String getInfos() {
        String str = "WordQuizzle server! Realizzato da Federico Matteoni, mat. 530257\n";
        long onlineMillis = System.currentTimeMillis() - onlineSince;
        int seconds = (int) (onlineMillis/1000) % 60;
        int minutes = (int) (onlineMillis/(1000*60)) % 60;
        int hours = (int) (onlineMillis / (1000*60*60)) % 60;
        str = str.concat("Online da " + (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + seconds + "s\n");
        str = str.concat("Con " + loggedIn.size() + " utenti connessi su " + userBase.size() + " registrati.\n");
        str = str.concat("Online sulle porte " + this.port + " e " + (this.port+1) + "\n");
        str = str.concat("\nUtenti registrati:\n");
        for(WQUtente usr : userBase.values()) {
            str = str.concat("-  " + usr.toString() + "\n");
        }

        return str;
    }

    /**
     * Ferma l'esecuzione del server
     */
    public void stopServer() {
        saveServer();
        this.running = false;
    }

    /**
     * Costruttore del server, inizializza e avvia
     * @param porta La porta sulla quale ascoltare le comunicazioni in entrata
     */
    public WQServer(int porta) {
        ServerSocketChannel srvSkt = null; //Socket di ascolto delle connessioni in entrata
        SocketChannel skt = null; //Socket da smistare all'handler per la gestione del singolo client
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(25); //Vari handler
        this.port = porta;
        this.running = true;
        WQServerController.server = this; //Si "registra" alla classe di comunicazione con la GUI

        try {
            WQServerController.gui.updateStatsText(Time.from(Calendar.getInstance().toInstant()).toString());
            loadServer();

            //Apertura alle connessioni
            srvSkt = ServerSocketChannel.open();
            srvSkt.socket().bind(new InetSocketAddress(porta));
            srvSkt.configureBlocking(false);
            WQServerController.gui.updateStatsText("Online!");
            onlineSince = System.currentTimeMillis();
            WQServerController.gui.serverIsOnline(port);

            //Loop principale di ascolto e smistamento
            do {
                skt = srvSkt.accept();
                if (skt != null) {
                    threadPool.execute(new WQHandler(this, skt));
                    WQServerController.gui.addThreadsText();
                }
                else Thread.sleep(500);
            } while (running);

            //Procedura di spegnimento
            WQServerController.gui.updateStatsText("Spegnimento del server");
            threadPool.shutdown();
            try { threadPool.awaitTermination(1, TimeUnit.SECONDS); }
            catch (InterruptedException ignored) {}
            srvSkt.close();
            WQServerController.gui.serverIsOffline();
        } catch (Exception e) {WQServerController.gui.updateStatsText(e.getMessage()); WQServerController.gui.serverIsOffline(); e.printStackTrace();}
    }
}
