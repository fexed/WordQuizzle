## Note sul progetto
### Protocollo client-server
##### Scambio di messaggi `<tipo>:<dati>`. Messaggi:
 * `challenge:<nickAmico>`
 * `login:<nickUtente> <password>`
 * `showonline`
 * `addfriend:<nickAmico>`
   * `answer:OKFREN` seguito da JSON rappresentante il nuovo `WQUtente` con la lista amici aggiornata
   * `answer:ERR <messaggio>` in caso di errore
 * `friendlist`
 * `points`
 * `ranking`
 * `notif:<testo>`
 * `challengeRound:`
   * `1` start
   * `-1` end
   * `-2` rifiutata
   * `-3` fine attendi risultati