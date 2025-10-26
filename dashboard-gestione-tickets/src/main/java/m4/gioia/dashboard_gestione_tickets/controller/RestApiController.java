package m4.gioia.dashboard_gestione_tickets.controller;

import java.time.LocalDate;
import java.util.*;


import m4.gioia.dashboard_gestione_tickets.model.Category;
import m4.gioia.dashboard_gestione_tickets.model.NoteTicket;
import m4.gioia.dashboard_gestione_tickets.model.Ticket;
import m4.gioia.dashboard_gestione_tickets.model.User;
import m4.gioia.dashboard_gestione_tickets.repository.CategoryRepository;
import m4.gioia.dashboard_gestione_tickets.repository.NoteTicketRepository;
import m4.gioia.dashboard_gestione_tickets.repository.TicketRepository;
import m4.gioia.dashboard_gestione_tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;



@Controller
@RequestMapping("")
public class RestApiController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteTicketRepository noteTicketRepository;

    //  verificare ruoli
    private boolean hasAuthority(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    @GetMapping("/")
    public String getHome(Model model, Authentication authentication) {

        if(authentication == null){
            return "redirect:/login";
        }
        int totalTicketsCompletati = 0;
        int totalTicketsInCorso = 0;
        int totalTicketsDaFare = 0;
        int totalTickets = 0;

        if (hasAuthority(authentication, "ADMIN")) {
            totalTicketsCompletati = ticketRepository.findTicketByStatus(Ticket.Status.COMPLETATO).size();
            totalTicketsInCorso = ticketRepository.findTicketByStatus(Ticket.Status.IN_CORSO).size();
            totalTicketsDaFare = ticketRepository.findTicketByStatus(Ticket.Status.DA_FARE).size();
        } else if (authentication != null) {
            // Utente  non admin
            String username = authentication.getName();
            totalTicketsCompletati = ticketRepository.findTicketByStatusAndUser_Username(Ticket.Status.COMPLETATO, username).size();
            totalTicketsInCorso = ticketRepository.findTicketByStatusAndUser_Username(Ticket.Status.IN_CORSO, username).size();
            totalTicketsDaFare = ticketRepository.findTicketByStatusAndUser_Username(Ticket.Status.DA_FARE, username).size();
        }

        totalTickets = totalTicketsCompletati + totalTicketsInCorso + totalTicketsDaFare;

        model.addAttribute("TicketTotali", totalTickets);
        model.addAttribute("TicketCompletati", totalTicketsCompletati);
        model.addAttribute("TicketInCorso", totalTicketsInCorso);
        model.addAttribute("TicketDaFare", totalTicketsDaFare);

        return "index";
    }

    @GetMapping("/tickets")
    public String getTickets(Model model,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Authentication authentication) {
        Set<Ticket> ticketToShow = new HashSet<>();

        if (hasAuthority(authentication, "OPERATOR") && authentication != null) {
            ticketToShow.addAll(ticketRepository.findByUser_Username(authentication.getName()));
        }

        if (hasAuthority(authentication, "ADMIN")) {
            ticketToShow.addAll(ticketRepository.findAll());
        }

        if (keyword != null) {
            ticketToShow.removeIf(ticket -> !ticket.getTitolo().contains(keyword));
        }

        model.addAttribute("ticketList", ticketToShow);
        return "tickets/displayTicket";
    }

    @PostMapping("/tickets/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        ticketRepository.deleteById(id);
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/edit/{id}")
    public String edit(@PathVariable Long id, Model model, Authentication authentication, CsrfToken csrfToken) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if (optTicket.isEmpty()) return "redirect:/tickets";

        Ticket ticket = optTicket.get();

        model.addAttribute("ticket", ticket);
        model.addAttribute("isAdmin", hasAuthority(authentication, "ADMIN"));
        model.addAttribute("isOperator", hasAuthority(authentication, "OPERATOR"));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.Status.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));

        model.addAttribute("_csrf", csrfToken); // 🔹 aggiungi CSRF

        return "tickets/edit";
    }

    @PostMapping("/tickets/edit/{id}")
    public String updateTicket(
            @PathVariable Long id,
            @Valid @ModelAttribute("ticket") Ticket formTicket,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // 1️⃣ Recupera il ticket esistente
        Ticket oldTicket = ticketRepository.findById(id).orElse(null);
        if (oldTicket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket not found.");
            return "redirect:/tickets";
        }

        // 2️⃣ Recupera l'utente assegnato al ticket dal DB (oggetto gestito da JPA)
        User user = userRepository.findById(formTicket.getUser().getId())
                .orElse(oldTicket.getUser()); // fallback all'utente esistente

        // 3️⃣ Mantieni la data di creazione originale
        formTicket.setDataCreazione(oldTicket.getDataCreazione());
        // 4️⃣ Mantieni eventuali note esistenti
        formTicket.setNotes(oldTicket.getNotes());

        // 5️⃣ Se ci sono errori di validazione, torna al form
        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", hasAuthority(authentication, "ADMIN"));
            model.addAttribute("isOperator", hasAuthority(authentication, "OPERATOR"));
            model.addAttribute("ticket", oldTicket);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.Status.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "tickets/edit";
        }

        // 6️⃣ Associa l'utente gestito al ticket
        formTicket.setUser(user);

        // 7️⃣ Salva il ticket
        ticketRepository.save(formTicket);

        // 8️⃣ Aggiorna lo status dell'utente in base ai ticket ancora attivi
        List<Ticket> activeTickets = ticketRepository.findByUserIdAndStatusIn(
                user.getId(),
                Arrays.asList(Ticket.Status.DA_FARE, Ticket.Status.IN_CORSO)
        );

        if (activeTickets.isEmpty()) {
            user.setStatus(User.UserStatus.INACTIVE); // Nessun ticket attivo
        } else {
            user.setStatus(User.UserStatus.ACTIVE); // Almeno un ticket DA_FARE o IN_CORSO
        }

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Ticket updated successfully!");
        return "redirect:/tickets";
    }

    @PostMapping("/users/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") User.UserStatus newStatus) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "redirect:/users";

        // Controlla se l'utente ha ticket attivi
        List<Ticket> activeTickets = ticketRepository.findByUserIdAndStatusIn(
                user.getId(),
                Arrays.asList(Ticket.Status.DA_FARE, Ticket.Status.IN_CORSO)
        );

        // Se ci sono ticket attivi, non permettere INACTIVE
        if (!activeTickets.isEmpty() && newStatus == User.UserStatus.INACTIVE) {
            // Mantieni lo status precedente e opzionalmente aggiungi messaggio di errore
            // Esempio: redirect con alert
            return "redirect:/users/profile/" + id + "?error=activeTickets";
        }

        // Altrimenti aggiorna lo status scelto manualmente
        user.setStatus(newStatus);
        userRepository.save(user);

        return "redirect:/users/profile/" + id;
    }


    @GetMapping("/tickets/create")
    public String getTicketsForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.Status.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
        model.addAttribute("ticket", new Ticket());
        return "tickets/form";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@Valid @ModelAttribute("ticket") Ticket formTicket,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               Authentication authentication) {
        if (ticketRepository.findByTitolo(formTicket.getTitolo()).isPresent()) {
            bindingResult.addError(new ObjectError("titolo", "There's already a ticket for this problem!"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.Status.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "tickets/form";
        }

        formTicket.setDataCreazione(LocalDate.now());
        ticketRepository.save(formTicket);

        userRepository.findByUsername(formTicket.getUser().getUsername()).ifPresent(userFound -> {
            if (formTicket.getStatus() == Ticket.Status.DA_FARE || formTicket.getStatus() == Ticket.Status.IN_CORSO) {
                userFound.setStatus(User.UserStatus.ACTIVE);
                userRepository.save(userFound);
            }
        });

        redirectAttributes.addFlashAttribute("successMessage", "Ticket created successfully");
        return "redirect:/tickets";
    }

    // --- Modifiche per gestione note ---

    @GetMapping("/tickets/{id}")
    public String showTicket(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        model.addAttribute("empty", ticketOpt.isEmpty());
        ticketOpt.ifPresent(ticket -> model.addAttribute("ticket", ticket));

        // Indica se è ADMIN (solo admin può aggiungere note)
        model.addAttribute("isAdmin", hasAuthority(authentication, "ADMIN"));

        return "tickets/ticketDetail";
    }

    @PostMapping("/tickets/{id}/notes/add")
    public String addNote(@PathVariable Long id,
                          @RequestParam("note") String noteContent,
                          Authentication authentication) {
        // Solo ADMIN può aggiungere note
        if (!hasAuthority(authentication, "ADMIN")) {
            return "redirect:/tickets/" + id;
        }

        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isEmpty()) {
            return "redirect:/tickets";
        }

        Ticket ticket = ticketOpt.get();

        // Trovo l'utente corrente (autore della nota)
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/tickets/" + id;
        }

        NoteTicket note = new NoteTicket();
        note.setTicket(ticket);
        note.setAutore(userOpt.get());
        note.setContenuto(noteContent);
        note.setCreatedAt(LocalDate.now());

        noteTicketRepository.save(note);

        return "redirect:/tickets/" + id;
    }
}

