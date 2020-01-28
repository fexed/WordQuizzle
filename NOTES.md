## Note sul progetto
#### Procedura di login
 * Client: `login:<nomeutente> <password>`
 * Server: `answer:` seguito da
   * `OK` in caso di login corretto
   * `ERRn` in caso di errore, dove n è un numero
     * 1 se l'utente non esiste
     * 2 se la password è sbagliata
     * 3 se l'utente è gia collegato al server da un altro client
 * Server: `<json>` dell'`WQUtente` dell'utente connesso
 * Client: `challengePort:<n>`, dove `<n>` è il numero di porta per le richieste UDP o -1 in caso di errore.

#### Aggiunta di un amico
 * Client `addfriend:<nickAmico>`
 * Server `answer:OKFREN` oppure `answer:ERR <messaggio>`
 
#### Richiesta lista amici
 * Client `showfriendlist`
 * Server `friendlist:<json>`
 
#### Richiesta della classifica
 * Client `showranking`
 * Server `ranking:<json>`

#### Richiesta della lista di utenti online
 * Client `showonlinelist`
 * Server `onlinelist:<json>`

#### Richiesta del punteggio attuale
 * Client `showpoints`
 * Server `userpoints:<n>`

#### Sfida
K = 6 parole scelte da un dizionario di N = 100 parole.

La richiesta di sfida deve essere accettata in T1 = 10 secondi e la sfida può durare al massimo T2 = K*5s = 30s
 
Ogni risposta giusta assegna X = 2 punti mentre ogni risposta sbagliata ne toglie Y = 1. Il vincitore si aggiudica Z = 5
punti bonus. La comunicazione avviene nel seguente modo:
 * ClientA > Server: `challengeRequest:<nickAmico>`
 * Server > ClientB: `challengeRequest:<nickUtente>`
 * B > S: risposta
   * Se B risponde con `challengeResponse:NO` o se scatta il timeout
     * S > A: `challengeRound:-2`
   * Se B risponde con `challengeResponse:OK` entro il timeout
     * S > A **e** B: `challengeRound:1` (*contemporaneamente*)
     * S > *: `challengeRound:<parola>`
     * \* > S: `challengeAnswer:<traduzione>` *oppure* `challengeAnswer:-1` se scatta il timeout
     * ... *ripeti* ...
     * S > *: `challengRound:-3`
     * S > ClientVincitore: `answer:challengeWin <punti>`
     * S > ClientPerdente: `answer:challengeLose <punti>`
     * Se pareggio, invece, allora S > *: `answer:challenge <punti>`