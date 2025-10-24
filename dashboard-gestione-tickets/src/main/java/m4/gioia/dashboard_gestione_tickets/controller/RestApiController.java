package m4.gioia.dashboard_gestione_tickets.controller;

import java.time.LocalDate;
import java.util.*;


import m4.gioia.dashboard_gestione_tickets.model.Ticket;
import m4.gioia.dashboard_gestione_tickets.model.User;
import m4.gioia.dashboard_gestione_tickets.repository.CategoryRepository;
import m4.gioia.dashboard_gestione_tickets.repository.TicketRepository;
import m4.gioia.dashboard_gestione_tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
            totalTicketsCompletati = ticketRepository.findTicketByStato(Ticket.Status.COMPLETATO).size();
            totalTicketsInCorso = ticketRepository.findTicketByStato(Ticket.Status.IN_CORSO).size();
            totalTicketsDaFare = ticketRepository.findTicketByStato(Ticket.Status.DA_FARE).size();
        } else if (authentication != null) {
            // Utente  non admin
            String username = authentication.getName();
            totalTicketsCompletati = ticketRepository.findTicketByStatoAndUser_Username(Ticket.Status.COMPLETATO, username).size();
            totalTicketsInCorso = ticketRepository.findTicketByStatoAndUser_Username(Ticket.Status.IN_CORSO, username).size();
            totalTicketsDaFare = ticketRepository.findTicketByStatoAndUser_Username(Ticket.Status.DA_FARE, username).size();
        }
        // Se authentication è null, totalTickets rimangono a 0 → evita il crash

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
        return "tickets/displayTickets";
    }

    @GetMapping("tickets/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        ticketRepository.deleteById(id);
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if (optTicket.isEmpty()) {
            return "redirect:/tickets";
        }

        Ticket ticket = optTicket.get();
        model.addAttribute("isAdmin", hasAuthority(authentication, "ADMIN"));
        model.addAttribute("isOperator", hasAuthority(authentication, "OPERATOR"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.Status.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));

        return "/tickets/editTicket";
    }

    @PostMapping("/tickets/edit/{id}")
    public String update(@Valid @ModelAttribute("ticket") Ticket formTicket,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication) {
        Ticket oldTicket = ticketRepository.findById(formTicket.getId()).orElse(null);
        if (oldTicket == null) {
            return "redirect:/tickets";
        }

        Optional<User> userOpt = userRepository.findByUsername(formTicket.getUser().getUsername());
        formTicket.setCreationDate(oldTicket.getDataCreazione());

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", hasAuthority(authentication, "ADMIN"));
            model.addAttribute("isOperator", hasAuthority(authentication, "OPERATOR"));
            model.addAttribute("ticket", oldTicket);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.Status.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "/tickets/editTicket";
        }

        ticketRepository.save(formTicket);

        User userFound = userOpt.orElse(oldTicket.getUser());

        if (formTicket.getStato() == Ticket.Status.DA_FARE || formTicket.getStato() == Ticket.Status.IN_CORSO) {
            userFound.setStatus(User.UserStatus.DISPONIBILE);
            userRepository.save(userFound);
        }

        return "redirect:/tickets";
    }

    @GetMapping("/tickets/create")
    public String getTicketsForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.Status.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
        model.addAttribute("ticket", new Ticket());
        return "tickets/ticketsForm";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@Valid @ModelAttribute("ticket") Ticket formTicket,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               Authentication authentication) {
        if (ticketRepository.findByTitolo(formTicket.getTitolo()).isPresent()) {
            bindingResult.addError(new ObjectError("title", "There's already a ticket for this problem!"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.Status.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "tickets/ticketsForm";
        }

        formTicket.setCreationDate(LocalDate.now());
        ticketRepository.save(formTicket);

        userRepository.findByUsername(formTicket.getUser().getUsername()).ifPresent(userFound -> {
            if (formTicket.getStato() == Ticket.Status.DA_FARE || formTicket.getStato() == Ticket.Status.IN_CORSO) {
                userFound.setStatus(User.UserStatus.DISPONIBILE);
                userRepository.save(userFound);
            }
        });

        redirectAttributes.addFlashAttribute("successMessage", "Ticket created successfully");
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String showTicket(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        model.addAttribute("empty", ticketOpt.isEmpty());
        ticketOpt.ifPresent(ticket -> model.addAttribute("ticket", ticket));
        return "tickets/ticketDetail";
    }
}

