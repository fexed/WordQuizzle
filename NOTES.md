## Note sul progetto
* Protocollo client-server
  * Scambio di messaggi `<tipo>:<dati>`. Esempi:
    * Risposta a login: `answer:OK` oppure `answer:ERR`
    * Richiesta di login: `login:<nomeutente> <password>`
    * Tipologie:
      * `challenge`
      * `login`
      * `showonline`
      * `addfriend`
      * `friendlist`
      * `points`
      * `ranking`