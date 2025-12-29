# P2P Kakuro

Gioco **Kakuro multiplayer** su rete **peer-to-peer decentralizzata** con **DHT** (Distributed Hash Table).

---

## Perché è un sistema decentralizzato?

### Nessun Server Centrale
```
    ❌ Sistema Centralizzato          ✅ Questo Progetto (P2P)
    
         [SERVER]                      Peer A ←→ Peer B
          /    \                           ↖   ↗
       Peer A  Peer B                      Peer C
       
    Server controlla tutto          I peer comunicano direttamente
    Se server muore = TUTTO FERMO   Se un peer esce = gli altri continuano
```

### Come funziona la DHT

I dati delle partite **non sono salvati su un server**, ma **distribuiti tra tutti i giocatori**:

```
"Partita1" → Hash SHA-1 → a7f3b2... → Salvato su Peer più vicino all'hash
"Partita2" → Hash SHA-1 → c9d1e4... → Salvato su altro Peer
```

- Ogni peer ha una parte dei dati
- Se cerchi una partita, la DHT sa su quale peer trovarla
- **Nessun single point of failure**: se un peer esce, i dati sono replicati su altri

## Cos'è il Kakuro?

Il Kakuro è un puzzle logico-matematico, simile al Sudoku ma con le somme.

**Regole:**
- Inserisci numeri da **1 a 9** nelle celle bianche
- La somma dei numeri deve essere uguale all'**indizio** nella cella nera
- Non puoi **ripetere** lo stesso numero nello stesso gruppo (orizzontale o verticale)

---

## Compilazione

```bash
mvn clean package -DskipTests
```

---

## Come Avviare

| Chi | Comando |
|-----|---------|
| **Primo giocatore** | `java -jar target/p2p-kakuro-1.0.jar -lp 4001` |
| **Altri giocatori** | `java -jar target/p2p-kakuro-1.0.jar -ma <IP> -mp 4001 -lp 4002` |

| Parametro | Significato |
|-----------|-------------|
| `-lp` | Porta locale |
| `-ma` | Indirizzo IP del primo giocatore |
| `-mp` | Porta del primo giocatore |

**Per giocare su PC diversi:** sostituisci `<IP>` con l'indirizzo IPv4 del primo giocatore.

| Sistema | Comando per trovare IP |
|---------|----------------------|
| Windows | `ipconfig` |
| Linux | `ip addr` o `hostname -I` |
| Mac | `ifconfig` |

---

## Come Giocare

1. Inserisci nickname
2. Crea o unisciti a una sfida
3. Attendi almeno 2 giocatori
4. Host clicca "Avvia"
5. Clicca celle bianche, inserisci numeri 1-9
6. **+1 punto** = primo a trovare un numero corretto

---

## Tecnologie

| Tecnologia | Uso |
|------------|-----|
| **TomP2P** | Rete P2P decentralizzata + DHT |
| **Java Swing + FlatLaf** | Interfaccia grafica moderna |
| **SHA-1** | Hashing per chiavi DHT |
| **Maven** | Build e dipendenze |

---

