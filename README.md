# Ticket Platform

Web application per la gestione dei ticket di assistenza sviluppata con Spring Boot, MySQL e frontend HTML/CSS.

## 📌 Descrizione
Ticket Platform è una dashboard che consente la gestione dei ticket tramite ruoli differenti.  
L’interfaccia e le funzionalità cambiano in base all’utente autenticato (Admin o Operatore).

## 🎯 Obiettivo del progetto
Progetto realizzato per esame con l’obiettivo di:
- applicare architettura MVC
- implementare autenticazione e autorizzazione
- gestire ruoli e permessi
- integrare backend, database e frontend

## 👥 Ruoli e funzionalità

### Admin
- Creazione nuovi ticket
- Visualizzazione di tutti i ticket
- Assegnazione ticket agli operatori disponibili
- Gestione stato operatori

### Operatore
- Visualizzazione ticket assegnati
- Modifica stato ticket
- Impostazione stato personale (disponibile / non disponibile)

## 🛠 Tecnologie utilizzate
- Spring Boot
- Spring Security
- MySQL
- JPA / Hibernate
- HTML / CSS

## 🚀 Miglioramenti futuri
- Sistema di notifiche
- Dashboard statistiche
- Gestione priorità ticket
- Storico attività utenti
