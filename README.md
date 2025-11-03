# Sudoku Solver UI

Sudoku Solver UI è un'applicazione desktop Java/Swing che permette di creare, importare, risolvere e studiare schemi di sudoku.
Il progetto nasce come evoluzione dell'applicazione open-source di Leonardo Savona e combina un editor visuale con un risolutore
passo-passo basato su più strategie logiche.

## Caratteristiche principali
- **Editor visuale**: componi nuove griglie 9x9 con il mouse o la tastiera, assegna la difficoltà e salva il puzzle per riutilizzarlo.
- **Archivio locale**: tutti i sudoku vengono salvati nella cartella `sudokus/` in formato di testo (`.txt`) con metadati (`.meta`).
- **Import assistito da immagine**: un flusso guidato ti aiuta ad allineare una foto o uno screenshot; un riconoscitore OCR leggero propone i numeri e segnala quelli a bassa confidenza.
- **Modalità risoluzione**: scegli un puzzle dall'archivio, avvia un timer, usa la modalità "note" per annotare candidati e controlla quante occorrenze sono già state usate.
- **Cronologia delle soluzioni**: il pannello *Soluzioni* applica una pipeline di strategie logiche (Basic, Possibili Valori, Coppie/Tris di candidati, Hidden Couples, X-Wing, ecc.) e consente di sfogliare tutti i passaggi generati dal risolutore.
- **Packaging pronto all'uso**: il build Maven produce sia un JAR eseguibile (con dipendenze incluse) sia un archivio ZIP con applicazione e cartella dati.

## Struttura del progetto
```
├── pom.xml                       # configurazione Maven (shade + assembly plugin)
├── src/main/java/leonardo/savona/sudoku/
│   ├── SudokuApp.java            # punto di ingresso, avvia l'interfaccia Swing
│   ├── ui/                       # componenti dell'interfaccia (frame principale, pannelli, dialog)
│   ├── solver/                   # motore di soluzione e strategie logiche
│   ├── ocr/                      # import assistito con riconoscimento dei numeri
│   ├── repository/ & io/         # persistenza di puzzle e metadati
│   └── model/                    # oggetti dominio (board, metadati, utilità)
├── src/main/assembly/dist.xml    # descrittore per il pacchetto ZIP di distribuzione
└── sudokus/                      # archivio locale di puzzle (.txt) e metadati associati (.meta)
```

### Formato dei puzzle
I file `.txt` contengono 9 righe, ognuna con 9 valori separati da spazi:
- `0` indica una cella vuota;
- numeri da `1` a `9` sono considerati valori fissi quando il puzzle viene ricaricato.

Per ogni puzzle è presente un file `.meta` con informazioni aggiuntive (ad esempio livello di difficoltà, tempo di risoluzione o note).

## Requisiti
- Java 8 o superiore
- Apache Maven 3.8+

## Come compilare ed eseguire
```bash
mvn clean package
java -jar target/SudokuSolverUI-1.0-shaded.jar
```

Il comando `mvn clean package` crea anche `target/SudokuSolverUI-1.0-dist.zip` che include il JAR e una cartella `sudokus/` pronta per essere distribuita su altre macchine.

## Suggerimenti d'uso
- Premi **Crea sudoku** per aprire l'editor: salva spesso per generare l'hash univoco del file nella cartella `sudokus/`.
- Premi **Risolvi sudoku** per allenarti: il timer parte solo dopo aver premuto *Start* e puoi mettere in pausa la sessione in qualsiasi momento.
- Usa **Soluzioni** per analizzare i passaggi logici del risolutore e capire come l'algoritmo avanza.

## Licenza
Il progetto è distribuito con la stessa licenza del repository originale. Controlla la licenza del progetto sorgente di riferimento per ulteriori dettagli.
